package com.pengtoolbox.emp.features.widgets;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw._main.CFWAppFeature;
import com.pengtoolbox.cfw._main.CFWApplicationExecutor;
import com.pengtoolbox.cfw.datahandling.CFWField.FormFieldType;
import com.pengtoolbox.cfw.features.config.Configuration;
import com.pengtoolbox.cfw.features.usermgmt.Permission;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, � 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class FeatureEMPWidgets extends CFWAppFeature {
	
	public static final String RESOURCE_PACKAGE = "com.pengtoolbox.emp.features.widgets.recourses";
	
	public static final String PERMISSION_AWAJOBSTATUS_PROD = "AWA Jobstatus Prod";
	public static final String PERMISSION_AWAJOBSTATUS_PREPROD = "AWA Jobstatus Pre-Prod";
	
	public static final String CONFIG_AWA_PROD_DBHOST 		= "AWA PROD DB Host";
	public static final String CONFIG_AWA_PROD_DBPORT 		= "AWA PROD DB Port";
	public static final String CONFIG_AWA_PROD_DBNAME 		= "AWA PROD DB Name";
	public static final String CONFIG_AWA_PROD_DBTYPE 		= "AWA PROD DB Type";
	public static final String CONFIG_AWA_PROD_DBUSER 		= "AWA PROD DB User";
	public static final String CONFIG_AWA_PROD_PASSWORD 	= "AWA PROD Password";
	
	public static final String CONFIG_AWA_PREPROD_DBHOST 	= "AWA PRE-PROD DB Host";
	public static final String CONFIG_AWA_PREPROD_DBPORT 	= "AWA PRE-PROD DB Port";
	public static final String CONFIG_AWA_PREPROD_DBNAME 	= "AWA PRE-PROD DB Name";
	public static final String CONFIG_AWA_PREPROD_DBTYPE 	= "AWA PRE-PROD DB Type";
	public static final String CONFIG_AWA_PREPROD_DBUSER 	= "AWA PRE-PROD DB User";
	public static final String CONFIG_AWA_PREPROD_PASSWORD 	= "AWA PRE-PROD Password";
	
	public static final String CONFIG_SPM_PREPROD_DB_HOST = "SPM Pre-Production DB Host";
	public static final String CONFIG_SPM_PREPROD_DB_PORT = "SPM Pre-Production DB Port";
	public static final String CONFIG_SPM_PREPROD_DB_NAME = "SPM Pre-Production DB Name";
	public static final String CONFIG_SPM_PREPROD_DB_USER = "SPM Pre-Production DB User";
	public static final String CONFIG_SPM_PREPROD_DB_PASSWORD = "SPM Pre-Production DB Password";
	
	public static final String CONFIG_SPM_PROD_DB_HOST = "SPM Production DB Host";
	public static final String CONFIG_SPM_PROD_DB_PORT = "SPM Production DB Port";
	public static final String CONFIG_SPM_PROD_DB_NAME = "SPM Production DB Name";
	public static final String CONFIG_SPM_PROD_DB_USER = "SPM Production DB User";
	public static final String CONFIG_SPM_PROD_DB_PASSWORD = "SPM Production DB Password";

	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(RESOURCE_PACKAGE);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.Registry.Widgets.add(new AWAJobStatusWidget());
		CFW.Registry.Widgets.add(new AWAJobStatusLegendWidget());
		CFW.Registry.Widgets.add(new SPMMonitorStatusWidget());
		CFW.Registry.Widgets.add(new SPMLegendWidget());
							
	}

	@Override
	public void initializeDB() {
		
		AWAJobStatusDatabase.initialize();
		SPMDatabase.initialize();
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
			new Configuration("AWA Job Status", CONFIG_AWA_PROD_DBHOST)
				.description("The host of the productive AWA Database instance.")
				.type(FormFieldType.TEXT)
		);
		
		CFW.DB.Config.oneTimeCreate(
			new Configuration("AWA Job Status", CONFIG_AWA_PROD_DBPORT)
				.description("The port of the productive AWA Database instance.")
				.type(FormFieldType.NUMBER)
		);
		
		CFW.DB.Config.oneTimeCreate(
			new Configuration("AWA Job Status", CONFIG_AWA_PROD_DBNAME)
				.description("The Service Name or SID of the productive database used to fetch the job status from.")
				.type(FormFieldType.TEXT)
		);
		
		CFW.DB.Config.oneTimeCreate(
				new Configuration("AWA Job Status", CONFIG_AWA_PROD_DBTYPE)
					.description("The type of the oracle service consumed.")
					.type(FormFieldType.SELECT)
					.options(new String[] {"Service Name", "SID"})
					.value("SID")
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
			new Configuration("AWA Job Status", CONFIG_AWA_PREPROD_DBHOST)
				.description("The host of the pre-productive AWA Database instance.")
				.type(FormFieldType.TEXT)
		);
	
		CFW.DB.Config.oneTimeCreate(
			new Configuration("AWA Job Status", CONFIG_AWA_PREPROD_DBPORT)
				.description("The port of the pre-productive AWA Database instance.")
				.type(FormFieldType.NUMBER)
		);
		
		CFW.DB.Config.oneTimeCreate(
				new Configuration("AWA Job Status", CONFIG_AWA_PREPROD_DBNAME)
					.description("The Service Name or SID of pre-productive database used to fetch the job status from.")
					.type(FormFieldType.TEXT)
			);
		
		CFW.DB.Config.oneTimeCreate(
				new Configuration("AWA Job Status", CONFIG_AWA_PREPROD_DBTYPE)
					.description("The type of the oracle service consumed.")
					.type(FormFieldType.SELECT)
					.options(new String[] {"Service Name", "SID"})
					.value("SID")
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
		

		
		//-----------------------------------------
		// 
		//-----------------------------------------
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Silk Performance Manager", CONFIG_SPM_PROD_DB_HOST)
				.description("The fully qualified hostname of the SPM database.")
				.type(FormFieldType.TEXT)
		);
		
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Silk Performance Manager", CONFIG_SPM_PROD_DB_PORT)
				.description("The port of the SPM database.")
				.type(FormFieldType.TEXT)
		);
		
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Silk Performance Manager", CONFIG_SPM_PROD_DB_NAME)
				.description("The name of the SPM database.")
				.type(FormFieldType.TEXT)
		);

		CFW.DB.Config.oneTimeCreate(
			new Configuration("Silk Performance Manager", CONFIG_SPM_PROD_DB_USER)
				.description("The username of the user that will access the DB.")
				.type(FormFieldType.TEXT)
		);

		CFW.DB.Config.oneTimeCreate(
			new Configuration("Silk Performance Manager", CONFIG_SPM_PROD_DB_PASSWORD)
				.description("The password of the DB User.")
				.type(FormFieldType.PASSWORD)
		);
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Silk Performance Manager", CONFIG_SPM_PREPROD_DB_HOST)
				.description("The fully qualified hostname of the SPM database.")
				.type(FormFieldType.TEXT)
		);
		
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Silk Performance Manager", CONFIG_SPM_PREPROD_DB_PORT)
				.description("The port of the SPM database.")
				.type(FormFieldType.TEXT)
		);
	
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Silk Performance Manager", CONFIG_SPM_PREPROD_DB_NAME)
				.description("The name of the SPM database.")
				.type(FormFieldType.TEXT)
		);
		
		CFW.DB.Config.oneTimeCreate(
			new Configuration("Silk Performance Manager", CONFIG_SPM_PREPROD_DB_USER)
				.description("The username of the user that will access the DB.")
				.type(FormFieldType.TEXT)
		);

		CFW.DB.Config.oneTimeCreate(
			new Configuration("Silk Performance Manager", CONFIG_SPM_PREPROD_DB_PASSWORD)
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
