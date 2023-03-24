package com.xresch.emp.features.influxdb;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.dashboard.DashboardWidget;
import com.xresch.cfw.features.dashboard.WidgetDefinition;
import com.xresch.cfw.features.dashboard.WidgetSettingsFactory;
import com.xresch.cfw.features.dashboard.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject.AlertType;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.utils.CFWState.CFWStateOption;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;
import com.xresch.cfw.validation.NumberRangeValidator;

public class WidgetInfluxQLThreshold extends WidgetDefinition {

	private static final String FIELDNAME_LABELS = "labels";
	private static final String FIELDNAME_VALUECOLUMN = "valuecolumn";
	private static final String FIELDNAME_SUFFIX = "suffix";
	private static final String FIELDNAME_ALERT_THRESHOLD = "ALERT_THRESHOLD";
	private static final String FIELDNAME_ALERT_TIMESPAN_MINUTES = "TIMESPAN_MINUTES";
	
	private static Logger logger = CFWLog.getLogger(WidgetInfluxQLThreshold.class.getName());
	@Override
	public String getWidgetType() {return "emp_influxdb_influxql_threshold";}
	
	@Override
	public WidgetDataCachePolicy getCachePolicy() {
		return WidgetDataCachePolicy.TIME_BASED;
	}
			
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
					.disableSanitization()
				)
			
				.addField(CFWField.newString(FormFieldType.TEXT, FIELDNAME_VALUECOLUMN)
						.setLabel("{!emp_widget_influxdb_influxql_valuecolumn!}")
						.setDescription("{!emp_widget_influxdb_influxql_valuecolumn_desc!}")
				)
				
				.addField(CFWField.newString(FormFieldType.TEXT, FIELDNAME_LABELS)
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
	public void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings, CFWTimeframe timeframe) { 
		
		long earliest = timeframe.getEarliest();
		long latest = timeframe.getLatest();
		
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
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "Influx DB Threashold: The chosen environment seems not configured correctly.");
			return null;
		}
				
		//---------------------------------
		// Fetch Data

		JsonObject queryResult = environment.queryRangeInfluxQL(databaseName, influxdbQuery, earliest, latest);
		
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
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureInfluxDB.PACKAGE_RESOURCE, "emp_influxdb_commonFunctions.js") );
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
		return true;
	}
	
	/************************************************************
	 * Override this method to return a description of what the
	 * task of this widget does.
	 ************************************************************/
	public String getTaskDescription() {
		return "Checks if the latest values returned by the InfluxQL query exceed the selected threshold.";
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
				)
				.addField(
					CFWField.newInteger(FormFieldType.NUMBER, FIELDNAME_ALERT_TIMESPAN_MINUTES)
					.setDescription("Select the timespan to check. For example, 60 minutes will query a timespan between present time and 60 minutes in the past.")
					.addValidator(new NumberRangeValidator(5, 60*24))
					.setValue(60)
				);
	}
	
	/*************************************************************************
	 * Implement the actions your task should execute.
	 * See {@link com.xresch.cfw.features.jobs.CFWJobTask#executeTask CFWJobTask.executeTask()} to get
	 * more details on how to implement this method.
	 *************************************************************************/
	public void executeTask(JobExecutionContext context, CFWObject taskParams, DashboardWidget widget, CFWObject settings, CFWTimeframe offset) throws JobExecutionException {
		
		//----------------------------------------
		// Get and Check Settings
		String valueColumnName = (String)settings.getField(FIELDNAME_VALUECOLUMN).getValue();
		
		if(Strings.isNullOrEmpty(valueColumnName)) {
			CFW.Messages.addWarningMessage("Job not executed properly, value column is not specified.");
			return;
		}
		//----------------------------------------
		// Fetch Data
		JsonObject payloadObject;
		Boolean isSampleData = (Boolean)settings.getField(WidgetSettingsFactory.FIELDNAME_SAMPLEDATA).getValue();
		if(isSampleData != null && isSampleData) {
			payloadObject = createSampleData();
		}else {
			Integer timespanMinutes = (Integer)settings.getField(FIELDNAME_ALERT_TIMESPAN_MINUTES).getValue();
			Integer timespanMillis = timespanMinutes * 60 * 1000;
			
			payloadObject = loadDataFromInfluxDB(settings, System.currentTimeMillis() - timespanMillis , System.currentTimeMillis());
		}
		
		if(payloadObject == null || payloadObject.size() == 0) {
			return;
		}
		
		//----------------------------------------
		// Get alertThreshhold
		String alertThreshholdString = (String)taskParams.getField(FIELDNAME_ALERT_THRESHOLD).getValue();
		
		if(alertThreshholdString == null ) {
			return;
		}

		CFWStateOption alertThreshholdCondition = CFWStateOption.valueOf(alertThreshholdString);
		
		//----------------------------------------
		// Get Results
		JsonArray resultArray = payloadObject.get("results").getAsJsonArray();

		//----------------------------------------
		// Check Condition
		boolean conditionMatched = false;
		ArrayList<Properties> valuesExceedingThreshold = new ArrayList<>();
		
		//----------------------------------------
		// Iterate Statements in Results
		
		for(JsonElement statementElement : resultArray) {

			JsonObject currentStatement = statementElement.getAsJsonObject();
			JsonArray seriesArray = currentStatement.get("series").getAsJsonArray();
			
			//----------------------------------------
			// Iterate Series in Statement
			for(JsonElement seriesElement : seriesArray) {
				
				JsonObject currentSeries = seriesElement.getAsJsonObject();
				JsonArray columnsArray = currentSeries.get("columns").getAsJsonArray();
				JsonObject tagsObject = currentSeries.get("tags").getAsJsonObject();
				
				JsonArray valuesArray = currentSeries.get("values").getAsJsonArray();
				
				if(valuesArray.isJsonNull() || valuesArray.size() == 0) {
					continue;
					
				}


				//-------------------------------------
				// Find Time Column Index
				int timeIndex = 0;
				for(; timeIndex < columnsArray.size(); timeIndex++) {
					String columnName = columnsArray.get(timeIndex).getAsString();
					if(columnName.equals("time")) {
						break;
					}
				}
				
				//-------------------------------------
				// Find Value Column Index
				valueColumnName = valueColumnName.trim();
				int valueIndex = 0;
				for(; valueIndex < columnsArray.size(); valueIndex++) {
					String columnName = columnsArray.get(valueIndex).getAsString();
					if(columnName.equals(valueColumnName)) {
						break;
					}
				}
				
				//-------------------------------------
				// Make sure values are sorted by Time
				// Descending
				System.out.println("============== Before Sort ===============");
				System.out.println(CFW.JSON.toJSON(valuesArray));
				
				final int finalTimeIndex = timeIndex;
				

				CFW.JSON.arraySortBy(valuesArray, 
					new Comparator<JsonElement>() {
						@Override
						public int compare(JsonElement e1, JsonElement e2) {
							long i1 = e1.getAsJsonArray().get(finalTimeIndex).getAsLong();
						    long i2 = e2.getAsJsonArray().get(finalTimeIndex).getAsLong();
						    return Long.compare(i1, i2);
						}
					}
				);
				
				System.out.println("============== After Sort ===============");
				System.out.println(CFW.JSON.toJSONPretty(valuesArray));
				
				
				
				//-------------------------------------
				// Evaluate Threshold
				JsonArray firstValues = valuesArray.get(0).getAsJsonArray();
				
				Float valueToCheck = firstValues.get(valueIndex).getAsFloat();
				
				CFWStateOption condition = CFW.Conditions.getConditionForValue(valueToCheck, settings);
				if(condition != null 
				&& CFW.Conditions.compareIsEqualsOrMoreDangerous(alertThreshholdCondition, condition)) {
					conditionMatched = true;
					Properties data = new Properties();
					data.put("values", firstValues);
					data.put("tags", tagsObject);
					data.put("columnNames", columnsArray);
					valuesExceedingThreshold.add(data);
				}

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
			String linkHTML = "";
			if(widget != null) {
				linkHTML = widget.createWidgetOriginMessage();
			}
			
			//----------------------------------------
			// RAISE
			if(type.equals(AlertType.RAISE)) {
				
				//----------------------------------------
				// Create Job List 
				JsonArray metricListArray = new JsonArray();
				String metricListHTML = "<div>";
				
				for(Properties current : valuesExceedingThreshold) {
					
					JsonArray values = (JsonArray)current.get("values");
					JsonObject tags = (JsonObject)current.get("tags");
					JsonArray columnNames = (JsonArray)current.get("columnNames");
					
					//-----------------------------
					// Create Metric String
					JsonObject metricObject = new JsonObject();
					
					//-----------------------------
					// Iterate Tags
					if(!tags.isJsonNull()) {
						for( Entry<String, JsonElement> entry : tags.entrySet()) {
							metricObject.add(entry.getKey(), entry.getValue());
						}
					}
					
					//-----------------------------
					// Iterate Columns and Values
					if(!columnNames.isJsonNull()) {
						for(int j = 0; j < columnNames.size(); j++) {
							metricObject.add(columnNames.get(j).getAsString(), values.get(j));
						}
					}
					
					//-----------------------------
					// Add to Lists
					metricListArray.add(metricObject);
					metricListHTML += CFW.JSON.formatJsonObjectToHTMLTable(metricObject, true)+"</br></br>";

				}
				

				metricListHTML+="</div>";
				
				//----------------------------------------
				// Create Message
				String baseMessage = "The following metrics(s) have reached the threshold "+alertThreshholdString+":";
				String messagePlaintext = baseMessage+" "+metricListArray.toString();
				String messageHTML = "<p>"+baseMessage+"</p>";
				messageHTML += metricListHTML;
				messageHTML += linkHTML;
				
				CFW.Messages.addErrorMessage(messagePlaintext);
				
				alertObject.doSendAlert(context, MessageType.ERROR, "EMP: Alert - InfluxDB metric(s) reached threshold", messagePlaintext, messageHTML);
			}
			
			//----------------------------------------
			// RESOLVE
			if(type.equals(AlertType.RESOLVE)) {
				String message = CFW.Random.randomIssueResolvedMessage();
				String messageHTML = "<p>"+message+"</p>"+linkHTML;
				
				CFW.Messages.addSuccessMessage("Issue has resolved.");
				alertObject.doSendAlert(context, MessageType.SUCCESS, "EMP: Resolved - InfluxDB metric(s) below threshold", message, messageHTML);
			}
		}
	}
}
