package com.pengtoolbox.emp.features.widgets;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.db.DBInterface;
import com.pengtoolbox.cfw.features.config.ConfigChangeListener;
import com.pengtoolbox.cfw.features.contextsettings.AbstractContextSettings;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.emp.features.environments.EnvironmentAWA;

import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;


public class AWAJobStatusDatabase {
	private static Logger logger = CFWLog.getLogger(AWAJobStatusDatabase.class.getName());
	
	private static boolean isInitialized = false;
	
	// Contains ContextSettings id and the associated database interface	
	private static HashMap<Integer, DBInterface> dbInterfaces = new HashMap<Integer, DBInterface>(); 
	
	public static void initialize() {
		
		ConfigChangeListener listener = new ConfigChangeListener(
				FeatureEMPWidgets.CONFIG_AWA_PROD_DBHOST, 
				FeatureEMPWidgets.CONFIG_AWA_PROD_DBPORT, 
				FeatureEMPWidgets.CONFIG_AWA_PROD_DBNAME, 
				FeatureEMPWidgets.CONFIG_AWA_PROD_DBTYPE,
				FeatureEMPWidgets.CONFIG_AWA_PROD_DBUSER, 
				FeatureEMPWidgets.CONFIG_AWA_PROD_PASSWORD,
				FeatureEMPWidgets.CONFIG_AWA_PREPROD_DBHOST,
				FeatureEMPWidgets.CONFIG_AWA_PREPROD_DBPORT, 
				FeatureEMPWidgets.CONFIG_AWA_PREPROD_DBNAME, 
				FeatureEMPWidgets.CONFIG_AWA_PREPROD_DBTYPE,
				FeatureEMPWidgets.CONFIG_AWA_PREPROD_DBUSER, 
				FeatureEMPWidgets.CONFIG_AWA_PREPROD_PASSWORD,
				FeatureEMPWidgets.CONFIG_AWA_DEV_DBHOST,
				FeatureEMPWidgets.CONFIG_AWA_DEV_DBPORT, 
				FeatureEMPWidgets.CONFIG_AWA_DEV_DBNAME, 
				FeatureEMPWidgets.CONFIG_AWA_DEV_DBTYPE,
				FeatureEMPWidgets.CONFIG_AWA_DEV_DBUSER, 
				FeatureEMPWidgets.CONFIG_AWA_DEV_PASSWORD
				
				) {
			
			@Override
			public void onChange() {
				AWAJobStatusDatabase.createEnvironments();
			}
		};
		
		CFW.DB.Config.addChangeListener(listener);
		
		createEnvironments();
		isInitialized = true;
	}
	
	private static void createEnvironments() {
		// Clear environments
		dbInterfaces = new HashMap<Integer, DBInterface>();
		
		ArrayList<AbstractContextSettings> settingsArray = CFW.DB.ContextSettings.getContextSettingsForType(EnvironmentAWA.SETTINGS_TYPE);
		System.out.println("AWA settingsArray.size(): "+settingsArray.size());
		for(AbstractContextSettings settings : settingsArray) {
			EnvironmentAWA current = (EnvironmentAWA)settings;
			System.out.println("AWA current.isDBDefined():"+current.isDBDefined());
			if(current.isDBDefined()) {
				System.out.println("AWA current.getWrapper().name():"+current.getWrapper().name());
				DBInterface db = initializeDBInterface(
						current.dbHost(), 
						current.dbPort(), 
						current.dbName(), 
						current.dbType(),
						current.dbUser(), 
						current.dbPassword()
				);
				
				dbInterfaces.put(current.getWrapper().id(), db);
			}
			
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
					pooledSource.setMaxPoolSize(30);
					
					
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
