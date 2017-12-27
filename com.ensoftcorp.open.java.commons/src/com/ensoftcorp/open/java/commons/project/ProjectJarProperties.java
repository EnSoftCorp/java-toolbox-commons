package com.ensoftcorp.open.java.commons.project;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.index.common.SourceCorrespondence;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.utilities.OSUtils;
import com.ensoftcorp.open.commons.utilities.WorkspaceUtils;
import com.ensoftcorp.open.commons.utilities.project.AnalysisPropertiesInitializer;
import com.ensoftcorp.open.commons.utilities.project.ProjectAnalysisProperties;
import com.ensoftcorp.open.java.commons.analysis.SetDefinitions;
import com.ensoftcorp.open.java.commons.bytecode.JarInspector;
import com.ensoftcorp.open.java.commons.log.Log;

public class ProjectJarProperties extends AnalysisPropertiesInitializer {

	private static final String JAR_MANIFEST_MAIN_CLASS = "Main-Class";
	private static final String JARS = "jars";
	private static final String JAR = "jar";
	private static final String JAR_APPLICATIONS = "applications";
	private static final String JAR_RUNTIMES = "runtimes";
	private static final String JAR_LIBRARIES = "libraries";
	
	private static final String JAR_MAIN_CLASS_ATTRIBUTE = "main-class";
	private static final String JAR_PATH_ATTRIBUTE = "path";
	
	private static Set<String> jdkLibraries = getJDKLibraries();
	
	public static enum JarType {
		APPLICATION, LIBRARY, RUNTIME
	}
	
	public static class Jar {
		protected IProject project;
		protected String portablePath;
		protected File file;
		protected String manifestMainClass = null;
		protected JarType type;
		protected Map<String,String> attributes;
		
		protected Jar(IProject project, JarType type, String portablePath) throws CoreException {
			this(project, type, portablePath, new HashMap<String,String>());
		}
		
		protected Jar(IProject project, JarType type, String portablePath, Map<String,String> attributes) throws CoreException {
			this(project, type, portablePath, null, new HashMap<String,String>());
		}
		
		protected Jar(IProject project, JarType type, String portablePath, String manifestMainClass) throws CoreException {
			this(project, type, portablePath, manifestMainClass, new HashMap<String,String>());
		}
		
		protected Jar(IProject project, JarType type, String portablePath, String manifestMainClass, Map<String,String> attributes) throws CoreException {
			this.project = project;
			this.type = type;
			this.portablePath = portablePath;
			this.manifestMainClass = manifestMainClass;
			this.attributes = attributes;
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
		
		public IProject getProject(){
			return project;
		}
		
		public Map<String,String> getJarAttributes(){
			return new HashMap<String,String>(attributes);
		}
		
		public void setJarAttribute(String name, String value) throws Exception {
			Map<String,String> attributes = new HashMap<String,String>();
			attributes.put(name, value);
			setJarAttributes(attributes);
		}
		
		public void setJarAttributes(Map<String,String> attributes) throws Exception {
			Document properties = ProjectAnalysisProperties.getAnalysisProperties(project);

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
				NodeList jarEntryElements = jarsElement.getChildNodes();
				for(int j=0; j<jarEntryElements.getLength(); j++){
					if(!(jarEntryElements.item(j) instanceof Element)){
						continue;
					}
					Element jarEntryElement = (Element) jarEntryElements.item(j);
					if(!(jarEntryElement.getTagName().equals(JAR_APPLICATIONS) || jarEntryElement.getTagName().equals(JAR_LIBRARIES) || jarEntryElement.getTagName().equals(JAR_RUNTIMES))){
						continue;
					}
					NodeList jarsElements = jarEntryElement.getChildNodes();
					for(int k=0; k<jarsElements.getLength(); k++){
						if(!(jarsElements.item(k) instanceof Element)){
							continue;
						}
						Element jarElement = (Element) jarsElements.item(k);
						if(!jarElement.getTagName().equals(JAR)){
							continue;
						}
						if(jarElement.hasAttribute(JAR_PATH_ATTRIBUTE) && jarElement.getAttribute(JAR_PATH_ATTRIBUTE).equals(portablePath)){
							// set the attribute values
							for(Entry<String,String> entry : attributes.entrySet()){
								jarElement.setAttribute(entry.getKey(), entry.getValue());
							}
						}
					}
				}
			}
			
			ProjectAnalysisProperties.setAnalysisProperties(project, properties);
			
			for(Entry<String,String> entry : attributes.entrySet()){
				this.attributes.put(entry.getKey(), entry.getValue());
			}
		}
		
