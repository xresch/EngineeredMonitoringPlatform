package com.pengtoolbox.emp.features.widgets;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.caching.FileDefinition;
import com.pengtoolbox.cfw.caching.FileDefinition.HandlingType;
import com.pengtoolbox.cfw.datahandling.CFWField;
import com.pengtoolbox.cfw.datahandling.CFWField.FormFieldType;
import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.db.DBInterface;
import com.pengtoolbox.cfw.features.dashboard.WidgetDefinition;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.response.JSONResponse;
import com.pengtoolbox.cfw.response.bootstrap.AlertMessage.MessageType;
import com.pengtoolbox.emp.features.environments.EnvironmentAWA;

public class AWAJobStatusWidget extends WidgetDefinition {

	private static Logger logger = CFWLog.getLogger(AWAJobStatusWidget.class.getName());
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
				
				.addField(CFWField.newString(FormFieldType.SELECT, "environment")
						.setLabel("{!cfw_widget_awajobstatus_environment!}")
						.setDescription("{!cfw_widget_awajobstatus_environment_desc!}")
						.setOptions(CFW.DB.ContextSettings.getSelectOptionsForType(EnvironmentAWA.SETTINGS_TYPE))
						.setValue("Pre-Prod")
				)
				
				.addField(CFWField.newString(FormFieldType.SELECT, "renderer")
						.setLabel("{!cfw_widget_spmmonitorstatus_renderer!}")
						.setDescription("{!cfw_widget_spmmonitorstatus_renderer_desc!}")
						.setOptions(new String[]{"Tiles", "Panels", "Table"})
						.setValue("Tiles")
				)
								
				.addField(CFWField.newString(FormFieldType.SELECT, "sizefactor")
						.setLabel("{!cfw_widget_awajobstatus_sizefactor!}")
						.setDescription("{!cfw_widget_awajobstatus_sizefactor_desc!}")
						.setOptions(new String[]{"0.25", "0.5", "0.75", "1", "1.25", "1.5", "1.75", "2.0", "2.5", "3.0"})
						.setValue("1")
				)
				
				.addField(CFWField.newString(FormFieldType.SELECT, "borderstyle")
						.setLabel("{!cfw_widget_spmmonitorstatus_borderstyle!}")
						.setDescription("{!cfw_widget_spmmonitorstatus_borderstyle_desc!}")
						.setOptions(new String[]{"None", "Round", "Superround", "Asymmetric", "Superasymmetric", "Ellipsis"})
						.setValue("None")
				)
				
				.addField(CFWField.newBoolean(FormFieldType.BOOLEAN, "showlabels")
						.setLabel("{!cfw_widget_awajobstatus_showlabels!}")
						.setDescription("{!cfw_widget_awajobstatus_showlabels_desc!}")
						.setValue(true)
				)
				
				.addField(CFWField.newBoolean(FormFieldType.BOOLEAN, "disable")
						.setLabel("{!cfw_widget_awajobstatus_disable!}")
						.setDescription("{!cfw_widget_awajobstatus_disable_desc!}")
						.setValue(false)
				)
				
				.addField(CFWField.newBoolean(FormFieldType.BOOLEAN, "sampledata")
						.setLabel("{!cfw_widget_awajobstatus_sampledata!}")
						.setDescription("{!cfw_widget_awajobstatus_sampledata_desc!}")
						.setValue(false)
				)
	
