package com.xresch.emp.features.databases.mysql;

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
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureMySQL extends CFWAppFeature {
	
	public static final String PACKAGE_RESOURCE = "com.xresch.emp.features.databases.mysql.resources";
	
	public static final String PERMISSION_MYSQL = "Database: MySQL";
	
	/************************************************************************************
	 * Override to make it managed and return something else then null.
	 ************************************************************************************/
	@Override
	public String getNameForFeatureManagement() {
		return "EMP MySQL";
	};
	
	/************************************************************************************
	 * Register a description for the feature management.
	 ************************************************************************************/
	@Override
	public String getDescriptionForFeatureManagement() {
		return "Use MySQL database extensions.(Dashboard Widgets, Query Source, Tasks ...)";
	};
	
	/************************************************************************************
	 * Return if the feature is active by default or if the admin has to enable it.
	 ************************************************************************************/
	public boolean activeByDefault() {
		return true;
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
		CFW.Registry.ContextSettings.register(MySQLEnvironment.SETTINGS_TYPE, MySQLEnvironment.class);
		
		//----------------------------------
		// Register Widgets
		CFW.Registry.Widgets.add(new WidgetMySQLQueryChart());
		CFW.Registry.Widgets.add(new WidgetMySQLQueryStatus());
		
		//----------------------------------
		// Register Widget Parameters
		CFW.Registry.Parameters.add(new ParameterDefinitionMySQLEnvironment());
		
		//----------------------------------
		// Register Query Source
		CFW.Registry.Query.registerSource(new CFWQuerySourceMySQL(null));
		
		//----------------------------------
		// Register Job Task
		CFW.Registry.Jobs.registerTask(new CFWJobTaskMySQLQueryStatus());
		
	}

	@Override
	public void initializeDB() {
		//----------------------------------
		// Permissions
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_MYSQL, FeatureUserManagement.CATEGORY_USER)
					.description("Create and Edit MySQL Widgets."),
				true,
				true);
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
		MySQLEnvironmentManagement.initialize();
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
