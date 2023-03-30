package com.xresch.emp.features.mongodb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.bson.Document;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import com.xresch.cfw.features.dashboard.DashboardWidget;
import com.xresch.cfw.features.dashboard.widgets.WidgetDefinition;
import com.xresch.cfw.features.dashboard.widgets.WidgetSettingsFactory;
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject.AlertType;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.utils.CFWState;
import com.xresch.cfw.utils.CFWState.CFWStateOption;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;

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
	public WidgetDataCachePolicy getCachePolicy() {
		return WidgetDataCachePolicy.TIME_BASED;
	}
		
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDBExtensions.PACKAGE_RESOURCE, "cfw_dbextensions_common.js") );
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
				
				.addAllFields(CFWState.createThresholdFields());
	}
	
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings, CFWTimeframe timeframe) { 
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
	 * @param offsetMinutes used to replace timeframe params, use null if not applicable
	 *********************************************************************/
	@SuppressWarnings("unchecked")
	public JsonArray loadDataFromDBInferface(CFWObject widgetSettings){
				
		//-----------------------------
		// Resolve Environment ID
		String environmentString = (String)widgetSettings.getField(FIELDNAME_ENVIRONMENT).getValue();

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
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public JsonArray createSampleData() { 	
		return CFW.Random.randomJSONArrayOfMightyPeople(12);
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
		return "Checks if any of the selected values returned by the MongoDB query exceeds the selected threshold.";
	}
	
	/************************************************************
	 * Override this method and return a CFWObject containing 
	 * fields for the task parameters. The settings will be passed 
	 * to the 
	 * Always return a new instance, do not reuse a CFWObject.
	 * @return CFWObject
	 ************************************************************/
	public CFWObject getTasksParameters() {
		
		return new CFWJobsAlertObject()
				.addField(
					CFWField.newString(FormFieldType.SELECT, FIELDNAME_ALERT_THRESHOLD)
					.setDescription("Select the threshhold that should trigger the alert when reached.")
					.addValidator(new NotNullOrEmptyValidator())
					.setOptions(CFW.Conditions.STATE_OPTIONS())
					.setValue(CFW.Conditions.STATE_ORANGE.toString())
				);
	}
	
	/*************************************************************************
	 * Implement the actions your task should execute.
	 * See {@link com.xresch.cfw.features.jobs.CFWJobTask#executeTask CFWJobTask.executeTask()} to get
	 * more details on how to implement this method.
	 *************************************************************************/
	public void executeTask(JobExecutionContext context, CFWObject taskParams, DashboardWidget widget, CFWObject settings, CFWTimeframe offset) throws JobExecutionException {
		
		String valueColumn = (String)settings.getField(FIELDNAME_VALUECOLUMN).getValue();
		String labelColumns = (String)settings.getField(FIELDNAME_LABELCOLUMNS).getValue();
		String detailColumns = (String)settings.getField(FIELDNAME_DETAILCOLUMNS).getValue();
		String urlColumn = (String)settings.getField(FIELDNAME_URLCOLUMN).getValue();
		
		//----------------------------------------
		// Fetch Data
		JsonArray resultArray;
		Boolean isSampleData = (Boolean)settings.getField(WidgetSettingsFactory.FIELDNAME_SAMPLEDATA).getValue();
		if(isSampleData != null && isSampleData) {
			resultArray = createSampleData();
		}else {
						
			resultArray = loadDataFromDBInferface(settings);
		}
		
		if(resultArray == null || resultArray.size() == 0) {
			return;
		}
		
		//----------------------------------------
		// Set Column default settings 
		JsonObject object = resultArray.get(0).getAsJsonObject();
		Set<String> fields = object.keySet();
		int i=0;
		for(String fieldname : fields) {
			
			//Set first column as default for label Column
			if(i == 0
			&& Strings.isNullOrEmpty(labelColumns)) {
				labelColumns = fieldname;
			}
			
			//Set last column as default for value
			if(i == fields.size()-1
			&& Strings.isNullOrEmpty(valueColumn)) {
				valueColumn = fieldname;
			}
			
			i++;
		}
		
		//----------------------------------------
		// Get alertThreshhold
		String alertThreshholdString = (String)taskParams.getField(FIELDNAME_ALERT_THRESHOLD).getValue();
		
		if(alertThreshholdString == null ) {
			return;
		}

		CFWStateOption alertThreshholdCondition = CFWStateOption.valueOf(alertThreshholdString);
		
		//----------------------------------------
		// Check Condition
		boolean conditionMatched = false;
		ArrayList<JsonObject> instantExceedingThreshold = new ArrayList<>();
		
		for(JsonElement element : resultArray) {
			
			JsonObject current = element.getAsJsonObject();
			Float value = current.get(valueColumn).getAsFloat();

			CFWStateOption condition = CFW.Conditions.getConditionForValue(value, settings);
			if(condition != null 
			&& CFW.Conditions.compareIsEqualsOrMoreDangerous(alertThreshholdCondition, condition)) {
				conditionMatched = true;
				instantExceedingThreshold.add(current);
			}
		}
				
		//----------------------------------------
		// Handle Alerting
		CFWJobsAlertObject alertObject = new CFWJobsAlertObject(context, this.getWidgetType());

		alertObject.mapJobExecutionContext(context);

		AlertType type = alertObject.checkSendAlert(conditionMatched, null);
		
		if(!type.equals(AlertType.NONE)) {

			//----------------------------------------
			// Prepare Contents
			String widgetLinkHTML = "";
			if(widget != null) {
				widgetLinkHTML = widget.createWidgetOriginMessage();
			}
			
			//----------------------------------------
			// RAISE
			if(type.equals(AlertType.RAISE)) {
				
				//----------------------------------------
				// Create Job List 
				String metricListText = "";
				String metricListHTML = "<ul>";
				for(JsonObject current : instantExceedingThreshold) {
					
					//-----------------------------
					// Create Label String
					String labelString = "";
					for (String fieldname : labelColumns.split(" *, *")) {
						labelString += current.get(fieldname.trim()).getAsString() + " ";
					}
					labelString = labelString.substring(0, labelString.length()-1);
					
					metricListText +=  labelString+" / ";
					
					//---------------------------------
					// Add Label as String and Link
					if( Strings.isNullOrEmpty(urlColumn) ){
						metricListHTML += "<li><b>"+labelString+"</b>";
					}else {
						String url = current.get(urlColumn.trim()).getAsString();
						if(Strings.isNullOrEmpty(url)) {
							metricListHTML += "<li><b>"+labelString+"</b>";
						}else {
							metricListHTML += "<li><b><a href=\""+url+"\">"+labelString+"</a></b>";
						}
					}
					
					//-----------------------------
					// Create Details String
					if(!Strings.isNullOrEmpty(detailColumns)) {
						String detailsString = "";
						for (String fieldname : detailColumns.split(",")) {
							if(fieldname != null && (urlColumn == null || !fieldname.trim().equals(urlColumn.trim())) ) {
								detailsString += fieldname+"=\""+current.get(fieldname.trim()).getAsString() + "\" ";
							}
						}
						metricListHTML += ": "+detailsString.substring(0, detailsString.length()-1);
					}
					
					metricListHTML += "</li>";
				}
				
				metricListText = metricListText.substring(0, metricListText.length()-3);
				metricListHTML+="</ul>";
				
				//----------------------------------------
				// Create Message
				String baseMessage = "The following record(s) have reached the threshold "+alertThreshholdString+":";
				String messagePlaintext = baseMessage+" "+metricListText;
				String messageHTML = "<p>"+baseMessage+"</p>";
				messageHTML += metricListHTML;
				messageHTML += widgetLinkHTML;
				messageHTML += "<h3>CSV Data</h3>"+CFW.JSON.formatJsonArrayToCSV(resultArray, ";");
				
				CFW.Messages.addErrorMessage(messagePlaintext);
				
				alertObject.doSendAlert(context, MessageType.ERROR, "EMP: Alert - Database record(s) reached threshold", messagePlaintext, messageHTML);
				
			}
			
			//----------------------------------------
			// RESOLVE
			if(type.equals(AlertType.RESOLVE)) {
				String message = CFW.Random.randomIssueResolvedMessage();
				String messageHTML = "<p>"+message+"</p>"+widgetLinkHTML;
				
				CFW.Messages.addSuccessMessage("Issue has resolved.");
				alertObject.doSendAlert(context, MessageType.SUCCESS, "EMP: Resolved - Database record(s) below threshold", message, messageHTML);
			}
		}
	}
		
}


