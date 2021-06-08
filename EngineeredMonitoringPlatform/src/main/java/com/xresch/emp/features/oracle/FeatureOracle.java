package com.xresch.emp.features.oracle;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWAppFeature;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.config.Configuration;
import com.xresch.cfw.features.manual.FeatureManual;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.response.bootstrap.MenuItem;
import com.xresch.emp._main.Main;
import com.xresch.emp.features.awa.ParameterDefinitionAWAEnvironment;
import com.xresch.emp.features.common.FeatureEMPCommon;
import com.xresch.emp.features.prometheus.PrometheusEnvironment;
import com.xresch.emp.features.prometheus.PrometheusEnvironmentManagement;
import com.xresch.emp.features.spm.EnvironmentSPM;
import com.xresch.emp.features.spm.EnvironmentManagerSPM;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureOracle extends CFWAppFeature {
	
	public static final String PACKAGE_MANUAL   = "com.xresch.emp.features.oracle.manual";
	public static final String PACKAGE_RESOURCE = "com.xresch.emp.features.oracle.resources";
	
	public static final String PERMISSION_WIDGETS_ORACLE = "Widgets: Oracle";
	
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
		return "Dashboard Widgets to fetch data from an Oracle Database.";
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
		CFW.Files.addAllowedPackage(PACKAGE_MANUAL);
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
		// Register Manual Page
		FeatureEMPCommon.WIDGET_PAGE.addChild(
				new ManualPage("Oracle Widgets")
					.faicon("fas fa-desktop")
					.addPermission(FeatureManual.PERMISSION_MANUAL)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "z_manual_widgets_oracle.html")
			);
	}

	@Override
	public void initializeDB() {
		
		
		//----------------------------------
		// Permissions
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_WIDGETS_ORACLE, "user")
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
