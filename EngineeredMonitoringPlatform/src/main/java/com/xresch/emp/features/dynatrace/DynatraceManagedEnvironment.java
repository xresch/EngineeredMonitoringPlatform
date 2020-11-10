package com.xresch.emp.features.dynatrace;

import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
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
	public static final long MILLIS_3_DAYS = 1000L * 60 * 60 * 24 * 3;
	
	public enum PrometheusEnvironmentFields{
		API_URL,
		API_TOKEN,
	}
	
	private String URL_API_V1 = null;
	private String URL_API_V2 = null;
	
	private HashMap<String,String> tokenHeader = null;
	

	/** Static field to store the assembled results by their file names. */
	private static final Cache<String, CFWHttpResponse> DYNATRACE_CACHE = CFW.Caching.addCache("EMP Dynatrace Cache", 
			CacheBuilder.newBuilder()
				.initialCapacity(10)
				.maximumSize(1000)
				.expireAfterAccess(1, TimeUnit.MINUTES)
		);

			
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
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public String getAPIUrlV2() {
		
		if(URL_API_V2 == null) {
			URL_API_V2 = apiUrl.getValue()+"/v2";
		}
		
		return URL_API_V2;
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
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
	public String getStartTimestampParam(long startTimestamp, long endTimestamp) {
		long duration = endTimestamp - startTimestamp;
		if(duration <= MILLIS_3_DAYS) {
			return startTimestamp+"";
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "Dynatrace widgets support a maximum timerange of 3 days(latest time - 3 days is shown).");
			return (endTimestamp - MILLIS_3_DAYS)+"";
		}
	}

	/************************************************************************************
	 * 
	 ************************************************************************************/
	public CFWHttpResponse sendCachedGetRequest(String url, HashMap<String, String> params, HashMap<String, String> headers) {

		String identifier = getDefaultObject().name()+url+params;

		try {
			return DYNATRACE_CACHE.get(identifier, new Callable<CFWHttpResponse>() {

				@Override
				public CFWHttpResponse call() throws Exception {
					return CFW.HTTP.sendGETRequest(url, params, headers);
				}
				
			});
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	

	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public JsonArray getAllHosts(Long startTimestamp, Long endTimestamp) {
		//curl -H 'Authorization: Api-Token token' \ -X GET "https://lpi31515.live.dynatrace.com/api/v1/entity/infrastructure/hosts"

		String queryURL = getAPIUrlV1() + "/entity/infrastructure/hosts";
		
		HashMap<String,String> requestParams = new HashMap<>();
		requestParams.put("includeDetails", "false");
		if(startTimestamp != null && endTimestamp != null) {
			requestParams.put("startTimestamp", getStartTimestampParam(startTimestamp,endTimestamp));
			requestParams.put("endTimestamp", endTimestamp+"");
		}
				
		CFWHttpResponse queryResult = sendCachedGetRequest(queryURL, requestParams, this.getTokenHeader());
		
		if(queryResult != null) {			
			return queryResult.getRequestBodyAsJsonArray();
		}
		
		return null;
	}
	
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public JsonObject getHostDetails(String hostID) {
		//curl -H 'Authorization: Api-Token token' \ -X GET "https://lpi31515.live.dynatrace.com/api/v1/entity/infrastructure/hosts"

		String queryURL = getAPIUrlV1() + "/entity/infrastructure/hosts/"+hostID;
		
		CFWHttpResponse queryResult = sendCachedGetRequest(queryURL, null, this.getTokenHeader());
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
	public JsonArray getHostProcesses(String hostID, long startTimestamp, long endTimestamp) {
		// curl -H 'Authorization: Api-Token token' \ -X GET "https://lpi31515.live.dynatrace.com/api/v1/entity/infrastructure/processes?startTimestamp=1604372880000&endTimestamp=1604588879160&host=HOST-1812428021FCE23D"

		String queryURL = getAPIUrlV1() + "/entity/infrastructure/processes";
		
		HashMap<String,String> requestParams = new HashMap<>();
		requestParams.put("host", hostID);
		requestParams.put("startTimestamp", getStartTimestampParam(startTimestamp,endTimestamp));
		requestParams.put("endTimestamp", endTimestamp+"");
		
		CFWHttpResponse queryResult = sendCachedGetRequest(queryURL, requestParams, this.getTokenHeader());
		
		if(queryResult != null) {			
			return queryResult.getRequestBodyAsJsonArray();
		}
		
		return null;
	}
		
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public static AutocompleteResult autocompleteHosts(int environmentID, String searchValue, int limit) {
		
		if(searchValue.length() < 3) {
			return null;
		}
		
		DynatraceManagedEnvironment environment = DynatraceManagedEnvironmentManagement.getEnvironment(environmentID);
		
		searchValue = searchValue.toLowerCase();
		
		JsonArray hostArray = environment.getAllHosts(null,null);
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
