package com.xresch.emp.features.awa;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
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
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.features.dashboard.widgets.WidgetDefinition;
import com.xresch.cfw.features.dashboard.widgets.WidgetSettingsFactory;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject.AlertType;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.utils.CFWMonitor;
import com.xresch.emp.features.common.FeatureEMPCommon;

public class WidgetJobStatusCurrent extends WidgetDefinition {

	protected static final String LAST_RUN_MINUTES = "last_run_minutes";
	protected static final String JOBLABELS = "joblabels";
	protected static final String JOBNAMES = "jobnames";
	
	private static Logger logger = CFWLog.getLogger(WidgetJobStatusCurrent.class.getName());
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	@Override
	public String getWidgetType() {return "emp_awajobstatus";}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	@Override
	public WidgetDataCachePolicy getCachePolicy() {
		return WidgetDataCachePolicy.TIME_BASED;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String widgetCategory() {
		return FeatureAWA.WIDGET_CATEGORY_AWA;
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String widgetName() { return "Job Status"; }
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureAWA.PACKAGE_MANUAL, "widget_"+getWidgetType()+".html");
	}
	
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	@Override
	public CFWObject getSettings() {
		return 
			createJobSelectionFields()
				
				.addField(CFWField.newInteger(FormFieldType.TEXT, LAST_RUN_MINUTES)
						.setLabel("{!emp_widget_awajobstatus_last_run_minutes!}")
						.setDescription("{!emp_widget_awajobstatus_last_run_minutes_desc!}")
						.setValue(0)	
				)

				.addField(WidgetSettingsFactory.createDefaultDisplayAsField())				
				.addAllFields(WidgetSettingsFactory.createTilesSettingsFields())
				.addField(WidgetSettingsFactory.createDisableBoolean())
				.addField(WidgetSettingsFactory.createSampleDataField())
	
		;
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public CFWObject createJobSelectionFields() {
		return new CFWObject()
		.addField(AWASettingsFactory.createEnvironmentSelectorField())
		
		.addField(CFWField.newString(FormFieldType.TEXTAREA, JOBNAMES)
				.setLabel("{!emp_widget_awajobstatus_jobnames!}")
				.setDescription("{!emp_widget_awajobstatus_jobnames_desc!}")
				.setValue("")			
		)
		
		.addField(CFWField.newString(FormFieldType.TEXTAREA, JOBLABELS)
				.setLabel("{!emp_widget_awajobstatus_joblabels!}")
				.setDescription("{!emp_widget_awajobstatus_joblabels_desc!}")
				.setValue("")
				
		);
		
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
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
		response.setPayload(loadDataFromAwaAsJsonArray(settings, earliest, latest));
		
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	private JsonArray loadDataFromAwaAsJsonArray(CFWObject widgetSettings, long earliest, long latest) {
		
		//---------------------------------
		// Resolve Jobnames
		String jobnamesString = (String)widgetSettings.getField(JOBNAMES).getValue();
		if(Strings.isNullOrEmpty(jobnamesString)) {
			return null;
		}
		String[] jobnames = jobnamesString.trim().split("[,\t\r\n]+");
				
		String joblabelsString = (String)widgetSettings.getField(JOBLABELS).getValue();
		String[] joblabels = null;
		if(!Strings.isNullOrEmpty(joblabelsString)) {
			joblabels = joblabelsString.trim().split("[,\t\r\n]+");
		}
		

		//---------------------------------
		// Get Environment
		String environmentID = (String)widgetSettings.getField("environment").getValue();
		AWAEnvironment environment;
		if(environmentID != null) {
			environment = AWAEnvironmentManagement.getEnvironment(Integer.parseInt(environmentID));
		}else {
			return null;
		}
		
		//---------------------------------
		// Fetch Data
		
		if(environment.source().equals(AWAEnvironment.SOURCE_REST_API)) {
			return environment.fetchFromAPILast(jobnames, joblabels, earliest, latest );
		}else {
			return environment.fetchFromDatabase(jobnames, joblabels);
		}

	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public JsonArray createSampleData() { 
		
		long currentTime = new Date().getTime();
		JsonElement element = CFW.JSON.stringToJsonElement("["
			+ "{ \"JOBNAME\":\"JP_0003_225\", \"LABEL\":\"JP_0003_225\", \"STATUS\":\"RUNNING\", \"END_TIME\":"+(currentTime-(120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0002_B\", \"LABEL\":\"Job B\", \"STATUS\":\"RUNNING\", \"END_TIME\":"+(currentTime-(1200*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_228\", \"LABEL\":\"JP_0003_228\", \"STATUS\":\"ABNORMAL ENDING\", \"END_TIME\":"+(currentTime-(2120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_V\", \"LABEL\":\"JP_0003_V\", \"STATUS\":\"ENDED OK\", \"END_TIME\":"+(currentTime-(3120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_C\", \"LABEL\":\"Some Very Long Label with blanks for breaks\", \"STATUS\":\"ABNORMAL ENDING\", \"END_TIME\":"+(currentTime-(120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0_A\", \"LABEL\":\"JP__A\", \"STATUS\":\"WAITING\", \"END_TIME\":"+(currentTime-(120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_226\", \"LABEL\":\"JP_0003_226\", \"STATUS\":\"OVERDUE (UNKNOWN)\", \"END_TIME\":"+(currentTime-(4120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0002_B\", \"LABEL\":\"Job B\", \"STATUS\":\"BLOCKED\", \"END_TIME\":"+(currentTime-(120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_234\", \"LABEL\":\"Crazy Job\", \"STATUS\":\"ABNORMAL ENDING\", \"END_TIME\":"+(currentTime-(9120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_V\", \"LABEL\":\"JP_0003_V\", \"STATUS\":\"ENDED OK\", \"END_TIME\":"+(currentTime-(34120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_Chjkl\", \"LABEL\":\"The Holy C\", \"STATUS\":\"ENDED OK\", \"END_TIME\":"+(currentTime-(120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_A\", \"LABEL\":\"JP_0003_A\", \"STATUS\":\"WAITING\", \"END_TIME\":"+(currentTime-(1220*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_225fksdfghuw\", \"LABEL\":\"JP_0003_225fksdfghuw\", \"STATUS\":\"ENDED OK\", \"END_TIME\":"+(currentTime-(120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0002_B\", \"LABEL\":\"Job B\", \"STATUS\":\"ENDED OK\", \"END_TIME\":"+(currentTime-(120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0004_725\", \"LABEL\":\"Crazy Job\", \"STATUS\":\"ABNORMAL ENDING\", \"END_TIME\":"+(currentTime-(620*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_Vhjklh\", \"LABEL\":\"JP_0003_Vhjklh\", \"STATUS\":\"ENDED OK\", \"END_TIME\":"+(currentTime-(120*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_01\", \"LABEL\":\"JP_01_WITH_URL\", \"STATUS\":\"ABNORMAL ENDING\", \"URL\":\"https://somelink.toawainstance.yourcompany.com\", \"END_TIME\":"+(currentTime-(1440*60000))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_A\", \"LABEL\":\"JP_0003_A\", \"STATUS\":\"OVERDUE (ABNORMAL ENDING)\", \"END_TIME\":"+(currentTime-(120*60000))+"}"
			+"]");

		return element.getAsJsonArray();
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureAWA.PACKAGE_RESOURCE, "emp_awa_commonFunctions.js") );
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureAWA.PACKAGE_RESOURCE, "emp_widget_awajobstatus_current.js") );
		return array;
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	@Override
	public ArrayList<FileDefinition> getCSSFiles() {
		return null;
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureAWA.PACKAGE_RESOURCE, "lang_en_emp_awa.properties"));
		return map;
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureAWA.PERMISSION_WIDGETS_AWA);
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public boolean supportsTask() {
		return true;
	}
	
	/************************************************************
	 * Override this method to return a description of what the
	 * task of this widget does.
	 ************************************************************/
	public String getTaskDescription() {
		return "Checks if any of the selected jobs ends with an issue.";
	}
	
	/************************************************************
	 * Override this method and return a CFWObject containing 
	 * fields for the task parameters. The settings will be passed 
	 * to the 
	 * Always return a new instance, do not reuse a CFWObject.
	 * @return CFWObject
	 ************************************************************/
	public CFWObject getTasksParameters() {
		return new CFWJobsAlertObject(false);
	}
	
	/*************************************************************************
	 * Implement the actions your task should execute.
	 * See {@link com.xresch.cfw.features.jobs.CFWJobTask#executeTask CFWJobTask.executeTask()} to get
	 * more details on how to implement this method.
	 *************************************************************************/
	public void executeTask(JobExecutionContext context, CFWObject taskParams, DashboardWidget widget, CFWObject settings, CFWMonitor monitor, CFWTimeframe offset) throws JobExecutionException {
		
		//----------------------------------------
		// Fetch Data
		JsonArray dataArray;
		Boolean isSampleData = (Boolean)settings.getField(WidgetSettingsFactory.FIELDNAME_SAMPLEDATA).getValue();
		if(isSampleData != null && isSampleData) {
			dataArray = createSampleData();
		}else {
			dataArray = loadDataFromAwaAsJsonArray(settings, offset.getEarliest(), offset.getLatest());
		}
		
		if(dataArray == null) {
			return;
		}
		
		//----------------------------------------
		// Check Condition
		boolean conditionMatched = false;
		ArrayList<JsonObject> jobsWithIssues = new ArrayList<>();
		for(JsonElement element : dataArray) {
			JsonObject current = element.getAsJsonObject();
			String status = current.get("STATUS").getAsString();
			if(status != null && status.equals("ABNORMAL ENDING")) {
				conditionMatched = true;
				jobsWithIssues.add(current);
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
				String joblistText = "";
				String joblistHTML = "<ul>";
				for(JsonObject current : jobsWithIssues) {
					
					String jobname = current.get("JOBNAME").getAsString();
					String label = current.get("LABEL").getAsString();
					if(!label.equals(jobname)) {
						jobname += " ("+label+")"; 
					}
					JsonElement url = current.get("URL");
					
					joblistText += jobname+", ";
					
					if(url == null || url.isJsonNull()) {
						joblistHTML += "<li>"+jobname+"</li>";
					}else {
						joblistHTML += "<li><a href=\""+url.getAsString()+"\">"+jobname+"</a></li>";
					}
				}
				joblistText = joblistText.substring(0, joblistText.length()-2);
				joblistHTML+="</ul>";
				
				//----------------------------------------
				// Create Message

				String message = "The following job(s) ended with one or more issues: "+joblistText;
				String messageHTML = "<p>The following job(s) ended with one or more issues:</p>";
				messageHTML += joblistHTML;
				messageHTML += linkHTML;
				
				CFW.Messages.addErrorMessage(message);
				
				alertObject.doSendAlert(context, MessageType.ERROR, "EMP: Alert - AWA job(s) ended with issues", message, messageHTML);
			}
			
			//----------------------------------------
			// RESOLVE
			if(type.equals(AlertType.RESOLVE)) {
				String message = CFW.Random.randomIssueResolvedMessage();
				String messageHTML = "<p>"+message+"</p>"+linkHTML;
				
				CFW.Messages.addSuccessMessage("Issue has resolved.");
				alertObject.doSendAlert(context, MessageType.SUCCESS, "EMP: Resolved - AWA Job Status is fine again.", message, messageHTML);
			}
		}
	}
	
}
