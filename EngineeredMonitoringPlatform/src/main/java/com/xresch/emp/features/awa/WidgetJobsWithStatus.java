package com.xresch.emp.features.awa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.features.dashboard.WidgetDefinition;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.validation.NumberRangeValidator;
import com.xresch.emp.features.common.FeatureEMPCommon;

public class WidgetJobsWithStatus extends WidgetDefinition {

	private static Logger logger = CFWLog.getLogger(WidgetJobsWithStatus.class.getName());
	@Override
	public String getWidgetType() {return "emp_awajobswithstatus";}

	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				
				.addField(CFWField.newString(FormFieldType.SELECT, "environment")
						.setLabel("{!cfw_widget_awajobstatus_environment!}")
						.setDescription("{!cfw_widget_awajobstatus_environment_desc!}")
						.setOptions(CFW.DB.ContextSettings.getSelectOptionsForType(AWAEnvironment.SETTINGS_TYPE))
				)
				
				.addField(CFWField.newString(FormFieldType.SELECT, "status")
						.setLabel("{!cfw_widget_awajobstatus_status!}")
						.setDescription("{!cfw_widget_awajobstatus_status_desc!}")
						.setOptions(new String[]{"ENDED OK", "ISSUE"}) // Only AH supported, missing EH: "RUNNING", "WAITING"
						.setValue("ISSUE")
				)
				
				.addField(CFWField.newString(FormFieldType.TEXTAREA, "jobfilters")
						.setLabel("{!cfw_widget_awajobstatus_jobfilters!}")
						.setDescription("{!cfw_widget_awajobstatus_jobfilters_desc!}")
						.setValue("")			
				)
				
				.addField(CFWField.newInteger(FormFieldType.NUMBER, "hours")
						.setLabel("{!cfw_widget_awajobstatus_hours!}")
						.setDescription("{!cfw_widget_awajobstatus_hours_desc!}")
						.setValue(24)
						.addValidator(new NumberRangeValidator(1, 720))
				)
				
				.addField(CFWField.newString(FormFieldType.SELECT, "renderer")
						.setLabel("{!cfw_widget_displayas!}")
						.setDescription("{!cfw_widget_displayas_desc!}")
						.setOptions(new String[]{"Tiles", "Panels", "Table"})
						.setValue("Tiles")
				)
								
				.addField(CFWField.newString(FormFieldType.SELECT, "sizefactor")
						.setLabel("{!cfw_widget_sizefactor!}")
						.setDescription("{!cfw_widget_sizefactor_desc!}")
						.setOptions(new String[]{"0.25", "0.5", "0.75", "1", "1.25", "1.5", "1.75", "2.0", "2.5", "3.0"})
						.setValue("1")
				)
				
				.addField(CFWField.newString(FormFieldType.SELECT, "borderstyle")
						.setLabel("{!cfw_widget_borderstyle!}")
						.setDescription("{!cfw_widget_borderstyle_desc!}")
						.setOptions(new String[]{"None", "Round", "Superround", "Asymmetric", "Superasymmetric", "Ellipsis"})
						.setValue("None")
				)
				
				.addField(CFWField.newBoolean(FormFieldType.BOOLEAN, "showlabels")
						.setLabel("{!cfw_widget_showlabels!}")
						.setDescription("{!cfw_widget_showlabels_desc!}")
						.setValue(true)
				)
				
				.addField(CFWField.newBoolean(FormFieldType.BOOLEAN, "disable")
						.setLabel("{!cfw_widget_disable!}")
						.setDescription("{!cfw_widget_disable_desc!}")
						.setValue(false)
				)
				
