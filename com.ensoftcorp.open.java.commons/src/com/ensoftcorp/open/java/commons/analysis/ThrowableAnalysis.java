package com.ensoftcorp.open.java.commons.analysis;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.graph.operation.ForwardStepGraph;
import com.ensoftcorp.atlas.core.db.graph.operation.InducedGraph;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.db.set.SingletonAtlasSet;
import com.ensoftcorp.atlas.core.index.common.SourceCorrespondence;
import com.ensoftcorp.atlas.core.query.Attr;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.CommonQueries.TraversalDirection;
import com.ensoftcorp.atlas.core.xcsg.XCSG;

public class ThrowableAnalysis {
	
	private ThrowableAnalysis(){
		// use to prevent object construction
	}
	
	/**
	 * Returns all Throwable types
	 * Throwable types are checked, except for subclasses of Error and RuntimeException types
	 * 
	 * @return
	 */
	public static Q getThrowables() {
		Q supertypes = Query.universe().edges(XCSG.Supertype);
		return supertypes.reverse(Common.typeSelect("java.lang", "Throwable"));
	}

	/**
	 * Returns all checked Throwable types
	 * @return
	 */
	public static Q getCheckedThrowables() {
		return getThrowables().difference(getErrors(), getUncheckedExceptions());
	}
	
	/**
	 * Returns all unchecked Throwable types
	 * @return
	 */
	public static Q getUncheckedThrowables() {
		return getThrowables().difference(getCheckedExceptions());
	}
	
	/**
	 * Returns all Error types
	 * Errors are unchecked Throwable types that subclass Error
	 * 
	 * @return
	 */
	public static Q getErrors() {
		Q supertypes = Query.universe().edges(XCSG.Supertype);
		return supertypes.reverse(Common.typeSelect("java.lang", "Error"));
	}

	/**
	 * Returns all checked exceptions
	 * Checked exceptions extend the Exception type, but do not extend the RuntimeException type
	 * 
	 * @return
	 */
	public static Q getCheckedExceptions() {
		Q supertypes = Query.universe().edges(XCSG.Supertype);
		Q exceptions = supertypes.reverse(Common.typeSelect("java.lang", "Exception"));
		Q uncheckedExceptions = supertypes.reverse(Common.typeSelect("java.lang", "RuntimeException"));
		return exceptions.difference(uncheckedExceptions);
	}

	/**
	 * Returns all unchecked Exceptions
	 * Unchecked exceptions extend the RuntimeException type
	 * 
	 * @return
	 */
	public static Q getUncheckedExceptions() {
		Q supertypes = Query.universe().edges(XCSG.Supertype);
		return supertypes.reverse(Common.typeSelect("java.lang", "RuntimeException"));
	}
	
	/**
	 * Builds a "stack graph". A stack graph combines CALL (per cf), CONTROL_FLOW,
	 * and DECLARES edges to build a complete call graph at the level of PER_CONTRO_FLOW.
	 * Ordinarily, this would have to be done piecewise.
	 * 
	 * Intended use: You have a control flow block X where something interesting happens,
	 * and you want to find other control flow blocks Y which are in stacks starting at X
	 * that have something else interesting. Use this to flow forward and find Y.
	 * 
	 * Operates in the index context.
	 * 
	 * @param origin
	 * @param direction
	 * @return
	 */
	public static Q stack(Q origin, TraversalDirection direction){
		return stack(Query.universe(), origin, direction);
	}
	
	/**
	 * Builds a "stack graph". A stack graph combines CALL (per cf), CONTROL_FLOW,
	 * and DECLARES edges to build a complete call graph at the level of PER_CONTRO_FLOW.
	 * Ordinarily, this would have to be done piecewise.
	 * 
	 * Intended use: You have a control flow block X where something interesting happens,
	 * and you want to find other control flow blocks Y which are in stacks starting at X
	 * that have something else interesting. Use this to flow forward and find Y.
	 * 
	 * Operates in the given context.
	 * 
	 * @param context
	 * @param origin
	 * @param direction
	 * @return
	 */
	public static Q stack(Q context, Q origin, TraversalDirection direction){
		context = context.edges(XCSG.Call, XCSG.ControlFlow_Edge, XCSG.Contains);
		Q result;
		if(direction == TraversalDirection.FORWARD){
			result = context.forward(origin);
		} else if(direction == TraversalDirection.REVERSE){
			result = context.reverse(origin);
		} else {
			result = context.forward(origin).union(context.reverse(origin));
		}
		result = result.differenceEdges(result.edges(Attr.Edge.PER_METHOD));
		return result.nodes(XCSG.Method, XCSG.ControlFlow_Node, Attr.Node.CONTROL_FLOW_PRESENTATION).induce(result);
	}
	
