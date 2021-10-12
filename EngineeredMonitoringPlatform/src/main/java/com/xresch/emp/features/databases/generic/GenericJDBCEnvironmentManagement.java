package com.xresch.emp.features.databases.generic;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.apache.commons.dbcp2.BasicDataSource;

import com.microsoft.sqlserver.jdbc.SQLServerConnectionPoolDataSource;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.contextsettings.ContextSettingsChangeListener;
import com.xresch.cfw.logging.CFWLog;

public class GenericJDBCEnvironmentManagement {
	private static Logger logger = CFWLog.getLogger(GenericJDBCEnvironmentManagement.class.getName());
	
	private static boolean isInitialized = false;
	
	// Contains ContextSettings id and the associated database interface
	private static HashMap<Integer, GenericJDBCEnvironment> environmentsWithDB = new HashMap<Integer, GenericJDBCEnvironment>();
	

	private GenericJDBCEnvironmentManagement() {
		//hide public constructor
	}
	public static void initialize() {
		
		ContextSettingsChangeListener listener = 
				new ContextSettingsChangeListener(GenericJDBCEnvironment.SETTINGS_TYPE) {
			
			@Override
			public void onChange(AbstractContextSettings setting, boolean isNew) {
				GenericJDBCEnvironment env = (GenericJDBCEnvironment)setting;
				GenericJDBCEnvironmentManagement.createEnvironment(env);
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
		environmentsWithDB = new HashMap<Integer, GenericJDBCEnvironment>();
		
		ArrayList<AbstractContextSettings> settingsArray = CFW.DB.ContextSettings.getContextSettingsForType(GenericJDBCEnvironment.SETTINGS_TYPE);

		for(AbstractContextSettings settings : settingsArray) {
			GenericJDBCEnvironment current = (GenericJDBCEnvironment)settings;
			createEnvironment(current);
			
		}
	}
	
	private static void createEnvironment(GenericJDBCEnvironment environment) {

		Integer id = environment.getDefaultObject().id();
		environmentsWithDB.remove(id);
		
		if(environment.isDBDefined()) {
			

			DBInterface db = DBInterface.createDBInterface(
					id+"-"+environment.getDefaultObject().name()+":GenericJBDC",
					environment.dbDriver(), 
					environment.dbConnectionURL(), 
					environment.dbUser(), 
					environment.dbPassword()
			);
			
			environment.setDBInstance(db);
			environmentsWithDB.put(environment.getDefaultObject().id(), environment);
		}
	}
	
	
	public static GenericJDBCEnvironment getEnvironment(int id) {
		if(!isInitialized) { initialize(); }
		return environmentsWithDB.get(id);
	}
	
	
}
