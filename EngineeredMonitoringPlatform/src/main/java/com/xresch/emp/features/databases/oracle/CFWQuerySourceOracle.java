package com.xresch.emp.features.databases.oracle;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.emp.features.databases.CFWQuerySourceDatabase;
import com.xresch.emp.features.databases.FeatureDatabases;
	
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQuerySourceOracle extends CFWQuerySourceDatabase {


	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQuerySourceOracle(CFWQuery parent) {
		super(parent, OracleEnvironment.SETTINGS_TYPE);
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public DBInterface getDatabaseInterface(int environmentID) {
		OracleEnvironment environment =
				OracleEnvironmentManagement.getEnvironment(environmentID);

		if(environment == null) { return null; }
		
		return environment.getDBInstance();
	
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String getTimezone(int environmentID) {
		OracleEnvironment environment =
				OracleEnvironmentManagement.getEnvironment(environmentID);
		if(environment == null) { return null; }
		return environment.timezone();
	}

	/******************************************************************
	 *
	 ******************************************************************/
	public String uniqueName() {
		return "oracle";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionShort() {
		return "Fetches data from an oracle database.";
	}
	
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionRequiredPermission() {
		return FeatureOracle.PERMISSION_ORACLE;
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureOracle.PERMISSION_ORACLE);
	}
	
}
