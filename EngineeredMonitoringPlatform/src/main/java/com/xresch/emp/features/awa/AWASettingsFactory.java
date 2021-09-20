package com.xresch.emp.features.awa;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;

public class AWASettingsFactory {
	
	/************************************************************************************
	 * Returns the  environment selector field.
	 * 
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createEnvironmentSelectorField(){
		return CFWField.newInteger(FormFieldType.SELECT, "environment")
				.setLabel("{!emp_widget_awajobstatus_environment!}")
				.setDescription("{!emp_widget_awajobstatus_environment_desc!}")
				.setOptions(CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(AWAEnvironment.SETTINGS_TYPE));
	}
		

}
