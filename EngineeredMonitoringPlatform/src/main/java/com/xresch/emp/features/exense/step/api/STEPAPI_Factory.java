package com.xresch.emp.features.exense.step.api;

import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.xresch.cfw._main.CFW.Registry.API;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionJsonArray;
import com.xresch.cfw.features.api.APIExecutorJsonArray;
import com.xresch.emp.features.exense.step.StepEnvironment;
import com.xresch.emp.features.exense.step.StepEnvironmentManagement;

public class STEPAPI_Factory {
	
	public static final String API_CATEGORY = "ExenseStep";
	
	public static final String INPUT_ENVIRONMENT = "ENVIRONMENT";
	public static final String INPUT_EXECUTIONS = "EXECUTIONS";
	public static final String INPUT_TIMEFRAME = "JSON_TIMEFRAME";
	public static final String INPUT_SCHEDULER_ID = "SCHEDULERID";


	//-------------------------------------------------
	// General Return Fields
	//-------------------------------------------------
	private static final CFWField<Long> RETURN_STARTTIME =
			CFWField.newLong(FormFieldType.NONE, "starttime" )
				.setDescription("The start time of the execution (unix timestamp, milliseconds since 01.01.1970).");
	
	private static final CFWField<Long> RETURN_ENDTIME =
			CFWField.newLong(FormFieldType.NONE, "endtime" )
			.setDescription("The end time of the execution (unix timestamp, milliseconds since 01.01.1970).");
	
	private static final CFWField<Long> RETURN_DURATION =
			CFWField.newLong(FormFieldType.NONE, "duration" )
				.setDescription("The duration in milliseconds.");
		
	private static final CFWField<String> RETURN_SCHEDULER_ID =
			CFWField.newString(FormFieldType.NONE, "schedulerid" )
				.setDescription("The id of the STEP scheduler.");
	
	private static final CFWField<String> RETURN_SCHEDULER_NAME =
			CFWField.newString(FormFieldType.NONE, "schedulername" )
				.setDescription("The name of the STEP scheduler.");
	
	private static final CFWField<Integer> RETURN_PLAN_ID =
			CFWField.newInteger(FormFieldType.NONE, "panid" )
				.setDescription("The id of the STEP plan.");
	
	private static final CFWField<String> RETURN_PLAN_NAME =
			CFWField.newString(FormFieldType.NONE, "planname" )
				.setDescription("The name of the STEP plan.");
	
	private static final CFWField<Integer> RETURN_PROJECT_ID =
			CFWField.newInteger(FormFieldType.NONE, "projectid" )
				.setDescription("The id of the STEP project.");
	
	private static final CFWField<String> RETURN_PROJECT_NAME =
			CFWField.newString(FormFieldType.NONE, "projectname" )
				.setDescription("The name of the STEP project.");
		
	private static final CFWField<String> RETURN_RESULT =
			CFWField.newString(FormFieldType.NONE, "result" )
				.setDescription("The result of the STEP execution.");
	
	private static final CFWField<String> RETURN_STATUS =
			CFWField.newString(FormFieldType.NONE, "status" )
				.setDescription("The status of the STEP execution.");
	

	private STEPAPI_Factory() {
		/*hide public constructor */
	}
	
	/***************************************************************************
	 * 
	 ***************************************************************************/
	public static ArrayList<APIDefinition> getAPIDefinitions() {

		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
		
		apis.add(createGetLastExecutionsForScheduler());
		

		return apis;
	}


	/***************************************************************************
	 * 
	 ***************************************************************************/
	private static APIDefinitionJsonArray createGetLastExecutionsForScheduler() {
	
		APIDefinitionJsonArray apiDef = 
				new APIDefinitionJsonArray(
						STEPAPI_Input_GetLastExecutionsForScheduler.class,
						API_CATEGORY,
						"getLastExecutionsForScheduler",
						new String[] {
								  INPUT_ENVIRONMENT
								, INPUT_SCHEDULER_ID
								, INPUT_TIMEFRAME
								, INPUT_EXECUTIONS 
							}
				);
				
		apiDef.setDescription("Returns the current status for the selected monitor.");
			
		apiDef.addOutputFields(
				  RETURN_PROJECT_ID
				, RETURN_PROJECT_NAME
				, RETURN_SCHEDULER_NAME
				, RETURN_SCHEDULER_ID
				, RETURN_PLAN_ID
				, RETURN_PLAN_NAME
				, RETURN_STATUS
				, RETURN_RESULT
				, RETURN_DURATION
				, RETURN_STARTTIME
				, RETURN_ENDTIME
			);
		
		APIExecutorJsonArray executor = new APIExecutorJsonArray() {
			@Override
			public JsonArray execute(APIDefinitionJsonArray definition, CFWObject instance) {
				
				
				STEPAPI_Input_GetLastExecutionsForScheduler inputs = (STEPAPI_Input_GetLastExecutionsForScheduler)instance;
				
				StepEnvironment env = StepEnvironmentManagement.getEnvironment(inputs.environmentID());
				CFWTimeframe tiemframe = inputs.timeframe();
				
				return env.getSchedulerLastNExecutions(
						  inputs.schedulerID()
						, inputs.executions()
						, tiemframe.getEarliest()
						, tiemframe.getLatest()
					);

			}
		};
			
			
		apiDef.setExecutor(executor);
		return apiDef;
	}
	

}
