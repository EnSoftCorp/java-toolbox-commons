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

public class NativeCodeUsage extends Property {

	@Override
	public String getName(){
		return "Native Code Usage";
	}
	
	@Override
	public String getDescription() {
		return "Finds calls to native code.";
	}

	@Override
	public String[] getAssumptions() {
		return new String[]{"All native calls are using JNI and are flagged with the keyword native."};
	}

	@Override
	public List<Result> getResults(Q context) {
		Q nativeMethods = context.nodes(XCSG.Java.nativeMethod);
		Q interactions = Query.resolve(null, CommonQueries.interactions(context, nativeMethods, XCSG.Call));
		
		List<Result> results = new LinkedList<Result>();
		for(Node nativeMethod : interactions.intersection(nativeMethods).eval().nodes()){
			Q interaction = CommonQueries.interactions(interactions, Common.toQ(nativeMethod), XCSG.Call);
			if(!interaction.eval().edges().isEmpty()){
				results.add(new Result("Native Method Usage", interaction));
			}
		}
		return results;
	}
	
}
