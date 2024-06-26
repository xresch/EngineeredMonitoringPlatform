package com.xresch.emp.features.spm;

import javax.servlet.http.HttpServletRequest;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.CFWFieldFlag;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;

public class SPMSettingsFactory {
	
	//do not change string values, might corrupt existing widgets
	public static final String FIELDNAME_MEASURE = "measure";
	public static final String FIELDNAME_PROJECTS = "JSON_PROJECTS";
	public static final String FIELDNAME_MONITORS = "JSON_MONITORS";
	public static final String FIELDNAME_ENVIRONMENT = "environment";
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public static CFWField<?> createEnvironmentSelectorField(){
		// use string to allow parameter substitution
		return CFWField.newString(FormFieldType.SELECT, FIELDNAME_ENVIRONMENT)
				.setLabel("{!emp_widget_spm_environment!}")
				.setDescription("{!emp_widget_spm_environment_desc!}")
				.setOptions(CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(EnvironmentSPM.SETTINGS_TYPE))
				.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
				;
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public static CFWField<?> createMonitorSelectorField(){
		return CFWField.newTagsSelector(FIELDNAME_MONITORS)
				.setLabel("{!emp_widget_spm_monitor!}")
				.setDescription("{!emp_widget_spm_monitor_desc!}")
				.addAttribute("maxTags", "1")
				.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
				.setAutocompleteHandler(new CFWAutocompleteHandler(20) {
					
					@Override
					public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
						String environment = request.getParameter(FIELDNAME_ENVIRONMENT);
						
						return EnvironmentManagerSPM.autocompleteMonitors(Integer.parseInt(environment), searchValue, this.getMaxResults());
					}
			});
	}
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public static CFWField<?> createMonitorsSelectorField(){
		return CFWField.newTagsSelector(FIELDNAME_MONITORS)
				.setLabel("{!emp_widget_spm_monitor!}")
				.setDescription("{!emp_widget_spm_monitor_desc!}")
				.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
				.setAutocompleteHandler(new CFWAutocompleteHandler(20) {
					
					@Override
					public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
						String environment = request.getParameter(FIELDNAME_ENVIRONMENT);
						
						return EnvironmentManagerSPM.autocompleteMonitors(Integer.parseInt(environment), searchValue, this.getMaxResults());
					}
				});
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public static CFWField<?> createProjectsSelectorField(int maxTags){
		return 	CFWField.newTagsSelector(FIELDNAME_PROJECTS)
				.setLabel("{!emp_widget_spm_project!}")
				.setDescription("{!emp_widget_spm_project_desc!}")
				.addAttribute("maxTags", ""+maxTags)
				.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
				.setAutocompleteHandler(new CFWAutocompleteHandler(20) {
					
					@Override
					public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
						String environment = request.getParameter(FIELDNAME_ENVIRONMENT);
						
						return EnvironmentManagerSPM.autocompleteProjects(Integer.parseInt(environment), searchValue, this.getMaxResults());
					}
				});		
		
	}
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public static CFWField<?> createMeasureSelectField(){
		return 	CFWField.newString(FormFieldType.SELECT, FIELDNAME_MEASURE)
				.setLabel("{!emp_widget_spm_measure!}")
				.setDescription("{!emp_widget_spm_measure_desc!}")
				.setOptions(new String[]{"Overall Health", "Availability", "Accuracy", "Performance"})
				.setValue("Overall Health");		
		
	}
		

}
