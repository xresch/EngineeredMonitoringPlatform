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
import com.pengtoolbox.cfw.db.DBInterface;
import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.features.dashboard.FeatureDashboard;
import com.pengtoolbox.cfw.features.dashboard.WidgetDefinition;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.response.JSONResponse;

public class AWAJobStatusWidget extends WidgetDefinition {

	private static Logger logger = CFWLog.getLogger(AWAJobStatusWidget.class.getName());
	@Override
	public String getWidgetType() {return "emp_awajobstatus";}

	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				.addField(CFWField.newString(FormFieldType.TEXT, "jobnames")
						.setLabel("{!cfw_widget_awajobstatus_jobnames!}")
						.setDescription("{!cfw_widget_awajobstatus_jobnames_desc!}")
						.setValue("")			
				)
				
				.addField(CFWField.newString(FormFieldType.TEXT, "joblabels")
						.setLabel("{!cfw_widget_awajobstatus_joblabels!}")
						.setDescription("{!cfw_widget_awajobstatus_joblabels_desc!}")
						.setValue("")
						
				)
				
				.addField(CFWField.newString(FormFieldType.SELECT, "environment")
						.setLabel("{!cfw_widget_awajobstatus_environment!}")
						.setDescription("{!cfw_widget_awajobstatus_environment_desc!}")
						.setOptions(new String[]{"Prod", "Pre-Prod"})
						.setValue("Pre-Prod")
				)
				
				.addField(CFWField.newString(FormFieldType.SELECT, "sizefactor")
						.setLabel("{!cfw_widget_awajobstatus_sizefactor!}")
						.setDescription("{!cfw_widget_awajobstatus_sizefactor_desc!}")
						.setOptions(new String[]{"0.5", "1", "1.25", "1.5", "1.75", "2.0", "2.5", "3.0"})
						.setValue("1")
				)
				
				.addField(CFWField.newBoolean(FormFieldType.BOOLEAN, "showlabels")
						.setLabel("{!cfw_widget_awajobstatus_showlabels!}")
						.setDescription("{!cfw_widget_awajobstatus_showlabels_desc!}")
						.setValue(true)
				)
	
		;
	}

	@Override
	public void fetchData(JSONResponse response, JsonObject settings) { 
		//---------------------------------
		// Test and Debug
		// response.getContent().append(getMockupResponse());
		// return;
		
		//---------------------------------
		// Resolve Jobnames
		JsonElement jobnamesElement = settings.get("jobnames");
		if(jobnamesElement.isJsonNull() && !jobnamesElement.getAsString().isEmpty()) {
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
		if(environmentElement.getAsString().equals("Prod")) {
			db = AWAJobStatusDatabase.getProd();
		}else {
			db = AWAJobStatusDatabase.getPreProd();
		}
			
		//---------------------------------
		// Fetch Data
		JsonArray resultArray = new JsonArray();
		for(int i = 0; i < jobnames.length; i++) {
			ResultSet result = db.preparedExecuteQuery("SELECT UC4.GET_JOB_STATUS(?) AS WorkflowStatus FROM DUAL", jobnames[i].trim());
			
			try {
				if(result != null && result.next()) {
					JsonObject object = new JsonObject();
					object.addProperty("jobname", jobnames[i]);
					object.addProperty("status", result.getString(1));
					
					if(joblabels != null && i < joblabels.length) {
						object.addProperty("label", joblabels[i]);
					}else {
						object.addProperty("label", jobnames[i]);
					}

					resultArray.add(object);
				}
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
	
	private String getMockupResponse() {
		return "["
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
		+"]";
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

	@Override
	public boolean hasPermission() {
		
		if(CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARDING)
		|| CFW.Context.Request.hasPermission(FeatureDashboard.PERMISSION_DASHBOARD_ADMIN)) {
			return true;
		}
		
		return false;
	}



}