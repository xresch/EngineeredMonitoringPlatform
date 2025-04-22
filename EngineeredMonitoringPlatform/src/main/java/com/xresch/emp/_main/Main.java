package com.xresch.emp._main;

import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.extensions.influxdb.FeatureInfluxDB;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.spi.CFWAppInterface;
import com.xresch.emp.features.awa.FeatureAWA;
import com.xresch.emp.features.common.FeatureEMPCommon;
import com.xresch.emp.features.dynatrace.FeatureDynatrace;
import com.xresch.emp.features.exense.step.FeatureExenseStep;
import com.xresch.emp.features.exense.stepmongodb.FeatureExenseStepMongoDB;
import com.xresch.emp.features.mongodb.FeatureMongoDB;
import com.xresch.emp.features.prometheus.FeaturePrometheus;
import com.xresch.emp.features.spm.FeatureSPM;
import com.xresch.emp.features.spm.FeatureSPMTheusinator;
import com.xresch.emp.features.webex.FeatureWebex;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2020
 **************************************************************************************************************/
public class Main implements CFWAppInterface {
	
	//Don't do a logger here
	//private static final Logger logger = CFWLog.getLogger(Main.class.getName());
	
    public static void main( String[] args ) throws Exception
    {
    	CFW.initializeApp(new Main(), args);
    }
    
	@Override
	public void settings() {
		CFW.AppSettings.enableContextSettings(true);
		CFW.AppSettings.enableDashboarding(true);
	}
	
	@Override
	public void register() {
    	
		//----------------------------------
		// Register Features
    	CFW.Registry.Features.addFeature(FeatureEMPCommon.class);
		CFW.Registry.Features.addFeature(FeatureAWA.class);
		
		CFW.Registry.Features.addFeature(FeatureMongoDB.class);
		
		CFW.Registry.Features.addFeature(FeatureDynatrace.class);
		CFW.Registry.Features.addFeature(FeatureExenseStep.class);
		//CFW.Registry.Features.addFeature(FeatureExenseStepMongoDB.class);
    	CFW.Registry.Features.addFeature(FeaturePrometheus.class);
    	CFW.Registry.Features.addFeature(FeatureSPM.class);
    	CFW.Registry.Features.addFeature(FeatureSPMTheusinator.class);
    	CFW.Registry.Features.addFeature(FeatureWebex.class);

    	
	}

	@Override
	public void initializeDB() {
		//do nothing
	}
	
	@Override
	public void stopApp() {
		//do nothing
	}
	
	@Override
	public void startApp(CFWApplicationExecutor executor) {
	    	
	        	        
	        //###################################################################
	        // Startup
	        //###################################################################
	        executor.setDefaultURL("/dashboard/list", true);
	        
	        try {
				executor.startServer();
			} catch (Exception e) {
				Logger logger = CFWLog.getLogger(Main.class.getName());
				new CFWLog(logger)
				.severe("Exception occured during startup.", e);
			}
		
	}


	@Override
	public void startTasks() {
		//do nothing
		
	}

}
