package com.xresch.emp.features.spm;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.manual.FeatureManual;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.spi.CFWAppFeature;
import com.xresch.emp.features.awa.CFWJobTaskAWAJobIssueAlert;
import com.xresch.emp.features.common.FeatureEMPCommon;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureSPM extends CFWAppFeature {
	
	public static final String PACKAGE_MANUAL = "com.xresch.emp.features.spm.manual";
	public static final String PACKAGE_RESOURCE = "com.xresch.emp.features.spm.resources";
	public static final String FILE_PATH_COMMON_JS = "emp_widget_spm_common.js";
	
	public static final String WIDGET_CATEGORY_SPM = "SPM";
	
	public static final String PERMISSION_WIDGETS_SPM = "Widgets: SPM";
	
	/************************************************************************************
	 * Override to make it managed and return something else then null.
	 ************************************************************************************/
	@Override
	public String getNameForFeatureManagement() {
		return "EMP SPM";
	};
	
	/************************************************************************************
	 * Register a description for the feature management.
	 ************************************************************************************/
	@Override
	public String getDescriptionForFeatureManagement() {
		return "Dashboard Widgets, Context Settings and APIs for Silk Performance Manager(SPM).";
	};
	
	/************************************************************************************
	 * Return if the feature is active by default or if the admin has to enable it.
	 ************************************************************************************/
	public boolean activeByDefault() {
		return false;
	};
	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCE);
		CFW.Files.addAllowedPackage(PACKAGE_MANUAL);
		
		//----------------------------------
		// Register Context Settings
		CFW.Registry.ContextSettings.register(EnvironmentSPM.SETTINGS_TYPE, EnvironmentSPM.class);
		
		//----------------------------------
		// Register Widgets
		CFW.Registry.Widgets.add(new WidgetProjectStatus());
		CFW.Registry.Widgets.add(new WidgetMonitorStatus());
		CFW.Registry.Widgets.add(new WidgetMonitorStatusForProjects());
		CFW.Registry.Widgets.add(new WidgetMonitorStatusAll());
		CFW.Registry.Widgets.add(new WidgetCounterForMonitorStatus());
		CFW.Registry.Widgets.add(new WidgetCounterForProjectStatus());
		CFW.Registry.Widgets.add(new WidgetTimersForMonitor());
		CFW.Registry.Widgets.add(new WidgetTimersForProject());
		CFW.Registry.Widgets.add(new WidgetStatusLegend());
		CFW.Registry.Widgets.add(new WidgetMeasureLegend());
		
		//----------------------------------
		// Register Parameters
		CFW.Registry.Parameters.add(new ParameterDefinitionSPMEnvironment());
		
		//----------------------------------
		// Register Job Task
		CFW.Registry.Jobs.registerTask(new CFWJobTaskSPMMonitorStatusAlert());
		
		//----------------------------------
		// Register Manuals
		registerSPMManual();
		
				
	}

	@Override
	public void initializeDB() {
		
		
		
		//----------------------------------
		// Permissions
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_WIDGETS_SPM, FeatureUserManagement.CATEGORY_USER)
					.description("Create and Edit SPM Widgets."),
				true,
				true);
											
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {
		
		//-----------------------------------------
		// Initialize SPM Environments
		EnvironmentManagerSPM.initialize();
		
		//---------------------------------------------------------
		//Register here as it won't work before DB is initialized.
		CFW.Registry.API.addAll(API_Factory.getAPIDefinitions());
		
	}

	@Override
	public void startTasks() {
		/* do nothing */
	}

	@Override
	public void stopFeature() {
		/* do nothing */
	}

	/**********************************************************************
	 * 
	 **********************************************************************/
	public void registerSPMManual() {
		
    	//----------------------------------
    	// Add SPM Widgets Manual
		CFW.Registry.Manual.addManualPage(null,
				new ManualPage("SPM")
					.faicon("fas fa-desktop")
					.addPermission(FeatureManual.PERMISSION_MANUAL)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "z_manual_widgets_SPM.html")
			);
	}
}
