package com.ensoftcorp.open.java.commons.strings.conversion;

import java.util.HashMap;
import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.Node;

import dk.brics.string.intermediate.Application;
import dk.brics.string.intermediate.Field;
import dk.brics.string.intermediate.Variable;
import dk.brics.string.intermediate.VariableType;

public class AtlasVariableManager {
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
		
//		Variable var = locals.get(local);
//		if (var == null) {
//			var = application.createVariable(jt.fromSootType(local.getType()));
//			locals.put(local, var);
//		}
//		return var;
	}
	
	public Field getField(Node field) {
		return null;
		// TODO: implement
		
//		Field field = fields.get(field);
//		if (field == null) {
//			VariableType type = jt.fromSootType(sootField.getType());
//			field = application.createField(type, sootField.isStatic());
//			fields.put(sootField, field);
//		}
//		return field;
	}
}