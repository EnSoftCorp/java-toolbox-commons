package com.ensoftcorp.open.java.commons.strings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.open.java.commons.log.Log;
import com.ensoftcorp.open.java.commons.strings.conversion.Atlas2Intermediate;

import dk.brics.automaton.Automaton;
import dk.brics.string.Misc;
import dk.brics.string.diagnostics.DiagnosticsStrategy;
import dk.brics.string.diagnostics.IntermediateCompletedEvent;
import dk.brics.string.diagnostics.NullDiagnosticsStrategy;
import dk.brics.string.external.ExternalVisibility;
import dk.brics.string.external.PublicExternalVisibility;
import dk.brics.string.flow.FlowGraph;
import dk.brics.string.flow.operations.FlowGraph2Grammar;
import dk.brics.string.grammar.Grammar;
import dk.brics.string.grammar.Nonterminal;
import dk.brics.string.grammar.operations.Grammar2MLFA;
import dk.brics.string.intermediate.Application;
import dk.brics.string.intermediate.Statement;
import dk.brics.string.intermediate.operations.AliasAnalysis;
import dk.brics.string.intermediate.operations.AliasAssertionAnalysis;
import dk.brics.string.intermediate.operations.FieldUsageAnalysis;
import dk.brics.string.intermediate.operations.Intermediate2FlowGraph;
import dk.brics.string.intermediate.operations.LivenessAnalysis;
import dk.brics.string.intermediate.operations.OperationAssertionAnalysis;
import dk.brics.string.intermediate.operations.ReachingDefinitions;
import dk.brics.string.java.StaticStringTypes;
import dk.brics.string.java.StaticStringTypesNull;
import dk.brics.string.mlfa.MLFA;
import dk.brics.string.mlfa.MLFAStatePair;
import dk.brics.string.mlfa.operations.MLFA2Automaton;

public class Strings {

