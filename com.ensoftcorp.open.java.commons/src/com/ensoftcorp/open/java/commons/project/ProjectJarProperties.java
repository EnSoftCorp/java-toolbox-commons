package com.ensoftcorp.open.java.commons.project;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.index.common.SourceCorrespondence;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.utilities.OSUtils;
import com.ensoftcorp.open.commons.utilities.WorkspaceUtils;
import com.ensoftcorp.open.commons.utilities.project.AnalysisPropertiesInitializer;
import com.ensoftcorp.open.commons.utilities.project.ProjectAnalysisProperties;
import com.ensoftcorp.open.java.commons.bytecode.JarInspector;
import com.ensoftcorp.open.java.commons.log.Log;

public class ProjectJarProperties extends AnalysisPropertiesInitializer {

	private static final String JAR_MANIFEST_MAIN_CLASS = "Main-Class";
	private static final String JARS = "jars";
	private static final String JAR = "jar";
	private static final String JAR_APPLICATIONS = "applications";
	private static final String JAR_LIBRARIES = "libraries";
	
	private static final String JAR_MAIN_CLASS_ATTRIBUTE = "main-class";
	private static final String JAR_PATH_ATTRIBUTE = "path";
	
	public abstract static class Jar {
		protected IProject project;
		protected String portablePath;
		protected File file;
		protected String manifestMainClass = null;
		
		protected Jar(IProject project, String portablePath) throws CoreException {
			this.project = project;
			this.portablePath = portablePath;
			if(portablePath.startsWith("/")){
				// absolute path, windows needs drive letter prefix
				if(OSUtils.isWindows()){
					file = new File(System.getenv("SystemDrive") + portablePath.replace("/", File.separator));
				} else {
					file = new File(portablePath.replace("/", File.separator));
				}
			} else {
				// project relative path
				file = WorkspaceUtils.getFile(project.getFile(portablePath));
			}
		}
		
		protected Jar(IProject project, String portablePath, String manifestMainClass) throws CoreException {
			this(project, portablePath);
			this.manifestMainClass = manifestMainClass;
		}
		
		/**
		 * Returns the file name of the jar
		 * @return
		 */
		public String getName() {
			return file.getName();
		}

		/**
		 * Returns a file of the location of the jar
		 * @return
		 */
		public File getFile() {
			return file;
		}

		/**
		 * Returns the qualified type of the Jar's Manifest declared Main-Class attribute or null if not specified
		 * @return
		 */
		public String getManifestMainClass() {
			return manifestMainClass;
		}
		
		/**
		 * Returns an OS portable string representing the Jar path
		 * Path is relative if Jar is contained in project, else path is absolute
		 * @return
		 */
		public String getPortablePath(){
			return portablePath;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((file == null) ? 0 : file.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Jar other = (Jar) obj;
			if (file == null) {
				if (other.file != null)
					return false;
			} else if (!file.equals(other.file))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			if(manifestMainClass != null){
				return "Jar [Path=" + getPortablePath() + ", ManifestMainClass=" + manifestMainClass + "]";
			} else {
				return "Jar [Path=" + getPortablePath() + "]";
			}
		}
	}
	
	public static class ApplicationJar extends Jar {

		protected ApplicationJar(IProject project, String portablePath) throws CoreException {
			super(project, portablePath);
		}
		
		protected ApplicationJar(IProject project, String portablePath, String manifestMainClass) throws CoreException {
			super(project, portablePath, manifestMainClass);
		}
		
		@Override
		public String toString() {
			return "Application" + super.toString();
		}
		
	}
	
	public static class LibraryJar extends Jar {

		protected LibraryJar(IProject project, String portablePath) throws CoreException {
			super(project, portablePath);
		}
		
		protected LibraryJar(IProject project, String portablePath, String manifestMainClass) throws CoreException {
			super(project, portablePath, manifestMainClass);
		}
		
		@Override
		public String toString() {
			return "Library" + super.toString();
		}
		
	}
	
	public static Set<Jar> getJars(IProject project) throws Exception {
		Set<Jar> jars = new HashSet<Jar>();
		jars.addAll(getApplicationJars(project));
		jars.addAll(getLibraryJars(project));
		return jars;
	}
	
	public static Set<ApplicationJar> getApplicationJars(IProject project) throws Exception {
		Document properties = ProjectAnalysisProperties.getAnalysisProperties(project);
		Set<ApplicationJar> applicationJars = new HashSet<ApplicationJar>();
		NodeList rootChildren = properties.getDocumentElement().getChildNodes();
		for(int i=0; i<rootChildren.getLength(); i++){
			if(!(rootChildren.item(i) instanceof Element)){
				continue;
			}
			Element rootChild = (Element) rootChildren.item(i);
			if(!rootChild.getTagName().equals(JARS)){
				continue;
			}
			Element jarsElement = rootChild;
			NodeList jarApplicationsElements = jarsElement.getChildNodes();
			for(int j=0; j<jarApplicationsElements.getLength(); j++){
				if(!(jarApplicationsElements.item(j) instanceof Element)){
					continue;
				}
				Element jarApplicationsElement = (Element) jarApplicationsElements.item(j);
				if(!jarApplicationsElement.getTagName().equals(JAR_APPLICATIONS)){
					continue;
				}
				NodeList jarApplicationElements = jarApplicationsElement.getChildNodes();
				for(int k=0; k<jarApplicationElements.getLength(); k++){
					if(!(jarApplicationElements.item(k) instanceof Element)){
						continue;
					}
					Element jarApplicationElement = (Element) jarApplicationElements.item(k);
					if(!jarApplicationElement.getTagName().equals(JAR)){
						continue;
					}
					if(jarApplicationElement.hasAttribute(JAR_PATH_ATTRIBUTE)){
						String path = jarApplicationElement.getAttribute(JAR_PATH_ATTRIBUTE);
						if(!jarApplicationElement.hasAttribute(JAR_MAIN_CLASS_ATTRIBUTE)){
							applicationJars.add(new ApplicationJar(project, path));
						} else {
							applicationJars.add(new ApplicationJar(project, path, jarApplicationElement.getAttribute(JAR_MAIN_CLASS_ATTRIBUTE)));
						}
					}
				}
			}
		}
		return applicationJars;
	}
	
