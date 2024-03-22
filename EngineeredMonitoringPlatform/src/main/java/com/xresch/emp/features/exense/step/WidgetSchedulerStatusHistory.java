package com.xresch.emp.features.exense.step;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.features.dashboard.widgets.WidgetDefinition;
import com.xresch.cfw.features.dashboard.widgets.WidgetSettingsFactory;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.utils.CFWState;

public class WidgetSchedulerStatusHistory extends WidgetDefinition {

	private static Logger logger = CFWLog.getLogger(WidgetSchedulerStatusHistory.class.getName());
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String getWidgetType() {return FeatureExenseStep.WIDGET_PREFIX+"_schedulerstatushistory";}
	
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
		return FeatureExenseStep.WIDGET_CATEGORY_EXENSESTEP;
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String widgetName() { return "Scheduler Status History"; }
	
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
	public CFWObject getSettings() {
		
		return createQueryAndThresholdFields()						
				.addField(CFWField.newBoolean(FormFieldType.BOOLEAN, "showstatistics")
						.setLabel("{!emp_widget_step_showstatistics!}")
						.setDescription("{!emp_widget_step_showstatistics_desc!}")
						.setValue(true)
				)
				
				.addField(CFWField.newBoolean(FormFieldType.BOOLEAN, "showsparkline")
						.setLabel("{!emp_widget_step_showsparkline!}")
						.setDescription("{!emp_widget_step_showsparkline_desc!}")
						.setValue(true)
				)
												
				.addField(WidgetSettingsFactory.createSampleDataField())
									
		;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	public CFWObject createQueryAndThresholdFields() {
		return new CFWObject()
				.addField(StepSettingsFactory.createEnvironmentSelectorField())
				.addField(StepSettingsFactory.createSchedulerSelectorField())							
				.addAllFields(CFWState.createThresholdFields());
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
			response.setPayload(createSampleData());
			return;
		}
		
		//#################################################
		// Real Data	
		//#################################################
		
		StepEnvironment environment = StepCommonFunctions.resolveEnvironmentFromWidgetSettings(settings);
		if(environment == null) { return; }
		
		if(!environment.isProperlyDefined()) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "Step Status: The chosen environment seems configured incorrectly or is unavailable.");
			return;
		}
		
		response.addCustomAttribute("url", environment.url());
		response.setPayload(loadDataFromStep(settings, earliest, latest));
	}
	
	/*********************************************************************
	 * 
	 * @param latest time in millis of which to fetch the data.
	 *********************************************************************/
	@SuppressWarnings("unchecked")
	public JsonArray loadDataFromStep(CFWObject widgetSettings, long earliest, long latest){
		
		//-----------------------------
		// Get Environment
		StepEnvironment environment = StepCommonFunctions.resolveEnvironmentFromWidgetSettings(widgetSettings);
		if(environment == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, this.widgetName()+": The chosen environment seems configured incorrectly or is unavailable.");
			return null;
		}
		
		//-----------------------------
		// Resolve Scheduler Param
		LinkedHashMap<String,String> schedulers = (LinkedHashMap<String,String>)widgetSettings.getField(StepSettingsFactory.FIELDNAME_STEP_SCHEDULERS).getValue();
		
		if(schedulers.size() == 0) {
			return null;
		}
		
		//-----------------------------
		// Load Last Execution for every Scheduler
		JsonArray results = new JsonArray();
		
		for(String schedulerID : schedulers.keySet()) {
			JsonArray lastExecutionArray = environment.getSchedulerLastNExecutions(schedulerID, Integer.MAX_VALUE, earliest, latest);
			results.addAll(lastExecutionArray);
		}
		
		return results;
		
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public JsonArray createSampleData() { 	
				
		JsonArray array = StepCommonFunctions.defaultStepStatusExampleData(24);
		return array;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureExenseStep.PACKAGE_RESOURCE, "emp_step_commonFunctions.js") );
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureExenseStep.PACKAGE_RESOURCE, "emp_widget_step_schedulerstatushistory.js") );
		return array;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public ArrayList<FileDefinition> getCSSFiles() {
		return null;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
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

}
