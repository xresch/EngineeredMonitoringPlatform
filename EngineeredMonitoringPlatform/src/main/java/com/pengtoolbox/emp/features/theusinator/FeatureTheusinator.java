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
import com.pengtoolbox.emp.features.environments.EnvironmentSPM;

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
	
	public static final String CONFIG_SPM_PROD_URL = "SPM Production URL";
	public static final String CONFIG_SPM_PROD_APIUSER = "SPM Production API User";
	public static final String CONFIG_SPM_PROD_PASSWORD = "SPM Production Password";
		
	//-----------------------------------------
	// PRE PROD
	//-----------------------------------------
	public static final String CONFIG_SPM_PREPROD_URL = "SPM Pre-Production URL";
	public static final String CONFIG_SPM_PREPROD_APIUSER = "SPM Pre-Production API User";
	public static final String CONFIG_SPM_PREPROD_PASSWORD = "SPM Pre-Production Password";
	
	
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
					
					ArrayList<AbstractContextSettings> environments = CFW.DB.ContextSettings.getContextSettingsForType(EnvironmentSPM.SETTINGS_TYPE);
					System.out.println("environments.size(): "+environments.size());
					
					ArrayList<HierarchicalHTMLItem> childitems = new ArrayList<HierarchicalHTMLItem>();
					
					for(AbstractContextSettings current : environments) {
						EnvironmentSPM spmEnv = (EnvironmentSPM)current;
						System.out.println("spmEnv.getWrapper().name(): "+spmEnv.getWrapper().name());
						childitems.add(
							(MenuItem)new MenuItem(spmEnv.getWrapper().name())
								.addPermission(PERMISSION_THEUSINATOR)
								.href("/app/theusinator?env="+spmEnv.getWrapper().id()) 
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
		
    	//----------------------------------
    	// Register Manual Pages
		ManualPage theusinator = new ManualPage("Theusinator").faicon("fas fa-tachometer-alt")
				.addPermission(PERMISSION_THEUSINATOR);
		
		Main.TOP_MANUAL_PAGE.addChild(theusinator);
		
			theusinator.addChild(
				new ManualPage("Introduction")
					.faicon("fas fa-star")
					.addPermission(PERMISSION_THEUSINATOR)
					.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "z_manual_intro.html")
			);
			
			theusinator.addChild(
				new ManualPage("Setup")
					.faicon("fas fa-check-square")
					.addPermission(FeatureManual.PERMISSION_ADMIN_MANUAL)
					.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "z_manual_setup.html")
			);
			
			//------------------------------
			// Manual Pages for Views
			ManualPage views =
				new ManualPage("Views")
						.faicon("fas fa-binoculars")
						.addPermission(PERMISSION_THEUSINATOR)
						.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "z_manual_views.html");
			
			theusinator.addChild(views);
			
				views.addChild(
					new ManualPage("Dashboard View")
						.faicon("fas fa-digital-tachograph")
						.addPermission(PERMISSION_THEUSINATOR)
						.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "z_manual_view_dashboard.html")
				);
				
				views.addChild(
					new ManualPage("Box View")
						.faicon("fas fa-cubes")
						.addPermission(PERMISSION_THEUSINATOR)
						.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "z_manual_view_box.html")
				);
				
				views.addChild(
					new ManualPage("Health History View")
						.faicon("fas fa-heartbeat")
						.addPermission(PERMISSION_THEUSINATOR)
						.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "z_manual_view_healthhistory.html")
				);
				
				views.addChild(
					new ManualPage("Status")
						.faicon("fas fa-circle-notch")
						.addPermission(PERMISSION_THEUSINATOR)
						.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "z_manual_view_status.html")
				);
				
				views.addChild(
					new ManualPage("Graph")
						.faicon("fas fa-chart-bar")
						.addPermission(PERMISSION_THEUSINATOR)
						.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "z_manual_view_graph.html")
				);
				
			//------------------------------
			// Manual Page Details Modal
			theusinator.addChild(
				new ManualPage("Details Modal")
					.faicon("fas fa-search")
					.addPermission(PERMISSION_THEUSINATOR)
					.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "z_manual_modal.html")
			);
			
			//------------------------------
			// Manual Page Details Modal
			theusinator.addChild(
				new ManualPage("Miscellaneous")
					.faicon("fas fa-ellipsis-h")
					.addPermission(PERMISSION_THEUSINATOR)
					.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "z_manual_misc.html")
			);
					
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
							
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Silk Performance Manager", CONFIG_SPM_PROD_URL)
				.description("The URL of the productive SPM instance.")
				.type(FormFieldType.TEXT)
		);
	
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Silk Performance Manager", CONFIG_SPM_PROD_APIUSER)
				.description("The username of the user that will access the API.")
				.type(FormFieldType.TEXT)
		);

		CFW.DB.Config.oneTimeCreate(
			new Configuration("Silk Performance Manager", CONFIG_SPM_PROD_PASSWORD)
				.description("The password of the API User.")
				.type(FormFieldType.PASSWORD)
		);
	
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Silk Performance Manager", CONFIG_SPM_PREPROD_URL)
				.description("The URL of the pre-productive SPM instance.")
				.type(FormFieldType.TEXT)
		);
	
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Silk Performance Manager", CONFIG_SPM_PREPROD_APIUSER)
				.description("The username of the user that will access the API.")
				.type(FormFieldType.TEXT)
		);

		CFW.DB.Config.oneTimeCreate(
			new Configuration("Silk Performance Manager", CONFIG_SPM_PREPROD_PASSWORD)
				.description("The password of the API User.")
				.type(FormFieldType.PASSWORD)
		);
		
		
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
