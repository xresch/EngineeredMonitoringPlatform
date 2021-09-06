package com.xresch.emp.features.dynatrace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.dashboard.WidgetDefinition;
import com.xresch.cfw.features.dashboard.WidgetSettingsFactory;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.emp.features.common.FeatureEMPCommon;
import com.xresch.emp.features.dynatrace.DynatraceEnvironment.EntityType;

public class WidgetHostMetricsChart extends WidgetDefinition {

	private static Logger logger = CFWLog.getLogger(WidgetHostMetricsChart.class.getName());
	@Override
	public String getWidgetType() {return "emp_dynatrace_hostmetricschart";}
		
	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				.addField(DynatraceSettingsFactory.createEnvironmentSelectorField())
				
				.addField(DynatraceSettingsFactory.createSingleHostSelectorField())
				
				.addField(DynatraceSettingsFactory.createMetricsSelectorField("HOST"))
				
				.addAllFields(WidgetSettingsFactory.createDefaultChartFields())
				
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
		// Resolve HostID
		JsonElement hostsElement = jsonSettings.get("JSON_HOST");
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
		JsonElement metricsElement = jsonSettings.get("JSON_METRICS");
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
		JsonElement environmentElement = jsonSettings.get("environment");
		if(environmentElement.isJsonNull()) {
			return;
		}
		
		DynatraceEnvironment environment = DynatraceEnvironmentManagement.getEnvironment(environmentElement.getAsInt());
		if(environment == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "Dynatace Host Details Widget: The chosen environment seems not configured correctly.");
			return;
		}
				
		
		//---------------------------------
		// Fetch Data
		JsonObject queryResult = environment.queryMetrics(EntityType.HOST, hostID, earliest, latest, metricsSelector);		
		response.getContent().append(CFW.JSON.toJSON(queryResult));	
	}
	
	public void createSampleData(JSONResponse response) { 

		response.getContent().append(CFW.Files.readPackageResource(FeatureDynatrace.PACKAGE_RESOURCE, "emp_widget_dynatrace_hostmetricschart_sample.json") );
		
	}
	
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDynatrace.PACKAGE_RESOURCE, "emp_dynatrace_commons.js") );
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDynatrace.PACKAGE_RESOURCE, "emp_widget_dynatrace_hostmetricschart.js") );
		return array;
	}

	@Override
	public ArrayList<FileDefinition> getCSSFiles() {
		return new ArrayList<FileDefinition>();
	}

	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDynatrace.PACKAGE_RESOURCE, "lang_en_emp_dynatrace.properties"));
		return map;
	}
	
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureDynatrace.PERMISSION_WIDGETS_DYNATRACE);
	}
}
