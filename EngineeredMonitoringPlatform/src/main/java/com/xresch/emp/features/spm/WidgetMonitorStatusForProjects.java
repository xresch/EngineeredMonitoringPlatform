package com.xresch.emp.features.spm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
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
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.features.dashboard.WidgetDefinition;
import com.xresch.cfw.features.dashboard.WidgetSettingsFactory;
import com.xresch.cfw.features.dashboard.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.emp.features.common.FeatureEMPCommon;

public class WidgetMonitorStatusForProjects extends WidgetDefinition {

	private static Logger logger = CFWLog.getLogger(WidgetMonitorStatusForProjects.class.getName());
	@Override
	public String getWidgetType() {return "emp_spmmonitorstatus_forprojects";}
		
	@Override
	public WidgetDataCachePolicy getCachePolicy() {
		return WidgetDataCachePolicy.ALWAYS;
	}
	
	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				.addField(SPMSettingsFactory.createEnvironmentSelectorField())
				
				.addField(SPMSettingsFactory.createProjectsSelectorField(1024))
				
				.addField(SPMSettingsFactory.createMeasureSelectField())
				
				.addField(WidgetSettingsFactory.createDefaultDisplayAsField())				
				.addAllFields(WidgetSettingsFactory.createTilesSettingsFields())
				.addField(WidgetSettingsFactory.createDisableBoolean())
				.addField(WidgetSettingsFactory.createSampleDataField())
				
	
		;
	}

	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings, long earliest, long latest) { 
		
		//---------------------------------
		// Example Data
		JsonElement sampleDataElement = jsonSettings.get("sampledata");
		
		if(sampleDataElement != null 
		&& !sampleDataElement.isJsonNull() 
		&& sampleDataElement.getAsBoolean()) {
			createSampleData(response);
			return;
		}
		//---------------------------------
		// Resolve Jobnames
		JsonElement projectsElement = jsonSettings.get("JSON_PROJECTS");
		if(projectsElement.isJsonNull()) {
			return;
		}
		
		JsonObject projectsObject = projectsElement.getAsJsonObject();
		if(projectsObject.size() == 0) {
			return;
		}
		
		//---------------------------------
		// Get Environment
		JsonElement environmentElement = jsonSettings.get("environment");
		if(environmentElement.isJsonNull()) {
			return;
		}
		
		EnvironmentSPM environment = EnvironmentManagerSPM.getEnvironment(environmentElement.getAsInt());
		if(environment == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "SPM Monitor Status All: The chosen environment seems not configured correctly.");
			return;
		}
		
		//---------------------------------
		// Get Database
		DBInterface db;
		db = environment.getDBInstance();
		
		if(db == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "SPM Monitor Status All: The db of the chosen environment seems not configured correctly.");
			return;
		}
		
		//---------------------------------
		// Get Measure
		String measureName = "Overall Health";
		JsonElement measureElement = jsonSettings.get("measure");
		if(measureElement != null && !measureElement.isJsonNull()) {
			measureName = measureElement.getAsString();
		}
		
		//---------------------------------
		// Fetch Data
		JsonArray resultArray = new JsonArray();
		for(Entry<String, JsonElement> entry : projectsObject.entrySet()) {
			
			String projectID = entry.getKey().trim();
			
			ResultSet result = db.preparedExecuteQuerySilent(
					CFW.Files.readPackageResource(FeatureSPM.PACKAGE_RESOURCE, "emp_widget_spmmonitorstatus_for_projects.sql"),
					measureName,
					projectID);
						
			try {
				if(result != null) {
					while(result.next()) {
						//--------------------------------
						// Return Status
						JsonObject object = new JsonObject();
						object.addProperty("MONITOR_ID", result.getString("MonitorID"));
						object.addProperty("MONITOR_NAME", result.getString("MonitorName"));
						object.addProperty("PROJECT_ID", result.getString("ProjectID"));
						object.addProperty("PROJECT_NAME", result.getString("ProjectName"));
						object.addProperty("MEASURE_NAME", result.getString("MeasureName"));
						object.addProperty("LOCATION_NAME", result.getString("LocationName"));
						object.addProperty("VALUE", result.getInt("Value"));
						
						String url = environment.url().trim();
						if( !Strings.isNullOrEmpty(url) ) {
							if( !url.startsWith("http")) { url = "http://"+url; }
							if(url.endsWith("/")) { url = url.substring(0, url.length()-1); }
							object.addProperty("PROJECT_URL", url+"/silk/DEF/Monitoring/Monitoring?pId="+result.getString("ProjectID"));
						}
						
						resultArray.add(object);
					}
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

		JsonArray resultArray = new JsonArray();
		
		String[] monitorNames = new String[] {
				"[AAA] Test Monitor", "[BUM] B&auml;ng T&auml;tsch", "[FOO] Foobar", 
				"[PAN] Page Analyzer", "[BMW] Bayrischer Mist Wagen", "[Short] Cut",
				"Just a Monitor", "[Rick] Roll that Astley! ", "ABC",
				"DEV", "TEST", "PROD", };
		for(int i = 0; i < monitorNames.length; i++) {
			String name = monitorNames[i];
			//--------------------------------
			// Return Status
			JsonObject object = new JsonObject();
			object.addProperty("MONITOR_ID", i);
			object.addProperty("MONITOR_NAME", name);
			object.addProperty("PROJECT_ID", i);
			object.addProperty("PROJECT_NAME", "Pseudo Project");
			object.addProperty("MEASURE_NAME", "Overall Health");
			object.addProperty("LOCATION_NAME", "Winterthur");
			object.addProperty("VALUE", (Math.random() > 0.7) ? 100 : Math.ceil(Math.random()*99));
			object.addProperty("PROJECT_URL", "http://spm.just-an-example.com/silk/DEF/Monitoring/Monitoring?pId="+i);
			
			resultArray.add(object);
				
		}
		
		response.getContent().append(resultArray.toString());
		
	}
	
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureSPM.PACKAGE_RESOURCE,  FeatureSPM.FILE_PATH_COMMON_JS) );
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureSPM.PACKAGE_RESOURCE,  "emp_widget_spmmonitorstatus_for_projects.js") );
		return array;
	}

	@Override
	public ArrayList<FileDefinition> getCSSFiles() {
		return null;
	}

	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureSPM.PACKAGE_RESOURCE, "lang_en_emp_spm.properties"));
		return map;
	}
	
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureSPM.PERMISSION_WIDGETS_SPM);
	}

}
