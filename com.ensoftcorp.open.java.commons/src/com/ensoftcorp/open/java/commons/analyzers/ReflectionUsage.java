package com.ensoftcorp.open.java.commons.analyzers;

import java.util.HashMap;
import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.java.core.script.Common;
import com.ensoftcorp.atlas.java.core.script.CommonQueries;
import com.ensoftcorp.open.commons.analysis.SetDefinitions;
import com.ensoftcorp.open.commons.analyzers.Analyzer;

public class ReflectionUsage extends Analyzer {

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
	public Map<String, Result> getResults(Q context) {
		// get all the java.lang.reflect methods
		Q containsEdges = Common.universe().edgesTaggedWithAny(XCSG.Contains).retainEdges();
		Q reflectionPackage = Common.universe().pkg("java.lang.reflect");
		Q reflectionMethods = containsEdges.forward(reflectionPackage).nodesTaggedWithAny(XCSG.Method);
		reflectionMethods = reflectionMethods.difference(SetDefinitions.objectMethodOverrides(), Common.methods("getName"), Common.methods("getSimpleName"));
		
		HashMap<String,Result> results = new HashMap<String,Result>();
		for(Node reflectionMethod : reflectionMethods.eval().nodes()){
			Q interaction = CommonQueries.interactions(context, Common.toQ(reflectionMethod), XCSG.Call);
			if(!interaction.eval().edges().isEmpty()){
				results.put(Analyzer.getUUID(), new Result("Reflection Usage", interaction));
			}
		}
		
		return results;
	}
	
}
