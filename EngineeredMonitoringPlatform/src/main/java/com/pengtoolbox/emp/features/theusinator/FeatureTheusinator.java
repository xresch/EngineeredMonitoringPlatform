package com.pengtoolbox.emp.features.theusinator;

import java.util.ArrayList;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw._main.CFWAppFeature;
import com.pengtoolbox.cfw._main.CFWApplicationExecutor;
import com.pengtoolbox.cfw.caching.FileDefinition.HandlingType;
import com.pengtoolbox.cfw.datahandling.CFWField.FormFieldType;
import com.pengtoolbox.cfw.features.config.Configuration;
import com.pengtoolbox.cfw.features.contextsettings.AbstractContextSettings;
import com.pengtoolbox.cfw.features.manual.FeatureManual;
import com.pengtoolbox.cfw.features.manual.ManualPage;
import com.pengtoolbox.cfw.features.usermgmt.Permission;
import com.pengtoolbox.cfw.response.bootstrap.DynamicItemCreator;
import com.pengtoolbox.cfw.response.bootstrap.HierarchicalHTMLItem;
import com.pengtoolbox.cfw.response.bootstrap.MenuItem;
import com.pengtoolbox.emp._main.Main;
import com.pengtoolbox.emp.features.environments.SPMEnvironment;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, � 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class FeatureTheusinator extends CFWAppFeature {
	
	public static final String RESOURCE_PACKAGE = "com.pengtoolbox.emp.features.theusinator.resources";
	
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
    	// Register Menus
		MenuItem theusinatorMenu = (MenuItem)new MenuItem("Theusinator") 
			.setDynamicCreator(new DynamicItemCreator() {		
	
				@Override
				public ArrayList<HierarchicalHTMLItem> createDynamicItems() {
					
					ArrayList<AbstractContextSettings> environments = CFW.DB.ContextSettings.getContextSettingsForType(SPMEnvironment.SETTINGS_TYPE);
					ArrayList<HierarchicalHTMLItem> childitems = new ArrayList<HierarchicalHTMLItem>();
					
					for(AbstractContextSettings current : environments) {
						SPMEnvironment spmEnv = (SPMEnvironment)current;
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
		
//		CFW.Registry.Components.addRegularMenuItem(
//				(MenuItem)new MenuItem("Production")
//					.faicon("fas fa-cogs")
//					.addPermission(PERMISSION_THEUSINATOR)
//					.href("/app/theusinator?env=prod")	
//				, "Dashboards | Theusinator");
//		
		
					
	}

	@Override
	public void initializeDB() {
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_THEUSINATOR, "user")
					.description("View and analyze productive SPM Monitoring status using the Theusinator Dashboard."),
				true,
				true);
									
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
        app.addAppServlet(ServletTheusinator.class,  "/theusinator");
	}

	@Override
	public void startTasks() {

	}

	@Override
	public void stopFeature() {

	}

}
