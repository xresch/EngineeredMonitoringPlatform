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
import com.xresch.cfw.features.dashboard.WidgetDefinition;
import com.xresch.cfw.features.dashboard.WidgetSettingsFactory;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.emp.features.common.FeatureEMPCommon;

public class WidgetHostUnitConsumptionByTags extends WidgetDefinition {

	private static Logger logger = CFWLog.getLogger(WidgetHostUnitConsumptionByTags.class.getName());
	@Override
	public String getWidgetType() {return "emp_dynatrace_hostunitconsumptionbytags";}
		
	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				.addField(DynatraceWidgetSettingsFactory.createDynatraceEnvironmentSelectorField())
								
				.addField(CFWField.newString(FormFieldType.TEXT, "tagsfilter")
						.setLabel("{!emp_widget_dynatrace_tagsfilter!}")
						.setDescription("{!emp_widget_dynatrace_tagsfilter_desc!}")
						.setOptions(CFW.DB.ContextSettings.getSelectOptionsForTypeAndUser(DynatraceEnvironment.SETTINGS_TYPE))
				)
				
				.addField(WidgetSettingsFactory.createSampleDataField())
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
		
		DynatraceEnvironment environment = DynatraceEnvironmentManagement.getEnvironment(environmentElement.getAsInt());
		if(environment == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "Dynatace Host Processes Widget: The chosen environment seems not configured correctly.");
			return;
		}
	
		//---------------------------------
		// Timeframe
		long earliest = settings.get("timeframe_earliest").getAsLong();
		long latest = settings.get("timeframe_latest").getAsLong();
		
		//---------------------------------
		// Fetch Data
		JsonArray array = environment.getAllHosts(earliest, latest);

		response.getContent().append(CFW.JSON.toJSON(array));	
	}
	
	public void createSampleData(JSONResponse response) { 

		response.getContent().append(CFW.Files.readPackageResource(FeatureDynatrace.PACKAGE_RESOURCE, "emp_widget_dynatrace_hostunitconsumptionbytags_sample.json") );
		
	}
	
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDynatrace.PACKAGE_RESOURCE, "emp_dynatrace_commons.js") );
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDynatrace.PACKAGE_RESOURCE, "emp_widget_dynatrace_hostunitconsumptionbytags.js") );
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

}
