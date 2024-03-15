package com.xresch.emp.features.exense.step;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.bson.Document;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
 * @author Reto Scheiwiller, (c) Copyright 2024
 * @license MIT-License
 * 
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
			.setValue("https://yourstepinstance:8080");

	protected CFWField<String> apiToken = CFWField.newString(FormFieldType.PASSWORD, StepEnvironmentFields.API_TOKEN)
			.setDescription("The token used to access the STEP API.")
			.disableSanitization()
			.enableEncryption("exense_step_DB_Password_Salt_withoutPepper")
			;
	
	// SchedulerID and Object
	private static final Cache<String, StepSchedulerDetails> schedulerCache = CFW.Caching.addCache("Step Schedulers", 
			CacheBuilder.newBuilder()
				.initialCapacity(10)
				.maximumSize(10000)
				.refreshAfterWrite(1,TimeUnit.HOURS) 
				// Do not expire, as this will also be used for autocomplete  
		);

	// PlanID and Object
	private static final Cache<String, JsonObject> planCache = CFW.Caching.addCache("Step Plans", 
			CacheBuilder.newBuilder()
				.initialCapacity(10)
				.maximumSize(10000)
				.refreshAfterWrite(1,TimeUnit.HOURS) 
				// Do not expire, as this will also be used for autocomplete 
		);
	
	
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
	private static final Cache<String, JsonObject> projectCache = CFW.Caching.addCache("Step Projects", 
			CacheBuilder.newBuilder()
			.initialCapacity(10)
			.maximumSize(10000)
			.refreshAfterWrite(1,TimeUnit.HOURS) 
			// Do not expire, as this will also be used for autocomplete  
			);
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
		
		if(apiEndpoint == null) {	apiEndpoint = "/"; }
		if(!apiEndpoint.startsWith("/")) {	apiEndpoint = "/"+apiEndpoint; }
		
		
		return CFW.HTTP.newRequestBuilder(this.url()+"/rest"+apiEndpoint)
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
    	
    	JsonArray responseArray = this.createAPIRequestBuilder("/scheduler/task")
    							.send()
    							.getResponseBodyAsJsonArray();
    	
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
     * Returns the plan name for a plan id
     *********************************************************************/
    public JsonObject getPlanByID(String planID) {
    	
    	if(planID == null) { return null; }
    	
    	try {
    		return planCache.get(planID, new Callable<JsonObject>() {
    			
    			@Override
    			public JsonObject call() throws Exception {
    				
    				CFWHttpResponse response = createAPIRequestBuilder("/plans/"+planID)
    						.send()
    						;
    				
    				if(response.getStatus() == 200
    						&& response.getResponseBody().startsWith("{")) {
    					return response.getResponseBodyAsJsonObject();
    				}else {
    					throw new Exception("Unexpected response when fetching plan: "+response.getResponseBody());
    				}
    				
    			}
    			
    		});
    	} catch (ExecutionException e) {
    		new CFWLog(logger).severe("Error occured while caching response: "+e.getMessage(), e );
    	}
    	
    	return null;
    	
    }

	/*********************************************************************
	 * 
	 * @return the plan for a plan id, or null if not found
	 *********************************************************************/
    public String getProjectName(String projectID) {
    	
    	if(projectID == null) { return null; }
    	
    	JsonObject plan = this.getProjectByID( projectID);
    	
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
     * Returns the plan name for a plan id.
     *********************************************************************/
    public JsonObject getProjectByID(String projectID) {
    	
    	if(projectID == null) { return null; }
    	
    	try {
    		return projectCache.get(projectID, new Callable<JsonObject>() {
    			
    			@Override
    			public JsonObject call() throws Exception {
    				
    				CFWHttpResponse response = createAPIRequestBuilder("/tenants/project/"+projectID)
    						.send()
    						;
    				
    				if(response.getStatus() == 200
    						&& response.getResponseBody().startsWith("{")) {
    					return response.getResponseBodyAsJsonObject();
    				}else {
    					throw new Exception("STEP: Unexpected response when fetching project: "+response.getResponseBody());
    				}
    				
    			}
    			
    		});
    	} catch (ExecutionException e) {
    		new CFWLog(logger).severe("Error occured while caching response: "+e.getMessage(), e );
    	}
    	
    	return null;
    	
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
