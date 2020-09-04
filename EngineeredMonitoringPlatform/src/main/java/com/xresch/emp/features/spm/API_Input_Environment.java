package com.xresch.emp.features.spm;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;

/********************************************************************
 *
 ********************************************************************/
public class API_Input_Environment extends CFWObject {
	
	private CFWField<Integer> environmentID = CFWField.newInteger(FormFieldType.SELECT, "ENVIRONMENT_ID")
			.setDescription("The id of the environment.")
			.setValue(0)
			.setOptions(EnvironmentManagerSPM.getEnvironmentsAsSelectOptions());
	
	public API_Input_Environment() {
		this.addFields(environmentID);
	}
	
	public Integer environmentID() {
		return environmentID.getValue();
	}
	
	public API_Input_Environment environmentID(Integer value) {
		this.environmentID.setValue(value);
		return this;
	}
	
	
}