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
public class CFWQuerySourceStep extends CFWQuerySource {

	private String contextSettingsType = StepEnvironment.SETTINGS_TYPE;
	
	private static final String FIELDNAME_ENVIRONMENT = "environment";
	private static final String FIELDNAME_COLLECTION = "collection";
	private static final String FIELDNAME_FIND = "find";
	private static final String FIELDNAME_AGGREGATE = "aggregate";
	private static final String FIELDNAME_SORT = "sort";
	private static final String FIELDNAME_TIMEZONE = "timezone";
	
	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQuerySourceStep(CFWQuery parent) {
		super(parent);
	}
	

	/******************************************************************
	 *
	 ******************************************************************/
	public String getTimezone(int environmentID) {
		StepEnvironment environment =
				StepEnvironmentManagement.getEnvironment(environmentID);
		
		if(environment == null) { return null; }
		
		return environment.timezone();
	}

	/******************************************************************
	 *
	 ******************************************************************/
	public String uniqueName() {
		return "step";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionShort() {
		return "Fetches data from a Step database.";
	}
		
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionRequiredPermission() {
		return FeatureStep.PERMISSION_STEP;
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureStep.PERMISSION_STEP);
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionTime() {
		return "Use placeholders $earliest$ and $latest$ to insert epoch time in milliseconds into your MongoDB document.";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureStep.PACKAGE_RESOURCE, "z_manual_source_step.html")
				.replaceAll("\\{sourcename\\}", this.uniqueName());
	}
	
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		
		// if source name is given, list up to 50 available database environments
		if( helper.getTokenCount() >= 2 ) {
			
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
				CFWField.newString(FormFieldType.TEXTAREA, FIELDNAME_COLLECTION)
						.setDescription("Choose the MongoDB collection.")
						.disableSanitization() //do not mess up the gorgeous queries
						.addValidator(new NotNullOrEmptyValidator())
				)	
				.addField(CFWField.newString(FormFieldType.TEXTAREA, FIELDNAME_FIND)
						.setDescription("Specify the MongoDB document for the find method.")
						.disableSanitization() //do not mess up the gorgeous queries
				)
				.addField(CFWField.newString(FormFieldType.TEXTAREA, FIELDNAME_SORT)
						.setDescription("Specify the MongoDB document for that specifies the sort conditions.")
						.disableSanitization() //do not mess up the gorgeous queries
						)
				.addField(
						CFWField.newString(FormFieldType.TEXTAREA, FIELDNAME_AGGREGATE)
						.setDescription("Specify the MongoDB pipeline array for the aggregate method.")
						.disableSanitization() //do not mess up the gorgeous queries
				)
				.addField(
						CFWField.newString(FormFieldType.TEXT, FIELDNAME_TIMEZONE)
							.setDescription("Parameter can be used to adjust time zone differences between epoch time and the database. See manual for list of available zones.")	
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
		if(environmentString != null) {
			 environment = StepEnvironmentManagement.getEnvironment(Integer.parseInt(environmentString));
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "step: The chosen environment seems configured incorrectly or is unavailable.");
			return;
		}
		
		//-----------------------------
		// Resolve Timezone Offsets
		TimeZone timezone;
		String timezoneParam = (String)parameters.getField(FIELDNAME_TIMEZONE).getValue();
		if(!Strings.isNullOrEmpty(timezoneParam)) {
			timezone = TimeZone.getTimeZone(timezoneParam);
			
		}else {
			timezone = TimeZone.getTimeZone(
					Strings.nullToEmpty(this.getTimezone(environmentID))
				);
		}
		
		earliestMillis +=  timezone.getOffset(earliestMillis);
		latestMillis +=  timezone.getOffset(latestMillis);
		
		//-----------------------------
		// Resolve Collection Param
		String collectionName = (String)parameters.getField(FIELDNAME_COLLECTION).getValue();
		
		if(Strings.isNullOrEmpty(collectionName)) {
			return;
		}
		
		//-----------------------------
		// Resolve Find Param
		String findDocString = (String)parameters.getField(FIELDNAME_FIND).getValue();
		if(findDocString != null) {
			findDocString = findDocString.replace("$earliest$", ""+earliestMillis)
				 .replace("$latest$", ""+latestMillis)
				 ;
		}
		
		//-----------------------------
		// Resolve Aggregate Param
		String aggregateDocString = (String)parameters.getField(FIELDNAME_AGGREGATE).getValue();
		if(aggregateDocString != null) {
			aggregateDocString = aggregateDocString.replace("$earliest$", ""+earliestMillis)
				 .replace("$latest$", ""+latestMillis)
				 ;
		}
		
		//-----------------------------
		// Resolve Sort Param
		String sortDocString = (String)parameters.getField(FIELDNAME_SORT).getValue();
				
		//-----------------------------
		// Check Permissions
		if(this.parent.getContext().checkPermissions()) {
			HashMap<Integer, Object> environmentMap = CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(contextSettingsType);
			
			if( !environmentMap.containsKey(environmentID) ) {
				throw new AccessException("Missing permission to fetch from the specified database environment with ID "+environmentID);
			}
		}

		//-----------------------------
		// Fetch Data
		MongoIterable<Document> result;
		if(Strings.isNullOrEmpty(aggregateDocString)) {
			result = environment.find(collectionName, findDocString, sortDocString, limit);
		}else {
			result = environment.aggregate(collectionName, aggregateDocString);
		}
		
		//-----------------------------
		// Push to Queue
		if(result != null) {
			for (Document currentDoc : result) {
				JsonObject object = CFW.JSON.stringToJsonObject(currentDoc.toJson(FeatureStep.writterSettings));
				outQueue.add(new EnhancedJsonObject(object));
			}
		}
		
	}

}
