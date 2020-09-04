package com.xresch.emp.features.common;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWAppFeature;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.manual.FeatureManual;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.emp.features.spm.FeatureSPM;
import com.xresch.emp.features.webex.FeatureWebex;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureEMPCommon extends CFWAppFeature {
	public static final String RESOURCE_PACKAGE = "com.xresch.emp.features.common.resources";
	
	public static final String PACKAGE_RESOURCE_THEUS = "com.xresch.emp.features.common.theusinator";
	public static final String PACKAGE_RESOURCE_WIDGETS = "com.xresch.emp.features.common.widgets";
		
	public static final ManualPage TOP_PAGE = CFW.Registry.Manual.addManualPage(null, new ManualPage("Engineered Montitoring Platform(EMP)").faicon("fa fa-desktop"));
	
	/**********************************************************************
	 * 
	 **********************************************************************/
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(RESOURCE_PACKAGE);
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCE_THEUS);
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCE_WIDGETS);
		
		//----------------------------------
		// Register Manuals	
		ManualPage widgetsPage = new ManualPage("Dashboard Widgets").faicon("fas fa-th-large")
				.addPermission(FeatureManual.PERMISSION_MANUAL)
				.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCE_WIDGETS, "z_manual_intro.html");
		
		TOP_PAGE.addChild(widgetsPage);
		registerWidgetsManual(widgetsPage);
		
		registerTheusinatorManual();
	}
	
	/**********************************************************************
	 * 
	 **********************************************************************/
	@Override public void initializeDB() { /* do nothing */ }
	@Override public void addFeature(CFWApplicationExecutor app) { /* do nothing */ }
	@Override public void startTasks() { /* do nothing */ }
	@Override public void stopFeature() { /* do nothing */ }

	
	/**********************************************************************
	 * 
	 **********************************************************************/
	public void registerTheusinatorManual() {
		
    	//----------------------------------
    	// Register Manual Pages
		ManualPage theusinator = new ManualPage("Theusinator").faicon("fas fa-tachometer-alt")
				.addPermission(FeatureSPM.PERMISSION_THEUSINATOR);
		
		TOP_PAGE.addChild(theusinator);
		
			theusinator.addChild(
				new ManualPage("Introduction")
					.faicon("fas fa-star")
					.addPermission(FeatureSPM.PERMISSION_THEUSINATOR)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCE_THEUS, "z_manual_intro.html")
			);
			
			theusinator.addChild(
				new ManualPage("Setup")
					.faicon("fas fa-check-square")
					.addPermission(FeatureManual.PERMISSION_ADMIN_MANUAL)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCE_THEUS, "z_manual_setup.html")
			);
			
			//------------------------------
			// Manual Pages for Views
			ManualPage views =
				new ManualPage("Views")
						.faicon("fas fa-binoculars")
						.addPermission(FeatureSPM.PERMISSION_THEUSINATOR)
						.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCE_THEUS, "z_manual_views.html");
			
			theusinator.addChild(views);
			
				views.addChild(
					new ManualPage("Dashboard View")
						.faicon("fas fa-digital-tachograph")
						.addPermission(FeatureSPM.PERMISSION_THEUSINATOR)
						.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCE_THEUS, "z_manual_view_dashboard.html")
				);
				
				views.addChild(
					new ManualPage("Box View")
						.faicon("fas fa-cubes")
						.addPermission(FeatureSPM.PERMISSION_THEUSINATOR)
						.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCE_THEUS, "z_manual_view_box.html")
				);
				
				views.addChild(
					new ManualPage("Health History View")
						.faicon("fas fa-heartbeat")
						.addPermission(FeatureSPM.PERMISSION_THEUSINATOR)
						.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCE_THEUS, "z_manual_view_healthhistory.html")
				);
				
				views.addChild(
					new ManualPage("Status")
						.faicon("fas fa-circle-notch")
						.addPermission(FeatureSPM.PERMISSION_THEUSINATOR)
						.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCE_THEUS, "z_manual_view_status.html")
				);
				
				views.addChild(
					new ManualPage("Graph")
						.faicon("fas fa-chart-bar")
						.addPermission(FeatureSPM.PERMISSION_THEUSINATOR)
						.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCE_THEUS, "z_manual_view_graph.html")
				);
				
			//------------------------------
			// Manual Page Details Modal
			theusinator.addChild(
				new ManualPage("Details Modal")
					.faicon("fas fa-search")
					.addPermission(FeatureSPM.PERMISSION_THEUSINATOR)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCE_THEUS, "z_manual_modal.html")
			);
			
			//------------------------------
			// Manual Page Details Modal
			theusinator.addChild(
				new ManualPage("Miscellaneous")
					.faicon("fas fa-ellipsis-h")
					.addPermission(FeatureSPM.PERMISSION_THEUSINATOR)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCE_THEUS, "z_manual_misc.html")
			);
	}
	
	/**********************************************************************
	 * 
	 **********************************************************************/
	public void registerWidgetsManual(ManualPage parent) {
		parent.addChild(
				new ManualPage("Display Examples")
					.faicon("fas fa-image")
					.addPermission(FeatureManual.PERMISSION_MANUAL)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCE_WIDGETS, "z_manual_widgets_displayexamples.html")
			);
		
		parent.addChild(
				new ManualPage("AWA Widgets")
					.faicon("fas fa-desktop")
					.addPermission(FeatureManual.PERMISSION_MANUAL)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCE_WIDGETS, "z_manual_widgets_AWA.html")
			);
		
		parent.addChild(
				new ManualPage("Prometheus Widgets")
					.faicon("fas fa-desktop")
					.addPermission(FeatureManual.PERMISSION_MANUAL)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCE_WIDGETS, "z_manual_widgets_Prometheus.html")
			);
		
		parent.addChild(
			new ManualPage("SPM Widgets")
				.faicon("fas fa-desktop")
				.addPermission(FeatureManual.PERMISSION_MANUAL)
				.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCE_WIDGETS, "z_manual_widgets_SPM.html")
		);
	}
			
}
