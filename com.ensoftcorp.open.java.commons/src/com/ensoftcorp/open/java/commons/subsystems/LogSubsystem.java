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
		return new String[] { "java.util.logging" };
	}

}
