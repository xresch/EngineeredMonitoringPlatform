package com.xresch.emp.features.prometheus;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWAppFeature;
import com.xresch.cfw._main.CFWApplicationExecutor;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeaturePrometheus extends CFWAppFeature {
	
	public static final String RESOURCE_PACKAGE = "com.xresch.emp.features.prometheus.resources";
	
	@Override
	public void register() {
		
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(RESOURCE_PACKAGE);
		
		//----------------------------------
		// Register Context Settings
		CFW.Registry.ContextSettings.register(PrometheusEnvironment.SETTINGS_TYPE, PrometheusEnvironment.class);
    
		//----------------------------------
		// Register Widgets
		CFW.Registry.Widgets.add(new WidgetInstantThreshold());
		CFW.Registry.Widgets.add(new WidgetRangeChart());
	}

	@Override
	public void initializeDB() {
		PrometheusEnvironmentManagement.initialize();
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
