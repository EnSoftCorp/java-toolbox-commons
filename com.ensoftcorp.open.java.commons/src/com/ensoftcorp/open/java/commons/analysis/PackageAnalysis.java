package com.ensoftcorp.open.java.commons.analysis;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CommonQueries;

public class PackageAnalysis {
	
	private PackageAnalysis() {
		// use to prevent object construction
	}

	/**
	 * Returns a Q containing package nodes including the given package 
	 * and its subpackages
	 * @param packageName
	 * @return
	 */
	public static Q getPackageWithSubpackages(String packageName){
		Q allPackages = Query.universe().nodes(XCSG.Package);
		return CommonQueries.nodesStartingWith(allPackages, packageName);
	}
	
	/**
	 * Returns a Q containing the types in the given collection of packages
	 * @param packages
	 * @return
	 */
	public static Q getPackageTypes(Q packages){
		packages = packages.nodes(XCSG.Package);
		return CommonQueries.declarations(Query.universe(), packages).nodes(XCSG.Type);
	}
	
	/**
	 * Returns everything declared under the given package and subpackages
	 * @param basePackageName
	 * @return
	 */
	public static Q getPackageDeclarations(String basePackageName){
		return CommonQueries.declarations(Query.universe(), getPackageWithSubpackages(basePackageName));
	}
	

}
