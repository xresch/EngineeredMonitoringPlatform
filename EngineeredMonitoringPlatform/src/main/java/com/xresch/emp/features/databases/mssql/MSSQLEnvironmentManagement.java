package com.xresch.emp.features.databases.mssql;

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

public class MSSQLEnvironmentManagement {
	private static Logger logger = CFWLog.getLogger(MSSQLEnvironmentManagement.class.getName());
	
	private static boolean isInitialized = false;
	
	// Contains ContextSettings id and the associated database interface
	private static HashMap<Integer, MSSQLEnvironment> environmentsWithDB = new HashMap<Integer, MSSQLEnvironment>();
	

	private MSSQLEnvironmentManagement() {
		//hide public constructor
	}
	public static void initialize() {
		
		ContextSettingsChangeListener listener = 
				new ContextSettingsChangeListener(MSSQLEnvironment.SETTINGS_TYPE) {
			
			@Override
			public void onChange(AbstractContextSettings setting, boolean isNew) {
				MSSQLEnvironment env = (MSSQLEnvironment)setting;
				MSSQLEnvironmentManagement.createEnvironment(env);
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
		environmentsWithDB = new HashMap<Integer, MSSQLEnvironment>();
		
		ArrayList<AbstractContextSettings> settingsArray = CFW.DB.ContextSettings.getContextSettingsForType(MSSQLEnvironment.SETTINGS_TYPE);

		for(AbstractContextSettings settings : settingsArray) {
			MSSQLEnvironment current = (MSSQLEnvironment)settings;
			createEnvironment(current);
			
		}
	}
	
	private static void createEnvironment(MSSQLEnvironment environment) {

		environmentsWithDB.remove(environment.getDefaultObject().id());
		
		if(environment.isDBDefined()) {
			DBInterface db = DBInterface.createDBInterfaceMSSQL(
					"EMP_MSSQL",
					environment.dbHost(), 
					environment.dbPort(), 
					environment.dbName(), 
					environment.dbUser(), 
					environment.dbPassword()
			);
			
			environment.setDBInstance(db);
			environmentsWithDB.put(environment.getDefaultObject().id(), environment);
		}
	}
	
	
	public static MSSQLEnvironment getEnvironment(int id) {
		if(!isInitialized) { initialize(); }
		return environmentsWithDB.get(id);
	}
	
	
}
