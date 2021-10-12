package com.xresch.emp.features.databases;

import java.util.Locale;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.dashboard.FeatureDashboard;
import com.xresch.cfw.features.jobs.FeatureJobs;
import com.xresch.cfw.features.manual.FeatureManual;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.spi.CFWAppFeature;
import com.xresch.emp.features.common.FeatureEMPCommon;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureDatabases extends CFWAppFeature {
	
	public static final String PACKAGE_MANUAL   = "com.xresch.emp.features.databases.manual";
	public static final String PACKAGE_RESOURCE = "com.xresch.emp.features.databases.resources";
	
			
	@Override
	public void register() {
		//----------------------------------
		// Register Settings
		CFW.Files.addAllowedPackage(PACKAGE_MANUAL);
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCE);
		
		//----------------------------------
		// Register Locales
		CFW.Localization.registerLocaleFile(
				Locale.ENGLISH, 
				FeatureDashboard.URI_DASHBOARD_VIEW, 
				new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDatabases.PACKAGE_RESOURCE, "lang_en_emp_database.properties")
		);
		
		CFW.Localization.registerLocaleFile(
				Locale.ENGLISH, 
				FeatureJobs.getJobsURI(), 
				new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDatabases.PACKAGE_RESOURCE, "lang_en_emp_database.properties")
		);
		
		//----------------------------------
		// Register Manual Page
		FeatureEMPCommon.WIDGET_PAGE.addChild(
				new ManualPage("Database Widgets")
					.faicon("fas fa-desktop")
					.addPermission(FeatureManual.PERMISSION_MANUAL)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "widgets_database.html")
			);
		
	}

	@Override
	public void initializeDB() {
		/* do nothing */
	}

	@Override
	public void addFeature(CFWApplicationExecutor app) {	
		/* do nothing */
	}

	@Override
	public void startTasks() {
		/* do nothing */
	}

	@Override
	public void stopFeature() {
		/* do nothing */
	}

}
