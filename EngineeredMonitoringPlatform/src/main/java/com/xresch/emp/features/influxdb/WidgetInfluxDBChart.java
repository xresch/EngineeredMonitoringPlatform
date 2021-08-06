package com.xresch.emp.features.influxdb;

import java.util.ArrayList;
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
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.features.dashboard.WidgetDefinition;
import com.xresch.cfw.features.dashboard.WidgetSettingsFactory;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.CSVResponse;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.PlaintextResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

public class WidgetInfluxDBChart extends WidgetDefinition {

	private static Logger logger = CFWLog.getLogger(WidgetInfluxDBChart.class.getName());
	@Override
	public String getWidgetType() {return "emp_influxdb_chart";}
		

	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				
				.addField(InfluxDBSettingsFactory.createEnvironmentSelectorField())
				
				.addField((CFWField)CFWField.newString(FormFieldType.TEXTAREA, "query")
						.setLabel("{!emp_widget_influxdb_query!}")
						.setDescription("{!emp_widget_influxdb_query_desc!}")
						.setOptions(CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(InfluxDBEnvironment.SETTINGS_TYPE))
						.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
							@Override
							public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue) {
								return InfluxDBEnvironment.autocompleteQuery(searchValue, this.getMaxResults());
							}
						})
						.addCssClass("textarea-nowrap")
						
				)
			
				.addField(CFWField.newString(FormFieldType.TEXT, "labels")
						.setLabel("{!emp_widget_influxdb_labels!}")
						.setDescription("{!emp_widget_influxdb_labels_desc!}")
				)
				.addAllFields(WidgetSettingsFactory.createDefaultChartFields())
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
		// Resolve Query
		JsonElement queryElement = jsonSettings.get("query");
		if(queryElement.isJsonNull()) {
			return;
		}
		
		String influxdbQuerys = queryElement.getAsString();
		
		//---------------------------------
		// Get Environment
		JsonElement environmentElement = jsonSettings.get("environment");
		if(environmentElement.isJsonNull()) {
			return;
		}
		
		InfluxDBEnvironment environment = InfluxDBEnvironmentManagement.getEnvironment(environmentElement.getAsInt());
		if(environment == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "InfluxDB Widget: The chosen environment seems not configured correctly.");
			return;
		}
		
		//---------------------------------
		// Timeframe
		long earliest = jsonSettings.get("timeframe_earliest").getAsLong();
		long latest = jsonSettings.get("timeframe_latest").getAsLong();
		
		//---------------------------------
		// Fetch Data
		JsonArray resultArray = new JsonArray();
		String[] queryArray = influxdbQuerys.trim().split("\r\n|\n");
		for(int i = 0; i < queryArray.length; i++) {
			JsonObject queryResult = environment.queryRange(queryArray[i], earliest, latest);
			
			if(queryResult != null) {
				resultArray.add(queryResult);
			}
		}
		
		response.getContent().append(resultArray.toString());	
	}
	
	public void createSampleData(JSONResponse response) { 

		response.setPayLoad(CFW.Files.readPackageResource(FeatureInfluxDB.PACKAGE_RESOURCE, "emp_widget_influxdb_chart_sample.csv") );
		
	}
	
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		//array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureInfluxDB.PACKAGE_RESOURCE, "emp_influxdb_commonFunctions.js") );
		array.add(  new FileDefinition(HandlingType.JAR_RESOURCE, FeatureInfluxDB.PACKAGE_RESOURCE, "emp_widget_influxdb_chart.js") );
		return array;
	}

	@Override
	public ArrayList<FileDefinition> getCSSFiles() {
		return null;
	}

	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureInfluxDB.PACKAGE_RESOURCE, "lang_en_emp_influxdb.properties"));
		return map;
	}
	
	@Override
	public boolean hasPermission() {
		return CFW.Context.Request.hasPermission(FeatureInfluxDB.PERMISSION_WIDGETS_INFLUXDB);
	}

}
