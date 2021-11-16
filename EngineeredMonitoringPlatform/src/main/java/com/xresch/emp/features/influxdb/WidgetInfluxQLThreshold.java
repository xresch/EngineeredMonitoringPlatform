package com.xresch.emp.features.influxdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.dashboard.WidgetDefinition;
import com.xresch.cfw.features.dashboard.WidgetSettingsFactory;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;

public class WidgetInfluxQLThreshold extends WidgetDefinition {

	private static final String FIELDNAME_SUFFIX = "suffix";
	private static final String FIELDNAME_QUERY = "query";
	private static final String FIELDNAME_ALERT_THRESHOLD = "ALERT_THRESHOLD";
	
	private static Logger logger = CFWLog.getLogger(WidgetInfluxQLThreshold.class.getName());
	@Override
	public String getWidgetType() {return "emp_influxdb_influxql_threshold";}
		
	/*********************************************************************
	 * 
	 *********************************************************************/
	@Override
	public CFWObject getSettings() {
		return createInstantAndThresholdFields()
				.addField(WidgetSettingsFactory.createDefaultDisplayAsField())				
				.addAllFields(WidgetSettingsFactory.createTilesSettingsFields())
				.addField(WidgetSettingsFactory.createSampleDataField())
		;
	}
	
	public CFWObject createInstantAndThresholdFields() {
		return new CFWObject()
				
				.addField(InfluxDBSettingsFactory.createEnvironmentSelectorField())
				
				.addField(InfluxDBSettingsFactory.createDatabaseSelectorField())
				
				.addField(InfluxDBSettingsFactory.createQueryField(
						"SELECT mean(*)\r\n"
						+ "FROM runtime \r\n"
						+ "WHERE time >= [earliest] and time < [latest] group by time([interval]);"
					)
				)
			
				.addField(CFWField.newString(FormFieldType.TEXT, "valuecolumn")
						.setLabel("{!emp_widget_influxdb_influxql_valuecolumn!}")
						.setDescription("{!emp_widget_influxdb_influxql_valuecolumn_desc!}")
						.addValidator(new NotNullOrEmptyValidator())
				)
				
				.addField(CFWField.newString(FormFieldType.TEXT, "labels")
						.setLabel("{!emp_widget_influxdb_influxql_labels!}")
						.setDescription("{!emp_widget_influxdb_influxql_labels_desc!}")
				)
		
				.addField(CFWField.newString(FormFieldType.TEXT, FIELDNAME_SUFFIX)
						.setLabel("{!emp_widget_influxdb_influxql_suffix!}")
						.setDescription("{!emp_widget_influxdb_influxql_suffix_desc!}")
				)
				
				.addAllFields(CFW.Conditions.createThresholdFields())
		;
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
		response.setPayLoad(loadDataFromInfluxDB(settings, earliest, latest));
		
	}
	
	/*********************************************************************
	 * 
	 * @param latest time in millis of which to fetch the data.
	 *********************************************************************/
	@SuppressWarnings("unchecked")
	public JsonObject loadDataFromInfluxDB(CFWObject widgetSettings, long earliest, long latest){
		
		//---------------------------------
		// Resolve Database		
		LinkedHashMap<String,String> databaseNameMap = (LinkedHashMap<String,String>)widgetSettings.getField(InfluxDBSettingsFactory.FIELDNAME_DATABASE).getValue();
		if(databaseNameMap == null || databaseNameMap.isEmpty()) {
			return null;
		}

		String databaseName = databaseNameMap.values().toArray(new String[]{})[0];
		
		//---------------------------------
		// Resolve Query		
		String influxdbQuery = (String)widgetSettings.getField(InfluxDBSettingsFactory.FIELDNAME_QUERY).getValue();
		
		if(Strings.isNullOrEmpty(influxdbQuery)) {
			return null;
		}
		
		//---------------------------------
		// Get Environment
		String environmentID = (String)widgetSettings.getField(InfluxDBSettingsFactory.FIELDNAME_ENVIRONMENT).getValue();
		InfluxDBEnvironment environment;
		if(environmentID != null) {
			environment = InfluxDBEnvironmentManagement.getEnvironment(Integer.parseInt(environmentID));
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "Influx DB Chart: The chosen environment seems not configured correctly.");
			return null;
		}
				
		//---------------------------------
		// Fetch Data

		JsonObject queryResult = environment.queryRangeV1(databaseName, influxdbQuery, earliest, latest);
		
		return queryResult;
		
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public JsonObject createSampleData() { 

		String jsonString = CFW.Files.readPackageResource(FeatureInfluxDB.PACKAGE_RESOURCE, "emp_widget_influxdb_influxql_threshold_sample_v1.json");
		
		JsonObject exampleData = CFW.JSON.stringToJsonElement(jsonString).getAsJsonObject();
		
		return exampleData;
		
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureInfluxDB.PACKAGE_RESOURCE, "emp_Influxdb_commonFunctions.js") );
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureInfluxDB.PACKAGE_RESOURCE, "emp_widget_influxdb_influxql_threshold.js") );
		return array;
	}

	/*********************************************************************
	 * 
	 *********************************************************************/
	@Override
	public ArrayList<FileDefinition> getCSSFiles() {
		return new ArrayList<FileDefinition>();
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureInfluxDB.PACKAGE_RESOURCE, "lang_en_emp_influxdb.properties"));
		return map;
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureInfluxDB.PERMISSION_WIDGETS_INFLUXDB);
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public boolean supportsTask() {
		return false;
	}
	
	/************************************************************
	 * Override this method to return a description of what the
	 * task of this widget does.
	 ************************************************************/
	public String getTaskDescription() {
		return "Checks if any of the selected values returned by the instant query exceeds the selected threshold.";
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
					.setOptions(CFW.Conditions.CONDITION_OPTIONS())
					.setValue(CFW.Conditions.CONDITION_EMERGENCY.toString())
				);
	}
	
	/*************************************************************************
	 * Implement the actions your task should execute.
	 * See {@link com.xresch.cfw.features.jobs.CFWJobTask#executeTask CFWJobTask.executeTask()} to get
	 * more details on how to implement this method.
	 *************************************************************************/