	/**
	 * Given a method or a control flow block which throws an exception, 
	 * returns a graph showing the matching catch blocks.
	 * 
	 * Operates within the given context.
	 * 
	 * @param context
	 * @param input
	 * @return
	 */
	public static Q findCatchForThrows(Q input){
		Q throwContext = Query.universe().edges(Attr.Edge.THROW);
		Q catchContext = Query.universe().edges(Attr.Edge.CATCH);
		Q cfContext = Query.universe().edges(XCSG.ControlFlow_Edge);
		
		input = CommonQueries.declarations(input).nodes(XCSG.ControlFlow_Node);
		Q thrown = throwContext.forwardStep(input).retainEdges();
		Q throwers = thrown.roots();
		Q thrownTypes = thrown.leaves();
		
		Q supertypeEdges = Query.universe().edges(XCSG.Supertype);
		Q thrownTypeHierarchy = supertypeEdges.forward(thrownTypes);
		
		Q caught = catchContext.reverseStep(thrownTypeHierarchy).retainEdges();
		Q caughtTypes = caught.leaves();
		Q catchers = caught.roots().nodes(XCSG.ControlFlow_Node);

		Q reverseStack = stack(input, TraversalDirection.REVERSE);
		
		Q connectedCatchers = cfContext.betweenStep(reverseStack, catchers);
		connectedCatchers = disambiguateMultipleCompatibleCatchBlocks(connectedCatchers);
		Q catcherCFParents = connectedCatchers.roots();
		catchers = connectedCatchers.leaves();
		
		Q filteredStack = reverseStack.differenceEdges(reverseStack.reverseStep(catcherCFParents)).reverse(input);
		connectedCatchers = cfContext.betweenStep(filteredStack, catchers);
		catchers = connectedCatchers.leaves();

		Q catchEdges = Query.universe().edges(Attr.Edge.CATCH);
		Q actualCaughtTypes = catchEdges.betweenStep(catchers, caughtTypes);
		Q finalStack = filteredStack.between(catcherCFParents, throwers).union(connectedCatchers);
				
		Q res = thrownTypeHierarchy.between(thrownTypes, actualCaughtTypes).union(
				finalStack,
				thrown,
				actualCaughtTypes);
		
		return res;
	}
	
	/**
	 * Given a method or control flow block which catches exceptions, 
	 * returns a graph showing the matching throw statements.
	 * 
	 * Operates within the given context.
	 * 
	 * @param context
	 * @param input
	 * @return
	 */
	public static Q findThrowForCatch(Q input){
		Q throwContext = Query.universe().edges(Attr.Edge.THROW);
		Q catchContext = Query.universe().edges(Attr.Edge.CATCH);
		Q cfContext = Query.universe().edges(XCSG.ControlFlow_Edge);
		
		input = CommonQueries.declarations(input).nodes(XCSG.ControlFlow_Node);
		Q caught = catchContext.edges(Attr.Edge.PER_CONTROL_FLOW).forwardStep(input).retainEdges();
		Q caughtTypes = caught.leaves();
		Q catchers = caught.roots().nodes(XCSG.ControlFlow_Node);
		
		Q supertypeEdges = Query.universe().edges(XCSG.Supertype);
		Q caughtTypeHierarchy = supertypeEdges.reverse(caughtTypes);
		
		Q thrown = throwContext.edges(Attr.Edge.PER_CONTROL_FLOW).reverseStep(caughtTypeHierarchy).retainEdges();
		Q throwers = thrown.roots();
		Q thrownTypes = thrown.leaves();

		Q catcherCFConnection = cfContext.reverseStep(catchers);
		Q catcherCFParents = catcherCFConnection.roots();
		Q forwardStack = stack(catcherCFParents, TraversalDirection.FORWARD);

		Q otherCompatibleCatchBlocks = catchContext.reverseStep(caughtTypes).difference(catchers).nodes(XCSG.ControlFlow_Node);
		Q filteredStack = forwardStack.differenceEdges(forwardStack.forwardStep(otherCompatibleCatchBlocks)).forward(catcherCFParents).retainEdges();
		filteredStack = filteredStack.between(catcherCFParents, throwers);
		
		throwers = filteredStack.intersection(throwers);
		Q actualThrownTypes = throwContext.betweenStep(throwers, thrownTypes);

		Q res = caughtTypeHierarchy.between(actualThrownTypes, caughtTypes).union(
				filteredStack,
				catcherCFConnection,
				actualThrownTypes,
				caught);
		
		return res;
	}
	
	/**
	 * This is a horrible, dirty hack. Atlas needs to provide ordering information, which
	 * we can use to disambiguage which compatible catch block will respond first.
	 * Until that time, we're using source correspondence as an extremely dirty way to
	 * figure that out.
	 * 
	 * @param catcherConnections
	 * @return
	 */
	private static Q disambiguateMultipleCompatibleCatchBlocks(Q catcherConnections){
		AtlasSet<Node> nodeSet = new AtlasHashSet<Node>();
		AtlasSet<Edge> edgeSet = new AtlasHashSet<Edge>();
		
		Graph catcherConnectionG = catcherConnections.eval();
		edgeSet.addAll(catcherConnectionG.edges());
		
		for(Node root : catcherConnections.roots().eval().nodes()){
			nodeSet.add(root);
			Graph connectedCatchers = new ForwardStepGraph(catcherConnectionG, new SingletonAtlasSet<Node>(root));
			
			Node responder = null;
			int responderOffset = Integer.MAX_VALUE;
			for(Node catcher : Common.toQ(connectedCatchers.leaves()).eval().nodes()){
				SourceCorrespondence correspondence = (SourceCorrespondence) catcher.attr().get(XCSG.sourceCorrespondence);
				int offset = correspondence.offset;
				if(responder == null || offset < responderOffset){
					responder = catcher;
					responderOffset = offset;
				}
			}
			nodeSet.add(responder);
		}
		
		return Common.toQ(new InducedGraph(nodeSet, edgeSet)).retainEdges();
	}
	
}