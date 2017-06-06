package com.ensoftcorp.open.java.commons.strings.conversion;

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
import com.ensoftcorp.open.java.commons.log.Log;
import com.ensoftcorp.open.java.commons.wishful.JavaStopGap;

import dk.brics.automaton.Automaton;
import dk.brics.string.StringAnalysis;
import dk.brics.string.annotation.Type;
import dk.brics.string.external.ExternalVisibility;
import dk.brics.string.intermediate.Application;
import dk.brics.string.intermediate.ArrayCorrupt;
import dk.brics.string.intermediate.AssertStatement;
import dk.brics.string.intermediate.Call;
import dk.brics.string.intermediate.FieldAssignment;
import dk.brics.string.intermediate.Method;
import dk.brics.string.intermediate.Nop;
import dk.brics.string.intermediate.ObjectCorrupt;
import dk.brics.string.intermediate.PrimitiveInit;
import dk.brics.string.intermediate.Return;
import dk.brics.string.intermediate.Statement;
import dk.brics.string.intermediate.StringAssignment;
import dk.brics.string.intermediate.StringBufferCorrupt;
import dk.brics.string.intermediate.StringInit;
import dk.brics.string.intermediate.StringStatement;
import dk.brics.string.intermediate.Variable;
import dk.brics.string.intermediate.VariableType;
import dk.brics.string.java.Automatons;
import dk.brics.string.java.ControlFlowBuilder;
import dk.brics.string.java.StaticStringTypes;
import dk.brics.string.stringoperations.Basic;

public class Atlas2Intermediate implements AtlasTranslationContext {

	private Application application;
	private Map<Node, Statement> translationMap;
	private AtlasMethodTranslator methodTranslator;
	private AtlasVariableManager variableManager;
	private AtlasSet<Node> hotspots;
	private ExternalVisibility externallyVisible;
	private StaticStringTypes staticStringTypes;

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

	public Atlas2Intermediate(AtlasSet<Node> hotspots, ExternalVisibility externallyVisible,
			StaticStringTypes staticStringTypes) {
		this.hotspots = hotspots;
		this.application = new Application();
		this.translationMap = new HashMap<Node, Statement>();
		this.variableManager = new AtlasVariableManager(application, this);
	}

