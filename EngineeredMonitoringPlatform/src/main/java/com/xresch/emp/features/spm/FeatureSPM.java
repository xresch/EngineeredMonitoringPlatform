package com.xresch.emp.features.spm;

import java.util.ArrayList;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWAppFeature;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.manual.FeatureManual;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.response.bootstrap.DynamicItemCreator;
import com.xresch.cfw.response.bootstrap.HierarchicalHTMLItem;
import com.xresch.cfw.response.bootstrap.MenuItem;
import com.xresch.emp.features.common.FeatureEMPCommon;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureSPM extends CFWAppFeature {
	
	public static final String PACKAGE_MANUAL = "com.xresch.emp.features.spm.manual";
	public static final String PACKAGE_RESOURCE = "com.xresch.emp.features.spm.resources";
	
	//-----------------------------------------
	// PROD
	//-----------------------------------------
	public static final String PERMISSION_THEUSINATOR = "Theusinator";

	
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
		// Register Manuals
		registerSPMManual();
		
    	//----------------------------------
    	// Register Menus
		MenuItem theusinatorMenu = (MenuItem)new MenuItem("Theusinator") 
			.setDynamicCreator(new DynamicItemCreator() {		
	
				@Override
				public ArrayList<HierarchicalHTMLItem> createDynamicItems() {
					
					ArrayList<AbstractContextSettings> environments = CFW.DB.ContextSettings.getContextSettingsForType(EnvironmentSPM.SETTINGS_TYPE);
					ArrayList<HierarchicalHTMLItem> childitems = new ArrayList<HierarchicalHTMLItem>();
					
					for(AbstractContextSettings current : environments) {
						EnvironmentSPM spmEnv = (EnvironmentSPM)current;
						childitems.add(
							(MenuItem)new MenuItem(spmEnv.getDefaultObject().name())
								.addPermission(PERMISSION_THEUSINATOR)
								.href("/app/theusinator?env="+spmEnv.getDefaultObject().id()) 
						);
					}
					return childitems;
				}
			});
		
		theusinatorMenu
			.faicon("fas fa-grip-horizontal")
			.addPermission(PERMISSION_THEUSINATOR);
		
		CFW.Registry.Components.addRegularMenuItem(theusinatorMenu, "Dashboards");
				
	}

	@Override
	public void initializeDB() {
		
		//-----------------------------------------
		// Initialize SPM Environments
		EnvironmentManagerSPM.initialize();
		
		//-----------------------------------------
		// Add Theusinator Menu
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_THEUSINATOR, "user")
					.description("View and analyze productive SPM Monitoring status using the Theusinator Dashboard."),
				true,
				true);
									
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
		//---------------------------------------------------------
		//Register here as it won't work before DB is initialized.
		CFW.Registry.API.addAll(API_Factory.getAPIDefinitions());
		
		//---------------------------------------------------------
		// Add Theusinator Servlet
        app.addAppServlet(ServletTheusinator.class,  "/theusinator");
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
		FeatureEMPCommon.WIDGET_PAGE.addChild(
				new ManualPage("SPM Widgets")
					.faicon("fas fa-desktop")
					.addPermission(FeatureManual.PERMISSION_MANUAL)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "z_manual_widgets_SPM.html")
			);
    	//----------------------------------
    	// Register Theusinator Manual
		ManualPage theusinator = new ManualPage("Theusinator").faicon("fas fa-tachometer-alt")
				.addPermission(FeatureSPM.PERMISSION_THEUSINATOR);
		
		FeatureEMPCommon.TOP_PAGE.addChild(theusinator);
		
			theusinator.addChild(
				new ManualPage("Introduction")
					.faicon("fas fa-star")
					.addPermission(FeatureSPM.PERMISSION_THEUSINATOR)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_theusinator_intro.html")
			);
			
			theusinator.addChild(
				new ManualPage("Setup")
					.faicon("fas fa-check-square")
					.addPermission(FeatureManual.PERMISSION_ADMIN_MANUAL)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_theusinator_setup.html")
			);
			
			//------------------------------
			// Manual Pages for Views
			ManualPage views =
				new ManualPage("Views")
						.faicon("fas fa-binoculars")
						.addPermission(FeatureSPM.PERMISSION_THEUSINATOR)
						.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_theusinator_views.html");
			
			theusinator.addChild(views);
			
				views.addChild(
					new ManualPage("Dashboard View")
						.faicon("fas fa-digital-tachograph")
						.addPermission(FeatureSPM.PERMISSION_THEUSINATOR)
						.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_theusinator_view_dashboard.html")
				);
				
				views.addChild(
					new ManualPage("Box View")
						.faicon("fas fa-cubes")
						.addPermission(FeatureSPM.PERMISSION_THEUSINATOR)
						.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_theusinator_view_box.html")
				);
				
				views.addChild(
					new ManualPage("Health History View")
						.faicon("fas fa-heartbeat")
						.addPermission(FeatureSPM.PERMISSION_THEUSINATOR)
						.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_theusinator_view_healthhistory.html")
				);
				
				views.addChild(
					new ManualPage("Status")
						.faicon("fas fa-circle-notch")
						.addPermission(FeatureSPM.PERMISSION_THEUSINATOR)
						.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_theusinator_view_status.html")
				);
				
				views.addChild(
					new ManualPage("Graph")
						.faicon("fas fa-chart-bar")
						.addPermission(FeatureSPM.PERMISSION_THEUSINATOR)
						.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_theusinator_view_graph.html")
				);
				
			//------------------------------
			// Manual Page Details Modal
			theusinator.addChild(
				new ManualPage("Details Modal")
					.faicon("fas fa-search")
					.addPermission(FeatureSPM.PERMISSION_THEUSINATOR)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_theusinator_modal.html")
			);
			
			//------------------------------
			// Manual Page Details Modal
			theusinator.addChild(
				new ManualPage("Miscellaneous")
					.faicon("fas fa-ellipsis-h")
					.addPermission(FeatureSPM.PERMISSION_THEUSINATOR)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "manual_theusinator_misc.html")
			);
	}
}
