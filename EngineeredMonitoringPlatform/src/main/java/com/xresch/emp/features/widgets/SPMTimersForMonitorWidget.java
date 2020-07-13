package com.xresch.emp.features.widgets;

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
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.emp.features.environments.SPMEnvironment;
import com.xresch.emp.features.environments.SPMEnvironmentManagement;

public class SPMTimersForMonitorWidget extends WidgetDefinition {

	private static Logger logger = CFWLog.getLogger(SPMTimersForMonitorWidget.class.getName());
	@Override
	public String getWidgetType() {return "emp_spmtimersformonitorstatus";}
		

	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				.addField(CFWField.newString(FormFieldType.SELECT, "environment")
						.setLabel("{!cfw_widget_spm_environment!}")
						.setDescription("{!cfw_widget_spm_environment_desc!}")
						.setOptions(CFW.DB.ContextSettings.getSelectOptionsForType(SPMEnvironment.SETTINGS_TYPE))
				)
				
				.addField(CFWField.newTagsSelector("JSON_MONITORS")
						.setLabel("{!cfw_widget_spm_monitor!}")
						.setDescription("{!cfw_widget_spm_monitor_desc!}")
						.addAttribute("maxTags", "1")
						.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
							
							@Override
							public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue) {
								String environment = request.getParameter("environment");
								
								return SPMEnvironmentManagement.autocompleteMonitors(Integer.parseInt(environment), searchValue, this.getMaxResults());
							}
						})			
				)
				
				.addField(CFWField.newTagsSelector("JSON_TIMERNAMES")
						.setLabel("{!cfw_widget_spm_counternames!}")
						.setDescription("{!cfw_widget_spm_counternames_desc!}")	
							.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
							
							@Override
							public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue) {
								String environment = request.getParameter("environment");
								String monitors = request.getParameter("JSON_MONITORS");
								
								if(!Strings.isNullOrEmpty(monitors) && !monitors.equals("{}")) {
									LinkedHashMap<String, String> monitorsMap = CFW.JSON.fromJsonLinkedHashMap(monitors);
									int firstID = Integer.parseInt(monitorsMap.keySet().toArray()[0].toString());
									return SPMEnvironmentManagement.autocompleteTimersForMonitor(Integer.parseInt(environment), firstID, searchValue, this.getMaxResults());
								}else {
									CFW.Context.Request.addAlertMessage(MessageType.INFO, "Please select a monitor first");
									return null;
								}
								
								
							}
						})
				)
				
				.addAllFields(WidgetSettingsFactory.createThresholdFields())
				
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
		// Resolve Monitors
		JsonElement monitorsElement = settings.get("JSON_MONITORS");
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
		JsonElement timerElement = settings.get("JSON_TIMERNAMES");
		if(timerElement.isJsonNull()) {
			return;
		}
		
		JsonObject timersObject = timerElement.getAsJsonObject();
		if(timersObject.size() == 0) {
			return;
		}
		
		//---------------------------------
		// Get Environment
		JsonElement environmentElement = settings.get("environment");
		if(environmentElement.isJsonNull()) {
			return;
		}
		
		SPMEnvironment environment = SPMEnvironmentManagement.getEnvironment(environmentElement.getAsInt());
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
					CFW.Files.readPackageResource(FeatureEMPWidgets.RESOURCE_PACKAGE, "emp_widget_spmtimersformonitor.sql"),
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
							CFW.Files.readPackageResource(FeatureEMPWidgets.RESOURCE_PACKAGE, "emp_widget_spmmonitordetails.sql"),
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
					.method("fetchData")
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
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureEMPWidgets.RESOURCE_PACKAGE, "emp_widget_spmtimersformonitor.js");
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
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureEMPWidgets.RESOURCE_PACKAGE, "lang_en_emp_widgets.properties"));
		return map;
	}

}
