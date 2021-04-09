package com.xresch.emp.features.dynatrace;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWAppFeature;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.manual.FeatureManual;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.emp.features.awa.ParameterDefinitionAWAEnvironment;
import com.xresch.emp.features.common.FeatureEMPCommon;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureDynatrace extends CFWAppFeature {
	
	public static final String PACKAGE_MANUAL   = "com.xresch.emp.features.dynatrace.manual";
	public static final String PACKAGE_RESOURCE = "com.xresch.emp.features.dynatrace.resources";
	
	public static final String PERMISSION_WIDGETS_DYNATRACE = "Widgets: Dynatrace";
	
	/************************************************************************************
	 * Override to make it managed and return something else then null.
	 ************************************************************************************/
	@Override
	public String getNameForFeatureManagement() {
		return "EMP Dynatrace";
	};
	
	/************************************************************************************
	 * Register a description for the feature management.
	 ************************************************************************************/
	@Override
	public String getDescriptionForFeatureManagement() {
		return "Dashboard Widgets and Context Settings for Dynatrace(not compatible with AppMon).";
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
		CFW.Files.addAllowedPackage(PACKAGE_MANUAL);
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCE);
		
		//----------------------------------
		// Register Context Settings
		CFW.Registry.ContextSettings.register(DynatraceEnvironment.SETTINGS_TYPE, DynatraceEnvironment.class);
    
		//----------------------------------
		// Register Widgets
		CFW.Registry.Widgets.add(new WidgetHostDetails());
		CFW.Registry.Widgets.add(new WidgetHostEvents());
		CFW.Registry.Widgets.add(new WidgetHostLogs());
		CFW.Registry.Widgets.add(new WidgetHostMetricsChart());
		CFW.Registry.Widgets.add(new WidgetHostProcesses());
		CFW.Registry.Widgets.add(new WidgetHostUnitConsumptionByTags());
		CFW.Registry.Widgets.add(new WidgetProcessEvents());
		CFW.Registry.Widgets.add(new WidgetProcessLogs());
		CFW.Registry.Widgets.add(new WidgetProcessMetricsChart());
		
		//----------------------------------
		// Register Parameters
		CFW.Registry.Parameters.add(new ParameterDefinitionDynatraceEnvironment());
		
		//----------------------------------
		// Register Manual Page
		FeatureEMPCommon.WIDGET_PAGE.addChild(
				new ManualPage("Dynatrace Widgets")
					.faicon("fas fa-desktop")
					.addPermission(FeatureManual.PERMISSION_MANUAL)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_widgets_dynatrace.html")
			);
	}

	@Override
	public void initializeDB() {
		DynatraceEnvironmentManagement.initialize();
		
		//----------------------------------
		// Permissions
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_WIDGETS_DYNATRACE, "user")
					.description("Create and Edit Dynatrace Widgets."),
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
