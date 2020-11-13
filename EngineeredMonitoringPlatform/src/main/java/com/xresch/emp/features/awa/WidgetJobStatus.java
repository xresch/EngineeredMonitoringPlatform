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
import com.xresch.emp.features.common.FeatureEMPCommon;

public class WidgetJobStatus extends WidgetDefinition {

	private static Logger logger = CFWLog.getLogger(WidgetJobStatus.class.getName());
	@Override
	public String getWidgetType() {return "emp_awajobstatus";}

	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				.addField(CFWField.newString(FormFieldType.TEXTAREA, "jobnames")
						.setLabel("{!cfw_widget_awajobstatus_jobnames!}")
						.setDescription("{!cfw_widget_awajobstatus_jobnames_desc!}")
						.setValue("")			
				)
				
				.addField(CFWField.newString(FormFieldType.TEXTAREA, "joblabels")
						.setLabel("{!cfw_widget_awajobstatus_joblabels!}")
						.setDescription("{!cfw_widget_awajobstatus_joblabels_desc!}")
						.setValue("")
						
				)
				
				.addField(CFWField.newInteger(FormFieldType.TEXT, "last_run_minutes")
						.setLabel("{!cfw_widget_awajobstatus_last_run_minutes!}")
						.setDescription("{!cfw_widget_awajobstatus_last_run_minutes_desc!}")
						.setValue(0)	
				)
				.addField(CFWField.newString(FormFieldType.SELECT, "environment")
						.setLabel("{!cfw_widget_awajobstatus_environment!}")
						.setDescription("{!cfw_widget_awajobstatus_environment_desc!}")
						.setOptions(CFW.DB.ContextSettings.getSelectOptionsForType(AWAEnvironment.SETTINGS_TYPE))
				)
				
				.addField(CFWField.newString(FormFieldType.SELECT, "renderer")
						.setLabel("{!cfw_widget_displayas!}")
						.setDescription("{!cfw_widget_displayas_desc!}")
						.setOptions(new String[]{"Tiles", "Panels", "Table"})
						.setValue("Tiles")
				)
								
				.addField(CFWField.newString(FormFieldType.SELECT, "sizefactor")
						.setLabel("{!cfw_widget_tilessizefactor!}")
						.setDescription("{!cfw_widget_tilessizefactor_desc!}")
						.setOptions(new String[]{"0.25", "0.5", "0.75", "1", "1.25", "1.5", "1.75", "2.0", "2.5", "3.0"})
						.setValue("1")
				)
				
				.addField(CFWField.newString(FormFieldType.SELECT, "borderstyle")
						.setLabel("{!cfw_widget_tilesborderstyle!}")
						.setDescription("{!cfw_widget_tilesborderstyle_desc!}")
						.setOptions(new String[]{"None", "Round", "Superround", "Asymmetric", "Superasymmetric", "Ellipsis"})
						.setValue("None")
				)
				
