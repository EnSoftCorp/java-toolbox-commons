package com.ensoftcorp.open.java.commons.analyzers;

import java.util.HashMap;
import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.java.core.script.Common;
import com.ensoftcorp.atlas.java.core.script.CommonQueries;
import com.ensoftcorp.open.commons.analyzers.Analyzer;

public class NativeCodeUsage extends Analyzer {

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
	public Map<String, Result> getResults(Q context) {
		Q nativeMethods = context.nodesTaggedWithAny(XCSG.Java.nativeMethod);
		HashMap<String,Result> results = new HashMap<String,Result>();
		for(Node nativeMethod : nativeMethods.eval().nodes()){
			Q interaction = CommonQueries.interactions(context, Common.toQ(nativeMethod), XCSG.Call);
			if(!interaction.eval().edges().isEmpty()){
				results.put(Analyzer.getUUID(), new Result("Native Method Usage", interaction));
			}
		}
		return results;
	}
	
}
