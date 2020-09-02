package com.xresch.emp.features.theusinator;

import java.util.ArrayList;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWAppFeature;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.config.Configuration;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.manual.FeatureManual;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.response.bootstrap.DynamicItemCreator;
import com.xresch.cfw.response.bootstrap.HierarchicalHTMLItem;
import com.xresch.cfw.response.bootstrap.MenuItem;
import com.xresch.emp._main.Main;
import com.xresch.emp.features.environments.SPMEnvironment;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureTheusinator extends CFWAppFeature {
	
	public static final String RESOURCE_PACKAGE = "com.xresch.emp.features.theusinator.resources";
	
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
		/* do nothing */
	}

	@Override
	public void stopFeature() {
		/* do nothing */
	}

}
