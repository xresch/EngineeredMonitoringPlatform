package com.xresch.emp.features.dynatrace;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.CFWFieldFlag;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.validation.NumberRangeValidator;
import com.xresch.emp.features.dynatrace.DynatraceEnvironment.EntityType;

public class DynatraceSettingsFactory {
	
	/************************************************************************************
	 * Returns the dynatrace environment selector field.
	 * 
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createEnvironmentSelectorField(){
		return CFWField.newString(FormFieldType.SELECT, "environment")
			.setLabel("{!emp_widget_dynatrace_environment!}")
			.setDescription("{!emp_widget_dynatrace_environment_desc!}")
			.setOptions(CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(DynatraceEnvironment.SETTINGS_TYPE))
			.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
			;
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
				.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
				.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
					
					@Override
					public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
						
						String environment = request.getParameter("environment");
						if(Strings.isNullOrEmpty(environment) ) {
							CFW.Messages.addInfoMessage("Please select an environment first.");
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
	public static CFWField<?> createSingleProcessGroupInstanceSelectorField(){
		
		return CFWField.newTagsSelector("JSON_PROCESS")
				.setLabel("{!emp_widget_dynatrace_processgroup!}")
				.setDescription("{!emp_widget_dynatrace_processgroup_desc!}")
				.addAttribute("maxTags", "1")
				.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
				.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
					
					@Override
					public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {

						String environment = request.getParameter("environment");
						if(Strings.isNullOrEmpty(environment) ) {
							CFW.Messages.addInfoMessage("Please select an environment first.");
							return null;
						}
						
						String host = request.getParameter("JSON_HOST");
						if(Strings.isNullOrEmpty(host) || host.equals("{}")) {
							CFW.Messages.addInfoMessage( "Please select a host first.");
							return null;
						}
						
						JsonObject hostObject = CFW.JSON.stringToJsonElement(host).getAsJsonObject();
						String hostID = hostObject.keySet().toArray(new String[]{})[0];
						
						return DynatraceEnvironment.autocompleteProcessGroupInstance(
								Integer.parseInt(environment), 
								searchValue, 
								this.getMaxResults(),hostID);
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
		
		return CFWField.newTagsSelector("JSON_PROCESS_GROUP")
				.setLabel("{!emp_widget_dynatrace_processgroup!}")
				.setDescription("{!emp_widget_dynatrace_processgroup_desc!}")
				.addAttribute("maxTags", "1")
				.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
				.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
					
					@Override
					public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {

						String environment = request.getParameter("environment");
						if(Strings.isNullOrEmpty(environment) ) {
							CFW.Messages.addInfoMessage( "Please select an environment first.");
							return null;
						}
						
						String host = request.getParameter("JSON_HOST");
						if(Strings.isNullOrEmpty(host) || host.equals("{}")) {
							CFW.Messages.addInfoMessage("Please select a host first.");
							return null;
						}
						
						JsonObject hostObject = CFW.JSON.stringToJsonElement(host).getAsJsonObject();
						String hostID = hostObject.keySet().toArray(new String[]{})[0];
						
						return DynatraceEnvironment.autocompleteProcessGroup(
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
				.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
				.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
					
					@Override
					public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
						
						String environment = request.getParameter("environment");
						if(Strings.isNullOrEmpty(environment) ) {
							CFW.Messages.addInfoMessage("Please select an environment first.");
							return null;
						}
						
						return DynatraceEnvironment.autocompleteMetrics(Integer.parseInt(environment), searchValue, this.getMaxResults());
					}
				});
	}
	
	/************************************************************************************
	 * Returns default log selector field to select a single log for the specified host.
	 * This field requires input from the field created by createSingleHostSelectorField();
	 * @param entityType HOST or PROCESS_GROUP
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createSingleLogSelectorField(EntityType entityType){
		
		return CFWField.newTagsSelector("JSON_LOG")
				.setLabel("{!emp_widget_dynatrace_log!}")
				.setDescription("{!emp_widget_dynatrace_log_desc!}")
				.addAttribute("maxTags", "1")
				.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
				.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
					
					@Override
					public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
						
						String environment = request.getParameter("environment");
						if(Strings.isNullOrEmpty(environment) ) {
							CFW.Messages.addInfoMessage("Please select an environment first.");
							return null;
						}
						
						String entityString = request.getParameter( (entityType == EntityType.HOST ? "JSON_HOST" : "JSON_PROCESS_GROUP") );
						if(Strings.isNullOrEmpty(entityString) || entityString.equals("{}")) {
							CFW.Messages.addInfoMessage("Please select a host or process first.");
							return null;
						}
						
						JsonObject entityObject = CFW.JSON.stringToJsonElement(entityString).getAsJsonObject();
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
				.setDescription("{!emp_widget_dynatrace_logquery_desc!}")
				.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
				;
	}
	
	/************************************************************************************
	 * Returns default log query field to define the query to filter the logs.
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createLogMaxEntriesField(){
		
		return CFWField.newInteger(FormFieldType.NUMBER, "LOG_MAX_ENTRIES")
				.setLabel("{!emp_widget_dynatrace_logmaxentries!}")
				.setDescription("{!emp_widget_dynatrace_logmaxentries_desc!}")
				.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
				.setValue(50)
				.addValidator(new NumberRangeValidator(1, 10000));
	}

		

}
