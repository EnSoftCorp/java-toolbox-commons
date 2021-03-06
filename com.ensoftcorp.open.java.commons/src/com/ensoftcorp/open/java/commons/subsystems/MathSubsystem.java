package com.ensoftcorp.open.java.commons.subsystems;

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
		return new String[] { "java.math", "org.apache.commons.math3.linear", "org.apfloat", "org.apache.commons.math3.complex", "org.apache.commons.math3.analysis" };
	}
	
	@Override
	public String[] getTypes() {
		return new String[] { "java.lang.Math", "org.jtransforms.fft.FloatFFT_1D" };
	}
}
