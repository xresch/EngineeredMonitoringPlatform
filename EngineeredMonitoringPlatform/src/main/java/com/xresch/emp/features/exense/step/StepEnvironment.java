package com.xresch.emp.features.exense.step;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoIterable;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.dashboard.DashboardWidget;
import com.xresch.cfw.features.dashboard.DashboardWidget.DashboardWidgetFields;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.emp.features.mongodb.MongoDBEnvironment;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class StepEnvironment extends MongoDBEnvironment {
	
	public static final String SETTINGS_TYPE = "Step Environment";
	
	public enum StepEnvironmentFields {
		URL
	}
	
	private CFWField<String> url = CFWField.newString(FormFieldType.TEXT, StepEnvironmentFields.URL)
			.setDescription("The URL of the step instance(including http/https, domain and port). Will be used to create links.")
			.setValue("https://yourstepinstance:8080/");
	
	// other fields are taken from the superclass
	
	public StepEnvironment() {
		super();
		this.addFields(url);
	}		
	
	@Override
	public boolean isDeletable(int id) {
		
		int count = new DashboardWidget()
			.selectCount()
			.whereLike(DashboardWidgetFields.JSON_SETTINGS, "%\"environment\":"+id+"%")
			.and().like(DashboardWidgetFields.TYPE, FeatureExenseStep.WIDGET_PREFIX+"%")
			.getCount();
		
		if(count == 0) {
			return true;
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The Step Environment cannot be deleted as it is still in use by "+count+"  widget(s).");
			return false;
		}

	}
				
	public String url() {
		return url.getValue();
	}
	
	public StepEnvironment url(String value) {
		this.url.setValue(value);
		return this;
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

		result = this.find("projects", findDoc, sortDoc, 0);

		//-----------------------------
		// Iterate results
		AutocompleteList list = new AutocompleteList();
		
		if(result != null) {
			for (Document currentDoc : result) {
				String id = currentDoc.get("_id").toString();
				String name = ((Document)currentDoc.get("attributes")).get("name").toString();
				System.out.println("id:"+id);
				System.out.println("name:"+name);
				list.addItem(id, name);
				
				if(list.size() >= maxResults) {
					break;
				}
			}
		}
    	
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
    	
    	result = this.find("plans", findDoc, sortDoc, 0);
    	
    	//-----------------------------
    	// Iterate results
    	AutocompleteList list = new AutocompleteList();
    	
    	if(result != null) {
    		for (Document currentDoc : result) {
    			String id = currentDoc.get("_id").toString();
    			String name = ((Document)currentDoc.get("attributes")).get("name").toString();
    			System.out.println("id:"+id);
    			System.out.println("name:"+name);
    			list.addItem(id, name);
    			
    			if(list.size() >= maxResults) {
    				break;
    			}
    		}
    	}
    	
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
    	
    	result = this.find("users", findDoc, sortDoc, 0);
    	
    	//-----------------------------
    	// Iterate results
    	AutocompleteList list = new AutocompleteList();
    	
    	if(result != null) {
    		for (Document currentDoc : result) {
    			String id = currentDoc.get("_id").toString();
    			String name = currentDoc.get("username").toString();
    			System.out.println("id:"+id);
    			System.out.println("name:"+name);
    			list.addItem(id, name);
    			
    			if(list.size() >= maxResults) {
    				break;
    			}
    		}
    	}
    	
    	return new AutocompleteResult(list);
    }
    
}
