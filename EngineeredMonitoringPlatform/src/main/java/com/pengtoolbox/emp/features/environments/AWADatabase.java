package com.pengtoolbox.emp.features.environments;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.db.DBInterface;
import com.pengtoolbox.cfw.features.contextsettings.AbstractContextSettings;
import com.pengtoolbox.cfw.features.contextsettings.ContextSettingsChangeListener;
import com.pengtoolbox.cfw.logging.CFWLog;

import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;


public class AWADatabase {
	private static Logger logger = CFWLog.getLogger(AWADatabase.class.getName());
	
	private static boolean isInitialized = false;
	
	// Contains ContextSettings id and the associated database interface	
	private static HashMap<Integer, DBInterface> dbInterfaces = new HashMap<Integer, DBInterface>(); 
	
	public static void initialize() {
		
		ContextSettingsChangeListener listener = 
				new ContextSettingsChangeListener(EnvironmentAWA.SETTINGS_TYPE) {
			
			@Override
			public void onChange(AbstractContextSettings setting, boolean isNew) {
				EnvironmentAWA env = (EnvironmentAWA)setting;
				AWADatabase.createEnvironment(env);
			}
		};
		
		CFW.DB.ContextSettings.addChangeListener(listener);
		
		createEnvironments();
		isInitialized = true;
	}
	
	private static void createEnvironments() {
		// Clear environments
		dbInterfaces = new HashMap<Integer, DBInterface>();
		
		ArrayList<AbstractContextSettings> settingsArray = CFW.DB.ContextSettings.getContextSettingsForType(EnvironmentAWA.SETTINGS_TYPE);

		for(AbstractContextSettings settings : settingsArray) {
			EnvironmentAWA current = (EnvironmentAWA)settings;
			createEnvironment(current);
			
		}
	}
	
	private static void createEnvironment(EnvironmentAWA environment) {

		dbInterfaces.remove(environment.getWrapper().id());
		
		System.out.println("AWA current.isDBDefined():"+environment.isDBDefined());
		if(environment.isDBDefined()) {
			System.out.println("AWA current.getWrapper().name():"+environment.getWrapper().name());
			DBInterface db = initializeDBInterface(
					environment.dbHost(), 
					environment.dbPort(), 
					environment.dbName(), 
					environment.dbType(), 
					environment.dbUser(), 
					environment.dbPassword()
			);
			
			dbInterfaces.put(environment.getWrapper().id(), db);
		}
	}
	
	
	public static DBInterface getEnvironment(int id) {
		if(!isInitialized) { initialize(); }
		return dbInterfaces.get(id);
	}
	
	public static DBInterface initializeDBInterface(String servername, int port, String name, String type, String username, String password) {
		
		@SuppressWarnings("deprecation")
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
					
					pooledSource.setMaxConnectionReuseCount(50);;
					pooledSource.setTimeoutCheckInterval(30);
					pooledSource.setConnectionWaitTimeout(60);
					pooledSource.setAbandonedConnectionTimeout(20);
					pooledSource.setMaxIdleTime(330);
					pooledSource.setInactiveConnectionTimeout(600);
					pooledSource.setTimeToLiveConnectionTimeout(3600);
					
				} catch (SQLException e) {
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

}
