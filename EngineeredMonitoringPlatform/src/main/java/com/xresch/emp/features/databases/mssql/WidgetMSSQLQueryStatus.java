package com.xresch.emp.features.databases.mssql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
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
import com.xresch.emp.features.databases.generic.FeatureGenericJDBC;

public class WidgetMSSQLQueryStatus extends WidgetBaseSQLQueryStatus {

	private static Logger logger = CFWLog.getLogger(WidgetMSSQLQueryStatus.class.getName());
	@Override
	public String getWidgetType() {return "emp_mssqlquerystatus";}

	@SuppressWarnings("rawtypes")
	@Override
	public CFWField createEnvironmentSelectorField() {
		return MSSQLSettingsFactory.createEnvironmentSelectorField();
	}

	@Override
	public DBInterface getDatabaseInterface(String environmentID) {

		MSSQLEnvironment environment;
		if(environmentID != null) {
			 environment = MSSQLEnvironmentManagement.getEnvironment(Integer.parseInt(environmentID));
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "MSSQL Query Status: The chosen environment seems not configured correctly.");
			return null;
		}
		
		//---------------------------------
		// Get DB
		return environment.getDBInstance();

	}

	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = super.getJavascriptFiles();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureMSSQL.PACKAGE_RESOURCE, "emp_widget_mssqlquerystatus.js") );
		return array;
	}
	
	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = super.getLocalizationFiles();
		System.out.println(CFW.JSON.toJSON(map));
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureMSSQL.PACKAGE_RESOURCE, "lang_en_emp_mssql.properties"));
		return map;
	}

	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureMSSQL.PERMISSION_WIDGETS_MSSQL);
	}

}