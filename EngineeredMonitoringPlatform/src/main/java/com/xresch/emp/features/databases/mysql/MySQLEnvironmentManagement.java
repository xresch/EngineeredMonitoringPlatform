package com.xresch.emp.features.databases.mysql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.contextsettings.ContextSettingsChangeListener;
import com.xresch.cfw.logging.CFWLog;

public class MySQLEnvironmentManagement {
	private static Logger logger = CFWLog.getLogger(MySQLEnvironmentManagement.class.getName());
	
	private static boolean isInitialized = false;
	
	// Contains ContextSettings id and the associated database interface
	private static HashMap<Integer, MySQLEnvironment> environmentsWithDB = new HashMap<Integer, MySQLEnvironment>();
	

	private MySQLEnvironmentManagement() {
		//hide public constructor
	}
	public static void initialize() {
		
		ContextSettingsChangeListener listener = 
				new ContextSettingsChangeListener(MySQLEnvironment.SETTINGS_TYPE) {
			
			@Override
			public void onChange(AbstractContextSettings setting, boolean isNew) {
				MySQLEnvironment env = (MySQLEnvironment)setting;
				MySQLEnvironmentManagement.createEnvironment(env);
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
		environmentsWithDB = new HashMap<Integer, MySQLEnvironment>();
		
		ArrayList<AbstractContextSettings> settingsArray = CFW.DB.ContextSettings.getContextSettingsForType(MySQLEnvironment.SETTINGS_TYPE);

		for(AbstractContextSettings settings : settingsArray) {
			MySQLEnvironment current = (MySQLEnvironment)settings;
			createEnvironment(current);
			
		}
	}
	
	private static void createEnvironment(MySQLEnvironment environment) {
		
		Integer id = environment.getDefaultObject().id();
		
		environmentsWithDB.remove(id);
		
		if(environment.isDBDefined()) {
			DBInterface db = DBInterface.createDBInterfaceMySQL(
					id+"-"+environment.getDefaultObject().name(),
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
	
	
	public static MySQLEnvironment getEnvironment(int id) {
		if(!isInitialized) { initialize(); }
		return environmentsWithDB.get(id);
	}
	
//	/************************************************************************
//	 * 
//	 ************************************************************************/
//	public static DBInterface initializeDBInterface(String servername, int port, String dbName, String username, String password) {
//		
//		DBInterface db = new DBInterface() {
//			
//			//SQLServerConnectionPoolDataSource pooledSource;
//			BasicDataSource pooledSource;
//			{
//				try {
//					//Driver name: com.mysql.jdbc.Driver
//					//Connection URL Example: "jdbc:mysql://localhost:3306/databasename"
//					//Connection URL Example: "jdbc:sqlserver://localhost:1433;databaseName=AdventureWorks;user=MyUserName;password=*****;";  
//					pooledSource = new BasicDataSource();
//					
//					pooledSource.setDriverClassName("com.mysql.jdbc.Driver");
//					pooledSource.setUrl("jdbc:mysql://"+servername+":"+port+"/"+dbName);					
//
//					pooledSource.setUsername(username);
//					pooledSource.setPassword(password);
//					
//					pooledSource.setMaxIdle(90);
//					pooledSource.setMinIdle(10);
//					pooledSource.setMaxOpenPreparedStatements(100);
//					
//					//----------------------------------
//					// Test connection
//					pooledSource.setLoginTimeout(5);
//					Connection connection = pooledSource.getConnection();
//					connection.close();
//					
//				} catch (Exception e) {
//					new CFWLog(logger)
//						.severe("Exception initializing Database.", e);
//				}
//			}
//			
//			@Override
//			public Connection getConnection() throws SQLException {
//				
//				if(transactionConnection.get() != null) {
//					return transactionConnection.get();
//				}else {
//					synchronized (pooledSource) {
//						Connection connection = pooledSource.getConnection();
//						addOpenConnection(connection);
//						return connection;
//					}
//				}				
//			}
//		};
//
//		return db;
//	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
//	public static DBInterface initializeDBInterface(String servername, int port, String dbName, String username, String password) {
//		
//		DBInterface db = new DBInterface() {
//			
//
//			MysqlConnectionPoolDataSource pooledSource;
//			{
//				try {
//
//					pooledSource = new MysqlConnectionPoolDataSource();
//					pooledSource.setServerName(servername);
//					pooledSource.setPortNumber(port);
//					
//					pooledSource.setDatabaseName(dbName);
//					pooledSource.setUser(username);
//					pooledSource.setPassword(password);
//					
//					//----------------------------------
//					// Test connection
//					pooledSource.setLoginTimeout(5);
//					Connection connection = pooledSource.getConnection();
//					connection.close();
//					
//				} catch (Exception e) {
//					new CFWLog(logger)
//						.severe("Exception initializing Database.", e);
//				}
//			}
//			
//			@Override
//			public Connection getConnection() throws SQLException {
//				
//				if(transactionConnection.get() != null) {
//					return transactionConnection.get();
//				}else {
//					synchronized (pooledSource) {
//						Connection connection = pooledSource.getConnection();
//						addOpenConnection(connection);
//						return connection;
//					}
//				}				
//			}
//		};
//
//		return db;
//	}
	
}
