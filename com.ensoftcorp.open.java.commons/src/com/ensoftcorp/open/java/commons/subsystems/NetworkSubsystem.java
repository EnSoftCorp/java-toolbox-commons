package com.ensoftcorp.open.java.commons.subsystems;

public class NetworkSubsystem extends JavaSubsystem {

	public static final String TAG = "NETWORK_SUBSYSTEM";

	@Override
	public String getName() {
		return "Network";
	}

	@Override
	public String getDescription() {
		return "Java network IO libraries";
	}

	@Override
	public String getTag() {
		return TAG;
	}

	@Override
	public String[] getParentTags() {
		return new String[] { IOSubsystem.TAG };
	}

	@Override
	public String[] getNamespaces() {
		return new String[] { "java.net", "java.rmi", "java.rmi.activation", "java.rmi.dgc", "java.rmi.registry",
				"java.rmi.server", "javax.net", "javax.net.ssl", 
				"io.netty.util", "io.netty.channel.nio", "io.netty.util.internal", "io.netty.channel.socket.nio", "io.netty.bootstrap",
				"io.netty.channel", "io.netty.util.concurrent", "io.netty.channel.socket", "io.netty.buffer", "io.netty.util.internal.logging", "org.apache.commons.fileupload" 
				, "com.vaadin.server"};
	}
	
	@Override
	public String[] getTypes() {
		return new String[] { "com.sun.net.httpserver.HttpExchange", "fi.iki.elonen.NanoHTTPD"};
	}

}