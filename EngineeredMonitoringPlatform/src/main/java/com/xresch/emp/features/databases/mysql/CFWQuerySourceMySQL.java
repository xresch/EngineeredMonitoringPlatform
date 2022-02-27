package com.xresch.emp.features.databases.mysql;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.emp.features.databases.CFWQuerySourceDatabase;
import com.xresch.emp.features.databases.FeatureDatabases;
	
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQuerySourceMySQL extends CFWQuerySourceDatabase {


	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQuerySourceMySQL(CFWQuery parent) {
		super(parent, MySQLEnvironment.SETTINGS_TYPE);
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public DBInterface getDatabaseInterface(int environmentID) {
		MySQLEnvironment environment =
				MySQLEnvironmentManagement.getEnvironment(environmentID);

		return environment.getDBInstance();
	
	}


	/******************************************************************
	 *
	 ******************************************************************/
	public String uniqueName() {
		return "mysql";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionShort() {
		return "Fetches data from a MySQL database.";
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureDatabases.PACKAGE_RESOURCE, "z_manual_source_database.html");
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionRequiredPermission() {
		return FeatureMySQL.PERMISSION_MYSQL;
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureMySQL.PERMISSION_MYSQL);
	}
	
}
