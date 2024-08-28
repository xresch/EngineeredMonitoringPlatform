package com.xresch.emp.features.mongodb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.contextsettings.ContextSettingsChangeListener;
import com.xresch.cfw.logging.CFWLog;

public class MongoDBEnvironmentManagement {

	private static boolean isInitialized = false;
	
	// Contains ContextSettings id and the associated database interface
	private static HashMap<Integer, MongoDBEnvironment> environmentsWithDB = new HashMap<Integer, MongoDBEnvironment>();
	

	private MongoDBEnvironmentManagement() {
		//hide public constructor
	}
	public static void initialize() {
		
		ContextSettingsChangeListener listener = 
				new ContextSettingsChangeListener(MongoDBEnvironment.SETTINGS_TYPE) {
			
			@Override
			public void onChange(AbstractContextSettings setting, boolean isNew) {
				MongoDBEnvironment env = (MongoDBEnvironment)setting;
				MongoDBEnvironmentManagement.createEnvironment(env);
			}

			@Override
			public void onDeleteOrDeactivate(AbstractContextSettings typeSettings) {
				environmentsWithDB.remove(typeSettings.getDefaultObject().id());
			}
		};
		
		CFW.DB.ContextSettings.addChangeListener(listener);
		
		createEnvironments();
		isInitialized = true;
	}
	
	private static void createEnvironments() {
		// Clear environments
		environmentsWithDB = new HashMap<Integer, MongoDBEnvironment>();
		
		ArrayList<AbstractContextSettings> settingsArray = CFW.DB.ContextSettings.getContextSettingsForType(MongoDBEnvironment.SETTINGS_TYPE, true);

		for(AbstractContextSettings settings : settingsArray) {
			MongoDBEnvironment current = (MongoDBEnvironment)settings;
			createEnvironment(current);
			
		}
	}
	
	private static void createEnvironment(MongoDBEnvironment environment) {
		
		Integer id = environment.getDefaultObject().id();
		
		environmentsWithDB.remove(id);
		
		if(environment.isDBDefined()) {
			
			//---------------------------------------------------
			// Create URI
			String connectionString = "mongodb://";
			
			if( !Strings.isNullOrEmpty(environment.dbUser()) ) {
				connectionString += environment.dbUser();
				
				if( !Strings.isNullOrEmpty(environment.dbPassword()) ) {
					connectionString += ":"+environment.dbPassword();
				}
				connectionString += "@";
			}
			
			connectionString += environment.dbHost()+":"+environment.dbPort();
			
			//---------------------------------------------------
			// Create Connection
			MongoClient mongoClient = new MongoClient(new MongoClientURI(connectionString));
			MongoDatabase mongoDB = mongoClient.getDatabase(environment.dbName());
			environment.setMongoDB(mongoDB);
			environmentsWithDB.put(id, environment);
			
		}else {
			CFW.Messages.addInfoMessage("Configuration incomplete, at least host, port and database name is needed to create a connection.");
		}
	}
	
	
	public static MongoDBEnvironment getEnvironment(int id) {
		if(!isInitialized) { initialize(); }
		return environmentsWithDB.get(id);
	}
	
}
