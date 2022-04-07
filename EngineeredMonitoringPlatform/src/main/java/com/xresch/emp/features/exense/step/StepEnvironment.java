package com.xresch.emp.features.exense.step;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
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
			.setDescription("The URL of the step instance(including http/https, domain and port). Used to create links ")
			.setValue("https://yourstepinstance:8080/");
	
	// other fields are taken from the superclass
	
	public StepEnvironment() {
		initializeFields();
	}
	
	private void initializeFields() {
		this.addFields(url, dbHost, dbPort, dbName, dbUser, dbPassword, timezone);
	}
		
	
	@Override
	public boolean isDeletable(int id) {
		
		int count = new DashboardWidget()
			.selectCount()
			.whereLike(DashboardWidgetFields.JSON_SETTINGS, "%\"environment\":"+id+"%")
			.and().like(DashboardWidgetFields.TYPE, FeatureStep.WIDGET_PREFIX+"%")
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
	
	
}
