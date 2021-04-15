package com.xresch.emp.features.influxdb;

import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.features.dashboard.parameters.ParameterDefinition;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

public class ParameterDefinitionInfluxDBEnvironment extends ParameterDefinition {

	public static final String LABEL = "InfluxDB Environment";
	
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
		CFWField settingsField = InfluxDBSettingsFactory.createEnvironmentSelectorField();
				
		if(fieldValue != null) {
			settingsField.setValueConvert(fieldValue);
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
			if(type.contains("influxdb")) {
				return true;
			}
			
		}
		return false;
	}

}
