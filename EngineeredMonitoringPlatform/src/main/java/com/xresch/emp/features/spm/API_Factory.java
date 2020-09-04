package com.xresch.emp.features.spm;

import java.sql.ResultSet;
import java.util.ArrayList;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.api.APIDefinition;
import com.xresch.cfw.features.api.APIDefinitionSQL;
import com.xresch.cfw.features.api.APISQLExecutor;
import com.xresch.emp.features.webex.FeatureWebex;

public class API_Factory {
	
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

		APISQLExecutor executor = new APISQLExecutor() {
			@Override
			public ResultSet execute(APIDefinitionSQL definition, CFWObject object) {
				API_Input_EnvTimeframeProject inputs = (API_Input_EnvTimeframeProject)object;
				EnvironmentSPM environment = EnvironmentManagerSPM.getEnvironment(inputs.environmentID());
				
				if(environment != null) {
					ResultSet result = environment.getDBInstance()
					.preparedExecuteQuery(CFW.Files.readPackageResource(FeatureSPM.RESOURCE_PACKAGE, "getExecutionLogsForProject.sql"),
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
	
		APISQLExecutor executor = new APISQLExecutor() {
			@Override
			public ResultSet execute(APIDefinitionSQL definition, CFWObject object) {
				API_Input_GetExecutionLogsForMonitor inputs = (API_Input_GetExecutionLogsForMonitor)object;
				EnvironmentSPM environment = EnvironmentManagerSPM.getEnvironment(inputs.environmentID());
				
				if(environment != null) {
					ResultSet result = environment.getDBInstance()
					.preparedExecuteQuery(CFW.Files.readPackageResource(FeatureSPM.RESOURCE_PACKAGE, "getExecutionLogsForMonitor.sql"),
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
	private static APIDefinitionSQL createGetStatusForMonitor() {
	
		APIDefinitionSQL apiDef = 
				new APIDefinitionSQL(
						API_Input_GetStatusForMonitor.class,
						"SPM",
						"getStatusForMonitor",
						new String[] {"ENVIRONMENT_ID", "MONITOR_ID", "MEASURE"}
				);
				
		apiDef.setDescription("Returns the current status for the selected monitor.");
	
		APISQLExecutor executor = new APISQLExecutor() {
			@Override
			public ResultSet execute(APIDefinitionSQL definition, CFWObject object) {
				API_Input_GetStatusForMonitor inputs = (API_Input_GetStatusForMonitor)object;
				EnvironmentSPM environment = EnvironmentManagerSPM.getEnvironment(inputs.environmentID());
				
				if(environment != null) {
					ResultSet result = environment.getDBInstance()
						.preparedExecuteQuerySilent(
							CFW.Files.readPackageResource(FeatureWebex.RESOURCE_PACKAGE, "emp_widget_spmmonitorstatus.sql"),
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

		APISQLExecutor executor = new APISQLExecutor() {
			@Override
			public ResultSet execute(APIDefinitionSQL definition, CFWObject object) {
				API_Input_EnvTimeframeProject inputs = (API_Input_EnvTimeframeProject)object;
				EnvironmentSPM environment = EnvironmentManagerSPM.getEnvironment(inputs.environmentID());
				
				if(environment != null) {
					ResultSet result = environment.getDBInstance()
					.preparedExecuteQuery(CFW.Files.readPackageResource(FeatureSPM.RESOURCE_PACKAGE, "getServiceTargetInfoForProject.sql"),
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

		APISQLExecutor executor = new APISQLExecutor() {
			@Override
			public ResultSet execute(APIDefinitionSQL definition, CFWObject object) {
				API_Input_EnvTimeframeProject inputs = (API_Input_EnvTimeframeProject)object;
				EnvironmentSPM environment = EnvironmentManagerSPM.getEnvironment(inputs.environmentID());
				
				if(environment != null) {
					ResultSet result = environment.getDBInstance()
					.preparedExecuteQuery(CFW.Files.readPackageResource(FeatureSPM.RESOURCE_PACKAGE, "getServiceTargetViolationsForProject.sql"),
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
}
