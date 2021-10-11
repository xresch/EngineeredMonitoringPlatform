package com.xresch.emp.features.oracle;

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
import com.xresch.emp.features.databases.WidgetBaseSQLQueryStatus;

public class WidgetOracleQueryStatus extends WidgetBaseSQLQueryStatus {

	private static Logger logger = CFWLog.getLogger(WidgetOracleQueryStatus.class.getName());
	@Override
	public String getWidgetType() {return "emp_oraclequerystatus";}

	@Override
	public CFWField createEnvironmentSelectorField() {
		return OracleSettingsFactory.createEnvironmentSelectorField();
	}

	@Override
	public DBInterface getDatabaseInterface(String environmentID) {

		OracleEnvironment environment;
		if(environmentID != null) {
			 environment = OracleEnvironmentManagement.getEnvironment(Integer.parseInt(environmentID));
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "Oracle Query Status: The chosen environment seems not configured correctly.");
			return null;
		}
		
		//---------------------------------
		// Get DB
		return environment.getDBInstance();

	}
	
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureOracle.PACKAGE_RESOURCE, "emp_widget_oraclequerystatus.js") );
		return array;
	}
	
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureOracle.PERMISSION_WIDGETS_ORACLE);
	}

}
