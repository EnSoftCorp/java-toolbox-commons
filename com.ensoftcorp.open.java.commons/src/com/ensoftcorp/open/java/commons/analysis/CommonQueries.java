package com.ensoftcorp.open.java.commons.analysis;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;

/**
 * Common queries which are useful for writing larger Java analysis programs,
 * and for using on the shell.
 * 
 * @author Ben Holland, Tom Deering, Jon Mathews
 */
public final class CommonQueries {	
	
	// hide constructor
	private CommonQueries() {}
	
	// begin wrapper queries
	
	/**
	 * Returns all references to class literals (Type.class) for the given
	 * types. 
	 * 
	 * Equivalent to classLiterals(index(), types).
	 * 
	 * @param types
	 * @return the query expression
	 */
	public static Q classLiterals(Q types){
		return com.ensoftcorp.atlas.java.core.script.CommonQueries.classLiterals(types);
	}
	
	/**
	 * Returns all references to class literals (Type.class) for the given
	 * types.
	 * 
	 * @param types
	 * @return the query expression
	 */
	public static Q classLiterals(Q context, Q types){
		return com.ensoftcorp.atlas.java.core.script.CommonQueries.classLiterals(context, types);
	}

	/**
	 * Given a type to search and a method signature, determine possible method definitions which could be called.
	 * @param typeToSearch
	 * @param methodSignature
	 * @return
	 */
	public static Q conservativeDynamicDispatch(Q typeToSearch, Q methodSignature){
		return com.ensoftcorp.atlas.java.core.script.CommonQueries.conservativeDynamicDispatch(typeToSearch, methodSignature);
	}
	
	/**
	 * Produces a declarations (contains) graph. 
	 * 
	 * Equivalent to declarations(index(), origin).
	 * 
	 * @param origin
	 * @return the query expression
	 */
	public static Q declarations(Q origin){
		return com.ensoftcorp.atlas.java.core.script.CommonQueries.declarations(origin);
	}
	
	/**
	 * Produces a declarations (contains) graph. Uses only the given context for
	 * the traversal.
	 * 
	 * @param context
	 * @param origin
	 * @return the query expression
	 */
	public static Q declarations(Q context, Q origin){
		return com.ensoftcorp.atlas.java.core.script.CommonQueries.declarations(context, origin);
	}
	
	/**
	 * Resolves invocation for a runtime type and method signature.
	 * 
	 * @param runtimeTypes
	 * @param methodSignature
	 * @return
	 */
	public static Q dynamicDispatch(Q runtimeTypes, Q methodSignature){
		return com.ensoftcorp.atlas.java.core.script.CommonQueries.dynamicDispatch(runtimeTypes, methodSignature);
	}
	
	/**
	 * Returns direct edges of the given kinds which lay immediately between the
	 * first group and second group of nodes.
	 * 
	 * @param first
	 * @param second
	 * @param edgeTags
	 * @return the query expression
	 * @deprecated use com.ensoftcorp.open.commons.analysis.CommonQueries.interactions
	 */
	@Deprecated 
	public static Q interactions(Q first, Q second, String... edgeTags){
		return com.ensoftcorp.open.commons.analysis.CommonQueries.interactions(first, second, edgeTags);
	}
	
	/**
	 * Returns direct edges of the given kinds which lay immediately between the
	 * first group and second group of nodes.
	 * 
	 * @param context
	 * @param first
	 * @param second
	 * @param edgeTags
	 * @return the query expression
	 * @deprecated use com.ensoftcorp.open.commons.analysis.CommonQueries.interactions
	 */
	@Deprecated 
	public static Q interactions(Q context, Q first, Q second, String... edgeTags){
		return com.ensoftcorp.open.commons.analysis.CommonQueries.interactions(context, first, second, edgeTags);
	}
	
