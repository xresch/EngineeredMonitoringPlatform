package com.xresch.emp.features.exense.step;

import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;

import com.google.common.util.concurrent.SettableFuture;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.manual.FeatureManual;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.spi.CFWAppFeature;
import com.xresch.emp.features.common.FeatureEMPCommon;
import com.xresch.emp.features.databases.generic.CFWQuerySourceGenericJDBC;
import com.xresch.emp.features.databases.mssql.CFWJobTaskMSSQLQueryStatus;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureExenseStep extends CFWAppFeature {
	
	public static final String PACKAGE_RESOURCE = "com.xresch.emp.features.exense.step.resources";
	
	public static final String PERMISSION_STEP = "Exense Step Extensions";
	public static final String WIDGET_PREFIX = "emp_step";
	
	static final JsonWriterSettings writterSettings = 
	JsonWriterSettings
	.builder()
	.outputMode(JsonMode.RELAXED)
	.build();


	
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
		return "Exense Step extensions.(Widgets, Source, Tasks ...)";
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
		
		//----------------------------------
		// Register Context Settings
		CFW.Registry.ContextSettings.register(StepEnvironment.SETTINGS_TYPE, StepEnvironment.class);
		
		//----------------------------------
		// Register Widgets
		CFW.Registry.Widgets.add(new WidgetStepQueryStatus());
		
		//----------------------------------
		// Register Widget Parameters
		CFW.Registry.Parameters.add(new ParameterDefinitionStepEnvironment());
		
		//----------------------------------
		// Register Query Source
		CFW.Registry.Query.registerSource(new CFWQuerySourceStep(null));
		
		//----------------------------------
		// Register Job Task
		CFW.Registry.Jobs.registerTask(new CFWJobStepQueryStatus());
		
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
