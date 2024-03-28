package com.xresch.emp.features.exense.step;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.contextsettings.ContextSettingsChangeListener;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.emp.features.spm.EnvironmentSPM;

public class StepEnvironmentManagement {
	private static Logger logger = CFWLog.getLogger(StepEnvironmentManagement.class.getName());
	
	private static boolean isInitialized = false;
	
	// Contains ContextSettings id and the associated database interface
	private static HashMap<Integer, StepEnvironment> environments = new HashMap<Integer, StepEnvironment>();
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	private StepEnvironmentManagement() {
		//hide public constructor
	}
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	public static void initialize() {
		
		ContextSettingsChangeListener listener = 
				new ContextSettingsChangeListener(StepEnvironment.SETTINGS_TYPE) {
			
			@Override
			public void onChange(AbstractContextSettings setting, boolean isNew) {
				
				StepEnvironment oldSettings = environments.get(setting.getDefaultObject().id());
				if(oldSettings != null) {
					oldSettings.removeCaches();
				}
				
				StepEnvironment newSettings = (StepEnvironment)setting;
				StepEnvironmentManagement.createEnvironment(newSettings);
			}

			@Override
			public void onDelete(AbstractContextSettings typeSettings) {
				StepEnvironment oldSettings = environments.remove(typeSettings.getDefaultObject().id());
				if(oldSettings != null) {
					oldSettings.removeCaches();
				}
			}
		};
		
		CFW.DB.ContextSettings.addChangeListener(listener);
		
		createEnvironments();
		isInitialized = true;
	}
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	private static void createEnvironments() {
		// Clear environments
		environments = new HashMap<Integer, StepEnvironment>();
		
		ArrayList<AbstractContextSettings> settingsArray = CFW.DB.ContextSettings.getContextSettingsForType(StepEnvironment.SETTINGS_TYPE);

		for(AbstractContextSettings settings : settingsArray) {
			StepEnvironment current = (StepEnvironment)settings;
			createEnvironment(current);
			
		}
	}
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	private static void createEnvironment(StepEnvironment environment) {
		
		Integer id = environment.getDefaultObject().id();
		
		environments.remove(id);
		
		if(environment.isProperlyDefined()) {
			environment.initializeCaches();
			environments.put(id, environment);
		}else {
			CFW.Messages.addInfoMessage("Configuration incomplete, at least URL and API token have to be defined.");
		}
	}
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	public static StepEnvironment getEnvironment(int id) {
		if(!isInitialized) { initialize(); }
		return environments.get(id);
	}
	
	/*****************************************************************
	 * 
	 *****************************************************************/
	public static HashMap<Integer, StepEnvironment> getEnvironmentsAll() {
		if(!isInitialized) { initialize(); }
		
		HashMap<Integer, StepEnvironment> clone = new HashMap<>();
		clone.putAll(environments);
		
		return  clone;
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static LinkedHashMap<Integer, String> getEnvironmentsAsSelectOptions() {
		if(!isInitialized) { initialize(); }
		LinkedHashMap<Integer,String> options = new LinkedHashMap<Integer,String>();
		
		for(StepEnvironment env : environments.values()) {
			options.put(env.getDefaultObject().id(), env.getDefaultObject().name());
		}
		
		return options;
	}
	
}
