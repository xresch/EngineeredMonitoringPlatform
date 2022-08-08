package com.xresch.emp.features.dynatrace;

import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

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
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.utils.CFWHttp.CFWHttpResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
/**
 * @author retos
 *
 */
public class DynatraceEnvironment extends AbstractContextSettings {
	
	private static Logger logger = CFWLog.getLogger(DynatraceEnvironment.class.getName());
	
	public static final String SETTINGS_TYPE = "Dynatrace Environment";
	public static final long MILLIS_1_DAY = 1000L * 60 * 60 * 24 * 1;
	
	public enum PrometheusEnvironmentFields{
		API_URL,
		API_TOKEN,
	}
	
	public enum EntityType {
		HOST,
		PROCESS_GROUP,
		PROCESS_GROUP_INSTANCE,
	}
	
	private String URL_API_V1 = null;
	private String URL_API_V2 = null;
	
	private HashMap<String,String> tokenHeader = null;
	

	/** Cache http requests to reduce number of API calls. */
	private static final Cache<String, CFWHttpResponse> DYNATRACE_CACHE_6MIN = CFW.Caching.addCache("EMP Dynatrace Cache[6min]", 
			CacheBuilder.newBuilder()
				.initialCapacity(10)
				.maximumSize(1000)
				.expireAfterWrite(6, TimeUnit.MINUTES)
		);
	
	private static final Cache<String, CFWHttpResponse> DYNATRACE_CACHE_30MIN = CFW.Caching.addCache("EMP Dynatrace Cache[30min]", 
			CacheBuilder.newBuilder()
				.initialCapacity(10)
				.maximumSize(100)
				.expireAfterWrite(30, TimeUnit.MINUTES)
		);
			
	private CFWField<String> apiUrl = CFWField.newString(FormFieldType.TEXT, PrometheusEnvironmentFields.API_URL)
			.allowHTML(true)
			.setDescription("The URL of the dynatrace api without the version(e.g https://yourinstance.live.dynatrace.com/api).");
	
