package com.xresch.emp.features.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.dashboard.WidgetDefinition;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.utils.CFWHttp.CFWHttpResponse;
import com.xresch.emp.features.environments.PrometheusEnvironment;
import com.xresch.emp.features.environments.PrometheusEnvironmentManagement;

public class PrometheusRangeChartWidget extends WidgetDefinition {

	private static Logger logger = CFWLog.getLogger(PrometheusRangeChartWidget.class.getName());
	@Override
	public String getWidgetType() {return "emp_prometheus_range_chart";}
		

	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				.addField(CFWField.newString(FormFieldType.SELECT, "environment")
						.setLabel("{!cfw_widget_spm_environment!}")
						.setDescription("{!cfw_widget_spm_environment_desc!}")
						.setOptions(CFW.DB.ContextSettings.getSelectOptionsForType(PrometheusEnvironment.SETTINGS_TYPE))
				)
				.addField(CFWField.newString(FormFieldType.TEXT, "query")
						.setLabel("{!emp_widget_prometheus_range_query!}")
						.setDescription("{!emp_widget_prometheus_range_query_desc!}")
						.setOptions(CFW.DB.ContextSettings.getSelectOptionsForType(PrometheusEnvironment.SETTINGS_TYPE))
				)
			
				.addField(CFWField.newString(FormFieldType.SELECT, "chart_type")
						.setLabel("{!cfw_widget_charttype!}")
						.setDescription("{!cfw_widget_charttype_desc!}")
						.setOptions(new String[]{"Line", "Bar", "Scatter"})
						.setValue("Line")
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
		// Resolve Query
		JsonElement queryElement = settings.get("query");
		if(queryElement.isJsonNull()) {
			return;
		}
		
		String prometheusQuery = queryElement.getAsString();
		
		//---------------------------------
		// Get Environment
		JsonElement environmentElement = settings.get("environment");
		if(environmentElement.isJsonNull()) {
			return;
		}
		
		PrometheusEnvironment environment = PrometheusEnvironmentManagement.getEnvironment(environmentElement.getAsInt());
		if(environment == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "Prometheus Widget: The chosen environment seems not configured correctly.");
			return;
		}
	
		CFWHttpResponse queryResult = environment.query(prometheusQuery);
		if(queryResult != null) {
			response.getContent().append(queryResult.getResponseBody());	
		}
		
	}
	
	public void createSampleData(JSONResponse response) { 

		JsonArray resultArray = new JsonArray();
				
		response.getContent().append(resultArray.toString());
		
	}
	
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureEMPWidgets.RESOURCE_PACKAGE, "emp_widget_prometheus_range_chart.js");
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