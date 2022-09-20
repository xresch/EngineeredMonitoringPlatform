package com.xresch.emp.features.mongodb;

import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.features.dashboard.parameters.ParameterDefinition;

public class ParameterDefinitionMongoDBEnvironment extends ParameterDefinition {

	public static final String LABEL = "MongoDB Environment";
	
	/***************************************************************
	 * 
	 ***************************************************************/
	@Override
	public String getParamLabel() { return LABEL; }

	/***************************************************************
	 * 
	 ***************************************************************/
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public CFWField getFieldForSettings(HttpServletRequest request, String dashboardid, Object fieldValue) {
		CFWField settingsField = MongoDBSettingsFactory.createEnvironmentSelectorField();
				
		if(fieldValue != null) {
			settingsField.setValueConvert(fieldValue, true);
		}
	
		return settingsField;
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	@SuppressWarnings({ "rawtypes" })
	@Override
	public CFWField getFieldForWidget(HttpServletRequest request, String dashboardid, Object fieldValue) {

		return getFieldForSettings(request, dashboardid, fieldValue);
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	@Override
	public boolean isAvailable(HashSet<String> widgetTypesArray) {
		
		for(String type : widgetTypesArray) {
			if(type.contains("emp_mongodb")) {
				return true;
			}
			
		}
		return false;
	}

}
