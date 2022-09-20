package com.xresch.emp.features.dynatrace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

public class WidgetHostLogs extends WidgetDefinition {

	private static Logger logger = CFWLog.getLogger(WidgetHostLogs.class.getName());
	@Override
	public String getWidgetType() {return "emp_dynatrace_hostlogs";}
	
	@Override
	public WidgetDataCachePolicy getCachePolicy() {
		return WidgetDataCachePolicy.TIME_BASED;
	}
	
	class WidgetHostLogsSettings extends CFWObject {
		public WidgetHostLogsSettings() {
			this.addField(DynatraceSettingsFactory.createEnvironmentSelectorField())
			
				.addField(DynatraceSettingsFactory.createSingleHostSelectorField())
			
				.addField(DynatraceSettingsFactory.createSingleLogSelectorField(EntityType.HOST))
			
				.addField(DynatraceSettingsFactory.createLogQueryField())
			
				.addField(DynatraceSettingsFactory.createLogMaxEntriesField())
			
				.addField(WidgetSettingsFactory.createSampleDataField());
		}
	}
	
	@Override
	public CFWObject getSettings() {
		return new WidgetHostLogsSettings();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings, long earliest, long latest) { 
		
		WidgetHostLogsSettings settingsObject = new WidgetHostLogsSettings();
		settingsObject.mapJsonFields(jsonSettings, true, true);
		
		//---------------------------------
		// Example Data
		Boolean sampleData = ((CFWField<Boolean>)settingsObject.getField("sampledata")).getValue();
		
		if(sampleData != null 
		&& sampleData) {
			createSampleData(response);
			return;
		}
		
		//---------------------------------
		// Resolve Host
		LinkedHashMap<String,String> hostMap = ((CFWField<LinkedHashMap<String,String>>)settingsObject.getField("JSON_HOST")).getValue();

		if(hostMap == null || hostMap.isEmpty()) {
			return;
		}
				
		String hostID = hostMap.keySet().toArray(new String[]{})[0];
		
		
		//---------------------------------
		// Resolve Host
		LinkedHashMap<String,String> logMap = ((CFWField<LinkedHashMap<String,String>>)settingsObject.getField("JSON_LOG")).getValue();

		if(logMap == null || logMap.isEmpty()) {
			return;
		}
				
		String logName = logMap.keySet().toArray(new String[]{})[0];
		
		//---------------------------------
		// Resolve Log Query
		String logQuery = ((CFWField<String>)settingsObject.getField("LOG_QUERY")).getValue();

		if(logQuery == null) {
			logQuery = "";
		}
		//---------------------------------
		// Resolve Log Query
		Integer logMaxEntries = ((CFWField<Integer>)settingsObject.getField("LOG_MAX_ENTRIES")).getValue();

		if(logMaxEntries == null || logMaxEntries == 0) {
			logMaxEntries = 20;
		}
		
		//---------------------------------
		// Get Environment
		JsonElement environmentElement = jsonSettings.get("environment");
		if(environmentElement.isJsonNull()) {
			return;
		}
		
		DynatraceEnvironment environment = DynatraceEnvironmentManagement.getEnvironment(environmentElement.getAsInt());
		if(environment == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "Dynatace Host Processes Widget: The chosen environment seems not configured correctly.");
			return;
		}

		//---------------------------------
		// Fetch Data
		JsonArray array = environment.getLogRecordsForHost(hostID, logName, logQuery, logMaxEntries, earliest, latest);

		response.getContent().append(CFW.JSON.toJSON(array));	
	}
	
	public void createSampleData(JSONResponse response) { 

		response.getContent().append(CFW.Files.readPackageResource(FeatureDynatrace.PACKAGE_RESOURCE, "emp_widget_dynatrace_hostlogs_sample.json") );
		
	}
	
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDynatrace.PACKAGE_RESOURCE, "emp_dynatrace_commons.js") );
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDynatrace.PACKAGE_RESOURCE, "emp_widget_dynatrace_hostlogs.js") );
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
