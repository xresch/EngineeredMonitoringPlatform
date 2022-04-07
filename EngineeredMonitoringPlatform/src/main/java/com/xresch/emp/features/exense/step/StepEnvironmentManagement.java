package com.xresch.emp.features.exense.step;

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

public class StepEnvironmentManagement {
	private static Logger logger = CFWLog.getLogger(StepEnvironmentManagement.class.getName());
	
	private static boolean isInitialized = false;
	
	// Contains ContextSettings id and the associated database interface
	private static HashMap<Integer, StepEnvironment> environmentsWithDB = new HashMap<Integer, StepEnvironment>();
	

	private StepEnvironmentManagement() {
		//hide public constructor
	}
	public static void initialize() {
		
		ContextSettingsChangeListener listener = 
				new ContextSettingsChangeListener(StepEnvironment.SETTINGS_TYPE) {
			
			@Override
			public void onChange(AbstractContextSettings setting, boolean isNew) {
				StepEnvironment env = (StepEnvironment)setting;
				StepEnvironmentManagement.createEnvironment(env);
			}

			@Override
			public void onDelete(AbstractContextSettings typeSettings) {
				environmentsWithDB.remove(typeSettings.getDefaultObject().id());
			}
		};
		
		CFW.DB.ContextSettings.addChangeListener(listener);
		
		createEnvironments();
		isInitialized = true;
	}
	
	private static void createEnvironments() {
		// Clear environments
		environmentsWithDB = new HashMap<Integer, StepEnvironment>();
		
		ArrayList<AbstractContextSettings> settingsArray = CFW.DB.ContextSettings.getContextSettingsForType(StepEnvironment.SETTINGS_TYPE);

		for(AbstractContextSettings settings : settingsArray) {
			StepEnvironment current = (StepEnvironment)settings;
			createEnvironment(current);
			
		}
	}
	
	private static void createEnvironment(StepEnvironment environment) {
		
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
	
	
	public static StepEnvironment getEnvironment(int id) {
		if(!isInitialized) { initialize(); }
		return environmentsWithDB.get(id);
	}
	
}
