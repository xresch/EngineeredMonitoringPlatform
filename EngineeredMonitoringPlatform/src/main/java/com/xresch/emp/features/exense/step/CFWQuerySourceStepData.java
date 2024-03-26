package com.xresch.emp.features.exense.step;

import java.rmi.AccessException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.concurrent.LinkedBlockingQueue;

import org.bson.Document;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoIterable;
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
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;
	
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQuerySourceStepData extends CFWQuerySource {

	private String contextSettingsType = StepEnvironment.SETTINGS_TYPE;
	
	private static final String FIELDNAME_ENVIRONMENT = "environment";
	private static final String FIELDNAME_TYPE = "type";
	
	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQuerySourceStepData(CFWQuery parent) {
		super(parent);
	}
	

	/******************************************************************
	 *
	 ******************************************************************/
	public String uniqueName() {
		return "stepdata";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionShort() {
		return "Get specific data related to STEP.";
	}
		
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionRequiredPermission() {
		return FeatureExenseStep.PERMISSION_STEP;
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureExenseStep.PERMISSION_STEP);
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionTime() {
		return "Time is ignored.";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureExenseStep.PACKAGE_MANUAL, "manual_source_stepdata.html")
				.replaceAll("\\{sourcename\\}", this.uniqueName());
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
				CFWField.newString(FormFieldType.TEXTAREA, FIELDNAME_TYPE)
						.setDescription("Choose the type of data to fetch: projects | schedulers | plans.")
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
		
		int environmentID = Integer.parseInt(environmentString);
		
		//-----------------------------
		// Get Environment
		StepEnvironment environment;
		environment = StepEnvironmentManagement.getEnvironment(environmentID);
		if(environment == null) {
			return;
		}
		
		//-----------------------------
		// Resolve Type
		String type = (String)parameters.getField(FIELDNAME_TYPE).getValue();
		
		if(Strings.isNullOrEmpty(type)) {
			return;
		}
		
		type = type.trim().toLowerCase();
		
		//-----------------------------
		// fetchData

		if(type.startsWith("scheduler")) {
			for(StepSchedulerDetails scheduler : environment.getSchedulersAll() ) { 
				EnhancedJsonObject object = new EnhancedJsonObject( scheduler.toJson() );
				outQueue.put(object);
			}
			return;
		}else if(type.startsWith("plan")) {
			for(JsonObject object : environment.getPlansAll() ) { 
				EnhancedJsonObject enhanced = new EnhancedJsonObject( object.deepCopy() );
				outQueue.put(enhanced);
			}
		}else if(type.startsWith("project")) {
			for(JsonObject object : environment.getProjectsAll() ) { 
				EnhancedJsonObject enhanced = new EnhancedJsonObject( object.deepCopy() );
				outQueue.put(enhanced);
			}
		}
		

	}

}