		public void delete() throws Exception {
			Document properties = ProjectAnalysisProperties.getAnalysisProperties(project);

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
				NodeList jarEntryElements = jarsElement.getChildNodes();
				for(int j=0; j<jarEntryElements.getLength(); j++){
					if(!(jarEntryElements.item(j) instanceof Element)){
						continue;
					}
					Element jarEntryElement = (Element) jarEntryElements.item(j);
					if(!(jarEntryElement.getTagName().equals(JAR_APPLICATIONS) || jarEntryElement.getTagName().equals(JAR_LIBRARIES) || jarEntryElement.getTagName().equals(JAR_RUNTIMES))){
						continue;
					}
					NodeList jarsElements = jarEntryElement.getChildNodes();
					for(int k=0; k<jarsElements.getLength(); k++){
						if(!(jarsElements.item(k) instanceof Element)){
							continue;
						}
						Element jarElement = (Element) jarsElements.item(k);
						if(!jarElement.getTagName().equals(JAR)){
							continue;
						}
						if(jarElement.hasAttribute(JAR_PATH_ATTRIBUTE) && jarElement.getAttribute(JAR_PATH_ATTRIBUTE).equals(portablePath)){
							// remove the entry
							jarElement.getParentNode().removeChild(jarElement);
						}
					}
				}
			}
			
			ProjectAnalysisProperties.setAnalysisProperties(project, properties);
		}
		
		public JarType getJarType(){
			return type;
		}
		