	/**
	 * Returns those nodes which are declared by a library.
	 * 
	 * @return the query expression
	 */
	public static Q libraryDeclarations(){
		return com.ensoftcorp.atlas.java.core.script.CommonQueries.libraryDeclarations(); 
	}
	
	/**
	 * Returns those nodes which are declared by a library. Results are only
	 * returned if they are within the given context.
	 * 
	 * Equivalent to libraryDeclarations(index())
	 * 
	 * @param context
	 * @return the query expression
	 */
	public static Q libraryDeclarations(Q context){
		return com.ensoftcorp.atlas.java.core.script.CommonQueries.libraryDeclarations(context); 
	}
	
	/**
	 * Returns those nodes which are declared by a library with the given name.
	 * 
	 * @param name
	 * @return the query expression
	 */
	public static Q libraryDeclarations(String name){
		return com.ensoftcorp.atlas.java.core.script.CommonQueries.libraryDeclarations(name); 
	}
	
	/**
	 * Returns those nodes which are declared by a library with the given name.
	 * Results are only returned if they are within the given context.
	 * 
	 * Equivalent to libraryDeclarations(index(), name)
	 * 
	 * @param context
	 * @param name
	 * @return the query expression
	 */
	public static Q libraryDeclarations(Q context, String name){
		return com.ensoftcorp.atlas.java.core.script.CommonQueries.libraryDeclarations(context, name); 
	}
	
	/**
	 * Returns the parameters of the given methods. 
	 * 
	 * Equivalent to methodParameter(index(), methods)
	 * 
	 * @param methods
	 * @return the query expression
	 */
	public static Q methodParameter(Q methods){
		return com.ensoftcorp.atlas.core.script.CommonQueries.methodParameter(methods); 
	}
	
	/**
	 * Returns the parameters of the given methods at the given indices. 
	 * 
	 * Equivalent to methodParameter(index(), methods, index)
	 * 
	 * @param methods
	 * @param index
	 * @return the query expression
	 */
	public static Q methodParameter(Q methods, Integer... index){
		return com.ensoftcorp.atlas.core.script.CommonQueries.methodParameter(methods, index); 
	}
	
	/**
	 * Returns the parameters of the given methods. Results are only returned if
	 * they are within the given context.
	 * 
	 * @param context
	 * @param methods
	 * @return the query expression
	 */
	public static Q methodParameter(Q context, Q methods){
		return com.ensoftcorp.atlas.core.script.CommonQueries.methodParameter(context, methods); 
	}
	
	/**
	 * Returns the parameters of the given methods at the given indices. Results
	 * are only returned if they are within the given context.
	 * 
	 * @param context
	 * @param methods
	 * @param index
	 * @return the query expression
	 */
	public static Q methodParameter(Q context, Q methods, Integer... index){
		return com.ensoftcorp.atlas.core.script.CommonQueries.methodParameter(context, methods, index); 
	}
	
	/**
	 * Returns the return nodes for the given methods.
	 * 
	 * Equivalent to methodReturn(index(), methods).
	 * 
	 * @param methods
	 * @return the query expression
	 */
	public static Q methodReturn(Q methods){
		return com.ensoftcorp.atlas.core.script.CommonQueries.methodReturn(methods); 
	}
	
	/**
	 * Returns the return nodes for the given methods.
	 * @param context
	 * @param methods
	 * @return the query expression
	 */
	public static Q methodReturn(Q context, Q methods){
		return com.ensoftcorp.atlas.core.script.CommonQueries.methodReturn(context, methods); 
	}
	
	/**
	 * Returns the methods declared by the given types. 
	 * 
	 * Equivalent to methodsOf(index(), types).
	 * 
	 * @param params
	 * @return the query expression
	 */
	public static Q methodsOf(Q types){
		return com.ensoftcorp.atlas.core.script.CommonQueries.methodsOf(types); 
	}
	
