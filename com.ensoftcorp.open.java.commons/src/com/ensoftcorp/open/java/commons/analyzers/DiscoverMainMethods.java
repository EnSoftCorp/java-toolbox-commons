package com.ensoftcorp.open.java.commons.analyzers;

import java.util.HashMap;
import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.CommonQueries;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.Analyzer;
import com.ensoftcorp.open.commons.analysis.StandardQueries;

public class DiscoverMainMethods extends Analyzer {
	
	@Override
	public String getName(){
		return "Discover Main Methods";
	}
	
	@Override
	public String getDescription() {
		return "Finds valid Java main methods.";
	}
	
	@Override
	public String[] getAssumptions() {
		return new String[]{"Main methods are methods.",
						    "Main methods are case-sensitively named \"main\"",
						    "Main methods are public.", 
						    "Main methods are static.", 
						    "Main methods return void.", 
						    "Main methods take a single one dimensional String array parameter", 
						    "Main methods may be final.", 
						    "Main methods may have restricted floating point calculations.", 
						    "Main methods may be synchronized."};
	}
	
	public static Q findMainMethods() {
		// Step 1) select nodes from the index that are marked as public, static, methods
		Q mainMethods = Common.universe().nodesTaggedWithAll(XCSG.publicVisibility, XCSG.ClassMethod);
		
		// Step 2) select nodes from the public static methods that are named "main"
		mainMethods = mainMethods.methods("main");
		
		// Step 3) filter out methods that are not void return types
		Q returnsEdges = Common.universe().edgesTaggedWithAny(XCSG.Returns).retainEdges();
		Q voidMethods = returnsEdges.predecessors(Common.types("void"));
		mainMethods = mainMethods.intersection(voidMethods);
		
		// Step 4) filter out methods that do not take exactly one parameter
		Q paramEdges = Common.universe().edgesTaggedWithAny(XCSG.HasParameter).retainEdges();
		Q mainMethodParams = paramEdges.successors(mainMethods);
		// methods with no parameters will not have a Parameter edge (and won't be reachable from parameters)
		Q methodsWithNoParams = mainMethods.difference(paramEdges.predecessors(mainMethodParams));
		mainMethods = mainMethods.difference(methodsWithNoParams);
		// methods with 2 or more parameters will have at least one parameter node with attribute 
		// parameterIndex == 1 (index 0 is the first parameter)
		Q mainMethodSecondParams = mainMethodParams.selectNode(XCSG.parameterIndex, 1);
		Q methodsWithTwoOrMoreParams = paramEdges.predecessors(mainMethodSecondParams);
		mainMethods = mainMethods.difference(methodsWithTwoOrMoreParams);
		
		// Step 5) filter out methods that do not take a one dimensional String array
		Q elementTypeEdges = Common.universe().edgesTaggedWithAny(XCSG.ArrayElementType).retainEdges();
		Q stringArraysTypes = elementTypeEdges.predecessors(Common.typeSelect("java.lang", "String"));
		// array types have a arrayTypeDimension attribute
		Q oneDimensionStringArrayType = stringArraysTypes.selectNode(XCSG.Java.arrayTypeDimension, 1);
		Q mainMethodFirstParams = CommonQueries.methodParameter(mainMethods, 0);
		Q typeOfEdges = Common.universe().edgesTaggedWithAny(XCSG.TypeOf);
		Q oneDimensionStringArrayParameters = typeOfEdges.predecessors(oneDimensionStringArrayType);
		Q validMainMethodFirstParams = mainMethodFirstParams.intersection(oneDimensionStringArrayParameters);
		mainMethods = paramEdges.predecessors(validMainMethodFirstParams);
		
		return mainMethods;
	}

	@Override
	public Map<String, Result> getResults(Q context) {
		HashMap<String,Result> results = new HashMap<String,Result>();
		for(Node method : findMainMethods().intersection(context).eval().nodes()){
			results.put(Analyzer.getUUID(), new Result((StandardQueries.getQualifiedFunctionName(method)), Common.toQ(method)));
		}
		return results;
	}
	
}
