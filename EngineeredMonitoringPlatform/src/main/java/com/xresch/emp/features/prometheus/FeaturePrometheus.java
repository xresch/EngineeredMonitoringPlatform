package com.xresch.emp.features.prometheus;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWAppFeature;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.manual.FeatureManual;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.emp.features.common.FeatureEMPCommon;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeaturePrometheus extends CFWAppFeature {
	
	public static final String PACKAGE_MANUAL   = "com.xresch.emp.features.prometheus.manual";
	public static final String PACKAGE_RESOURCE = "com.xresch.emp.features.prometheus.resources";
	
	@Override
	public void register() {
		
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(PACKAGE_MANUAL);
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCE);
		
		//----------------------------------
		// Register Context Settings
		CFW.Registry.ContextSettings.register(PrometheusEnvironment.SETTINGS_TYPE, PrometheusEnvironment.class);
    
		//----------------------------------
		// Register Widgets
		CFW.Registry.Widgets.add(new WidgetInstantThreshold());
		CFW.Registry.Widgets.add(new WidgetRangeChart());
		
		//----------------------------------
		// Register Manual Page
		FeatureEMPCommon.WIDGET_PAGE.addChild(
				new ManualPage("Prometheus Widgets")
					.faicon("fas fa-desktop")
					.addPermission(FeatureManual.PERMISSION_MANUAL)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "z_manual_widgets_Prometheus.html")
			);
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
