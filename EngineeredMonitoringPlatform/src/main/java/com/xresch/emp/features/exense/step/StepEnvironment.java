package com.xresch.emp.features.exense.step;

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
	
	// PlanID and Object, Object Example
	//	{
	//		  "customFields": null,
	//		  "attributes": {
	//		    "name": "Test_Reto",
	//		    "owner": ""
	//		  },
	//		  "global": false,
	//		  "members": null,
	//		  "id": "65f41ae4a68ef02f12cecfab"
	//		}
	private LoadingCache<String, JsonObject> projectCache;
	
	
	
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
	public void initializeCaches() {
		
		removeCaches();
		
		String cacheID = "["+this.getDefaultObject().name()+"]";
		schedulerCache = CFW.Caching.addCache("Step Schedulers"+cacheID, 
				CacheBuilder.newBuilder()
					.initialCapacity(10)
					.maximumSize(10000)
					//.refreshAfterWrite(1,TimeUnit.HOURS) 
					// Do not expire, as this will also be used for autocomplete  
				);
		
		planCache = CFW.Caching.addLoadingCache("Step Plans"+cacheID, 
				CacheBuilder.newBuilder()
					.initialCapacity(10)
					.maximumSize(10000)
					.refreshAfterWrite(1,TimeUnit.MINUTES) 
					, 
					new CacheLoader<String, JsonObject>() {
					    @Override
					    public JsonObject load(final String planID) throws Exception {
					    	return fetchPlanByID(planID);
					    }
					}
				);
		
		projectCache = CFW.Caching.addLoadingCache("Step Projects"+cacheID, 
				CacheBuilder.newBuilder()
				.initialCapacity(10)
				.maximumSize(10000)
				.refreshAfterWrite(1,TimeUnit.MINUTES) 
				,
				new CacheLoader<String, JsonObject>() {
				    @Override
				    public JsonObject load(final String projectID) throws Exception {
				    	return fetchProjectByID(projectID);
				    }
				}
			);
		
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public void removeCaches() {
		
		String id = "["+this.getDefaultObject().name()+"]";
		
		CFW.Caching.removeCache("Step Schedulers"+id);
		CFW.Caching.removeCache("Step Plans"+id);
		CFW.Caching.removeCache("Step Projects"+id);
		
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
		return url.getValue();
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public StepEnvironment url(String value) {
		this.url.setValue(value);
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
		
		if(finalURL != null) {
			finalURL = this.url().trim();
			
			if(finalURL != null && !finalURL.endsWith("/")) {
				finalURL += "/";
			}
		}
		
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
     * @return JsonObject or null if nothing found
     *********************************************************************/
    public JsonObject getSchedulerLastExecutionStatus(String schedulerID, long earliest, long latest) {
    	
    	//----------------------------
    	// Retrieve Scheduler
    	if(schedulerID == null) { return null; }
    	
    	StepSchedulerDetails scheduler = this.getSchedulerByID(schedulerID);
    	
    	if(scheduler == null) {
    		return new StepSchedulerDetails().toJson("UNKNOWN SCHEDULER", "UNKNOWN SCHEDULER", null, null);
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
					  "limit": 1,
					  "sort": {
					    "field": "endTime",
					    "direction": "DESCENDING"
					  }
					}
					""".formatted(schedulerID, earliest, latest)
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
			
			if(responseObject == null) { return scheduler.toJson("UNKNOWN (Error Fetching API)", "UNKOWN", null, null); }
			
			JsonElement data = responseObject.get("data");
			if(data != null && data.isJsonArray()) {
				JsonArray dataArray = data.getAsJsonArray();
				
				if(dataArray.isEmpty()) {
					return scheduler.toJson("NO DATA", "NO DATA", null, null);
				}
				
				JsonElement lastExecutionResults = dataArray.get(0);
				if(lastExecutionResults.isJsonObject()) {
					JsonObject theMightyResults = lastExecutionResults.getAsJsonObject();
					
					return scheduler.toJson(theMightyResults);
					
				}
			}
			
			//return new StepSchedulerDetails(THIS_ENV, schedulerObject);
			
		}else {
			String message = "STEP: Unexpected response when fetching data from API: Status: "+response.getStatus();
			new CFWLog(logger).severe(message, new Exception(message+", Body: "+response.getResponseBody()));
		}
    				

    	return scheduler.toJson("UNKNOWN", "UNKOWN", null, null);
    	
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
    	
    	String findDoc = "{'attributes.name': {'$regex': '"+searchValue+"', '$options': 'i'}}";
    	String sortDoc = "{'attributes.name': 1}";
    	
    	System.out.println(findDoc);
    	//-----------------------------
    	// Fetch Data
    	MongoIterable<Document> result;
    	
    	result = null; //TODO this.find("projects", findDoc, sortDoc, 0);
    	
    	//-----------------------------
    	// Iterate results
    	AutocompleteList list = new AutocompleteList();
    	
//		if(result != null) {
//			for (Document currentDoc : result) {
//				String id = currentDoc.get("_id").toString();
//				String name = ((Document)currentDoc.get("attributes")).get("name").toString();
//				System.out.println("id:"+id);
//				System.out.println("name:"+name);
//				list.addItem(id, name);
//				
//				if(list.size() >= maxResults) {
//					break;
//				}
//			}
//		}
    	
    	return new AutocompleteResult(list);
    }
    
    
    /*********************************************************************
     * Create autocomplete for plans.
     *********************************************************************/
    public AutocompleteResult autocompletePlans(String searchValue, int maxResults) {
    	
    	String findDoc = "{'attributes.name': {'$regex': '"+searchValue+"', '$options': 'i'}}";
    	String sortDoc = "{'attributes.name': 1}";
    	
    	System.out.println(findDoc);
    	//-----------------------------
    	// Fetch Data
    	MongoIterable<Document> result;
    	
    	result = null; //TODO this.find("plans", findDoc, sortDoc, 0);
    	
    	//-----------------------------
    	// Iterate results
    	AutocompleteList list = new AutocompleteList();
    	
//    	if(result != null) {
//    		for (Document currentDoc : result) {
//    			String id = currentDoc.get("_id").toString();
//    			String name = ((Document)currentDoc.get("attributes")).get("name").toString();
//    			System.out.println("id:"+id);
//    			System.out.println("name:"+name);
//    			list.addItem(id, name);
//    			
//    			if(list.size() >= maxResults) {
//    				break;
//    			}
//    		}
//    	}
    	
    	return new AutocompleteResult(list);
    }
    
    /*********************************************************************
     * Create autocomplete for plans.
     *********************************************************************/
    public AutocompleteResult autocompleteUsers(String searchValue, int maxResults) {
    	
    	String findDoc = "{'username': {'$regex': '"+searchValue+"', '$options': 'i'}}";
    	String sortDoc = "{'username': 1}";
    	
    	System.out.println(findDoc);
    	//-----------------------------
    	// Fetch Data
    	MongoIterable<Document> result;
    	
    	result = null; //TODO this.find("users", findDoc, sortDoc, 0);
    	
    	//-----------------------------
    	// Iterate results
    	AutocompleteList list = new AutocompleteList();
    	
//    	if(result != null) {
//    		for (Document currentDoc : result) {
//    			String id = currentDoc.get("_id").toString();
//    			String name = currentDoc.get("username").toString();
//    			System.out.println("id:"+id);
//    			System.out.println("name:"+name);
//    			list.addItem(id, name);
//    			
//    			if(list.size() >= maxResults) {
//    				break;
//    			}
//    		}
//    	}
    	
    	return new AutocompleteResult(list);
    }
}
