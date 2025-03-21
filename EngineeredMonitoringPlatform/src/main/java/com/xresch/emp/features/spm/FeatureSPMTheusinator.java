package com.xresch.emp.features.spm;

import java.util.ArrayList;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.core.FeatureCore;
import com.xresch.cfw.features.manual.FeatureManual;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.response.bootstrap.CFWHTMLItemDynamic;
import com.xresch.cfw.response.bootstrap.CFWHTMLItem;
import com.xresch.cfw.response.bootstrap.CFWHTMLItemMenuItem;
import com.xresch.cfw.spi.CFWAppFeature;
import com.xresch.emp.features.common.FeatureEMPCommon;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureSPMTheusinator extends CFWAppFeature {
	
	public static final String PERMISSION_THEUSINATOR = "Theusinator";

	/************************************************************************************
	 * Override to make it managed and return something else then null.
	 ************************************************************************************/
	@Override
	public String getNameForFeatureManagement() {
		return "SPM Theusinator";
	};
	
	/************************************************************************************
	 * Register a description for the feature management.
	 ************************************************************************************/
	@Override
	public String getDescriptionForFeatureManagement() {
		return "Theusinator Dashboard for Silk Performance Manager(SPM).";
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
		CFW.Files.addAllowedPackage(FeatureSPM.PACKAGE_RESOURCE);
		CFW.Files.addAllowedPackage(FeatureSPM.PACKAGE_MANUAL);
		//----------------------------------
		// Register Context Settings
		CFW.Registry.ContextSettings.register(EnvironmentSPM.SETTINGS_TYPE, EnvironmentSPM.class);
				
		//----------------------------------
		// Register Manuals
		registerTheusinatorManual();
		
    	//----------------------------------
    	// Register Menus
		CFWHTMLItemMenuItem theusinatorMenu = (CFWHTMLItemMenuItem)new CFWHTMLItemMenuItem("Theusinator") 
			.setDynamicCreator(new CFWHTMLItemDynamic() {		
	
				@Override
				public ArrayList<CFWHTMLItem> createDynamicItems() {
					
					ArrayList<AbstractContextSettings> environments = 
							CFW.DB.ContextSettings.getContextSettingsForTypeAndUser(
									EnvironmentSPM.SETTINGS_TYPE
								  , CFW.Context.Request.getUser());
					
					ArrayList<CFWHTMLItem> childitems = new ArrayList<CFWHTMLItem>();
					
					for(AbstractContextSettings current : environments) {
						EnvironmentSPM spmEnv = (EnvironmentSPM)current;
						childitems.add(
							(CFWHTMLItemMenuItem)new CFWHTMLItemMenuItem(spmEnv.getDefaultObject().name())
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
		
		CFW.Registry.Components.addRegularMenuItem(theusinatorMenu, null);
				
	}

	@Override
	public void initializeDB() {
		
		//-----------------------------------------
		// Add Theusinator Menu
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_THEUSINATOR, FeatureUserManagement.CATEGORY_USER)
					.description("View and analyze productive SPM Monitoring status using the Theusinator Dashboard."),
				true,
				true);
									
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
		//-----------------------------------------
		// Initialize SPM Environments
		EnvironmentManagerSPM.initialize();
		
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
	public void registerTheusinatorManual() {
				
    	//----------------------------------
    	// Register Theusinator Manual
		ManualPage theusinator = new ManualPage("Theusinator").faicon("fas fa-tachometer-alt")
				.addPermission(FeatureSPMTheusinator.PERMISSION_THEUSINATOR);
		
		CFW.Registry.Manual.addManualPage(null, theusinator);
		
			theusinator.addChild(
				new ManualPage("Introduction")
					.faicon("fas fa-star")
					.addPermission(FeatureSPMTheusinator.PERMISSION_THEUSINATOR)
					.content(HandlingType.JAR_RESOURCE, FeatureSPM.PACKAGE_MANUAL, "manual_theusinator_intro.html")
			);
			
			theusinator.addChild(
				new ManualPage("Setup")
					.faicon("fas fa-check-square")
					.addPermission(FeatureManual.PERMISSION_ADMIN_MANUAL)
					.content(HandlingType.JAR_RESOURCE, FeatureSPM.PACKAGE_MANUAL, "manual_theusinator_setup.html")
			);
			
			//------------------------------
			// Manual Pages for Views
			ManualPage views =
				new ManualPage("Views")
						.faicon("fas fa-binoculars")
						.addPermission(FeatureSPMTheusinator.PERMISSION_THEUSINATOR)
						.content(HandlingType.JAR_RESOURCE, FeatureSPM.PACKAGE_MANUAL, "manual_theusinator_views.html");
			
			theusinator.addChild(views);
			
				views.addChild(
					new ManualPage("Dashboard View")
						.faicon("fas fa-digital-tachograph")
						.addPermission(FeatureSPMTheusinator.PERMISSION_THEUSINATOR)
						.content(HandlingType.JAR_RESOURCE, FeatureSPM.PACKAGE_MANUAL, "manual_theusinator_view_dashboard.html")
				);
				
				views.addChild(
					new ManualPage("Box View")
						.faicon("fas fa-cubes")
						.addPermission(FeatureSPMTheusinator.PERMISSION_THEUSINATOR)
						.content(HandlingType.JAR_RESOURCE, FeatureSPM.PACKAGE_MANUAL, "manual_theusinator_view_box.html")
				);
				
				views.addChild(
					new ManualPage("Health History View")
						.faicon("fas fa-heartbeat")
						.addPermission(FeatureSPMTheusinator.PERMISSION_THEUSINATOR)
						.content(HandlingType.JAR_RESOURCE, FeatureSPM.PACKAGE_MANUAL, "manual_theusinator_view_healthhistory.html")
				);
				
				views.addChild(
					new ManualPage("Status")
						.faicon("fas fa-circle-notch")
						.addPermission(FeatureSPMTheusinator.PERMISSION_THEUSINATOR)
						.content(HandlingType.JAR_RESOURCE, FeatureSPM.PACKAGE_MANUAL, "manual_theusinator_view_status.html")
				);
				
				views.addChild(
					new ManualPage("Graph")
						.faicon("fas fa-chart-bar")
						.addPermission(FeatureSPMTheusinator.PERMISSION_THEUSINATOR)
						.content(HandlingType.JAR_RESOURCE, FeatureSPM.PACKAGE_MANUAL, "manual_theusinator_view_graph.html")
				);
				
			//------------------------------
			// Manual Page Details Modal
			theusinator.addChild(
				new ManualPage("Details Modal")
					.faicon("fas fa-search")
					.addPermission(FeatureSPMTheusinator.PERMISSION_THEUSINATOR)
					.content(HandlingType.JAR_RESOURCE, FeatureSPM.PACKAGE_MANUAL, "manual_theusinator_modal.html")
			);
			
			//------------------------------
			// Manual Page Details Modal
			theusinator.addChild(
				new ManualPage("Miscellaneous")
					.faicon("fas fa-ellipsis-h")
					.addPermission(FeatureSPMTheusinator.PERMISSION_THEUSINATOR)
					.content(HandlingType.JAR_RESOURCE, FeatureSPM.PACKAGE_MANUAL, "manual_theusinator_misc.html")
			);
	}
}