	public static Set<LibraryJar> getLibraryJars(IProject project) throws Exception {
		Document properties = ProjectAnalysisProperties.getAnalysisProperties(project);
		Set<LibraryJar> libraryJars = new HashSet<LibraryJar>();
		NodeList rootChildren = properties.getDocumentElement().getChildNodes();
		for(int i=0; i<rootChildren.getLength(); i++){
			if(!(rootChildren.item(i) instanceof Element)){
				continue;
			}
			Element rootChild = (Element) rootChildren.item(i);
			if(!rootChild.getTagName().equals(JARS)){
				continue;
			}
			Element jarsElement = rootChild;
			NodeList jarLibrariesElements = jarsElement.getChildNodes();
			for(int j=0; j<jarLibrariesElements.getLength(); j++){
				if(!(jarLibrariesElements.item(j) instanceof Element)){
					continue;
				}
				Element jarLibrariesElement = (Element) jarLibrariesElements.item(j);
				if(!jarLibrariesElement.getTagName().equals(JAR_LIBRARIES)){
					continue;
				}
				NodeList jarLibraryElements = jarLibrariesElement.getChildNodes();
				for(int k=0; k<jarLibraryElements.getLength(); k++){
					if(!(jarLibraryElements.item(k) instanceof Element)){
						continue;
					}
					Element jarLibraryElement = (Element) jarLibraryElements.item(k);
					if(!jarLibraryElement.getTagName().equals(JAR)){
						continue;
					}
					if(jarLibraryElement.hasAttribute(JAR_PATH_ATTRIBUTE)){
						String path = jarLibraryElement.getAttribute(JAR_PATH_ATTRIBUTE);
						if(!jarLibraryElement.hasAttribute(JAR_MAIN_CLASS_ATTRIBUTE)){
							libraryJars.add(new LibraryJar(project, path));
						} else {
							libraryJars.add(new LibraryJar(project, path, jarLibraryElement.getAttribute(JAR_MAIN_CLASS_ATTRIBUTE)));
						}
					}
				}
			}
		}
		return libraryJars;
	}
	
	@Override
	public boolean supportsProject(IProject project) {
		try {
			return project.hasNature(JavaCore.NATURE_ID);
		} catch (Exception e){
			return false;
		}
	}

	@Override
	public void initialize(IProject project, Document properties) {
		Set<IFile> libraries = new HashSet<IFile>();
		Map<IFile,String> jarMainClasses = new HashMap<IFile,String>();
		
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
							libraries.add(sc.sourceFile);
							JarInspector jarInspector = new JarInspector(libraryFile);
							if(jarInspector.getManifest() != null){
								String mainClass = jarInspector.getManifest().getMainAttributes().getValue(JAR_MANIFEST_MAIN_CLASS);
								if(mainClass != null){
									jarMainClasses.put(sc.sourceFile, mainClass);
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
					libraries.add(WorkspaceUtils.getFile(libraryFile));
					JarInspector jarInspector = new JarInspector(libraryFile);
					if(jarInspector.getManifest() != null){
						String mainClass = jarInspector.getManifest().getMainAttributes().getValue(JAR_MANIFEST_MAIN_CLASS);
						if(mainClass != null){
							jarMainClasses.put(WorkspaceUtils.getFile(libraryFile), mainClass);
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
		Element jarsElement = properties.createElement(JARS);
		properties.getDocumentElement().appendChild(jarsElement);
		
		// by default consider none of the libraries as application libraries
		Element jarApplicationsElement = properties.createElement(JAR_APPLICATIONS);
		jarsElement.appendChild(jarApplicationsElement);
		
		// add all libraries as support libraries
		ArrayList<IFile> librariesSorted = new ArrayList<IFile>(libraries);
		Collections.sort(librariesSorted, new Comparator<IFile>(){
			@Override
			public int compare(IFile a, IFile b) {
				return a.getName().compareTo(b.getName());
			}
		});
		Element jarLibrariesElement = properties.createElement(JAR_LIBRARIES);
		jarsElement.appendChild(jarLibrariesElement);
		for(IFile library : librariesSorted){
			Element jarLibraryElement = properties.createElement(JAR);
			IFile projectResource = project.getFile(library.getProjectRelativePath());
			if(projectResource != null && projectResource.exists()){
				jarLibraryElement.setAttribute(JAR_PATH_ATTRIBUTE, library.getProjectRelativePath().toPortableString());
			} else {
				jarLibraryElement.setAttribute(JAR_PATH_ATTRIBUTE, library.getFullPath().toPortableString());
			}
			if(jarMainClasses.containsKey(library)){
				jarLibraryElement.setAttribute(JAR_MAIN_CLASS_ATTRIBUTE, jarMainClasses.get(library));
			}
			jarLibrariesElement.appendChild(jarLibraryElement);
		}
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
