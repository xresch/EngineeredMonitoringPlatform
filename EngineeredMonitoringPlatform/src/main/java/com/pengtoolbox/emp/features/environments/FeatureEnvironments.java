package com.pengtoolbox.emp.features.environments;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw._main.CFWAppFeature;
import com.pengtoolbox.cfw._main.CFWApplicationExecutor;
import com.pengtoolbox.cfw.caching.FileDefinition.HandlingType;
import com.pengtoolbox.cfw.datahandling.CFWField.FormFieldType;
import com.pengtoolbox.cfw.features.config.Configuration;
import com.pengtoolbox.cfw.features.manual.FeatureManual;
import com.pengtoolbox.cfw.features.manual.ManualPage;
import com.pengtoolbox.cfw.features.usermgmt.Permission;
import com.pengtoolbox.cfw.response.bootstrap.MenuItem;
import com.pengtoolbox.emp._main.Main;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, � 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class FeatureEnvironments extends CFWAppFeature {
	
	//public static final String RESOURCE_PACKAGE = "com.pengtoolbox.emp.features.theusinator.resources";	
	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		//CFW.Files.addAllowedPackage(RESOURCE_PACKAGE);
		
		//----------------------------------
		// Register Settings
		CFW.Registry.ContextSettings.register(EnvironmentAWA.SETTINGS_TYPE, EnvironmentAWA.class);
		CFW.Registry.ContextSettings.register(EnvironmentSPM.SETTINGS_TYPE, EnvironmentSPM.class);
    
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
