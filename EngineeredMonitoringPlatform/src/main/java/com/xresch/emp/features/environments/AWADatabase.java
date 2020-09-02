package com.xresch.emp.features.environments;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.contextsettings.ContextSettingsChangeListener;
import com.xresch.cfw.logging.CFWLog;

import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;


public class AWADatabase {
	private static Logger logger = CFWLog.getLogger(AWADatabase.class.getName());
	
	private static boolean isInitialized = false;
	
	// Contains ContextSettings id and the associated database interface	
	private static HashMap<Integer, DBInterface> dbInterfaces = new HashMap<Integer, DBInterface>(); 
	
	private AWADatabase() {
		// hide public constructor
	}
	public static void initialize() {
		
		ContextSettingsChangeListener listener = 
				new ContextSettingsChangeListener(AWAEnvironment.SETTINGS_TYPE) {
			
			@Override
			public void onChange(AbstractContextSettings setting, boolean isNew) {
				AWAEnvironment env = (AWAEnvironment)setting;
				AWADatabase.createEnvironment(env);
			}

			@Override
			public void onDelete(AbstractContextSettings typeSettings) {
				dbInterfaces.remove(typeSettings.getDefaultObject().id());
			}
		};
		
		CFW.DB.ContextSettings.addChangeListener(listener);
		
		createEnvironments();
		isInitialized = true;
	}
	
	private static void createEnvironments() {
		// Clear environments
		dbInterfaces = new HashMap<Integer, DBInterface>();
		
		ArrayList<AbstractContextSettings> settingsArray = CFW.DB.ContextSettings.getContextSettingsForType(AWAEnvironment.SETTINGS_TYPE);

		for(AbstractContextSettings settings : settingsArray) {
			AWAEnvironment current = (AWAEnvironment)settings;
			createEnvironment(current);
			
		}
	}
	
	private static void createEnvironment(AWAEnvironment environment) {

		dbInterfaces.remove(environment.getDefaultObject().id());
		
		if(environment.isDBDefined()) {
			DBInterface db = initializeDBInterface(
					environment.dbHost(), 
					environment.dbPort(), 
					environment.dbName(), 
					environment.dbType(), 
					environment.dbUser(), 
					environment.dbPassword()
			);
			
			dbInterfaces.put(environment.getDefaultObject().id(), db);
		}
	}
	
	
	public static DBInterface getEnvironment(int id) {
		if(!isInitialized) { initialize(); }
		return dbInterfaces.get(id);
	}
	
	public static DBInterface initializeDBInterface(String servername, int port, String name, String type, String username, String password) {
		
		DBInterface db = new DBInterface() {
			
			PoolDataSource pooledSource;
			{
				try {
					
					
					String url = "";
					if(type.trim().equals("SID")) {
						//jdbc:oracle:thin:@myHost:myport:sid
						url = "jdbc:oracle:thin:@"+servername+":"+port+":"+name;
					}else {
						//jdbc:oracle:thin:@//myHost:1521/service_name
						url = "jdbc:oracle:thin:@//"+servername+":"+port+"/"+name;
					}
					
					pooledSource = PoolDataSourceFactory.getPoolDataSource();
					pooledSource.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
					pooledSource.setURL(url);
					pooledSource.setUser(username);
					pooledSource.setPassword(password);
					pooledSource.setInitialPoolSize(5);
					pooledSource.setMinPoolSize(5);
					pooledSource.setMaxPoolSize(50);
					pooledSource.setMaxStatements(20);
					
					pooledSource.setMaxConnectionReuseCount(50);
					pooledSource.setTimeoutCheckInterval(30);
					pooledSource.setConnectionWaitTimeout(60);
					pooledSource.setAbandonedConnectionTimeout(20);
					pooledSource.setMaxIdleTime(330);
					pooledSource.setInactiveConnectionTimeout(600);
					pooledSource.setTimeToLiveConnectionTimeout(3600);
					
					//----------------------------------
					// Test connection
					Connection connection = pooledSource.getConnection();
					connection.close();
					
				} catch (SQLException e) {
					new CFWLog(logger)
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

}
