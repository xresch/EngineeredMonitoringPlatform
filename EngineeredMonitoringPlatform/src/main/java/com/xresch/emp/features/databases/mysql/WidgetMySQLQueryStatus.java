package com.xresch.emp.features.databases.mysql;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.emp.features.databases.FeatureDatabases;
import com.xresch.emp.features.databases.WidgetBaseSQLQueryStatus;

public class WidgetMySQLQueryStatus extends WidgetBaseSQLQueryStatus {

	
	private static Logger logger = CFWLog.getLogger(WidgetMySQLQueryStatus.class.getName());
	
	@Override
	public String getWidgetType() {return "emp_mysqlquerystatus";}


	@Override
	public CFWField createEnvironmentSelectorField() {
		return MySQLSettingsFactory.createEnvironmentSelectorField();
	}

	@Override
	public DBInterface getDatabaseInterface(String environmentID) {

		MySQLEnvironment environment;
		if(environmentID != null) {
			 environment = MySQLEnvironmentManagement.getEnvironment(Integer.parseInt(environmentID));
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "MySQL Query Status: The chosen environment seems not configured correctly.");
			return null;
		}
		
		//---------------------------------
		// Get DB
		return environment.getDBInstance();

	}
	
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDatabases.PACKAGE_RESOURCE, "emp_widget_database_common.js") );
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureMySQL.PACKAGE_RESOURCE, "emp_widget_mysqlquerystatus.js") );
		return array;
	}

	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureMySQL.PERMISSION_WIDGETS_MYSQL);
	}
		
}


