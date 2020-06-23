package com.xresch.emp.features.manual;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWAppFeature;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.manual.FeatureManual;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.emp.features.theusinator.FeatureTheusinator;
import com.xresch.emp.features.widgets.FeatureEMPWidgets;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class FeatureEMPManual extends CFWAppFeature {
	
	public static final String PACKAGE_RESOURCE_THEUS = "com.xreschemp.features.manual.theusinator";
	public static final String PACKAGE_RESOURCE_WIDGETS = "com.xreschemp.features.manual.widgets";
		
	public static final ManualPage TOP_PAGE = CFW.Registry.Manual.addManualPage(null, new ManualPage("Engineered Montitoring Platform(EMP)").faicon("fa fa-desktop"));
	
	/**********************************************************************
	 * 
	 **********************************************************************/
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCE_THEUS);
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCE_WIDGETS);
		//----------------------------------
		// Register Manuals
		registerTheusinatorManual();	
		
		ManualPage widgetsPage = new ManualPage("Dashboard Widgets").faicon("fas fa-th-large")
				.addPermission(FeatureManual.PERMISSION_MANUAL)
				.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCE_WIDGETS, "z_manual_intro.html");
		
		TOP_PAGE.addChild(widgetsPage);
		registerWidgetsManual(widgetsPage);
	}
	
	/**********************************************************************
	 * 
	 **********************************************************************/
	@Override public void initializeDB() {}
	@Override public void addFeature(CFWApplicationExecutor app) { }
	@Override public void startTasks() {}
	@Override public void stopFeature() {}

	
	/**********************************************************************
	 * 
	 **********************************************************************/
	public void registerTheusinatorManual() {
		
    	//----------------------------------
    	// Register Manual Pages
		ManualPage theusinator = new ManualPage("Theusinator").faicon("fas fa-tachometer-alt")
				.addPermission(FeatureTheusinator.PERMISSION_THEUSINATOR);
		
		TOP_PAGE.addChild(theusinator);
		
			theusinator.addChild(
				new ManualPage("Introduction")
					.faicon("fas fa-star")
					.addPermission(FeatureTheusinator.PERMISSION_THEUSINATOR)
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
						.addPermission(FeatureTheusinator.PERMISSION_THEUSINATOR)
						.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCE_THEUS, "z_manual_views.html");
			
			theusinator.addChild(views);
			
				views.addChild(
					new ManualPage("Dashboard View")
						.faicon("fas fa-digital-tachograph")
						.addPermission(FeatureTheusinator.PERMISSION_THEUSINATOR)
						.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCE_THEUS, "z_manual_view_dashboard.html")
				);
				
				views.addChild(
					new ManualPage("Box View")
						.faicon("fas fa-cubes")
						.addPermission(FeatureTheusinator.PERMISSION_THEUSINATOR)
						.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCE_THEUS, "z_manual_view_box.html")
				);
				
				views.addChild(
					new ManualPage("Health History View")
						.faicon("fas fa-heartbeat")
						.addPermission(FeatureTheusinator.PERMISSION_THEUSINATOR)
						.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCE_THEUS, "z_manual_view_healthhistory.html")
				);
				
				views.addChild(
					new ManualPage("Status")
						.faicon("fas fa-circle-notch")
						.addPermission(FeatureTheusinator.PERMISSION_THEUSINATOR)
						.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCE_THEUS, "z_manual_view_status.html")
				);
				
				views.addChild(
					new ManualPage("Graph")
						.faicon("fas fa-chart-bar")
						.addPermission(FeatureTheusinator.PERMISSION_THEUSINATOR)
						.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCE_THEUS, "z_manual_view_graph.html")
				);
				
			//------------------------------
			// Manual Page Details Modal
			theusinator.addChild(
				new ManualPage("Details Modal")
					.faicon("fas fa-search")
					.addPermission(FeatureTheusinator.PERMISSION_THEUSINATOR)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCE_THEUS, "z_manual_modal.html")
			);
			
			//------------------------------
			// Manual Page Details Modal
			theusinator.addChild(
				new ManualPage("Miscellaneous")
					.faicon("fas fa-ellipsis-h")
					.addPermission(FeatureTheusinator.PERMISSION_THEUSINATOR)
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
					.faicon("fas fa-cogs")
					.addPermission(FeatureManual.PERMISSION_MANUAL)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCE_WIDGETS, "z_manual_widgets_AWA.html")
			);
		
		parent.addChild(
			new ManualPage("SPM Widgets")
				.faicon("fas fa-star")
				.addPermission(FeatureManual.PERMISSION_MANUAL)
				.content(HandlingType.JAR_RESOURCE, PACKAGE_RESOURCE_WIDGETS, "z_manual_widgets_SPM.html")
		);
	}
			
}
