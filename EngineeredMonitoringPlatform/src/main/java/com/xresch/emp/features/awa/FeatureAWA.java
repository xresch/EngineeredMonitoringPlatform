package com.xresch.emp.features.awa;

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
public class FeatureAWA extends CFWAppFeature {
	
	public static final String PACKAGE_MANUAL   = "com.xresch.emp.features.awa.manual";
	public static final String PACKAGE_RESOURCE = "com.xresch.emp.features.awa.resources";
	
	//public static final String PERMISSION_AWAJOBSTATUS = "AWA Jobstatus";
	
	/************************************************************************************
	 * Override to make it managed and return something else then null.
	 ************************************************************************************/
	@Override
	public String getNameForFeatureManagement() {
		return "AWA";
	};
	
	/************************************************************************************
	 * Register a description for the feature management.
	 ************************************************************************************/
	@Override
	public String getDescriptionForFeatureManagement() {
		return "Dashboard Widgets and Context Settings for Automic Workload Automation(AWA).";
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
		CFW.Registry.ContextSettings.register(AWAEnvironment.SETTINGS_TYPE, AWAEnvironment.class);
		
		//----------------------------------
		// Register Widgets
		CFW.Registry.Widgets.add(new WidgetJobStatusCurrent());
		CFW.Registry.Widgets.add(new WidgetJobStatusHistory());
		CFW.Registry.Widgets.add(new WidgetJobsWithStatus());
		CFW.Registry.Widgets.add(new WidgetJobStatusLegend());
		
		//----------------------------------
		// Register Widgets
		CFW.Registry.Parameters.add(new ParameterDefinitionAWAJobname());
    
		
		//----------------------------------
		// Register Manual Page
		FeatureEMPCommon.WIDGET_PAGE.addChild(
				new ManualPage("AWA Widgets")
					.faicon("fas fa-desktop")
					.addPermission(FeatureManual.PERMISSION_MANUAL)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "z_manual_widgets_AWA.html")
			);
	}

	@Override
	public void initializeDB() {
		AWAEnvironmentManagement.initialize();
		
		//----------------------------------
		// Permissions
//		CFW.DB.Permissions.oneTimeCreate(
//				new Permission(PERMISSION_AWAJOBSTATUS, "user")
//					.description("View and analyze productive AWA job status."),
//				true,
//				true);
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
		/* do nothing */
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
