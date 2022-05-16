package com.xresch.emp.features.databases.generic;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.manual.FeatureManual;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.spi.CFWAppFeature;
import com.xresch.emp.features.awa.CFWJobTaskAWAJobIssueAlert;
import com.xresch.emp.features.common.FeatureEMPCommon;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureGenericJDBC extends CFWAppFeature {
	
	public static final String PACKAGE_RESOURCE = "com.xresch.emp.features.databases.generic.resources";
	
	public static final String PERMISSION_GENERICJDBC = "Database: JDBC";
	
	/************************************************************************************
	 * Override to make it managed and return something else then null.
	 ************************************************************************************/
	@Override
	public String getNameForFeatureManagement() {
		return "EMP Generic JDBC";
	};
	
	/************************************************************************************
	 * Register a description for the feature management.
	 ************************************************************************************/
	@Override
	public String getDescriptionForFeatureManagement() {
		return "Database extensions (Dashboard Widgets, Query Source, Tasks ...) to fetch data from a database based on a JDBC driver available in the ./extensions directory.";
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
		CFW.Registry.ContextSettings.register(GenericJDBCEnvironment.SETTINGS_TYPE, GenericJDBCEnvironment.class);
		
		//----------------------------------
		// Register Widgets
		CFW.Registry.Widgets.add(new WidgetGenericJDBCQueryChart());
		CFW.Registry.Widgets.add(new WidgetGenericJDBCQueryStatus());
		
		//----------------------------------
		// Register Parameters
		CFW.Registry.Parameters.add(new ParameterDefinitionGenericJDBCEnvironment());
		
		//----------------------------------
		// Register Query Source
		CFW.Registry.Query.registerSource(new CFWQuerySourceGenericJDBC(null));
		
		//----------------------------------
		// Register Job Task
		CFW.Registry.Jobs.registerTask(new CFWJobTaskGenericJDBCQueryStatus());
	}

	@Override
	public void initializeDB() {
	
		//----------------------------------
		// Permissions
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_GENERICJDBC, FeatureUserManagement.CATEGORY_USER)
					.description("Use the Generic JDBC extensions(Dashboard Widgets, Query Source)."),
				true,
				true);
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
		GenericJDBCEnvironmentManagement.initialize();
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
