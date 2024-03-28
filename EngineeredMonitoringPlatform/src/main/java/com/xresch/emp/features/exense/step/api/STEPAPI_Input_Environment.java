package com.xresch.emp.features.exense.step.api;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.emp.features.exense.step.StepEnvironmentManagement;

/********************************************************************
 *
 ********************************************************************/
public class STEPAPI_Input_Environment extends CFWObject {
	
	private CFWField<Integer> environmentID = CFWField.newInteger(FormFieldType.SELECT, STEPAPI_Factory.INPUT_ENVIRONMENT)
			.setDescription("The id of the environment.")
			.setOptions(StepEnvironmentManagement.getEnvironmentsAsSelectOptions());
	
	public STEPAPI_Input_Environment() {
		this.addFields(environmentID);
	}
	
	public Integer environmentID() {
		return environmentID.getValue();
	}
	
	public STEPAPI_Input_Environment environmentID(Integer value) {
		this.environmentID.setValue(value);
		return this;
	}
	
	
}