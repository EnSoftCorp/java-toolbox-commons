package com.ensoftcorp.open.java.commons.subsystems;

public class LogSubsystem extends JavaSubsystem {

	public static final String TAG = "LOG_SUBSYSTEM";

	@Override
	public String getName() {
		return "Log";
	}

	@Override
	public String getDescription() {
		return "Java logging libraries";
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
		return new String[] { 
			"java.util.logging", // standard JDK logging apis
			"org.apache.logging.log4j", // Public API for Log4j 2
			"org.apache.logging.log4j.message", // Public Message Types used for Log4j 2
			"org.apache.logging.log4j.simple", // Simple logging implementation
			"org.apache.logging.log4j.spi", // Internal interfaces and classes to be used by authors of logging implementations or for internal use by API classes
			"org.apache.logging.log4j.status", // Status API for Log4j 2
			"org.apache.logging.log4j.util" // Internal utility classes for the Log4j 2 API
			};
	}

}
