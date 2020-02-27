package com.pengtoolbox.emp.features.widgets;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.sql.PooledConnection;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.db.CFWDB;
import com.pengtoolbox.cfw.db.DBInterface;
import com.pengtoolbox.cfw.logging.CFWLog;

import oracle.jdbc.pool.OracleConnectionPoolDataSource;

public class AWAJobStatusDatabase {
	private static Logger logger = CFWLog.getLogger(AWAJobStatusDatabase.class.getName());
	
	private static DBInterface DB_PREPROD;
	private static DBInterface DB_PROD;
	
	private static void initialize() {
		
		DB_PREPROD = initializeDBInterface(
				CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_AWA_PREPROD_DBHOST), 
				CFW.DB.Config.getConfigAsInt(   FeatureEMPWidgets.CONFIG_AWA_PREPROD_DBPORT), 
				CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_AWA_PREPROD_DBNAME), 
				CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_AWA_PREPROD_DBUSER), 
				CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_AWA_PREPROD_PASSWORD)
			);
				
		DB_PROD = initializeDBInterface(
				CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_AWA_PROD_DBHOST), 
				CFW.DB.Config.getConfigAsInt(   FeatureEMPWidgets.CONFIG_AWA_PROD_DBPORT), 
				CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_AWA_PROD_DBNAME), 
				CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_AWA_PROD_DBUSER), 
				CFW.DB.Config.getConfigAsString(FeatureEMPWidgets.CONFIG_AWA_PROD_PASSWORD)
			);
	}
	
	public static DBInterface getProd() {
		return DB_PROD;
	}
	
	public static DBInterface getPreProd() {
		return DB_PREPROD;
	}
	
	public static DBInterface initializeDBInterface(String servername, int port, String dbname,  String username, String password) {
		
		DBInterface db = new DBInterface() {
			
			OracleConnectionPoolDataSource pooledSource;
			
			{
				try {
					pooledSource = new OracleConnectionPoolDataSource();
					pooledSource.setDriverType("oci");
					pooledSource.setServerName(servername);
					pooledSource.setNetworkProtocol("tcp");
					pooledSource.setDatabaseName(dbname);
					pooledSource.setPortNumber(port);
					pooledSource.setUser(username); 
					pooledSource.setPassword(password);
					
				} catch (SQLException e) {
					new CFWLog(logger)
						.method("initialize")
						.severe("Exception initializing Database.", e);
				}
			}
			
			@Override
			public Connection getConnection() throws SQLException {
				PooledConnection pc = pooledSource.getPooledConnection();
				return  pc.getConnection();
			}
		};

		return db;
	}

}