	private CFWField<String> apiToken = CFWField.newString(FormFieldType.TEXT, PrometheusEnvironmentFields.API_TOKEN)
			.setDescription("The apiToken used to access the prometheus instance.");
	
	
	public DynatraceEnvironment() {
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
			.executeCount();
		
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
	
	public DynatraceEnvironment apiURL(String value) {
		this.apiUrl.setValue(value);
		return this;
	}
		
	public String apiToken() {
		return apiToken.getValue();
	}
	
	public DynatraceEnvironment apiToken(String value) {
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
	
	
	/**
	 * @param maxDays TODO**********************************************************************************
	 * 
	 ************************************************************************************/
	public String getStartTimestampParam(long startTimestamp, long endTimestamp, int maxDays) {
		long duration = endTimestamp - startTimestamp;
		if(duration <= (MILLIS_1_DAY * maxDays) ) {
			return startTimestamp+"";
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "Dynatrace Widget: Time range adjusted to "+maxDays+" days(latest time - 3 days).");
			return (endTimestamp - (MILLIS_1_DAY * maxDays) )+"";
		}
	}

	/************************************************************************************
	 * 
	 ************************************************************************************/
	public CFWHttpResponse doGetCached5Minutes(String url, HashMap<String, String> params, HashMap<String, String> headers) {

		String identifier = getDefaultObject().name()+url+params;

		try {
			return DYNATRACE_CACHE_6MIN.get(identifier, new Callable<CFWHttpResponse>() {

				@Override
				public CFWHttpResponse call() throws Exception {
					return CFW.HTTP.sendGETRequest(url, params, headers);
				}
				
			});
		} catch (ExecutionException e) {
			new CFWLog(logger).severe("Error occured while caching response.", e );
		}
		
		return null;
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public CFWHttpResponse doGetCached30Minutes(String url, HashMap<String, String> params, HashMap<String, String> headers) {

		String identifier = getDefaultObject().name()+url+params;

		try {
			return DYNATRACE_CACHE_30MIN.get(identifier, new Callable<CFWHttpResponse>() {

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
		//curl -H 'Authorization: Api-Token token' \ -X GET "https://xxxxxx.live.dynatrace.com/api/v1/entity/infrastructure/hosts"

		String queryURL = getAPIUrlV1() + "/entity/infrastructure/hosts";
		
		HashMap<String,String> requestParams = new HashMap<>();
		requestParams.put("includeDetails", "false");
		if(startTimestamp != null && endTimestamp != null) {
			requestParams.put("startTimestamp", getStartTimestampParam(startTimestamp,endTimestamp, 3));
			requestParams.put("endTimestamp", endTimestamp+"");
		}
				
		CFWHttpResponse queryResult = doGetCached5Minutes(queryURL, requestParams, this.getTokenHeader());
		
		if(queryResult != null) {			
			return queryResult.getRequestBodyAsJsonArray();
		}
		
		return null;
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public JsonArray getAllMetrics() {
		//curl -H 'Authorization: Api-Token token' \ -X GET "https://xxxxxx.live.dynatrace.com/api/v2/metrics"

		String queryURL = getAPIUrlV2() + "/metrics";
		
		HashMap<String,String> requestParams = new HashMap<>();
		requestParams.put("pageSize", "5000");
		
		CFWHttpResponse queryResult = doGetCached30Minutes(queryURL, requestParams, this.getTokenHeader());
		if(queryResult != null) {			
			JsonObject payload = queryResult.getRequestBodyAsJsonObject();
			
			if(!payload.get("nextPageKey").isJsonNull()) {
				CFW.Context.Request.addAlertMessage(MessageType.WARNING, "Over 5000 metrics available. Only the first 5000 were loaded.");
			}
			
			if(payload.get("metrics") != null) {
				return payload.get("metrics").getAsJsonArray();
			}
		}
		
		return null;
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public JsonArray getFilteredMetrics(String filterString) {
		//curl -H 'Authorization: Api-Token token' \ -X GET "https://xxxxxx.live.dynatrace.com/api/v2/metrics"

		String queryURL = getAPIUrlV2() + "/metrics";
		
		HashMap<String,String> requestParams = new HashMap<>();
		requestParams.put("fields", "+entityType");
		requestParams.put("text", filterString);
		
		CFWHttpResponse queryResult = doGetCached30Minutes(queryURL, requestParams, this.getTokenHeader());
		if(queryResult != null) {			
			JsonObject payload = queryResult.getRequestBodyAsJsonObject();
						
			if(payload.get("metrics") != null) {
				return payload.get("metrics").getAsJsonArray();
			}
		}
		
		return null;
	}
	
	
	
	/************************************************************************************
	 * @param entityType type of the entity (e.g "HOST")
	 * @param entityID the id of the entity (e.g "HOST-123456021FCE23D")
	 * @param startTimestamp of the time frame in UTC milliseconds since 01.01.1970
	 * @param endTimestamp  of the time frame in UTC milliseconds since 01.01.1970
	 * @param metricSelector the dynatrace metrics selector (e.g "builtin:host.cpu.usage,builtin:host.mem.usage")
	 * @return
	 ************************************************************************************/
	public JsonObject queryMetrics(EntityType entityType, String entityID, long startTimestamp, long endTimestamp, String metricSelector) {
		// Host CPU Usage Example
		//curl -H 'Authorization: Api-Token token123' -X GET "https://xxxxxx.live.dynatrace.com/api/v2/metrics/query?metricSelector=builtin:host.cpu.usage&from=1604372880000&to=1604588879160&resolution=h&entitySelector=type(HOST),entityId(HOST-1234528021FCE23D)"
		// Process Instance CPU Usage Example
		//curl -H 'Authorization: Api-Token token123' -X GET "https://xxxxxx.live.dynatrace.com/api/v2/metrics/query?metricSelector=builtin:tech.generic.cpu.usage&from=1604372880000&to=1604588879160&resolution=h&entitySelector=type(PROCESS_GROUP),entityId(PROCESS_GROUP-43892357E55BAA3)"

		String queryURL = getAPIUrlV2() + "/metrics/query";
		
		HashMap<String,String> requestParams = new HashMap<>();
		requestParams.put("metricSelector", metricSelector);
		requestParams.put("from", startTimestamp+"");
		requestParams.put("to", endTimestamp+"");
		requestParams.put("resolution", "120");
		requestParams.put("entitySelector", "type("+entityType+"),entityId("+entityID+")");

		CFWHttpResponse queryResult = doGetCached30Minutes(queryURL, requestParams, this.getTokenHeader());
		if(queryResult != null) {			
			return queryResult.getRequestBodyAsJsonObject();
			
		}
		
		return null;
	}
		
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public JsonObject getHostDetails(String hostID) {
		//curl -H 'Authorization: Api-Token token' \ -X GET "https://xxxxxx.live.dynatrace.com/api/v1/entity/infrastructure/hosts"

		String queryURL = getAPIUrlV1() + "/entity/infrastructure/hosts/"+hostID;
		
		CFWHttpResponse queryResult = doGetCached5Minutes(queryURL, null, this.getTokenHeader());
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
	public JsonArray getLogsForHost(String hostID) {
		//curl -H 'Authorization: Api-Token dt0c01.12345' -X GET "https://xxxx.live.dynatrace.com/api/v1/entity/infrastructure/hosts/HOST-1123456781FCE23D/logs"

		String queryURL = getAPIUrlV1() + "/entity/infrastructure/hosts/"+hostID+"/logs";
		
		CFWHttpResponse queryResult = doGetCached5Minutes(queryURL, null, this.getTokenHeader());
		if(queryResult != null) {
			JsonElement jsonElement = CFW.JSON.fromJson(queryResult.getResponseBody());
			JsonObject json = jsonElement.getAsJsonObject();
			
			if(json.get("error") != null) {
				CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Dynatrace Error: "+json.get("error").getAsString());
				return null;
			}
			
			JsonElement logElement = json.get("logs");
			if(logElement != null && logElement.isJsonArray() ) {
				return logElement.getAsJsonArray();
			}

		}
		return null;
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public JsonArray getLogsForProcessGroup(String processGroupID) {
		//curl -H 'Authorization: Api-Token dt0c01.12345' -X GET "https://xxxx.live.dynatrace.com/api/v1/entity/infrastructure/hosts/HOST-1123456781FCE23D/logs"

		String queryURL = getAPIUrlV1() + "/entity/infrastructure/process-groups/"+processGroupID+"/logs";
		
		CFWHttpResponse queryResult = doGetCached5Minutes(queryURL, null, this.getTokenHeader());
		if(queryResult != null) {
			JsonElement jsonElement = CFW.JSON.fromJson(queryResult.getResponseBody());
			JsonObject json = jsonElement.getAsJsonObject();
			
			if(json.get("error") != null) {
				CFW.Context.Request.addAlertMessage(MessageType.ERROR, "Dynatrace Error: "+json.get("error").getAsString());
				return null;
			}
			
			JsonElement logElement = json.get("logs");
			if(logElement != null && logElement.isJsonArray() ) {
				return logElement.getAsJsonArray();
			}

		}
		return null;
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public JsonArray getLogRecordsForHost(String hostID, String logPath, String query, int maxEntries, long startTimestamp, long endTimestamp) {
		//curl -H 'Authorization: Api-Token dt0c01.123456' -X POST "https://xxx.live.dynatrace.com/api/v1/entity/infrastructure/hosts/HOST-12345FCE23D/logs/Windows%20Application%20Log"

		String postLogAnalysisJobURL = getAPIUrlV1() + "/entity/infrastructure/hosts/"+hostID+"/logs/"+CFW.HTTP.encode(logPath).replace("+", "%20");

		HashMap<String,String> requestParams = new HashMap<>();
		requestParams.put("startTimestamp", startTimestamp+"");
		requestParams.put("endTimestamp", endTimestamp+"");
		requestParams.put("query", query);
		
		CFWHttpResponse queryResult = CFW.HTTP.sendPOSTRequest(postLogAnalysisJobURL, requestParams, this.getTokenHeader());
		if(queryResult != null) {	
			JsonObject payload = queryResult.getRequestBodyAsJsonObject();

			if(payload.get("jobId") != null) {
				String jobId = payload.get("jobId").getAsString();
				
				//----------------------------------
				// Wait before Polling
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					new CFWLog(logger).severe("Thread interrupted.", e);
				}
				
				//----------------------------------
				// Poll Status for max 10 Seconds
				int i = 0;
				while(i < 10) {
					i++;
					String getJobStatusURL = getAPIUrlV1() + "/entity/infrastructure/hosts/"+hostID+"/logs/jobs/"+jobId;
					CFWHttpResponse jobStatusResult = CFW.HTTP.sendGETRequest(getJobStatusURL, null, this.getTokenHeader());

					if(jobStatusResult != null) {			
						JsonObject jobStatusObject = jobStatusResult.getRequestBodyAsJsonObject();

						if(jobStatusObject.get("logAnalysisStatus") != null 
						&& jobStatusObject.get("logAnalysisStatus").getAsString().equals("READY")) {
							
							//----------------------------------
							// Get Results
							String getLogRecordsURL = getAPIUrlV1() + "/entity/infrastructure/hosts/"+hostID+"/logs/jobs/"+jobId+"/records?pageSize="+maxEntries;
							CFWHttpResponse getLogRecordsResult = CFW.HTTP.sendGETRequest(getLogRecordsURL, null, this.getTokenHeader());
							if(getLogRecordsResult != null) {
								
								JsonObject logRecordsObject = getLogRecordsResult.getRequestBodyAsJsonObject();
								if(logRecordsObject.get("records") != null) {
									return logRecordsObject.get("records").getAsJsonArray();
								}
							}
							break;
						}
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						new CFWLog(logger).severe("Thread interrupted.", e);
					}
				}
			}else if(payload.get("error") != null) {
				new CFWLog(logger).severe("Error:"+payload.get("error").getAsJsonObject().get("message").getAsString());
			}
		}
		return null;
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public JsonArray getLogRecordsForProcess(String hostID, String processGroupID, String logPath, String query, int maxEntries, long startTimestamp, long endTimestamp) {
		//curl -H 'Authorization: Api-Token dt0c01.123456' -X POST "https://xxx.live.dynatrace.com/api/v1/entity/infrastructure/process-groups/PROCESS_GROUP-12345FCE23D/logs/Windows%20Application%20Log"

		String postLogAnalysisJobURL = getAPIUrlV1() + "/entity/infrastructure/process-groups/"+processGroupID+"/logs/"+CFW.HTTP.encode(logPath).replace("+", "%20");

		HashMap<String,String> requestParams = new HashMap<>();
		requestParams.put("hostFilter", hostID);
		requestParams.put("startTimestamp", startTimestamp+"");
		requestParams.put("endTimestamp", endTimestamp+"");
		requestParams.put("query", query);
		
		CFWHttpResponse queryResult = CFW.HTTP.sendPOSTRequest(postLogAnalysisJobURL, requestParams, this.getTokenHeader());
		if(queryResult != null) {	
			JsonObject payload = queryResult.getRequestBodyAsJsonObject();

			if(payload.get("jobId") != null) {
				String jobId = payload.get("jobId").getAsString();
				
				//----------------------------------
				// Wait before Polling
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					new CFWLog(logger).severe("Thread interrupted.", e);
				}
				
				//----------------------------------
				// Poll Status for max 10 Seconds
				int i = 0;
				while(i < 10) {
					i++;
					String getJobStatusURL = getAPIUrlV1() + "/entity/infrastructure/process-groups/"+processGroupID+"/logs/jobs/"+jobId;
					CFWHttpResponse jobStatusResult = CFW.HTTP.sendGETRequest(getJobStatusURL, null, this.getTokenHeader());

					if(jobStatusResult != null) {			
						JsonObject jobStatusObject = jobStatusResult.getRequestBodyAsJsonObject();

						if(jobStatusObject.get("logAnalysisStatus") != null 
						&& jobStatusObject.get("logAnalysisStatus").getAsString().equals("READY")) {
							
							//----------------------------------
							// Get Results
							String getLogRecordsURL = getAPIUrlV1() + "/entity/infrastructure/process-groups/"+processGroupID+"/logs/jobs/"+jobId+"/records?pageSize="+maxEntries;
							CFWHttpResponse getLogRecordsResult = CFW.HTTP.sendGETRequest(getLogRecordsURL, null, this.getTokenHeader());
							if(getLogRecordsResult != null) {
								
								JsonObject logRecordsObject = getLogRecordsResult.getRequestBodyAsJsonObject();
								if(logRecordsObject.get("records") != null) {
									return logRecordsObject.get("records").getAsJsonArray();
								}
							}
							break;
						}
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						new CFWLog(logger).severe("Thread interrupted.", e);
					}
				}
			}else if(payload.get("error") != null) {
				new CFWLog(logger).severe("Error:"+payload.get("error").getAsJsonObject().get("message").getAsString());
			}
		}
		return null;
	}
	
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public JsonArray getEventsForEntity(String entityID, long startTimestamp, long endTimestamp) {
		//curl -H 'Authorization: Api-Token ' -X GET "https://xxxxx.live.dynatrace.com/api/v1/events?entityId=HOST-18124265361FCE23D"

		String queryURL = getAPIUrlV1() + "/events";
		
		HashMap<String,String> requestParams = new HashMap<>();
		requestParams.put("entityId", entityID);
		requestParams.put("from", getStartTimestampParam(startTimestamp,endTimestamp, 365*2));
		requestParams.put("to", endTimestamp+"");
		
		CFWHttpResponse queryResult = doGetCached5Minutes(queryURL, requestParams, this.getTokenHeader());
		if(queryResult != null) {			
			JsonObject payload = queryResult.getRequestBodyAsJsonObject();
			
			if(payload.get("events") != null) {
				return payload.get("events").getAsJsonArray();
			}
		}
		return null;
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public JsonArray getHostProcessGroupInstances(String hostID, Long startTimestamp, Long endTimestamp) {
		// curl -H 'Authorization: Api-Token token' \ -X GET "https://lpi31515.live.dynatrace.com/api/v1/entity/infrastructure/processes?startTimestamp=1604372880000&endTimestamp=1604588879160&host=HOST-1812428021FCE23D"

		String queryURL = getAPIUrlV1() + "/entity/infrastructure/processes";
		
		HashMap<String,String> requestParams = new HashMap<>();
		requestParams.put("host", hostID);
		if(startTimestamp != null && endTimestamp != null) {
			requestParams.put("startTimestamp", getStartTimestampParam(startTimestamp,endTimestamp, 3));
			requestParams.put("endTimestamp", endTimestamp+"");
		}
		
		CFWHttpResponse queryResult = doGetCached5Minutes(queryURL, requestParams, this.getTokenHeader());
		
		if(queryResult != null) {			
			return queryResult.getRequestBodyAsJsonArray();
		}
		
		return null;
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public JsonArray getHostProcessGroup(String hostID, Long startTimestamp, Long endTimestamp) {
		// curl -H 'Authorization: Api-Token token' \ -X GET "https://lpi31515.live.dynatrace.com/api/v1/entity/infrastructure/processes?startTimestamp=1604372880000&endTimestamp=1604588879160&host=HOST-1812428021FCE23D"

		String queryURL = getAPIUrlV1() + "/entity/infrastructure/process-groups";
		
		HashMap<String,String> requestParams = new HashMap<>();
		requestParams.put("host", hostID);
		if(startTimestamp != null && endTimestamp != null) {
			requestParams.put("startTimestamp", getStartTimestampParam(startTimestamp,endTimestamp, 3));
			requestParams.put("endTimestamp", endTimestamp+"");
		}
		
		CFWHttpResponse queryResult = doGetCached5Minutes(queryURL, requestParams, this.getTokenHeader());
		
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
		
		DynatraceEnvironment environment = DynatraceEnvironmentManagement.getEnvironment(environmentID);
		
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
						"<b>Tags: </b>"+currentHostObject.get("tags").toString()
						+", <b>OS Type: </b>"+currentHostObject.get("osType").getAsString());
				
				if(suggestions.getItems().size() == limit) {
					break;
				}
			}
			
		}
		
		return new AutocompleteResult(suggestions);
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public static AutocompleteResult autocompleteLog(int environmentID, String searchValue, int limit, EntityType type, String entityID) {
		
		if(searchValue.length() < 3) {
			return null;
		}
		
		DynatraceEnvironment environment = DynatraceEnvironmentManagement.getEnvironment(environmentID);
		
		searchValue = searchValue.toLowerCase();
		
		JsonArray logfilesArray = null;
			
		switch(type) {
			case HOST:						logfilesArray = environment.getLogsForHost(entityID);
											break;
											
			case PROCESS_GROUP: 			logfilesArray = environment.getLogsForProcessGroup(entityID);
											break;
											
			default:
											break;
		
		}
			
		
		
		if(logfilesArray == null) { return new AutocompleteResult();}
		
		AutocompleteList suggestions = new AutocompleteList();
		for(int i = 0; i < logfilesArray.size(); i++) {
		
			JsonObject currentLogObject = logfilesArray.get(i).getAsJsonObject();
			String path = currentLogObject.get("path").getAsString();

			if(path.toLowerCase().contains(searchValue)) {
				
				String availableForAnalysis = "";
				if(currentLogObject.get("availableForAnalysis") != null) {
					availableForAnalysis = ", <b>Available for Analysis: </b>"+currentLogObject.get("availableForAnalysis").getAsString();
				}
				
				suggestions.addItem(path, path, 
						"<b>File Size: </b>"+(currentLogObject.get("size").getAsInt() / 1000)+" KB"
						+availableForAnalysis
						);
				
				if(suggestions.getItems().size() == limit) {
					break;
				}
			}
			
		}
		
		return new AutocompleteResult(suggestions);
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public static AutocompleteResult autocompleteProcessGroupInstance(int environmentID, String searchValue, int limit, String hostID) {
		
		if(searchValue.length() < 3) {
			return null;
		}
		
		DynatraceEnvironment environment = DynatraceEnvironmentManagement.getEnvironment(environmentID);
		
		searchValue = searchValue.toLowerCase();
		
		JsonArray processArray = environment.getHostProcessGroupInstances(hostID,null,null);
		if(processArray == null) { return new AutocompleteResult();}
		
		AutocompleteList suggestions = new AutocompleteList();
		for(int i = 0; i < processArray.size(); i++) {
		
			JsonObject currentProcessObject = processArray.get(i).getAsJsonObject();
			String processInstanceID = currentProcessObject.get("entityId").getAsString();
			String displayName = currentProcessObject.get("displayName").getAsString();
			String discoveredName = currentProcessObject.get("discoveredName").getAsString();

			if(processInstanceID.toLowerCase().contains(searchValue)
			|| displayName.toLowerCase().contains(searchValue)
			|| discoveredName.toLowerCase().contains(searchValue)) {
				
				suggestions.addItem(processInstanceID, displayName, 
						"<b>Executables: </b>"+currentProcessObject.get("metadata").getAsJsonObject().get("executables").toString());
				
				if(suggestions.getItems().size() == limit) {
					break;
				}
			}
			
		}
		
		return new AutocompleteResult(suggestions);
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public static AutocompleteResult autocompleteProcessGroup(int environmentID, String searchValue, int limit, String hostID) {
		
		if(searchValue.length() < 3) {
			return null;
		}
		
		DynatraceEnvironment environment = DynatraceEnvironmentManagement.getEnvironment(environmentID);
		
		searchValue = searchValue.toLowerCase();
		
		JsonArray processArray = environment.getHostProcessGroup(hostID,null,null);
		if(processArray == null) { return new AutocompleteResult();}
		
		AutocompleteList suggestions = new AutocompleteList();
		for(int i = 0; i < processArray.size(); i++) {
		
			JsonObject currentProcessObject = processArray.get(i).getAsJsonObject();
			String processInstanceID = currentProcessObject.get("entityId").getAsString();
			String displayName = currentProcessObject.get("displayName").getAsString();
			String discoveredName = currentProcessObject.get("discoveredName").getAsString();

			if(processInstanceID.toLowerCase().contains(searchValue)
			|| displayName.toLowerCase().contains(searchValue)
			|| discoveredName.toLowerCase().contains(searchValue)) {
				
				suggestions.addItem(processInstanceID, displayName, 
						"<b>Executables: </b>"+currentProcessObject.get("metadata").getAsJsonObject().get("executables").toString());
				
				if(suggestions.getItems().size() == limit) {
					break;
				}
			}
			
		}
		
		return new AutocompleteResult(suggestions);
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public static AutocompleteResult autocompleteMetrics(int environmentID, String searchValue, int limit)  {
		
		if(searchValue.length() < 3) {
			return null;
		}
		
		DynatraceEnvironment environment = DynatraceEnvironmentManagement.getEnvironment(environmentID);
		
		searchValue = searchValue.toLowerCase();
		
		JsonArray metricsArray = environment.getFilteredMetrics(searchValue);
		
		if(metricsArray == null) { return new AutocompleteResult();}
		
		AutocompleteList suggestions = new AutocompleteList();
		for(int i = 0; i < metricsArray.size(); i++) {
		
			JsonObject currentMetricObject = metricsArray.get(i).getAsJsonObject();
			String metricsID = currentMetricObject.get("metricId").getAsString();
			String description = currentMetricObject.get("displayName").getAsString();
			String metricEntityTypes = currentMetricObject.get("entityType").toString();
				
			suggestions.addItem(metricsID, metricsID, 
					"<b>Description: </b>"+description
					+", <b>Unit: </b>"+currentMetricObject.get("unit").getAsString()
					+", <b>Supported Types: </b>"+metricEntityTypes);
			
			if(suggestions.getItems().size() == limit) {
				break;
			}
		
			
		}
		
		return new AutocompleteResult(suggestions);
	}
	

}
