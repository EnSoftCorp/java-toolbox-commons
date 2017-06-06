package com.ensoftcorp.open.java.commons.strings.conversion;

import java.util.List;

import com.ensoftcorp.atlas.core.db.graph.Node;

import dk.brics.automaton.Automaton;
import dk.brics.string.annotation.Type;
import dk.brics.string.intermediate.Application;
import dk.brics.string.intermediate.Method;
import dk.brics.string.intermediate.Variable;
import dk.brics.string.intermediate.VariableType;

public interface AtlasTranslationContext {

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
