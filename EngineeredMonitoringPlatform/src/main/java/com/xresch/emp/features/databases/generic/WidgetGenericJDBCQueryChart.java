package com.xresch.emp.features.databases.generic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.features.dashboard.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.emp.features.databases.WidgetBaseSQLQueryChart;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class WidgetGenericJDBCQueryChart extends WidgetBaseSQLQueryChart {

	private static Logger logger = CFWLog.getLogger(WidgetGenericJDBCQueryChart.class.getName());
	@Override
	public String getWidgetType() {return "emp_genericjdbcquerychart";}
		
	@SuppressWarnings("rawtypes")
	@Override
	public CFWField createEnvironmentSelectorField() {
		return GenericJDBCSettingsFactory.createEnvironmentSelectorField();
	}

	@Override
	public DBInterface getDatabaseInterface(String environmentID) {

		GenericJDBCEnvironment environment;
		if(environmentID != null) {
			 environment = GenericJDBCEnvironmentManagement.getEnvironment(Integer.parseInt(environmentID));
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "Generic JDBC Query Chart: The chosen environment seems not configured correctly.");
			return null;
		}
		
		//---------------------------------
		// Get DB
		return environment.getDBInstance();

	}
	
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = super.getJavascriptFiles();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureGenericJDBC.PACKAGE_RESOURCE, "emp_widget_genericjdbcquerychart.js") );
		return array;
	}
	
	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = super.getLocalizationFiles();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureGenericJDBC.PACKAGE_RESOURCE, "lang_en_emp_genericjdbc.properties"));
		return map;
	}

	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureGenericJDBC.PERMISSION_GENERICJDBC);
	}

}
