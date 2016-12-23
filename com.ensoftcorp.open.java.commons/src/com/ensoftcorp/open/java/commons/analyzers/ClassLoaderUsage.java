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

public class ClassLoaderUsage extends Analyzer {

	@Override
	public String getName(){
		return "Class Loader Usage";
	}
	
	@Override
	public String getDescription() {
		return "Finds calls to Class Loaders.";
	}

	@Override
	public String[] getAssumptions() {
		return new String[]{"All uses of Reflection are through java.lang.reflect package."};
	}

	@Override
	public Map<String, Result> getResults(Q context) {
		// get all the java.lang.reflect methods
		Q containsEdges = Common.universe().edgesTaggedWithAny(XCSG.Contains).retainEdges();
		Q supertypeEdges = Common.universe().edgesTaggedWithAny(XCSG.Supertype).retainEdges();
		Q loaders = supertypeEdges.reverse(Common.typeSelect("java.lang", "ClassLoader"));
		Q loaderMethods = containsEdges.forwardStep(loaders).nodesTaggedWithAny(XCSG.Method).difference(SetDefinitions.objectMethodOverrides());
		HashMap<String,Result> results = new HashMap<String,Result>();
		for(Node loaderMethod : loaderMethods.eval().nodes()){
			Q interaction = CommonQueries.interactions(context, Common.toQ(loaderMethod), XCSG.Call);
			if(!interaction.eval().edges().isEmpty()){
				results.put(Analyzer.getUUID(), new Result("Class Loader Usage", interaction));
			}
		}
		return results;
	}
	
}
