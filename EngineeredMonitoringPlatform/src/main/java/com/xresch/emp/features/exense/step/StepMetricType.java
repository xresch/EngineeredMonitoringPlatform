package com.xresch.emp.features.exense.step;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;

/*********************************************************************
 * The class that holds details for the scheduler
 *********************************************************************/
public class StepMetricType {
	
	private String name;
	private String label;
	
	private String aggregation;

	
	/*********************************************************************
	 * This constructor expects a JsonObject structure like:
	    {
		    "customFields": null,
		    "name": "executions/failure-percentage",
		    "displayName": "Execution failure percentage",
		    "description": null,
		    "attributes": [
		      {
		        "name": "taskId",
		        "displayName": "Task",
		        "type": "TEXT",
		        "metadata": {
		          "entity": "task"
		        }
		      },
		      {
		        "name": "eId",
		        "displayName": "Execution",
		        "type": "TEXT",
		        "metadata": {
		          "entity": "execution"
		        }
		      },
		      {
		        "name": "planId",
		        "displayName": "Plan",
		        "type": "TEXT",
		        "metadata": {
		          "entity": "plan"
		        }
		      }
		    ],
		    "unit": "%",
		    "defaultAggregation": "AVG",
		    "defaultGroupingAttributes": [],
		    "renderingSettings": {
		      "seriesColors": null
		    },
		    "id": "65549ac662cdec3786cd5432"
		},
	 * 
	 *********************************************************************/
	public StepMetricType(JsonObject metricObject) {

		this.setName(		metricObject.get("name").getAsString() );
		this.setLabel(		metricObject.get("displayName").getAsString() );
		this.setAggregation(metricObject.get("defaultAggregation").getAsString() );
		
		if(aggregation != null) {
			aggregation = aggregation.toLowerCase();
		}

	}


	
	
	// ###########################################################################
	// LOS GETTEROS E SETTEROS
	// ###########################################################################
	protected String getName() {
		return name;
	}
	
	protected StepMetricType setName(String value) {
		this.name = value;
		return this;
	}
	
	protected String getLabel() {
		return label;
	}
	
	protected StepMetricType setLabel(String value) {
		this.label = value;
		return this;
	}
	
	protected String getAggregation() {
		return aggregation;
	}
	
	protected StepMetricType setAggregation(String value) {
		this.aggregation = value;
		return this;
	}
	
	
	
}