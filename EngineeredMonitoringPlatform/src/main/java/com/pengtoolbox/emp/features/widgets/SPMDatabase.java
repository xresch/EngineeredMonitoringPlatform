package com.pengtoolbox.emp.features.widgets;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.microsoft.sqlserver.jdbc.SQLServerConnectionPoolDataSource;
import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.db.DBInterface;
import com.pengtoolbox.cfw.features.config.ConfigChangeListener;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.emp.features.theusinator.FeatureTheusinator;

import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;


public class SPMDatabase {
	
	private static Logger logger = CFWLog.getLogger(SPMDatabase.class.getName());
	
	private static boolean isInitialized = false;
	private static DBInterface DB_PREPROD;
	private static DBInterface DB_PROD;
	
	public static void initialize() {
		
		ConfigChangeListener listener = new ConfigChangeListener(
				FeatureEMPWidgets.CONFIG_SPM_PROD_DB_HOST,
				FeatureEMPWidgets.CONFIG_SPM_PROD_DB_PORT,
				FeatureEMPWidgets.CONFIG_SPM_PROD_DB_NAME,
				FeatureEMPWidgets.CONFIG_SPM_PROD_DB_USER,
				FeatureEMPWidgets.CONFIG_SPM_PROD_DB_PASSWORD,
				FeatureEMPWidgets.CONFIG_SPM_PREPROD_DB_HOST,
				FeatureEMPWidgets.CONFIG_SPM_PREPROD_DB_PORT,
				FeatureEMPWidgets.CONFIG_SPM_PREPROD_DB_NAME,
				FeatureEMPWidgets.CONFIG_SPM_PREPROD_DB_USER,
				FeatureEMPWidgets.CONFIG_SPM_PREPROD_DB_PASSWORD
			) {
			
			@Override
			public void onChange() {
				SPMDatabase.createEnvironments();
			}
		};
		
		CFW.DB.Config.addChangeListener(listener);
		
		createEnvironments();
		isInitialized = true;
	}
	
	private static void createEnvironments() {
		
		DB_PROD = null;
		DB_PREPROD = null;
		
		if(!Strings.isNullOrEmpty(CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_AWA_PREPROD_DBHOST)) ) {
			DB_PREPROD = initializeDBInterface(
					CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_SPM_PREPROD_DB_HOST), 
					CFW.DB.Config.getConfigAsInt(   FeatureEMPWidgets.CONFIG_SPM_PREPROD_DB_PORT), 
					CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_SPM_PREPROD_DB_NAME), 
					CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_SPM_PREPROD_DB_USER), 
					CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_SPM_PREPROD_DB_PASSWORD)
				);
		}	
		
		if(!Strings.isNullOrEmpty(CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_SPM_PROD_DB_HOST)) ) {
			DB_PROD = initializeDBInterface(
					CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_SPM_PROD_DB_HOST), 
					CFW.DB.Config.getConfigAsInt(   FeatureEMPWidgets.CONFIG_SPM_PROD_DB_PORT), 
					CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_SPM_PROD_DB_NAME), 
					CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_SPM_PROD_DB_USER), 
					CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_SPM_PROD_DB_PASSWORD)
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
	
	public static DBInterface initializeDBInterface(String servername, int port, String name, String username, String password) {
		
		@SuppressWarnings("deprecation")
		DBInterface db = new DBInterface() {
			
			SQLServerConnectionPoolDataSource pooledSource;
			{
				try {

					pooledSource = new SQLServerConnectionPoolDataSource();
					pooledSource.setServerName(servername);
					pooledSource.setPortNumber(port);
					
					pooledSource.setDatabaseName(name);
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
						
						return  pooledSource.getConnection();
					}
				}				
			}
		};

		return db;
	}

}
