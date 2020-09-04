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
	
	public static final String RESOURCE_PACKAGE = "com.xresch.emp.features.awa.resources";
	
	//public static final String PERMISSION_AWAJOBSTATUS = "AWA Jobstatus";
	
	@Override
	public void register() {
		//----------------------------------
		// Register Settings
		CFW.Files.addAllowedPackage(RESOURCE_PACKAGE);
		
		//----------------------------------
		// Register Context Settings
		CFW.Registry.ContextSettings.register(AWAEnvironment.SETTINGS_TYPE, AWAEnvironment.class);
		
		//----------------------------------
		// Register Widgets
		CFW.Registry.Widgets.add(new WidgetJobStatus());
		CFW.Registry.Widgets.add(new WidgetJobsWithStatus());
		CFW.Registry.Widgets.add(new WidgetJobStatusLegend());
		

    
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
