package com.xresch.emp.features.prometheus;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;

public class PrometheusSettingsFactory {
	
	/************************************************************************************
	 * Returns the dynatrace environment selector field.
	 * 
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createPrometheusEnvironmentSelectorField(){
		return CFWField.newString(FormFieldType.SELECT, "environment")
				.setLabel("{!emp_widget_spm_environment!}")
				.setDescription("{!emp_widget_spm_environment_desc!}")
				.setOptions(CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(PrometheusEnvironment.SETTINGS_TYPE));
	}
		

}
