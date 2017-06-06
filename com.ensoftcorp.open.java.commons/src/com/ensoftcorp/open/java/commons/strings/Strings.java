package com.ensoftcorp.open.java.commons.strings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.commons.analysis.SetDefinitions;
import com.ensoftcorp.open.commons.utilities.FormattedSourceCorrespondence;
import com.ensoftcorp.open.java.commons.log.Log;
import com.ensoftcorp.open.java.commons.wishful.JavaStopGap;

import dk.brics.automaton.Automaton;
import dk.brics.string.StringAnalysis;
import dk.brics.string.annotation.Type;
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
import dk.brics.string.intermediate.AssertStatement;
import dk.brics.string.intermediate.Call;
import dk.brics.string.intermediate.Field;
import dk.brics.string.intermediate.Method;
import dk.brics.string.intermediate.Nop;
import dk.brics.string.intermediate.Return;
import dk.brics.string.intermediate.Statement;
import dk.brics.string.intermediate.StringAssignment;
import dk.brics.string.intermediate.StringInit;
import dk.brics.string.intermediate.StringStatement;
import dk.brics.string.intermediate.Variable;
import dk.brics.string.intermediate.VariableType;
import dk.brics.string.intermediate.operations.AliasAnalysis;
import dk.brics.string.intermediate.operations.AliasAssertionAnalysis;
import dk.brics.string.intermediate.operations.FieldUsageAnalysis;
import dk.brics.string.intermediate.operations.Intermediate2FlowGraph;
import dk.brics.string.intermediate.operations.LivenessAnalysis;
import dk.brics.string.intermediate.operations.OperationAssertionAnalysis;
import dk.brics.string.intermediate.operations.ReachingDefinitions;
import dk.brics.string.java.MethodTranslator;
import dk.brics.string.java.StaticStringTypes;
import dk.brics.string.java.StaticStringTypesNull;
import dk.brics.string.mlfa.MLFA;
import dk.brics.string.mlfa.MLFAStatePair;
import dk.brics.string.mlfa.operations.MLFA2Automaton;
import dk.brics.string.stringoperations.Basic;
import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.LongType;
import soot.ShortType;

public class Strings {
	
	private static class AtlasVariableManager {
		private Application application;
		private Variable nothingVar;
		private Map<Node, Variable> locals = new HashMap<Node, Variable>();
		private Map<Node, Field> fields = new HashMap<Node, Field>();
		private AtlasTranslationContext translationContext;
		
		public AtlasVariableManager(Application application, AtlasTranslationContext translationContext) {
			this.application = application;
			this.nothingVar = application.createVariable(VariableType.NONE);
			this.translationContext = translationContext;
		}
		
		public Variable createVariable(VariableType type) {
		    return application.createVariable(type);
		}
		
		public Variable getNothing() {
			return nothingVar;
		}
		
		public Variable getLocal(Node local) {
			return null;
			// TODO: implement
			
//			Variable var = locals.get(local);
//			if (var == null) {
//				var = application.createVariable(jt.fromSootType(local.getType()));
//				locals.put(local, var);
//			}
//			return var;
		}
		
		public Field getField(Node field) {
			return null;
			// TODO: implement
			
//			Field field = fields.get(field);
//			if (field == null) {
//				VariableType type = jt.fromSootType(sootField.getType());
//				field = application.createField(type, sootField.isStatic());
//				fields.put(sootField, field);
//			}
//			return field;
		}
	}
	
	private static interface AtlasTranslationContext {
		public Automaton getParameterType(Node method, int paramIndex);
		public Automaton getMethodReturnType(Node method);
		public Automaton getFieldType(Node field);
		public Application getApplication();
		public AtlasVariableManager getVariableManager();
		public Method getMethod(Node method);
		public Variable getParameter(Node method, Node ref);
		public List<Node> getTargetsOf(Node expr);
		public Method getToStringMethod(Node clazz);
		public boolean isHotspot(Node expr);
		public void setExpressionVariable(Node value, Variable variable);
		public Variable getExpressionVariable(Node value);
		public boolean isSubtypeOf(Node a, Node b);
		public VariableType fromAtlasType(Type type);
	}
	
	private static class AtlasHotspotInfo {

		private Node node;
		private Statement statement;
		private FormattedSourceCorrespondence fsc;
		
		public AtlasHotspotInfo(Node node, Statement statement){
			this.node = node;
			this.fsc = FormattedSourceCorrespondence.getSourceCorrespondent(node);
			this.statement = statement;
		}
		
		public Node getNode() {
			return node;
		}

		public Statement getStatement() {
			return statement;
		}

		public String getSourcefile() {
			try {
				return fsc.getRelativeFile();
			} catch (Exception e){
				return fsc.getFile().getAbsolutePath();
			}
		}

