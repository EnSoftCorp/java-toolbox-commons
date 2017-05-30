package com.ensoftcorp.open.java.commons.analysis;

import static com.ensoftcorp.atlas.core.script.Common.toGraph;
import static com.ensoftcorp.atlas.core.script.Common.toQ;
import static com.ensoftcorp.atlas.core.script.Common.universe;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.db.set.EmptyAtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.G;
import com.ensoftcorp.open.java.commons.log.Log;


public class CallSiteAnalysis {

	private CallSiteAnalysis(){
		// use to prevent object construction
	}
	
	/**
	 * Given a {@link XCSG#CallSite}, return formal {@link XCSG#Identity} arguments for possible target methods.  
	 **/
	public static Q resolveToFormalIdentity(Node callsite) { 
		return resolveToFormalIdentity(toQ(callsite));
	}
	
	/**
	 * Given {@link XCSG#CallSite}s, return formal {@link XCSG#Identity} arguments for possible target methods.  
	 **/
	public static Q resolveToFormalIdentity(Q callsites) { 
		Q actualArgument = universe().edgesTaggedWithAny(XCSG.IdentityPassedTo).predecessors(callsites);
		return universe().edgesTaggedWithAny(XCSG.InterproceduralDataFlow)
			.successors(actualArgument); 
	}
	
	/**
	 * Given a callsite, return the method representing the invoked signature
	 * @param callsite
	 * @return
	 */
	public static Q getSignature(Node callsite) {
		return getSignature(toQ(callsite));
	}
	
	public static Q getSignature(Q callsites) {
		Q signature = universe().edgesTaggedWithAny(XCSG.InvokedFunction, XCSG.InvokedSignature).successors(callsites);
		return signature;
	}
	
	/**
	 * Returns the set of target methods that the given callsites could resolve to
	 * @param callsites
	 * @return
	 */
	public static Q getTargetMethods(Q callsites){
		AtlasSet<Node> targets = new AtlasHashSet<Node>();
		for(Node callsite : callsites.eval().nodes()){
			targets.addAll(getTargetMethods(callsite).eval().nodes());
		}
		return Common.toQ(targets);
	}
	
	/**
	 * Given a StaticDispatchCallSite or a DynamicDispatchCallSite, return the methods which may
	 * have been invoked.
	 * 
	 * @param callsite
	 * @return
	 */
	public static Q getTargetMethods(Node callsite) {
		
		// Note: nodes and edges currently need not be bounded (i.e. any ModelElement is acceptable)
		// The following are used if present: 
		//     nodes <- DataFlow_Node | Variable 
		//     edges <- DataFlow_Edge | InvokedSignature | InvokedFunction | IdentityPassedTo
		
		Graph dataFlowGraph = universe().eval();

		return getTargetMethods(new NullProgressMonitor(), dataFlowGraph, callsite);
	}
	
	private static Q getTargetMethods(IProgressMonitor monitor, Graph dataFlowGraph, Node callsite) {
		if (callsite.taggedWith(XCSG.StaticDispatchCallSite)) {
			return toQ(dataFlowGraph).edgesTaggedWithAny(XCSG.InvokedFunction)
				.successors(toQ(toGraph(callsite)));
			
		} else if (callsite.taggedWith(XCSG.DynamicDispatchCallSite)) {
			
			AtlasSet<Node> targetMethods = new AtlasHashSet<Node>();
			AtlasSet<Node> targetIdentities = getIdentity(dataFlowGraph, callsite).eval().nodes();
			for (Node targetIdentity : targetIdentities) {
				Node targetMethod = CommonQueries.getContainingMethod(targetIdentity);
				if (targetMethod != null){
					targetMethods.add(targetMethod);
				} else {
					Log.warning("Cannot find containing Method for Identity: " + targetIdentity);
				}
				if (monitor.isCanceled()){
					throw new OperationCanceledException();
				}
			}
			return toQ(toGraph(targetMethods));
		}
		throw new IllegalArgumentException();
	}

	/**
	 * Given a call site, return formal identity arguments for possible target methods.  
	 **/
	private static Q getIdentity(Graph dataFlowGraph, Node callsite) {
		return getIdentity(toQ(dataFlowGraph), toQ(callsite));
	}
	
	private static Q getIdentity(Q dataFlowGraph, Q callsites) { 
		Q actualArgument = dataFlowGraph.edgesTaggedWithAny(XCSG.IdentityPassedTo).predecessors(callsites);
		return dataFlowGraph.edgesTaggedWithAny(XCSG.InterproceduralDataFlow)
			.successors(actualArgument); 
	}
	
	
	/**
	 * Given Methods, return possible CallSites.
	 * @param methods
	 * @return
	 */
	public static Q getCallSites(Q methods) {
		AtlasHashSet<Node> callSites = new AtlasHashSet<Node>();
		for (Node method : methods.eval().nodes()) {
			callSites.addAll(getCallSites(method));
		}
		return toQ(callSites);
	}

	public static AtlasSet<Node> getCallSites(Node method) {
		// if ClassMethod or Constructor, go back along InvokedFunction
		// if InstanceMethod, go back along Identity <- IdentityPass -IdentityPassedTo> CallSite

		if (method.taggedWith(XCSG.ClassMethod) || method.taggedWith(XCSG.Constructor)) {
			
			return G.ins(universe().eval(),method,XCSG.InvokedFunction);
			
		} else if (method.taggedWith(XCSG.InstanceMethod)) {
			
			Q formalIdentity = CommonQueries.methodThis(Query.universe(), toQ(method));
			
			Q df = universe().edgesTaggedWithAny(XCSG.DataFlow_Edge);
			Q actualIdentities = formalIdentity.predecessorsOn(df);
			
			Q ipt = universe().edgesTaggedWithAny(XCSG.IdentityPassedTo);
			Q callSites = actualIdentities.successorsOn(ipt);
			return callSites.eval().nodes();
			
		} else {
			return EmptyAtlasSet.<Node>instance(Graph.U);
		}
		
		
	}
}
