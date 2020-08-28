package com.xresch.emp.features.widgets;

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
import com.xresch.emp.features.environments.PrometheusEnvironment;
import com.xresch.emp.features.environments.PrometheusEnvironmentManagement;

public class PrometheusInstantThresholdWidget extends WidgetDefinition {

	private static Logger logger = CFWLog.getLogger(PrometheusInstantThresholdWidget.class.getName());
	@Override
	public String getWidgetType() {return "emp_prometheus_instant_threshold";}
		

	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				.addField(CFWField.newString(FormFieldType.SELECT, "environment")
						.setLabel("{!cfw_widget_spm_environment!}")
						.setDescription("{!cfw_widget_spm_environment_desc!}")
						.setOptions(CFW.DB.ContextSettings.getSelectOptionsForType(PrometheusEnvironment.SETTINGS_TYPE))
				)
				.addField(CFWField.newString(FormFieldType.TEXT, "query")
						.setLabel("{!emp_widget_prometheus_instant_query!}")
						.setDescription("{!emp_widget_prometheus_instant_query_desc!}")
				)
				.addField(CFWField.newString(FormFieldType.TEXT, "suffix")
						.setLabel("{!emp_widget_prometheus_suffix!}")
						.setDescription("{!emp_widget_prometheus_suffix_desc!}")
				)
				
				.addAllFields(WidgetSettingsFactory.createThresholdFields())

				.addField(CFWField.newString(FormFieldType.SELECT, "renderer")
						.setLabel("{!cfw_widget_displayas!}")
						.setDescription("{!cfw_widget_displayas_desc!}")
						.setOptions(new String[]{"Tiles", "Panels", "Table"})
						.setValue("Tiles")
				)
				.addField(CFWField.newString(FormFieldType.SELECT, "sizefactor")
						.setLabel("{!cfw_widget_sizefactor!}")
						.setDescription("{!cfw_widget_sizefactor_desc!}")
						.setOptions(new String[]{"0.25", "0.5", "0.75", "1", "1.25", "1.5", "1.75", "2.0", "2.5", "3.0"})
						.setValue("1")
				)
				
				.addField(CFWField.newString(FormFieldType.SELECT, "borderstyle")
						.setLabel("{!cfw_widget_borderstyle!}")
						.setDescription("{!cfw_widget_borderstyle_desc!}")
						.setOptions(new String[]{"None", "Round", "Superround", "Asymmetric", "Superasymmetric", "Ellipsis"})
						.setValue("None")
				)
				
				.addField(CFWField.newBoolean(FormFieldType.BOOLEAN, "showlabels")
						.setLabel("{!cfw_widget_showlabels!}")
						.setDescription("{!cfw_widget_showlabels_desc!}")
						.setValue(true)
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
	
		//---------------------------------
		// Timeframe
		long latest = settings.get("timeframe_latest").getAsLong();
		
		//---------------------------------
		// Fetch Data
		JsonObject queryResult = environment.query(prometheusQuery, latest);
		JsonArray array = new JsonArray();

		if(queryResult != null) {
			array.add(queryResult);
		}
		
		response.getContent().append(CFW.JSON.toJSON(array));	
	}
	
	public void createSampleData(JSONResponse response) { 

		response.getContent().append(CFW.Files.readPackageResource(FeatureEMPWidgets.RESOURCE_PACKAGE, "emp_widget_prometheus_instant_threshold_sample.json") );
		
	}
	
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureEMPWidgets.RESOURCE_PACKAGE, "emp_commonFunctions.js") );
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureEMPWidgets.RESOURCE_PACKAGE, "emp_widget_prometheus_instant_threshold.js") );
		return array;
	}

	@Override
	public ArrayList<FileDefinition> getCSSFiles() {
		return new ArrayList<FileDefinition>();
	}

	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureEMPWidgets.RESOURCE_PACKAGE, "lang_en_emp_widgets.properties"));
		return map;
	}

}
