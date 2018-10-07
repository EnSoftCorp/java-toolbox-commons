package com.ensoftcorp.open.java.commons.bytecode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.java.commons.log.Log;

public class BytecodeUtils {

	/**
	 * Reads a bytecode class file into a ClassNode object
	 * @param classFile
	 * @return
	 * @throws IOException
	 */
	public static ClassNode getClassNode(File classFile) throws IOException {
		byte[] bytes = Files.readAllBytes(classFile.toPath());
		return getClassNode(bytes);
	}

	/**
	 * Reads a bytecode class file into a ClassNode object
	 * @param classFile
	 * @return
	 * @throws IOException
	 */
	public static ClassNode getClassNode(byte[] bytes) {
		ClassReader classReader = new ClassReader(bytes);
		ClassNode classNode = new ClassNode();
		classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
		return classNode;
	}
	
	public static Node getTypeNode(String jvmDescriptor){
		return getTypeNode(null, jvmDescriptor);
	}
	
	public static Node getTypeNode(Node library, String jvmDescriptor){
		if(jvmDescriptor.endsWith(";")){
			jvmDescriptor = jvmDescriptor.substring(0, jvmDescriptor.length()-1);
		}
		int arrayDimension = 0;
		while(jvmDescriptor.startsWith("[")){
			jvmDescriptor = jvmDescriptor.substring(1);
			arrayDimension++;
		}
		
		Node typeNode = null;
		if(jvmDescriptor.equals("I") || jvmDescriptor.equals("int")){
			typeNode = Query.universe().nodes(XCSG.Primitive).selectNode(XCSG.name, "int").eval().nodes().one();
		} else if(jvmDescriptor.equals("J") || jvmDescriptor.equals("long")){
			typeNode = Query.universe().nodes(XCSG.Primitive).selectNode(XCSG.name, "long").eval().nodes().one();
		} else if(jvmDescriptor.equals("S") || jvmDescriptor.equals("short")){
			typeNode = Query.universe().nodes(XCSG.Primitive).selectNode(XCSG.name, "short").eval().nodes().one();
		} else if(jvmDescriptor.equals("F") || jvmDescriptor.equals("float")){
			typeNode = Query.universe().nodes(XCSG.Primitive).selectNode(XCSG.name, "float").eval().nodes().one();
		} else if(jvmDescriptor.equals("D") || jvmDescriptor.equals("double")){
			typeNode = Query.universe().nodes(XCSG.Primitive).selectNode(XCSG.name, "double").eval().nodes().one();
		} else if(jvmDescriptor.equals("C") || jvmDescriptor.equals("char")){
			typeNode = Query.universe().nodes(XCSG.Primitive).selectNode(XCSG.name, "char").eval().nodes().one();
		} else if(jvmDescriptor.equals("B") || jvmDescriptor.equals("byte")){
			typeNode = Query.universe().nodes(XCSG.Primitive).selectNode(XCSG.name, "byte").eval().nodes().one();
		} else if(jvmDescriptor.equals("Z") || jvmDescriptor.equals("boolean")){
			typeNode = Query.universe().nodes(XCSG.Primitive).selectNode(XCSG.name, "boolean").eval().nodes().one();
		} else if(jvmDescriptor.startsWith("L")){
			// any non-primitive Object
			jvmDescriptor = jvmDescriptor.substring(1);
			jvmDescriptor = jvmDescriptor.replace("/", ".").trim();
			String qualifiedClassName = jvmDescriptor;
			int index = qualifiedClassName.lastIndexOf(".");
			String className = qualifiedClassName;
			String pkgName = ""; // default package
			if(index != -1){
				pkgName = qualifiedClassName.substring(0, index);
				className = qualifiedClassName.substring(index+1, qualifiedClassName.length());
			}
			Q searchContext = library != null ? Common.toQ(library).contained() : Query.universe();
			Q pkgs = searchContext.nodes(XCSG.Package).selectNode(XCSG.name, pkgName);
			AtlasSet<Node> classNodes = pkgs.contained().nodes(XCSG.Classifier).selectNode(XCSG.name, className).eval().nodes();
			if(classNodes.isEmpty()){
				Log.debug("Could not find class: " + qualifiedClassName);
				return null;
			} else if(classNodes.size() > 1){
				Log.debug("Found multiple class matches for " + qualifiedClassName);
				typeNode = classNodes.one();
			} else {
				typeNode = classNodes.one();
			}
		}
		
		if(arrayDimension > 0){
			Q arrayElementTypeEdges = Query.universe().edges(XCSG.ArrayElementType);
			Q arrayTypes = arrayElementTypeEdges.predecessors(Common.toQ(typeNode));
			AtlasSet<Node> arrayDimensionTypes = arrayTypes.selectNode(XCSG.Java.arrayTypeDimension, arrayDimension).eval().nodes();
			if(arrayDimensionTypes.size() != 1){
				Log.warning("Could not find a matching array dimension for type [" + typeNode.address().toAddressString() + "]");
				return null;
			} else {
				return arrayDimensionTypes.one();
			}
		}
		
		return typeNode;
	}
	
	public static String normalizeDescriptor(String jvmDescriptor){
		String suffix = "";
		while(jvmDescriptor.startsWith("[")){
			jvmDescriptor = jvmDescriptor.substring(1);
			suffix+="[]";
		}
		if(jvmDescriptor.equals("I") || jvmDescriptor.equals("int")){
			jvmDescriptor = "int";
		} else if(jvmDescriptor.equals("J") || jvmDescriptor.equals("long")){
			jvmDescriptor = "long";
		} else if(jvmDescriptor.equals("S") || jvmDescriptor.equals("short")){
			jvmDescriptor = "short";
		} else if(jvmDescriptor.equals("F") || jvmDescriptor.equals("float")){
			jvmDescriptor = "float";
		} else if(jvmDescriptor.equals("D") || jvmDescriptor.equals("double")){
			jvmDescriptor = "double";
		} else if(jvmDescriptor.equals("C") || jvmDescriptor.equals("char")){
			jvmDescriptor = "char";
		} else if(jvmDescriptor.equals("B") || jvmDescriptor.equals("byte")){
			jvmDescriptor = "byte";
		} else if(jvmDescriptor.equals("Z") || jvmDescriptor.equals("boolean")){
			jvmDescriptor = "boolean";
		} else if(jvmDescriptor.startsWith("L")){
			// any non-primitive Object
			jvmDescriptor = jvmDescriptor.substring(1);
			jvmDescriptor = jvmDescriptor.replace("/", ".").trim();
		}
		jvmDescriptor += suffix;
		return jvmDescriptor.replace(";", "");
	}
	
}
