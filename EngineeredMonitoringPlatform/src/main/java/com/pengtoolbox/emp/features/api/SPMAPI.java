package com.pengtoolbox.emp.features.api;

import java.sql.ResultSet;
import java.util.ArrayList;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.features.api.APIDefinition;
import com.pengtoolbox.cfw.features.api.APIDefinitionSQL;
import com.pengtoolbox.cfw.features.api.APISQLExecutor;
import com.pengtoolbox.emp.features.environments.SPMEnvironment;
import com.pengtoolbox.emp.features.environments.SPMEnvironmentManagement;

public class SPMAPI {
	
	/***************************************************************************
	 * 
	 ***************************************************************************/
	public static ArrayList<APIDefinition> getAPIDefinitions() {

		ArrayList<APIDefinition> apis = new ArrayList<APIDefinition>();
		
		apis.add(createGetExecutionLogsForProject());
		apis.add(createGetExecutionLogsForMonitor());
		apis.add(createGetServiceTargetInfoForProject());
		apis.add(createGetServiceTargetViolationsForProject());
		return apis;
	}

	/***************************************************************************
	 * 
	 ***************************************************************************/
	private static APIDefinitionSQL createGetExecutionLogsForProject() {

		APIDefinitionSQL apiDef = 
				new APIDefinitionSQL(
						SPMAPI_EnvTimeframeProjectInputs.class,
						"SPM",
						"getExecutionLogsForProject",
						new String[] {"ENVIRONMENT_ID", "EARLIEST_TIME", "LATEST_TIME", "PROJECT_ID"}
				);
				
		apiDef.setDescription(
				"Returns execution logs of the monitors for the specified project and timeframe.");

		APISQLExecutor executor = new APISQLExecutor() {
			@Override
			public ResultSet execute(APIDefinitionSQL definition, CFWObject object) {
				SPMAPI_EnvTimeframeProjectInputs inputs = (SPMAPI_EnvTimeframeProjectInputs)object;
				SPMEnvironment environment = SPMEnvironmentManagement.getEnvironment(inputs.environmentID());
				
				if(environment != null) {
					ResultSet result = environment.getDBInstance()
					.preparedExecuteQuery(CFW.Files.readPackageResource(FeatureEMPAPI.RESOURCE_PACKAGE, "getExecutionLogsForProject.sql"),
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
						SPMAPI_getExecutionLogsForMonitorInputs.class,
						"SPM",
						"getExecutionLogsForMonitor",
						new String[] {"ENVIRONMENT_ID", "EARLIEST_TIME", "LATEST_TIME", "PROJECT_ID", "MONITOR_NAME"}
				);
				
		apiDef.setDescription(
				"Returns execution logs of a single monitor for the specified timeframe.");
	
		APISQLExecutor executor = new APISQLExecutor() {
			@Override
			public ResultSet execute(APIDefinitionSQL definition, CFWObject object) {
				SPMAPI_getExecutionLogsForMonitorInputs inputs = (SPMAPI_getExecutionLogsForMonitorInputs)object;
				SPMEnvironment environment = SPMEnvironmentManagement.getEnvironment(inputs.environmentID());
				
				if(environment != null) {
					ResultSet result = environment.getDBInstance()
					.preparedExecuteQuery(CFW.Files.readPackageResource(FeatureEMPAPI.RESOURCE_PACKAGE, "getExecutionLogsForMonitor.sql"),
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
						SPMAPI_EnvTimeframeProjectInputs.class,
						"SPM",
						"getServiceTargetInfoForProject",
						new String[] {"ENVIRONMENT_ID", "EARLIEST_TIME", "LATEST_TIME", "PROJECT_ID"}
				);
				
		apiDef.setDescription(
				"Returns information about service target violation and service uptime for a project.");

		APISQLExecutor executor = new APISQLExecutor() {
			@Override
			public ResultSet execute(APIDefinitionSQL definition, CFWObject object) {
				SPMAPI_EnvTimeframeProjectInputs inputs = (SPMAPI_EnvTimeframeProjectInputs)object;
				SPMEnvironment environment = SPMEnvironmentManagement.getEnvironment(inputs.environmentID());
				
				if(environment != null) {
					ResultSet result = environment.getDBInstance()
					.preparedExecuteQuery(CFW.Files.readPackageResource(FeatureEMPAPI.RESOURCE_PACKAGE, "getServiceTargetInfoForProject.sql"),
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
						SPMAPI_EnvTimeframeProjectInputs.class,
						"SPM",
						"getServiceTargetViolationsForProject",
						new String[] {"ENVIRONMENT_ID", "EARLIEST_TIME", "LATEST_TIME", "PROJECT_ID"}
				);
				
		apiDef.setDescription(
				"Returns information about service target violation and service uptime for a project.");

		APISQLExecutor executor = new APISQLExecutor() {
			@Override
			public ResultSet execute(APIDefinitionSQL definition, CFWObject object) {
				SPMAPI_EnvTimeframeProjectInputs inputs = (SPMAPI_EnvTimeframeProjectInputs)object;
				SPMEnvironment environment = SPMEnvironmentManagement.getEnvironment(inputs.environmentID());
				
				if(environment != null) {
					ResultSet result = environment.getDBInstance()
					.preparedExecuteQuery(CFW.Files.readPackageResource(FeatureEMPAPI.RESOURCE_PACKAGE, "getServiceTargetViolationsForProject.sql"),
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