	public static void resolve(AtlasSet<Node> hotspots, boolean debug){
		Log.info("Analyzing...");

		// Assign some default strategies, if none were specified
		ExternalVisibility externallyVisible = new PublicExternalVisibility();
		StaticStringTypes staticStringTypes = new StaticStringTypesNull();
		DiagnosticsStrategy diagnostics = new NullDiagnosticsStrategy();

        diagnostics.analysisStarted();

        //
        //	Create intermediate code
        //
//        jt = new Jimple2Intermediate(new Jimple2IntermediateFactoryImpl(externallyVisible, taintAnalysisStrategy, staticStringTypes, resolvers));
        Atlas2Intermediate atlas2Intermediate = new Atlas2Intermediate(hotspots, externallyVisible, staticStringTypes);
        Application app = atlas2Intermediate.translateApplicationClasses();
        Map<Node, Statement> m1 = atlas2Intermediate.getTranslationMap();
//        num_exps = m1.size() + jt.getNumberOfExpsSkipped();
        
        //
        //	Analyze the intermediate code
        //
        Log.info("Performing field usage analysis...");
        FieldUsageAnalysis fieldUsage = new FieldUsageAnalysis(app.getMethods());
        Log.info("Performing liveness analysis...");
        LivenessAnalysis liveness = new LivenessAnalysis(app);
        Log.info("Performing alias assertion analysis...");
        AliasAssertionAnalysis aliasAssertions = new AliasAssertionAnalysis(app);
        Log.info("Performing alias analysis...");
        AliasAnalysis alias = new AliasAnalysis(app, liveness, fieldUsage, aliasAssertions);
        Log.info("Performing reaching definitions analysis...");
        ReachingDefinitions reachingDefinitions = new ReachingDefinitions(app, liveness, alias);

        // get the intermediate statements corresponding to each hotspot
        Set<Statement> hotspotStatements = new HashSet<Statement>();
        for (Node hotspot : hotspots) {
            Statement statement = m1.get(hotspot);
            if (statement == null) {
                Log.warning("Invalid hotspot");
                continue;
            }
            hotspotStatements.add(statement);
        }
        
        // find invalid assertion statements
        OperationAssertionAnalysis assertions = new OperationAssertionAnalysis(app, reachingDefinitions);
        
        if (debug) {
            Log.info(app.toDot(reachingDefinitions, alias, assertions, hotspotStatements));
        }
        
        // End of intermediate creation. Notify diagnostics.
        diagnostics.intermediateCompleted(new IntermediateCompletedEvent(app, liveness, alias, reachingDefinitions, assertions,
				hotspotStatements));

        //
        //	Create flow graph
        //
        Log.info("Generating flow graph...");
        Intermediate2FlowGraph tr = new Intermediate2FlowGraph(app);
        FlowGraph g = tr.convert(alias, reachingDefinitions, assertions);
        
        // Notify diagnostics of flow graph
        diagnostics.flowGraphCompleted(g);
        
        Map<Statement, dk.brics.string.flow.Node> m2 = tr.getTranslationMap();
        if (debug) {
            Log.info("Statement -> Node:");
            for (Map.Entry<Statement, dk.brics.string.flow.Node> me : m2.entrySet()) {
                Log.info("  " + me.getKey() + " -> " + me.getValue());
            }
        }
        Log.info("Simplifying flow graph...");
        Map<dk.brics.string.flow.Node, dk.brics.string.flow.Node> m3 = g.simplify();
        Set<dk.brics.string.flow.Node> nodes = new HashSet<dk.brics.string.flow.Node>();
        for (Statement ss : hotspotStatements) {
        	dk.brics.string.flow.Node beforeSimplifyNode = m2.get(ss);
        	dk.brics.string.flow.Node n = m3.get(beforeSimplifyNode);
            if (n != null) {
                nodes.add(n);
            }
        }
//        for (StringStatement ss : jt.getToStringHotspotMap().values()) {
//        	dk.brics.string.flow.Node n = m3.get(m2.get(ss));
//            if (n != null) {
//                nodes.add(n);
//            }
//        }
        for (dk.brics.string.flow.Node n : m3.keySet()) { // TODO: inefficient, use entrySet iterator instead
        	dk.brics.string.flow.Node n2 = m3.get(n);
            if (n.isTaint() && n2 != null) {
                n2.setTaint(true);
            }
        }
        if (debug) {
            Log.info(g.toDot(nodes));
        }
        Log.info("Transforming into grammar...");
        FlowGraph2Grammar f2g = new FlowGraph2Grammar(g);
        Grammar r = f2g.convert();
        Set<Nonterminal> hs_nt = new HashSet<Nonterminal>();
        for (dk.brics.string.flow.Node hn : nodes) {
            hs_nt.add(f2g.getNonterminal(hn));
        }
        if (debug) {
            Log.info(r.toString() + "Hotspots: " + hs_nt);
        }

        // Approximate grammar
        Log.info("Cutting operation cycles...");
        r.approximateOperationCycles();
        Log.info("Performing regular approximation...");
        r.approximateNonLinear(hs_nt);
        if (debug) {
            Log.info(r.toString() + "Hotspots: " + hs_nt);
        }
        Log.info("Converting to MLFA...");

        Grammar2MLFA gm = new Grammar2MLFA(r);
        MLFA mlfa = gm.convert();

//        propagateTaint(r);

        for (dk.brics.string.flow.Node n : nodes) {
            Nonterminal nt = f2g.getNonterminal(n);
            MLFAStatePair sp = gm.getMLFAStatePair(nt);
            if (nt.isTaint()) {
                sp.setTaint(true);
            }
        }
        Log.info(mlfa.toString());

        // Make map
        Map<Node, MLFAStatePair> map = new HashMap<Node, MLFAStatePair>();
        for (Node hotspot : hotspots) {
        	dk.brics.string.flow.Node n = m3.get(m2.get(m1.get(hotspot)));
            if (n != null) {
                Nonterminal nt = f2g.getNonterminal(n);
                MLFAStatePair sp = gm.getMLFAStatePair(nt);
                map.put(hotspot, sp);
            }
        }
//        tostring_map = new HashMap<SootClass, MLFAStatePair>();
//        Map<SootClass, StringStatement> tostring_hotspot_map = jt.getToStringHotspotMap();
//        for (Map.Entry<SootClass, StringStatement> tse : tostring_hotspot_map.entrySet()) {
//            SootClass tsc = tse.getKey();
//            StringStatement ss = tse.getValue();
//            Node n = m3.get(m2.get(ss));
//            if (n != null) {
//                Nonterminal nt = f2g.getNonterminal(n);
//                MLFAStatePair sp = gm.getMLFAStatePair(nt);
//                tostring_map.put(tsc, sp);
//            }
//        }
//        sourcefile_map = jt.getSourceFileMap();
//        class_map = jt.getClassNameMap();
//        method_map = jt.getMethodNameMap();
//        line_map = jt.getLineNumberMap();

        MLFA2Automaton mlfa2aut = new MLFA2Automaton(mlfa);
	}
	
//	public static Automaton resolve(Node statement){
//		
//		// 1
//		// jt = new Jimple2Intermediate(new Jimple2IntermediateFactoryImpl(externallyVisible, taintAnalysisStrategy, staticStringTypes, resolvers));
//		// Map<ValueBox, Statement> m1 = jt.getTranslationMap();
//		// Map<Node, Statement> m1 = ...
//		
//		
//		Log.info("Computing flow graph...");
//		
//		AtlasIntermediate2FlowGraph tr = new AtlasIntermediate2FlowGraph();
//		FlowGraph flowGraph = tr.convert();
//		Log.info("Transforming into grammar...");
//        FlowGraph2Grammar f2g = new FlowGraph2Grammar(flowGraph);
//        Log.info("Converting to grammar...");
//        Grammar r = f2g.convert();
//        Log.info("Converting to MLFA...");
//        Grammar2MLFA gm = new Grammar2MLFA(r);
//        MLFA mlfa = gm.convert();
//        dk.brics.string.flow.Node flowNode = tr.getTranslationMap().get(statement);
//        Nonterminal nt = f2g.getNonterminal(flowNode);
//        MLFAStatePair sp = gm.getMLFAStatePair(nt);
//        return new MLFA2Automaton(mlfa).extract(sp);
//	}
//	
	public static void inspect(Automaton automaton){
        if (automaton.isFinite()) {
            Log.info("A finite number of strings:");
            for (String s : automaton.getFiniteStrings()) {
            	Log.info("\"" + Misc.escape(s) + "\"");
            }
        } else if (automaton.complement().isEmpty()) {
        	Log.info("All possible strings.");
        } else {
        	Log.info("Infinitely many strings with common prefix:");
        	Log.info(automaton.getCommonPrefix());
        }
	}

}
