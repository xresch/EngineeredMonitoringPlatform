package com.xresch.emp.features.exense.step;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map.Entry;
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
import com.xresch.cfw.features.dashboard.DashboardWidget;
import com.xresch.cfw.features.dashboard.WidgetDefinition;
import com.xresch.cfw.features.dashboard.WidgetSettingsFactory;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject.AlertType;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.utils.CFWConditions;
import com.xresch.cfw.utils.CFWConditions.ThresholdCondition;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;

public class WidgetPlanStatusByProject extends WidgetDefinition  {
		
	private String FIELDNAME_ENVIRONMENT = StepSettingsFactory.FIELDNAME_ENVIRONMENT;
	
	private static final String FIELDNAME_ALERT_THRESHOLD = "ALERT_THRESHOLD";
	
	private static Logger logger = CFWLog.getLogger(WidgetPlanStatusByProject.class.getName());
	
	@Override
	public String getWidgetType() {return FeatureExenseStep.WIDGET_PREFIX+"_planstatusprojects";}


	
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureExenseStep.PACKAGE_RESOURCE, "emp_widget_step_common.js") );
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureExenseStep.PACKAGE_RESOURCE, "emp_widget_step_planstatusprojects.js") );
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
				.addField(StepSettingsFactory.createProjectsSelectorField())							
				.addAllFields(CFWConditions.createThresholdFields());
	}
	
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings, long earliest, long latest) { 

		//#################################################
		// Example Data	
		//#################################################
		Boolean isSampleData = (Boolean)settings.getField(WidgetSettingsFactory.FIELDNAME_SAMPLEDATA).getValue();
		if(isSampleData != null && isSampleData) {
			response.addCustomAttribute("url", "http://sampleurl.yourserver.io");
			response.setPayLoad(createSampleData());
			return;
		}
		
		
		//#################################################
		// Real Data	
		//#################################################
		
		//-----------------------------
		// Resolve Environment ID
		String environmentString = (String)settings.getField(FIELDNAME_ENVIRONMENT).getValue();

		if(Strings.isNullOrEmpty(environmentString)) {
			return;
		}
		
		//-----------------------------
		// Get Environment
		StepEnvironment environment;
		if(environmentString != null) {
			 environment = StepEnvironmentManagement.getEnvironment(Integer.parseInt(environmentString));
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "Step Query Status: The chosen environment seems configured incorrectly or is unavailable.");
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
		// Resolve Environment ID
		String environmentString = (String)widgetSettings.getField(FIELDNAME_ENVIRONMENT).getValue();

		if(Strings.isNullOrEmpty(environmentString)) {
			return null;
		}
		
		//-----------------------------
		// Get Environment
		StepEnvironment environment;
		if(environmentString != null) {
			 environment = StepEnvironmentManagement.getEnvironment(Integer.parseInt(environmentString));
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "Step Query Status: The chosen environment seems configured incorrectly or is unavailable.");
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
		String aggregateDocString = CFW.Files.readPackageResource( FeatureExenseStep.PACKAGE_RESOURCE, "emp_widget_step_planstatusprojects_query.bson");
		aggregateDocString = CFW.Utils.Time.replaceTimeframePlaceholders(aggregateDocString, earliest, latest);
		
		// Example Project Filter >> $or: [ {'_id': ObjectId('62443ecfee10d74e1b132860')},{'_id': ObjectId('62444fadee10d74e1b1395af')} ]
		StringBuilder projectsFilter = new StringBuilder("$or: [");
		for(Entry<String, String> entry : projects.entrySet()) {
			projectsFilter.append("{'_id': ObjectId('"+entry.getKey()+"')},");
		}
		projectsFilter.append("]");
		
		aggregateDocString = aggregateDocString.replace("$$projectsFilter$$", projectsFilter.toString());
		
		//-----------------------------
		// Fetch Data
		MongoIterable<Document> result;

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
					.setOptions(CFW.Conditions.CONDITION_OPTIONS())
					.setValue(CFW.Conditions.CONDITION_EMERGENCY.toString())
				);
	}
	
	/*************************************************************************
	 * Implement the actions your task should execute.
	 * See {@link com.xresch.cfw.features.jobs.CFWJobTask#executeTask CFWJobTask.executeTask()} to get
	 * more details on how to implement this method.
	 *************************************************************************/
	public void executeTask(JobExecutionContext context, CFWObject taskParams, DashboardWidget widget, CFWObject settings) throws JobExecutionException {
		
		
		//----------------------------------------
		// Fetch Data
		JsonArray resultArray;
		Boolean isSampleData = (Boolean)settings.getField(WidgetSettingsFactory.FIELDNAME_SAMPLEDATA).getValue();
		if(isSampleData != null && isSampleData) {
			resultArray = createSampleData();
		}else {
			//To Be Done!!!
			resultArray = loadDataFromStepDB(settings, CFW.Utils.Time.getCurrentDateWithOffset(0, 0, 0, -30).getTime(), System.currentTimeMillis());
		}
		
		if(resultArray == null || resultArray.size() == 0) {
			return;
		}
		
		//----------------------------------------
		// Get alertThreshhold
		String alertThreshholdString = (String)taskParams.getField(FIELDNAME_ALERT_THRESHOLD).getValue();
		
		if(alertThreshholdString == null ) {
			return;
		}

		ThresholdCondition alertThreshholdCondition = ThresholdCondition.valueOf(alertThreshholdString);
		
		//----------------------------------------
		// Check Condition
		boolean conditionMatched = false;
		ArrayList<JsonObject> instantExceedingThreshold = new ArrayList<>();
		
		for(JsonElement element : resultArray) {
			
			JsonObject current = element.getAsJsonObject();
			Float value = current.get("duration").getAsFloat();

			ThresholdCondition condition = CFW.Conditions.getConditionForValue(value, settings);
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
					for (String fieldname : new String[]{"duration"}) {
						labelString += current.get(fieldname.trim()).getAsString() + " ";
					}
					labelString = labelString.substring(0, labelString.length()-1);
					
					metricListText +=  labelString+" / ";
					
					//---------------------------------
					// Add Label as String and Link
//					if( Strings.isNullOrEmpty(urlColumn) ){
//						metricListHTML += "<li><b>"+labelString+"</b>";
//					}else {
//						String url = current.get(urlColumn.trim()).getAsString();
//						if(Strings.isNullOrEmpty(url)) {
//							metricListHTML += "<li><b>"+labelString+"</b>";
//						}else {
//							metricListHTML += "<li><b><a href=\""+url+"\">"+labelString+"</a></b>";
//						}
//					}
					
					//-----------------------------
					// Create Details String
//					if(!Strings.isNullOrEmpty(detailColumns)) {
//						String detailsString = "";
//						for (String fieldname : detailColumns.split(",")) {
//							if(fieldname != null && (urlColumn == null || !fieldname.trim().equals(urlColumn.trim())) ) {
//								detailsString += fieldname+"=\""+current.get(fieldname.trim()).getAsString() + "\" ";
//							}
//						}
//						metricListHTML += ": "+detailsString.substring(0, detailsString.length()-1);
//					}
//					
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


