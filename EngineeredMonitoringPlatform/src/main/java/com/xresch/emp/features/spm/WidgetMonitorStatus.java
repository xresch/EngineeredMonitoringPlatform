package com.xresch.emp.features.spm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.db.DBInterface;
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
import com.xresch.cfw.validation.NumberRangeValidator;

public class WidgetMonitorStatus extends WidgetDefinition {

	private static final String PROJECT_URL = "PROJECT_URL";
	private static final String IS_PROJECT_ACTIVE = "IS_PROJECT_ACTIVE";
	private static final String IS_MONITOR_ACTIVE = "IS_MONITOR_ACTIVE";
	private static final String VALUE = "VALUE";
	private static final String LOCATION_NAME = "LOCATION_NAME";
	private static final String MEASURE_NAME = "MEASURE_NAME";
	private static final String PROJECT_NAME = "PROJECT_NAME";
	private static final String PROJECT_ID = "PROJECT_ID";
	private static final String MONITOR_NAME = "MONITOR_NAME";
	private static final String MONITOR_ID = "MONITOR_ID";
	
	private static final String FIELDNAME_THRESHOLD_PERCENT = "THRESHOLD_PERCENT";
	
	private static Logger logger = CFWLog.getLogger(WidgetMonitorStatus.class.getName());
	
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String getWidgetType() {return "emp_spmmonitorstatus";}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public WidgetDataCachePolicy getCachePolicy() {
		return WidgetDataCachePolicy.ALWAYS;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String widgetCategory() {
		return FeatureSPM.WIDGET_CATEGORY_SPM;
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String widgetName() { return "Monitor Status"; }
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureSPM.PACKAGE_MANUAL, "widget_"+getWidgetType()+".html");
	}
		
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public CFWObject getSettings() {
		return createMonitorSelectionFields()
				.addField(WidgetSettingsFactory.createDefaultDisplayAsField())				
				.addAllFields(WidgetSettingsFactory.createTilesSettingsFields())
				.addField(WidgetSettingsFactory.createDisableBoolean())
				.addField(WidgetSettingsFactory.createSampleDataField())
		;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	public CFWObject createMonitorSelectionFields() {
		return new CFWObject()
				.addField(SPMSettingsFactory.createEnvironmentSelectorField())
				.addField(SPMSettingsFactory.createMonitorsSelectorField())
				.addField(SPMSettingsFactory.createMeasureSelectField())
		;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings, CFWTimeframe timeframe) { 
		
		//---------------------------------
		// Example Data
		Boolean isSampleData = (Boolean)settings.getField(WidgetSettingsFactory.FIELDNAME_SAMPLEDATA).getValue();
		if(isSampleData != null && isSampleData) {
			response.setPayload(createSampleData());
			return;
		}
		
		//---------------------------------
		// Real Data		
		response.setPayload(loadDataFromSPM(settings));
						
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@SuppressWarnings("unchecked")
	public JsonArray loadDataFromSPM(CFWObject widgetSettings){
		//---------------------------------
		// Resolve Jobnames
		
		LinkedHashMap<String,String> monitorsMap = (LinkedHashMap<String,String>)widgetSettings.getField(SPMSettingsFactory.FIELDNAME_MONITORS).getValue();
		if(monitorsMap == null || monitorsMap.isEmpty()) {
			return null;
		}
		
		//---------------------------------
		// Get Environment
		String environmentID = (String)widgetSettings.getField(SPMSettingsFactory.FIELDNAME_ENVIRONMENT).getValue();
		EnvironmentSPM environment;
		if(environmentID != null) {
			environment = EnvironmentManagerSPM.getEnvironment(Integer.parseInt(environmentID));
		}else {
			return null;
		}
		
		
		//---------------------------------
		// Get Database
		DBInterface db;
		db = environment.getDBInstance();
		
		if(db == null) {
			CFW.Messages.addWarningMessage("SPM Monitor Status: The db of the chosen environment seems not configured correctly.");
			return null;
		}
		
		//---------------------------------
		// Get Measure
		String measureName = (String)widgetSettings.getField(SPMSettingsFactory.FIELDNAME_MEASURE).getValue();
		if(Strings.isNullOrEmpty(measureName)) {
			measureName = "Overall Health";
		}
		
		//---------------------------------
		// Fetch Data
		JsonArray resultArray = new JsonArray();
		for(Entry<String, String> entry : monitorsMap.entrySet()) {
			
			String monitorID = entry.getKey().trim();
			ResultSet result = db.preparedExecuteQuerySilent(
					CFW.Files.readPackageResource(FeatureSPM.PACKAGE_RESOURCE, "emp_widget_spmmonitorstatus.sql"),
					measureName,
					entry.getKey().trim());
			ResultSet nameResult = null;
			
			try {
				if(result != null && result.next()) {
					//--------------------------------
					// Return Status
					JsonObject object = new JsonObject();
					object.addProperty(MONITOR_ID, monitorID);
					object.addProperty(MONITOR_NAME, result.getString("MonitorName"));
					object.addProperty(PROJECT_ID, result.getString("ProjectID"));
					object.addProperty(PROJECT_NAME, result.getString("ProjectName"));
					object.addProperty(MEASURE_NAME, result.getString("MeasureName"));
					object.addProperty(LOCATION_NAME, result.getString("LocationName"));
					object.addProperty(VALUE, result.getInt("Value"));
					
					String url = environment.url().trim();
					if( !Strings.isNullOrEmpty(url) ) {
						if( !url.startsWith("http")) { url = "http://"+url; }
						if(url.endsWith("/")) { url = url.substring(0, url.length()-1); }
						object.addProperty(PROJECT_URL, url+"/silk/DEF/Monitoring/Monitoring?pId="+result.getString("ProjectID"));
					}
					
					resultArray.add(object);
				}else {
					
					//--------------------------------
					// Return No Data as -1
					nameResult = db.preparedExecuteQuerySilent(
							CFW.Files.readPackageResource(FeatureSPM.PACKAGE_RESOURCE, "emp_widget_spmmonitordetails.sql"),
							monitorID);
					
					if(nameResult != null && nameResult.next()) {
						
						JsonObject object = new JsonObject();
						object.addProperty(MONITOR_ID, monitorID);
						object.addProperty(MONITOR_NAME, nameResult.getString("MonitorName"));
						object.addProperty(PROJECT_ID, nameResult.getString("ProjectID"));
						object.addProperty(PROJECT_NAME, nameResult.getString("ProjectName"));
						object.addProperty(IS_MONITOR_ACTIVE, (nameResult.getInt("MonitorIsActive") == 1) ? true : false);
						object.addProperty(IS_PROJECT_ACTIVE, (nameResult.getInt("ProjectIsActive") == 1) ? true : false);
						object.addProperty(VALUE, -1);
						
						String url = environment.url().trim();
						if( !Strings.isNullOrEmpty(url) ) {
							if( !url.startsWith("http")) { url = "http://"+url; }
							if(url.endsWith("/")) { url = url.substring(0, url.length()-1); }
							object.addProperty(PROJECT_URL, url+"/silk/DEF/Monitoring/Monitoring?pId="+nameResult.getString("ProjectID"));
						}
						
						resultArray.add(object);
					}else {
						//--------------------------------
						// Return Error as -2
						JsonObject object = new JsonObject();
						object.addProperty(MONITOR_ID, monitorID);
						object.addProperty(MONITOR_NAME, "Not Found");
						object.addProperty(PROJECT_ID, "Unknown");
						object.addProperty(PROJECT_NAME, "Unknown");
						object.addProperty(VALUE, -2);
						resultArray.add(object);
					}
				}
			} catch (SQLException e) {
				new CFWLog(logger)
					.severe("Error fetching Widget data.", e);
			}finally {
				db.close(result);
				db.close(nameResult);
			}
		}
		
		return resultArray;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	public JsonArray createSampleData() { 

		JsonArray resultArray = new JsonArray();
		
		String[] monitorNames = new String[] {
				"[AAA] Test Monitor", "[BUM] B&auml;ng T&auml;tsch", "[FOO] Foobar", 
				"[PAN] Page Analyzer", "[BMW] Bayrischer Mist Wagen", "[Short] Cut",
				"Just a Monitor", "[Rick] Roll that Astley! ", "ABC",
				"DEV", "TEST", "PROD", };
		for(int i = 0; i < monitorNames.length; i++) {
			String name = monitorNames[i];
			//--------------------------------
			// Return Status
			JsonObject object = new JsonObject();
			object.addProperty(MONITOR_ID, i);
			object.addProperty(MONITOR_NAME, name);
			object.addProperty(PROJECT_ID, i);
			object.addProperty(PROJECT_NAME, "Pseudo Project");
			object.addProperty(MEASURE_NAME, "Overall Health");
			object.addProperty(LOCATION_NAME, "Winterthur");
			object.addProperty(VALUE, (Math.random() > 0.7) ? 100 : Math.ceil(Math.random()*99));
			object.addProperty(PROJECT_URL, "http://spm.just-an-example.com/silk/DEF/Monitoring/Monitoring?pId="+i);
			
			resultArray.add(object);
				
		}
		
		return resultArray;
		
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureSPM.PACKAGE_RESOURCE, FeatureSPM.FILE_PATH_COMMON_JS) );
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureSPM.PACKAGE_RESOURCE,  "emp_widget_spmmonitorstatus.js") );
		return array;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public ArrayList<FileDefinition> getCSSFiles() {
		return null;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureSPM.PACKAGE_RESOURCE, "lang_en_emp_spm.properties"));
		return map;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureSPM.PERMISSION_WIDGETS_SPM);
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	public boolean supportsTask() {
		return true;
	}
	
	/************************************************************
	 * Override this method to return a description of what the
	 * task of this widget does.
	 ************************************************************/
	public String getTaskDescription() {
		return "Checks if any of the selected monitors  ends with an issue.";
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
				CFWField.newInteger(FormFieldType.NUMBER, FIELDNAME_THRESHOLD_PERCENT)
				.setDescription("Threshhold in percent, trigger event when a value of one selected monitor falls below this value.")
				.addValidator(new NumberRangeValidator(1, 100).setNullAllowed(false))
				.setValue(50)
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
		JsonArray dataArray;
		Boolean isSampleData = (Boolean)settings.getField(WidgetSettingsFactory.FIELDNAME_SAMPLEDATA).getValue();
		if(isSampleData != null && isSampleData) {
			dataArray = createSampleData();
		}else {
			dataArray = loadDataFromSPM(settings);
		}
		
		if(dataArray == null) {
			return;
		}
		
		
		//----------------------------------------
		// Get Percentage
		Integer thresholdPercent = (Integer)taskParams.getField(FIELDNAME_THRESHOLD_PERCENT).getValue();
		
		if(thresholdPercent == null ) {
			return;
		}
		
		//----------------------------------------
		// Check Condition
		boolean conditionMatched = false;
		ArrayList<JsonObject> monitorsWithIssues = new ArrayList<>();
		for(JsonElement element : dataArray) {
			JsonObject current = element.getAsJsonObject();
			Integer value = current.get(VALUE).getAsInt();
			if(value != null 
			&& value >= 0 
			&& value < thresholdPercent) {
				conditionMatched = true;
				monitorsWithIssues.add(current);
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
				String monitorlistText = "";
				String monitorlistHTML = "<ul>";
				for(JsonObject current : monitorsWithIssues) {
					
					String projectName = current.get(PROJECT_NAME).getAsString();
					String monitorName = current.get(MONITOR_NAME).getAsString();

					
					JsonElement url = current.get(PROJECT_URL);
					
					monitorlistText +=  projectName +" >> "+ monitorName+", ";
					
					if(url == null || url.isJsonNull()) {
						monitorlistHTML += "<li>"+projectName+" &gt;&gt; "+monitorName+"</li>";
					}else {
						monitorlistHTML += "<li><a href=\""+url.getAsString()+"\">"+projectName+" &gt;&gt; "+monitorName+"</a></li>";
					}
				}
				monitorlistText = monitorlistText.substring(0, monitorlistText.length()-2);
				monitorlistHTML+="</ul>";
				
				//----------------------------------------
				// Create Message
				String baseMessage = "The following monitor(s) are below the threshhold of "+thresholdPercent+"%:";
				String messagePlaintext = baseMessage+" "+monitorlistText;
				String messageHTML = "<p>"+baseMessage+"</p>";
				messageHTML += monitorlistHTML;
				messageHTML += linkHTML;
				
				CFW.Messages.addErrorMessage(messagePlaintext);
				
				alertObject.doSendAlert(context, MessageType.INFO, "EMP: Alert - SPM monitor(s) below threshold", messagePlaintext, messageHTML);
			}
			
			//----------------------------------------
			// RESOLVE
			if(type.equals(AlertType.RESOLVE)) {
				String message = CFW.Random.randomIssueResolvedMessage();
				String messageHTML = "<p>"+message+"</p>"+linkHTML;
				
				CFW.Messages.addSuccessMessage("Issue has resolved.");
				alertObject.doSendAlert(context, MessageType.INFO, "EMP: Resolved - SPM monitor(s) are above threshold.", message, messageHTML);
			}
		}
	}

}
