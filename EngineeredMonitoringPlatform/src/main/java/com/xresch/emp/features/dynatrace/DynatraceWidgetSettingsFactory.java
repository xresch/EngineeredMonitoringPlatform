package com.xresch.emp.features.dynatrace;

import javax.servlet.http.HttpServletRequest;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;

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
	 * 
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
						
						return DynatraceManagedEnvironment.autocompleteHosts(Integer.parseInt(environment), searchValue, this.getMaxResults());
					}
				});
	}

	/************************************************************************************
	 * Returns the metrics selector field to select multiple metrics.
	 * 
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createMetricsSelectorField(){
		
		return 	CFWField.newTagsSelector("JSON_METRICS")
				.setLabel("{!emp_widget_dynatrace_metrics!}")
				.setDescription("{!emp_widget_dynatrace_metrics_desc!}")
				.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
					
					@Override
					public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue) {
						String environment = request.getParameter("environment");
						
						return DynatraceManagedEnvironment.autocompleteMetrics(Integer.parseInt(environment), searchValue, this.getMaxResults());
					}
				});
	}
	

		

}