	/**
	 * Returns the methods declared by the given types.
	 * 
	 * @param context
	 * @param types
	 * @return the query expression
	 */
	public static Q methodsOf(Q context, Q types){
		return com.ensoftcorp.atlas.core.script.CommonQueries.methodsOf(context, types); 
	}
	
	/**
	 * Returns the 'this' identity nodes for the given methods.
	 * 
	 * Equivalent to methodThis(index(), methods).
	 * 
	 * @param methods
	 * @return the query expression
	 */
	public static Q methodThis(Q methods){
		return com.ensoftcorp.atlas.core.script.CommonQueries.methodThis(methods); 
	}
	
	/**
	 * Returns the 'this' identity nodes for the given methods.
	 * 
	 * @param context
	 * @param methods
	 * @return the query expression
	 */
	public static Q methodThis(Q context, Q methods){
		return com.ensoftcorp.atlas.core.script.CommonQueries.methodThis(context, methods);
	}
	
	/**
	 * Returns the nodes whose names contain the given string.
	 * 
	 * Equivalent to nodesContaining(index(), substring).
	 * 
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesContaining(String substring){
		return com.ensoftcorp.atlas.java.core.script.CommonQueries.nodesContaining(substring);
	}
	
	/**
	 * Returns the nodes whose names contain the given string within the given
	 * context.
	 * 
	 * @param context
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesContaining(Q context, String substring){
		return com.ensoftcorp.atlas.java.core.script.CommonQueries.nodesContaining(context, substring);
	}
	
	/**
	 * Returns the nodes whose names end with the given string.
	 * 
	 * Equivalent to nodesEndingWith(index(), suffix).
	 * 
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesEndingWith(String suffix){
		return com.ensoftcorp.atlas.java.core.script.CommonQueries.nodesEndingWith(suffix);
	}
	
	/**
	 * Returns the nodes whose names end with the given string within the given
	 * context.
	 * 
	 * @param context
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesEndingWith(Q context, String suffix){
		return com.ensoftcorp.atlas.java.core.script.CommonQueries.nodesEndingWith(context, suffix);
	}
	
	/**
	 * Returns the nodes whose names match the given regular expression.
	 * 
	 * Equivalent to nodesMatchingRegex(index(), regex).
	 * 
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesMatchingRegex(String regex){
		return com.ensoftcorp.atlas.java.core.script.CommonQueries.nodesMatchingRegex(regex);
	}
	
	/**
	 * Returns the nodes whose names match the given regular expression within
	 * the given context.
	 * 
	 * @param context
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesMatchingRegex(Q context, String regex){
		return com.ensoftcorp.atlas.java.core.script.CommonQueries.nodesMatchingRegex(context, regex);
	}
	
	/**
	 * Returns the nodes whose names start with the given string.
	 * 
	 * Equivalent to nodesStartingWith(index(), prefix).
	 * 
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesStartingWith(String prefix){
		return com.ensoftcorp.atlas.java.core.script.CommonQueries.nodesStartingWith(prefix);
	}
	
	/**
	 * Returns the nodes whose names start with the given string within the
	 * given context.
	 * 
	 * @param context
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesStartingWith(Q context, String prefix){
		return com.ensoftcorp.atlas.java.core.script.CommonQueries.nodesStartingWith(context, prefix);
	}
	
	/**
	 * Returns the number of edges contained.
	 * @param toCount
	 * @return
	 */
	public static long edgeSize(Q toCount){
		return com.ensoftcorp.atlas.java.core.script.CommonQueries.edgeSize(toCount);
	}
	
	/**
	 * Returns the number of nodes contained.
	 * @param toCount
	 * @return
	 */
	public static long nodeSize(Q toCount){
		return com.ensoftcorp.atlas.java.core.script.CommonQueries.nodeSize(toCount);
	}
	
	/**
	 * Returns whether the given Q is empty.
	 * 
	 * @param test
	 * @return
	 */
	public static boolean isEmpty(Q test){
		return com.ensoftcorp.atlas.java.core.script.CommonQueries.isEmpty(test);
	}
	
