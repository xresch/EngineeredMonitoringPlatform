package com.xresch.emp.features.spm;

import java.sql.ResultSet;
import java.util.ArrayList;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionSQL;
import com.xresch.cfw.features.api.APIExecutorSQL;

public class API_Factory {
	
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
	
	private API_Factory() {
		/*hide public constructor */
	}
	
	/***************************************************************************
	 * 
	 ***************************************************************************/
	public static ArrayList<APIDefinition> getAPIDefinitions() {

		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
		
		apis.add(createGetExecutionLogsForProject());
		apis.add(createGetExecutionLogsForMonitor());
		apis.add(createGetServiceTargetInfoForProject());
		apis.add(createGetServiceTargetViolationsForProject());
		apis.add(createGetStatusForMonitor());
		apis.add(createGetStatusForProject());
		apis.add(createGetStatusForProjectMonitors());

		return apis;
	}

	/***************************************************************************
	 * 
	 ***************************************************************************/
	private static APIDefinitionSQL createGetExecutionLogsForProject() {

		APIDefinitionSQL apiDef = 
				new APIDefinitionSQL(
						API_Input_EnvTimeframeProject.class,
						"SPM",
						"getExecutionLogsForProject",
						new String[] {"ENVIRONMENT_ID", "EARLIEST_TIME", "LATEST_TIME", "PROJECT_ID"}
				);
				
		apiDef.setDescription(
				"Returns execution logs of the monitors for the specified project and timeframe.");

		apiDef.addOutputFields(
			RETURN_TIMESTAMP
			, RETURN_MONITOR_NAME
			, RETURN_LOCATION_NAME
			, RETURN_LOG_MESSAGE
			, RETURN_STATUS
		);
		
		APIExecutorSQL executor = new APIExecutorSQL() {
			@Override
			public ResultSet execute(APIDefinitionSQL definition, CFWObject object) {
				API_Input_EnvTimeframeProject inputs = (API_Input_EnvTimeframeProject)object;
				EnvironmentSPM environment = EnvironmentManagerSPM.getEnvironment(inputs.environmentID());
				
				if(environment != null) {
					ResultSet result = environment.getDBInstance()
					.preparedExecuteQuery(CFW.Files.readPackageResource(FeatureSPM.PACKAGE_RESOURCE, "getExecutionLogsForProject.sql"),
							inputs.projectID(),
							inputs.earliestTime(),
							inputs.latestTime()
							);
					return result;
				}
				return null;
			}
		};
			
		apiDef.setSQLExecutor(executor);
		return apiDef;
	}
	
	/***************************************************************************
	 * 
	 ***************************************************************************/
	private static APIDefinitionSQL createGetExecutionLogsForMonitor() {
	
		APIDefinitionSQL apiDef = 
				new APIDefinitionSQL(
						API_Input_GetExecutionLogsForMonitor.class,
						"SPM",
						"getExecutionLogsForMonitor",
						new String[] {"ENVIRONMENT_ID", "EARLIEST_TIME", "LATEST_TIME", "PROJECT_ID", "MONITOR_NAME"}
				);
				
		apiDef.setDescription(
				"Returns execution logs of a single monitor for the specified timeframe.");
	
		apiDef.addOutputFields(
				RETURN_TIMESTAMP
				, RETURN_MONITOR_NAME
				, RETURN_LOCATION_NAME
				, RETURN_LOG_MESSAGE
				, RETURN_STATUS
			);
		
		APIExecutorSQL executor = new APIExecutorSQL() {
			@Override
			public ResultSet execute(APIDefinitionSQL definition, CFWObject object) {
				API_Input_GetExecutionLogsForMonitor inputs = (API_Input_GetExecutionLogsForMonitor)object;
				EnvironmentSPM environment = EnvironmentManagerSPM.getEnvironment(inputs.environmentID());
				
				if(environment != null) {
					ResultSet result = environment.getDBInstance()
					.preparedExecuteQuery(CFW.Files.readPackageResource(FeatureSPM.PACKAGE_RESOURCE, "getExecutionLogsForMonitor.sql"),
							inputs.projectID(),
							inputs.monitorName(),
							inputs.earliestTime(),
							inputs.latestTime()
							);
					return result;
				}
				return null;
			}
		};
			
		apiDef.setSQLExecutor(executor);
		return apiDef;
	}
	
