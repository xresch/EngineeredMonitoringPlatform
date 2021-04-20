package com.xresch.emp.features.spm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import org.apache.commons.dbcp2.BasicDataSource;

import com.microsoft.sqlserver.jdbc.SQLServerConnectionPoolDataSource;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.contextsettings.ContextSettingsChangeListener;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.emp.features.common.EMPDBInterfaceFactory;


public class EnvironmentManagerSPM {
	
	private static Logger logger = CFWLog.getLogger(EnvironmentManagerSPM.class.getName());
	
	private static boolean isInitialized = false;

	// Contains ContextSettings id and the associated database interface
	private static HashMap<Integer, EnvironmentSPM> environmentsWithDB = new HashMap<Integer, EnvironmentSPM>();
	
	private EnvironmentManagerSPM() {
		// hide public constructor
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static void initialize() {
	
		ContextSettingsChangeListener listener = 
				new ContextSettingsChangeListener(EnvironmentSPM.SETTINGS_TYPE) {
			
			@Override
			public void onChange(AbstractContextSettings setting, boolean isNew) {
				EnvironmentSPM env = (EnvironmentSPM)setting;
				EnvironmentManagerSPM.createEnvironment(env);
			}
			
			@Override
			public void onDelete(AbstractContextSettings typeSettings) {
				environmentsWithDB.remove(typeSettings.getDefaultObject().id());
			}
		};
		
		CFW.DB.ContextSettings.addChangeListener(listener);
		
		createEnvironments();
		isInitialized = true;
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	private static void createEnvironments() {
		// Clear environments
		environmentsWithDB = new HashMap<Integer, EnvironmentSPM>();
		
		ArrayList<AbstractContextSettings> settingsArray = CFW.DB.ContextSettings.getContextSettingsForType(EnvironmentSPM.SETTINGS_TYPE);

		for(AbstractContextSettings settings : settingsArray) {
			EnvironmentSPM current = (EnvironmentSPM)settings;
			createEnvironment(current);
			
		}
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	private static void createEnvironment(EnvironmentSPM environment) {

		environmentsWithDB.remove(environment.getDefaultObject().id());

		if(environment.isDBDefined()) {
			
			DBInterface db = EMPDBInterfaceFactory.createMSSQLInterface(
					environment.dbHost(), 
					environment.dbPort(), 
					environment.dbName(), 
					environment.dbUser(), 
					environment.dbPassword()
			);
			environment.setDBInstance(db);
			environmentsWithDB.put(environment.getDefaultObject().id(), environment);
		}
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static LinkedHashMap<Integer, String> getEnvironmentsAsSelectOptions() {
		if(!isInitialized) { initialize(); }
		LinkedHashMap<Integer,String> options = new LinkedHashMap<Integer,String>();
		
		for(EnvironmentSPM env : environmentsWithDB.values()) {
			options.put(env.getDefaultObject().id(), env.getDefaultObject().name());
		}
		
		return options;
	}
	/************************************************************************
	 * 
	 ************************************************************************/
	public static EnvironmentSPM getEnvironment(int id) {
		if(!isInitialized) { initialize(); }
		return environmentsWithDB.get(id);
	}
	
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static AutocompleteResult autocompleteMonitors(int environmentID, String searchValue, int maxResults) {

		if(searchValue.length() < 3) {
			return null;
		}
		//---------------------------
		// Get DB
		DBInterface db;
		
		db = EnvironmentManagerSPM.getEnvironment(environmentID).getDBInstance();
		
		if(db == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "The chosen environment seems not configured correctly.");
			return null;
		}
		
		ResultSet result = db.preparedExecuteQuery(
			CFW.Files.readPackageResource(FeatureSPM.PACKAGE_RESOURCE, "emp_spm_monitor_autocomplete.sql"),
			"%"+searchValue+"%",
			"%"+searchValue+"%");
		
		LinkedHashMap<Object, Object> suggestions = new LinkedHashMap<Object, Object>();
		try {
			if(result != null) {
				for(int i = 0;i < maxResults && result.next();i++) {
					int monitorID = result.getInt("MonitorID");
					String monitorName = result.getString("MonitorName");
					String projectName = result.getString("ProjectName");
					suggestions.put(monitorID, projectName +" &gt;&gt; "+ monitorName);	
				}
			}
		
		} catch (SQLException e) {
			new CFWLog(logger)
				.severe("Error fetching Widget data.", e);
		}finally {
			db.close(result);
		}
		
		return new AutocompleteResult(suggestions);
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static AutocompleteResult autocompleteMonitorName(int environmentID, String searchValue, int maxResults) {

		if(searchValue.length() < 3) {
			return null;
		}
		//---------------------------
		// Get DB
		DBInterface db;
		
		db = EnvironmentManagerSPM.getEnvironment(environmentID).getDBInstance();
		
		if(db == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "The chosen environment seems not configured correctly.");
			return null;
		}
		
		ResultSet result = db.preparedExecuteQuery(
			CFW.Files.readPackageResource(FeatureSPM.PACKAGE_RESOURCE, "emp_spm_monitor_autocomplete.sql"),
			"%"+searchValue+"%",
			"%"+searchValue+"%");
		
		LinkedHashMap<Object, Object> suggestions = new LinkedHashMap<Object, Object>();
		try {
			if(result != null) {
				for(int i = 0;i < maxResults && result.next();i++) {
					String monitorName = result.getString("MonitorName");
					String projectName = result.getString("ProjectName");
					suggestions.put(monitorName, projectName +" &gt;&gt; "+ monitorName);	
				}
			}
		
		} catch (SQLException e) {
			new CFWLog(logger)
				.severe("Error fetching Monitor names.", e);
		}finally {
			db.close(result);
		}
		
		return new AutocompleteResult(suggestions);
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static AutocompleteResult autocompleteProjects(int environmentID, String searchValue, int maxResults) {

		if(searchValue.length() < 3) {
			return null;
		}
		//---------------------------
		// Get DB
		DBInterface db;
		
		db = EnvironmentManagerSPM.getEnvironment(environmentID).getDBInstance();
		
		if(db == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "The chosen environment seems not configured correctly.");
			return null;
		}
		
		ResultSet result = db.preparedExecuteQuery(
			CFW.Files.readPackageResource(FeatureSPM.PACKAGE_RESOURCE, "emp_spm_project_autocomplete.sql"),
			"%"+searchValue+"%");
		
		LinkedHashMap<Object, Object> suggestions = new LinkedHashMap<Object, Object>();
		try {
			if(result != null) {
				for(int i = 0;i < maxResults && result.next();i++) {
					int projectID = result.getInt("ProjectID");
					String projectName = result.getString("ProjectName");
					suggestions.put(projectID, projectName);	
				}
			}
		
		} catch (SQLException e) {
			new CFWLog(logger)
				.severe("Error fetching Widget data.", e);
		}finally {
			db.close(result);
		}
		
		return new AutocompleteResult(suggestions);
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static AutocompleteResult autocompleteCountersForMonitor(int environmentID,  int monitorID, String searchValue, int maxResults) {

		if(searchValue.length() < 3) {
			return null;
		}
		
		//---------------------------
		// Get DB
		DBInterface db;
		
		db = EnvironmentManagerSPM.getEnvironment(environmentID).getDBInstance();
		
		if(db == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "The chosen environment seems not configured correctly.");
			return null;
		}

		ResultSet result = db.preparedExecuteQuery(
			CFW.Files.readPackageResource(FeatureSPM.PACKAGE_RESOURCE, "emp_spm_measuresformonitor_autocomplete.sql"),
			"%Custom counter%"+searchValue+"%",
			monitorID
			);
		
		LinkedHashMap<Object, Object> suggestions = new LinkedHashMap<Object, Object>();
		try {
			if(result != null) {
				for(int i = 0;i < maxResults && result.next();i++) {
					String measureName = result.getString("MeasureName");
					suggestions.put(measureName, measureName.split("/")[1] );	
				}
			}
		
		} catch (SQLException e) {
			new CFWLog(logger)
				.severe("Error fetching Widget data.", e);
		}finally {
			db.close(result);
		}
		
		return new AutocompleteResult(suggestions);
	}

	/************************************************************************
	 * 
	 ************************************************************************/
	public static AutocompleteResult autocompleteTimersForMonitor(int environmentID,  int monitorID, String searchValue, int maxResults) {

		if(searchValue.length() < 3) {
			return null;
		}
		
		//---------------------------
		// Get DB
		DBInterface db;
		
		db = EnvironmentManagerSPM.getEnvironment(environmentID).getDBInstance();
		
		if(db == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "The chosen environment seems not configured correctly.");
			return null;
		}

		ResultSet result = db.preparedExecuteQuery(
			CFW.Files.readPackageResource(FeatureSPM.PACKAGE_RESOURCE, "emp_spm_measuresformonitor_autocomplete.sql"),
			"%Custom timer%"+searchValue+"%",
			monitorID
			);
		
		LinkedHashMap<Object, Object> suggestions = new LinkedHashMap<Object, Object>();
		try {
			if(result != null) {
				for(int i = 0;i < maxResults && result.next();i++) {
					String measureName = result.getString("MeasureName");
					suggestions.put(measureName, measureName.split("/")[1] );	
				}
			}
		
		} catch (SQLException e) {
			new CFWLog(logger)
				.severe("Error fetching Widget data.", e);
		}finally {
			db.close(result);
		}
		
		return new AutocompleteResult(suggestions);
	}
	/************************************************************************
	 * 
	 ************************************************************************/
	public static AutocompleteResult autocompleteCountersForProject(int environmentID,  int projectID, String searchValue, int maxResults) {

		if(searchValue.length() < 3) {
			return null;
		}
		
		//---------------------------
		// Get DB
		DBInterface db;
		
		db = EnvironmentManagerSPM.getEnvironment(environmentID).getDBInstance();
		
		if(db == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "The chosen environment seems not configured correctly.");
			return null;
		}

		ResultSet result = db.preparedExecuteQuery(
			CFW.Files.readPackageResource(FeatureSPM.PACKAGE_RESOURCE, "emp_spm_measuresforproject_autocomplete.sql"),
			"%Custom counter%"+searchValue+"%",
			projectID
			);
		
		LinkedHashMap<Object, Object> suggestions = new LinkedHashMap<Object, Object>();
		try {
			if(result != null) {
				for(int i = 0;i < maxResults && result.next();i++) {
					String measureName = result.getString("MeasureName");
					suggestions.put(measureName, measureName.split("/")[1] );	
				}
			}
		
		} catch (SQLException e) {
			new CFWLog(logger)
				.severe("Error fetching Widget data.", e);
		}finally {
			db.close(result);
		}
		
		return new AutocompleteResult(suggestions);
	}
	
	public static AutocompleteResult autocompleteTimersForProject(int environmentID,  int projectID, String searchValue, int maxResults) {

		if(searchValue.length() < 3) {
			return null;
		}
		
		//---------------------------
		// Get DB
		DBInterface db;
		
		db = EnvironmentManagerSPM.getEnvironment(environmentID).getDBInstance();
		
		if(db == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "The chosen environment seems not configured correctly.");
			return null;
		}

		ResultSet result = db.preparedExecuteQuery(
			CFW.Files.readPackageResource(FeatureSPM.PACKAGE_RESOURCE, "emp_spm_measuresforproject_autocomplete.sql"),
			"%Custom timer%"+searchValue+"%",
			projectID
			);
		
		LinkedHashMap<Object, Object> suggestions = new LinkedHashMap<Object, Object>();
		try {
			if(result != null) {
				for(int i = 0;i < maxResults && result.next();i++) {
					String measureName = result.getString("MeasureName");
					suggestions.put(measureName, measureName.split("/")[1] );	
				}
			}
		
		} catch (SQLException e) {
			new CFWLog(logger)
				.severe("Error fetching Widget data.", e);
		}finally {
			db.close(result);
		}
		
		return new AutocompleteResult(suggestions);
	}
}
