package com.xresch.emp.features.prometheus;

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
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.emp.features.common.FeatureEMPCommon;

public class WidgetRangeChart extends WidgetDefinition {

	private static Logger logger = CFWLog.getLogger(WidgetRangeChart.class.getName());
	@Override
	public String getWidgetType() {return "emp_prometheus_range_chart";}
		

	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				.addField(CFWField.newString(FormFieldType.SELECT, "environment")
						.setLabel("{!cfw_widget_spm_environment!}")
						.setDescription("{!cfw_widget_spm_environment_desc!}")
						.setOptions(CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(PrometheusEnvironment.SETTINGS_TYPE))
				)
				.addField((CFWField)CFWField.newString(FormFieldType.TEXTAREA, "query")
						.setLabel("{!emp_widget_prometheus_range_query!}")
						.setDescription("{!emp_widget_prometheus_range_query_desc!}")
						.setOptions(CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(PrometheusEnvironment.SETTINGS_TYPE))
						.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
							@Override
							public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue) {
								return PrometheusEnvironment.autocompleteQuery(searchValue, this.getMaxResults());
							}
						})
						.addCssClass("textarea-nowrap")
						
				)
			
				.addAllFields(WidgetSettingsFactory.createDefaultChartFields())
				
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
		// Resolve Query
		JsonElement queryElement = settings.get("query");
		if(queryElement.isJsonNull()) {
			return;
		}
		
		String prometheusQuerys = queryElement.getAsString();
		
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
		
		//---------------------------------
		// Timeframe
		long earliest = settings.get("timeframe_earliest").getAsLong();
		long latest = settings.get("timeframe_latest").getAsLong();
		
		//---------------------------------
		// Fetch Data
		JsonArray resultArray = new JsonArray();
		String[] queryArray = prometheusQuerys.trim().split("\r\n|\n");
		for(int i = 0; i < queryArray.length; i++) {
			JsonObject queryResult = environment.queryRange(queryArray[i], earliest, latest);
			
			if(queryResult != null) {
				resultArray.add(queryResult);
			}
		}
		
		response.getContent().append(resultArray.toString());	
	}
	
	public void createSampleData(JSONResponse response) { 

		response.getContent().append(CFW.Files.readPackageResource(FeaturePrometheus.PACKAGE_RESOURCE, "emp_widget_prometheus_range_chart_sample.json") );
		
	}
	
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeaturePrometheus.PACKAGE_RESOURCE, "emp_prometheus_commonFunctions.js") );
		array.add(  new FileDefinition(HandlingType.JAR_RESOURCE, FeaturePrometheus.PACKAGE_RESOURCE, "emp_widget_prometheus_range_chart.js") );
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
