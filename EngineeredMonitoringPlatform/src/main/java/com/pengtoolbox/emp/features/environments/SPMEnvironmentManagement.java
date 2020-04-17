package com.pengtoolbox.emp.features.environments;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.microsoft.sqlserver.jdbc.SQLServerConnectionPoolDataSource;
import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.db.DBInterface;
import com.pengtoolbox.cfw.features.contextsettings.AbstractContextSettings;
import com.pengtoolbox.cfw.features.contextsettings.ContextSettingsChangeListener;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.response.bootstrap.AlertMessage.MessageType;
import com.pengtoolbox.emp.features.widgets.FeatureEMPWidgets;


public class SPMEnvironmentManagement {
	
	private static Logger logger = CFWLog.getLogger(SPMEnvironmentManagement.class.getName());
	
	private static boolean isInitialized = false;

	// Contains ContextSettings id and the associated database interface
	private static HashMap<Integer, SPMEnvironment> environmentsWithDB = new HashMap<Integer, SPMEnvironment>();
	
	public static void initialize() {
	
		ContextSettingsChangeListener listener = 
				new ContextSettingsChangeListener(SPMEnvironment.SETTINGS_TYPE) {
			
			@Override
			public void onChange(AbstractContextSettings setting, boolean isNew) {
				SPMEnvironment env = (SPMEnvironment)setting;
				SPMEnvironmentManagement.createEnvironment(env);
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
		environmentsWithDB = new HashMap<Integer, SPMEnvironment>();
		
		ArrayList<AbstractContextSettings> settingsArray = CFW.DB.ContextSettings.getContextSettingsForType(SPMEnvironment.SETTINGS_TYPE);

		for(AbstractContextSettings settings : settingsArray) {
			SPMEnvironment current = (SPMEnvironment)settings;
			createEnvironment(current);
			
		}
	}
	
	private static void createEnvironment(SPMEnvironment environment) {

		environmentsWithDB.remove(environment.getDefaultObject().id());

		if(environment.isDBDefined()) {
			
			DBInterface db = initializeDBInterface(
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
	
	public static SPMEnvironment getEnvironment(int id) {
		if(!isInitialized) { initialize(); }
		return environmentsWithDB.get(id);
	}
	
	
	public static DBInterface initializeDBInterface(String servername, int port, String dbName, String username, String password) {
		
		@SuppressWarnings("deprecation")
		DBInterface db = new DBInterface() {
			
			SQLServerConnectionPoolDataSource pooledSource;
			{
				try {

					pooledSource = new SQLServerConnectionPoolDataSource();
					pooledSource.setServerName(servername);
					pooledSource.setPortNumber(port);
					
					pooledSource.setDatabaseName(dbName);
					pooledSource.setUser(username);
					pooledSource.setPassword(password);
					pooledSource.setMultiSubnetFailover(true);
					
				} catch (Exception e) {
					new CFWLog(logger)
						.method("initialize")
						.severe("Exception initializing Database.", e);
				}
			}
			
			@Override
			public Connection getConnection() throws SQLException {
				
				if(transactionConnection.get() != null) {
					return transactionConnection.get();
				}else {
					synchronized (pooledSource) {
						Connection connection = pooledSource.getConnection();
						addOpenConnection(connection);
						return connection;
					}
				}				
			}
		};

		return db;
	}
	
	public static LinkedHashMap<Object, Object> autocompleteMonitors(int environmentID, String searchValue, int maxResults) {

		if(searchValue.length() < 3) {
			return null;
		}
		//---------------------------
		// Get DB
		DBInterface db;
		
		db = SPMEnvironmentManagement.getEnvironment(environmentID).getDBInstance();
		
		if(db == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "The chosen environment seems not configured correctly.");
			return null;
		}
		
		ResultSet result = db.preparedExecuteQuery(
			CFW.Files.readPackageResource(FeatureEMPWidgets.RESOURCE_PACKAGE, "emp_spm_monitor_autocomplete.sql"),
			"%"+searchValue+"%",
			"%"+searchValue+"%");
		
		LinkedHashMap<Object, Object> suggestions = new LinkedHashMap<Object, Object>();
		try {
			if(result != null) {
				for(int i = 0;i < maxResults && result.next();i++) {
					int monitorID = result.getInt("MonitorID");
					String monitorName = result.getString("MonitorName");
					String projectName = result.getString("ProjectName");
					suggestions.put(monitorID, projectName +" &gt;&gt; "+ monitorName);	
				}
			}
		
		} catch (SQLException e) {
			new CFWLog(logger)
				.method("fetchData")
				.severe("Error fetching Widget data.", e);
		}finally {
			db.close(result);
		}
		
		return suggestions;
	}
	
	public static LinkedHashMap<Object, Object> autocompleteProjects(int environmentID, String searchValue, int maxResults) {

		if(searchValue.length() < 3) {
			return null;
		}
		//---------------------------
		// Get DB
		DBInterface db;
		
		db = SPMEnvironmentManagement.getEnvironment(environmentID).getDBInstance();
		
		if(db == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "The chosen environment seems not configured correctly.");
			return null;
		}
		
		ResultSet result = db.preparedExecuteQuery(
			CFW.Files.readPackageResource(FeatureEMPWidgets.RESOURCE_PACKAGE, "emp_spm_project_autocomplete.sql"),
			"%"+searchValue+"%");
		
		LinkedHashMap<Object, Object> suggestions = new LinkedHashMap<Object, Object>();
		try {
			if(result != null) {
				for(int i = 0;i < maxResults && result.next();i++) {
					int projectID = result.getInt("ProjectID");
					String projectName = result.getString("ProjectName");
					suggestions.put(projectID, projectName);	
				}
			}
		
		} catch (SQLException e) {
			new CFWLog(logger)
				.method("fetchData")
				.severe("Error fetching Widget data.", e);
		}finally {
			db.close(result);
		}
		
		return suggestions;
	}

}
