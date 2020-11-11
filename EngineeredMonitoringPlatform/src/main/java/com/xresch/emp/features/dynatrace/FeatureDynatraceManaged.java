package com.xresch.emp.features.dynatrace;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWAppFeature;
import com.xresch.cfw._main.CFWApplicationExecutor;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureDynatraceManaged extends CFWAppFeature {
	
	public static final String PACKAGE_MANUAL   = "com.xresch.emp.features.dynatrace.manual";
	public static final String PACKAGE_RESOURCE = "com.xresch.emp.features.dynatrace.resources";
	
	@Override
	public void register() {
		
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(PACKAGE_MANUAL);
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCE);
		
		//----------------------------------
		// Register Context Settings
		CFW.Registry.ContextSettings.register(DynatraceManagedEnvironment.SETTINGS_TYPE, DynatraceManagedEnvironment.class);
    
		//----------------------------------
		// Register Widgets
		CFW.Registry.Widgets.add(new WidgetHostDetails());
		CFW.Registry.Widgets.add(new WidgetHostMetricsChart());
		CFW.Registry.Widgets.add(new WidgetHostProcesses());
		CFW.Registry.Widgets.add(new WidgetHostUnitConsumptionByTags());
		
		//----------------------------------
		// Register Manual Page
//		FeatureEMPCommon.WIDGET_PAGE.addChild(
//				new ManualPage("Dynatrace Widgets")
//					.faicon("fas fa-desktop")
//					.addPermission(FeatureManual.PERMISSION_MANUAL)
//					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "z_manual_widgets_Prometheus.html")
//			);
	}

	@Override
	public void initializeDB() {
		DynatraceManagedEnvironmentManagement.initialize();
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
