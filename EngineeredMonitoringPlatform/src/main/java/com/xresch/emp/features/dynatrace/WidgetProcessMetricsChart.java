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
import com.xresch.cfw.features.dashboard.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.emp.features.common.FeatureEMPCommon;
import com.xresch.emp.features.dynatrace.DynatraceEnvironment.EntityType;

public class WidgetProcessMetricsChart extends WidgetDefinition {

	private static Logger logger = CFWLog.getLogger(WidgetProcessMetricsChart.class.getName());
	@Override
	public String getWidgetType() {return "emp_dynatrace_processmetricschart";}
	
	@Override
	public WidgetDataCachePolicy getCachePolicy() {
		return WidgetDataCachePolicy.TIMEPRESET_BASED;
	}
		
	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				.addField(DynatraceSettingsFactory.createEnvironmentSelectorField())
				
				.addField(DynatraceSettingsFactory.createSingleHostSelectorField())
				
				.addField(DynatraceSettingsFactory.createSingleProcessGroupInstanceSelectorField())
				
				.addField(DynatraceSettingsFactory.createMetricsSelectorField("PROCESS_GROUP"))
				
				
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
		// Resolve HostID
		JsonElement processElement = jsonSettings.get("JSON_PROCESS");
		if(processElement == null || processElement.isJsonNull()) {
			return;
		}
		
		JsonObject processObject = processElement.getAsJsonObject();
		if(processObject.size() == 0) {
			return;
		}
		
		String processID = processObject.keySet().toArray(new String[]{})[0];
		
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
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "Dynatrace Process Metrics Chart Widget: The chosen environment seems not configured correctly.");
			return;
		}
		
		//---------------------------------
		// Fetch Data
		
		//Example URL
		// curl -H 'Authorization: Api-Token xxxxxxx' -X GET "https://xxxxx.live.dynatrace.com/api/v2/metrics/query?metricSelector=builtin:tech.generic.cpu.usage&from=1604372880000&to=1604588879160&resolution=h&entitySelector=type(PROCESS_GROUP),entityId(PROCESS_GROUP-8675886ACE55BAA3)"
		
		JsonObject queryResult = environment.queryMetrics(EntityType.PROCESS_GROUP_INSTANCE, processID, earliest, latest, metricsSelector);		
		response.getContent().append(CFW.JSON.toJSON(queryResult));	
	}
	
	public void createSampleData(JSONResponse response) { 

		response.getContent().append(CFW.Files.readPackageResource(FeatureDynatrace.PACKAGE_RESOURCE, "emp_widget_dynatrace_processmetricschart_sample.json") );
		
	}
	
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDynatrace.PACKAGE_RESOURCE, "emp_dynatrace_commons.js") );
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDynatrace.PACKAGE_RESOURCE, "emp_widget_dynatrace_processmetricschart.js") );
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
