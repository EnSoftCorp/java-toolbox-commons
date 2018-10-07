package com.ensoftcorp.open.java.commons.analyzers;

import java.util.LinkedList;
import java.util.List;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.java.core.script.Common;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.commons.analyzers.Property;

public class ClassLoaderUsage extends Property {

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
	public List<Result> getResults(Q context) {
		Q supertypeEdges = Query.universe().edges(XCSG.Supertype);
		Q loaders = supertypeEdges.reverse(Common.typeSelect("java.lang", "ClassLoader"));
		Q overridesEdges = Query.universe().edges(XCSG.Overrides);
		Q objectMethodOverrides = overridesEdges.reverse(Common.typeSelect("java.lang", "Object").contained().nodes(XCSG.Method));
		Q loaderMethods = loaders.children().nodes(XCSG.Method).difference(objectMethodOverrides);
		Q interactions = Query.resolve(null, CommonQueries.interactions(context, loaderMethods, XCSG.Call));
		
		List<Result> results = new LinkedList<Result>();
		for(Node loaderMethod : loaderMethods.eval().nodes()){
			Q interaction = CommonQueries.interactions(interactions, Common.toQ(loaderMethod), XCSG.Call);
			if(!interaction.eval().edges().isEmpty()){
				results.add(new Result("Class Loader Usage", interaction));
			}
		}
		return results;
	}
	
}