//	public void executeTask(JobExecutionContext context, CFWObject taskParams, DashboardWidget widget, CFWObject settings) throws JobExecutionException {
//		
//		//----------------------------------------
//		// Fetch Data
//		JsonObject payloadObject;
//		Boolean isSampleData = (Boolean)settings.getField(WidgetSettingsFactory.FIELDNAME_SAMPLEDATA).getValue();
//		if(isSampleData != null && isSampleData) {
//			payloadObject = createSampleData();
//		}else {
//			payloadObject = loadDataFromInfluxDB(settings, System.currentTimeMillis() , System.currentTimeMillis());
//		}
//		
//		if(payloadObject == null || payloadObject.size() == 0) {
//			return;
//		}
//		
//		//----------------------------------------
//		// Get alertThreshhold
//		String alertThreshholdString = (String)taskParams.getField(FIELDNAME_ALERT_THRESHOLD).getValue();
//		
//		if(alertThreshholdString == null ) {
//			return;
//		}
//
//		ThresholdCondition alertThreshholdCondition = ThresholdCondition.valueOf(alertThreshholdString);
//		
//		//----------------------------------------
//		// Get Results
//		JsonObject payloadObject = payloadObject.get(0).getAsJsonObject();
//		
//		JsonElement dataObject = payloadObject.get("data");
//		if(dataObject == null || !dataObject.isJsonObject()) {
//			return;
//		}
//		
//		JsonElement resultElement = dataObject.getAsJsonObject().get("result");
//		if(resultElement == null || !resultElement.isJsonArray()) {
//			return;
//		}
//		
//		JsonArray resultArray = resultElement.getAsJsonArray();
//		
//		//----------------------------------------
//		// Check Condition
//		boolean conditionMatched = false;
//		ArrayList<JsonObject> instantExceedingThreshold = new ArrayList<>();
//		
//		for(JsonElement element : resultArray) {
//			
//			
//			JsonObject current = element.getAsJsonObject();
//			JsonArray valueArray = current.get("value").getAsJsonArray();
//			Float value = valueArray.get(1).getAsFloat();
//			ThresholdCondition condition = CFW.Conditions.getConditionForValue(value, settings);
//			if(condition != null 
//			&& CFW.Conditions.compareIsEqualsOrMoreDangerous(alertThreshholdCondition, condition)) {
//				conditionMatched = true;
//				instantExceedingThreshold.add(current);
//			}
//		}
//				
//		//----------------------------------------
//		// Handle Alerting
//		CFWJobsAlertObject alertObject = new CFWJobsAlertObject(context, this.getWidgetType());
//
//		alertObject.mapJobExecutionContext(context);
//
//		AlertType type = alertObject.checkSendAlert(conditionMatched, null);
//		
//		if(!type.equals(AlertType.NONE)) {
//
//			//----------------------------------------
//			// Prepare Contents
//			String linkHTML = "";
//			if(widget != null) {
//				linkHTML = widget.createWidgetOriginMessage();
//			}
//			
//			//----------------------------------------
//			// RAISE
//			if(type.equals(AlertType.RAISE)) {
//				
//				//----------------------------------------
//				// Create Job List 
//				String metricListText = "";
//				String metricListHTML = "<ul>";
//				for(JsonObject current : instantExceedingThreshold) {
//					
//					JsonObject metricObject = current.get("metric").getAsJsonObject();
//					
//					//-----------------------------
//					// Create Metric String
//					String metricString = "";
//					for(Entry<String, JsonElement> entry : metricObject.entrySet()) {
//						
//						metricString += entry.getKey()+": "+entry.getValue().toString()+", ";
//					}
//					metricString = metricString.substring(0, metricString.length()-2);
//					
//					
//					//-----------------------------
//					// Add to Lists
//					metricListText +=  metricString+" / ";
//					metricListHTML += "<li>"+metricString+"</li>";
//
//				}
//				
//				metricListText = metricListText.substring(0, metricListText.length()-3);
//				metricListHTML+="</ul>";
//				
//				//----------------------------------------
//				// Create Message
//				String baseMessage = "The following metrics(s) have reached the threshold "+alertThreshholdString+":";
//				String messagePlaintext = baseMessage+" "+metricListText;
//				String messageHTML = "<p>"+baseMessage+"</p>";
//				messageHTML += metricListHTML;
//				messageHTML += linkHTML;
//				
//				CFW.Messages.addErrorMessage(messagePlaintext);
//				
//				alertObject.doSendAlert(context, MessageType.ERROR, "EMP: Alert - InfluxDB metric(s) reached threshold", messagePlaintext, messageHTML);
//			}
//			
//			//----------------------------------------
//			// RESOLVE
//			if(type.equals(AlertType.RESOLVE)) {
//				String message = CFW.Random.randomIssueResolvedMessage();
//				String messageHTML = "<p>"+message+"</p>"+linkHTML;
//				
//				CFW.Messages.addSuccessMessage("Issue has resolved.");
//				alertObject.doSendAlert(context, MessageType.SUCCESS, "EMP: Resolved - InfluxDB metric(s) below threshold", message, messageHTML);
//			}
//		}
//	}
}
