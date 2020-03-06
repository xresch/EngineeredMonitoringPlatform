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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.caching.FileDefinition;
import com.pengtoolbox.cfw.caching.FileDefinition.HandlingType;
import com.pengtoolbox.cfw.datahandling.CFWAutocompleteHandler;
import com.pengtoolbox.cfw.datahandling.CFWField;
import com.pengtoolbox.cfw.datahandling.CFWField.FormFieldType;
import com.pengtoolbox.cfw.db.DBInterface;
import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.features.dashboard.FeatureDashboard;
import com.pengtoolbox.cfw.features.dashboard.WidgetDefinition;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.response.JSONResponse;
import com.pengtoolbox.cfw.response.bootstrap.AlertMessage.MessageType;

public class SPMMonitorStatusWidget extends WidgetDefinition {

	private static Logger logger = CFWLog.getLogger(SPMMonitorStatusWidget.class.getName());
	@Override
	public String getWidgetType() {return "emp_spmmonitorstatus";}

	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				.addField(CFWField.newTagsSelector("JSON_MONITORS")
						.setLabel("{!cfw_widget_spmmonitorstatus_monitors!}")
						.setDescription("{!cfw_widget_spmmonitorstatus_monitors_desc!}")
						.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
							
							@Override
							public LinkedHashMap<Object, Object> getAutocompleteData(HttpServletRequest request, String searchValue) {
								
								String environment = request.getParameter("environment");
								
								if(searchValue.length() < 3) {
									return null;
								}
								//---------------------------
								// Get DB
								DBInterface db;
								
								if(environment.equals("Prod")) {
									db = SPMDatabase.getProd();
								}else {
									db = SPMDatabase.getPreProd();
								}
								
								if(db == null) {
									CFW.Context.Request.addAlertMessage(MessageType.WARNING, "The choosen environment '"+environment+"' is not configured.");
									return null;
								}
								
								ResultSet result = db.preparedExecuteQuery(
									CFW.Files.readPackageResource(FeatureEMPWidgets.RESOURCE_PACKAGE, "emp_widget_spmmonitorstatus_autocomplete_sql.sql"),
									"%"+searchValue+"%",
									"%"+searchValue+"%");
								
								LinkedHashMap<Object, Object> suggestions = new LinkedHashMap<Object, Object>();
								try {
									if(result != null) {
										for(int i = 0;i < this.getMaxResults() && result.next();i++) {
											int monitorID = result.getInt("MonitorID");
											String monitorName = result.getString("MonitorName");
											String projectName = result.getString("ProjectName");
											suggestions.put(monitorID, projectName +" &gt;&gt; "+ monitorName);	
										}
									}
								
								} catch (SQLException e) {
									new CFWLog(logger)
										.method("fetchData")
										.severe("Error fetching Widget data.", e);
								}finally {
									db.close(result);
								}
								
								return suggestions;
							}
						})			
				)
				
				.addField(CFWField.newString(FormFieldType.SELECT, "environment")
						.setLabel("{!cfw_widget_spmmonitorstatus_environment!}")
						.setDescription("{!cfw_widget_spmmonitorstatus_environment_desc!}")
						.setOptions(new String[]{"Prod", "Pre-Prod"})
						.setValue("Pre-Prod")
				)
				
				.addField(CFWField.newString(FormFieldType.SELECT, "sizefactor")
						.setLabel("{!cfw_widget_spmmonitorstatus_sizefactor!}")
						.setDescription("{!cfw_widget_spmmonitorstatus_sizefactor_desc!}")
						.setOptions(new String[]{"0.5", "1", "1.25", "1.5", "1.75", "2.0", "2.5", "3.0"})
						.setValue("1")
				)
				
				.addField(CFWField.newBoolean(FormFieldType.BOOLEAN, "showlabels")
						.setLabel("{!cfw_widget_spmmonitorstatus_showlabels!}")
						.setDescription("{!cfw_widget_spmmonitorstatus_showlabels_desc!}")
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
		JsonElement monitorsElement = settings.get("JSON_MONITORS");
		if(monitorsElement.isJsonNull()) {
			return;
		}
		
		JsonObject  monitorsObject = monitorsElement.getAsJsonObject();
				
		//---------------------------------
		// Get Environment
		DBInterface db;
		String url = null;
		JsonElement environmentElement = settings.get("environment");
		if(environmentElement.isJsonNull()) {
			return;
		}
		
		if(environmentElement.getAsString().equals("Prod")) {
			db = SPMDatabase.getProd();
		}else {
			db = SPMDatabase.getPreProd();
		}
		
		if(db == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "The chosen environment '"+environmentElement.getAsString()+"' is not configured.");
			return;
		}	
		//---------------------------------
		// Fetch Data
		JsonArray resultArray = new JsonArray();
		for(Entry<String, JsonElement> entry : monitorsObject.entrySet()) {
			
			String monitorID = entry.getKey().trim();
			ResultSet result = db.preparedExecuteQuery(
					CFW.Files.readPackageResource(FeatureEMPWidgets.RESOURCE_PACKAGE, "emp_widget_spmmonitorstatus_sql.sql"),
					entry.getKey().trim());
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
					object.addProperty("MEASURE_NAME", result.getString("MeasureName"));
					object.addProperty("LOCATION_NAME", result.getString("LocationName"));
					object.addProperty("VALUE", result.getInt("Value"));
					
					resultArray.add(object);
				}else {
					
					//--------------------------------
					// Return No Data as -1
					nameResult = db.preparedExecuteQuery(
							CFW.Files.readPackageResource(FeatureEMPWidgets.RESOURCE_PACKAGE, "emp_widget_spmmonitordetails_sql.sql"),
							monitorID);
					
					if(nameResult != null && nameResult.next()) {
						
						JsonObject object = new JsonObject();
						object.addProperty("MONITOR_ID", monitorID);
						object.addProperty("MONITOR_NAME", nameResult.getString("MonitorName"));
						object.addProperty("PROJECT_ID", nameResult.getString("ProjectID"));
						object.addProperty("PROJECT_NAME", nameResult.getString("ProjectName"));
						object.addProperty("IS_MONITOR_ACTIVE", (nameResult.getInt("MonitorIsActive") == 1) ? true : false);
						object.addProperty("IS_PROJECT_ACTIVE", (nameResult.getInt("ProjectIsActive") == 1) ? true : false);
						
						object.addProperty("VALUE", -1);
						resultArray.add(object);
					}else {
						//--------------------------------
						// Return Error as -2
						JsonObject object = new JsonObject();
						object.addProperty("MONITOR_ID", monitorID);
						object.addProperty("MONITOR_NAME", "Not Found");
						object.addProperty("PROJECT_ID", "Unknown");
						object.addProperty("PROJECT_NAME", "Unknown");
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
	
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureEMPWidgets.RESOURCE_PACKAGE, "emp_widget_spmmonitorstatus.js");
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
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureEMPWidgets.RESOURCE_PACKAGE, "lang_en_widget_spmmonitorstatus.properties"));
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
