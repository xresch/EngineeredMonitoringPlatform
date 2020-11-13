package com.xresch.emp.features.spm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
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
import com.xresch.cfw.features.dashboard.WidgetDefinition;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.emp.features.common.FeatureEMPCommon;

public class WidgetMonitorStatusAll extends WidgetDefinition {

	private static Logger logger = CFWLog.getLogger(WidgetMonitorStatusAll.class.getName());
	@Override
	public String getWidgetType() {return "emp_spmmonitorstatus_all";}
		

	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				.addField(CFWField.newString(FormFieldType.SELECT, "environment")
						.setLabel("{!cfw_widget_spm_environment!}")
						.setDescription("{!cfw_widget_spm_environment_desc!}")
						.setOptions(CFW.DB.ContextSettings.getSelectOptionsForType(EnvironmentSPM.SETTINGS_TYPE))
				)
								
				.addField(CFWField.newString(FormFieldType.SELECT, "measure")
						.setLabel("{!cfw_widget_spm_measure!}")
						.setDescription("{!cfw_widget_spm_measure_desc!}")
						.setOptions(new String[]{"Overall Health", "Availability", "Accuracy", "Performance"})
						.setValue("Overall Health")
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
						.setValue("0.5")
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
						.setValue(false)
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
		// Get Environment
		JsonElement environmentElement = settings.get("environment");
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
		JsonElement measureElement = settings.get("measure");
		if(measureElement != null && !measureElement.isJsonNull()) {
			measureName = measureElement.getAsString();
		}
		
		//---------------------------------
		// Fetch Data
		JsonArray resultArray = new JsonArray();

		ResultSet result = db.preparedExecuteQuerySilent(
				CFW.Files.readPackageResource(FeatureSPM.PACKAGE_RESOURCE, "emp_widget_spmmonitorstatus_all.sql"),
				measureName);

		
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
				
		response.getContent().append(resultArray.toString());
	}
	
	public void createSampleData(JSONResponse response) { 

		JsonArray resultArray = new JsonArray();
		
		String[] monitorNames = new String[] {
				"[AAA] Test Monitor", "[BUM] Bang Pew Pew", "[FOO] Foobar", 
				"[PAN] Page Analyzer", "[BMW] Bayrischer Mist Wagen", "[Short] Cut",
				"Just a Monitor", "[Rick] Roll that Astley! ", "ABC",
				"DEV", "TEST", "PROD", };
		for(int count = 0; count < monitorNames.length*50; count++) {
			int index = count % monitorNames.length;
			String name = monitorNames[index];
			//--------------------------------
			// Return Status
			JsonObject object = new JsonObject();
			
			object.addProperty("MONITOR_ID", index);
			object.addProperty("MONITOR_NAME", name);
			object.addProperty("PROJECT_ID", index);
			object.addProperty("PROJECT_NAME", "Pseudo Project");
			object.addProperty("MEASURE_NAME", "Overall Health");
			object.addProperty("LOCATION_NAME", "Winterthur");
			object.addProperty("VALUE", (Math.random() > 0.6) ? 100 : Math.ceil(Math.random()*99));
			object.addProperty("PROJECT_URL", "http://spm.just-an-example.com/silk/DEF/Monitoring/Monitoring?pId="+index);
			resultArray.add(object);
				
		}
		
		response.getContent().append(resultArray.toString());
		
	}
	
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureSPM.PACKAGE_RESOURCE, "emp_widget_spmmonitorstatus_all.js");
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
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureEMPCommon.PACKAGE_RESOURCE, "lang_en_emp_widgets.properties"));
		return map;
	}

}
