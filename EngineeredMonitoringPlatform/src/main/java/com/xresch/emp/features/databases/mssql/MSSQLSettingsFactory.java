package com.xresch.emp.features.databases.mssql;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;

public class MSSQLSettingsFactory {
	
	/************************************************************************************
	 * Returns the  environment selector field.
	 * 
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createEnvironmentSelectorField(){
		return CFWField.newString(FormFieldType.SELECT, "environment")
				.setLabel("{!emp_common_environment!}")
				.setDescription("{!emp_common_environment!}")
				.setOptions(CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(MSSQLEnvironment.SETTINGS_TYPE));
	}
		

}