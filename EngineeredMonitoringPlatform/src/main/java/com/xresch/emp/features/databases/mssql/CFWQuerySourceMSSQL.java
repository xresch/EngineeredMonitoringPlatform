package com.xresch.emp.features.databases.mssql;

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
public class CFWQuerySourceMSSQL extends CFWQuerySourceDatabase {


	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQuerySourceMSSQL(CFWQuery parent) {
		super(parent, MSSQLEnvironment.SETTINGS_TYPE);
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public DBInterface getDatabaseInterface(int environmentID) {
		MSSQLEnvironment environment =
				MSSQLEnvironmentManagement.getEnvironment(environmentID);

		return environment.getDBInstance();
	
	}


	/******************************************************************
	 *
	 ******************************************************************/
	public String uniqueName() {
		return "mssql";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionShort() {
		return "Fetches data from a MSSQL database.";
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
		return FeatureMSSQL.PERMISSION_MSSQL;
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureMSSQL.PERMISSION_MSSQL);
	}
	
}
