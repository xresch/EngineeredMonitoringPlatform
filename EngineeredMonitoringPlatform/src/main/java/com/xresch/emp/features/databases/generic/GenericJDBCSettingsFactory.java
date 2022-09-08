package com.xresch.emp.features.databases.generic;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.CFWFieldFlag;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;

public class GenericJDBCSettingsFactory {
	
	/************************************************************************************
	 * Returns the  environment selector field.
	 * 
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createEnvironmentSelectorField(){
		return CFWField.newString(FormFieldType.SELECT, "environment")
				.setLabel("{!emp_common_environment!}")
				.setDescription("{!emp_common_environment!}")
				.setOptions(CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(GenericJDBCEnvironment.SETTINGS_TYPE))
				.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
				;
	}
		

}
