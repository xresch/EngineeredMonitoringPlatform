package com.xresch.emp.features.exense.step;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.bson.Document;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoIterable;
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
import com.xresch.cfw.utils.CFWHttp.CFWHttpRequestBuilder;
import com.xresch.cfw.utils.CFWHttp.CFWHttpResponse;

/**************************************************************************************************************
 * 
 * Plan Object Structure Example:
	{
	  "_class": "step.core.plans.Plan",
	  "customFields": {
		"versionId": "65f41c33a68ef02f12cee834"
	  },
	  "attributes": {
		"name": "K6_Check_google.com",
		"project": "65f41ae4a68ef02f12cecfab"
	  },
	  "root": {
		"_class": "TestCase",
		"customFields": null,
		"attributes": {
		  "name": "K6_Check_google.com"
		},
		"dynamicName": {
		  "dynamic": false,
		  "value": "",
		  "expression": "",
		  "expressionType": null
		},
		"useDynamicName": false,
		"description": null,
		"children": [
		  {
			"_class": "CallKeyword",
			"customFields": null,
			"attributes": {
			  "name": "K6_ResponseChecker"
			},
			"dynamicName": {
			  "dynamic": false,
			  "value": "",
			  "expression": "",
			  "expressionType": null
			},
			"useDynamicName": false,
			"description": null,
			"children": [],
			"customAttributes": null,
			"attachments": null,
			"skipNode": {
			  "dynamic": false,
			  "value": false,
			  "expression": null,
			  "expressionType": null
			},
			"instrumentNode": {
			  "dynamic": false,
			  "value": false,
			  "expression": null,
			  "expressionType": null
			},
			"continueParentNodeExecutionOnError": {
			  "dynamic": false,
			  "value": false,
			  "expression": null,
			  "expressionType": null
			},
			"remote": {
			  "dynamic": false,
			  "value": true,
			  "expression": null,
			  "expressionType": null
			},
			"token": {
			  "dynamic": false,
			  "value": "{}",
			  "expression": null,
			  "expressionType": null
			},
			"function": {
			  "dynamic": false,
			  "value": "{\"name\":{\"value\":\"K6_ResponseChecker\",\"dynamic\":false}}",
			  "expression": null,
			  "expressionType": null
			},
			"argument": {
			  "dynamic": false,
			  "value": "{\"url\":{\"dynamic\":false,\"value\":\"http://www.google.com\"},\"httpStatus\":{\"value\":200,\"dynamic\":false}}",
			  "expression": "",
			  "expressionType": null
			},
			"resultMap": {
			  "dynamic": false,
			  "value": null,
			  "expression": null,
			  "expressionType": null
			},
			"id": "65f41be9a68ef02f12cedfb1"
		  }
		],
		"customAttributes": null,
		"attachments": null,
		"skipNode": {
		  "dynamic": false,
		  "value": false,
		  "expression": null,
		  "expressionType": null
		},
		"instrumentNode": {
		  "dynamic": false,
		  "value": false,
		  "expression": null,
		  "expressionType": null
		},
		"continueParentNodeExecutionOnError": {
		  "dynamic": false,
		  "value": false,
		  "expression": null,
		  "expressionType": null
		},
		"id": "65f41bdda68ef02f12cedea3"
	  },
	  "functions": null,
	  "subPlans": null,
	  "visible": true,
	  "id": "65f41bdda68ef02f12cedea4"
	}
 
 *
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 * 
 **************************************************************************************************************/
public class StepEnvironment extends AbstractContextSettings {
	
	public static final String SETTINGS_TYPE = "Exense STEP";
	
	private StepEnvironment THIS_ENV;
	private static Logger logger = CFWLog.getLogger(StepEnvironment.class.getName());
	
	
	public enum StepEnvironmentFields {
		URL
		, API_TOKEN
	}
	
	private CFWField<String> url = CFWField.newString(FormFieldType.TEXT, StepEnvironmentFields.URL)
			.setDescription("The URL of the step instance(including http/https, domain and port). Will be used to access the API and create links.")
			.setValue("https://yourstepinstance:8080")
			;

	protected CFWField<String> apiToken = CFWField.newString(FormFieldType.PASSWORD, StepEnvironmentFields.API_TOKEN)
			.setDescription("The token used to access the STEP API.")
			.disableSanitization()
			.enableEncryption("exense_step_DB_Password_Salt_withoutPepper")
			;
	
	
	// will contain the trimmed url that definitely ends with "/"
	private String finalURL = null;
	
	// SchedulerID and Object
	private Cache<String, StepSchedulerDetails> schedulerCache;

