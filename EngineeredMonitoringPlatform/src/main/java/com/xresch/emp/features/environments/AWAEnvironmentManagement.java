package com.xresch.emp.features.environments;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.contextsettings.ContextSettingsChangeListener;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;


public class AWAEnvironmentManagement {
	private static Logger logger = CFWLog.getLogger(AWAEnvironmentManagement.class.getName());
	
	private static boolean isInitialized = false;
	
	// Contains ContextSettings id and the associated database interface
	private static HashMap<Integer, AWAEnvironment> environmentsWithDB = new HashMap<Integer, AWAEnvironment>();
	

	public static void initialize() {
		
		ContextSettingsChangeListener listener = 
				new ContextSettingsChangeListener(AWAEnvironment.SETTINGS_TYPE) {
			
			@Override
			public void onChange(AbstractContextSettings setting, boolean isNew) {
				AWAEnvironment env = (AWAEnvironment)setting;
				AWAEnvironmentManagement.createEnvironment(env);
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
		environmentsWithDB = new HashMap<Integer, AWAEnvironment>();
		
		ArrayList<AbstractContextSettings> settingsArray = CFW.DB.ContextSettings.getContextSettingsForType(AWAEnvironment.SETTINGS_TYPE);

		for(AbstractContextSettings settings : settingsArray) {
			AWAEnvironment current = (AWAEnvironment)settings;
			createEnvironment(current);
			
		}
	}
	
	private static void createEnvironment(AWAEnvironment environment) {

		environmentsWithDB.remove(environment.getDefaultObject().id());
		
		if(environment.isDBDefined()) {
			DBInterface db = initializeDBInterface(
					environment.dbHost(), 
					environment.dbPort(), 
					environment.dbName(), 
					environment.dbType(), 
					environment.dbUser(), 
					environment.dbPassword()
			);
			
			environment.setDBInstance(db);
			environmentsWithDB.put(environment.getDefaultObject().id(), environment);
		}
	}
	
	
	public static AWAEnvironment getEnvironment(int id) {
		if(!isInitialized) { initialize(); }
		return environmentsWithDB.get(id);
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

	public static AutocompleteResult autocompleteClient(HttpServletRequest request){
		
		AutocompleteList list = new AutocompleteList();
		
		AWAEnvironment environment = new AWAEnvironment();
		environment.mapRequestParameters(request);
		
		if(environment.isDBDefined()) {
			
			DBInterface db = initializeDBInterface(
					environment.dbHost(), 
					environment.dbPort(), 
					environment.dbName(), 
					environment.dbType(), 
					environment.dbUser(), 
					environment.dbPassword()
			);
			ResultSet result  = db.preparedExecuteQuery(
					  "SELECT COUNT(AH_CLIENT) AS COUNT, AH_CLIENT " 
					+ "FROM UC4.AH "		  
					+ "GROUP BY AH_CLIENT "
					+ "ORDER BY COUNT DESC");
			
			try {
				
				while(result != null && result.next()) {
					Object client = (Object) result.getString("AH_CLIENT");
					Integer count = result.getInt("COUNT");
					list.addItem(client, client, count+" Entries");
				}
			} catch (SQLException e) {
				new CFWLog(logger)
				.method("autocompleteClient")
				.severe("Error reading SQL results.", e);
				
			}
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.INFO, "Please specify database first to get autocomplete values.");
		}
		
		return new AutocompleteResult(list);
	}
}