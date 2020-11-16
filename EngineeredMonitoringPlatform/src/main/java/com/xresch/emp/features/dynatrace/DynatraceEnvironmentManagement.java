package com.xresch.emp.features.dynatrace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.contextsettings.ContextSettingsChangeListener;
import com.xresch.cfw.logging.CFWLog;


public class DynatraceEnvironmentManagement {
	
	private static Logger logger = CFWLog.getLogger(DynatraceEnvironmentManagement.class.getName());
	
	private static boolean isInitialized = false;

	// Contains ContextSettings id and the associated database interface
	private static HashMap<Integer, DynatraceEnvironment> environments = new HashMap<Integer, DynatraceEnvironment>();
	
	private DynatraceEnvironmentManagement() {
		// hide public constructor
	}
	/************************************************************************
	 * 
	 ************************************************************************/
	public static void initialize() {
	
		ContextSettingsChangeListener listener = 
				new ContextSettingsChangeListener(DynatraceEnvironment.SETTINGS_TYPE) {
			
			@Override
			public void onChange(AbstractContextSettings setting, boolean isNew) {
				DynatraceEnvironment env = (DynatraceEnvironment)setting;
				DynatraceEnvironmentManagement.createEnvironment(env);
			}
			
			@Override
			public void onDelete(AbstractContextSettings typeSettings) {
				environments.remove(typeSettings.getDefaultObject().id());
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
		environments = new HashMap<Integer, DynatraceEnvironment>();
		
		ArrayList<AbstractContextSettings> settingsArray = CFW.DB.ContextSettings.getContextSettingsForType(DynatraceEnvironment.SETTINGS_TYPE);

		for(AbstractContextSettings settings : settingsArray) {
			DynatraceEnvironment current = (DynatraceEnvironment)settings;
			createEnvironment(current);
			
		}
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	private static void createEnvironment(DynatraceEnvironment environment) {

		environments.remove(environment.getDefaultObject().id());

//		InetSocketAddress address = new InetSocketAddress(environment.apiURL());
//		if(address.isUnresolved()) {
//			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The URL could not be resolved: "+environment.apiURL()+":"+environment.apiToken());
//			return;
//		};
		
		environments.put(environment.getDefaultObject().id(), environment);
		
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static LinkedHashMap<Integer, String> getEnvironmentsAsSelectOptions() {
		if(!isInitialized) { initialize(); }
		LinkedHashMap<Integer,String> options = new LinkedHashMap<Integer,String>();
		
		for(DynatraceEnvironment env : environments.values()) {
			options.put(env.getDefaultObject().id(), env.getDefaultObject().name());
		}
		
		return options;
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static DynatraceEnvironment getEnvironment(int id) {
		if(!isInitialized) { initialize(); }
		return environments.get(id);
	}
	
}
