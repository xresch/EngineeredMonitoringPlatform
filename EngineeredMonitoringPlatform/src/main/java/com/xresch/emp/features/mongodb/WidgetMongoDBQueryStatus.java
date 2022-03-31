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
import com.xresch.cfw.features.dashboard.WidgetDefinition;
import com.xresch.cfw.features.dashboard.WidgetSettingsFactory;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.utils.CFWConditions;

public class WidgetMongoDBQueryStatus extends WidgetDefinition  {
	
	private static final String FIELDNAME_DETAILCOLUMNS = "detailcolumns";
	private static final String FIELDNAME_LABELCOLUMNS = "labelcolumns";
	private static final String FIELDNAME_VALUECOLUMN = "valuecolumn";
	private static final String FIELDNAME_URLCOLUMN = "urlcolumn";
	private static final String FIELDNAME_QUERY_FIND = "find";
	private static final String FIELDNAME_QUERY_SORT = "sort";
	private static final String FIELDNAME_QUERY_AGGREGATE = "aggregate";
	
	private String FIELDNAME_ENVIRONMENT = MongoDBSettingsFactory.FIELDNAME_ENVIRONMENT;
	
	private static final String FIELDNAME_ALERT_THRESHOLD = "ALERT_THRESHOLD";
	
	private static Logger logger = CFWLog.getLogger(WidgetMongoDBQueryStatus.class.getName());
	
	@Override
	public String getWidgetType() {return "emp_mongodb_querystatus";}


	
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureMongoDB.PACKAGE_RESOURCE, "emp_widget_mongodb_querystatus.js") );
		return array;
	}
	
	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new LinkedHashMap<>();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureMongoDB.PACKAGE_RESOURCE, "lang_en_emp_mongodb.properties"));
		return map;
	}
	
	
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureMongoDB.PERMISSION_MONGODB);
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
				
				.addField(CFWField.newString(FormFieldType.TEXT, FIELDNAME_VALUECOLUMN)
						.setLabel("{!emp_widget_mongodb_valuecolumn!}")
						.setDescription("{!emp_widget_mongodb_valuecolumn_desc!}")
				)
				
				.addField(CFWField.newString(FormFieldType.TEXT, FIELDNAME_LABELCOLUMNS)
						.setLabel("{!emp_widget_mongodb_labelcolumns!}")
						.setDescription("{!emp_widget_mongodb_labelcolumns_desc!}")
				)
				
				.addField(CFWField.newString(FormFieldType.TEXT, FIELDNAME_DETAILCOLUMNS)
						.setLabel("{!emp_widget_mongodb_detailcolumns!}")
						.setDescription("{!emp_widget_mongodb_detailcolumns_desc!}")
				)

				.addField(CFWField.newString(FormFieldType.TEXT, FIELDNAME_URLCOLUMN)
						.setLabel("{!emp_widget_mongodb_urlcolumn!}")
						.setDescription("{!emp_widget_mongodb_urlcolumn_desc!}")
				)
				
				.addAllFields(CFWConditions.createThresholdFields());
	}
	
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings, long earliest, long latest) { 
		//---------------------------------
		// Example Data
		Boolean isSampleData = (Boolean)settings.getField(WidgetSettingsFactory.FIELDNAME_SAMPLEDATA).getValue();
		if(isSampleData != null && isSampleData) {
			response.setPayLoad(createSampleData());
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
		String environmentString = (String)widgetSettings.getField(FIELDNAME_ENVIRONMENT).getValue();

		if(Strings.isNullOrEmpty(environmentString)) {
			return null;
		}
		
		int environmentID = Integer.parseInt(environmentString);
		
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
		String collectionName = (String)widgetSettings.getField(MongoDBSettingsFactory.FIELDNAME_QUERY_COLLECTION).getValue();
		
		if(Strings.isNullOrEmpty(collectionName)) {
			return null;
		}
		
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
	
	public JsonArray createSampleData() { 	
		return CFW.Random.randomJSONArrayOfMightyPeople(12);
	}
	
		
}


