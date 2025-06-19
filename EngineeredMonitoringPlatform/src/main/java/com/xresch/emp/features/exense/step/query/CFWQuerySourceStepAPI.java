package com.xresch.emp.features.exense.step.query;

import java.text.ParseException;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.CFWQuerySource;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;
import com.xresch.emp.features.exense.step.FeatureExenseStep;
import com.xresch.emp.features.exense.step.StepEnvironment;
import com.xresch.emp.features.exense.step.StepEnvironmentManagement;
	
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQuerySourceStepAPI extends CFWQuerySource {

	private String contextSettingsType = StepEnvironment.SETTINGS_TYPE;
	
	private static final String FIELDNAME_ENVIRONMENT = "environment";
	private static final String FIELDNAME_TABLE = "table";
	private static final String FIELDNAME_QUERY = "query";
	
	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQuerySourceStepAPI(CFWQuery parent) {
		super(parent);
	}
	

	/******************************************************************
	 *
	 ******************************************************************/
	public String uniqueName() {
		return "stepapi";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionShort() {
		return "Fetch data from the STEP's /table/{tablename} API endpoint.";
	}
		
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionRequiredPermission() {
		return FeatureExenseStep.PERMISSION_STEP_SOURCE_STEPAPI;
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureExenseStep.PERMISSION_STEP_SOURCE_STEPAPI);
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionTime() {
		return "Use earliest() and latest() function to insert time into your filter.";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureExenseStep.PACKAGE_MANUAL, "manual_source_stepapi.html");
	}
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		
		// if source name is given, list up to 50 available step environments
		if( helper.getCommandTokenCount() >= 2 ) {
			
			HashMap<Integer, Object> environmentMap = CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(contextSettingsType);
			
			AutocompleteList list = new AutocompleteList();
			result.addList(list);
			int i = 0;
			for (Object envID : environmentMap.keySet() ) {

				Object envName = environmentMap.get(envID);
				
				JsonObject envJson = new JsonObject();
				envJson.addProperty("id", Integer.parseInt(envID.toString()));
				envJson.addProperty("name", envName.toString());
				String envJsonString = "environment="+CFW.JSON.toJSON(envJson)+" ";
				
				list.addItem(
					helper.createAutocompleteItem(
						""
					  , envJsonString
					  , "Environment: "+envName
					  , envJsonString
					)
				);
				
				i++;
				
				if((i % 10) == 0) {
					list = new AutocompleteList();
					result.addList(list);
				}
				if(i == 50) { break; }
			}
		}
	}

	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public CFWObject getParameters() {
		return new CFWObject()
				.addField(
						CFWField.newString(FormFieldType.TEXT, FIELDNAME_ENVIRONMENT)
							.setDescription("The step environment to fetch the data from. Use Ctrl+Space in the query editor for content assist.")	
					)
				.addField(
						CFWField.newString(FormFieldType.TEXTAREA, FIELDNAME_TABLE)
							.setDescription("Specify the STEP table you want to fetch data from.")
							.disableSanitization() //do not mess up the gorgeous queries
							.addValidator(new NotNullOrEmptyValidator())
				)	
				.addField(
						CFWField.newString(FormFieldType.TEXTAREA, FIELDNAME_QUERY)
							.setDescription("Specify the STEP filter query.")
							.disableSanitization() //do not mess up the gorgeous queries
							.addValidator(new NotNullOrEmptyValidator())
				)	
			;
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public void parametersPermissionCheck(CFWObject parameters) throws ParseException {
		//-----------------------------
		// Resolve Environment ID
		String environmentString = (String)parameters.getField(FIELDNAME_ENVIRONMENT).getValue();

		if(environmentString.startsWith("{")) {
			JsonObject settingsObject = CFW.JSON.fromJson(environmentString).getAsJsonObject();
			
			if(settingsObject.get("id") != null) {
				 environmentString = settingsObject.get("id").getAsInt()+"";
			}
		}
		
		int environmentID = Integer.parseInt(environmentString);
		
		//-----------------------------
		// Check Permissions
		if(this.parent.getContext().checkPermissions()) {
			HashMap<Integer, Object> environmentMap = CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(contextSettingsType);
			
			if( !environmentMap.containsKey(environmentID) ) {
				throw new ParseException("Missing permission to fetch from the specified Step environment with ID "+environmentID, -1);
			}
		}
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public void execute(CFWObject parameters, LinkedBlockingQueue<EnhancedJsonObject> outQueue, long earliestMillis, long latestMillis, int limit) throws Exception {
		
		//-----------------------------
		// Resolve Environment ID
		String environmentString = (String)parameters.getField(FIELDNAME_ENVIRONMENT).getValue();

		if(environmentString != null && environmentString.startsWith("{")) {
			JsonObject settingsObject = CFW.JSON.fromJson(environmentString).getAsJsonObject();
			
			if(settingsObject.get("id") != null) {
				 environmentString = settingsObject.get("id").getAsInt()+"";
			}
		}
		
		if(environmentString == null) {
			 CFW.Messages.addWarningMessage("Parameter 'environment' cannot be null.");
			 return;
		}
			
		int environmentID = Integer.parseInt(environmentString);
		
		
		//-----------------------------
		// Get Environment
		StepEnvironment environment;
		environment = StepEnvironmentManagement.getEnvironment(environmentID);
		if(environment == null) {
			return;
		}
		
		//-----------------------------
		// Resolve table
		String table = (String)parameters.getField(FIELDNAME_TABLE).getValue();
		
		if(Strings.isNullOrEmpty(table)) {
			return;
		}
		
		table = table.trim();
		
		//-----------------------------
		// Resolve query
		String query = (String)parameters.getField(FIELDNAME_QUERY).getValue();
		
		if(Strings.isNullOrEmpty(query)) {
			return;
		}
		
		query = query.trim();
		
		//-----------------------------
		// fetchData

		JsonArray array = environment.getDataFromTableAPIEndpoint(table, query);
		for(JsonElement element : array ) { 
			
			EnhancedJsonObject object = new EnhancedJsonObject( element.getAsJsonObject() );
			outQueue.put(object);
		}
		return;

	}

}
