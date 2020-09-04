package com.xresch.emp.features.spm;

import java.util.ArrayList;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWAppFeature;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.response.bootstrap.DynamicItemCreator;
import com.xresch.cfw.response.bootstrap.HierarchicalHTMLItem;
import com.xresch.cfw.response.bootstrap.MenuItem;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureSPM extends CFWAppFeature {
	
	public static final String RESOURCE_PACKAGE = "com.xresch.emp.features.spm.resources";
	
	//-----------------------------------------
	// PROD
	//-----------------------------------------
	public static final String PERMISSION_THEUSINATOR = "Theusinator";

	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(RESOURCE_PACKAGE);
		
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

}