	/***************************************************************************
	 * 
	 ***************************************************************************/
	private static APIDefinitionSQL createGetServiceTargetInfoForProject() {

		APIDefinitionSQL apiDef = 
				new APIDefinitionSQL(
						API_Input_EnvTimeframeProject.class,
						"SPM",
						"getServiceTargetInfoForProject",
						new String[] {"ENVIRONMENT_ID", "EARLIEST_TIME", "LATEST_TIME", "PROJECT_ID"}
				);
				
		apiDef.setDescription(
				"Returns information about service target violation and service uptime for a project.");

		CFWField<Integer> violation_count =
				CFWField.newInteger(FormFieldType.NONE, "ViolationCount" )
				.setDescription("The name of the rule.");
		
		CFWField<String> duration_avg =
				CFWField.newString(FormFieldType.NONE, "DurationAvg" )
				.setDescription("The average duration of the violations represented as hh:mm:ss.");
		
		CFWField<Integer> duration_avg_seconds =
				CFWField.newInteger(FormFieldType.NONE, "DurationAvgSeconds" )
				.setDescription("The average duration of the violations in seconds.");
		
		CFWField<String> availability =
				CFWField.newString(FormFieldType.NONE, "Availability" )
				.setDescription("The availability in percentage.");
		
		apiDef.addOutputFields(
				RETURN_PROJECT_NAME
				, RETURN_RULE
				, violation_count
				, duration_avg
				, duration_avg_seconds
				, availability
			);
		
		APIExecutorSQL executor = new APIExecutorSQL() {
			@Override
			public ResultSet execute(APIDefinitionSQL definition, CFWObject object) {
				API_Input_EnvTimeframeProject inputs = (API_Input_EnvTimeframeProject)object;
				EnvironmentSPM environment = EnvironmentManagerSPM.getEnvironment(inputs.environmentID());
				
				if(environment != null) {
					ResultSet result = environment.getDBInstance()
					.preparedExecuteQuery(CFW.Files.readPackageResource(FeatureSPM.PACKAGE_RESOURCE, "getServiceTargetInfoForProject.sql"),
							inputs.earliestTime(),
							inputs.latestTime(),
							inputs.projectID()
							);
					return result;
				}
				return null;
			}
		};
			
		apiDef.setSQLExecutor(executor);
		return apiDef;
	}
	
	/***************************************************************************
	 * 
	 ***************************************************************************/
	private static APIDefinitionSQL createGetServiceTargetViolationsForProject() {

		APIDefinitionSQL apiDef = 
				new APIDefinitionSQL(
						API_Input_EnvTimeframeProject.class,
						"SPM",
						"getServiceTargetViolationsForProject",
						new String[] {"ENVIRONMENT_ID", "EARLIEST_TIME", "LATEST_TIME", "PROJECT_ID"}
				);
				
		apiDef.setDescription(
				"Returns a list of service target violations for a project occurred in the specified timeframe.");

		CFWField<Long> from =
				CFWField.newLong(FormFieldType.NONE, "From" )
				.setDescription("The start time of the violation(unix timestamp, milliseconds since 01.01.1970).");
		
		CFWField<Long> to =
				CFWField.newLong(FormFieldType.NONE, "To" )
				.setDescription("The end time of the violation(unix timestamp, milliseconds since 01.01.1970).");
		
		CFWField<String> duration =
				CFWField.newString(FormFieldType.NONE, "Duration" )
				.setDescription("The duration of the violation represented as hh:mm:ss.");
		
		CFWField<Integer> duration_seconds =
				CFWField.newInteger(FormFieldType.NONE, "DurationSeconds" )
				.setDescription("The duration of the violation in seconds.");
		
		
		apiDef.addOutputFields(
				RETURN_PROJECT_NAME
				, RETURN_RULE
				, from
				, to
				, duration
				, duration_seconds
			);
		
		APIExecutorSQL executor = new APIExecutorSQL() {
			@Override
			public ResultSet execute(APIDefinitionSQL definition, CFWObject object) {
				API_Input_EnvTimeframeProject inputs = (API_Input_EnvTimeframeProject)object;
				EnvironmentSPM environment = EnvironmentManagerSPM.getEnvironment(inputs.environmentID());
				
				if(environment != null) {
					ResultSet result = environment.getDBInstance()
					.preparedExecuteQuery(CFW.Files.readPackageResource(FeatureSPM.PACKAGE_RESOURCE, "getServiceTargetViolationsForProject.sql"),
							inputs.earliestTime(),
							inputs.latestTime(),
							inputs.projectID()
							);
					return result;
				}
				return null;
			}
		};
			
		apiDef.setSQLExecutor(executor);
		return apiDef;
	}

