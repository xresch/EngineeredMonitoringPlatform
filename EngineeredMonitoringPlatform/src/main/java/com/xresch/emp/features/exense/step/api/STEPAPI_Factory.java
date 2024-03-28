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
	private static final CFWField<Long> RETURN_TIMESTAMP =
			CFWField.newLong(FormFieldType.NONE, "Timestamp" )
				.setDescription("The time of the execution log (unix timestamp, milliseconds since 01.01.1970).");
	
	private static final CFWField<Long> RETURN_SERIESTIME =
			CFWField.newLong(FormFieldType.NONE, "SeriesTime" )
				.setDescription("The time when the measure was taken(unix timestamp, milliseconds since 01.01.1970).");
	
	private static final CFWField<Integer> RETURN_AGGREGATION =
			CFWField.newInteger(FormFieldType.NONE, "Aggregation" )
				.setDescription("The aggregation interval in minutes represented by this value. 0 if there was no aggregation.");
	
	private static final CFWField<Integer> RETURN_RK =
			CFWField.newInteger(FormFieldType.NONE, "rk" )
				.setDescription("Ranking value(used internally only).");
	
	private static final CFWField<Float> RETURN_VALUE =
			CFWField.newFloat(FormFieldType.NONE, "Value" )
				.setDescription("The average measure value in percentage ranging from 0.0(worst) to 100.0(best).");
	
	private static final CFWField<Integer> RETURN_MONITOR_ID =
			CFWField.newInteger(FormFieldType.NONE, "MonitorID" )
				.setDescription("The id of the monitor.");
	
	private static final CFWField<String> RETURN_MONITOR_NAME =
			CFWField.newString(FormFieldType.NONE, "MonitorName" )
				.setDescription("The name of the monitor.");
	
	private static final CFWField<Integer> RETURN_PROJECT_ID =
			CFWField.newInteger(FormFieldType.NONE, "ProjectID" )
				.setDescription("The id of the project.");
	
	private static final CFWField<String> RETURN_PROJECT_NAME =
			CFWField.newString(FormFieldType.NONE, "ProjectName" )
				.setDescription("The name of the project.");
		
	private static final CFWField<Integer> RETURN_LOCATION_ID =
			CFWField.newInteger(FormFieldType.NONE, "LocationID" )
				.setDescription("The id of the location.");
	
	private static final CFWField<String> RETURN_LOCATION_NAME =
			CFWField.newString(FormFieldType.NONE, "LocationName" )
				.setDescription("The name of the location defined in SPM.");
	
	private static final CFWField<String> RETURN_MEASURE_NAME =
			CFWField.newString(FormFieldType.NONE, "MeasureName" )
				.setDescription("The name of the measure.");
	//-------------------------------------------------
	// Execution Log Return Fields
	//-------------------------------------------------
	private static final CFWField<String> RETURN_LOG_MESSAGE =
			CFWField.newString(FormFieldType.NONE, "LogMessage" )
				.setDescription("The message of the execution log.");
	
	private static final CFWField<String> RETURN_STATUS =
			CFWField.newString(FormFieldType.NONE, "Status" )
				.setDescription("The status of the execution. Tells if the script was executed successfully. Does not give a status if the monitored application is working.");
	
	//-------------------------------------------------
	// Service Target Return Fields
	//-------------------------------------------------
	private static final CFWField<String> RETURN_RULE =
			CFWField.newString(FormFieldType.NONE, "Rule" )
				.setDescription("The name of the rule.");	
	
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
				, RETURN_MONITOR_ID
				, RETURN_MONITOR_NAME
				, RETURN_LOCATION_ID
				, RETURN_LOCATION_NAME
				, RETURN_MEASURE_NAME
				, RETURN_SERIESTIME
				, RETURN_AGGREGATION
				, RETURN_VALUE
				, RETURN_RK
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
