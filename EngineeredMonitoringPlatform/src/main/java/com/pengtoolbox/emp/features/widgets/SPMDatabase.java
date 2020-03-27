package com.pengtoolbox.emp.features.widgets;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.microsoft.sqlserver.jdbc.SQLServerConnectionPoolDataSource;
import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.db.DBInterface;
import com.pengtoolbox.cfw.features.config.ConfigChangeListener;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.response.bootstrap.AlertMessage.MessageType;
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
						Connection connection = pooledSource.getConnection();
						addOpenConnection(connection);
						return connection;
					}
				}				
			}
		};

		return db;
	}
	
	public static LinkedHashMap<Object, Object> autocompleteMonitors(String environment, String searchValue, int maxResults) {

		if(searchValue.length() < 3) {
			return null;
		}
		//---------------------------
		// Get DB
		DBInterface db;
		
		if(environment.equals("Prod")) {
			db = SPMDatabase.getProd();
		}else {
			db = SPMDatabase.getPreProd();
		}
		
		if(db == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "The chosen environment '"+environment+"' is not configured.");
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
	
	public static LinkedHashMap<Object, Object> autocompleteProjects(String environment, String searchValue, int maxResults) {

		if(searchValue.length() < 3) {
			return null;
		}
		//---------------------------
		// Get DB
		DBInterface db;
		
		if(environment.equals("Prod")) {
			db = SPMDatabase.getProd();
		}else {
			db = SPMDatabase.getPreProd();
		}
		
		if(db == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "The chosen environment '"+environment+"' is not configured.");
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
