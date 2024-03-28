package com.xresch.emp.features.exense.step;

import java.util.concurrent.ScheduledFuture;

import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.manual.FeatureManual;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.spi.CFWAppFeature;
import com.xresch.cfw.utils.CFWTime.CFWTimeUnit;
import com.xresch.emp.features.exense.step.api.STEPAPI_Factory;
import com.xresch.emp.features.exense.step.query.CFWQueryFunctionStepPlanName;
import com.xresch.emp.features.exense.step.query.CFWQueryFunctionStepProjectName;
import com.xresch.emp.features.exense.step.query.CFWQueryFunctionStepSchedulerName;
import com.xresch.emp.features.exense.step.query.CFWQueryFunctionStepSetEnv;
import com.xresch.emp.features.exense.step.query.CFWQuerySourceStepAPI;
import com.xresch.emp.features.exense.step.query.CFWQuerySourceStepData;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureExenseStep extends CFWAppFeature {
	
	public static final String PACKAGE_RESOURCE = "com.xresch.emp.features.exense.step.resources";
	public static final String PACKAGE_MANUAL = "com.xresch.emp.features.exense.step.manual";
	
	public static final String PERMISSION_STEP = "Exense Step: Extensions";
	public static final String PERMISSION_STEP_SOURCE_STEPAPI = "Exense Step: Source stepapi";
	public static final String WIDGET_PREFIX = "emp_step";
	
	public static final String QUERY_FUNCTION_TAG = "Exense Step";
	
	private static ScheduledFuture<?> taskReloadSchedulerCache;
	
	static final JsonWriterSettings writterSettings = 
		JsonWriterSettings
		.builder()
		.outputMode(JsonMode.RELAXED)
		.build();

	public static final String WIDGET_CATEGORY_EXENSESTEP = "Exense Step";

	public enum StepExecutionResult {
		  PASSED
		, FAILED
		, TECHNICAL_ERROR
		, IMPORT_ERROR
		, VETOED
		, SKIPPED
		, INTERRUPTED
		, RUNNING
		, NORUN
	}
	
	/************************************************************************************
	 * Override to make it managed and return something else then null.
	 ************************************************************************************/
	@Override
	public String getNameForFeatureManagement() {
		return "EMP Exense Step";
	};
	
	/************************************************************************************
	 * Register a description for the feature management.
	 ************************************************************************************/
	@Override
	public String getDescriptionForFeatureManagement() {
		return "Exense Step extensions that fetch data directly from the STEP REST API.(Widgets, Source, Tasks ...)";
	};
	
	/************************************************************************************
	 * Return if the feature is active by default or if the admin has to enable it.
	 ************************************************************************************/
	public boolean activeByDefault() {
		return false;
	};
	
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	@Override
	public void register() {
		//----------------------------------
		// Register Settings
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCE);
		CFW.Files.addAllowedPackage(PACKAGE_MANUAL);
		
		//----------------------------------
		// Register Context Settings
		CFW.Registry.ContextSettings.register(StepEnvironment.SETTINGS_TYPE, StepEnvironment.class);
		
		//----------------------------------
		// Register Widgets
		CFW.Registry.Widgets.add(new WidgetSchedulerStatus());
		CFW.Registry.Widgets.add(new WidgetSchedulerStatusByProject());
		CFW.Registry.Widgets.add(new WidgetSchedulerStatusByUsers());
		CFW.Registry.Widgets.add(new WidgetSchedulerStatusHistory());
		CFW.Registry.Widgets.add(new WidgetSchedulerExecutionsLastN());
		CFW.Registry.Widgets.add(new WidgetSchedulerExecutionsTimerange());
		CFW.Registry.Widgets.add(new WidgetSchedulerDurationChart());
		CFW.Registry.Widgets.add(new WidgetSchedulerMetricsChart());
		CFW.Registry.Widgets.add(new WidgetStepStatusLegend());
		
		//----------------------------------
		// Register Widget Parameters
		CFW.Registry.Parameters.add(new ParameterDefinitionStepEnvironment());
		
		//----------------------------------
		// Register Query Source
		CFW.Registry.Query.registerSource(new CFWQuerySourceStepData(null));
		CFW.Registry.Query.registerSource(new CFWQuerySourceStepAPI(null));
		
		//----------------------------------
		// Register Query Function
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionStepSetEnv(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionStepPlanName(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionStepProjectName(null));
		CFW.Registry.Query.registerFunction(new CFWQueryFunctionStepSchedulerName(null));
				
		//----------------------------------
		// Register Job Task
//		CFW.Registry.Jobs.registerTask(new CFWJobTaskSchedulerStatus());
//		CFW.Registry.Jobs.registerTask(new CFWJobTaskSchedulerStatusByProject());
//		CFW.Registry.Jobs.registerTask(new CFWJobTaskSchedulerStatusByUser());

		//----------------------------------
		// Register Manual Page
		CFW.Registry.Manual.addManualPage(null,
				new ManualPage("Exense Step")
					.faicon("fas fa-desktop")
					.addPermission(FeatureManual.PERMISSION_MANUAL)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "z_manual_step.html")
			);
		
	}

	@Override
	public void initializeDB() {
				
		//----------------------------------
		// Permissions
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_STEP, FeatureUserManagement.CATEGORY_USER)
					.description("Use the Step Extensions(Widgets, Sources, JobTasks, etc...)."),
				true,
				true);
		
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_STEP_SOURCE_STEPAPI, FeatureUserManagement.CATEGORY_USER)
				.description("Allows the user to use the query source stepapi."),
				true,
				false);
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
		
		//---------------------------------------------------------
		// Load Environments
		StepEnvironmentManagement.initialize();
		
		//---------------------------------------------------------
		//Register here as it won't work before DB is initialized.
		CFW.Registry.API.addAll(STEPAPI_Factory.getAPIDefinitions());
	}

	@Override
	public void startTasks() {
		//----------------------------------------
		// Task: Store to Database
		if(taskReloadSchedulerCache != null) {
			taskReloadSchedulerCache.cancel(false);
		}
		
		int millis = (int)CFWTimeUnit.m.toMillis(5);
		taskReloadSchedulerCache = CFW.Schedule.runPeriodicallyMillis(millis, millis, new TaskStepReloadSchedulerCache());
				
	}

	@Override
	public void stopFeature() {
		/* do nothing */
	}

}
