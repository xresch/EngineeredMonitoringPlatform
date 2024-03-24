package com.xresch.emp.features.exense.step;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;

/*********************************************************************
 * The class that holds details for the scheduler
 *********************************************************************/
public class StepSchedulerDetails {
	
	private String schedulerID;
	private String schedulerName;
	
	private String projectID;
	private String projectName;
	
	private String planID;
	private String planName;
	
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public StepSchedulerDetails() {
	}
	/*********************************************************************
	 * This constructor expects a JsonObject structure like:
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
	public StepSchedulerDetails(StepEnvironment env, JsonObject schedulerObject) {

		JsonElement id = schedulerObject.get("id");
		
		//---------------------
		// Scheduler ID
		if(id != null && id.isJsonPrimitive()) {
			this.setSchedulerID(id.getAsString());
		}
		//---------------------
		// Attributes
		JsonElement attributes = schedulerObject.get("attributes");
		if(attributes != null && attributes.isJsonObject()) {
				
			JsonObject attributesObject = attributes.getAsJsonObject();
			
			//---------------------
			// Scheduler Name
			JsonElement name = attributesObject.get("name");
			if(name != null && name.isJsonPrimitive()) {	
				this.setSchedulerName(name.getAsString());
			}
			
			//---------------------
			// Get ProjectID
			JsonElement project = attributesObject.get("project");
			if(project != null && project.isJsonPrimitive()) {	
				this.setProjectID(project.getAsString());
			}
		}
		
		JsonElement execParams = schedulerObject.get("executionsParameters"); 
		
		if(execParams != null && execParams.isJsonObject()) {
				
    		//---------------------
			// repositoryObject
			JsonObject execParamsObject = execParams.getAsJsonObject();
			JsonElement repository = execParamsObject.get("repositoryObject");
			
			if(repository != null && repository.isJsonObject()) {
				JsonObject repositoryObject = repository.getAsJsonObject();
				
				//---------------------
				// repositoryParameters
				JsonElement repositoryParameters = repositoryObject.get("repositoryParameters");
				
				if(repositoryParameters != null && repositoryParameters.isJsonObject()) {
    				JsonObject repoParamsObject = repositoryParameters.getAsJsonObject();
    				
    				//---------------------
    				// Plan ID
    				JsonElement planid = repoParamsObject.get("planid");

    				if(planid != null && planid.isJsonPrimitive()) {	
    					this.setPlanID(planid.getAsString());
    				}
				}
			}	
		}
		
		//---------------------
		// Plan Name
		this.setPlanName( env.getPlanName(this.planID) );
		
		//---------------------
		// Project Name
		this.setProjectName( env.getProjectName(this.projectID) );
	}

	/*********************************************************************
	 * 
	 *********************************************************************/
	public JsonObject toJson() {
		
		JsonObject object = new JsonObject();
		object.addProperty("projectid", this.projectID);
		object.addProperty("projectname", this.projectName);
		object.addProperty("planid", this.planID);
		object.addProperty("planname",  this.planName);
		object.addProperty("schedulerid", this.schedulerID);
		object.addProperty("schedulername", this.schedulerName);
		return object;
		
		
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public JsonObject toJson(String status, String result, Long starttime, Long endtime) {
		
		JsonObject object = this.toJson();
		long duration = 0l;
		if(starttime != null && endtime != null) {
			duration = endtime - starttime;
		}
		object.addProperty("status", status);
		object.addProperty("result", result);
		object.addProperty("duration",  duration);
		object.addProperty("starttime", starttime);
		object.addProperty("endtime",  endtime);
		
		return object;
		
	}
	
	/*********************************************************************
	 * @param executionData object containing at least the following fields:
		{
			"startTime": 1710507000012,
			"endTime": 1710507002047,
			"status": "ENDED",
			"result": "PASSED",
			[...]
		}
	 *********************************************************************/
	public JsonObject toJson(JsonObject executionData) {
		
		//-----------------------------------
		// Start Time
		JsonElement starttimeElement = executionData.get("startTime");
		Long starttime = null;
		if(starttimeElement != null 
		&& starttimeElement.isJsonPrimitive()) {
			starttime = starttimeElement.getAsLong();
		}
		
		//-----------------------------------
		// End Time
		JsonElement endtimeElement = executionData.get("endTime");
		Long endtime = null;
		if(endtimeElement != null 
		&& endtimeElement.isJsonPrimitive()) {
			endtime = endtimeElement.getAsLong();
		}
		
		//-----------------------------------
		// Duration
		long duration = 0;
		if(starttime != null && endtime != null) {
			duration = endtime - starttime;
		}
		
		//-----------------------------------
		// Create Object
		JsonObject object = this.toJson();
		object.add("status", executionData.get("status"));
		object.add("result", executionData.get("result"));
		object.addProperty("duration",  duration);
		object.addProperty("starttime",  starttime);
		object.addProperty("endtime",  endtime);

		return object;
		
	}
	
	
	// ###########################################################################
	// LOS GETTEROS E SETTEROS
	// ###########################################################################
	protected String getSchedulerID() {
		return schedulerID;
	}
	
	protected StepSchedulerDetails setSchedulerID(String schedulerID) {
		this.schedulerID = schedulerID;
		return this;
	}
	
	protected String getSchedulerName() {
		return schedulerName;
	}
	
	protected StepSchedulerDetails setSchedulerName(String schedulerName) {
		this.schedulerName = schedulerName;
		return this;
	}
	
	protected String getProjectID() {
		return projectID;
	}
	
	protected StepSchedulerDetails setProjectID(String projectID) {
		this.projectID = projectID;
		return this;
	}
	
	protected String getProjectName() {
		return projectName;
	}
	
	protected StepSchedulerDetails setProjectName(String projectName) {
		this.projectName = projectName;
		return this;
	}
	
	protected String getPlanID() {
		return planID;
	}
	
	protected StepSchedulerDetails setPlanID(String planID) {
		this.planID = planID;
		return this;
	}
	
	protected String getPlanName() {
		return planName;
	}
	
	protected StepSchedulerDetails setPlanName(String planName) {
		this.planName = planName;
		return this;
	}
	
	
}