				.addField(CFWField.newBoolean(FormFieldType.BOOLEAN, "showlabels")
						.setLabel("{!cfw_widget_tilesshowlabels!}")
						.setDescription("{!cfw_widget_tilesshowlabels_desc!}")
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
		// Resolve Jobnames
		JsonElement jobnamesElement = settings.get("jobnames");
		if(jobnamesElement.isJsonNull() || jobnamesElement.getAsString().isEmpty()) {
			return;
		}
		
		String  jobnamesString = jobnamesElement.getAsString();
		if(jobnamesString.isEmpty()) {
			return;
		}
		String[] jobnames = jobnamesString.trim().split("[,\t\r\n]+");

		//---------------------------------
		// Resolve Joblabels
		JsonElement joblabelsElement = settings.get("joblabels");
		String[] joblabels = null;
		if(!joblabelsElement.isJsonNull() && !joblabelsElement.getAsString().isEmpty()) {
			joblabels = joblabelsElement.getAsString().trim().split("[,\t\r\n]+");
		}

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
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "AWA Job Status: The chosen environment seems not configured correctly.");
			return;
		}
			
		//---------------------------------
		// Fetch Data
		JsonArray resultArray = new JsonArray();
		for(int i = 0; i < jobnames.length; i++) {

			ResultSet result = db.preparedExecuteQuerySilent(
					CFW.Files.readPackageResource(FeatureAWA.PACKAGE_RESOURCE, "emp_awa_last_jobstatus.sql"),
					environment.clientID(),
					jobnames[i].trim() );
			try {
				JsonObject object = new JsonObject();
				object.addProperty("JOBNAME", jobnames[i]);
				
				if(joblabels != null && i < joblabels.length) {
					object.addProperty("LABEL", joblabels[i]);
				}else {
					object.addProperty("LABEL", jobnames[i]);
				}
				
				if(result != null && result.next()){

					OffsetDateTime startTime = result.getObject("START_TIME", OffsetDateTime.class);
					OffsetDateTime endTime = result.getObject("END_TIME", OffsetDateTime.class);

					object.addProperty("STATUS", result.getString("STATUS"));
					object.addProperty("CLIENT_ID", result.getString("CLIENT_ID"));
					object.addProperty("TYPE", result.getString("TYPE"));
					object.addProperty("START_TIME", (startTime != null) ? startTime.toInstant().toEpochMilli() : null );
					object.addProperty("END_TIME", (endTime != null) ? endTime.toInstant().toEpochMilli() : null);
					object.addProperty("HOST_DESTINATION", result.getString("HOST_DESTINATION"));
					object.addProperty("HOST_SOURCE", result.getString("HOST_SOURCE"));
					object.addProperty("DURATION_SECONDS", result.getString("DURATION_SECONDS"));
					object.addProperty("STATUS_CODE", result.getString("STATUS_CODE"));

				}else {
					object.addProperty("status", "UNKNOWN");
				}
				
				resultArray.add(object);
				
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
			+ "{ \"JOBNAME\":\"JP_0003_225\", \"LABEL\":\"JP_0003_225\", \"STATUS\":\"RUNNING\", \"END_TIME\":"+(currentTime-(120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0002_B\", \"LABEL\":\"Job B\", \"STATUS\":\"RUNNING\", \"END_TIME\":"+(currentTime-(1200*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_225\", \"LABEL\":\"Crazy Job\", \"STATUS\":\"ISSUE\", \"END_TIME\":"+(currentTime-(2120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_V\", \"LABEL\":\"JP_0003_V\", \"STATUS\":\"ENDED OK\", \"END_TIME\":"+(currentTime-(3120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_C\", \"LABEL\":\"Some Very Long Label with blanks for breaks\", \"STATUS\":\"ISSUE\", \"END_TIME\":"+(currentTime-(120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0_A\", \"LABEL\":\"JP__A\", \"STATUS\":\"RUNNING\", \"END_TIME\":"+(currentTime-(120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_225\", \"LABEL\":\"JP_0003_225\", \"STATUS\":\"OVERDUE (UNKNOWN)\", \"END_TIME\":"+(currentTime-(4120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0002_B\", \"LABEL\":\"Job B\", \"STATUS\":\"OVERDUE (ENDED OK)\", \"END_TIME\":"+(currentTime-(120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_225\", \"LABEL\":\"Crazy Job\", \"STATUS\":\"ISSUE\", \"END_TIME\":"+(currentTime-(9120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_V\", \"LABEL\":\"JP_0003_V\", \"STATUS\":\"ENDED OK\", \"END_TIME\":"+(currentTime-(34120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_Chjkl\", \"LABEL\":\"The Holy C\", \"STATUS\":\"ENDED OK\", \"END_TIME\":"+(currentTime-(120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_A\", \"LABEL\":\"JP_0003_A\", \"STATUS\":\"RUNNING\", \"END_TIME\":"+(currentTime-(1220*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_225fksdfghuw\", \"LABEL\":\"JP_0003_225fksdfghuw\", \"STATUS\":\"ENDED OK\", \"END_TIME\":"+(currentTime-(120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0002_B\", \"LABEL\":\"Job B\", \"STATUS\":\"ENDED OK\", \"END_TIME\":"+(currentTime-(120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_225\", \"LABEL\":\"Crazy Job\", \"STATUS\":\"ISSUE\", \"END_TIME\":"+(currentTime-(620*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_Vhjklh\", \"LABEL\":\"JP_0003_Vhjklh\", \"STATUS\":\"ENDED OK\", \"END_TIME\":"+(currentTime-(120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_01\", \"LABEL\":\"JP_01\", \"STATUS\":\"ISSUE\", \"END_TIME\":"+(currentTime-(1440*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_A\", \"LABEL\":\"JP_0003_A\", \"STATUS\":\"OVERDUE (ISSUE)\", \"END_TIME\":"+(currentTime-(120*60000))+"}"
			+"]");

	}
	
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureAWA.PACKAGE_RESOURCE, "emp_awa_commonFunctions.js") );
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureAWA.PACKAGE_RESOURCE, "emp_widget_awajobstatus.js") );
		return array;
	}

	@Override
	public ArrayList<FileDefinition> getCSSFiles() {
		return null;
	}

	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureEMPCommon.PACKAGE_RESOURCE, "lang_en_emp_widgets.properties"));
		return map;
	}

}
