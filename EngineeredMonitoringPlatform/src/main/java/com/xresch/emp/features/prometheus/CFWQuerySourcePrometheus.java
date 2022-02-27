package com.xresch.emp.features.prometheus;

import java.rmi.AccessException;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

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
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;
	
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQuerySourcePrometheus extends CFWQuerySource {

	private static final String FIELDNAME_QUERY = "query";
	private static final String FIELDNAME_ENVIRONMENT = "environment";
	//private static final String FIELDNAME_TIMEFORMAT = "timeformat";



	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQuerySourcePrometheus(CFWQuery parent) {
		super(parent);
	}

	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String uniqueName() {
		return "prometheus";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionShort() {
		return "Fetches data from a prometheus environment.";
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionTime() {
		return "No special handling needed for time input.";
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".sources", "source_json.html");
	}
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionRequiredPermission() {
		return "None";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return true;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void autocomplete(AutocompleteResult result, CFWQueryAutocompleteHelper helper) {
		
		// if source name is given, list up to 50 available prometheus environments
		if( helper.getTokenCount() >= 2 ) {
			
			HashMap<Integer, Object> environmentMap = CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(PrometheusEnvironment.SETTINGS_TYPE);
			
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
					CFWField.newString(FormFieldType.TEXTAREA, FIELDNAME_QUERY)
						.setDescription("The prometheus query to fetch the data.")
						.addValidator(new NotNullOrEmptyValidator())
				)
				.addField(
						CFWField.newString(FormFieldType.TEXT, FIELDNAME_ENVIRONMENT)
							.setDescription("The prometheus environment to fetch the data from.")	
					)
				
//				.addField(
//						CFWField.newString(FormFieldType.TEXTAREA, FIELDNAME_TIMEFORMAT)
//							.setDescription("The format of the time in the time field. (Default: 'epoch').")	
//							.setValue("epoch")
//					)
			;
	}
	

	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public void execute(CFWObject parameters, LinkedBlockingQueue<EnhancedJsonObject> outQueue, long earliestMillis, long latestMillis, int limit) throws Exception {
		
		//-----------------------------
		// Resolve Query
		String query = (String)parameters.getField(FIELDNAME_QUERY).getValue();
		
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
			System.out.println("CheckPermissions");
			HashMap<Integer, Object> environmentMap = CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(PrometheusEnvironment.SETTINGS_TYPE);
			
			if( !environmentMap.containsKey(environmentID) ) {
				throw new AccessException("Missing permission to fetch from the specified prometheus environment with ID "+environmentID);
			}
		}
		
		//-----------------------------
		// Resolve Environment & Fetch Data
		PrometheusEnvironment environment =
					PrometheusEnvironmentManagement.getEnvironment(environmentID);

		JsonElement element = environment.queryRange(query, earliestMillis, latestMillis);
		
		if(element == null) { return; }
		
		//-----------------------------
		// Iterate Data
//		
//		{
//			"status": "success",
//			"data": {
//				"resultType": "vector",
//				"result": [
//					{
//						"metric": {
//							"instance": "myserver:9182",
//							"job": "example_job",
//							"volume": "C:"
//						},
//						"value": [
//							1594821066.464,
//							"46.14569686382971"
//						]
//					},
		if(element != null && element.isJsonObject()) {
			
			JsonObject resultObject = element.getAsJsonObject();
			JsonObject dataObject = resultObject.get("data").getAsJsonObject();
			if(dataObject == null || dataObject.isJsonNull()) {
				return;
			}
			
			String resultType = dataObject.get("resultType").getAsString();
			JsonArray resultArray = dataObject.get("result").getAsJsonArray();
			
			for(JsonElement resultElement : resultArray) {
				JsonObject metricObject = resultElement.getAsJsonObject().get("metric").getAsJsonObject();
				
				if(resultType.equals("vector")) {
					JsonObject recordForQueue = metricObject.deepCopy();
					JsonArray valueArray = resultElement.getAsJsonObject().get("value").getAsJsonArray();
					recordForQueue.addProperty("time", valueArray.get(0).getAsDouble()*1000);
					recordForQueue.addProperty("value", valueArray.get(1).getAsNumber());
					
					outQueue.add( new EnhancedJsonObject(recordForQueue));
					
				}else if(resultType.equals("matrix")) {
					JsonArray valuesArray = resultElement.getAsJsonObject().get("values").getAsJsonArray();
					
					for(JsonElement valueArrayElement : valuesArray) {
						JsonArray valueArray = valueArrayElement.getAsJsonArray();
						
						JsonObject recordForQueue = metricObject.deepCopy();
						
						recordForQueue.addProperty("time", valueArray.get(0).getAsDouble()*1000);
						recordForQueue.addProperty("value", valueArray.get(1).getAsNumber());
						
						outQueue.add( new EnhancedJsonObject(recordForQueue));
					}
				}else {
					throw new Exception("Unrecognized result type: "+resultType);
				}
			}
			
			
			return;
		}
		
	}

}
