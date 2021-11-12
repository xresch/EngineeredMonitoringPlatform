package com.xresch.emp.features.influxdb;

import javax.servlet.http.HttpServletRequest;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;

public class InfluxDBSettingsFactory {
	
	public static final String FIELDNAME_QUERY = "QUERY";
	public static final String FIELDNAME_DB_OR_BUCKET = "JSON_DATABASE_OR_BUCKET";
	public static final String FIELDNAME_ENVIRONMENT = "ENVIRONMENT";


	/************************************************************************************
	 * Returns the environment selector field.
	 * 
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createEnvironmentSelectorField(){
		return CFWField.newString(FormFieldType.SELECT, FIELDNAME_ENVIRONMENT)
				.setLabel("{!emp_widget_spm_environment!}")
				.setDescription("{!emp_widget_spm_environment_desc!}")
				.setOptions(CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(InfluxDBEnvironment.SETTINGS_TYPE));
	}
	
	
	/************************************************************************************
	 * Returns the database or Bucket selector field.
	 * 
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createDatabaseOrBucketSelectorField(){
		return 	(CFWField)CFWField.newTagsSelector(FIELDNAME_DB_OR_BUCKET)
				.setLabel("{!emp_widget_influxdb_databasebucket!}")
				.setDescription("{!emp_widget_influxdb_databasebucket_desc!}")
				.setOptions(CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(InfluxDBEnvironment.SETTINGS_TYPE))
				.setAutocompleteHandler(new CFWAutocompleteHandler(10, 3) {
					@Override
					public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue) {
						String environment = request.getParameter(FIELDNAME_ENVIRONMENT);
						return InfluxDBEnvironment.autocompleteDatabaseOrBucket(Integer.parseInt(environment), searchValue, this.getMaxResults());
					}
				})
				.addAttribute("maxTags", "1")
				.addCssClass("textarea-nowrap");
	}
	
	
	/************************************************************************************
	 * Returns the database or Bucket selector field.
	 * 
	 * @return
	 ************************************************************************************/
	public static CFWField<?> createQueryField(){
		return (CFWField)CFWField.newString(FormFieldType.TEXTAREA, FIELDNAME_QUERY)
			.setLabel("{!emp_widget_influxdb_query!}")
			.setDescription("{!emp_widget_influxdb_query_desc!}")
			.setOptions(CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(InfluxDBEnvironment.SETTINGS_TYPE))
			.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
				@Override
				public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue) {
					return InfluxDBEnvironment.autocompleteQuery(searchValue, this.getMaxResults());
				}
			})
			.addCssClass("textarea-nowrap");
	}
	

	
	

	
		

}
