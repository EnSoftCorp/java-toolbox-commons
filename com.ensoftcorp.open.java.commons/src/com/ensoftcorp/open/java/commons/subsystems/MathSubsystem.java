package com.ensoftcorp.open.java.commons.subsystems;

import com.ensoftcorp.open.commons.subsystems.Subsystem;

public class MathSubsystem extends JavaSubsystem {

	public static final String TAG = "MATH_SUBSYSTEM";

	@Override
	public String getName() {
		return "Math";
	}

	@Override
	public String getDescription() {
		return "Java math libraries";
	}

	@Override
	public String getTag() {
		return TAG;
	}

	@Override
	public String[] getParentTags() {
		return new String[] { JavaCoreSubsystem.TAG };
	}

	@Override
	public String[] getNamespaces() {
		return new String[] { "java.math" };
	}
}
