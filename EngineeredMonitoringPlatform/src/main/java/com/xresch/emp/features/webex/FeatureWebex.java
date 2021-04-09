package com.xresch.emp.features.webex;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWAppFeature;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.emp.features.awa.WidgetJobStatusLegend;
import com.xresch.emp.features.awa.WidgetJobStatusCurrent;
import com.xresch.emp.features.awa.WidgetJobsWithStatus;
import com.xresch.emp.features.prometheus.WidgetInstantThreshold;
import com.xresch.emp.features.prometheus.WidgetRangeChart;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureWebex extends CFWAppFeature {
	
	public static final String PACKAGE_RESOURCE = "com.xresch.emp.features.webex.resources";
	public static final String MANUAL_PACKAGE = "com.xresch.emp.features.webex.manual";
	
	public static final String PERMISSION_WIDGETS_WEBEX = "Widgets: Webex";
	
	/************************************************************************************
	 * Override to make it managed and return something else then null.
	 ************************************************************************************/
	@Override
	public String getNameForFeatureManagement() {
		return "EMP Webex";
	};
	
	/************************************************************************************
	 * Register a description for the feature management.
	 ************************************************************************************/
	@Override
	public String getDescriptionForFeatureManagement() {
		return "Dashboard Widget to fetch service status from Webex.";
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
		// Register Package
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCE);
		CFW.Files.addAllowedPackage(MANUAL_PACKAGE); 
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.Registry.Widgets.add(new WidgetServiceStatus());			
		CFW.Registry.Widgets.add(new WidgetServiceStatusLegend());	
	}

	@Override
	public void initializeDB() {
		//----------------------------------
		// Permissions
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_WIDGETS_WEBEX, "user")
					.description("Create and Edit Webex Widgets."),
				true,
				true);							
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
