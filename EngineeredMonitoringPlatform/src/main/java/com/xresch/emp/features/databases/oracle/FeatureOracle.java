package com.xresch.emp.features.databases.oracle;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.manual.FeatureManual;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.spi.CFWAppFeature;
import com.xresch.emp.features.common.FeatureEMPCommon;
import com.xresch.emp.features.databases.mysql.CFWJobTaskMySQLQueryStatus;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureOracle extends CFWAppFeature {
	
	public static final String PACKAGE_RESOURCE = "com.xresch.emp.features.databases.oracle.resources";
	
	public static final String PERMISSION_WIDGETS_ORACLE = "Database Extensions: Oracle";
	
	/************************************************************************************
	 * Override to make it managed and return something else then null.
	 ************************************************************************************/
	@Override
	public String getNameForFeatureManagement() {
		return "EMP Oracle";
	};
	
	/************************************************************************************
	 * Register a description for the feature management.
	 ************************************************************************************/
	@Override
	public String getDescriptionForFeatureManagement() {
		return "Use Oracle database extensions.(Dashboard Widgets, Query Source, Tasks ...)";
	};
	
	/************************************************************************************
	 * Return if the feature is active by default or if the admin has to enable it.
	 ************************************************************************************/
	public boolean activeByDefault() {
		return true;
	};
	
	@Override
	public void register() {
		//----------------------------------
		// Register Settings
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCE);
		
		//----------------------------------
		// Register Context Settings
		CFW.Registry.ContextSettings.register(OracleEnvironment.SETTINGS_TYPE, OracleEnvironment.class);
		
		//----------------------------------
		// Register Widgets
		CFW.Registry.Widgets.add(new WidgetOracleQueryStatus());
		
		//----------------------------------
		// Register Parameters
		CFW.Registry.Parameters.add(new ParameterDefinitionOracleEnvironment());
		
		//----------------------------------
		// Register Job Task
		CFW.Registry.Jobs.registerTask(new CFWJobTaskOracleQueryStatus());
		
	}

	@Override
	public void initializeDB() {
		
		//----------------------------------
		// Permissions
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_WIDGETS_ORACLE, FeatureUserManagement.CATEGORY_USER)
					.description("Create and Edit Oracle Widgets."),
				true,
				true);
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
		OracleEnvironmentManagement.initialize();
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
