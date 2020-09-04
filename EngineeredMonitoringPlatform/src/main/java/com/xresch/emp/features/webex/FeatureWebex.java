package com.xresch.emp.features.webex;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWAppFeature;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.emp.features.awa.WidgetJobStatusLegend;
import com.xresch.emp.features.awa.WidgetJobStatus;
import com.xresch.emp.features.awa.WidgetJobsWithStatus;
import com.xresch.emp.features.prometheus.WidgetInstantThreshold;
import com.xresch.emp.features.prometheus.WidgetRangeChart;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureWebex extends CFWAppFeature {
	
	public static final String RESOURCE_PACKAGE = "com.xresch.emp.features.webex.resources";
	public static final String MANUAL_PACKAGE = "com.xresch.emp.features.webex.manual";
	
	@Override
	public void register() {
		
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(RESOURCE_PACKAGE);
		CFW.Files.addAllowedPackage(MANUAL_PACKAGE); 
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.Registry.Widgets.add(new WidgetServiceStatus());			
		CFW.Registry.Widgets.add(new WidgetServiceStatusLegend());	
	}

	@Override
	public void initializeDB() {
									
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
