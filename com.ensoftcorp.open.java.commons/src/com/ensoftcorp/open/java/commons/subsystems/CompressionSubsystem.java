package com.ensoftcorp.open.java.commons.subsystems;

import com.ensoftcorp.open.commons.subsystems.Subsystem;

public class CompressionSubsystem extends JavaSubsystem {

	public static final String TAG = "COMPRESSION_SUBSYSTEM";

	@Override
	public String getName() {
		return "Compression";
	}

	@Override
	public String getDescription() {
		return "Java compression libraries";
	}

	@Override
	public String getTag() {
		return TAG;
	}

	@Override
	public String[] getParentTags() {
		return new String[] { SerializationSubsystem.TAG };
	}

	@Override
	public String[] getNamespaces() {
		return new String[] { "java.util.jar", "java.util.zip" };
	}
}
