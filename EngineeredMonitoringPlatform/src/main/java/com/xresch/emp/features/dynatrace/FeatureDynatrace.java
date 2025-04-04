package com.xresch.emp.features.dynatrace;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.manual.FeatureManual;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.spi.CFWAppFeature;
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
	
	public static final String WIDGET_PREFIX = "emp_dynatrace";
	public static final String WIDGET_CATEGORY_DYNATRACE = "Dynatrace";
	
	public static ManualPage MANUALPAGE_PARENT;
	
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
		CFW.Registry.Widgets.add(new WidgetHostMetricsStatus());
		CFW.Registry.Widgets.add(new WidgetHostProcesses());
		CFW.Registry.Widgets.add(new WidgetHostUnitConsumptionByTags());
		CFW.Registry.Widgets.add(new WidgetProcessEvents());
		CFW.Registry.Widgets.add(new WidgetProcessLogs());
		CFW.Registry.Widgets.add(new WidgetProcessMetricsChart());
		
		//----------------------------------
		// Register Parameters
		CFW.Registry.Parameters.add(new ParameterDefinitionDynatraceEnvironment());
		
		//----------------------------------
		// Register Manual
		registerManual();
	}

	@Override
	public void initializeDB() {
		//----------------------------------
		// Permissions
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_WIDGETS_DYNATRACE, FeatureUserManagement.CATEGORY_USER)
					.description("Create and Edit Dynatrace Widgets."),
				true,
				true);
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
		DynatraceEnvironmentManagement.initialize();
	}

	@Override
	public void startTasks() {
		/* do nothing */
	}

	@Override
	public void stopFeature() {
		/* do nothing */
	}
	
	
	public void registerManual() {
		
		//----------------------------------
		// Register Parent
		MANUALPAGE_PARENT = 
				new ManualPage("Dynatrace")
					.faicon("fas fa-desktop")
					.addPermission(FeatureManual.PERMISSION_MANUAL)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_dynatrace.html")
					;
		
		CFW.Registry.Manual.addManualPage(null, MANUALPAGE_PARENT);
		
		//----------------------------------
		// Register Queries
		MANUALPAGE_PARENT.addChild(
			new ManualPage("Dynatrace Queries")
			.faicon("fas fa-code")
			.addPermission(FeatureManual.PERMISSION_MANUAL)
			.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_dynatrace_queries.html")
		);
		
	}

}
