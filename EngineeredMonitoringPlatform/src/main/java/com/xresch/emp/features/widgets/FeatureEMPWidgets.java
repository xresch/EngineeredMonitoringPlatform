package com.xresch.emp.features.widgets;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWAppFeature;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.emp.features.environments.AWAEnvironmentManagement;
import com.xresch.emp.features.environments.PrometheusEnvironment;
import com.xresch.emp.features.environments.SPMEnvironmentManagement;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class FeatureEMPWidgets extends CFWAppFeature {
	
	public static final String RESOURCE_PACKAGE = "com.xresch.emp.features.widgets.recourses";
	public static final String MANUAL_PACKAGE = "com.xresch.emp.features.widgets.manual";
	
	public static final String PERMISSION_AWAJOBSTATUS = "AWA Jobstatus";
	

	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(RESOURCE_PACKAGE);
		CFW.Files.addAllowedPackage(MANUAL_PACKAGE); 
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.Registry.Widgets.add(new AWAJobStatusWidget());
		CFW.Registry.Widgets.add(new AWAJobStatusLegendWidget());
		
		CFW.Registry.Widgets.add(new PrometheusGeneralQueryWidget());
		
		CFW.Registry.Widgets.add(new SPMProjectStatusWidget());
		CFW.Registry.Widgets.add(new SPMMonitorStatusWidget());
		CFW.Registry.Widgets.add(new SPMMonitorStatusForProjectsWidget());
		CFW.Registry.Widgets.add(new SPMMonitorStatusAllWidget());
		CFW.Registry.Widgets.add(new SPMCounterForMonitorStatusWidget());
		CFW.Registry.Widgets.add(new SPMCounterForProjectStatusWidget());
		CFW.Registry.Widgets.add(new SPMTimersForMonitorWidget());
		CFW.Registry.Widgets.add(new SPMTimersForProjectWidget());
		CFW.Registry.Widgets.add(new SPMStatusLegendWidget());
		CFW.Registry.Widgets.add(new SPMMeasureLegendWidget());
		
		CFW.Registry.Widgets.add(new WebexServiceStatusWidget());			
		CFW.Registry.Widgets.add(new WebexServiceStatusLegendWidget());	
	}

	@Override
	public void initializeDB() {
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_AWAJOBSTATUS, "user")
					.description("View and analyze productive AWA job status."),
				true,
				true);
							
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
        //app.addAppServlet(ServletTheusinator.class,  "/theusinator");
	}

	@Override
	public void startTasks() {

	}

	@Override
	public void stopFeature() {

	}

}