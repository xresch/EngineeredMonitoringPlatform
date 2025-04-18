package com.xresch.emp.features.prometheus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWMessages;
import com.xresch.cfw._main.CFWMessages.MessageType;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.CFWFieldFlag;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.dashboard.DashboardWidget;
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.features.dashboard.widgets.WidgetDefinition;
import com.xresch.cfw.features.dashboard.widgets.WidgetSettingsFactory;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject.AlertType;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.utils.CFWMonitor;
import com.xresch.cfw.utils.CFWState.CFWStateOption;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;

public class WidgetInstantThreshold extends WidgetDefinition {

	private static final String FIELDNAME_SUFFIX = "suffix";
	private static final String FIELDNAME_QUERY = "query";
	private static final String FIELDNAME_ALERT_THRESHOLD = "ALERT_THRESHOLD";
	
	private static Logger logger = CFWLog.getLogger(WidgetInstantThreshold.class.getName());
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String getWidgetType() {return "emp_prometheus_instant_threshold";}
	
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
		return FeaturePrometheus.WIDGET_CATEGORY_PROMETHEUS;
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String widgetName() { return "Instant Threshold"; }
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeaturePrometheus.PACKAGE_MANUAL, "widget_"+getWidgetType()+".html");
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
	
	/************************************************************
	 * 
	 ************************************************************/
	public CFWObject createInstantAndThresholdFields() {
		return new CFWObject()
				.addField(PrometheusSettingsFactory.createEnvironmentSelectorField())

				// Disable Security to not mess up Queries
				.addField(CFWField.newString(FormFieldType.TEXT, FIELDNAME_QUERY)
						.setLabel("{!emp_widget_prometheus_instant_query!}")
						.setDescription("{!emp_widget_prometheus_instant_query_desc!}")
						.disableSanitization()
						.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
				)
				.addField(CFWField.newString(FormFieldType.TEXT, FIELDNAME_SUFFIX)
						.setLabel("{!emp_widget_prometheus_suffix!}")
						.setDescription("{!emp_widget_prometheus_suffix_desc!}")
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
			response.setPayload(createSampleData());
			return;
		}
		//---------------------------------
		// Real Data		
		response.setPayload(loadDataFromPrometheus(settings, latest));
		
	}
	
	/*********************************************************************
	 * 
	 * @param latest time in millis of which to fetch the data.
	 *********************************************************************/
	@SuppressWarnings("unchecked")
	public JsonArray loadDataFromPrometheus(CFWObject widgetSettings, long latest){
		
		//---------------------------------
		// Resolve Query		
		String prometheusQuery = (String)widgetSettings.getField(FIELDNAME_QUERY).getValue();
		
		if(Strings.isNullOrEmpty(prometheusQuery)) {
			return null;
		}
		
		//---------------------------------
		// Get Environment
		String environmentID = (String)widgetSettings.getField(PrometheusSettingsFactory.FIELDNAME_ENVIRONMENT).getValue();
		PrometheusEnvironment environment;
		if(environmentID != null) {
			environment = PrometheusEnvironmentManagement.getEnvironment(Integer.parseInt(environmentID));
		}else {
			CFW.Messages.addWarningMessage("Prometheus Instant Threshold: The db of the chosen environment seems not configured correctly.");
			return null;
		}
		
		//---------------------------------
		// Fetch Data
		JsonObject queryResult = environment.query(prometheusQuery, latest);
		JsonArray array = new JsonArray();

		if(queryResult != null) {
			array.add(queryResult);
		}
		
		return array;
		
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	public JsonArray createSampleData() { 

		String jsonString = CFW.Files.readPackageResource(FeaturePrometheus.PACKAGE_RESOURCE, "emp_widget_prometheus_instant_threshold_sample.json");
		
		JsonArray exampleData = CFW.JSON.stringToJsonElement(jsonString).getAsJsonArray();
		
		return exampleData;
		
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeaturePrometheus.PACKAGE_RESOURCE, "emp_prometheus_commonFunctions.js") );
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeaturePrometheus.PACKAGE_RESOURCE, "emp_widget_prometheus_instant_threshold.js") );
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
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeaturePrometheus.PACKAGE_RESOURCE, "lang_en_emp_prometheus.properties"));
		return map;
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeaturePrometheus.PERMISSION_PROMETHEUS);
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
		
		return new CFWJobsAlertObject(false)
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
	public void executeTask(JobExecutionContext context, CFWObject taskParams, DashboardWidget widget, CFWObject settings, CFWMonitor monitor, CFWTimeframe offset) throws JobExecutionException {
		
		//----------------------------------------
		// Fetch Data
		JsonArray payloadArray;
		Boolean isSampleData = (Boolean)settings.getField(WidgetSettingsFactory.FIELDNAME_SAMPLEDATA).getValue();
		if(isSampleData != null && isSampleData) {
			payloadArray = createSampleData();
		}else {
			payloadArray = loadDataFromPrometheus(settings, System.currentTimeMillis());
		}
		
		if(payloadArray == null || payloadArray.size() == 0) {
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
		JsonObject payloadObject = payloadArray.get(0).getAsJsonObject();
		
		JsonElement dataObject = payloadObject.get("data");
		if(dataObject == null || !dataObject.isJsonObject()) {
			return;
		}
		
		JsonElement resultElement = dataObject.getAsJsonObject().get("result");
		if(resultElement == null || !resultElement.isJsonArray()) {
			return;
		}
		
		JsonArray resultArray = resultElement.getAsJsonArray();
		
		//----------------------------------------
		// Check Condition
		boolean conditionMatched = false;
		ArrayList<JsonObject> instantExceedingThreshold = new ArrayList<>();
		
		for(JsonElement element : resultArray) {
			
//			{
//				"metric": {
//					"instance": "localhost:9182",
//					"job": "windows_localhost",
//					"volume": "C:"
//				},
//				"value": [
//					1633079796,
//					"78.69094408591715"
//				]
//			},
			
			JsonObject current = element.getAsJsonObject();
			JsonArray valueArray = current.get("value").getAsJsonArray();
			Float value = valueArray.get(1).getAsFloat();
			CFWStateOption condition = CFW.Conditions.getConditionForValue(value, settings);
			if(condition != null 
			&& CFW.Conditions.compareIsEqualsOrMoreDangerous(alertThreshholdCondition, condition)) {
				conditionMatched = true;
				instantExceedingThreshold.add(current);
			}
		}
				
		//----------------------------------------
		// Handle Alerting
		CFWJobsAlertObject alertObject = new CFWJobsAlertObject(context, this.getWidgetType(), false);

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
				String metricListText = "";
				String metricListHTML = "<ul>";
				for(JsonObject current : instantExceedingThreshold) {
					
					JsonObject metricObject = current.get("metric").getAsJsonObject();
					
					//-----------------------------
					// Create Metric String
					String metricString = "";
					for(Entry<String, JsonElement> entry : metricObject.entrySet()) {
						
						metricString += entry.getKey()+": "+entry.getValue().toString()+", ";
					}
					metricString = metricString.substring(0, metricString.length()-2);
					
					
					//-----------------------------
					// Add to Lists
					metricListText +=  metricString+" / ";
					metricListHTML += "<li>"+metricString+"</li>";

				}
				
				metricListText = metricListText.substring(0, metricListText.length()-3);
				metricListHTML+="</ul>";
				
				//----------------------------------------
				// Create Message
				String baseMessage = "The following metrics(s) have reached the threshold "+alertThreshholdString+":";
				String messagePlaintext = baseMessage+" "+metricListText;
				String messageHTML = "<p>"+baseMessage+"</p>";
				messageHTML += metricListHTML;
				messageHTML += linkHTML;
				
				CFW.Messages.addErrorMessage(messagePlaintext);
				
				alertObject.doSendAlert(context, MessageType.ERROR, "EMP: Alert - Prometheus metric(s) reached threshold", messagePlaintext, messageHTML);
			}
			
			//----------------------------------------
			// RESOLVE
			if(type.equals(AlertType.RESOLVE)) {
				String message = CFW.Random.issueResolvedMessage();
				String messageHTML = "<p>"+message+"</p>"+linkHTML;
				
				CFW.Messages.addSuccessMessage("Issue has resolved.");
				alertObject.doSendAlert(context, MessageType.SUCCESS, "EMP: Resolved - Prometheus metric(s) below threshold", message, messageHTML);
			}
		}
	}
}