	// PlanID and Object
	private LoadingCache<String, JsonObject> planCache;
	
	// PlanID and Object: Object Example
	//	{
	//		  "customFields": null,
	//		  "attributes": {
	//			"name": "Test_Reto",
	//			"owner": ""
	//		  },
	//		  "global": false,
	//		  "members": null,
	//		  "id": "65f41ae4a68ef02f12cecfab"
	//		}
	private LoadingCache<String, JsonObject> projectCache;
	
	// UserID and Array of Users Projects
	private LoadingCache<String, JsonArray> userProjectCache;
	
	// metric name and metric, will only be loaded once
	private LinkedHashMap<String, StepMetricType> metricTypes = null;
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public StepEnvironment() {
		THIS_ENV = this;
		this.addFields(url, apiToken);
	}	
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	private String createCacheID(String type) {
		return "Step:"+this.getDefaultObject().name()+"["+type+"]";
	}

	/*********************************************************************
	 * 
	 *********************************************************************/
	public void initializeCaches() {
		
		//-----------------------------------
		// Remove all existing Caches
		removeCaches();
		
		//-----------------------------------
		// Schedulers

		schedulerCache = CFW.Caching.addCache( createCacheID("Schedulers"), 
				CacheBuilder.newBuilder()
					.initialCapacity(10)
					.maximumSize(10000)
					//.refreshAfterWrite(1,TimeUnit.HOURS) 
					// Do not expire, as this will also be used for autocomplete  
				);
		
		//-----------------------------------
		// Plans
		planCache = CFW.Caching.addLoadingCache( createCacheID("Plans"), 
				CacheBuilder.newBuilder()
					.initialCapacity(10)
					.maximumSize(10000)
					.refreshAfterWrite(1,TimeUnit.HOURS) 
					, 
					new CacheLoader<String, JsonObject>() {
						@Override
						public JsonObject load(final String planID) throws Exception {
							return fetchPlanByID(planID);
						}
					}
				);
		
		//-----------------------------------
		// Projects
		projectCache = CFW.Caching.addLoadingCache( createCacheID("Projects"), 
				CacheBuilder.newBuilder()
				.initialCapacity(10)
				.maximumSize(10000)
				.refreshAfterWrite(1,TimeUnit.HOURS) 
				,
				new CacheLoader<String, JsonObject>() {
					@Override
					public JsonObject load(final String projectID) throws Exception {
						return fetchProjectByID(projectID);
					}
				}
			);
		
		//-----------------------------------
		// User Project List
		userProjectCache = CFW.Caching.addLoadingCache( createCacheID("UserProjects"), 
				CacheBuilder.newBuilder()
				.initialCapacity(10)
				.maximumSize(10000)
				.refreshAfterWrite(1, TimeUnit.HOURS) 
				,
				new CacheLoader<String, JsonArray>() {
					@Override
					public JsonArray load(final String userID) throws Exception {
						return fetchProjectsForUser(userID);
					}
				}
			);
		
		//-----------------------------------
		// Load Scheduler Cache
		loadSchedulersCache();
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public void removeCaches() {
		
		String id = "["+this.getDefaultObject().name()+"]";
		
		CFW.Caching.removeCache( createCacheID("Schedulers") );
		CFW.Caching.removeCache( createCacheID("Plans") );
		CFW.Caching.removeCache( createCacheID("Projects") );
		CFW.Caching.removeCache( createCacheID("UserProjects") );
		
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public boolean isProperlyDefined() {
		if(url.getValue() != null
		&& apiToken.getValue() != null
		) {
			return true;
		}
		
		return false;
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	@Override
	public boolean isDeletable(int id) {
		
		int count = new DashboardWidget()
			.selectCount()
			.whereLike(DashboardWidgetFields.JSON_SETTINGS, "%\"environment\":"+id+"%")
			.and().like(DashboardWidgetFields.TYPE, FeatureExenseStep.WIDGET_PREFIX+"%")
			.executeCount();
		
		if(count == 0) {
			return true;
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The STEP environment cannot be deleted as it is still in use by "+count+"  widget(s).");
			return false;
		}

	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/		
	public String url() {
		
		if(finalURL == null) {
			finalURL = url.getValue().trim();
			
			if(finalURL != null && !finalURL.endsWith("/")) {
				finalURL += "/";
			}
		}
		
		return finalURL;
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public StepEnvironment url(String value) {
		this.url.setValue(value);
		this.finalURL = null;
		return this;
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public String apiToken() {
		return apiToken.getValue();
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public StepEnvironment apiToken(String value) {
		this.apiToken.setValue(value);
		return this;
	}
	
	
	
	/*********************************************************************
	 * Create request builder with URL and token
	 *********************************************************************/
	private CFWHttpRequestBuilder createAPIRequestBuilder(String apiEndpoint) {
				
		if(apiEndpoint == null) {	apiEndpoint = "/"; }
		if(!apiEndpoint.startsWith("/")) {	apiEndpoint = "/"+apiEndpoint; }
		
		
		return CFW.HTTP.newRequestBuilder(this.url()+"rest"+apiEndpoint)
				.header("Accept", "application/json")
				.header("Authorization", "Bearer "+this.apiToken())
			;
	}
	
	/*********************************************************************
	 * Loads all the schedulers from the STEP API.
	 * The response from the API is an array containing objects like:
		 {
			"customFields": null,
			"attributes": {
				"name": "K6_Check_google.com",
				"project": "65f41ae4a68ef02f12cecfab"
			},
			"name": null,
			"executionsParameters": {
				"customFields": null,
				"attributes": {
					"project": "65f41ae4a68ef02f12cecfab"
				},
				"customParameters": {},
				"userID": null,
				"artefactFilter": null,
				"mode": "RUN",
				"plan": null,
				"repositoryObject": {
					"repositoryID": "local",
					"repositoryParameters": {
						"planid": "65f41bdda68ef02f12cedea4"
					}
				},
				"isolatedExecution": false,
				"exports": null,
				"description": "K6_Check_google.com",
				"id": "65f41c46a68ef02f12ceeb64"
			},
			"assertionPlan": null,
			"cronExpression": "0 0/5 * * * ?",
			"cronExclusions": null,
			"active": true,
			"id": "65f41c46a68ef02f12ceeb63"
		}
	 * 
	 *********************************************************************/
	public void loadSchedulersCache() {
		
		schedulerCache.invalidateAll();
		
		CFWHttpResponse response = this.createAPIRequestBuilder("/scheduler/task")
								.GET()
								.send()
								;
				
		JsonArray responseArray = response.getResponseBodyAsJsonArray();

		for(JsonElement element : responseArray) {
			
			JsonObject schedulerObject = element.getAsJsonObject();
			JsonElement id = schedulerObject.get("id");
			
			if(id != null && !id.isJsonNull()) {
				getSchedulerByID(id.getAsString());
			}
		}
	}
	
	/*********************************************************************
	 * Returns the scheduler details or null
	 *********************************************************************/
	public Collection<StepSchedulerDetails> getSchedulersAll() {
		return this.schedulerCache.asMap().values();
	}
	
	/*********************************************************************
	 * Returns the scheduler details or null
	 *********************************************************************/
	public ArrayList<StepSchedulerDetails> getSchedulersForUser(String userID) {
		ArrayList<StepSchedulerDetails> userSchedulerList = new ArrayList<>();
				
		JsonArray projectList = this.getProjectsForUser(userID);
		
		for(JsonElement projectElement : projectList) {
			if(projectElement.isJsonNull()) { continue; }
			
			JsonElement projectID = projectElement.getAsJsonObject().get("projectId");
			if(projectID != null 
			&& projectID.isJsonPrimitive()) {	 			
				userSchedulerList.addAll( 
					getSchedulersForProject( projectID.getAsString() ) 
				);
			}
		}
		
		return userSchedulerList;
	}
	
	/*********************************************************************
	 * Returns the scheduler details or null
	 *********************************************************************/
	public ArrayList<StepSchedulerDetails> getSchedulersForProject(String projectID) {
		
		ArrayList<StepSchedulerDetails> result = new ArrayList<>();
		
		for(StepSchedulerDetails details : schedulerCache.asMap().values()) {
			if(details.getProjectID().equals(projectID)) {
				result.add(details);
			}
		}
		
		return result;
	}
	

	/*********************************************************************
	 * Returns the scheduler details or null
	 *********************************************************************/
	public StepSchedulerDetails getSchedulerByID(String scheduleID) {
		
		if(scheduleID == null) { return null; }
		
		try {
			return schedulerCache.get(scheduleID, new Callable<StepSchedulerDetails>() {

				@Override
				public StepSchedulerDetails call() throws Exception {

					CFWHttpResponse response = createAPIRequestBuilder("/scheduler/task/"+scheduleID)
							.send()
							;
					
					if(response.getStatus() == 200
					&& response.getResponseBody().startsWith("{")) {
						JsonObject schedulerObject = response.getResponseBodyAsJsonObject();
							
						if(schedulerObject == null) {
							return null;
						}
						
						return new StepSchedulerDetails(THIS_ENV, schedulerObject);
						
					}else {
						throw new Exception("Unexpected response when fetching scheduler: "+response.getResponseBody());
					}
					
				}
				
			});
		} catch (ExecutionException e) {
			new CFWLog(logger).severe("Error occured while caching response: "+e.getMessage(), e );
		}
		
		return null;
		
	}
	
	
	/*********************************************************************
	 * Returns a JsonObject containing the last Status for the given scheduler.
	 * @param executionCount TODO
	 * @return JsonArray with executions, empty array if nothing found
	 *********************************************************************/
	public JsonArray getSchedulerLastNExecutions(String schedulerID, int executionCount, long earliest, long latest) {
		
		JsonArray result = new JsonArray();
		
		//----------------------------
		// Retrieve Scheduler
		if(schedulerID == null) { return null; }
		
		StepSchedulerDetails scheduler = this.getSchedulerByID(schedulerID);
		
		if(scheduler == null) {
			result.add(
					new StepSchedulerDetails().toJson("UNKNOWN SCHEDULER", "UNKNOWN SCHEDULER", null, null)
				);
			return result;
		}
		
		//----------------------------
		// Call API
		CFWHttpResponse response = createAPIRequestBuilder("/table/executions")
				.POST()
				.body("application/json",
					"""
					{
					  "filters": [
						{"collectionFilter":
							{"type":"And","children":[
									{"type":"Equals","field":"executionTaskID","expectedValue": "%s"}
									,{"type":"Gte","field":"startTime","value": %s }
									,{"type":"Lte","field":"endTime","value": %s }
									]
							}
						}
					  ],
					  "skip": 0,
					  "limit": %s,
					  "sort": {
						"field": "endTime",
						"direction": "DESCENDING"
					  }
					}
					""".formatted(schedulerID, earliest, latest, executionCount)
				)
				.send()
				;
		

		//----------------------------
		// Read API Response
		if(response.getStatus() == 200
		&& response.getResponseBody().startsWith("{")) {
			JsonObject responseObject = response.getResponseBodyAsJsonObject();
			// Example structure of relevant fields, if nothing found "data" is an empty array			
			//		{
			//			"recordsTotal": 961,
			//			"recordsFiltered": 916,
			//			"data": [
			//				{
			//					"startTime": 1710507000012,
			//					"endTime": 1710507002047,
			//					"status": "ENDED",
			//					"result": "PASSED",
			//					[...]
			
			if(responseObject == null) { 
				result.add( scheduler.toJson("UNKNOWN (Error Fetching API)", "UNKOWN", null, null) );
				return result; 
			}
			
			JsonElement data = responseObject.get("data");
			if(data != null && data.isJsonArray()) {
				JsonArray dataArray = data.getAsJsonArray();
				
				//-------------------------
				// Empty Array
				if(dataArray.isEmpty()) {
					result.add( scheduler.toJson("NO DATA", "NO DATA", null, null) );
					return result;
				}
				
				//-------------------------
				// Create Response with Last N Executions
				for(JsonElement lastExecutionResults : dataArray) {
					if(lastExecutionResults.isJsonObject()) {
						JsonObject theMightyResults = lastExecutionResults.getAsJsonObject();
						result.add( scheduler.toJson(theMightyResults) );
					}
				}
				
				return result;
			}
			
		}else {
			String message = "STEP: Unexpected response when fetching data from API: Status: "+response.getStatus();
			new CFWLog(logger).severe(message, new Exception(message+", Body: "+response.getResponseBody()));
		}
			
		//----------------------------------
		// Any Other Case
		result.add(scheduler.toJson("UNKNOWN", "UNKOWN", null, null) );
		return result;
		
	}
	
	/*********************************************************************
	 * Returns a JsonArray with the fetched data.
	 * This method will not create logs on errors, it will only create
	 * messages for the end user.
	 * 
	 * @param tableName the name of the STEP table to fetch data from
	 * @param  filterQuery the STEP filter query
	 * @return JsonArray results, never null but might be empty
	 *********************************************************************/
	public JsonArray getDataFromTableAPIEndpoint(String tableName, String filterQuery) {

		JsonArray result = new JsonArray();
		
		//----------------------------
		// Call API
		CFWHttpResponse response = createAPIRequestBuilder("/table/"+tableName)
				.POST()
				.body("application/json", filterQuery)
				.send()
				;

		//----------------------------
		// Read API Response
		if(response.getStatus() == 200
		&& response.getResponseBody().startsWith("{")) {
			JsonObject responseObject = response.getResponseBodyAsJsonObject();
			
			if(responseObject == null) { 
				return result; 
			}
			
			JsonElement data = responseObject.get("data");

			if(data != null && data.isJsonArray()) {
				JsonArray dataArray = data.getAsJsonArray();
								
				return dataArray;
			}
			
		}else {
			String message = "STEP: Unexpected response when fetching data from API: "
							+"Status: "+ response.getStatus()
							+" / Body: "+ response.getResponseBody()
							;
			CFW.Messages.addErrorMessage(message);
		}

		//----------------------------------
		// Any Other Case
		return result;
		
	}
	
	
	/*********************************************************************
	 * Returns all cached plans
	 *********************************************************************/
	public Collection<JsonObject> getPlansAll() {
		return this.planCache.asMap().values();
	}
	
	/*********************************************************************
	 * 
	 * @return the plan for a plan id, or null if not found
	 *********************************************************************/
	public String getPlanName(String planID) {
		
		if(planID == null) { return null; }
		
		JsonObject plan = this.getPlanByID( planID);
		
		if(plan == null) {	return null; }
		
		//---------------------
		// Attributes
		JsonElement attributes = plan.get("attributes");
		if(attributes != null && attributes.isJsonObject()) {
				
			JsonObject attributesObject = attributes.getAsJsonObject();
			
			//---------------------
			// Name
			JsonElement name = attributesObject.get("name");
			if(name != null && name.isJsonPrimitive()) {	
				return name.getAsString();
			}
			
		}
		
		return null;
		
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public JsonObject getPlanByID(String planID) {
		try {
			return planCache.get(planID);
		} catch (ExecutionException e) {
			new CFWLog(logger).severe("STEP: Error occured while loading plan from cache: "+e.getMessage(), e );
		}
		
		return null;
	}
	
	/*********************************************************************
	 * Uncached method
	 *********************************************************************/
	private JsonObject fetchPlanByID(String planID) {
		
		//---------------------------------
		// Handle Null
		if(planID == null) { 
			return createMissingObject(
				planID  
				, "The plan id was null."
				, "null"
			); }
		
		//---------------------------------
		// Call API
		CFWHttpResponse response = createAPIRequestBuilder("/plans/"+planID)
				.send()
				;

		//---------------------------------
		// Handle Plan does not Exist
		if(response.getStatus() == 204) { // No Content
			return createMissingObject(
					planID  
					, "A plan with this ID does not exist."
					, "deleted"
				);
		}
		
		//---------------------------------
		// Handle Regular Response
		if(response.getStatus() < 300
		&& response.getResponseBody().startsWith("{")) {
			return response.getResponseBodyAsJsonObject();
		}else {
			new CFWLog(logger).severe("STEP: Unexpected response while fetching plan: HTTP Status "+response.getStatus());
		}

		return null;
		
	}

	/*********************************************************************
	 * Makes a object for plans or projects.
	 *********************************************************************/
	private JsonObject createMissingObject(String planID, String name, String empfetchstatus) {
		JsonObject object = new JsonObject();
		object.addProperty("id", planID);
		object.addProperty("empfetchstatus", empfetchstatus);
		
		JsonObject attributes = new JsonObject();
		attributes.addProperty("name", name);
		object.add("attributes", attributes);
		
		return object;
	}

	/*********************************************************************
	 * Returns all cached projects
	 *********************************************************************/
	public Collection<JsonObject> getProjectsAll() {
		return this.projectCache.asMap().values();
	}
	
	/*********************************************************************
	 * 
	 * @return the plan for a plan id, or null if not found
	 *********************************************************************/
	public String getProjectName(String projectID) {
		
		if(projectID == null) { return null; }
		
		JsonObject plan = this.getProjectByID(projectID);
		
		if(plan == null) {	return null; }
		
		//---------------------
		// Attributes
		JsonElement attributes = plan.get("attributes");
		if(attributes != null && attributes.isJsonObject()) {
				
			JsonObject attributesObject = attributes.getAsJsonObject();
			
			//---------------------
			// Name
			JsonElement name = attributesObject.get("name");
			if(name != null && name.isJsonPrimitive()) {	
				return name.getAsString();
			}
			
		}
		
		return null;
		
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public JsonObject getProjectByID(String projectID) {
		try {
			return projectCache.get(projectID);
		} catch (ExecutionException e) {
			new CFWLog(logger).severe("STEP: Error occured while loading project from cache: "+e.getMessage(), e );
			e.printStackTrace();
		}
		
		return null;
	}
		
	/*********************************************************************
	 *
	 *********************************************************************/
	private JsonObject fetchProjectByID(String projectID) {
		
		if(projectID == null) { return null; }
		

		CFWHttpResponse response = createAPIRequestBuilder("/tenants/project/"+projectID)
				.send()
				;
		
		if(response.getStatus() == 200
				&& response.getResponseBody().startsWith("{")) {
			return response.getResponseBodyAsJsonObject();
		}else {
			new CFWLog(logger).severe("STEP: Unexpected response while fetching project: "+response.getResponseBody());
		}
		
		return null;
		
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public JsonArray getProjectsForUser(String userID) {
		try {
			return userProjectCache.get(userID);
		} catch (ExecutionException e) {
			new CFWLog(logger).severe("STEP: Error occured while loading projects for user from cache: "+e.getMessage(), e );
			e.printStackTrace();
		}
		
		return null;
	}
		
	/*********************************************************************
	*
	*********************************************************************/
	private JsonArray fetchProjectsForUser(String userID) {
  	
		if(userID == null) { return null; }
	
		CFWHttpResponse response = createAPIRequestBuilder("/tenants/user/"+userID)
		.send()
		;

		if(response.getStatus() == 200
				&& response.getResponseBody().startsWith("[")) {
			return response.getResponseBodyAsJsonArray();
		}else {
			new CFWLog(logger).severe("STEP: Unexpected response while fetching projects for user: "+response.getResponseBody());
		}
		
		return null;
	}
	  
	/*********************************************************************
	 * 
	 *********************************************************************/
	// !!! IMPORTANT !!! Do not make this method public, this API endpoint might return unencrypted passwords (untested)
	private JsonArray fetchAllUsers() {
 			
		CFWHttpResponse response = createAPIRequestBuilder("/admin/users")
				.send()
				;
		
		if(response.getStatus() == 200
		&& response.getResponseBody().startsWith("[")) {
			return response.getResponseBodyAsJsonArray();
		}else {
			new CFWLog(logger).severe("STEP: Unexpected response while fetching full user list: HTTP Status "+response.getStatus());
		}
		
		return new JsonArray();
	}
	
	 /*********************************************************************
	 *
	 *********************************************************************/
	public LinkedHashMap<String, StepMetricType> fetchMetricTypes() {
		
		//------------------------------
		// Load if not already loaded
		if(metricTypes == null) {
			
			CFWHttpResponse response = createAPIRequestBuilder("/time-series/metric-types")
					.send()
					;
			
			if(response.getStatus() == 200
			&& response.getResponseBody().startsWith("[")) {
				
				JsonArray metricsArray = response.getResponseBodyAsJsonArray();
			
				metricTypes = new LinkedHashMap<>();
				for(JsonElement metric : metricsArray) {
					if(metric != null && metric.isJsonObject()) {
						JsonObject metricObject = metric.getAsJsonObject();
						
						String name = metricObject.get("name").getAsString();
						StepMetricType metricType = new StepMetricType(metricObject);
						metricTypes.put(name, metricType);
					}
				}
				
				if(metricTypes.isEmpty()) {
					metricTypes = null; // try reloading next time again
					return new LinkedHashMap<>();
				}
			}else {
				new CFWLog(logger).severe("STEP: Unexpected response while fetching projects for user: "+response.getResponseBody());
			}
		}
	
		return metricTypes;
	}
	

	
	
	 /*********************************************************************
	 * Returns an array of time series.
	 * 
	 * @param schedulerIDs array of Step schedulerIDs
	 * @param metricNames names of metrics to fetch
	 * @param earliest time to fetch
	 * @param latest time to fetch
	 *********************************************************************/
	public JsonArray fetchTimeSeriesMultiple(Set<String> schedulerIDs, Set<String> metricNames, long earliest, long latest) {
		
		JsonArray result = new JsonArray();
		
		//------------------------------
		// Check Inputs
		if(schedulerIDs == null || schedulerIDs.isEmpty()
		|| metricNames == null  || metricNames.isEmpty()) { 
			return  result; 
		}
		
		
		//------------------------------
		// Load if not already loaded
		for(String schedulerID : schedulerIDs) {
			
			for(String metricName : metricNames) {
				JsonArray partialResult = fetchTimeSeries(schedulerID, metricName, earliest, latest);
				result.addAll(partialResult);
			}
		}
		
	
		return result;
	}
	
	 /*********************************************************************
	 * Returns an array of time series.
	 * 
	 * @param schedulerIDs array of Step schedulerIDs
	 * @param metricNames names of metrics to fetch
	 * @param earliest time to fetch
	 * @param latest time to fetch
	 * 
	 * @return JsonArray empty if nothing found
	 *********************************************************************/
	public JsonArray fetchTimeSeries(String schedulerID, String metricName, long earliest, long latest) {
		
		JsonArray result = new JsonArray();
		
		//------------------------------
		// Check Inputs
		if(schedulerID == null || metricName == null  ) { 
			return  result; 
		}
		
		//------------------------------
		// Get Scheduler
		StepSchedulerDetails scheduler = this.getSchedulerByID(schedulerID);
		
		if(scheduler == null) {
			return result;
		}
		
		
		//------------------------------
		// Determine Chart Value
		String chartvalue = "sum";
		StepMetricType metricType = fetchMetricTypes().get(metricName);
		
		if(metricType != null) {
			chartvalue = metricType.getAggregation();
		}
		
		//------------------------------
		// Fetch Data
			
		CFWHttpResponse response = createAPIRequestBuilder("/time-series")
				.body("application/json", 
					""" 
					{
					    "start": %s,
					    "end": %s,
					    "oqlFilter": "(attributes.metricType = \\"%s\\") and (attributes.taskId = '%s')",
					    "groupDimensions": ["name"],
					    "numberOfBuckets": 100,
					    "collectAttributesValuesLimit": 10
					}
					""".formatted(earliest, latest, metricName, schedulerID)
				)
				.send()
				
				;
		
//		{
//			  "start": 1711005180000,
//			  "interval": 180000,
//			  "end": 1711008960000,
//			  "matrixKeys": [
//			    {}
//			  ],
//			  "matrix": [
//			    [
//			      {
//			        "begin": 1711005180000,
//			        "attributes": {},
//			        "count": 9,
//			        "sum": 1761,
//			        "min": 0,
//			        "max": 1579,
//			        "pclValues": {},
//			        "throughputPerHour": 180
//			      },
//			      null,
		
		if(response.getStatus() == 200
		&& response.getResponseBody().startsWith("{")) {
			
			JsonObject timeseries = response.getResponseBodyAsJsonObject();
			
			//---------------------
			// Matrix
			JsonElement matrix = timeseries.get("matrix");
			if(matrix != null && matrix.isJsonArray()) {
				JsonArray matrixKeys = timeseries.get("matrixKeys").getAsJsonArray();
				
				JsonArray matrixArray = matrix.getAsJsonArray();
				int i = 0;
				for(JsonElement datapoints : matrixArray ) {
					JsonObject groupObject = matrixKeys.get(i).getAsJsonObject();
					String groupName = "";
					for(String memberName : groupObject.keySet()) {
						groupName += memberName +"="+ groupObject.get(memberName).getAsString();
					}
					i++;
					//---------------------
					// Datapoints
					if(datapoints != null && datapoints.isJsonArray()) {
						JsonArray datapointsArray = datapoints.getAsJsonArray();
						for(JsonElement datapoint : datapointsArray) {
							if(datapoint != null && datapoint.isJsonObject()) {
								JsonObject datapointObject = datapoint.getAsJsonObject();
								JsonObject resultObject = scheduler.toJson();
								
								int count = datapointObject.get("count").getAsInt();
								int sum = datapointObject.get("sum").getAsInt();
								float avg = (float)sum / count;
								resultObject.addProperty("group", groupName);
								resultObject.addProperty("metric", metricName);
								resultObject.add("time", datapointObject.get("begin"));
								
								resultObject.addProperty("count", count);
								resultObject.add("min", datapointObject.get("min"));
								resultObject.addProperty("avg", avg);
								resultObject.add("max", datapointObject.get("max"));
								resultObject.addProperty("sum", sum);
								resultObject.add("throughput_per_hour", datapointObject.get("throughputPerHour"));
								
								resultObject.add("val", resultObject.get(chartvalue));
								
								result.add(resultObject);
							}
						}
					}
				}
			}

			
		}else {
			new CFWLog(logger).severe("STEP: Unexpected response while fetching time series for scheduler: "+response.getResponseBody());
		}
		
		
		return result;
	}
	

	/*********************************************************************
	 * Create autocomplete for projects.
	 *********************************************************************/
	public AutocompleteResult autocompleteSchedulers(String searchValue, int maxResults) {

		String lowerSearch = searchValue.toLowerCase();
		
		//-----------------------------
		// Iterate results
		AutocompleteList list = new AutocompleteList();
		
		for(StepSchedulerDetails details : schedulerCache.asMap().values()) {
			
			String id = details.getSchedulerID();
			String name = details.getSchedulerName();
			String projectName = details.getProjectName();
			String planName = details.getPlanName();
			
			if(
				(name != null && name.toLowerCase().contains(lowerSearch))
				|| (projectName != null && projectName.toLowerCase().contains(lowerSearch))
				|| (planName != null && planName.toLowerCase().contains(lowerSearch))
			) {
				list.addItem(id, name, "Project: "+projectName+" / Plan: "+planName);
			}
			
			
			if(list.size() >= maxResults) {
				break;
			}
		}
		
		
		AutocompleteResult autocomplete = new AutocompleteResult(list);
		autocomplete.setHTMLDescription(
				"<b>Note: &nbsp;</b> This list is refreshed all 5 minutes, newly created schedulers will popup after a while."
			);
		 
		return autocomplete ;
	}
	
	/*********************************************************************
	 * Create autocomplete for projects.
	 *********************************************************************/
	public AutocompleteResult autocompleteProjects(String searchValue, int maxResults) {
		
		String lowerSearch = searchValue.toLowerCase();
			
		//-----------------------------
		// Iterate results
		AutocompleteList list = new AutocompleteList();
		
		for(JsonObject project : projectCache.asMap().values()) {
			
			//----------------------------
			// Get ProjectID
			JsonElement idElement = project.get("id");
			String projectID;
			if(idElement != null && idElement.isJsonPrimitive()) {
				projectID = idElement.getAsString();
			}else {
				continue;
			}

			//----------------------------
			// Get Project Name
			JsonElement attributesElement = project.get("attributes");
			String projectName = null;
			if(attributesElement != null && attributesElement.isJsonObject()) {
				JsonObject attributesObject = attributesElement.getAsJsonObject();
				JsonElement nameElement = attributesObject.get("name");
				if(nameElement != null && nameElement.isJsonPrimitive()) {
					projectName = nameElement.getAsString();
				}
			}

			//----------------------------
			// Search in Project Name
			if(projectName != null 
			&& projectName.toLowerCase().contains(lowerSearch)
			){
				list.addItem(projectID, projectName);
			}
			
			if(list.size() >= maxResults) {
				break;
			}
		}

		return new AutocompleteResult(list);
	}
	
	/*********************************************************************
	 * Create autocomplete for projects.
	 *********************************************************************/
	public AutocompleteResult autocompleteUsers(String searchValue, int maxResults) {
		
		String lowerSearch = searchValue.toLowerCase();
		
		//-----------------------------
		// Iterate results
		AutocompleteList list = new AutocompleteList();
		
		
		for(JsonElement userElement : fetchAllUsers()) {
			
			//----------------------------
			// Skip anything that is unexpected
			if(userElement == null 
			|| !userElement.isJsonObject()) {
				continue;
			}
			
			//----------------------------
			// Get ID
			JsonObject user = userElement.getAsJsonObject();
			JsonElement idElement = user.get("id");
			String userID;
			if(idElement != null && idElement.isJsonPrimitive()) {
				userID = idElement.getAsString();
			}else {
				continue;
			}
			
			//----------------------------
			// Get Username
			String userName = null;

			JsonElement nameElement = user.get("username");
			if(nameElement != null && nameElement.isJsonPrimitive()) {
				userName = nameElement.getAsString();
			}
			
			
			//----------------------------
			// Search in Project Name
			if(userName != null 
					&& userName.toLowerCase().contains(lowerSearch)
					){
				list.addItem(userID, userName);
			}
			
			if(list.size() >= maxResults) {
				break;
			}
		}
		
		return new AutocompleteResult(list);
	}
	
	/*********************************************************************
	 * Create autocomplete for projects.
	 *********************************************************************/
	public AutocompleteResult autocompleteMetrics(String searchValue, int maxResults) {
		
		AutocompleteList list = new AutocompleteList();
		//list.addItem("response-time", "Response Time");
		
		for(StepMetricType type : this.fetchMetricTypes().values()) {
			list.addItem(type.getName(), type.getLabel(), "Aggregation: "+type.getAggregation());
		}
		
		return new AutocompleteResult(list);
	}
	
	
	/*********************************************************************
	 * Create autocomplete for plans.
	 *********************************************************************/
	public AutocompleteResult autocompletePlans(String searchValue, int maxResults) {
		
		String findDoc = "{'attributes.name': {'$regex': '"+searchValue+"', '$options': 'i'}}";
		String sortDoc = "{'attributes.name': 1}";
		
		//-----------------------------
		// Fetch Data
		MongoIterable<Document> result;
		
		result = null; //TODO this.find("plans", findDoc, sortDoc, 0);
		
		//-----------------------------
		// Iterate results
		AutocompleteList list = new AutocompleteList();
		
//		if(result != null) {
//			for (Document currentDoc : result) {
//				String id = currentDoc.get("_id").toString();
//				String name = ((Document)currentDoc.get("attributes")).get("name").toString();
//				list.addItem(id, name);
//				
//				if(list.size() >= maxResults) {
//					break;
//				}
//			}
//		}
		
		return new AutocompleteResult(list);
	}
	
}
