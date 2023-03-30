package com.xresch.emp.features.mongodb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.bson.Document;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoIterable;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.extensions.databases.FeatureDBExtensions;
import com.xresch.cfw.features.dashboard.widgets.WidgetDefinition;
import com.xresch.cfw.features.dashboard.widgets.WidgetSettingsFactory;
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class WidgetMongoDBQueryChart extends WidgetDefinition {

	private static final String FIELDNAME_QUERY_FIND = "find";
	private static final String FIELDNAME_QUERY_SORT = "sort";
	private static final String FIELDNAME_QUERY_AGGREGATE = "aggregate";
	
	private static final String FIELDNAME_SERIESCOLUMNS = "seriescolumns";
	private static final String FIELDNAME_XCOLUMN = "xcolumn";
	private static final String FIELDNAME_YCOLUMN = "ycolumn";
	
	private static Logger logger = CFWLog.getLogger(WidgetMongoDBQueryChart.class.getName());
	
	@Override
	public String getWidgetType() {return "emp_mongodb_querychart";}
	
	@Override
	public WidgetDataCachePolicy getCachePolicy() {
		return WidgetDataCachePolicy.TIME_BASED;
	}
		
	
	@Override
	public CFWObject getSettings() {
		
		return createQueryFields()						
				.addAllFields(WidgetSettingsFactory.createDefaultChartFields(true, false))
				.addField(WidgetSettingsFactory.createSampleDataField())
									
		;
	}
	
	public CFWObject createQueryFields() {
		return new CFWObject()
				.addField(MongoDBSettingsFactory.createEnvironmentSelectorField())
				.addField(MongoDBSettingsFactory.createCollectionSelectorField())
								
				.addField(CFWField.newString(FormFieldType.TEXTAREA, FIELDNAME_QUERY_FIND)
						.setLabel("{!emp_widget_mongodb_query_find!}")
						.setDescription("{!emp_widget_mongodb_query_find_desc!}")
						.disableSanitization() // Do not convert character like "'" to &#x27; etc...
						.setValue("")
				)
				
				.addField(CFWField.newString(FormFieldType.TEXTAREA, FIELDNAME_QUERY_SORT)
						.setLabel("{!emp_widget_mongodb_query_sort!}")
						.setDescription("{!emp_widget_mongodb_query_sort_desc!}")
						.disableSanitization() // Do not convert character like "'" to &#x27; etc...
						.setValue("")
				)
				
				.addField(CFWField.newString(FormFieldType.TEXTAREA, FIELDNAME_QUERY_AGGREGATE)
						.setLabel("{!emp_widget_mongodb_query_aggregate!}")
						.setDescription("{!emp_widget_mongodb_query_aggregate_desc!}")
						.disableSanitization() // Do not convert character like "'" to &#x27; etc...
						.setValue("")
				)
				
				.addField(CFWField.newString(FormFieldType.TEXT, FIELDNAME_SERIESCOLUMNS)
						.setLabel("{!emp_widget_database_seriescolumns!}")
						.setDescription("{!emp_widget_database_seriescolumns_desc!}")
				)
				
				.addField(CFWField.newString(FormFieldType.TEXT, FIELDNAME_XCOLUMN)
						.setLabel("{!emp_widget_database_xcolumn!}")
						.setDescription("{!emp_widget_database_xcolumn_desc!}")
				)
				
				.addField(CFWField.newString(FormFieldType.TEXT, FIELDNAME_YCOLUMN)
						.setLabel("{!emp_widget_database_ycolumn!}")
						.setDescription("{!emp_widget_database_ycolumn_desc!}")
				)
				;
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings, CFWTimeframe timeframe) { 
		
		long earliest = timeframe.getEarliest();
		long latest = timeframe.getLatest();

		//---------------------------------
		// Example Data
		Boolean isSampleData = (Boolean)settings.getField(WidgetSettingsFactory.FIELDNAME_SAMPLEDATA).getValue();
		if(isSampleData != null && isSampleData) {
			response.setPayLoad(createSampleData(earliest, latest));
			return;
		}
		//---------------------------------
		// Real Data		
		response.setPayLoad(loadDataFromDBInferface(settings));
		
	}
	
	/*********************************************************************
	 * 
	 * @param latest time in millis of which to fetch the data.
	 *********************************************************************/
	@SuppressWarnings("unchecked")
	public JsonArray loadDataFromDBInferface(CFWObject widgetSettings){
		
		//-----------------------------
		// Resolve Environment ID
		String environmentString = (String)widgetSettings.getField(MongoDBSettingsFactory.FIELDNAME_ENVIRONMENT).getValue();

		if(Strings.isNullOrEmpty(environmentString)) {
			return null;
		}
		
		//-----------------------------
		// Get Environment
		MongoDBEnvironment environment;
		if(environmentString != null) {
			 environment = MongoDBEnvironmentManagement.getEnvironment(Integer.parseInt(environmentString));
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "mongodb: The chosen environment seems configured incorrectly or is unavailable.");
			return null;
		}
				
		//-----------------------------
		// Resolve Collection Param
		LinkedHashMap<String,String> collectionNameMap = (LinkedHashMap<String,String>)widgetSettings.getField(MongoDBSettingsFactory.FIELDNAME_QUERY_COLLECTION).getValue();
		
		if(collectionNameMap.size() == 0) {
			return null;
		}
		
		String collectionName = collectionNameMap.keySet().toArray(new String[]{})[0];
		
		//-----------------------------
		// Resolve Find Param
		String findDocString = (String)widgetSettings.getField(FIELDNAME_QUERY_FIND).getValue();

		//-----------------------------
		// Resolve Aggregate Param
		String aggregateDocString = (String)widgetSettings.getField(FIELDNAME_QUERY_AGGREGATE).getValue();

		//-----------------------------
		// Resolve Sort Param
		String sortDocString = (String)widgetSettings.getField(FIELDNAME_QUERY_SORT).getValue();

		//-----------------------------
		// Fetch Data
		MongoIterable<Document> result;
		if(Strings.isNullOrEmpty(aggregateDocString)) {
			result = environment.find(collectionName, findDocString, sortDocString, -1);
		}else {

			result = environment.aggregate(collectionName, aggregateDocString);
		}
		
		//-----------------------------
		// Push to Queue
		JsonArray resultArray = new JsonArray();
		if(result != null) {
			for (Document currentDoc : result) {
				JsonObject object = CFW.JSON.stringToJsonObject(currentDoc.toJson(FeatureMongoDB.writterSettings));
				resultArray.add(object);
			}
		}
		
		return resultArray;
		
	}
	
	public JsonArray createSampleData( long earliest, long latest) { 	
		return CFW.Random.randomJSONArrayOfSeriesData(5,24, earliest, latest);
	}
	


	@Override
	public ArrayList<FileDefinition> getCSSFiles() {
		return null;
	}
	
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDBExtensions.PACKAGE_RESOURCE, "cfw_dbextensions_common.js") );
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureMongoDB.PACKAGE_RESOURCE, "emp_widget_mongodb_querychart.js") );
		return array;
	}
	
	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new LinkedHashMap<>();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureMongoDB.PACKAGE_RESOURCE, "lang_en_emp_mongodb.properties"));
		return map;
	}
	
}


