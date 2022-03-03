package com.xresch.emp.features.databases.generic;

import java.rmi.AccessException;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.query.CFWQuery;
import com.xresch.cfw.features.query.CFWQueryAutocompleteHelper;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;
import com.xresch.emp.features.databases.CFWQuerySourceDatabase;
import com.xresch.emp.features.databases.FeatureDatabases;
import com.xresch.emp.features.databases.mssql.MSSQLEnvironment;
import com.xresch.emp.features.databases.mssql.MSSQLEnvironmentManagement;
	
/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021 
 * @license MIT-License
 **************************************************************************************************************/
public class CFWQuerySourceGenericJDBC extends CFWQuerySourceDatabase {

	/******************************************************************
	 *
	 ******************************************************************/
	public CFWQuerySourceGenericJDBC(CFWQuery parent) {
		super(parent, GenericJDBCEnvironment.SETTINGS_TYPE);
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public DBInterface getDatabaseInterface(int environmentID) {
		GenericJDBCEnvironment environment =
				GenericJDBCEnvironmentManagement.getEnvironment(environmentID);

		if(environment == null) { return null; }
		
		return environment.getDBInstance();
	
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String getTimezone(int environmentID) {
		GenericJDBCEnvironment environment =
				GenericJDBCEnvironmentManagement.getEnvironment(environmentID);
		
		if(environment == null) { return null; }
		
		return environment.timezone();
	}

	/******************************************************************
	 *
	 ******************************************************************/
	public String uniqueName() {
		return "jdbc";
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionShort() {
		return "Fetches data from a JDBC database.";
	}
	
	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public String descriptionRequiredPermission() {
		return FeatureGenericJDBC.PERMISSION_GENERICJDBC;
	}

	/******************************************************************
	 *
	 ******************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureGenericJDBC.PERMISSION_GENERICJDBC);
	}
	
}
