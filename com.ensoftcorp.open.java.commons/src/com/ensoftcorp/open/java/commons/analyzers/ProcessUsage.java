package com.ensoftcorp.open.java.commons.analyzers;

import java.util.HashMap;
import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.java.core.script.Common;
import com.ensoftcorp.atlas.java.core.script.CommonQueries;
import com.ensoftcorp.open.commons.analyzers.Analyzer;

public class ProcessUsage extends Analyzer {

	@Override
	public String getName(){
		return "Process Usage";
	}
	
	@Override
	public String getDescription() {
		return "Finds calls to methods that would allow the application to run a shell command.";
	}

	@Override
	public String[] getAssumptions() {
		return new String[]{"The only way to run shell commands is by directly invoking the Runtime.exec method."};
	}

	@Override
	public Map<String, Result> getResults(Q context) {
		Q runtimeType = Common.typeSelect("java.lang", "Runtime");
		Q declaresEdges = Common.universe().edgesTaggedWithAny(XCSG.Contains).retainEdges();
		Q runtimeMethods = declaresEdges.forwardStep(runtimeType).nodesTaggedWithAny(XCSG.Method);
		Q execMethods = runtimeMethods.intersection(Common.methods("exec"));
		HashMap<String,Result> results = new HashMap<String,Result>();
		for(Node execMethod : execMethods.eval().nodes()){
			Q interaction = CommonQueries.interactions(context, Common.toQ(execMethod), XCSG.Call);
			if(!interaction.eval().edges().isEmpty()){
				results.put(Analyzer.getUUID(), new Result("Process Usage", interaction));
			}
		}
		return results;
	}
	
}
