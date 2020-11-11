package com.xresch.emp.features.dynatrace;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

public class DynatraceWidgetSettingsFactory {
	
	/************************************************************************************
	 * Returns the dynatrace environment selector field.
	 * 
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createDynatraceEnvironmentSelectorField(){
		return CFWField.newString(FormFieldType.SELECT, "environment")
			.setLabel("{!cfw_widget_dynatrace_environment!}")
			.setDescription("{!cfw_widget_dynatrace_environment_desc!}")
			.setOptions(CFW.DB.ContextSettings.getSelectOptionsForType(DynatraceManagedEnvironment.SETTINGS_TYPE));
	}

	/************************************************************************************
	 * Returns default host selector field to select a single host.
	 * This field requires input from the field created by createDynatraceEnvironmentSelectorField();
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createSingleHostSelectorField(){
		
		return CFWField.newTagsSelector("JSON_HOST")
				.setLabel("{!emp_widget_dynatrace_host!}")
				.setDescription("{!emp_widget_dynatrace_host_desc!}")
				.addAttribute("maxTags", "1")
				.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
					
					@Override
					public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue) {
						
						String environment = request.getParameter("environment");
						if(Strings.isNullOrEmpty(environment) ) {
							CFW.Context.Request.addAlertMessage(MessageType.INFO, "Please select an environment first.");
							return null;
						}
						
						return DynatraceManagedEnvironment.autocompleteHosts(Integer.parseInt(environment), searchValue, this.getMaxResults());
					}
				});
	}
	
	/************************************************************************************
	 * Returns default process selector field to select a process of a specific host.
	 * This field requires input from the field created by createSingleHostSelectorField();
	 * 
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createSingleProcessGroupSelectorField(){
		
		return CFWField.newTagsSelector("JSON_PROCESS")
				.setLabel("{!emp_widget_dynatrace_processgroup!}")
				.setDescription("{!emp_widget_dynatrace_processgroup_desc!}")
				.addAttribute("maxTags", "1")
				.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
					
					@Override
					public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue) {

						String environment = request.getParameter("environment");
						if(Strings.isNullOrEmpty(environment) ) {
							CFW.Context.Request.addAlertMessage(MessageType.INFO, "Please select an environment first.");
							return null;
						}
						
						String host = request.getParameter("JSON_HOST");
						if(Strings.isNullOrEmpty(host) || host.equals("{}")) {
							CFW.Context.Request.addAlertMessage(MessageType.INFO, "Please select a host first.");
							return null;
						}
						
						JsonObject hostObject = CFW.JSON.jsonStringToJsonElement(host).getAsJsonObject();
						String hostID = hostObject.keySet().toArray(new String[]{})[0];
						
						return DynatraceManagedEnvironment.autocompleteProcesses(
								Integer.parseInt(environment), 
								searchValue, 
								this.getMaxResults(),hostID);
					}
				});
	}

	/************************************************************************************
	 * Returns the metrics selector field to select multiple metrics.
	 * 
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createMetricsSelectorField(String entityType){
		
		return 	CFWField.newTagsSelector("JSON_METRICS")
				.setLabel("{!emp_widget_dynatrace_metrics!}")
				.setDescription("{!emp_widget_dynatrace_metrics_desc!}")
				.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
					
					@Override
					public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue) {
						
						String environment = request.getParameter("environment");
						if(Strings.isNullOrEmpty(environment) ) {
							CFW.Context.Request.addAlertMessage(MessageType.INFO, "Please select an environment first.");
							return null;
						}
						
						return DynatraceManagedEnvironment.autocompleteMetrics(Integer.parseInt(environment), searchValue, this.getMaxResults());
					}
				});
	}
	

		

}