		;
	}
		
	@Override
	public void fetchData(JSONResponse response, JsonObject settings) { 
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
		String[] jobnames = jobnamesString.trim().split(",");
		
		//---------------------------------
		// Resolve Joblabels
		JsonElement joblabelsElement = settings.get("joblabels");
		String[] joblabels = null;
		if(!joblabelsElement.isJsonNull() && !joblabelsElement.getAsString().isEmpty()) {
			joblabels = joblabelsElement.getAsString().trim().split(",");
		}
		
		//---------------------------------
		// Get DB
		DBInterface db;
		JsonElement environmentElement = settings.get("environment");
		if(environmentElement.isJsonNull()) {
			return;
		}
		
		db = SPMDatabase.getEnvironment(environmentElement.getAsInt());
		
		if(db == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "AWA Job Status: The chosen environment seems not configured correctly.");
			return;
		}
			
		//---------------------------------
		// Fetch Data
		JsonArray resultArray = new JsonArray();
		for(int i = 0; i < jobnames.length; i++) {
			ResultSet result = db.preparedExecuteQuerySilent("SELECT UC4.GET_JOB_STATUS(?) AS WorkflowStatus FROM DUAL", jobnames[i].trim());
			
			try {
				JsonObject object = new JsonObject();
				object.addProperty("jobname", jobnames[i]);
				
				if(result != null && result.next()) {
					object.addProperty("status", result.getString(1));
					
				}else {
					object.addProperty("status", "UNKNOWN");
				}
				
				if(joblabels != null && i < joblabels.length) {
					object.addProperty("label", joblabels[i]);
				}else {
					object.addProperty("label", jobnames[i]);
				}

				resultArray.add(object);
				
			} catch (SQLException e) {
				new CFWLog(logger)
					.method("fetchData")
					.severe("Error fetching Widget data.", e);
			}finally {
				db.close(result);
			}
		}
		
		response.getContent().append(resultArray.toString());
		
	}
	
	public void createSampleData(JSONResponse response) { 
		
		response.getContent().append("["
			+ "{ \"jobname\":\"JP_0003_225\", \"label\":\"JP_0003_225\", \"status\":\"RUNNING\"},"
			+ "{ \"jobname\":\"JP_0002_B\", \"label\":\"Job B\", \"status\":\"RUNNING\"},"
			+ "{ \"jobname\":\"JP_0003_225\", \"label\":\"Crazy Job\", \"status\":\"ISSUE\"},"
			+ "{ \"jobname\":\"JP_0003_V\", \"label\":\"JP_0003_V\", \"status\":\"ENDED OK\"},"
			+ "{ \"jobname\":\"JP_0003_C\", \"label\":\"Some Very Long Label with blanks for breaks\", \"status\":\"ISSUE\"},"
			+ "{ \"jobname\":\"JP_0_A\", \"label\":\"JP__A\", \"status\":\"RUNNING\"},"
			+ "{ \"jobname\":\"JP_0003_225\", \"label\":\"JP_0003_225\", \"status\":\"RUNNING\"},"
			+ "{ \"jobname\":\"JP_0002_B\", \"label\":\"Job B\", \"status\":\"ENDED OK\"},"
			+ "{ \"jobname\":\"JP_0003_225\", \"label\":\"Crazy Job\", \"status\":\"ISSUE\"},"
			+ "{ \"jobname\":\"JP_0003_V\", \"label\":\"JP_0003_V\", \"status\":\"ENDED OK\"},"
			+ "{ \"jobname\":\"JP_0003_Chjkl\", \"label\":\"The Holy C\", \"status\":\"ENDED OK\"},"
			+ "{ \"jobname\":\"JP_0003_A\", \"label\":\"JP_0003_A\", \"status\":\"RUNNING\"},"
			+ "{ \"jobname\":\"JP_0003_225fksdfghuw\", \"label\":\"JP_0003_225fksdfghuw\", \"status\":\"ENDED OK\"},"
			+ "{ \"jobname\":\"JP_0002_B\", \"label\":\"Job B\", \"status\":\"ENDED OK\"},"
			+ "{ \"jobname\":\"JP_0003_225\", \"label\":\"Crazy Job\", \"status\":\"ISSUE\"},"
			+ "{ \"jobname\":\"JP_0003_Vhjklh\", \"label\":\"JP_0003_Vhjklh\", \"status\":\"ENDED OK\"},"
			+ "{ \"jobname\":\"JP_01\", \"label\":\"JP_01\", \"status\":\"ISSUE\"},"
			+ "{ \"jobname\":\"JP_0003_A\", \"label\":\"JP_0003_A\", \"status\":\"RUNNING\"}"
			+"]");

	}
	
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureEMPWidgets.RESOURCE_PACKAGE, "emp_widget_awajobstatus.js");
		array.add(js);
		return array;
	}

	@Override
	public ArrayList<FileDefinition> getCSSFiles() {
		return null;
	}

	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureEMPWidgets.RESOURCE_PACKAGE, "lang_en_widget_awajobstatus.properties"));
		return map;
	}

}
