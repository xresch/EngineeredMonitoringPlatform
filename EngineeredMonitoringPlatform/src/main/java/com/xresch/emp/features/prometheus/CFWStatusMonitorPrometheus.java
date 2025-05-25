package com.xresch.emp.features.prometheus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.extensions.databases.generic.GenericJDBCEnvironment;
import com.xresch.cfw.extensions.databases.mssql.MSSQLEnvironment;
import com.xresch.cfw.extensions.databases.mysql.MySQLEnvironment;
import com.xresch.cfw.extensions.databases.oracle.OracleEnvironment;
import com.xresch.cfw.extensions.databases.postgres.PostgresEnvironment;
import com.xresch.cfw.features.analytics.CFWStatusMonitor;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.utils.CFWState.CFWStateOption;

public class CFWStatusMonitorPrometheus implements CFWStatusMonitor {

	/*************************************************************
	 * 
	 *************************************************************/
	@Override
	public String category() {
		return "Prometheus";
	}

	/*************************************************************
	 * 
	 *************************************************************/
	@Override
	public String uniqueName() {
		return "Prometheus Monitor";
	}

	/*************************************************************
	 * 
	 *************************************************************/
	@Override
	public HashMap<JsonObject, CFWStateOption> getStatuses() {
		
		
		LinkedHashMap<JsonObject, CFWStateOption> result = new LinkedHashMap<>();
		
		//----------------------------------
		// Generic JDBC
		ArrayList<AbstractContextSettings> settingsList = CFW.DB.ContextSettings.getContextSettingsForType(PrometheusEnvironment.SETTINGS_TYPE, true);
		for(AbstractContextSettings  setting : settingsList) {
			
			PrometheusEnvironment env = (PrometheusEnvironment)setting;
			
			CFWStateOption state = env.getStatus();
			
			JsonObject object = new JsonObject();
			object.addProperty("name", env.getDefaultObject().name()); 
			object.addProperty("type", env.getDefaultObject().type()); 
			
			result.put(object, state);
		}
		
		return result;
	}
	

}
