package com.ensoftcorp.open.java.commons.analyzers;

import java.util.LinkedList;
import java.util.List;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.java.core.script.Common;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.commons.analyzers.Property;

public class ReflectionUsage extends Property {

	@Override
	public String getName(){
		return "Reflection Usage";
	}
	
	@Override
	public String getDescription() {
		return "Finds usage of Java Reflection.";
	}

	@Override
	public String[] getAssumptions() {
		return new String[]{"All uses of Reflection are through java.lang.reflect package."};
	}

	@Override
	public List<Result> getResults(Q context) {
		// get all the java.lang.reflect methods
		Q reflectionPackage = Query.universe().pkg("java.lang.reflect");
		Q reflectionMethods = reflectionPackage.contained().nodes(XCSG.Method);
		Q objectMethodOverrides = Common.edges(XCSG.Overrides).reverse(
				Common.typeSelect("java.lang", "Object").children().nodes(XCSG.Method));
		reflectionMethods = reflectionMethods.difference(objectMethodOverrides, Common.methods("getName"), Common.methods("getSimpleName"));
		
		List<Result> results = new LinkedList<Result>();
		List<Q> qs = CommonQueries.interactions2(Query.codemap(), context, reflectionMethods, XCSG.Call);
		for (Q q : qs) {
			results.add(new Result("Reflection Usage", q));
		}
		
		return results;
	}
	
}
