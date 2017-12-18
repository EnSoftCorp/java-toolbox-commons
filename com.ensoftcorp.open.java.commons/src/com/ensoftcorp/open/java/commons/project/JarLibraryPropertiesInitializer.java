package com.ensoftcorp.open.java.commons.project;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.JavaCore;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.index.common.SourceCorrespondence;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.utilities.WorkspaceUtils;
import com.ensoftcorp.open.commons.utilities.project.AnalysisPropertiesInitializer;
import com.ensoftcorp.open.java.commons.bytecode.JarInspector;
import com.ensoftcorp.open.java.commons.log.Log;

public class JarLibraryPropertiesInitializer extends AnalysisPropertiesInitializer {
	
	public static final String JAR_APPLICATION = "JarApplication";
	public static final String JAR_LIBRARY = "JarLibrary";
	public static final String JAR_MAIN_CLASS = "JarMainClass";
	
	@Override
	public boolean supportsProject(IProject project) {
		try {
			return project.hasNature(JavaCore.NATURE_ID);
		} catch (Exception e){
			return false;
		}
	}

	@Override
	public void initialize(IProject project, Properties properties) {
		Set<String> libraries = new HashSet<String>();
		Set<String> jarMainClasses = new HashSet<String>();
		
		Node projectNode = Common.universe().project(project.getName()).eval().nodes().one();
		if(projectNode != null){
			// add main classes in jar manifest libraries for each library method
			for(Node library : Common.universe().nodes(XCSG.Library).eval().nodes()){

				// search for libraries based on source correspondence
				try {
					File libraryFile = null;
					SourceCorrespondence sc = (SourceCorrespondence) library.getAttr(XCSG.sourceCorrespondence);
					if(sc != null){
						libraryFile = WorkspaceUtils.getFile(sc.sourceFile);
						if(libraryFile.exists()){
							libraries.add(libraryFile.getName());
							JarInspector jarInspector = new JarInspector(libraryFile);
							if(jarInspector.getManifest() != null){
								String mainClass = jarInspector.getManifest().getMainAttributes().getValue("Main-Class");
								if(mainClass != null){
									jarMainClasses.add(library.getAttr(XCSG.name).toString() + "/" + mainClass);
								}
							}
						}
					}
				} catch (Exception e){
					Log.warning("Could not inspect indexed library: " + library.getAttr(XCSG.name) + "\n" + library.toString(), e);
				}
			}
			
			// search for libraries contained in the project subfolder
			for(File libraryFile : findJars(new File(project.getLocation().toOSString()))){
				try {
					libraries.add(libraryFile.getName());
					JarInspector jarInspector = new JarInspector(libraryFile);
					if(jarInspector.getManifest() != null){
						String mainClass = jarInspector.getManifest().getMainAttributes().getValue("Main-Class");
						if(mainClass != null){
							jarMainClasses.add(libraryFile.getName() + "/" + mainClass);
						}
					}
				} catch (Exception e){
					Log.warning("Could not inspect project library: " + libraryFile.getName() , e);
				}
			}
		}
		
		// at this time we are just initializing the application property key
		// user or another analysis can decide which of the libraries may be
		// part of the application
		properties.setProperty(JAR_APPLICATION, "null");
		
		// list all the libraries comma separated
		ArrayList<String> librariesSorted = new ArrayList<String>(libraries);
		Collections.sort(librariesSorted);
		String librariesResult = librariesSorted.toString().replace(" ", "");
		properties.setProperty(JAR_LIBRARY, librariesResult.substring(1,librariesResult.length()-1));
		
		// list all the jar main classes as identified in jar manifests
		ArrayList<String> jarMainClassesSorted = new ArrayList<String>(jarMainClasses);
		Collections.sort(jarMainClassesSorted);
		String jarMainClassesResult = jarMainClassesSorted.toString().replace(" ", "");
		properties.setProperty(JAR_MAIN_CLASS, jarMainClassesResult.substring(1,jarMainClassesResult.length()-1));
	}

	// helper method for recursively finding jar files in a given directory
	public static LinkedList<File> findJars(File directory){
		LinkedList<File> jimple = new LinkedList<File>();
		if(directory.exists()){
			if (directory.isDirectory()) {
				for (File f : directory.listFiles()) {
					jimple.addAll(findJars(f));
				}
			}
			File file = directory;
			if(file.getName().endsWith(".jar")){
				jimple.add(file);
			}
		}
		return jimple;
	}
}
