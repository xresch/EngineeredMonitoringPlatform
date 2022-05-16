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
import com.xresch.cfw.features.usermgmt.User;
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
				
				.addField(PrometheusSettingsFactory.createEnvironmentSelectorField())
				
				// Disable Security to not mess up Queries
				.addField((CFWField)CFWField.newString(FormFieldType.TEXTAREA, "query")
						.setLabel("{!emp_widget_prometheus_range_query!}")
						.setDescription("{!emp_widget_prometheus_range_query_desc!}")
						.disableSanitization()
						.setOptions(CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(PrometheusEnvironment.SETTINGS_TYPE))
						.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
							@Override
							public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
								return PrometheusEnvironment.autocompleteQuery(searchValue, this.getMaxResults());
							}
						})
						.addCssClass("textarea-nowrap")
						
				)
			
				.addField(CFWField.newString(FormFieldType.TEXT, "labels")
						.setLabel("{!emp_widget_prometheus_labels!}")
						.setDescription("{!emp_widget_prometheus_labels_desc!}")
				)
				.addAllFields(WidgetSettingsFactory.createDefaultChartFields(false, false))
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
		// Resolve Query
		JsonElement queryElement = jsonSettings.get("query");
		if(queryElement.isJsonNull()) {
			return;
		}
		
		String prometheusQuerys = queryElement.getAsString();
		
		//---------------------------------
		// Get Environment
		JsonElement environmentElement = jsonSettings.get("environment");
		if(environmentElement.isJsonNull()) {
			return;
		}
		
		PrometheusEnvironment environment = PrometheusEnvironmentManagement.getEnvironment(environmentElement.getAsInt());
		if(environment == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "Prometheus Widget: The chosen environment seems not configured correctly.");
			return;
		}
		
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
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeaturePrometheus.PACKAGE_RESOURCE, "lang_en_emp_prometheus.properties"));
		return map;
	}
	
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeaturePrometheus.PERMISSION_PROMETHEUS);
	}

}
