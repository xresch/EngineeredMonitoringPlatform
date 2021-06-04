package com.xresch.emp.features.awa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.contextsettings.ContextSettingsChangeListener;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;


public class AWAEnvironmentManagement {
	private static Logger logger = CFWLog.getLogger(AWAEnvironmentManagement.class.getName());
	
	private static boolean isInitialized = false;
	
	// Contains ContextSettings id and the associated database interface
	private static HashMap<Integer, AWAEnvironment> environmentsWithDB = new HashMap<Integer, AWAEnvironment>();
	

	private AWAEnvironmentManagement() {
		//hide public constructor
	}
	public static void initialize() {
		
		ContextSettingsChangeListener listener = 
				new ContextSettingsChangeListener(AWAEnvironment.SETTINGS_TYPE) {
			
			@Override
			public void onChange(AbstractContextSettings setting, boolean isNew) {
				AWAEnvironment env = (AWAEnvironment)setting;
				AWAEnvironmentManagement.createEnvironment(env);
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
	
	private static void createEnvironments() {
		// Clear environments
		environmentsWithDB = new HashMap<Integer, AWAEnvironment>();
		
		ArrayList<AbstractContextSettings> settingsArray = CFW.DB.ContextSettings.getContextSettingsForType(AWAEnvironment.SETTINGS_TYPE);

		for(AbstractContextSettings settings : settingsArray) {
			AWAEnvironment current = (AWAEnvironment)settings;
			createEnvironment(current);
			
		}
	}
	
	private static void createEnvironment(AWAEnvironment environment) {

		environmentsWithDB.remove(environment.getDefaultObject().id());
		
		if(environment.isDBDefined()) {
			DBInterface db = DBInterface.createDBInterfaceOracle(
					"EMP_AWA",
					environment.dbHost(), 
					environment.dbPort(), 
					environment.dbName(), 
					environment.dbType(), 
					environment.dbUser(), 
					environment.dbPassword()
			);
			
			environment.setDBInstance(db);
			environmentsWithDB.put(environment.getDefaultObject().id(), environment);
		}
	}
	
	
	public static AWAEnvironment getEnvironment(int id) {
		if(!isInitialized) { initialize(); }
		return environmentsWithDB.get(id);
	}
	

	
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	public static AutocompleteResult autocompleteClient(HttpServletRequest request){
		
		AutocompleteList list = new AutocompleteList();
		
		//TODO: Check if environment exists, don't always create a new DBInterface if possible
		AWAEnvironment environment = new AWAEnvironment();
		environment.mapRequestParameters(request);
		
		if(environment.isDBDefined()) {
			
			DBInterface db = DBInterface.createDBInterfaceOracle(
					"EMP_AWA",
					environment.dbHost(), 
					environment.dbPort(), 
					environment.dbName(), 
					environment.dbType(), 
					environment.dbUser(), 
					environment.dbPassword()
			);
			ResultSet result  = db.preparedExecuteQuery(
					  "SELECT COUNT(AH_CLIENT) AS COUNT, AH_CLIENT " 
					+ "FROM UC4.AH "		  
					+ "GROUP BY AH_CLIENT "
					+ "ORDER BY COUNT DESC");
			
			try {
				
				while(result != null && result.next()) {
					Object client = (Object) result.getString("AH_CLIENT");
					Integer count = result.getInt("COUNT");
					list.addItem(client, client, count+" Entries");
				}
			} catch (SQLException e) {
				new CFWLog(logger)
				.severe("Error reading SQL results.", e);
				
			}
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.INFO, "Please specify database first to get autocomplete values.");
		}
		
		return new AutocompleteResult(list);
	}
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	public static AutocompleteResult autocompleteJobname(int environmentID, String searchValue, int maxResults) {

		if(searchValue.length() < 3) {
			return null;
		}
		//---------------------------
		// Get DB
		DBInterface db;
		AWAEnvironment environment = AWAEnvironmentManagement.getEnvironment(environmentID);
		db = environment.getDBInstance();
		
		if(db == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "The chosen environment seems not configured correctly.");
			return null;
		}
		//System.out.println("===== Query ==== \n"+CFW.Files.readPackageResource(FeatureAWA.PACKAGE_RESOURCE, "emp_awa_autocomplete_jobnames.sql"));
		ResultSet result = db.preparedExecuteQuery(
			CFW.Files.readPackageResource(FeatureAWA.PACKAGE_RESOURCE, "emp_awa_autocomplete_jobnames.sql"),
			environment.clientID(),
			"%"+searchValue+"%",
			maxResults);
		
		LinkedHashMap<Object, Object> suggestions = new LinkedHashMap<Object, Object>();
		try {
			if(result != null) {
				for(int i = 0;i < maxResults && result.next();i++) {
					String jobname = result.getString("NAME");
					suggestions.put(jobname, jobname);	
				}
			}
		
		} catch (SQLException e) {
			new CFWLog(logger)
				.severe("Error fetching autocomplete data.", e);
		}finally {
			db.close(result);
		}
		
		return new AutocompleteResult(suggestions);
	}
}
