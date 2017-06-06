package com.ensoftcorp.open.java.commons.strings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.java.commons.log.Log;

import dk.brics.automaton.Automaton;
import dk.brics.string.Misc;
import dk.brics.string.flow.AssignmentNode;
import dk.brics.string.flow.BinaryNode;
import dk.brics.string.flow.ConcatenationNode;
import dk.brics.string.flow.FlowGraph;
import dk.brics.string.flow.InitializationNode;
import dk.brics.string.flow.NodeVisitor;
import dk.brics.string.flow.UnaryNode;
import dk.brics.string.flow.operations.FlowGraph2Grammar;
import dk.brics.string.grammar.Grammar;
import dk.brics.string.grammar.Nonterminal;
import dk.brics.string.grammar.operations.Grammar2MLFA;
import dk.brics.string.intermediate.Application;
import dk.brics.string.intermediate.Statement;
import dk.brics.string.intermediate.operations.AliasAnalysis;
import dk.brics.string.intermediate.operations.FlowGraphEdgeCreator;
import dk.brics.string.intermediate.operations.FlowGraphNodeCreator;
import dk.brics.string.intermediate.operations.Intermediate2FlowGraph;
import dk.brics.string.intermediate.operations.OperationAssertionAnalysis;
import dk.brics.string.intermediate.operations.ReachingDefinitions;
import dk.brics.string.java.Jimple2Intermediate;
import dk.brics.string.java.Jimple2IntermediateFactoryImpl;
import dk.brics.string.mlfa.MLFA;
import dk.brics.string.mlfa.MLFAStatePair;
import dk.brics.string.mlfa.operations.MLFA2Automaton;
import dk.brics.string.stringoperations.Basic;

public class Strings {
	
	private static class AtlasIntermediate2FlowGraph {

		 /**
	     * Main node for each statement.
	     */
	    private Map<Statement, Node> trans_map;
		
	    private FlowGraphNodeCreator nodeCreator;
	    private FlowGraphEdgeCreator edgeCreator;
	    
		/**
	     * Converts the application.
	     */
	    public FlowGraph convert() {
	    	nodeCreator = new AtlasFlowGraphNodeCreator();
	    	edgeCreator = new AtlasFlowGraphEdgeCreator();
	        return nodeCreator.getGraph();
	    }

	    /**
	     * Returns map containing main node for each statement.
	     * {@link #convert(AliasAnalysis, ReachingDefinitions, OperationAssertionAnalysis)} must be called first.
	     */
	    public Map<Statement, dk.brics.string.flow.Node> getTranslationMap() {
	        return nodeCreator.getTranslationMap();
	    }

	}
	
	public static Automaton resolve(Node statement){
		
		// 1
		// jt = new Jimple2Intermediate(new Jimple2IntermediateFactoryImpl(externallyVisible, taintAnalysisStrategy, staticStringTypes, resolvers));
		// Map<ValueBox, Statement> m1 = jt.getTranslationMap();
		// Map<Node, Statement> m1 = ...
		
		
		Log.info("Computing flow graph...");
		
		AtlasIntermediate2FlowGraph tr = new AtlasIntermediate2FlowGraph();
		FlowGraph flowGraph = tr.convert();
		Log.info("Transforming into grammar...");
        FlowGraph2Grammar f2g = new FlowGraph2Grammar(flowGraph);
        Log.info("Converting to grammar...");
        Grammar r = f2g.convert();
        Log.info("Converting to MLFA...");
        Grammar2MLFA gm = new Grammar2MLFA(r);
        MLFA mlfa = gm.convert();
        dk.brics.string.flow.Node flowNode = tr.getTranslationMap().get(statement);
        Nonterminal nt = f2g.getNonterminal(flowNode);
        MLFAStatePair sp = gm.getMLFAStatePair(nt);
        return new MLFA2Automaton(mlfa).extract(sp);
	}
	
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
	
	private static Statement getStatement(Node controlFlowNode){
		return null;
	}
	
}
