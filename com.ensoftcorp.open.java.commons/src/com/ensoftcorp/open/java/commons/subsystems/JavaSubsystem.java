package com.ensoftcorp.open.java.commons.subsystems;

import com.ensoftcorp.open.commons.subsystems.Subsystem;

public abstract class JavaSubsystem extends Subsystem {

	@Override
	public String getCategory() {
		return "JAVA_SUBSYSTEM";
	}
	
	@Override
	public String getCategoryDescription(){
		return "Java Subsystems";
	}
	
}
