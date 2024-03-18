package com.xresch.emp.features.exense.step;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.CFWFieldFlag;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;

public class StepSettingsFactory {
	
	public static final String FIELDNAME_ENVIRONMENT = "environment";
	public static final String FIELDNAME_STEP_PROJECT = "JSON_STEP_PROJECT";
	
	public static final String FIELDNAME_STEP_SCHEDULERS = "JSON_STEP_SCHEDULERS";
	
	/************************************************************************************
	 * Returns the  environment selector field.
	 * 
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createEnvironmentSelectorField(){
		// use string to allow parameter substitution
		return CFWField.newString(FormFieldType.SELECT, FIELDNAME_ENVIRONMENT)
				.setLabel("{!emp_common_environment!}")
				.setDescription("{!emp_common_environment_desc!}")
				.setOptions(CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(StepEnvironment.SETTINGS_TYPE))
				.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY);
	}

	/************************************************************************************
	 *
	 ************************************************************************************/
	public static CFWField<?> createProjectsSelectorField() {
		return CFWField.newTagsSelector(FIELDNAME_STEP_PROJECT)
				.setLabel("{!emp_widget_step_projects!}")
				.setDescription("{!emp_widget_step_projects_desc!}")
				.addAttribute("maxTags", "128")
				.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
				.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
					
					@Override
					public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
						
						//-------------------------
						// Get ID
						String environmentID = request.getParameter(FIELDNAME_ENVIRONMENT);
						
						if(Strings.isNullOrEmpty(environmentID)) {
							CFW.Messages.addInfoMessage("Please select an environment first.");
						}
						
						//-------------------------
						// Get mongoDB
						StepEnvironment env = StepEnvironmentManagement.getEnvironment(Integer.parseInt(environmentID));
						if(env == null) {
							CFW.Messages.addWarningMessage("The chosen environment seems configured incorrectly or is unavailable.");
						}
						//-------------------------
						// Do Autocomplete
						return env.autocompleteProjects(searchValue, this.getMaxResults());
					}
				});
	}
	
	/************************************************************************************
	 *
	 ************************************************************************************/
	public static CFWField<?> createSchedulerSelectorField() {
		return CFWField.newTagsSelector(FIELDNAME_STEP_SCHEDULERS)
				.setLabel("{!emp_widget_step_schedulers!}")
				.setDescription("{!emp_widget_step_schedulers_desc!}")
				.addAttribute("maxTags", "128")
				.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
				.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
					
					@Override
					public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
						
						//-------------------------
						// Get ID
						String environmentID = request.getParameter(FIELDNAME_ENVIRONMENT);
						
						if(Strings.isNullOrEmpty(environmentID)) {
							CFW.Messages.addInfoMessage("Please select an environment first.");
						}
						
						//-------------------------
						// Get mongoDB
						StepEnvironment env = StepEnvironmentManagement.getEnvironment(Integer.parseInt(environmentID));
						if(env == null) {
							CFW.Messages.addWarningMessage("The chosen environment seems configured incorrectly or is unavailable.");
						}
						//-------------------------
						// Do Autocomplete
						return env.autocompleteSchedulers(searchValue, this.getMaxResults());
					}
				});
	}
	
	/************************************************************************************
	 *
	 ************************************************************************************/
	public static CFWField<?> createUsersSelectorField() {
		return CFWField.newTagsSelector(FIELDNAME_STEP_PROJECT)
				.setLabel("{!emp_widget_step_users!}")
				.setDescription("{!emp_widget_step_users_desc!}")
				.addAttribute("maxTags", "128")
				.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
				.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
					
					@Override
					public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
						
						//-------------------------
						// Get ID
						String environmentID = request.getParameter(FIELDNAME_ENVIRONMENT);
						
						if(Strings.isNullOrEmpty(environmentID)) {
							CFW.Messages.addInfoMessage("Please select an environment first.");
						}
						
						//-------------------------
						// Get mongoDB
						StepEnvironment env = StepEnvironmentManagement.getEnvironment(Integer.parseInt(environmentID));
						if(env == null) {
							CFW.Messages.addWarningMessage("The chosen environment seems configured incorrectly or is unavailable.");
						}
						//-------------------------
						// Do Autocomplete
						return env.autocompleteUsers(searchValue, this.getMaxResults());
					}
				});
	}
		

}