		public void setJarType(JarType type) throws Exception {
			if(type == getJarType()){
				return; // nothing to do
			} else {
				Document properties = ProjectAnalysisProperties.getAnalysisProperties(project);
				
				// step 1) remove entry from properties
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
					NodeList jarEntryElements = jarsElement.getChildNodes();
					for(int j=0; j<jarEntryElements.getLength(); j++){
						if(!(jarEntryElements.item(j) instanceof Element)){
							continue;
						}
						Element jarEntryElement = (Element) jarEntryElements.item(j);
						if(!(jarEntryElement.getTagName().equals(JAR_APPLICATIONS) || jarEntryElement.getTagName().equals(JAR_LIBRARIES) || jarEntryElement.getTagName().equals(JAR_RUNTIMES))){
							continue;
						}
						NodeList jarsElements = jarEntryElement.getChildNodes();
						for(int k=0; k<jarsElements.getLength(); k++){
							if(!(jarsElements.item(k) instanceof Element)){
								continue;
							}
							Element jarElement = (Element) jarsElements.item(k);
							if(!jarElement.getTagName().equals(JAR)){
								continue;
							}
							if(jarElement.hasAttribute(JAR_PATH_ATTRIBUTE) && jarElement.getAttribute(JAR_PATH_ATTRIBUTE).equals(portablePath)){
								// remove the entry
								jarElement.getParentNode().removeChild(jarElement);
							}
						}
					}
				}
				
				// step 2) added updated entry to properties
				String newType;
				switch (type){
				case APPLICATION:
					newType = JAR_APPLICATIONS;
					break;
				case LIBRARY:
					newType = JAR_LIBRARIES;
					break;
				case RUNTIME:
					newType = JAR_RUNTIMES;
					break;
					default: 
						throw new RuntimeException("Unhandled library type.");
				}
				
				for(int i=0; i<rootChildren.getLength(); i++){
					if(!(rootChildren.item(i) instanceof Element)){
						continue;
					}
					Element rootChild = (Element) rootChildren.item(i);
					if(!rootChild.getTagName().equals(JARS)){
						continue;
					}
					Element jarsElement = rootChild;
					NodeList jarEntryElements = jarsElement.getChildNodes();
					for(int j=0; j<jarEntryElements.getLength(); j++){
						if(!(jarEntryElements.item(j) instanceof Element)){
							continue;
						}
						Element jarEntryElement = (Element) jarEntryElements.item(j);
						if(!jarEntryElement.getTagName().equals(newType)){
							continue;
						}
						
						Element jarLibraryElement = properties.createElement(JAR);
						jarLibraryElement.setAttribute(JAR_PATH_ATTRIBUTE, portablePath);
						if(manifestMainClass != null){
							jarLibraryElement.setAttribute(JAR_MAIN_CLASS_ATTRIBUTE, manifestMainClass);
						}
						jarEntryElement.appendChild(jarLibraryElement);
					}
				}
				
				// step 3) overwrite the old properties with new properties
				ProjectAnalysisProperties.setAnalysisProperties(project, properties);
				
				this.type = type;
			}
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
			String prefix = "";
			switch (getJarType()){
			case APPLICATION:
				prefix = "Application ";
				break;
			case LIBRARY:
				prefix = "Library ";
				break;
			case RUNTIME:
				prefix = "Runtime ";
				break;
				default: 
					throw new RuntimeException("Unhandled library type.");
			}
			if(manifestMainClass != null){
				return prefix + "Jar [Path=" + getPortablePath() + ", ManifestMainClass=" + manifestMainClass + "]";
			} else {
				return prefix + "Jar [Path=" + getPortablePath() + "]";
			}
		}
	}
	
	public static Set<Jar> getJars(IProject project) throws Exception {
		Set<Jar> jars = new HashSet<Jar>();
		jars.addAll(getApplicationJars(project));
		jars.addAll(getLibraryJars(project));
		jars.addAll(getRuntimeJars(project));
		return jars;
	}
	
	private static Set<String> getJDKLibraries() {
		Set<String> jdkLibraries = new HashSet<String>();
		for(String jdkLibrary : SetDefinitions.JDK_LIBRARIES){
			jdkLibraries.add(jdkLibrary);
		}
		return jdkLibraries;
	}

	public static Set<Jar> getApplicationJars(IProject project) throws Exception {
		Document properties = ProjectAnalysisProperties.getAnalysisProperties(project);
		Set<Jar> applicationJars = new HashSet<Jar>();
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
						// collect all the extensible attributes
						Map<String,String> attributes = new HashMap<String,String>();
						NamedNodeMap jarApplicationElementAttributes = jarApplicationElement.getAttributes();
						for (int l = 0; l < jarApplicationElementAttributes.getLength(); l++) {
							org.w3c.dom.Node attr = jarApplicationElementAttributes.item(l);
							if(!attr.getNodeName().equals(JAR_PATH_ATTRIBUTE) && !attr.getNodeName().equals(JAR_MAIN_CLASS_ATTRIBUTE)){
								attributes.put(attr.getNodeName(), attr.getNodeValue());
							}
						}
						
						String path = jarApplicationElement.getAttribute(JAR_PATH_ATTRIBUTE);
						if(!jarApplicationElement.hasAttribute(JAR_MAIN_CLASS_ATTRIBUTE)){
							applicationJars.add(new Jar(project, JarType.APPLICATION, path, attributes));
						} else {
							applicationJars.add(new Jar(project, JarType.APPLICATION, path, jarApplicationElement.getAttribute(JAR_MAIN_CLASS_ATTRIBUTE), attributes));
						}
					}
				}
			}
		}
		return applicationJars;
	}
	
	public static Set<Jar> getRuntimeJars(IProject project) throws Exception {
		Document properties = ProjectAnalysisProperties.getAnalysisProperties(project);
		Set<Jar> runtimeJars = new HashSet<Jar>();
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
			NodeList jarRuntimesElements = jarsElement.getChildNodes();
			for(int j=0; j<jarRuntimesElements.getLength(); j++){
				if(!(jarRuntimesElements.item(j) instanceof Element)){
					continue;
				}
				Element jarRuntimesElement = (Element) jarRuntimesElements.item(j);
				if(!jarRuntimesElement.getTagName().equals(JAR_RUNTIMES)){
					continue;
				}
				NodeList jarRuntimeElements = jarRuntimesElement.getChildNodes();
				for(int k=0; k<jarRuntimeElements.getLength(); k++){
					if(!(jarRuntimeElements.item(k) instanceof Element)){
						continue;
					}
					Element jarRuntimeElement = (Element) jarRuntimeElements.item(k);
					if(!jarRuntimeElement.getTagName().equals(JAR)){
						continue;
					}
					if(jarRuntimeElement.hasAttribute(JAR_PATH_ATTRIBUTE)){
						// collect all the extensible attributes
						Map<String,String> attributes = new HashMap<String,String>();
						NamedNodeMap jarApplicationElementAttributes = jarRuntimeElement.getAttributes();
						for (int l = 0; l < jarApplicationElementAttributes.getLength(); l++) {
							org.w3c.dom.Node attr = jarApplicationElementAttributes.item(l);
							if(!attr.getNodeName().equals(JAR_PATH_ATTRIBUTE) && !attr.getNodeName().equals(JAR_MAIN_CLASS_ATTRIBUTE)){
								attributes.put(attr.getNodeName(), attr.getNodeValue());
							}
						}
						
						String path = jarRuntimeElement.getAttribute(JAR_PATH_ATTRIBUTE);
						if(!jarRuntimeElement.hasAttribute(JAR_MAIN_CLASS_ATTRIBUTE)){
							runtimeJars.add(new Jar(project, JarType.RUNTIME, path, attributes));
						} else {
							runtimeJars.add(new Jar(project, JarType.RUNTIME, path, jarRuntimeElement.getAttribute(JAR_MAIN_CLASS_ATTRIBUTE), attributes));
						}
					}
				}
			}
		}
		return runtimeJars;
	}
	
	public static Set<Jar> getLibraryJars(IProject project) throws Exception {
		Document properties = ProjectAnalysisProperties.getAnalysisProperties(project);
		Set<Jar> libraryJars = new HashSet<Jar>();
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
						// collect all the extensible attributes
						Map<String,String> attributes = new HashMap<String,String>();
						NamedNodeMap jarApplicationElementAttributes = jarLibraryElement.getAttributes();
						for (int l = 0; l < jarApplicationElementAttributes.getLength(); l++) {
							org.w3c.dom.Node attr = jarApplicationElementAttributes.item(l);
							if(!attr.getNodeName().equals(JAR_PATH_ATTRIBUTE) && !attr.getNodeName().equals(JAR_MAIN_CLASS_ATTRIBUTE)){
								attributes.put(attr.getNodeName(), attr.getNodeValue());
							}
						}
						
						String path = jarLibraryElement.getAttribute(JAR_PATH_ATTRIBUTE);
						if(!jarLibraryElement.hasAttribute(JAR_MAIN_CLASS_ATTRIBUTE)){
							libraryJars.add(new Jar(project, JarType.LIBRARY, path, attributes));
						} else {
							libraryJars.add(new Jar(project, JarType.LIBRARY, path, jarLibraryElement.getAttribute(JAR_MAIN_CLASS_ATTRIBUTE), attributes));
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
		
		// search for an existing jars element
		Element jarsElement = null;
		NodeList rootChildren = properties.getDocumentElement().getChildNodes();
		for(int i=0; i<rootChildren.getLength(); i++){
			if(!(rootChildren.item(i) instanceof Element)){
				continue;
			}
			Element rootChild = (Element) rootChildren.item(i);
			if(!rootChild.getTagName().equals(JARS)){
				continue;
			}
			jarsElement = rootChild;
			
			Log.warning("Project JAR Properties are being reinitialized for project " + project.getName());
		}
		// in jars element does not exist then create one now
		if(jarsElement == null){
			jarsElement = properties.createElement(JARS);
			properties.getDocumentElement().appendChild(jarsElement);
		}
		
		Set<IFile> libraries = new HashSet<IFile>();
		Map<IFile,String> jarMainClasses = new HashMap<IFile,String>();
		
		// add main classes in jar manifest libraries for each library method
		for(Node library : Common.universe().nodes(XCSG.Library).eval().nodes()){
			// search for libraries based on source correspondence
			try {
				File libraryFile = null;
				SourceCorrespondence sc = (SourceCorrespondence) library.getAttr(XCSG.sourceCorrespondence);
				if(sc != null){
					// ideally we can just use the node's source correspondence
					libraryFile = WorkspaceUtils.getFile(sc.sourceFile);
				} else {
					// node name may be an absolute file path in some setups
					libraryFile = new File(library.getAttr(XCSG.name).toString());
				}
				if(libraryFile.exists()){
					libraries.add(sc.sourceFile);
					JarInspector jarInspector = new JarInspector(libraryFile);
					if(jarInspector.getManifest() != null){
						String mainClass = jarInspector.getManifest().getMainAttributes().getValue(JAR_MANIFEST_MAIN_CLASS);
						if(mainClass != null && !mainClass.isEmpty()){
							jarMainClasses.put(sc.sourceFile, mainClass);
						}
					}
				}
			} catch (Exception e){
				Log.warning("Could not inspect indexed library: " + library.getAttr(XCSG.name) + "\n" + library.toString(), e);
			}
		}
		
		// check the project's classpath (if it has one...)
		try {
			IJavaProject jProject = JavaCore.create(project);
			IClasspathEntry[] entries = jProject.getRawClasspath();
			for(IClasspathEntry entry : entries){
				String entryPath = entry.getPath().toOSString();
				if(entryPath.endsWith(".jar")){
					File entryFile = new File(entryPath);
					if(entryFile.exists()){
						// absolute path
						// TODO: how to handle absolute paths
					} else {
						// relative path
						if(entryPath.startsWith("/" + project.getName())){
							entryPath = entryPath.substring(("/" + project.getName()).length());
						}
						IFile libraryEntry = project.getFile(entryPath);
						if(libraryEntry.exists()){
							libraries.add(libraryEntry);
						}
					}
				}
			}
		} catch (Exception e){
			// project may not actually be a java nature...
			// its ok to fail silently here
		}
		
		// default to searching for libraries contained in the project subfolder
		for(File jarFile : findJars(new File(project.getLocation().toOSString()))){
			try {
				libraries.add(WorkspaceUtils.getFile(jarFile));
				JarInspector jarInspector = new JarInspector(jarFile);
				if(jarInspector.getManifest() != null){
					String mainClass = jarInspector.getManifest().getMainAttributes().getValue(JAR_MANIFEST_MAIN_CLASS);
					if(mainClass != null && !mainClass.isEmpty()){
						jarMainClasses.put(WorkspaceUtils.getFile(jarFile), mainClass);
					}
				}
			} catch (Exception e){
				Log.warning("Could not inspect project library: " + jarFile.getName() , e);
			}
		}
		
		// at this time we are just initializing the application property key
		// user or another analysis can decide which of the libraries may be
		// part of the application
		
		// by default consider none of the libraries as application libraries
		Element jarApplicationsElement = properties.createElement(JAR_APPLICATIONS);
		jarsElement.appendChild(jarApplicationsElement);
		
		// add all libraries as support libraries
		ArrayList<IFile> jarsSorted = new ArrayList<IFile>(libraries);
		Collections.sort(jarsSorted, new Comparator<IFile>(){
			@Override
			public int compare(IFile a, IFile b) {
				return a.getName().compareTo(b.getName());
			}
		});
		
		Element jarLibrariesElement = properties.createElement(JAR_LIBRARIES);
		jarsElement.appendChild(jarLibrariesElement);
		
		Element jarRuntimesElement = properties.createElement(JAR_RUNTIMES);
		jarsElement.appendChild(jarRuntimesElement);

		for(IFile jar : jarsSorted){
			Element jarLibraryElement = properties.createElement(JAR);
			IFile projectResource = project.getFile(jar.getProjectRelativePath());
			boolean isRelativePath = projectResource != null && projectResource.exists();
			if(isRelativePath){
				jarLibraryElement.setAttribute(JAR_PATH_ATTRIBUTE, jar.getProjectRelativePath().toPortableString());
			} else {
				jarLibraryElement.setAttribute(JAR_PATH_ATTRIBUTE, jar.getFullPath().toPortableString());
			}
			if(jarMainClasses.containsKey(jar)){
				jarLibraryElement.setAttribute(JAR_MAIN_CLASS_ATTRIBUTE, jarMainClasses.get(jar));
			}
			if(jdkLibraries.contains(jar.getName())){
				jarRuntimesElement.appendChild(jarLibraryElement);
			} else {
				jarLibrariesElement.appendChild(jarLibraryElement);
			}
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
