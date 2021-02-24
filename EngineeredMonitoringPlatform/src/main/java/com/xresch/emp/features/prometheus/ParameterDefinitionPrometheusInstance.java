package com.xresch.emp.features.prometheus;

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

public class ParameterDefinitionPrometheusInstance extends ParameterDefinition {

	public static final String LABEL = "Prometheus Instance";
	
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
		CFWField settingsField = CFWField.newString(FormFieldType.TAGS, LABEL)
				.addAttribute("maxTags", "1")
				.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
					
					@Override
					public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue) {
						String environmentID = request.getParameter("environment");
						
						if(Strings.isNullOrEmpty(environmentID)) {
							CFW.Context.Request.addAlertMessage(MessageType.INFO, "Please choose an environment first.");
							return null;
						}
						
						PrometheusEnvironment environment = PrometheusEnvironmentManagement.getEnvironment(Integer.parseInt(environmentID));
						
						return environment.autocompleteInstance(searchValue, this.getMaxResults());
					}
				});
				
		
		if(fieldValue !=null) {
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
			if(type.contains("prometheus")) {
				return true;
			}
			
		}
		return false;
	}

}
