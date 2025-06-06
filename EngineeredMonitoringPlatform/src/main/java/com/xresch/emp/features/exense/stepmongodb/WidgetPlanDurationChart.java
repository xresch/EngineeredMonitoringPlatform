package com.xresch.emp.features.exense.stepmongodb;

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
import com.xresch.cfw.features.dashboard.widgets.WidgetDefinition;
import com.xresch.cfw.features.dashboard.widgets.WidgetSettingsFactory;
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.response.JSONResponse;

import com.xresch.cfw.utils.CFWState;
import com.xresch.cfw.validation.NumberRangeValidator;

public class WidgetPlanDurationChart extends WidgetDefinition  {
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String getWidgetType() {return FeatureExenseStepMongoDB.WIDGET_PREFIX+"_plandurationchart";}
	
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
		return FeatureExenseStepMongoDB.WIDGET_CATEGORY_EXENSESTEP_MONGODB;
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String widgetName() { return "Plan Duration Chart"; }
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureExenseStepMongoDB.PACKAGE_MANUAL, "widget_"+getWidgetType()+".html");
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureExenseStepMongoDB.PACKAGE_RESOURCE, "emp_widget_step_common.js") );
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureExenseStepMongoDB.PACKAGE_RESOURCE, "emp_widget_step_plandurationchart.js") );
		return array;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new LinkedHashMap<>();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureExenseStepMongoDB.PACKAGE_RESOURCE, "lang_en_emp_step.properties"));
		return map;
	}
	
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureExenseStepMongoDB.PERMISSION_STEP);
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
				.addField(StepSettingsFactory.createPlansSelectorField());	
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
		
		if(!environment.isDBDefined()) {
			CFW.Messages.addWarningMessage("Step Plan Executions Time Range: The chosen environment seems configured incorrectly or is unavailable.");
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
			CFW.Messages.addWarningMessage("Step Plan Executions Time Range: The chosen environment seems configured incorrectly or is unavailable.");
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
		String aggregateDocString = CFW.Files.readPackageResource( FeatureExenseStepMongoDB.PACKAGE_RESOURCE, "emp_widget_step_plandurationchart_query.bson");
		aggregateDocString = CFW.Time.replaceTimeframePlaceholders(aggregateDocString, earliest, latest, 0);
		
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
				JsonObject object = CFW.JSON.stringToJsonObject(currentDoc.toJson(FeatureExenseStepMongoDB.writterSettings));
				resultArray.add(object);
			}
		}
		
		return resultArray;
		
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public JsonArray createSampleData(long earliest, long latest) { 	
				
		return StepCommonFunctions.defaultStepSeriesExampleData(3,24, earliest, latest);
	}
		
}


