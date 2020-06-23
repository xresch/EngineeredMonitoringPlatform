package com.xresch.emp.features.environments;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWAppFeature;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.config.Configuration;
import com.xresch.cfw.features.manual.FeatureManual;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.response.bootstrap.MenuItem;
import com.xresch.emp._main.Main;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class FeatureEnvironments extends CFWAppFeature {
	
	//public static final String RESOURCE_PACKAGE = "com.xresch.emp.features.theusinator.resources";	
	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		//CFW.Files.addAllowedPackage(RESOURCE_PACKAGE);
		
		//----------------------------------
		// Register Settings
		CFW.Registry.ContextSettings.register(AWAEnvironment.SETTINGS_TYPE, AWAEnvironment.class);
		CFW.Registry.ContextSettings.register(SPMEnvironment.SETTINGS_TYPE, SPMEnvironment.class);
    
	}

	@Override
	public void initializeDB() {
	
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
	}

	@Override
	public void startTasks() {

	}

	@Override
	public void stopFeature() {

	}

}
