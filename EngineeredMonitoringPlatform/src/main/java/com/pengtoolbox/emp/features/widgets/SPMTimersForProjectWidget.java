package com.pengtoolbox.emp.features.widgets;

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
import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.caching.FileDefinition;
import com.pengtoolbox.cfw.caching.FileDefinition.HandlingType;
import com.pengtoolbox.cfw.datahandling.CFWAutocompleteHandler;
import com.pengtoolbox.cfw.datahandling.CFWField;
import com.pengtoolbox.cfw.datahandling.CFWField.FormFieldType;
import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.db.DBInterface;
import com.pengtoolbox.cfw.features.dashboard.WidgetDefinition;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.response.JSONResponse;
import com.pengtoolbox.cfw.response.bootstrap.AlertMessage.MessageType;
import com.pengtoolbox.emp.features.environments.SPMEnvironment;
import com.pengtoolbox.emp.features.environments.SPMEnvironmentManagement;

public class SPMTimersForProjectWidget extends WidgetDefinition {

	private static Logger logger = CFWLog.getLogger(SPMTimersForProjectWidget.class.getName());
	@Override
	public String getWidgetType() {return "emp_spmtimersforprojectstatus";}
		

	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				.addField(CFWField.newString(FormFieldType.SELECT, "environment")
						.setLabel("{!cfw_widget_spm_environment!}")
						.setDescription("{!cfw_widget_spm_environment_desc!}")
						.setOptions(CFW.DB.ContextSettings.getSelectOptionsForType(SPMEnvironment.SETTINGS_TYPE))
				)
				
				.addField(CFWField.newTagsSelector("JSON_PROJECTS")
						.setLabel("{!cfw_widget_spm_project!}")
						.setDescription("{!cfw_widget_spm_project_desc!}")
						.addAttribute("maxTags", "1")
						.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
							
							@Override
							public LinkedHashMap<Object, Object> getAutocompleteData(HttpServletRequest request, String searchValue) {
								String environment = request.getParameter("environment");
								
								return SPMEnvironmentManagement.autocompleteProjects(Integer.parseInt(environment), searchValue, this.getMaxResults());
							}
						})			
				)
				
				.addField(CFWField.newTagsSelector("JSON_TIMERNAMES")
						.setLabel("{!cfw_widget_spm_counternames!}")
						.setDescription("{!cfw_widget_spm_counternames_desc!}")	
							.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
							
							@Override
							public LinkedHashMap<Object, Object> getAutocompleteData(HttpServletRequest request, String searchValue) {
								String environment = request.getParameter("environment");
								String projects = request.getParameter("JSON_PROJECTS");
								
								if(!Strings.isNullOrEmpty(projects) && !projects.equals("{}")) {
									LinkedHashMap<String, String> projectsMap = CFW.JSON.fromJsonLinkedHashMap(projects);
									int firstID = Integer.parseInt(projectsMap.keySet().toArray()[0].toString());
									return SPMEnvironmentManagement.autocompleteTimersForProject(Integer.parseInt(environment), firstID, searchValue, this.getMaxResults());
								}else {
									CFW.Context.Request.addAlertMessage(MessageType.INFO, "Please select a project first.");
									return null;
								}
							}
						})
				)
				
				.addField(CFWField.newFloat(FormFieldType.NUMBER, "threshold_excellent")
						.setLabel("{!cfw_widget_thresholdexcellent!}")
						.setDescription("{!cfw_widget_thresholdexcellent_desc!}")
						.setValue(null)
				)
				.addField(CFWField.newFloat(FormFieldType.NUMBER, "threshold_good")
						.setLabel("{!cfw_widget_thresholdgood!}")
						.setDescription("{!cfw_widget_thresholdgood_desc!}")
						.setValue(null)
				)
				.addField(CFWField.newFloat(FormFieldType.NUMBER, "threshold_warning")
						.setLabel("{!cfw_widget_thresholdwarning!}")
						.setDescription("{!cfw_widget_thresholdwarning_desc!}")
						.setValue(null)
				)
				.addField(CFWField.newFloat(FormFieldType.NUMBER, "threshold_emergency")
						.setLabel("{!cfw_widget_thresholdemergency!}")
						.setDescription("{!cfw_widget_thresholdemergency_desc!}")
						.setValue(null)
				)
				.addField(CFWField.newFloat(FormFieldType.NUMBER, "threshold_danger")
						.setLabel("{!cfw_widget_thresholddanger!}")
						.setDescription("{!cfw_widget_thresholddanger_desc!}")
						.setValue(null)
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
		JsonElement projectsElement = settings.get("JSON_PROJECTS");
		if(projectsElement.isJsonNull()) {
			return;
		}
		
		JsonObject projectsObject = projectsElement.getAsJsonObject();
		if(projectsObject.size() == 0) {
			return;
		}
		
		String projectID = projectsObject.keySet().toArray(new String[]{})[0];
		
		//---------------------------------
		// Resolve CounterNames
		JsonElement timersElement = settings.get("JSON_TIMERNAMES");
		if(timersElement.isJsonNull()) {
			return;
		}
		
		JsonObject timersObject = timersElement.getAsJsonObject();
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
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "SPM Counters for Project: The chosen environment seems not configured correctly.");
			return;
		}
		
		//---------------------------------
		// Get Database
		DBInterface db;
		db = environment.getDBInstance();
		
		if(db == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "SPM Counters for Project: The db of the chosen environment seems not configured correctly.");
			return;
		}
				
		//---------------------------------
		// Fetch Data
		JsonArray resultArray = new JsonArray();
		for(Entry<String, JsonElement> entry : timersObject.entrySet()) {
			
			String measureName = entry.getKey().trim();
			ResultSet result = db.preparedExecuteQuerySilent(
					CFW.Files.readPackageResource(FeatureEMPWidgets.RESOURCE_PACKAGE, "emp_widget_spmtimersforproject.sql"),
					projectID,
					measureName);
			
			ResultSet nameResult = null;
			
			try {
				if(result != null && result.next()) {
					do {
						//--------------------------------
						// Return Status
						JsonObject object = new JsonObject();
						object.addProperty("MONITOR_ID", projectID);
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
					} while(result.next());
				}else {
					
					//--------------------------------
					// Return No Data as -1
					nameResult = db.preparedExecuteQuerySilent(
							CFW.Files.readPackageResource(FeatureEMPWidgets.RESOURCE_PACKAGE, "emp_widget_spmprojectdetails.sql"),
							projectID);
					
					if(nameResult != null && nameResult.next()) {
						
						JsonObject object = new JsonObject();
						object.addProperty("PROJECT_ID", nameResult.getString("ProjectID"));
						object.addProperty("PROJECT_NAME", nameResult.getString("ProjectName"));
						object.addProperty("DESCRIPTION", nameResult.getString("Description"));
						object.addProperty("MEASURE_NAME", measureName.split("/")[1]);
						object.addProperty("IS_PROJECT_ACTIVE", (nameResult.getInt("IsActive") == 1) ? true : false);
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
				"A Counter", "Another Counter", "Foobar Counter", 
				"So many Counter", "Uncountable Counter", "Theusinators Favorite Counter",
				"Countastic Counter", "Counticulous!", "DEV", "TEST", "PROD", };
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
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureEMPWidgets.RESOURCE_PACKAGE, "emp_widget_spmtimersforproject.js");
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
