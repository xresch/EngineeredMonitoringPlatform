package com.xresch.emp.features.dynatrace;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.core.AutocompleteItem;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.dashboard.DashboardWidget;
import com.xresch.cfw.features.dashboard.DashboardWidget.DashboardWidgetFields;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.utils.CFWHttp.CFWHttpResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class DynatraceManagedEnvironment extends AbstractContextSettings {
	
	public static final String SETTINGS_TYPE = "Dynatrace Managed Environment";
	private String URL_API_V1 = null;
	private String URL_API_V2 = null;
	
	private HashMap<String,String> tokenHeader = null;
			
	public enum PrometheusEnvironmentFields{
		API_URL,
		API_TOKEN,
	}
			
	private CFWField<String> apiUrl = CFWField.newString(FormFieldType.TEXT, PrometheusEnvironmentFields.API_URL)
			.allowHTML(true)
			.setDescription("The URL of the dynatrace api without the version(e.g https://yourinstance.live.dynatrace.com/api).");
	
	private CFWField<String> apiToken = CFWField.newString(FormFieldType.TEXT, PrometheusEnvironmentFields.API_TOKEN)
			.setDescription("The apiToken used to access the prometheus instance.");
	
	
	public DynatraceManagedEnvironment() {
		initializeFields();
	}
		
	private void initializeFields() {
		this.addFields(apiUrl, apiToken);
	}
			
	@Override
	public boolean isDeletable(int id) {

		int count = new DashboardWidget()
			.selectCount()
			.whereLike(DashboardWidgetFields.JSON_SETTINGS, "%\"environment\":"+id+"%")
			//.custom("AND \"TYPE\" LIKE 'emp_prometheus%'")
			.getCount();
		
		if(count == 0) {
			return true;
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The Dynatrace environment cannot be deleted as it is still in use by "+count+"  dashboard widget(s).");
			return false;
		}

	}
	
	public boolean isDefined() {
		if(apiUrl.getValue() != null
		&& apiToken.getValue() != null) {
			return true;
		}
		
		return false;
	}
	
	
	public String apiURL() {
		return apiUrl.getValue();
	}
	
	public DynatraceManagedEnvironment apiURL(String value) {
		this.apiUrl.setValue(value);
		return this;
	}
		
	public String apiToken() {
		return apiToken.getValue();
	}
	
	public DynatraceManagedEnvironment apiToken(String value) {
		this.apiToken.setValue(value);
		return this;
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public String getAPIUrlV1() {
		
		if(URL_API_V1 == null) {
			URL_API_V1 = apiUrl.getValue()+"/v1";
		}
		
		return URL_API_V1;
	}
	
	public String getAPIUrlV2() {
		
		if(URL_API_V2 == null) {
			URL_API_V2 = apiUrl.getValue()+"/v2";
		}
		
		return URL_API_V2;
	}
	
	public HashMap<String,String> getTokenHeader() {
		
		if(tokenHeader == null) {
			tokenHeader = new HashMap<>();
			tokenHeader.put("Authorization", "Api-Token "+apiToken.getValue());
		}
		
		return tokenHeader;
	}
	

	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public JsonArray getAllHosts() {
		//curl -H 'Authorization: Api-Token token' \ -X GET "https://lpi31515.live.dynatrace.com/api/v1/entity/infrastructure/hosts"

		String queryURL = getAPIUrlV1() + "/entity/infrastructure/hosts";
		
		HashMap<String,String> requestParams = new HashMap<>();
		requestParams.put("includeDetails", "false");
		
		CFWHttpResponse queryResult = CFW.HTTP.sendGETRequest(queryURL, requestParams, this.getTokenHeader());
		if(queryResult != null) {
			JsonElement jsonElement = CFW.JSON.fromJson(queryResult.getResponseBody());
			
			JsonArray jsonArray = new JsonArray();
			if(jsonElement.isJsonArray()) {
				jsonArray = jsonElement.getAsJsonArray();
			}else if(jsonElement.isJsonObject()) {
				JsonObject object = jsonElement.getAsJsonObject();
				if(object.get("error") != null) {
					CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Dynatrace Error: "+object.get("error").toString());
					return null;
				}
			}
			
			
			
			return jsonArray;
		}
		return null;
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public JsonObject getHostDetails(String hostID) {
		//curl -H 'Authorization: Api-Token token' \ -X GET "https://lpi31515.live.dynatrace.com/api/v1/entity/infrastructure/hosts"

		String queryURL = getAPIUrlV1() + "/entity/infrastructure/hosts/"+hostID;
		
		CFWHttpResponse queryResult = CFW.HTTP.sendGETRequest(queryURL, null, this.getTokenHeader());
		if(queryResult != null) {
			JsonElement jsonElement = CFW.JSON.fromJson(queryResult.getResponseBody());
			JsonObject json = jsonElement.getAsJsonObject();
			
			if(json.get("error") != null) {
				CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Dynatrace Error: "+json.get("error").getAsString());
				return null;
			}
			
			return json;
		}
		return null;
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
//	public JsonObject query(String prometheusQuery, long latestMillis) {
//		
//		String queryURL = getAPIUrlV1() 
//				+ "/query?query="+CFW.HTTP.encode(prometheusQuery)
//				+ "&time="+(latestMillis/1000);
//		
//		CFWHttpResponse queryResult = CFW.HTTP.sendGETRequest(queryURL);
//		if(queryResult != null) {
//			JsonElement jsonElement = CFW.JSON.fromJson(queryResult.getResponseBody());
//			JsonObject json = jsonElement.getAsJsonObject();
//			if(json.get("error") != null) {
//				CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Prometheus Error: "+json.get("error").getAsString());
//				return null;
//			}
//			
//			return json;
//		}
//		return null;
//	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
//	public JsonObject queryRange(String prometheusQuery, long earliestMillis, long latestMillis) {
//		
//		String interval = CFW.Time.calculateDatapointInterval(earliestMillis, latestMillis, 100);
//		
//		prometheusQuery = prometheusQuery.replace("[interval]", "["+( (interval.endsWith("s")) ? "1m" : interval )+"]");
//
//		String queryURL = getAPIUrlV1() 
//				+ "/query_range?query="+CFW.HTTP.encode(prometheusQuery)
//				+"&start="+(earliestMillis/1000)
//				+"&end="+(latestMillis/1000)
//				+"&step="+interval;
//		
//		CFWHttpResponse queryResult = CFW.HTTP.sendGETRequest(queryURL);
//		if(queryResult != null) {
//			JsonElement jsonElement = CFW.JSON.fromJson(queryResult.getResponseBody());
//			JsonObject json = jsonElement.getAsJsonObject();
//			if(json.get("error") != null) {
//				CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Prometheus Error: "+json.get("error").getAsString());
//				return null;
//			}
//			
//			return json;
//		}
//		return null;
//	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public static AutocompleteResult autocompleteHosts(int environmentID, String searchValue, int limit) {
		
		if(searchValue.length() < 3) {
			return null;
		}
		
		DynatraceManagedEnvironment environment = DynatraceManagedEnvironmentManagement.getEnvironment(environmentID);
		
		searchValue = searchValue.toLowerCase();
		
		JsonArray hostArray = environment.getAllHosts();
		if(hostArray == null) { return new AutocompleteResult();}
		
		AutocompleteList suggestions = new AutocompleteList();
		for(int i = 0; i < hostArray.size(); i++) {
		
			JsonObject currentHostObject = hostArray.get(i).getAsJsonObject();
			String hostID = currentHostObject.get("entityId").getAsString();
			String displayName = currentHostObject.get("displayName").getAsString();
			String discoveredName = currentHostObject.get("discoveredName").getAsString();

			if(hostID.toLowerCase().contains(searchValue)
			|| displayName.toLowerCase().contains(searchValue)
			|| discoveredName.toLowerCase().contains(searchValue)) {
				
				suggestions.addItem(hostID, displayName, 
						"Tags:"+currentHostObject.get("tags").toString()
						+", OS Type:"+currentHostObject.get("osType").getAsString());
				
				if(suggestions.getItems().size() == limit) {
					break;
				}
			}
			
		}
		
		return new AutocompleteResult(suggestions);
	}
	
}
