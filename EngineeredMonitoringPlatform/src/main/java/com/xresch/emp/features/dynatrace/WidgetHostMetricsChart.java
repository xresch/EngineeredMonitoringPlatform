package com.xresch.emp.features.dynatrace;

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
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.emp.features.common.FeatureEMPCommon;

public class WidgetHostMetricsChart extends WidgetDefinition {

	private static Logger logger = CFWLog.getLogger(WidgetHostMetricsChart.class.getName());
	@Override
	public String getWidgetType() {return "emp_dynatrace_hostmetricschart";}
		
	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				.addField(CFWField.newString(FormFieldType.SELECT, "environment")
						.setLabel("{!cfw_widget_dynatrace_environment!}")
						.setDescription("{!cfw_widget_dynatrace_environment_desc!}")
						.setOptions(CFW.DB.ContextSettings.getSelectOptionsForType(DynatraceManagedEnvironment.SETTINGS_TYPE))
				)
				
				.addField(CFWField.newTagsSelector("JSON_HOST")
						.setLabel("{!emp_widget_dynatrace_host!}")
						.setDescription("{!emp_widget_dynatrace_host_desc!}")
						.addAttribute("maxTags", "1")
						.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
							
							@Override
							public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue) {
								String environment = request.getParameter("environment");
								
								return DynatraceManagedEnvironment.autocompleteHosts(Integer.parseInt(environment), searchValue, this.getMaxResults());
							}
						})		
				)
				
				.addField(CFWField.newTagsSelector("JSON_METRICS")
						.setLabel("{!emp_widget_dynatrace_metrics!}")
						.setDescription("{!emp_widget_dynatrace_metrics_desc!}")
						.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
							
							@Override
							public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue) {
								String environment = request.getParameter("environment");
								
								return DynatraceManagedEnvironment.autocompleteMetrics(Integer.parseInt(environment), searchValue, this.getMaxResults());
							}
						})		
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
		// Resolve HostID
		JsonElement hostsElement = settings.get("JSON_HOST");
		if(hostsElement == null || hostsElement.isJsonNull()) {
			return;
		}
		
		JsonObject hostsObject = hostsElement.getAsJsonObject();
		if(hostsObject.size() == 0) {
			return;
		}
		
		String hostID = hostsObject.keySet().toArray(new String[]{})[0];
		
		//---------------------------------
		// Resolve Metrics
		JsonElement metricsElement = settings.get("JSON_METRICS");
		if(metricsElement == null || metricsElement.isJsonNull()) {
			return;
		}
		
		JsonObject metricsObject = metricsElement.getAsJsonObject();
		if(metricsObject.size() == 0) {
			return;
		}
		
		String metricsSelector = String.join(",", metricsObject.keySet());

		//---------------------------------
		// Get Environment
		JsonElement environmentElement = settings.get("environment");
		if(environmentElement.isJsonNull()) {
			return;
		}
		
		DynatraceManagedEnvironment environment = DynatraceManagedEnvironmentManagement.getEnvironment(environmentElement.getAsInt());
		if(environment == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "Dynatace Host Details Widget: The chosen environment seems not configured correctly.");
			return;
		}
		
		//---------------------------------
		// Timeframe
		long earliest = settings.get("timeframe_earliest").getAsLong();
		long latest = settings.get("timeframe_latest").getAsLong();
		
		
		//---------------------------------
		// Fetch Data
		JsonObject queryResult = environment.queryMetrics("HOST", hostID, earliest, latest, metricsSelector);		
		response.getContent().append(CFW.JSON.toJSON(queryResult));	
	}
	
	public void createSampleData(JSONResponse response) { 

		response.getContent().append(CFW.Files.readPackageResource(FeatureDynatraceManaged.PACKAGE_RESOURCE, "emp_widget_dynatrace_hostmetricschart_sample.json") );
		
	}
	
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDynatraceManaged.PACKAGE_RESOURCE, "emp_dynatrace_commons.js") );
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDynatraceManaged.PACKAGE_RESOURCE, "emp_widget_dynatrace_hostmetricschart.js") );
		return array;
	}

	@Override
	public ArrayList<FileDefinition> getCSSFiles() {
		return new ArrayList<FileDefinition>();
	}

	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureEMPCommon.PACKAGE_RESOURCE, "lang_en_emp_widgets.properties"));
		return map;
	}

}
