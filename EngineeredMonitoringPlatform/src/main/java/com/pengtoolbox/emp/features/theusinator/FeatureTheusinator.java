package com.pengtoolbox.emp.features.theusinator;

import org.eclipse.jetty.servlet.ServletContextHandler;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw._main.CFWAppFeature;
import com.pengtoolbox.cfw._main.CFWApplication;
import com.pengtoolbox.cfw.datahandling.CFWField.FormFieldType;
import com.pengtoolbox.cfw.features.config.Configuration;
import com.pengtoolbox.cfw.features.usermgmt.Permission;
import com.pengtoolbox.cfw.features.usermgmt.Role;
import com.pengtoolbox.cfw.response.bootstrap.MenuItem;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, © 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class FeatureTheusinator extends CFWAppFeature {
	
	public static final String RESOURCE_PACKAGE = "com.pengtoolbox.emp.features.theusinator.resources";
	
	public static final String PERMISSION_THEUSINATOR_PROD = "Theusinator Prod";
	public static final String PERMISSION_THEUSINATOR_PREPROD = "Theusinator Pre-Prod";
	
	public static final String CONFIG_SPM_PROD_URL = "SPM Production URL";
	public static final String CONFIG_SPM_PROD_APIUSER = "SPM Production API User";
	public static final String CONFIG_SPM_PROD_PASSWORD = "SPM Production Password";
	
	public static final String CONFIG_SPM_PREPROD_URL = "SPM Pre-Production URL";
	public static final String CONFIG_SPM_PREPROD_APIUSER = "SPM Pre-Production API User";
	public static final String CONFIG_SPM_PREPROD_PASSWORD = "SPM Pre-Production Password";

	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(RESOURCE_PACKAGE);
		
		//----------------------------------
		// Register Objects
		//CFW.Registry.Objects.addCFWObject(CPUSampleSignature.class);

    	//----------------------------------
    	// Register Regular Menu
		
		CFW.Registry.Components.addRegularMenuItem(
				(MenuItem)new MenuItem("Theusinator")
					.faicon("fas fa-tachometer-alt")
				, null);
		
		CFW.Registry.Components.addRegularMenuItem(
				(MenuItem)new MenuItem("Production")
					.faicon("fas fa-cogs")
					.addPermission(PERMISSION_THEUSINATOR_PROD)
					.href("./theusinator?env=prod")	
				, "Theusinator");
		
		CFW.Registry.Components.addRegularMenuItem(
				(MenuItem)new MenuItem("Pre-Production")
					.faicon("fas fa-flask")
					.addPermission(PERMISSION_THEUSINATOR_PREPROD)
					.href("./theusinator?env=preprod")	
				, "Theusinator");
	}

	@Override
	public void initializeDB() {
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_THEUSINATOR_PROD, "user")
					.description("View and analyze productive SPM Monitoring status using the Theusinator Dashboard.")
					.isDeletable(false),
				true,
				true);
					
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_THEUSINATOR_PREPROD, "user")
					.description("View and analyze pre-productive SPM Monitoring status using the Theusinator Dashboard.")
					.isDeletable(false),
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
	public void addFeature(CFWApplication app) {	
        app.addAppServlet(ServletTheusinator.class,  "/theusinator");
	}

	@Override
	public void startTasks() {

	}

	@Override
	public void stopFeature() {

	}

}
