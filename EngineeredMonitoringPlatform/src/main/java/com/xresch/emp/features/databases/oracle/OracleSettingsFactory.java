package com.xresch.emp.features.databases.oracle;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;

public class OracleSettingsFactory {
	
	/************************************************************************************
	 * Returns the  environment selector field.
	 * 
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createEnvironmentSelectorField(){
		return CFWField.newString(FormFieldType.SELECT, "environment")
				.setLabel("{!emp_common_environment!}")
				.setDescription("{!emp_common_environment_desc!}")
				.setOptions(CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(OracleEnvironment.SETTINGS_TYPE));
	}
		

}
