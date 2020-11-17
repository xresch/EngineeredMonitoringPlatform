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
import com.xresch.cfw.validation.NumberRangeValidator;
import com.xresch.emp.features.dynatrace.DynatraceEnvironment.EntityType;

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
			.setOptions(CFW.DB.ContextSettings.getSelectOptionsForType(DynatraceEnvironment.SETTINGS_TYPE));
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
						
						return DynatraceEnvironment.autocompleteHosts(Integer.parseInt(environment), searchValue, this.getMaxResults());
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
						
						return DynatraceEnvironment.autocompleteProcesses(
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
						
						return DynatraceEnvironment.autocompleteMetrics(Integer.parseInt(environment), searchValue, this.getMaxResults());
					}
				});
	}
	
	/************************************************************************************
	 * Returns default log selector field to select a single log for the specified host.
	 * This field requires input from the field created by createSingleHostSelectorField();
	 * @param entityType HOST or PROCESS_GROUP_INSTANCE
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createSingleLogSelectorField(EntityType entityType){
		
		return CFWField.newTagsSelector("JSON_LOG")
				.setLabel("{!emp_widget_dynatrace_log!}")
				.setDescription("{!emp_widget_dynatrace_log_desc!}")
				.addAttribute("maxTags", "1")
				.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
					
					@Override
					public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue) {
						
						String environment = request.getParameter("environment");
						if(Strings.isNullOrEmpty(environment) ) {
							CFW.Context.Request.addAlertMessage(MessageType.INFO, "Please select an environment first.");
							return null;
						}
						
						String entityString = request.getParameter( (entityType == EntityType.HOST ? "JSON_HOST" : "JSON_PROCESS") );
						if(Strings.isNullOrEmpty(entityString) || entityString.equals("{}")) {
							CFW.Context.Request.addAlertMessage(MessageType.INFO, "Please select a host or process first.");
							return null;
						}
						
						JsonObject entityObject = CFW.JSON.jsonStringToJsonElement(entityString).getAsJsonObject();
						String entityID = entityObject.keySet().toArray(new String[]{})[0];
						
						return DynatraceEnvironment.autocompleteLog(Integer.parseInt(environment), 
								searchValue, 
								this.getMaxResults(), 
								entityType,
								entityID);
					}
				});
	}
	
	/************************************************************************************
	 * Returns default log query field to define the query to filter the logs.
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createLogQueryField(){
		
		return CFWField.newString(FormFieldType.TEXT, "LOG_QUERY")
				.setLabel("{!emp_widget_dynatrace_logquery!}")
				.setDescription("{!emp_widget_dynatrace_logquery_desc!}");
	}
	
	/************************************************************************************
	 * Returns default log query field to define the query to filter the logs.
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createLogMaxEntriesField(){
		
		return CFWField.newInteger(FormFieldType.NUMBER, "LOG_MAX_ENTRIES")
				.setLabel("{!emp_widget_dynatrace_logmaxentries!}")
				.setDescription("{!emp_widget_dynatrace_logmaxentries_desc!}")
				.setValue(20)
				.addValidator(new NumberRangeValidator(1, 200));
	}

		

}
