package com.xresch.emp.features.prometheus;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.CFWFieldFlag;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;

public class PrometheusSettingsFactory {
	
	public static final String FIELDNAME_ENVIRONMENT = "environment";

	/************************************************************************************
	 * Returns the dynatrace environment selector field.
	 * 
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createEnvironmentSelectorField(){
		// use string to allow parameter substitution
		return CFWField.newString(FormFieldType.SELECT, FIELDNAME_ENVIRONMENT)
				.setLabel("{!emp_common_environment!}")
				.setDescription("{!emp_common_environment!}")
				.setOptions(CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(PrometheusEnvironment.SETTINGS_TYPE))
				.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
				;
	}
		
}
