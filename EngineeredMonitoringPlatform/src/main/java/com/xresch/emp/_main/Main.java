package com.xresch.emp._main;

import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWAppInterface;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.emp.features.api.FeatureEMPAPI;
import com.xresch.emp.features.environments.FeatureEMPEnvironments;
import com.xresch.emp.features.manual.FeatureEMPManual;
import com.xresch.emp.features.theusinator.FeatureTheusinator;
import com.xresch.emp.features.widgets.FeatureEMPWidgets;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class Main implements CFWAppInterface {
	
	public static Logger logger = CFWLog.getLogger(Main.class.getName());
	protected static CFWLog log = new CFWLog(logger);
	
    public static void main( String[] args ) throws Exception
    {
    	CFW.initializeApp(new Main(), args);
    }
    
	@Override
	public void settings() {
		CFW.AppSettings.setEnableContextSettings(true);
		CFW.AppSettings.setEnableDashboarding(true);
	}
	
	@Override
	public void register() {
    	
		//----------------------------------
		// Register Objects
		CFW.Registry.Features.addFeature(FeatureEMPEnvironments.class);
    	CFW.Registry.Features.addFeature(FeatureTheusinator.class);
    	CFW.Registry.Features.addFeature(FeatureEMPWidgets.class);
    	CFW.Registry.Features.addFeature(FeatureEMPAPI.class);
    	CFW.Registry.Features.addFeature(FeatureEMPManual.class);
		
	}

	@Override
	public void initializeDB() {
		
	}
	
	@Override
	public void stopApp() {
		
	}
	
	@Override
	public void startApp(CFWApplicationExecutor app) {
	    	
	        	        
	        //###################################################################
	        // Startup
	        //###################################################################
	        app.setDefaultURL("/dashboard/list", true);
	        
	        try {
				app.start();
			} catch (Exception e) {
				new CFWLog(logger)
				.method("startApp")
				.severe("Exception occured during startup.", e);
			}
		
	}


	@Override
	public void startTasks() {
		// TODO Auto-generated method stub
		
	}

}