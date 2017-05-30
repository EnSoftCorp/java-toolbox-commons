package com.ensoftcorp.open.java.commons.filters;

import java.util.ArrayList;
import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.filters.InvalidFilterParameterException;
import com.ensoftcorp.open.commons.filters.NodeFilter;

/**
 * Filters callsite nodes based on how whether the callsite parameter(s) are fixed literal values
 * 
 * @author Ben Holland
 */
public class LiteralPassedParameterCallsiteFilter extends NodeFilter {

	private static final String EXCLUDE_MATCHES = "EXCLUDE_MATCHES";
	private static final String PARAMETER_INDEXES = "PARAMETER_INDEXES";

	public LiteralPassedParameterCallsiteFilter() {
		this.addPossibleFlag(EXCLUDE_MATCHES, "Retain only callsites whose selected passed parameters are not literals.");
		this.addPossibleParameter(PARAMETER_INDEXES, String.class, false, "A comma seperated list of integers denoting the parameter indexes to consider. Index values begin at 0. By default all parameters are considered.");
	}
	
	@Override
	public String getName() {
		return "Callsite Literal Value Passed Parameters";
	}

	@Override
	public String getDescription() {
		return "Filters callsite nodes based on how whether the callsite parameter(s) are fixed literal values.";
	}

	@Override
	public Q filter(Q input, Map<String,Object> parameters) throws InvalidFilterParameterException {
		checkParameters(parameters);
		input = super.filter(input, parameters);
		
		Q parameterPassedToEdges = Common.universe().edges(XCSG.ParameterPassedTo);
		Q dataFlowEdges = Common.universe().edges(XCSG.DataFlow_Edge);
		
		AtlasSet<Node> literalOnlyCallsites = new AtlasHashSet<Node>();
		for(Node callsite : input.eval().nodes()){
			Q parametersPass = parameterPassedToEdges.predecessors(Common.toQ(callsite));
			if(this.isParameterSet(PARAMETER_INDEXES, parameters)){
				Integer[] indexes = getParameterIndexes(parameters);
				parametersPass = parametersPass.selectNode(XCSG.parameterIndex, (Object[]) indexes);
			}
			Q parameterPassValues = dataFlowEdges.predecessors(parametersPass);
			// if all passed parameter values are literals, add to results
			long parameterPassValuesSize = parameterPassValues.eval().nodes().size();
			if(parameterPassValuesSize > 0 && parameterPassValuesSize == parameterPassValues.nodes(XCSG.Literal).eval().nodes().size()){
				literalOnlyCallsites.add(callsite);
			}
		}
		
		Q result = Common.toQ(literalOnlyCallsites);
		if(isFlagSet(EXCLUDE_MATCHES, parameters)){
			return input.difference(result);
		} else {
			return result;
		}
	}

	private Integer[] getParameterIndexes(Map<String, Object> parameters) {
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		try {
			String inputString = (String) this.getParameterValue(PARAMETER_INDEXES, parameters);
			inputString = inputString.replaceAll("\\s","");
			String[] indexValues = inputString.split(",");
			for(String indexValue : indexValues){
				indexes.add(Integer.parseInt(indexValue));
			}
		} catch (Exception e){
			throw new RuntimeException(PARAMETER_INDEXES + " must be a comma seperated list of integers!");
		}
		Integer[] result = new Integer[indexes.size()];
		indexes.toArray(result);
		return result;
	}

	@Override
	protected String[] getSupportedNodeTags() {
		return new String[]{ XCSG.CallSite };
	}

}