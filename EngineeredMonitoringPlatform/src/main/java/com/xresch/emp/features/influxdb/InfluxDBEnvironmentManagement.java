package com.xresch.emp.features.influxdb;

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


public class InfluxDBEnvironmentManagement {
	
	private static Logger logger = CFWLog.getLogger(InfluxDBEnvironmentManagement.class.getName());
	
	private static boolean isInitialized = false;

	// Contains ContextSettings id and the associated database interface
	private static HashMap<Integer, InfluxDBEnvironment> environments = new HashMap<Integer, InfluxDBEnvironment>();
	
	private InfluxDBEnvironmentManagement() {
		// hide public constructor
	}
	/************************************************************************
	 * 
	 ************************************************************************/
	public static void initialize() {
	
		ContextSettingsChangeListener listener = 
				new ContextSettingsChangeListener(InfluxDBEnvironment.SETTINGS_TYPE) {
			
			@Override
			public void onChange(AbstractContextSettings setting, boolean isNew) {
				InfluxDBEnvironment env = (InfluxDBEnvironment)setting;
				InfluxDBEnvironmentManagement.createEnvironment(env);
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
		environments = new HashMap<Integer, InfluxDBEnvironment>();
		
		ArrayList<AbstractContextSettings> settingsArray = CFW.DB.ContextSettings.getContextSettingsForType(InfluxDBEnvironment.SETTINGS_TYPE);

		for(AbstractContextSettings settings : settingsArray) {
			InfluxDBEnvironment current = (InfluxDBEnvironment)settings;
			createEnvironment(current);
			
		}
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	private static void createEnvironment(InfluxDBEnvironment environment) {

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
		
		for(InfluxDBEnvironment env : environments.values()) {
			options.put(env.getDefaultObject().id(), env.getDefaultObject().name());
		}
		
		return options;
	}
	
	/************************************************************************
	 * 
	 ************************************************************************/
	public static InfluxDBEnvironment getEnvironment(int id) {
		if(!isInitialized) { initialize(); }
		return environments.get(id);
	}
	
}
