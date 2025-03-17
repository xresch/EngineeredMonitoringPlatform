package com.xresch.emp.features.exense.step;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.dashboard.DashboardWidget;
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.features.dashboard.widgets.WidgetDefinition;
import com.xresch.cfw.features.dashboard.widgets.WidgetSettingsFactory;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.response.JSONResponse;

import com.xresch.cfw.utils.CFWMonitor;
import com.xresch.cfw.utils.CFWState;

public class WidgetSchedulerStatusByUsers extends WidgetDefinition  {
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String getWidgetType() {return FeatureExenseStep.WIDGET_PREFIX+"_schedulerstatususers";}
	
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
	public String widgetName() { return "Scheduler Status By Users"; }
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String descriptionHTML() {
		return 
			  CFW.Files.readPackageResource(FeatureExenseStep.PACKAGE_MANUAL, "widget_"+getWidgetType()+".html")
			+ CFW.Files.readPackageResource(FeatureExenseStep.PACKAGE_MANUAL, "widget_common_content_thresholds.html")
			;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureExenseStep.PACKAGE_RESOURCE, "emp_widget_step_common.js") );
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureExenseStep.PACKAGE_RESOURCE, "emp_widget_step_schedulerstatususers.js") );
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
		
		return createQueryAndThresholdFields()						
				.addField(WidgetSettingsFactory.createDefaultDisplayAsField())				
				.addAllFields(WidgetSettingsFactory.createTilesSettingsFields())
				//.addField(WidgetSettingsFactory.createDisableBoolean())
				.addField(WidgetSettingsFactory.createSampleDataField())
									
		;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	public CFWObject createQueryAndThresholdFields() {
		return new CFWObject()
				.addField(StepSettingsFactory.createEnvironmentSelectorField())
				.addField(StepSettingsFactory.createUsersSelectorField())							
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
			CFW.Messages.addWarningMessage(widgetName()+": The chosen environment seems configured incorrectly or is unavailable.");
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
			CFW.Messages.addWarningMessage(widgetName()+": The chosen environment seems configured incorrectly or is unavailable.");
			return null;
		}
		
		//-----------------------------
		// Resolve Users Param
		LinkedHashMap<String,String> users = (LinkedHashMap<String,String>)widgetSettings.getField(StepSettingsFactory.FIELDNAME_STEP_USERS).getValue();
		
		if(users.size() == 0) {
			return null;
		}
		
		//-----------------------------
		// Load Last Execution for Every Scheduler
		JsonArray results = new JsonArray();
		
    	//--------------------------
    	// Merge Unique list of Schedulers
    	LinkedHashSet<StepSchedulerDetails> uniqueSchedulers = new LinkedHashSet<>();
    	
		for(String userID : users.keySet()) { 
			uniqueSchedulers.addAll( environment.getSchedulersForUser(userID) );
		}
			
    	//--------------------------
    	// Fetch Status For Schedulers
		for(StepSchedulerDetails details : uniqueSchedulers) {
			JsonArray lastExecutionArray = 
					environment.getSchedulerLastNExecutions(
							  details.getSchedulerID()
							, 1
							, earliest, latest);
			
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

	/*********************************************************************
	 * 
	 *********************************************************************/
	public boolean supportsTask() {
		return true;
	}
	
	/************************************************************
	 * Override this method to return a description of what the
	 * task of this widget does.
	 ************************************************************/
	public String getTaskDescription() {
		return "Checks if any of the schedulers exceeds the selected threshold, either by status or by result.";
	}
	
	/************************************************************
	 * Override this method and return a CFWObject containing 
	 * fields for the task parameters. The settings will be passed 
	 * to the 
	 * Always return a new instance, do not reuse a CFWObject.
	 * @return CFWObject
	 ************************************************************/
	public CFWObject getTasksParameters() {
		
		return new CFWJobsAlertObject(false)
				.addField(CFW.Conditions.createThresholdTriggerSelectorField());
	}
	
	/*************************************************************************
	 * Implement the actions your task should execute.
	 * See {@link com.xresch.cfw.features.jobs.CFWJobTask#executeTask CFWJobTask.executeTask()} to get
	 * more details on how to implement this method.
	 *************************************************************************/
	public void executeTask(JobExecutionContext context, CFWObject taskParams, DashboardWidget widget, CFWObject settings, CFWMonitor monitor, CFWTimeframe offset) throws JobExecutionException {
				
		//----------------------------------------
		// Fetch Data
		JsonArray resultArray;
		Boolean isSampleData = (Boolean)settings.getField(WidgetSettingsFactory.FIELDNAME_SAMPLEDATA).getValue();
		if(isSampleData != null && isSampleData) {
			resultArray = createSampleData();
		}else {
			
			long earliest = offset.getEarliest();
			long latest = offset.getLatest();
			
			resultArray = loadDataFromStepDB(settings, earliest, latest);
		}
		
		if(resultArray == null || resultArray.size() == 0) {
			return;
		}
		
		StepEnvironment environment = StepCommonFunctions.resolveEnvironmentFromWidgetSettings(settings);
		String stepURL = environment.url();
		String widgetType = this.getWidgetType();
		StepCommonFunctions.defaultStatusAlerting(context, taskParams, widget, widgetType, settings, stepURL, resultArray);
	}
		
}


