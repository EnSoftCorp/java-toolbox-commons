package com.ensoftcorp.open.java.commons.project;

import java.io.File;
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
import com.ensoftcorp.open.java.commons.analysis.CommonQueries;
import com.ensoftcorp.open.java.commons.analyzers.JavaProgramEntryPoints;
import com.ensoftcorp.open.java.commons.bytecode.JarInspector;
import com.ensoftcorp.open.java.commons.log.Log;

public class MainClassAnalysisPropertiesInitializer extends AnalysisPropertiesInitializer {

	public static final String JAVA_PROGRAM_ENTRY_POINT = "JavaProgramEntryPoint";
	public static final String JAVA_MAIN_CLASSES = "JavaMainClasses";
	public static final String INDEXED_JAR_MAIN_CLASSES = "IndexedJarMainClasses";
	public static final String PROJECT_JAR_MAIN_CLASSES = "ProjectJarMainClasses";
	
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
		Set<String> javaMainClasses = new HashSet<String>();
		Set<String> indexJarMainClasses = new HashSet<String>();
		Set<String> projectJarMainClasses = new HashSet<String>();
		Node projectNode = Common.universe().project(project.getName()).eval().nodes().one();
		if(projectNode != null){
			for(Node mainMethod : JavaProgramEntryPoints.findMainMethods().intersection(Common.toQ(projectNode).contained()).eval().nodes()){
				String javaMainClass = CommonQueries.getQualifiedTypeName(Common.toQ(mainMethod).parent().eval().nodes().one());
				javaMainClasses.add(javaMainClass);
			}
			if(!javaMainClasses.isEmpty()){
				String result = javaMainClasses.toString();
				properties.setProperty(JAVA_MAIN_CLASSES, result.substring(1, result.length()-2));
			}
			
			// add main classes in jar manifest libraries for each library method
			for(Node library : Common.universe().nodes(XCSG.Library).eval().nodes()){
				try {
					File libraryFile = null;
					SourceCorrespondence sc = (SourceCorrespondence) library.getAttr(XCSG.sourceCorrespondence);
					if(sc != null){
						libraryFile = WorkspaceUtils.getFile(sc.sourceFile);
						if(libraryFile.exists()){
							JarInspector jarInspector = new JarInspector(libraryFile);
							if(jarInspector.getManifest() != null){
								String mainClass = jarInspector.getManifest().getMainAttributes().getValue("Main-Class");
								if(mainClass != null){
									indexJarMainClasses.add(library.getAttr(XCSG.name).toString() + "/" + mainClass);
								}
							}
						}
					}
				} catch (Exception e){
					Log.warning("Could not inspect indexed library: " + library.getAttr(XCSG.name) + "\n" + library.toString(), e);
				}
			}
			if(!indexJarMainClasses.isEmpty()){
				String result = indexJarMainClasses.toString();
				properties.setProperty(INDEXED_JAR_MAIN_CLASSES, result.substring(1, result.length()-2));
			}
			
			// add main classes in jar manifest libraries for jar contained in the project subfolder
			for(File libraryFile : findJars(new File(project.getLocation().toOSString()))){
				try {
					JarInspector jarInspector = new JarInspector(libraryFile);
					if(jarInspector.getManifest() != null){
						String mainClass = jarInspector.getManifest().getMainAttributes().getValue("Main-Class");
						if(mainClass != null){
							projectJarMainClasses.add(libraryFile.getName() + "/" + mainClass);
						}
					}
				} catch (Exception e){
					Log.warning("Could not inspect project library: " + libraryFile.getName() , e);
				}
			}
			if(!projectJarMainClasses.isEmpty()){
				String result = projectJarMainClasses.toString();
				properties.setProperty(PROJECT_JAR_MAIN_CLASSES, result.substring(1, result.length()-2));
			}
		}
		
		String programEntryPoint = null;
		Set<String> all = new HashSet<String>(javaMainClasses);
		all.retainAll(indexJarMainClasses);
		all.retainAll(projectJarMainClasses);
		if(all.size() == 1){
			programEntryPoint = all.iterator().next();
		} else {
			// indexed project
			Set<String> indexedProject = new HashSet<String>(indexJarMainClasses);
			indexedProject.retainAll(projectJarMainClasses);
			if(indexedProject.size() == 1){
				programEntryPoint = indexedProject.iterator().next();
			} else {
				// just project
				if(projectJarMainClasses.size() == 1){
					programEntryPoint = projectJarMainClasses.iterator().next();
				}
			}
		}
		
		// if the entry point is not found it will be null
		properties.setProperty(JAVA_PROGRAM_ENTRY_POINT, programEntryPoint);
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