	public Application translateApplicationClasses() {
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
			// assertions referring to a nop statement must have their target
			// updated to the nop's predecessor,
			// or in case there are multiple predecessors, the nop must not be
			// removed
			for (Statement s : m.getStatements()) {
				if (s instanceof AssertStatement) {
					AssertStatement a = (AssertStatement) s;
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
				if (!method.taggedWith(XCSG.abstractMethod) && !method.taggedWith(XCSG.Java.nativeMethod)) {
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
		// Link toString calls to the hotspots for all superclasses of the
		// receiver type
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
			tostring_hotspots.put(applicationClass, spot); // these hotspots are
															// used by
															// StringAnalysis.getTypeAutomaton

			AtlasSet<Node> subtypes;
			Q supertypeEdges = Common.universe().edges(XCSG.Supertype);
			if (applicationClass.taggedWith(XCSG.Java.Interface)) {
				// get implementers of application class
				subtypes = supertypeEdges.predecessors(Common.toQ(applicationClass)).eval().nodes();
			} else {
				// get subclasses of including application class
				subtypes = supertypeEdges.reverseStep(Common.toQ(applicationClass)).eval().nodes();
			}
			for (Node subtype : subtypes) {
				if (applicationClass.taggedWith(XCSG.Java.Interface)) {
					continue;
				}
				Method target = tostring_targets.get(subtype);
				if (target == null) {
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

	/**
	 * Makes wrapper method that calls all externally visible methods in
	 * application classes, using arbitrary arguments.
	 */
	void makeWrapperMethod() {
		Method wrapper = new Method(application, "<wrapper>", new Variable[0]);
		methods.add(wrapper);

		// build the wrapper's body
		ControlFlowBuilder cfg = new ControlFlowBuilder(wrapper);
		cfg.moveToStatement(wrapper.getEntry());

		// create a variable holding any string
		Variable anyVar = application.createVariable(VariableType.STRING);
		Statement assignAny = new StringInit(anyVar, Basic.makeAnyString());
		cfg.addStatement(assignAny);

		// create a variable holding the null string
		Variable nullVar = application.createVariable(VariableType.STRING);
		Statement assignNull = new StringInit(nullVar, Automatons.getNull());
		cfg.addStatement(assignNull);

		// initialize externally visible field variables to anything
		// and set string fields to "null"
		for (Node applicationClass : getApplicationClasses()) {
			for (Node field : getClassFields(applicationClass)) {
				// String fields should be assigned to "null" because they are
				// exempt from the
				// null-pointer analysis we use for other objects
				if (field.getType().equals(RefType.v("java.lang.String"))) {
					FieldAssignment assignment = new FieldAssignment(variableManager.getField(field), nullVar);
					cfg.addStatement(assignment);
				}

				// corrupt externally visible fields
				if (externallyVisible.isExternallyVisibleField(field)) {
					VariableType type = fromAtlasType(field.getType());

					if (type == VariableType.NONE) {
						continue;
					}

					Variable fieldInit;

					switch (type) {
					case OBJECT:
					case STRING:
					case PRIMITIVE:
						fieldInit = anyVar;
						break;

					case STRINGBUFFER: {
						fieldInit = application.createVariable(VariableType.STRINGBUFFER);
						Statement s = new StringBufferCorrupt(fieldInit);
						cfg.addStatement(s);
						break;
					}

					case ARRAY: {
						fieldInit = application.createVariable(VariableType.ARRAY);
						Statement s = new ArrayCorrupt(fieldInit);
						cfg.addStatement(s);
						break;
					}
					default:
						throw new RuntimeException("Unknown field type " + type);
					}// switch

					FieldAssignment assignment = new FieldAssignment(variableManager.getField(field), fieldInit);
					cfg.addStatement(assignment);
				}
			}
		}

		// split control here, and call a random externally visible method
		cfg.startBranch();

		// call externally visible methods
		for (Node applicationClass : getApplicationClasses()) {
			for (Node method : getClassMethods(applicationClass)) {
				if (externallyVisible.isExternallyVisibleMethod(method)) {
					Method m = sms_m.get(getSignature(method));
					Variable[] params = m.getEntry().params;
					Variable[] args = new Variable[params.length];
					for (int i = 0; i < params.length; i++) {
						Variable arg = application.createVariable(params[i].getType());
						args[i] = arg;
						Statement s;
						switch (arg.getType()) {
						case STRING:
							s = new StringInit(arg, Basic.makeAnyString());
							break;
						case STRINGBUFFER:
							s = new StringBufferCorrupt(arg);
							break;
						case ARRAY:
							s = new ArrayCorrupt(arg);
							break;
						case PRIMITIVE:
							// TODO: Integers can contain two characters, right?
							// look deeper into which primitive type
							s = new PrimitiveInit(arg, Automaton.makeAnyChar());
							break;
						default:
							s = new ObjectCorrupt(arg);
							// (case NONE or NULL cannot occur because such
							// parameters do not get created for intermediate
							// methods)
						}
						cfg.addStatement(s);
					}
					Variable retvar = makeVariable(method.getReturnType());
					Call c = new Call(retvar, m, args);
					cfg.addStatement(c);
					// If this is toString, remember the return value
					if (method.getAttr(XCSG.name).toString().equals("toString") && method.getParameterCount() == 0
							&& method.getReturnType().toString().equals("java.lang.String")) {
						tostring_targets.put(applicationClass, m);
					}

					cfg.useBranch();
				}
			}
		}

		// also add the possibility of no method being called.
		// in case the application has no externally visible methods, we still
		// want
		// the return statement to be reachable so we don't create a malformed
		// program.
		cfg.useBranch();

		cfg.endBranch();

		// add a return statement
		Return ret = new Return(application.createVariable(VariableType.NONE));
		cfg.addStatement(ret);
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
	 * 
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
	 * 
	 * @param clazz
	 * @return
	 */
	private String getClassName(Node clazz) {
		return CommonQueries.getQualifiedTypeName(clazz);
	}

	/**
	 * Returns the qualified method name
	 * 
	 * @param method
	 * @return
	 */
	private String getMethodName(Node method) {
		return CommonQueries.getQualifiedFunctionName(method);
	}

	/**
	 * Returns the method parameter types
	 * 
	 * @param method
	 * @return
	 */
	private List<Type> getParameterTypes(Node method) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Returns the class's methods
	 * 
	 * @param clazz
	 * @return
	 */
	private AtlasSet<Node> getClassMethods(Node clazz) {
		return Common.toQ(clazz).children().nodes(XCSG.Method).eval().nodes();
	}

	/**
	 * Returns the application classes
	 * 
	 * @return
	 */
	private AtlasSet<Node> getApplicationClasses() {
		return SetDefinitions.app().nodes(XCSG.Method).eval().nodes();
	}

	/**
	 * Returns the node to statement mapping
	 * 
	 * @return
	 */
	public Map<Node, Statement> getTranslationMap() {
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
