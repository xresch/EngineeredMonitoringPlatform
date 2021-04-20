package com.xresch.emp.features.common;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.apache.commons.dbcp2.BasicDataSource;

import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.emp.features.mysql.MySQLEnvironmentManagement;

import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

public class EMPDBInterfaceFactory {
	
	private static Logger logger = CFWLog.getLogger(MySQLEnvironmentManagement.class.getName());
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static DBInterface createOracleInterface(String servername, int port, String name, String type, String username, String password) {
		
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
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static DBInterface createMSSQLInterface(String servername, int port, String dbName, String username, String password) {
		
		DBInterface db = new DBInterface() {
			
			//SQLServerConnectionPoolDataSource pooledSource;
			BasicDataSource pooledSource;
			{
				try {
					//Driver name com.microsoft.sqlserver.jdbc.SQLServerDriver
					//Connection URL Example: "jdbc:sqlserver://localhost:1433;databaseName=AdventureWorks;user=MyUserName;password=*****;";  
					pooledSource = new BasicDataSource();
					
					pooledSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
					pooledSource.setUrl("jdbc:sqlserver://"+servername+":"+port+";databaseName="+dbName);					

					pooledSource.setUsername(username);
					pooledSource.setPassword(password);
					
					pooledSource.setMaxIdle(90);
					pooledSource.setMinIdle(10);
					pooledSource.setMaxOpenPreparedStatements(100);
					
					//----------------------------------
					// Test connection
					//pooledSource.setLoginTimeout(5);
					Connection connection = pooledSource.getConnection();
					connection.close();
					
				} catch (Exception e) {
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
	/************************************************************************
	 * 
	 ************************************************************************/
	public static DBInterface createMySQLInterface(String servername, int port, String dbName, String username, String password) {
		
		DBInterface db = new DBInterface() {
			
			//SQLServerConnectionPoolDataSource pooledSource;
			BasicDataSource pooledSource;
			{
				try {
					//Driver name: com.mysql.cj.jdbc.Driver
					//Connection URL Example: "jdbc:mysql://localhost:3306/databasename"
					//Connection URL Example: "jdbc:sqlserver://localhost:1433;databaseName=AdventureWorks;user=MyUserName;password=*****;";  
					pooledSource = new BasicDataSource();
					
					pooledSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
					pooledSource.setUrl("jdbc:mysql://"+servername+":"+port+"/"+dbName);					

					pooledSource.setUsername(username);
					pooledSource.setPassword(password);
					
					pooledSource.setMaxIdle(90);
					pooledSource.setMinIdle(10);
					pooledSource.setMaxOpenPreparedStatements(100);
					
					//----------------------------------
					// Test connection
					//pooledSource.setLoginTimeout(5);
					Connection connection = pooledSource.getConnection();
					connection.close();
					
				} catch (Exception e) {
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
