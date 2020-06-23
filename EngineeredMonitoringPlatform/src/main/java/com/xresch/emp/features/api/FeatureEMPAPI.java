package com.xresch.emp.features.api;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWAppFeature;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.emp.features.environments.AWAEnvironmentManagement;
import com.xresch.emp.features.environments.SPMEnvironmentManagement;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class FeatureEMPAPI extends CFWAppFeature {
	
	public static final String RESOURCE_PACKAGE = "com.xreschemp.features.api.resources";
	public static final String MANUAL_PACKAGE = "com.xreschemp.features.api.manual";
	
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(RESOURCE_PACKAGE);
		CFW.Files.addAllowedPackage(MANUAL_PACKAGE); 
		
		//-----------------------------------------
		// 
		//-----------------------------------------
		
	}

	@Override
	public void initializeDB() {
					
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {
		
		//Register here as it won't work before DB is initialized.
		CFW.Registry.API.addAll(SPMAPI.getAPIDefinitions());
	}

	@Override
	public void startTasks() {}

	@Override
	public void stopFeature() {}

}
