package com.xresch.emp.features.spm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.emp.features.common.FeatureEMPCommon;

public class WidgetTimersForMonitor extends WidgetDefinition {

	private static Logger logger = CFWLog.getLogger(WidgetTimersForMonitor.class.getName());
	@Override
	public String getWidgetType() {return "emp_spmtimersformonitorstatus";}
		

	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				.addField(SPMSettingsFactory.createEnvironmentSelectorField())
				
				.addField(SPMSettingsFactory.createMonitorSelectorField())
				
				.addField(CFWField.newTagsSelector("JSON_TIMERNAMES")
						.setLabel("{!emp_widget_spm_timernames!}")
						.setDescription("{!emp_widget_spm_timernames_desc!}")	
							.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
							
							@Override
							public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue) {
								String environment = request.getParameter("environment");
								String monitors = request.getParameter("JSON_MONITORS");
								
								if(!Strings.isNullOrEmpty(monitors) && !monitors.equals("{}")) {
									LinkedHashMap<String, String> monitorsMap = CFW.JSON.fromJsonLinkedHashMap(monitors);
									int firstID = Integer.parseInt(monitorsMap.keySet().toArray()[0].toString());
									return EnvironmentManagerSPM.autocompleteTimersForMonitor(Integer.parseInt(environment), firstID, searchValue, this.getMaxResults());
								}else {
									CFW.Context.Request.addAlertMessage(MessageType.INFO, "Please select a monitor first");
									return null;
								}
								
								
							}
						})
				)
				
				.addAllFields(WidgetSettingsFactory.createThresholdFields())
				
				.addField(WidgetSettingsFactory.createDefaultDisplayAsField())				
				.addAllFields(WidgetSettingsFactory.createTilesSettingsFields())
				.addField(WidgetSettingsFactory.createDisableBoolean())
				.addField(WidgetSettingsFactory.createSampleDataField())
				
	
		;
	}

	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings) { 
		
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
		// Resolve Monitors
		JsonElement monitorsElement = jsonSettings.get("JSON_MONITORS");
		if(monitorsElement.isJsonNull()) {
			return;
		}
		
		JsonObject monitorsObject = monitorsElement.getAsJsonObject();
		if(monitorsObject.size() == 0) {
			return;
		}
		
		String monitorID = monitorsObject.keySet().toArray(new String[]{})[0];
		
		//---------------------------------
		// Resolve CounterNames
		JsonElement timerElement = jsonSettings.get("JSON_TIMERNAMES");
		if(timerElement.isJsonNull()) {
			return;
		}
		
		JsonObject timersObject = timerElement.getAsJsonObject();
		if(timersObject.size() == 0) {
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
		// Fetch Data
		JsonArray resultArray = new JsonArray();
		for(Entry<String, JsonElement> entry : timersObject.entrySet()) {
			
			String measureName = entry.getKey().trim();
			ResultSet result = db.preparedExecuteQuerySilent(
					CFW.Files.readPackageResource(FeatureSPM.PACKAGE_RESOURCE, "emp_widget_spmtimersformonitor.sql"),
					monitorID,
					measureName);
			ResultSet nameResult = null;
			
			try {
				if(result != null && result.next()) {
					//--------------------------------
					// Return Status
					JsonObject object = new JsonObject();
					object.addProperty("MONITOR_ID", monitorID);
					object.addProperty("MONITOR_NAME", result.getString("MonitorName"));
					object.addProperty("PROJECT_ID", result.getString("ProjectID"));
					object.addProperty("PROJECT_NAME", result.getString("ProjectName"));
					object.addProperty("MEASURE_NAME", result.getString("MeasureName").split("/")[1]);
					object.addProperty("LOCATION_NAME", result.getString("LocationName"));
					object.addProperty("VALUE", result.getFloat("Value"));
					
					String url = environment.url().trim();
					if( !Strings.isNullOrEmpty(url) ) {
						if( !url.startsWith("http")) { url = "http://"+url; }
						if(url.endsWith("/")) { url = url.substring(0, url.length()-1); }
						object.addProperty("PROJECT_URL", url+"/silk/DEF/Monitoring/Monitoring?pId="+result.getString("ProjectID"));
					}
					
					resultArray.add(object);
				}else {
					
					//--------------------------------
					// Return No Data as -1
					nameResult = db.preparedExecuteQuerySilent(
							CFW.Files.readPackageResource(FeatureSPM.PACKAGE_RESOURCE, "emp_widget_spmmonitordetails.sql"),
							monitorID);
					
					if(nameResult != null && nameResult.next()) {
						
						JsonObject object = new JsonObject();
						object.addProperty("MONITOR_ID", monitorID);
						object.addProperty("MONITOR_NAME", nameResult.getString("MonitorName"));
						object.addProperty("PROJECT_ID", nameResult.getString("ProjectID"));
						object.addProperty("PROJECT_NAME", nameResult.getString("ProjectName"));
						object.addProperty("MEASURE_NAME", measureName.split("/")[1]);
						object.addProperty("IS_MONITOR_ACTIVE", (nameResult.getInt("MonitorIsActive") == 1) ? true : false);
						object.addProperty("IS_PROJECT_ACTIVE", (nameResult.getInt("ProjectIsActive") == 1) ? true : false);
						object.addProperty("VALUE", -1);
						
						String url = environment.url().trim();
						if( !Strings.isNullOrEmpty(url) ) {
							if( !url.startsWith("http")) { url = "http://"+url; }
							if(url.endsWith("/")) { url = url.substring(0, url.length()-1); }
							object.addProperty("PROJECT_URL", url+"/silk/DEF/Monitoring/Monitoring?pId="+nameResult.getString("ProjectID"));
						}
						
						resultArray.add(object);
					}else {
						//--------------------------------
						// Return Error as -2
						JsonObject object = new JsonObject();
						object.addProperty("MONITOR_ID", monitorID);
						object.addProperty("MONITOR_NAME", "Not Found");
						object.addProperty("PROJECT_ID", "Unknown");
						object.addProperty("PROJECT_NAME", "Unknown");
						object.addProperty("MEASURE_NAME", measureName.split("/")[1]);
						object.addProperty("VALUE", -2);
						resultArray.add(object);
					}
				}
			} catch (SQLException e) {
				new CFWLog(logger)
					.severe("Error fetching Widget data.", e);
			}finally {
				db.close(result);
				db.close(nameResult);
			}
		}
		
		response.getContent().append(resultArray.toString());
		
	}
	
	public void createSampleData(JSONResponse response) { 

		JsonArray resultArray = new JsonArray();
		
		String[] monitorNames = new String[] {
				"A Timer", "Another Timer", "Foobar Timer", 
				"So many Timer", "Untimeable Timer", "Theusinators Favorite Timer",
				"Countastic Timer", "Timeticulous!", "DEV", "TEST", "PROD", };
		for(int i = 0; i < monitorNames.length; i++) {
			String name = monitorNames[i];
			//--------------------------------
			// Return Status
			JsonObject object = new JsonObject();
			object.addProperty("MONITOR_ID", i);
			object.addProperty("MONITOR_NAME", "Pseudo Monitor");
			object.addProperty("PROJECT_ID", i);
			object.addProperty("PROJECT_NAME", "Pseudo Project");
			object.addProperty("MEASURE_NAME", name);
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
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureSPM.PACKAGE_RESOURCE, "emp_widget_spmtimersformonitor.js");
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
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureSPM.PACKAGE_RESOURCE, "lang_en_emp_spm.properties"));
		return map;
	}
	
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureSPM.PERMISSION_WIDGETS_SPM);
	}

}
