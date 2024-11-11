package com.xresch.emp.features.exense.step;

import java.util.ArrayList;

import org.quartz.JobExecutionContext;

import com.google.common.base.Enums;
import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.dashboard.DashboardWidget;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject.AlertType;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.utils.CFWState.CFWStateOption;
import com.xresch.emp.features.exense.step.FeatureExenseStep.StepExecutionResult;

public class StepCommonFunctions {

	
	/**
	 * @param widgetType *******************************************************************
	 * 
	 *********************************************************************/
	public static void defaultStatusAlerting(
			JobExecutionContext context
			, CFWObject taskParams
			, DashboardWidget widget
			, String widgetType
			, CFWObject settings
			, String stepURL
			, JsonArray resultArray) {
		//----------------------------------------
		// Get alertThreshhold
		String alertThreshholdString = (String)taskParams.getField(CFW.Conditions.FIELDNAME_ALERT_THRESHOLD).getValue();
		
		if(alertThreshholdString == null ) {
			return;
		}
	
		CFWStateOption alertThreshholdCondition = CFWStateOption.valueOf(alertThreshholdString);
		
		//----------------------------------------
		// Check Condition
		boolean conditionMatched = false;
		ArrayList<JsonObject> recordsExceedingThreshold = new ArrayList<>();
		
		for(JsonElement element : resultArray) {
			
			JsonObject current = element.getAsJsonObject();
			Float duration = current.get("duration").getAsFloat();
			String result = current.get("result").getAsString();
			
			//--------------------------------------
			// Ignore any unknown status
			if(!Enums.getIfPresent(StepExecutionResult.class, result).isPresent()) {
				continue;
			}
			
			
			//--------------------------------------
			// Get condition for step result
			StepExecutionResult execResult = StepExecutionResult.valueOf(result);
			
			CFWStateOption condition = CFWStateOption.NONE;
			switch(execResult) {
				case PASSED: 			condition = CFW.Conditions.getConditionForValue(duration, settings);
										break;
				
				case FAILED:
				case IMPORT_ERROR:
				case TECHNICAL_ERROR:	condition = CFWStateOption.RED;
										break;
					
					
				case SKIPPED:
				case VETOED:
				case INTERRUPTED:		condition = CFWStateOption.ORANGE;
					break;

					
				case NORUN:
				case RUNNING:
				default:				condition = CFWStateOption.NONE;
										break;
			
			}
			
			//--------------------------------------
			// Evaluate condition
			if(condition != null 
			&& CFW.Conditions.compareIsEqualsOrMoreDangerous(alertThreshholdCondition, condition)) {
				conditionMatched = true;
				recordsExceedingThreshold.add(current);
			}

		}
				
		//----------------------------------------
		// Handle Alerting
		CFWJobsAlertObject alertObject = new CFWJobsAlertObject(context, widgetType, false);
	
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
				// Create Table Header
				String tableHeader = """ 
						<tr> \
						<td>Project</td>\
						<td>Plan</td>\
						<td>Scheduler</td>\
						<td>Status</td>\
						<td>Result</td>\
						<td>Duration</td>\
						</tr>""";

				//----------------------------------------
				// Create Job List 
				String metricListText = "";
				String metricTableHTML = "<table class=\"table table-sm table-striped\" border=1 cellspacing=0 cellpadding=5>"+tableHeader;
				
				for(JsonObject current : recordsExceedingThreshold) {
					
					String projectname = current.get("projectname").getAsString();
					String planname = current.get("planname").getAsString();
					String planid = current.get("planid").getAsString();
					String schedulerid = current.get("schedulerid").getAsString();
					String schedulername = current.get("schedulername").getAsString();
					String status = current.get("status").getAsString();
					String result = current.get("result").getAsString();
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
					String projectCell = "&nbsp;";
					if(!Strings.isNullOrEmpty(projectname)) {
						projectCell = "<a target=\"_blank\" href=\""
											+stepURL+"#/root/plans/list?tenant="
											+CFW.HTTP.encode(projectname)
											+"\">"+projectname+"</a>";
					}
					
					String planCell = "&nbsp;";
					if(!Strings.isNullOrEmpty(projectname)) {
						planCell = "<a target=\"_blank\" href=\""
										+stepURL+"#/root/plans/editor/"+planid
										+"?tenant="+CFW.HTTP.encode(projectname)
										+"\">"+planname+"</a>";
					}
					
					String schedulerCell = "&nbsp;";
					if(!Strings.isNullOrEmpty(projectname)) {
						schedulerCell = "<a target=\"_blank\" href=\""
											+stepURL+"#/root/analytics?taskId="
											+schedulerid+"&refresh=1&relativeRange=86400000&tsParams=taskId,refresh,relativeRange&tenant="
											+CFW.HTTP.encode(projectname)
											+"\">"+schedulername+"</a>";
					}
					
					
					String row = """
							<tr>
							<td>%s</td>\
							<td>%s</td>\
							<td>%s</td>\
							<td>%s</td>\
							<td>%s</td>\
							<td>%s ms</td>\
							</tr>""".formatted(projectCell
										, planCell
										, schedulerCell
										, status
										, result
										, duration
									)
						;
					metricTableHTML += row;
				}
				
				metricListText = metricListText.substring(0, metricListText.length()-3);
				metricTableHTML+="</table>";
				
				//----------------------------------------
				// Create Message
				String baseMessage = "The following record(s) have reached the threshold "+alertThreshholdString+":";
				String messagePlaintext = baseMessage+" "+metricListText;
				String messageHTML = "<p>"+baseMessage+"</p>";
				messageHTML += metricTableHTML;
				messageHTML += widgetLinkHTML;

				CFW.Messages.addErrorMessage(messagePlaintext);
				
				alertObject.addTextData("data", "csv", CFW.JSON.toCSV(resultArray, ";") );
				alertObject.addTextData("data", "json", CFW.JSON.toJSONPretty(resultArray) );
				alertObject.doSendAlert(context, MessageType.ERROR, "EMP: Alert - Step scheduler(s) reached threshold", messagePlaintext, messageHTML);
				
			}
			
