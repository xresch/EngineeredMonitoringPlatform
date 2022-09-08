package com.xresch.emp.features.mongodb;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.CFWFieldFlag;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;

public class MongoDBSettingsFactory {
	
	public static final String FIELDNAME_ENVIRONMENT = "environment";
	public static final String FIELDNAME_QUERY_COLLECTION = "JSON_COLLECTION";
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
				.setOptions(CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(MongoDBEnvironment.SETTINGS_TYPE))
				.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
				;
	}

	public static CFWField<?> createCollectionSelectorField() {
		return CFWField.newTagsSelector(FIELDNAME_QUERY_COLLECTION)
				.setLabel("{!emp_widget_mongodb_query_collection!}")
				.setDescription("{!emp_widget_mongodb_query_collection_desc!}")
				.addAttribute("maxTags", "1")
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
						MongoDBEnvironment env = MongoDBEnvironmentManagement.getEnvironment(Integer.parseInt(environmentID));
						if(env == null) {
							CFW.Messages.addWarningMessage("The chosen environment seems configured incorrectly or is unavailable.");
						}
						//-------------------------
						// Do Autocomplete
						return env.autocompleteCollection(searchValue, this.getMaxResults());
					}
				});
	}
		

}
