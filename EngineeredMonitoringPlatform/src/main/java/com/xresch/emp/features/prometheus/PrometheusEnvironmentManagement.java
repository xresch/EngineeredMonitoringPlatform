package com.xresch.emp.features.prometheus;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.contextsettings.ContextSettingsChangeListener;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;


public class PrometheusEnvironmentManagement {
	
	private static Logger logger = CFWLog.getLogger(PrometheusEnvironmentManagement.class.getName());
	
	private static boolean isInitialized = false;

	// Contains ContextSettings id and the associated database interface
	private static HashMap<Integer, PrometheusEnvironment> environments = new HashMap<Integer, PrometheusEnvironment>();
	
	private PrometheusEnvironmentManagement() {
		// hide public constructor
	}
	/************************************************************************
	 * 
	 ************************************************************************/
	public static void initialize() {
	
		ContextSettingsChangeListener listener = 
				new ContextSettingsChangeListener(PrometheusEnvironment.SETTINGS_TYPE) {
			
			@Override
			public void onChange(AbstractContextSettings setting, boolean isNew) {
				PrometheusEnvironment env = (PrometheusEnvironment)setting;
				PrometheusEnvironmentManagement.createEnvironment(env);
			}
			
			@Override
			public void onDeleteOrDeactivate(AbstractContextSettings typeSettings) {
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
		environments = new HashMap<Integer, PrometheusEnvironment>();
		
		ArrayList<AbstractContextSettings> settingsArray = CFW.DB.ContextSettings.getContextSettingsForType(PrometheusEnvironment.SETTINGS_TYPE, true);

		for(AbstractContextSettings settings : settingsArray) {
			PrometheusEnvironment current = (PrometheusEnvironment)settings;
			createEnvironment(current);
			
		}
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	private static void createEnvironment(PrometheusEnvironment environment) {

		environments.remove(environment.getDefaultObject().id());

		InetSocketAddress address = new InetSocketAddress(environment.host(), environment.port());
		if(address.isUnresolved()) {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The URL could not be resolved: "+environment.host()+":"+environment.port());
			return;
		};
		
		environments.put(environment.getDefaultObject().id(), environment);
		
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static LinkedHashMap<Integer, String> getEnvironmentsAsSelectOptions() {
		if(!isInitialized) { initialize(); }
		LinkedHashMap<Integer,String> options = new LinkedHashMap<Integer,String>();
		
		for(PrometheusEnvironment env : environments.values()) {
			options.put(env.getDefaultObject().id(), env.getDefaultObject().name());
		}
		
		return options;
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static PrometheusEnvironment getEnvironment(int id) {
		if(!isInitialized) { initialize(); }
		return environments.get(id);
	}
	
}