			//----------------------------------------
			// RESOLVE
			if(type.equals(AlertType.RESOLVE)) {
				String message = CFW.Random.randomIssueResolvedMessage();
				String messageHTML = "<p>"+message+"</p>"+widgetLinkHTML;
				
				CFW.Messages.addSuccessMessage("Issue has resolved.");
				alertObject.doSendAlert(context, MessageType.SUCCESS, "EMP: Resolved - Step scheduler(s) below threshold", message, messageHTML);
			}
		}
	}

	
	/**
	 * @param planCount TODO*******************************************************************
	 * 
	 *********************************************************************/
	public static JsonArray defaultStepStatusExampleData(int planCount) {
			
	//		{
	//		"projectid": "62444fadee10d74e1b1395af",
	//		"projectname": "AnotherTestProject",
	//		"planid": "62694470ee10d74e1b26d744",
	//		"planname": "Bla Bla Bla",
	//		"schedulerid": "626944feee10d74e1b26df94",
	//		"schedulername": "Bla Bla Bla",
	//		"status": "ENDED",
	//		"result": "PASSED",
	//		"duration": 37,
	//		"starttime": 1651134840013,
	//		"endtime": 1651134840050
	//	},
			JsonArray array = new JsonArray();
			
			for(int i = 0 ; i < planCount; i++) {
				String alphas = ("["+CFW.Random.randomStringAlphaNumerical(3)+"] ").toUpperCase();
				String randomProject = CFW.Random.randomFromArray(new String[] {"Project Omega", "Project Alpha", "Project Epsilon"});
				String randomPlan = CFW.Random.randomFromArray(new String[] {"Test Plan", "Ricks Plan Rolls", "Plan Ahead", "Plan of a Lifetime", "No Plan", "Plan Tage", "Plan E", "Plan ET"});
				String randomResult = CFW.Random.randomFromArray(new String[] {"PASSED", "PASSED", "PASSED", "PASSED", "PASSED", "PASSED", "PASSED", "PASSED", "PASSED", "PASSED", "PASSED",
																				"FAILED", "VETOED", "TECHNICAL_ERROR", "IMPORT_ERROR", "SKIPPED", "INTERRUPTED", "NORUN", "RUNNING"});
				
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
				object.addProperty("starttime", CFW.Time.getCurrentTimestampWithOffset(0, 0, 0, 0, offsetMinutes*offsetMultiplier).getTime());
				object.addProperty("endtime",  CFW.Time.getCurrentTimestampWithOffset(0, 0, 0, 0, offsetMinutes).getTime());
				
				array.add(object);
			}
			return array;
		}

	/*********************************************************************
	 * 
	 *********************************************************************/
	public static JsonArray defaultStepSeriesExampleData(int seriesCount, int valuesCount, long earliest, long latest) { 
		JsonArray array = new JsonArray();
		
		long timerange = latest - earliest;
		long timestep = timerange / valuesCount;
		
		for(int j = 0; j < seriesCount; j++) {
			
			String alphas = ("["+CFW.Random.randomStringAlphaNumerical(3)+"] ").toUpperCase();
			String randomProject = CFW.Random.randomFromArray(new String[] {"Project Omega", "Project Alpha", "Project Epsilon"});
			String randomPlan = CFW.Random.randomFromArray(new String[] {"Test Plan", "Ricks Plan Rolls", "Plan Ahead", "Plan of a Lifetime", "No Plan", "Plan Tage", "Plan E", "Plan ET"});
			
			for(int i = 0 ; i < 24; i++) {
				String randomResult = CFW.Random.randomFromArray(new String[] {"PASSED", "PASSED", "PASSED", "PASSED", "PASSED", "FAILED", "TECHNICAL_ERROR", "RUNNING"});
				
				int duration = CFW.Random.randomIntegerInRange(10, 12000);
				JsonObject object = new JsonObject();
				object.addProperty("projectid", "62444fadee10d74e1b1395af");
				object.addProperty("projectname", alphas+randomProject);
				object.addProperty("planid", CFW.Random.randomStringAlphaNumerical(24).toLowerCase());
				object.addProperty("planname",  alphas+randomPlan);
				object.addProperty("schedulerid", CFW.Random.randomStringAlphaNumerical(24).toLowerCase());
				object.addProperty("schedulername", alphas+"Scheduler for "+randomPlan);
				object.addProperty("status", "ENDED");
				object.addProperty("result", randomResult);
				object.addProperty("duration",  duration);
				object.addProperty("starttime", earliest+(timestep*i)-duration);
				object.addProperty("endtime",  earliest+(timestep*i));
				
				array.add(object);
			}
		}
		
		return array;
	}

	public static StepEnvironment resolveEnvironmentFromWidgetSettings(CFWObject settings) {
		//-----------------------------
		// Resolve Environment ID
		String environmentString = (String)settings.getField(StepSettingsFactory.FIELDNAME_ENVIRONMENT).getValue();
	
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

}