	/***************************************************************************
	 * 
	 ***************************************************************************/
	private static APIDefinitionSQL createGetStatusForMonitor() {
	
		APIDefinitionSQL apiDef = 
				new APIDefinitionSQL(
						API_Input_GetStatusForMonitor.class,
						"SPM",
						"getStatusForMonitor",
						new String[] {"ENVIRONMENT_ID", "MONITOR_ID", "MEASURE"}
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
		
		APIExecutorSQL executor = new APIExecutorSQL() {
			@Override
			public ResultSet execute(APIDefinitionSQL definition, CFWObject object) {
				API_Input_GetStatusForMonitor inputs = (API_Input_GetStatusForMonitor)object;
				EnvironmentSPM environment = EnvironmentManagerSPM.getEnvironment(inputs.environmentID());
				
				if(environment != null) {
					ResultSet result = environment.getDBInstance()
						.preparedExecuteQuerySilent(
							CFW.Files.readPackageResource(FeatureSPM.PACKAGE_RESOURCE, "emp_widget_spmmonitorstatus.sql"),
							inputs.measureName(),
							inputs.monitorID());
					return result;
				}
				return null;
			}
		};
			
		apiDef.setSQLExecutor(executor);
		return apiDef;
	}
	
	/***************************************************************************
	 * 
	 ***************************************************************************/
	private static APIDefinitionSQL createGetStatusForProject() {
	
		APIDefinitionSQL apiDef = 
				new APIDefinitionSQL(
						API_Input_GetStatusForProject.class,
						"SPM",
						"getStatusForProject",
						new String[] {"ENVIRONMENT_ID", "PROJECT_ID", "MEASURE"}
				);
				
		apiDef.setDescription("Returns the current status for the selected project.");
	
		CFWField<Float> ValCount =
				CFWField.newFloat(FormFieldType.NONE, "ValCount" )
					.setDescription("Number of data points included in the value.");
				
		CFWField<Float> ValSum =
				CFWField.newFloat(FormFieldType.NONE, "ValSum" )
					.setDescription("Sum of all values.");
		
		CFWField<Float> ValSumSquare =
				CFWField.newFloat(FormFieldType.NONE, "ValSumSquare" )
					.setDescription("Sum of the square of all individual values.(useful for standard deviation and variance)");
		
		CFWField<Float> ValMin =
				CFWField.newFloat(FormFieldType.NONE, "ValMin" )
					.setDescription("The lowest value.");
		
		CFWField<Float> ValMax =
				CFWField.newFloat(FormFieldType.NONE, "ValMax" )
					.setDescription("The highest value.");
		
		CFWField<Float> BoundCount1 =
				CFWField.newFloat(FormFieldType.NONE, "BoundCount1" )
					.setDescription("Number of values lying below a defined lower boundary(equals ValCount if not defined).");
		
		CFWField<Float> BoundCount2 =
				CFWField.newFloat(FormFieldType.NONE, "BoundCount2" )
					.setDescription("Number of values lying below a defined upper boundary(equals ValCount if not defined).");
		
		apiDef.addOutputFields(
				  RETURN_PROJECT_NAME
				, RETURN_PROJECT_ID
				, RETURN_MEASURE_NAME
				, RETURN_VALUE
				, ValCount
				, ValSum
				, ValSumSquare
				, ValMin
				, ValMax
				, BoundCount1
				, BoundCount2
			);
		
		APIExecutorSQL executor = new APIExecutorSQL() {
			@Override
			public ResultSet execute(APIDefinitionSQL definition, CFWObject object) {
				API_Input_GetStatusForProject inputs = (API_Input_GetStatusForProject)object;
				EnvironmentSPM environment = EnvironmentManagerSPM.getEnvironment(inputs.environmentID());
				
				if(environment != null) {
					ResultSet result = environment.getDBInstance()
						.preparedExecuteQuerySilent(
							CFW.Files.readPackageResource(FeatureSPM.PACKAGE_RESOURCE, "emp_widget_spmprojectstatus.sql"),
							inputs.projectID(),
							inputs.measureName()
							);
					return result;
				}
				return null;
			}
		};
			
		apiDef.setSQLExecutor(executor);
		return apiDef;
	}
	
	/***************************************************************************
	 * 
	 ***************************************************************************/
	private static APIDefinitionSQL createGetStatusForProjectMonitors() {
	
		APIDefinitionSQL apiDef = 
				new APIDefinitionSQL(
						API_Input_GetStatusForProject.class,
						"SPM",
						"getStatusForProjectMonitors",
						new String[] {"ENVIRONMENT_ID", "PROJECT_ID", "MEASURE"}
				);
				
		apiDef.setDescription("Returns the current status for all the monitors of the selected project.");
	
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
		
		APIExecutorSQL executor = new APIExecutorSQL() {
			@Override
			public ResultSet execute(APIDefinitionSQL definition, CFWObject object) {
				API_Input_GetStatusForProject inputs = (API_Input_GetStatusForProject)object;
				EnvironmentSPM environment = EnvironmentManagerSPM.getEnvironment(inputs.environmentID());
				
				if(environment != null) {
					ResultSet result = environment.getDBInstance()
						.preparedExecuteQuerySilent(
							CFW.Files.readPackageResource(FeatureSPM.PACKAGE_RESOURCE, "emp_widget_spmmonitorstatus_for_projects.sql"),
							inputs.measureName(),
							inputs.projectID()
						);
					return result;
				}
				return null;
			}
		};
			
		apiDef.setSQLExecutor(executor);
		return apiDef;
	}
}
