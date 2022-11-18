package com.xresch.emp.features.exense.step;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.bson.Document;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoIterable;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.dashboard.DashboardWidget;
import com.xresch.cfw.features.dashboard.WidgetDefinition;
import com.xresch.cfw.features.dashboard.WidgetSettingsFactory;
import com.xresch.cfw.features.dashboard.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.utils.CFWConditions;
import com.xresch.cfw.validation.NumberRangeValidator;

public class WidgetPlanExecutionsTimerange extends WidgetDefinition  {
		
	@Override
	public String getWidgetType() {return FeatureExenseStep.WIDGET_PREFIX+"_planexecutionstimerange";}
	
	@Override
	public WidgetDataCachePolicy getCachePolicy() {
		return WidgetDataCachePolicy.TIME_BASED;
	}
	
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureExenseStep.PACKAGE_RESOURCE, "emp_widget_step_common.js") );
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureExenseStep.PACKAGE_RESOURCE, "emp_widget_step_planexecutionstimerange.js") );
		return array;
	}
	
	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new LinkedHashMap<>();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureExenseStep.PACKAGE_RESOURCE, "lang_en_emp_step.properties"));
		return map;
	}
	
	
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureExenseStep.PERMISSION_STEP);
	}
	
	
	@Override
	public CFWObject getSettings() {
		
		return createQueryAndThresholdFields()						
				.addField(WidgetSettingsFactory.createDefaultDisplayAsField())				
				.addAllFields(WidgetSettingsFactory.createTilesSettingsFields())
				//.addField(WidgetSettingsFactory.createDisableBoolean())
				.addField(WidgetSettingsFactory.createSampleDataField())
									
		;
	}
	
	public CFWObject createQueryAndThresholdFields() {
		return new CFWObject()
				.addField(StepSettingsFactory.createEnvironmentSelectorField())
				.addField(StepSettingsFactory.createPlansSelectorField())	
				.addAllFields(CFWConditions.createThresholdFields());
	}
	
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings, long earliest, long latest, int timezoneOffsetMinutes) { 

		//#################################################
		// Example Data	
		//#################################################
		Boolean isSampleData = (Boolean)settings.getField(WidgetSettingsFactory.FIELDNAME_SAMPLEDATA).getValue();
		if(isSampleData != null && isSampleData) {
			response.addCustomAttribute("url", "http://sampleurl.yourserver.io");
			response.setPayLoad(createSampleData(earliest, latest));
			return;
		}
		
		//#################################################
		// Real Data	
		//#################################################
		
		StepEnvironment environment = StepCommonFunctions.resolveEnvironmentFromWidgetSettings(settings);
		if(environment == null) { return; }
		
		if(!environment.isDBDefined()) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "Step Plan Executions Time Range: The chosen environment seems configured incorrectly or is unavailable.");
			return;
		}
		
		response.addCustomAttribute("url", environment.url());
		response.setPayLoad(loadDataFromStepDB(settings, earliest, latest));
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
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "Step Plan Executions Time Range: The chosen environment seems configured incorrectly or is unavailable.");
			return null;
		}
		
		//-----------------------------
		// Resolve Projects Param
		LinkedHashMap<String,String> projects = (LinkedHashMap<String,String>)widgetSettings.getField(StepSettingsFactory.FIELDNAME_STEP_PROJECT).getValue();
		
		if(projects.size() == 0) {
			return null;
		}		
		
		//-----------------------------
		// Create Aggregate Document
		String aggregateDocString = CFW.Files.readPackageResource( FeatureExenseStep.PACKAGE_RESOURCE, "emp_widget_step_planexecutionstimerange_query.bson");
		aggregateDocString = CFW.Utils.Time.replaceTimeframePlaceholders(aggregateDocString, earliest, latest);
		
		// Example Project Filter >> $or: [ {'_id': ObjectId('62443ecfee10d74e1b132860')},{'_id': ObjectId('62444fadee10d74e1b1395af')} ]
		StringBuilder plansFilter = new StringBuilder("$or: [");
		for(Entry<String, String> entry : projects.entrySet()) {
			plansFilter.append("{'planid': '"+entry.getKey()+"'},");
		}
		plansFilter.append("]");
		
		aggregateDocString = aggregateDocString.replace("$$plansFilter$$", plansFilter.toString());

		//-----------------------------
		// Fetch Data
		MongoIterable<Document> result;
		
		//start from projects to get projects data as well
		result = environment.aggregate("projects", aggregateDocString);
		
		//-----------------------------
		// Push to Queue
		JsonArray resultArray = new JsonArray();
		if(result != null) {
			for (Document currentDoc : result) {
				JsonObject object = CFW.JSON.stringToJsonObject(currentDoc.toJson(FeatureExenseStep.writterSettings));
				resultArray.add(object);
			}
		}
		
		return resultArray;
		
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public JsonArray createSampleData(long earliest, long latest) { 	
		
		return StepCommonFunctions.defaultStepSeriesExampleData(2,24, earliest, latest);
	}
		
}


