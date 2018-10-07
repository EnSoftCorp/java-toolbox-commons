package com.ensoftcorp.open.java.commons.analysis;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;

/**
 * Common set definitions which are useful for program analysis
 * 
 * @author Tom Deering, Ben Holland
 */
public final class SetDefinitions {

	// hide constructor
	private SetDefinitions() {}
	
	public static final String[] JDK_LIBRARIES = new String[] { "resources.jar", "rt.jar", "jsse.jar", "jce.jar", "charset.jar",
			"jfr.jar", "cldrdata.jar", "dnsns.jar", "jaccess.jar", "jfxrt.jar", "localedata.jar", "nashorn.jar",
			"sunec.jar", "sunjce_provider.jar", "sunpkcs11.jar", "zipfs.jar", "MRJToolkit.jar", "local_policy.jar",
			"US_export_policy.jar", "rhino.jar", "pulse-java.jar", "management-agent.jar", "charsets.jar" };

	/**
	 * A collection of specifically just the JDK libraries
	 * @return
	 */
	public static Q JDKLibraries(){
		AtlasSet<Node> libraries = new AtlasHashSet<Node>();
		for(String jdkLibrary : JDK_LIBRARIES){
			libraries.addAll(Query.universe().nodes(XCSG.Library).selectNode(XCSG.name, jdkLibrary).eval().nodes());
		}
		return Common.toQ(libraries);
	}
	
	/**
	 * Types which represent arrays of other types
	 * 
	 * NOTE: These nodes are NOT declared by anything. They are outside of any
	 * project.
	 */
	public static Q arrayTypes() {
		return com.ensoftcorp.open.commons.analysis.SetDefinitions.arrayTypes();
	}

	/**
	 * Types which represent language primitive types
	 * 
	 * NOTE: These nodes are NOT declared by anything. They are outside of any
	 * project.
	 */
	public static Q primitiveTypes() {
		return com.ensoftcorp.open.commons.analysis.SetDefinitions.primitiveTypes();
	}

	/**
	 * Everything declared under any of the known API projects, if they are in
	 * the index.
	 */
	public static Q libraries() {
		return com.ensoftcorp.open.commons.analysis.SetDefinitions.libraries();
	}
	
	/**
	 * Everything in the universe which is part of the app (not part of the
	 * libraries, or any "floating" nodes).
	 */
	public static Q app() {
		return com.ensoftcorp.open.commons.analysis.SetDefinitions.app();
	}

}
