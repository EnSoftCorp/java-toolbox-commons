package com.ensoftcorp.open.java.commons.analyzers;

import java.util.LinkedList;
import java.util.List;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.java.core.script.Common;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.commons.analyzers.Property;

public class ExecutorThreadPoolUsage extends Property {

	@Override
	public String getName(){
		return "Executors.newFixedThreadPool Usage";
	}
	
	@Override
	public String getDescription() {
		return "Finds calls to Executors.newFixedThreadPool.";
	}

	@Override
	public String[] getAssumptions() {
		return new String[]{""};
	}

	@Override
	public List<Result> getResults(Q context) {
		Q exitMethods = Common.methodSelect("java.util.concurrent", "Executors", "newFixedThreadPool");
		List<Result> results = new LinkedList<Result>();
		for(Node exitMethod : exitMethods.eval().nodes()){
			Q interaction = CommonQueries.interactions(context, Common.toQ(exitMethod), XCSG.Call);
			if(!interaction.eval().edges().isEmpty()){
				results.add(new Result("Executors.newFixedThreadPool", interaction));
			}
		}
		return results;
	}
	
}
