package com.pengtoolbox.emp.features.widgets;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.PooledConnection;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.db.DBInterface;
import com.pengtoolbox.cfw.features.config.ConfigChangeListener;
import com.pengtoolbox.cfw.logging.CFWLog;

import oracle.jdbc.pool.OracleConnectionPoolDataSource;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;


public class AWAJobStatusDatabase {
	private static Logger logger = CFWLog.getLogger(AWAJobStatusDatabase.class.getName());
	
	private static boolean isInitialized = false;
	private static DBInterface DB_PREPROD;
	private static DBInterface DB_PROD;
	
	public static void initialize() {
		
		ConfigChangeListener listener = new ConfigChangeListener(
				FeatureEMPWidgets.CONFIG_AWA_PREPROD_DBHOST,
				FeatureEMPWidgets.CONFIG_AWA_PREPROD_DBPORT, 
				FeatureEMPWidgets.CONFIG_AWA_PREPROD_DBNAME, 
				FeatureEMPWidgets.CONFIG_AWA_PREPROD_DBTYPE,
				FeatureEMPWidgets.CONFIG_AWA_PREPROD_DBUSER, 
				FeatureEMPWidgets.CONFIG_AWA_PREPROD_PASSWORD,
				FeatureEMPWidgets.CONFIG_AWA_PROD_DBHOST, 
				FeatureEMPWidgets.CONFIG_AWA_PROD_DBPORT, 
				FeatureEMPWidgets.CONFIG_AWA_PROD_DBNAME, 
				FeatureEMPWidgets.CONFIG_AWA_PROD_DBTYPE,
				FeatureEMPWidgets.CONFIG_AWA_PROD_DBUSER, 
				FeatureEMPWidgets.CONFIG_AWA_PROD_PASSWORD
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
		
		DB_PROD = null;
		DB_PREPROD = null;
		
		if(CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_AWA_PREPROD_DBHOST) != null
		&&	!CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_AWA_PREPROD_DBHOST).isEmpty()) {
			DB_PREPROD = initializeDBInterface(
					CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_AWA_PREPROD_DBHOST), 
					CFW.DB.Config.getConfigAsInt(   FeatureEMPWidgets.CONFIG_AWA_PREPROD_DBPORT), 
					CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_AWA_PREPROD_DBNAME), 
					CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_AWA_PREPROD_DBTYPE),
					CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_AWA_PREPROD_DBUSER), 
					CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_AWA_PREPROD_PASSWORD)
				);
		}	
		
		if(CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_AWA_PROD_DBHOST) != null
		&& !CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_AWA_PROD_DBHOST).isEmpty()) {
			DB_PROD = initializeDBInterface(
					CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_AWA_PROD_DBHOST), 
					CFW.DB.Config.getConfigAsInt(   FeatureEMPWidgets.CONFIG_AWA_PROD_DBPORT), 
					CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_AWA_PROD_DBNAME), 
					CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_AWA_PROD_DBTYPE),
					CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_AWA_PROD_DBUSER), 
					CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_AWA_PROD_PASSWORD)
				);
		}
	}
	
	
	public static DBInterface getProd() {
		if(!isInitialized) { initialize(); }
		return DB_PROD;
	}
	
	public static DBInterface getPreProd() {
		if(!isInitialized) { initialize(); }
		return DB_PREPROD;
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
						
						return  pooledSource.getConnection();
					}
				}				
			}
		};

		return db;
	}

}
