package com.xresch.emp.features.mongodb;

import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.manual.FeatureManual;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.spi.CFWAppFeature;
import com.xresch.emp.features.common.FeatureEMPCommon;
import com.xresch.emp.features.databases.generic.CFWQuerySourceGenericJDBC;
import com.xresch.emp.features.databases.mssql.CFWJobTaskMSSQLQueryStatus;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureMongoDB extends CFWAppFeature {
	
	public static final String PACKAGE_RESOURCE = "com.xresch.emp.features.mongodb.resources";
	
	public static final String PERMISSION_MONGODB = "Database: MongoDB";

	static final JsonWriterSettings writterSettings = 
	JsonWriterSettings
	.builder()
	.outputMode(JsonMode.RELAXED)
	.build();
	
	/************************************************************************************
	 * Override to make it managed and return something else then null.
	 ************************************************************************************/
	@Override
	public String getNameForFeatureManagement() {
		return "EMP MongoDB";
	};
	
	/************************************************************************************
	 * Register a description for the feature management.
	 ************************************************************************************/
	@Override
	public String getDescriptionForFeatureManagement() {
		return "Use MongoDB database extensions.(Dashboard Widgets, Query Source, Tasks ...)";
	};
	
	/************************************************************************************
	 * Return if the feature is active by default or if the admin has to enable it.
	 ************************************************************************************/
	public boolean activeByDefault() {
		return true;
	};
	
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	@Override
	public void register() {
		//----------------------------------
		// Register Settings
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCE);
		
		//----------------------------------
		// Register Context Settings
		CFW.Registry.ContextSettings.register(MongoDBEnvironment.SETTINGS_TYPE, MongoDBEnvironment.class);
		
		//----------------------------------
		// Register Widgets
		CFW.Registry.Widgets.add(new WidgetMongoDBQueryStatus());
		
		//----------------------------------
		// Register Widget Parameters
		CFW.Registry.Parameters.add(new ParameterDefinitionMongoDBEnvironment());
		
		//----------------------------------
		// Register Query Source
		CFW.Registry.Query.registerSource(new CFWQuerySourceMongoDB(null));
		
		//----------------------------------
		// Register Job Task
		CFW.Registry.Jobs.registerTask(new CFWJobTaskMongoDBQueryStatus());
		
	}

	@Override
	public void initializeDB() {
		//----------------------------------
		// Permissions
		CFW.DB.Permissions.oneTimeCreate(
				new Permission(PERMISSION_MONGODB, FeatureUserManagement.CATEGORY_USER)
					.description("Use the MongoDB Extensions(Widgets, Sources, JobTasks, etc...)."),
				true,
				true);
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
		MongoDBEnvironmentManagement.initialize();
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