	// begin toolbox commons queries
	
	/**
	 * Selects the Atlas graph element given a serialized graph
	 * element address
	 * 
	 * Returns null if the address does not correspond to a graph element
	 * 
	 * @param address
	 * @return
	 */
	public static GraphElement getGraphElementByAddress(String address){
		return com.ensoftcorp.open.commons.analysis.CommonQueries.getGraphElementByAddress(address);
	}
	
	/**
	 * Selects the Atlas node graph element given a serialized graph
	 * element address
	 * 
	 * Returns null if the address does not correspond to a node
	 * 
	 * @param address
	 * @return
	 */
	public static Node getNodeByAddress(String address){
		return com.ensoftcorp.open.commons.analysis.CommonQueries.getNodeByAddress(address);
	}
	
	/**
	 * Selects the Atlas edge graph element given a serialized graph
	 * element address
	 * 
	 * Returns null if the address does not correspond to a edge
	 * 
	 * @param address
	 * @return
	 */
	public static Edge getEdgeByAddress(String address){
		return com.ensoftcorp.open.commons.analysis.CommonQueries.getEdgeByAddress(address);
	}
	
	/**
	 * All nodes declared under the given functions, but NOT declared under
	 * additional functions or types. Retrieves declarations of only this function.
	 * Results are only returned if they are within the given context.
	 * 
	 * @param functions
	 * @return
	 */
	public static Q localDeclarations(Q functions) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.localDeclarations(functions);
	}

	/**
	 * All nodes declared under the given functions, but NOT declared under
	 * additional functions or types. Retrieves declarations of only this function.
	 * Results are only returned if they are within the given context.
	 * 
	 * @param context
	 * @param functions
	 * @return
	 */
	public static Q localDeclarations(Q context, Q functions) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.localDeclarations(context, functions);
	}

	/**
	 * Returns the direct callers of the given functions.
	 * 
	 * Operates in the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q callers(Q origin) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.callers(origin);
	}

	/**
	 * Returns the direct callers of the given functions.
	 * 
	 * Operates in the given context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q callers(Q context, Q origin) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.callers(context, origin);
	}

	/**
	 * Returns the subset of the given functions which are called.
	 * 
	 * Operates in the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q called(Q origin) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.called(origin);
	}

	/**
	 * Returns the subset of the given functions which are called. Results are
	 * only returned if they are within the given context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q called(Q context, Q origin) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.called(context, origin);
	}

	/**
	 * Returns the given functions which were called by the given callers.
	 * 
	 * Operates in the index context.
	 * 
	 * @param callers
	 * @param called
	 * @return
	 */
	public static Q calledBy(Q callers, Q called) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.calledBy(callers, called);
	}

	/**
	 * Returns the given functions which were called by the given callers. Results
	 * are only returned if they are within the given context.
	 * 
	 * @param context
	 * @param callers
	 * @param called
	 * @return
	 */
	public static Q calledBy(Q context, Q callers, Q called) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.calledBy(context, callers, called);
	}
	
	/**
	 * 
	 * @param functions
	 * @return the data flow graph under the function
	 */
	public static Q dfg(Q functions) {
		return localDeclarations(functions).nodes(XCSG.DataFlow_Node).induce(Common.edges(XCSG.DataFlow_Edge));
	}
	
	/**
	 * 
	 * @param function
	 * @return the data flow graph under the function
	 */
	public static Q dfg(Node function) {
		return dfg(Common.toQ(function));
	}
	
	/**
	 * 
	 * @param functions
	 * @return the control flow graph under the function
	 */
	public static Q cfg(Q functions) {
		return com.ensoftcorp.atlas.core.script.CommonQueries.cfg(functions);
	}
	
	/**
	 * 
	 * @param function
	 * @return the control flow graph under the function
	 */
	public static Q cfg(Node function) {
		return cfg(Common.toQ(function));
	}
	
	/**
	 * 
	 * @param functions
	 * @return the control flow graph (including exceptional control flow) under the function
	 */
	public static Q excfg(Q functions) {
		return com.ensoftcorp.atlas.core.script.CommonQueries.excfg(functions);
	}
	
	/**
	 * 
	 * @param function
	 * @return the control flow graph (including exceptional control flow) under the function
	 */
	public static Q excfg(Node function) {
		return excfg(Common.toQ(function));
	}

	/**
	 * Returns the first declaring node of the given Q which is tagged with one
	 * of the given types.
	 * 
	 * Operates in the index context.
	 * 
	 * @param declared
	 * @param declaratorTypes
	 * @return
	 */
	public static Q firstDeclarator(Q declared, String... declaratorTypes) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.firstDeclarator(declared, declaratorTypes);
	}

	/**
	 * Returns the first declaring node of the given Q which is tagged with one
	 * of the given types. Results are only returned if they are within the
	 * given context.
	 * 
	 * @param context
	 * @param declared
	 * @param declaratorTypes
	 * @return
	 */
	public static Q firstDeclarator(Q context, Q declared, String... declaratorTypes) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.firstDeclarator(context, declared, declaratorTypes);
	}

	/**
	 * Given two query expressions, intersects the given node and edge kinds to
	 * produce a new expression.
	 * 
	 * @param first
	 * @param second
	 * @param nodeTags
	 * @param edgeTags
	 * @return
	 */
	public static Q advancedIntersection(Q first, Q second, String[] nodeTags, String[] edgeTags) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.advancedIntersection(first, second, nodeTags, edgeTags);
	}

	/**
	 * Returns the nodes which directly read from nodes in origin.
	 * 
	 * Operates in the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q readersOf(Q origin) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.readersOf(origin);
	}

	/**
	 * Returns the nodes which directly read from nodes in origin.
	 * 
	 * Operates in the given context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q readersOf(Q context, Q origin) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.readersOf(context, origin);
	}

	/**
	 * Returns the nodes which directly write to nodes in origin.
	 * 
	 * Operates in the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q writersOf(Q origin) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.writersOf(origin);
	}

	/**
	 * Returns the nodes which directly write to nodes in origin.
	 * 
	 * Operates in the given context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q writersOf(Q context, Q origin) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.writersOf(context, origin);
	}

	/**
	 * Returns the nodes from which nodes in the origin read.
	 * 
	 * Operates in the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q readBy(Q origin) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.readBy(origin);
	}

	/**
	 * Returns the nodes from which nodes in the origin read.
	 * 
	 * Operates in the given context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q readBy(Q context, Q origin) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.readBy(context, origin);
	}

	/**
	 * Returns the nodes to which nodes in origin write.
	 * 
	 * Operates in the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q writtenBy(Q origin) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.writtenBy(origin);
	}

	/**
	 * Returns the nodes to which nodes in origin write.
	 * 
	 * Operates in the given context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q writtenBy(Q context, Q origin) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.writtenBy(context, origin);
	}
	
	/**
	 * Returns the least common ancestor of both child1 and child2 within the given graph
	 * 
	 * @param child1
	 * @param child2
	 * @param graph
	 * @return
	 */
	public static Node leastCommonAncestor(Node child1, Node child2, Graph graph){
		return com.ensoftcorp.open.commons.analysis.CommonQueries.leastCommonAncestor(child1, child2, graph);
	}
	
	/**
	 * Returns the least common ancestor of both child1 and child2 within the given graph
	 * 
	 * @param child1
	 * @param child2
	 * @param graph
	 * @return
	 */
	public static Node leastCommonAncestor(Node child1, Node child2, Q graph){
		return com.ensoftcorp.open.commons.analysis.CommonQueries.leastCommonAncestor(child1, child2, graph);
	}

	/**
	 * Returns the containing method of a given Q or empty if one is not found
	 * @param nodes
	 * @return
	 */
	public static Q getContainingMethods(Q nodes) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.getContainingFunctions(nodes);
	}
	
	/**
	 * Returns the nearest parent that is a control flow node
	 * @param node
	 * @return
	 */
	public static Node getContainingControlFlowNode(Node node) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.getContainingControlFlowNode(node);
	}

	/**
	 * Returns the containing method of a given graph element or null if one is not found
	 * @param node
	 * @return
	 */
	public static Node getContainingMethod(Node node) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.getContainingFunction(node);
	}
	
	/**
	 * Find the next immediate containing node with the given tag.
	 * 
	 * @param node 
	 * @param containingTag
	 * @return the next immediate containing node, or null if none exists; never returns the given node
	 */
	public static Node getContainingNode(Node node, String containingTag) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.getContainingNode(node, containingTag);
	}

	/**
	 * Returns the control flow graph between conditional nodes and the given
	 * origin.
	 * 
	 * Operates within the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q conditionsAbove(Q origin) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.conditionsAbove(origin);
	}

	/**
	 * Returns the control flow graph between conditional nodes and the given
	 * origin.
	 * 
	 * Operates within the given context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q conditionsAbove(Q context, Q origin) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.conditionsAbove(context, origin);
	}

	/**
	 * Given a Q containing methods or data flow nodes, returns a Q of things
	 * which write to or call things in the Q.
	 * 
	 * Operates within the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q mutators(Q origin) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.mutators(origin);
	}

	/**
	 * Returns those nodes in the context which have self edges.
	 * 
	 * @param context
	 * @return
	 */
	public static Q nodesWithSelfEdges(Q context) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.nodesWithSelfEdges(context);
	}

	/**
	 * Given a Q containing methods or data flow nodes, returns a Q of things
	 * which write to or call things in the Q.
	 * 
	 * Operates within the index context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q mutators(Q context, Q origin) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.mutators(context, origin);
	}

	/**
	 * Returns those elements in the origin which were called by or written by
	 * elements in the mutators set.
	 * 
	 * Operates within the index context.
	 * 
	 * @param mutators
	 * @param origin
	 * @return
	 */
	public static Q mutatedBy(Q mutators, Q origin) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.mutatedBy(mutators, origin);
	}

	/**
	 * Returns those elements in the origin which were called by or written by
	 * elements in the mutators set.
	 * 
	 * Operates within the given context.
	 * 
	 * @param context
	 * @param mutators
	 * @param origin
	 * @return
	 */
	public static Q mutatedBy(Q context, Q mutators, Q origin) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.mutatedBy(context, mutators, origin);
	}
	
	/**
	 * Helper method to get the stringified qualified name of the class
	 * @param method
	 * @return
	 */
	public static String getQualifiedTypeName(Node type) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.getQualifiedTypeName(type);
	}
	
	/**
	 * Helper method to get the stringified qualified name of the method
	 * @param method
	 * @return
	 */
	public static String getQualifiedMethodName(Node method) {
		if(method == null){
			throw new IllegalArgumentException("Method is null!");
		}
		if(!method.taggedWith(XCSG.Method)){
			throw new IllegalArgumentException("Method parameter is not a method!");
		}
		return com.ensoftcorp.open.commons.analysis.CommonQueries.getQualifiedFunctionName(method);
	}
	
	/**
	 * Helper method to get the stringified qualified name of the method
	 * @param method
	 * @return
	 */
	public static String getQualifiedName(Node node) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.getQualifiedName(node);
	}
	
	/**
	 * Helper method to get the stringified qualified name of the class
	 * Stop after tags specify parent containers to stop qualifying at (example packages or jars)
	 * @param method
	 * @return
	 */
	public static String getQualifiedName(Node node, String...stopAfterTags) {
		return com.ensoftcorp.open.commons.analysis.CommonQueries.getQualifiedName(node, stopAfterTags);
	}
	
}