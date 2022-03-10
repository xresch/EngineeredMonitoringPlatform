package com.xresch.emp.features.mongodb;

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
import com.xresch.emp.features.databases.WidgetBaseSQLQueryStatus;

public class WidgetMongoDBQueryStatus extends WidgetBaseSQLQueryStatus {

	
	private static Logger logger = CFWLog.getLogger(WidgetMongoDBQueryStatus.class.getName());
	
	@Override
	public String getWidgetType() {return "emp_mongodb_querystatus";}


	@Override
	public CFWField createEnvironmentSelectorField() {
		return MongoDBSettingsFactory.createEnvironmentSelectorField();
	}

	@Override
	public DBInterface getDatabaseInterface(String environmentID) {

		MongoDBEnvironment environment;
		if(environmentID != null) {
			 environment = MongoDBEnvironmentManagement.getEnvironment(Integer.parseInt(environmentID));
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "MongoDB Query Status: The chosen environment seems not configured correctly.");
			return null;
		}
		
		//---------------------------------
		// Get DB
		return null;
		//return environment.getMongoDB();

	}
	
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = super.getJavascriptFiles();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureMongoDB.PACKAGE_RESOURCE, "emp_widget_mongodb_querystatus.js") );
		return array;
	}
	
	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = super.getLocalizationFiles();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureMongoDB.PACKAGE_RESOURCE, "lang_en_emp_mongodb.properties"));
		return map;
	}
	
	
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureMongoDB.PERMISSION_MONGODB);
	}
	
		
}


