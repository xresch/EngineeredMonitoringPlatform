package com.pengtoolbox.emp.features.widgets;

import org.eclipse.jetty.servlet.ServletContextHandler;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw._main.CFWAppFeature;
import com.pengtoolbox.cfw._main.CFWApplicationExecutor;
import com.pengtoolbox.cfw.caching.FileDefinition.HandlingType;
import com.pengtoolbox.cfw.datahandling.CFWField.FormFieldType;
import com.pengtoolbox.cfw.features.config.Configuration;
import com.pengtoolbox.cfw.features.manual.FeatureManual;
import com.pengtoolbox.cfw.features.manual.ManualPage;
import com.pengtoolbox.cfw.features.usermgmt.Permission;
import com.pengtoolbox.cfw.features.usermgmt.Role;
import com.pengtoolbox.cfw.response.bootstrap.MenuItem;
import com.pengtoolbox.emp._main.Main;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, � 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class FeatureEMPWidgets extends CFWAppFeature {
	
	public static final String RESOURCE_PACKAGE = "com.pengtoolbox.emp.features.widgets.recourses";
	
	public static final String PERMISSION_AWAJOBSTATUS_PROD = "AWA Jobstatus Prod";
	public static final String PERMISSION_AWAJOBSTATUS_PREPROD = "AWA Jobstatus Pre-Prod";
	
	public static final String CONFIG_AWA_PROD_URL = "AWA PROD DB URL";
	public static final String CONFIG_AWA_PROD_DBNAME = "AWA PROD DB Name";
	public static final String CONFIG_AWA_PROD_DBUSER = "AWA PROD DB User";
	public static final String CONFIG_AWA_PROD_PASSWORD = "AWA PROD Password";
	
	public static final String CONFIG_AWA_PREPROD_URL = "AWA PRE-PROD URL";
	public static final String CONFIG_AWA_PREPROD_DBNAME = "AWA PRE-PROD DB Name";
	public static final String CONFIG_AWA_PREPROD_DBUSER = "AWA PRE-PROD DB User";
	public static final String CONFIG_AWA_PREPROD_PASSWORD = "AWA PRE-PROD Password";

	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(RESOURCE_PACKAGE);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.Registry.Widgets.add(new WidgetAWAJobStatus());
		
		//----------------------------------
		// Register Objects
		//CFW.Registry.Objects.addCFWObject(CPUSampleSignature.class);


    	//----------------------------------
    	// Register Manual Pages
//		ManualPage theusinator = new ManualPage("Theusinator").faicon("fas fa-tachometer-alt")
//				.addPermission(PERMISSION_AWAJOBSTATUS_PROD)
//				.addPermission(PERMISSION_AWAJOBSTATUS_PREPROD);
//		
//		Main.TOP_MANUAL_PAGE.addChild(theusinator);
//		
//			theusinator.addChild(
//				new ManualPage("Introduction")
//					.faicon("fas fa-star")
//					.addPermission(PERMISSION_AWAJOBSTATUS_PROD)
//					.addPermission(PERMISSION_AWAJOBSTATUS_PREPROD)
//					.content(HandlingType.JAR_RESOURCE, RESOURCE_PACKAGE, "z_manual_intro.html")
//			);
					
	}

	@Override
	public void initializeDB() {
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_AWAJOBSTATUS_PROD, "user")
					.description("View and analyze productive AWA job status."),
				true,
				true);
					
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_AWAJOBSTATUS_PREPROD, "user")
					.description("View and analyze pre-productive AWA job status."),
				true,
				true);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration("AWA Job Status", CONFIG_AWA_PROD_URL)
				.description("The URL of the productive AWA Database instance.")
				.type(FormFieldType.TEXT)
		);
		
		CFW.DB.Config.oneTimeCreate(
			new Configuration("AWA Job Status", CONFIG_AWA_PROD_DBNAME)
				.description("The name of the productive database used to fetch the job status from.")
				.type(FormFieldType.TEXT)
		);
		
		CFW.DB.Config.oneTimeCreate(
			new Configuration("AWA Job Status", CONFIG_AWA_PROD_DBUSER)
				.description("The username of the user that will access the API.")
				.type(FormFieldType.TEXT)
		);

		CFW.DB.Config.oneTimeCreate(
			new Configuration("AWA Job Status", CONFIG_AWA_PROD_PASSWORD)
				.description("The password of the API User.")
				.type(FormFieldType.PASSWORD)
		);

	
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration("AWA Job Status", CONFIG_AWA_PREPROD_URL)
				.description("The URL of the pre-productive AWA Database instance.")
				.type(FormFieldType.TEXT)
		);
	
		CFW.DB.Config.oneTimeCreate(
				new Configuration("AWA Job Status", CONFIG_AWA_PREPROD_URL)
					.description("The name of the pre-productive database used to fetch the job status from.")
					.type(FormFieldType.TEXT)
			);
			
		CFW.DB.Config.oneTimeCreate(
			new Configuration("AWA Job Status", CONFIG_AWA_PREPROD_DBUSER)
				.description("The username of the user that will access the DB.")
				.type(FormFieldType.TEXT)
		);

		CFW.DB.Config.oneTimeCreate(
			new Configuration("AWA Job Status", CONFIG_AWA_PREPROD_PASSWORD)
				.description("The password of the DB User.")
				.type(FormFieldType.PASSWORD)
		);
		
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
        //app.addAppServlet(ServletTheusinator.class,  "/theusinator");
	}

	@Override
	public void startTasks() {

	}

	@Override
	public void stopFeature() {

	}

}