				.addField(CFWField.newBoolean(FormFieldType.BOOLEAN, "sampledata")
						.setLabel("{!cfw_widget_sampledata!}")
						.setDescription("{!cfw_widget_sampledata_desc!}")
						.setValue(false)
				)
		;
	}
		
	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, JsonObject settings) { 
		
		//---------------------------------
		// Example Data
		JsonElement sampleDataElement = settings.get("sampledata");
		
		if(sampleDataElement != null 
		&& !sampleDataElement.isJsonNull() 
		&& sampleDataElement.getAsBoolean()) {
			createSampleData(response);
			return;
		}
		
		//---------------------------------
		// Resolve Hours
		JsonElement statusElement = settings.get("status");
		
		if(statusElement == null ) {
			return;
		}
		
		String status = statusElement.getAsString();
		
		// Return code docs: https://docs.automic.com/documentation/webhelp/english/ALL/components/AE/11/All%20Guides/Content/ucaaiy.htm
		int lowerStatusCode = 0;
		int upperStatusCode = 1700;
		switch(status) {
			case "RUNNING": 		lowerStatusCode = 0; 	 upperStatusCode = 1700; break;
			case "WAITING": 		lowerStatusCode = 1700; upperStatusCode = 1799; break;
			case "ISSUE": 			lowerStatusCode = 1800; upperStatusCode = 1899; break;
			case "ENDED OK": 		lowerStatusCode = 1900; upperStatusCode = 1999; break;
			default: 				lowerStatusCode = -2; 	 upperStatusCode = -1; break;
		}
		
		//---------------------------------
		// Resolve Hours
		JsonElement hoursElement = settings.get("hours");
		
		int hours = 24;
		if(hoursElement != null ) {
			hours = hoursElement.getAsInt();
		}
		//---------------------------------
		// Resolve Jobfilters
		JsonElement jobfiltersElement = settings.get("jobfilters");
		if(jobfiltersElement.isJsonNull() || jobfiltersElement.getAsString().isEmpty()) {
			return;
		}
		
		String  jobfiltersString = jobfiltersElement.getAsString();
		if(jobfiltersString.isEmpty()) {
			return;
		}
		String[] jobfilters = jobfiltersString.trim().split("[,\t\r\n]+");

		//---------------------------------
		// Get Environment & DB
		
		JsonElement environmentElement = settings.get("environment");
		if(environmentElement.isJsonNull()) {
			return;
		}
		AWAEnvironment environment = AWAEnvironmentManagement.getEnvironment(environmentElement.getAsInt());

		//---------------------------------
		// Get DB
		DBInterface db = environment.getDBInstance();
		
		if(db == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "AWA Jobs with Issues: The chosen environment seems not configured correctly.");
			return;
		}
			
		//---------------------------------
		// Fetch Data
		JsonArray resultArray = new JsonArray();
		for(int i = 0; i < jobfilters.length; i++) {

			ResultSet result = db.preparedExecuteQuerySilent(
					CFW.Files.readPackageResource(FeatureAWA.RESOURCE_PACKAGE, "emp_awa_jobswithstatus.sql"),
					environment.clientID(),
					lowerStatusCode,
					upperStatusCode,
					hours,
					jobfilters[i].trim() );
			try {
				
				while(result.next()){
					
					JsonObject object = new JsonObject();

					OffsetDateTime startTime = result.getObject("START_TIME", OffsetDateTime.class);
					OffsetDateTime endTime = result.getObject("END_TIME", OffsetDateTime.class);
					
					object.addProperty("JOBNAME", result.getString("NAME"));
					object.addProperty("STATUS", result.getString("STATUS"));
					object.addProperty("CLIENT_ID", result.getString("CLIENT_ID"));
					object.addProperty("TYPE", result.getString("TYPE"));
					object.addProperty("START_TIME", (startTime != null) ? startTime.toInstant().toEpochMilli() : null );
					object.addProperty("END_TIME", (endTime != null) ? endTime.toInstant().toEpochMilli() : null);
					object.addProperty("HOST_DESTINATION", result.getString("HOST_DESTINATION"));
					object.addProperty("HOST_SOURCE", result.getString("HOST_SOURCE"));
					object.addProperty("DURATION_SECONDS", result.getString("DURATION_SECONDS"));
					object.addProperty("STATUS_CODE", result.getString("STATUS_CODE"));
					
					resultArray.add(object);
				}
				
				
				
			} catch (SQLException e) {
				new CFWLog(logger)
					.severe("Error fetching Widget data.", e);
			}finally {
				db.close(result);
			}
		}
		
		response.getContent().append(resultArray.toString());
		
	}
	
	public void createSampleData(JSONResponse response) { 
		
		long currentTime = new Date().getTime();
		response.getContent().append("["
			+ "{ \"JOBNAME\":\"JP_0003_225\", \"LABEL\":\"JP_0003_225\", \"STATUS\":\"ISSUE\", \"END_TIME\":"+(currentTime-(120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0002_B\", \"LABEL\":\"Job B\", \"STATUS\":\"ISSUE\", \"END_TIME\":"+(currentTime-(1200*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_225\", \"LABEL\":\"Crazy Job\", \"STATUS\":\"ISSUE\", \"END_TIME\":"+(currentTime-(2120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_V\", \"LABEL\":\"JP_0003_V\", \"STATUS\":\"ISSUE\", \"END_TIME\":"+(currentTime-(3120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_C\", \"LABEL\":\"Some Very Long Label with blanks for breaks\", \"STATUS\":\"ISSUE\", \"END_TIME\":"+(currentTime-(120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0_A\", \"LABEL\":\"JP__A\", \"STATUS\":\"ISSUE\", \"END_TIME\":"+(currentTime-(120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_225\", \"LABEL\":\"JP_0003_225\", \"STATUS\":\"ISSUE\", \"END_TIME\":"+(currentTime-(4120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0002_B\", \"LABEL\":\"Job B\", \"STATUS\":\"ISSUE\", \"END_TIME\":"+(currentTime-(120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_225\", \"LABEL\":\"Crazy Job\", \"STATUS\":\"ISSUE\", \"END_TIME\":"+(currentTime-(9120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_V\", \"LABEL\":\"JP_0003_V\", \"STATUS\":\"ISSUE\", \"END_TIME\":"+(currentTime-(34120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_Chjkl\", \"LABEL\":\"The Holy C\", \"STATUS\":\"ISSUE\", \"END_TIME\":"+(currentTime-(120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_A\", \"LABEL\":\"JP_0003_A\", \"STATUS\":\"ISSUE\", \"END_TIME\":"+(currentTime-(1220*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_225fksdfghuw\", \"LABEL\":\"JP_0003_225fksdfghuw\", \"STATUS\":\"ISSUE\", \"END_TIME\":"+(currentTime-(120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0002_B\", \"LABEL\":\"Job B\", \"STATUS\":\"ISSUE\", \"END_TIME\":"+(currentTime-(120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_225\", \"LABEL\":\"Crazy Job\", \"STATUS\":\"ISSUE\", \"END_TIME\":"+(currentTime-(620*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_Vhjklh\", \"LABEL\":\"JP_0003_Vhjklh\", \"STATUS\":\"ISSUE\", \"END_TIME\":"+(currentTime-(120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_01\", \"LABEL\":\"JP_01\", \"STATUS\":\"ISSUE\", \"END_TIME\":"+(currentTime-(1440*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_A\", \"LABEL\":\"JP_0003_A\", \"STATUS\":\"ISSUE\", \"END_TIME\":"+(currentTime-(120*60000))+"}"
			+"]");

	}
		
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureAWA.RESOURCE_PACKAGE, "emp_awa_commonFunctions.js") );
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureAWA.RESOURCE_PACKAGE, "emp_widget_awajobswithstatus.js") );
		return array;
	}

	@Override
	public ArrayList<FileDefinition> getCSSFiles() {
		return null;
	}

	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureEMPCommon.RESOURCE_PACKAGE, "lang_en_emp_widgets.properties"));
		return map;
	}

}