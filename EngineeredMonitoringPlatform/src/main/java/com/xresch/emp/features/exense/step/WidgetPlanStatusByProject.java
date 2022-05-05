package com.xresch.emp.features.exense.step;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.bson.Document;
import org.quartz.JobDataMap;
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
import com.xresch.emp.features.exense.step.FeatureExenseStep.StepExecutionResult;

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
		
		StepEnvironment environment = getStepEnvironment(settings);
		if(environment == null) { return; }
		
		if(!environment.isDBDefined()) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "Step Query Status: The chosen environment seems configured incorrectly or is unavailable.");
			return;
		}
		
		response.addCustomAttribute("url", environment.url());
		response.setPayLoad(loadDataFromStepDB(settings, earliest, latest));
	}



	private StepEnvironment getStepEnvironment(CFWObject settings) {
		//-----------------------------
		// Resolve Environment ID
		String environmentString = (String)settings.getField(FIELDNAME_ENVIRONMENT).getValue();

		if(Strings.isNullOrEmpty(environmentString)) {
			return null;
		}
		
		//-----------------------------
		// Get Environment
		StepEnvironment environment = null;
		if(environmentString != null) {
			 environment = StepEnvironmentManagement.getEnvironment(Integer.parseInt(environmentString));
		}
		
		return environment;
	}
	
	/*********************************************************************
	 * 
	 * @param latest time in millis of which to fetch the data.
	 *********************************************************************/
	@SuppressWarnings("unchecked")
	public JsonArray loadDataFromStepDB(CFWObject widgetSettings, long earliest, long latest){
		
		//-----------------------------
		// Get Environment
		StepEnvironment environment = getStepEnvironment(widgetSettings);
		if(environment == null) {
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
		
//		{
//			"projectid": "62444fadee10d74e1b1395af",
//			"projectname": "AnotherTestProject",
//			"planid": "62694470ee10d74e1b26d744",
//			"planname": "Bla Bla Bla",
//			"schedulerid": "626944feee10d74e1b26df94",
//			"schedulername": "Bla Bla Bla",
//			"status": "ENDED",
//			"result": "PASSED",
//			"duration": 37,
//			"starttime": 1651134840013,
//			"endtime": 1651134840050
//		},
		
		JsonArray array = new JsonArray();
		
		for(int i = 0 ; i < 24; i++) {
			String alphas = ("["+CFW.Random.randomStringAlphaNumerical(3)+"] ").toUpperCase();
			String randomProject = CFW.Random.randomFromArray(new String[] {"Project Omega", "Project Alpha", "Project Epsilon"});
			String randomPlan = CFW.Random.randomFromArray(new String[] {"Test Plan", "Ricks Plan Rolls", "Plan Ahead", "Plan of a Lifetime", "No Plan", "Plan Tage", "Plan E", "Plan ET"});
			String randomResult = CFW.Random.randomFromArray(new String[] {"PASSED", "PASSED", "PASSED", "PASSED", "PASSED", "FAILED", "TECHNICAL_ERROR", "RUNNING"});
			
			int offsetMinutes = -1 * CFW.Random.randomIntegerInRange(15, 120);
			int offsetMultiplier = CFW.Random.randomIntegerInRange(2, 5);
			JsonObject object = new JsonObject();
			object.addProperty("projectid", "62444fadee10d74e1b1395af");
			object.addProperty("projectname", alphas+randomProject);
			object.addProperty("planid", CFW.Random.randomStringAlphaNumerical(24).toLowerCase());
			object.addProperty("planname",  alphas+randomPlan);
			object.addProperty("schedulerid", CFW.Random.randomStringAlphaNumerical(24).toLowerCase());
			object.addProperty("schedulername", alphas+"Scheduler for "+randomPlan);
			object.addProperty("status", "ENDED");
			object.addProperty("result", randomResult);
			object.addProperty("duration", CFW.Random.randomIntegerInRange(10, 12000) );
			object.addProperty("starttime", CFW.Utils.Time.getCurrentTimestampWithOffset(0, 0, 0, 0, offsetMinutes*offsetMultiplier).getTime());
			object.addProperty("endtime",  CFW.Utils.Time.getCurrentTimestampWithOffset(0, 0, 0, 0, offsetMinutes).getTime());
			
			array.add(object);
		}
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
		return "Checks if any of the plans exceeds the selected threshold, either by status or by result.";
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
	public void executeTask(JobExecutionContext context, CFWObject taskParams, DashboardWidget widget, CFWObject settings, CFWTimeframe offset) throws JobExecutionException {
		
		long earliest = offset.getEarliest();
		long latest = offset.getLatest();
		
		//----------------------------------------
		// Fetch Data
		JsonArray resultArray;
		Boolean isSampleData = (Boolean)settings.getField(WidgetSettingsFactory.FIELDNAME_SAMPLEDATA).getValue();
		if(isSampleData != null && isSampleData) {
			resultArray = createSampleData();
		}else {
			resultArray = loadDataFromStepDB(settings, earliest, latest);
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
			Float duration = current.get("duration").getAsFloat();
			String result = current.get("result").getAsString();
			
			StepExecutionResult execResult = StepExecutionResult.valueOf(result);
			
			if(execResult == null || execResult.equals(StepExecutionResult.PASSED)) {
				//check duration if passed
				ThresholdCondition condition = CFW.Conditions.getConditionForValue(duration, settings);
				if(condition != null 
				&& CFW.Conditions.compareIsEqualsOrMoreDangerous(alertThreshholdCondition, condition)) {
					conditionMatched = true;
					instantExceedingThreshold.add(current);
				}
			}else if(!execResult.equals(StepExecutionResult.RUNNING)) {
				//if failed or error
				conditionMatched = true;
				instantExceedingThreshold.add(current);
			}
		}
		
		//----------------------------------------
		// Handle Alerting
		StepEnvironment environment = getStepEnvironment(settings);
		String stepURL = environment.url();
		
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
					
					String projectname = current.get("projectname").getAsString();
					String planname = current.get("planname").getAsString();
					long duration = current.get("duration").getAsLong();
					//-----------------------------
					// Create Label String
					String labelString = "";
					if(!Strings.isNullOrEmpty(projectname)) {
						labelString += projectname + " >> " + planname +" - "+duration+"ms";
					}else {
						labelString += planname +" - "+duration+"ms";
						
					}

					metricListText +=  labelString+" / ";
					
					//---------------------------------
					// Add Label as String and Link
					if(!Strings.isNullOrEmpty(stepURL)) {
						
						String planid = current.get("planid").getAsString();
						String schedulerid = current.get("schedulerid").getAsString();
						
						metricListHTML += "<li>"+labelString+"&nbsp;("
								+"<a target=\"_blank\" href=\""+stepURL+"#/root/plans/editor/"+planid+"?tenant="+projectname+"\">Plan</a>";
						
						if(!Strings.isNullOrEmpty(projectname)) {
							metricListHTML += ", <a target=\"_blank\" href=\""+stepURL+"#/root/plans/list?tenant="+projectname+"\">Project</a>";
						}
						
						if(!Strings.isNullOrEmpty(schedulerid)) {
							metricListHTML += ", <a target=\"_blank\" href=\""+stepURL+"#/root/dashboards/__pp__RTMDashboard?__filter1__=text,taskId,"+projectname+"\">Stats</a>";
						}
						
						metricListHTML += ")</li>";
						
					}else {
						metricListHTML += "<li>"+labelString+"</li>";
					}
			
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
				
				alertObject.doSendAlert(context, MessageType.ERROR, "EMP: Alert - Step plan(s) reached threshold", messagePlaintext, messageHTML);
				
			}
			
			//----------------------------------------
			// RESOLVE
			if(type.equals(AlertType.RESOLVE)) {
				String message = CFW.Random.randomIssueResolvedMessage();
				String messageHTML = "<p>"+message+"</p>"+widgetLinkHTML;
				
				CFW.Messages.addSuccessMessage("Issue has resolved.");
				alertObject.doSendAlert(context, MessageType.SUCCESS, "EMP: Resolved - Step plan(s) below threshold", message, messageHTML);
			}
		}
	}
		
}


