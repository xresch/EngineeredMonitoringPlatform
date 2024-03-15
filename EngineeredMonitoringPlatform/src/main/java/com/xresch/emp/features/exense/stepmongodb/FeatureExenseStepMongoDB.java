package com.xresch.emp.features.exense.stepmongodb;

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

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureExenseStepMongoDB extends CFWAppFeature {
	
	public static final String PACKAGE_RESOURCE = "com.xresch.emp.features.exense.stepmongodb.resources";
	public static final String PACKAGE_MANUAL = "com.xresch.emp.features.exense.stepmongodb.manual";
	
	public static final String PERMISSION_STEP = "Exense Step Extensions(MongoDB)";
	public static final String WIDGET_PREFIX = "emp_stepmongodb";
	
	static final JsonWriterSettings writterSettings = 
		JsonWriterSettings
		.builder()
		.outputMode(JsonMode.RELAXED)
		.build();

	public static final String WIDGET_CATEGORY_EXENSESTEP_MONGODB = "Exense Step(MongoDB)";

	public enum StepExecutionResult {
		PASSED, FAILED, TECHNICAL_ERROR, RUNNING
	}
	
	/************************************************************************************
	 * Override to make it managed and return something else then null.
	 ************************************************************************************/
	@Override
	public String getNameForFeatureManagement() {
		return "EMP Exense Step MongoDB";
	};
	
	/************************************************************************************
	 * Register a description for the feature management.
	 ************************************************************************************/
	@Override
	public String getDescriptionForFeatureManagement() {
		return "(Deprecated)Exense Step extensions that fetch data directly from the MongoDB.(Widgets, Source, Tasks ...)";
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
		CFW.Registry.Widgets.add(new WidgetPlanStatus());
		CFW.Registry.Widgets.add(new WidgetPlanStatusByProject());
		CFW.Registry.Widgets.add(new WidgetPlanStatusByUser());
		CFW.Registry.Widgets.add(new WidgetPlanStatusByCurrentUser());
		CFW.Registry.Widgets.add(new WidgetPlanStatusAll());
		CFW.Registry.Widgets.add(new WidgetPlanExecutionsLast());
		CFW.Registry.Widgets.add(new WidgetPlanExecutionsTimerange());
		CFW.Registry.Widgets.add(new WidgetPlanDurationChart());
		CFW.Registry.Widgets.add(new WidgetStepStatusLegend());
		
		//----------------------------------
		// Register Widget Parameters
		CFW.Registry.Parameters.add(new ParameterDefinitionStepEnvironment());
		
		//----------------------------------
		// Register Query Source
		//CFW.Registry.Query.registerSource(new CFWQuerySourceStep(null));
		
		//----------------------------------
		// Register Job Task
		CFW.Registry.Jobs.registerTask(new CFWJobTaskPlanStatus());
		CFW.Registry.Jobs.registerTask(new CFWJobTaskPlanStatusByProject());
		CFW.Registry.Jobs.registerTask(new CFWJobTaskPlanStatusByUser());

		
		//----------------------------------
		// Register Manual Page
		CFW.Registry.Manual.addManualPage(null,
				new ManualPage("Exense Step(MongoDB)")
					.faicon("fas fa-desktop")
					.addPermission(FeatureManual.PERMISSION_MANUAL)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCE, "z_manual_step.html")
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
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
		StepEnvironmentManagement.initialize();
	}

	@Override
	public void startTasks() {
		/* do nothing */
	}

	@Override
	public void stopFeature() {
		/* do nothing */
	}

}