		public String getClassName() {
			return CommonQueries.getQualifiedTypeName(Common.toQ(node).parent().eval().nodes().one());
		}

		public String getMethodName() {
			return CommonQueries.getQualifiedFunctionName(node);
		}

		public Integer getLineNumber() {
			try {
				return (int) fsc.getStartLineNumber();
			} catch (IOException e) {
				return -1;
			}
		}
		
	}
	
	private static class AtlasMethodTranslator {

		public List<AtlasHotspotInfo> translateMethod(Node method, Atlas2Intermediate atlas2Intermediate) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	private static class Atlas2Intermediate implements AtlasTranslationContext {
		
		private Application application;
		private Map<Node, Statement> translationMap;
		private AtlasMethodTranslator methodTranslator;
		private AtlasVariableManager variableManager;
		private AtlasSet<Node> hotspots;
		
		/**
	     * Map from classes to their <code>toString</code> methods.
	     */
	    private Map<Node, Method> tostring_targets = new HashMap<Node, Method>();

	    /**
	     * Map from names of a classes to their <code>toString</code> methods.
	     */
	    private Map<String, Method> tostring_methods = new HashMap<String, Method>();

	    /**
	     * Hotspots for <code>toString</code> methods. Used by
	     * {@link StringAnalysis#getTypeAutomaton(VariableType)}.
	     */
	    private Map<Node, StringStatement> tostring_hotspots = new HashMap<Node, StringStatement>();

	    /**
	     * Cache of automata for primitive types and null type.
	     */
	    private Map<Type, Automaton> type_automaton = new HashMap<Type, Automaton>();
		
		/**
	     * Methods being generated.
	     */
	    private List<Method> methods = new LinkedList<Method>();

	    /**
	     * Map from signatures to methods.
	     */
	    private Map<String, Method> sms_m = new HashMap<String, Method>();

	    /**
	     * For each method signature, map from real-arg number to relevant-arg
	     * number.
	     */
	    private Map<String, int[]> sms_sa_ma = new HashMap<String, int[]>();

	    /**
	     * For each method signature, map from relevant-arg number to real-arg
	     * number.
	     */
	    private Map<String, int[]> sms_ma_sa = new HashMap<String, int[]>();
	    
	    /**
	     * Map from Jimple expression to intermediate representation statement.
	     */
	    private Map<Node, Statement> trans_map = new HashMap<Node, Statement>();

	    /**
	     * Map from Jimple expression to source file name.
	     */
	    private Map<Node, String> sourcefile_map = new HashMap<Node, String>();

	    /**
	     * Map from Jimple expression to class name.
	     */
	    private Map<Node, String> class_map = new HashMap<Node, String>();

	    /**
	     * Map from Jimple expression to method name.
	     */
	    private Map<Node, String> method_map = new HashMap<Node, String>();

	    /**
	     * Map from Jimple expression to line number.
	     */
	    private Map<Node, Integer> line_map = new HashMap<Node, Integer>();
		
		public Atlas2Intermediate(AtlasSet<Node> hotspots){
			this.hotspots = hotspots;
			this.application = new Application();
			this.translationMap = new HashMap<Node,Statement>();
			this.variableManager = new AtlasVariableManager(application, this);
		}
		
		public Application translateApplicationClasses(){
			Log.info("Translating classes to intermediate form...");
			makeMethods();
	        makeWrapperMethod();
	        makeToStringMethods();
	        translate();
	        removeNops();
			return application;
		}
		
		private void removeNops() {
			Set<Statement> protectedNops = new HashSet<Statement>();
	        for (Method m : methods) {
	        	protectedNops.clear();
	        	// assertions referring to a nop statement must have their target updated to the nop's predecessor,
	        	// or in case there are multiple predecessors, the nop must not be removed
	        	for (Statement s : m.getStatements()) {
	        		if (s instanceof AssertStatement) {
	        			AssertStatement a = (AssertStatement)s;
	        			Statement target = a.targetStatement;
	        			while (target instanceof Nop && target.getPreds().size() == 1) {
	        				target = target.getPreds().iterator().next();
	        				a.targetStatement = target;
	        			}
	        			if (target instanceof Nop) {
	        				protectedNops.add(target);
	        			}
	        		}
	        	}
	        	
	            for (Statement s : new ArrayList<Statement>(m.getStatements())) {
	                if (s instanceof Nop && !protectedNops.contains(s)) {
	                    m.removeNop((Nop) s);
	                }
	            }
	        }
		}

		private void translate() {
			for (Node applicationClass : getApplicationClasses()) {
	            for (Node method : getClassMethods(applicationClass)) {
	            	if(!method.taggedWith(XCSG.abstractMethod) && !method.taggedWith(XCSG.Java.nativeMethod)){
	            		// method is concrete
	            		List<AtlasHotspotInfo> hotspots = methodTranslator.translateMethod(method, this);

	                    // add all the hotspots we found
	                    for (AtlasHotspotInfo hotspot : hotspots) {
	                        registerHotspot(hotspot);
	                    }
	            	}
	            }
	        }
		}
		
		private void registerHotspot(AtlasHotspotInfo hotspot) {
	        Node node = hotspot.getNode();
	        trans_map.put(node, hotspot.getStatement());
	        sourcefile_map.put(node, hotspot.getSourcefile());
	        class_map.put(node, hotspot.getClassName());
	        method_map.put(node, hotspot.getMethodName());
	        line_map.put(node, hotspot.getLineNumber());
	    }

		/**
	     * Makes <code>toString</code> methods for basic wrapper classes and
	     * application classes.
	     */
	    private void makeToStringMethods() {
	        // Make basic tostring methods
			makeBasicToStringMethod(null, "java.lang", "Object", Basic.makeObjectString());
			makeBasicToStringMethod(BooleanType.v(), "java.lang", "Boolean", Basic.makeBooleanString());
			makeBasicToStringMethod(ByteType.v(), "java.lang", "Byte", Basic.makeByteString());
			makeBasicToStringMethod(CharType.v(), "java.lang", "Character", Basic.makeCharacterString());
			makeBasicToStringMethod(DoubleType.v(), "java.lang", "Double", Basic.makeDoubleString());
			makeBasicToStringMethod(FloatType.v(), "java.lang", "Float", Basic.makeFloatString());
			makeBasicToStringMethod(IntType.v(), "java.lang", "Integer", Basic.makeIntegerString());
			makeBasicToStringMethod(LongType.v(), "java.lang", "Long", Basic.makeLongString());
			makeBasicToStringMethod(ShortType.v(), "java.lang", "Short", Basic.makeShortString());
	        
	        // Make tostring methods for application classes
	        // Link toString calls to the hotspots for all superclasses of the receiver type
	        for (Node applicationClass : getApplicationClasses()) {
	            Method m = new Method(application, getClassName(applicationClass) + ".toString", new Variable[0]);
	            methods.add(m);
	            tostring_methods.put(getClassName(applicationClass), m);
	            Variable var = application.createVariable(VariableType.STRING);
	            StringStatement spot = new StringAssignment(var, var);
	            m.addStatement(spot);
	            Return ret = new Return(var);
	            m.addStatement(ret);
	            spot.addSucc(ret);
	            tostring_hotspots.put(applicationClass, spot); // these hotspots are used by StringAnalysis.getTypeAutomaton
	            
	            AtlasSet<Node> subtypes;
	            Q supertypeEdges = Common.universe().edges(XCSG.Supertype);
	            if(applicationClass.taggedWith(XCSG.Java.Interface)){
	            	// get implementers of application class
	            	subtypes = supertypeEdges.predecessors(Common.toQ(applicationClass)).eval().nodes();
	            } else {
	            	// get subclasses of including application class
	            	subtypes = supertypeEdges.reverseStep(Common.toQ(applicationClass)).eval().nodes();
	            }
	            for(Node subtype : subtypes){
	            	 if(applicationClass.taggedWith(XCSG.Java.Interface)){
	            		 continue;
	            	 }
	            	 Method target = tostring_targets.get(subtype);
		                if (target == null){
		                    continue;
		                }
		                Call call = new Call(var, target, new Variable[0]);
		                m.addStatement(call);
		                m.getEntry().addSucc(call);
		                call.addSucc(spot);
	            }
	        }
	    }

		/**
	     * Makes a method that simulates the <code>toString</code> method of a basic
	     * wrapper classes.
	     */
	    void makeBasicToStringMethod(Type prim, String packageName, String typeName, Automaton a) {
	    	String classname = packageName + "." + typeName;
	        Method m = new Method(application, classname + ".toString", new Variable[0]);
	        Node c = Common.typeSelect(packageName, typeName).eval().nodes().one();
	        Variable var = application.createVariable(VariableType.STRING);
	        StringStatement ss = new StringInit(var, a);
	        m.addStatement(ss);
	        m.getEntry().addSucc(ss);
	        Return ret = new Return(var);
	        m.addStatement(ret);
	        ss.addSucc(ret);
	        methods.add(m);
	        tostring_targets.put(c, m);
	        tostring_methods.put(classname, m);
	        type_automaton.put(prim, a);
	    }

		private void makeWrapperMethod() {
			// TODO Auto-generated method stub
			
		}

		private void makeMethods() {
			for (Node applicationClass : getApplicationClasses()) {
				for (Node method : getClassMethods(applicationClass)) {
					List<Variable> vars = new LinkedList<Variable>();
					List<Type> params = getParameterTypes(method);
					int[] sa_ma = new int[params.size()];
					int[] ma_sa = new int[params.size()];
					int ma = 0;
					int sa = 0;
					for (Type pt : params) {
						// if (isSType(pt)) {
						if (fromAtlasType(pt) != VariableType.NONE) {
							vars.add(makeVariable(pt));
							sa_ma[sa] = ma;
							ma_sa[ma] = sa;
							ma++;
						} else {
							sa_ma[sa] = -1;
						}
						sa++;
					}
					Variable[] var_array = vars.toArray(new Variable[0]);
					Method m = new Method(application, getMethodName(method), var_array);
					methods.add(m);
					sms_m.put(getSignature(method), m);
	                sms_sa_ma.put(getSignature(method), sa_ma);
	                sms_ma_sa.put(getSignature(method), ma_sa);
				}
			}
		}
		
		/**
		 * Returns the method signature
		 * @param method
		 * @return
		 */
		private String getSignature(Node method) {
			return method.getAttr(JavaStopGap.SIGNATURE).toString();
		}

		/**
	     * Makes a new variable, using the given Soot type.
	     */
		private Variable makeVariable(Type t) {
	        return application.createVariable(fromAtlasType(t));
	    }
		
		/**
		 * Returns the qualified class name
		 * @param clazz
		 * @return
		 */
		private String getClassName(Node clazz) {
			return CommonQueries.getQualifiedTypeName(clazz);
		}

		/**
		 * Returns the qualified method name
		 * @param method
		 * @return
		 */
		private String getMethodName(Node method) {
			return CommonQueries.getQualifiedFunctionName(method);
		}

		/**
		 * Returns the method parameter types
		 * @param method
		 * @return
		 */
		private List<Type> getParameterTypes(Node method) {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * Returns the class's methods
		 * @param clazz
		 * @return
		 */
		private AtlasSet<Node> getClassMethods(Node clazz){
			return Common.toQ(clazz).children().nodes(XCSG.Method).eval().nodes();
		}

		/**
		 * Returns the application classes
		 * @return
		 */
		private AtlasSet<Node> getApplicationClasses() {
			return SetDefinitions.app().nodes(XCSG.Method).eval().nodes();
		}

		/**
		 * Returns the node to statement mapping
		 * @return
		 */
		public Map<Node, Statement> getTranslationMap(){
			return translationMap;
		}

		@Override
		public Automaton getParameterType(Node method, int paramIndex) {
			// TODO: implement
			return null;
		}

		@Override
		public Automaton getMethodReturnType(Node method) {
			// TODO: implement
			return null;
		}

		@Override
		public Automaton getFieldType(Node field) {
			// TODO: implement
			return null;
		}

		@Override
		public Application getApplication() {
			return application;
		}

		@Override
		public AtlasVariableManager getVariableManager() {
			return variableManager;
		}

		@Override
		public Method getMethod(Node method) {
			// TODO: implement
			return null;
		}

		@Override
		public Variable getParameter(Node method, Node ref) {
			// TODO: implement
			return null;
		}

		@Override
		public List<Node> getTargetsOf(Node expr) {
			// TODO: implement
			return null;
		}

		@Override
		public Method getToStringMethod(Node clazz) {
			// TODO: implement
			return null;
		}

		@Override
		public boolean isHotspot(Node expr) {
			return hotspots == null || hotspots.contains(expr);
		}

		@Override
		public void setExpressionVariable(Node value, Variable variable) {
			// TODO: implement
			
		}

		@Override
		public Variable getExpressionVariable(Node value) {
			// TODO: implement
			return null;
		}

		@Override
		public boolean isSubtypeOf(Node a, Node b) {
			// TODO: implement
			return false;
		}

		@Override
		public VariableType fromAtlasType(Type type) {
			// TODO: implement
			return null;
		}
		
	}
	
	public static void execute(AtlasSet<Node> hotspots, boolean debug){
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
        Atlas2Intermediate atlas2Intermediate = new Atlas2Intermediate(hotspots);
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
//	public static void inspect(Automaton automaton){
//        if (automaton.isFinite()) {
//            Log.info("A finite number of strings:");
//            for (String s : automaton.getFiniteStrings()) {
//            	Log.info("\"" + Misc.escape(s) + "\"");
//            }
//        } else if (automaton.complement().isEmpty()) {
//        	Log.info("All possible strings.");
//        } else {
//        	Log.info("Infinitely many strings with common prefix:");
//        	Log.info(automaton.getCommonPrefix());
//        }
//	}
//	
//	private static Statement getStatement(Node controlFlowNode){
//		return null;
//	}
	
}
