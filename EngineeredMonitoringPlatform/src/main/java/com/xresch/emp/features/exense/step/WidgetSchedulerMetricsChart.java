package com.xresch.emp.features.exense.step;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.features.dashboard.widgets.WidgetDefinition;
import com.xresch.cfw.features.dashboard.widgets.WidgetSettingsFactory;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

public class WidgetSchedulerMetricsChart extends WidgetDefinition  {
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String getWidgetType() {return FeatureExenseStep.WIDGET_PREFIX+"_schedulermetricschart";}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public WidgetDataCachePolicy getCachePolicy() {
		return WidgetDataCachePolicy.TIME_BASED;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String widgetCategory() {
		return FeatureExenseStep.WIDGET_CATEGORY_EXENSESTEP_MONGODB;
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String widgetName() { return "Scheduler Metrics Chart"; }
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureExenseStep.PACKAGE_MANUAL, "widget_"+getWidgetType()+".html");
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureExenseStep.PACKAGE_RESOURCE, "emp_widget_step_common.js") );
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureExenseStep.PACKAGE_RESOURCE, "emp_widget_step_schedulermetricschart.js") );
		return array;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new LinkedHashMap<>();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureExenseStep.PACKAGE_RESOURCE, "lang_en_emp_step.properties"));
		return map;
	}
	
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureExenseStep.PERMISSION_STEP);
	}
	
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public CFWObject getSettings() {
		
		return createQueryFields()									
				.addAllFields(WidgetSettingsFactory.createDefaultChartFields(false, false))
				.addField(WidgetSettingsFactory.createSampleDataField())
									
		;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	public CFWObject createQueryFields() {
		return new CFWObject()
				.addField(StepSettingsFactory.createEnvironmentSelectorField())
				.addField(StepSettingsFactory.createSchedulerSelectorField())
				.addField(StepSettingsFactory.createMetricsSelectorField())
				;	
	}
	
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings, CFWTimeframe timeframe) { 

		long earliest = timeframe.getEarliest();
		long latest = timeframe.getLatest();
		
		//#################################################
		// Example Data	
		//#################################################
		Boolean isSampleData = (Boolean)settings.getField(WidgetSettingsFactory.FIELDNAME_SAMPLEDATA).getValue();
		if(isSampleData != null && isSampleData) {
			response.addCustomAttribute("url", "http://sampleurl.yourserver.io");
			response.setPayload(createSampleData(earliest, latest));
			return;
		}
		
		//#################################################
		// Real Data	
		//#################################################
		
		StepEnvironment environment = StepCommonFunctions.resolveEnvironmentFromWidgetSettings(settings);
		if(environment == null) { return; }
		
		if(!environment.isProperlyDefined()) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, widgetName()+": The chosen environment seems configured incorrectly or is unavailable.");
			return;
		}
		
		response.addCustomAttribute("url", environment.url());
		response.setPayload(loadDataFromStepDB(settings, earliest, latest));
	}

	/*********************************************************************
	 * 
	 * @param latest time in millis of which to fetch the data.
	 *********************************************************************/
	@SuppressWarnings("unchecked")
	public JsonArray loadDataFromStepDB(CFWObject widgetSettings, long earliest, long latest){
		
		//-----------------------------
		// Get Environment
		StepEnvironment environment = StepCommonFunctions.resolveEnvironmentFromWidgetSettings(widgetSettings);
		if(environment == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, widgetName()+": The chosen environment seems configured incorrectly or is unavailable.");
			return null;
		}
		
		//-----------------------------
		// Resolve schedulers Param
		LinkedHashMap<String,String> schedulers = (LinkedHashMap<String,String>)widgetSettings.getField(StepSettingsFactory.FIELDNAME_STEP_SCHEDULERS).getValue();
		
		if(schedulers.size() == 0) {
			return null;
		}	
		
		//-----------------------------
		// Resolve Metrics Param
		LinkedHashMap<String,String> metrics = (LinkedHashMap<String,String>)widgetSettings.getField(StepSettingsFactory.FIELDNAME_STEP_METRICS).getValue();
		
		if(metrics.size() == 0) {
			return null;
		}	
		
		//-----------------------------
		// Fetch Data
		JsonArray result = environment.fetchTimeSeriesMultiple(schedulers.keySet(), metrics.keySet(), earliest, latest);
		
		return result;
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public JsonArray createSampleData(long earliest, long latest) { 	
				
		return StepCommonFunctions.defaultStepSeriesExampleData(3,24, earliest, latest);
	}
		
}


