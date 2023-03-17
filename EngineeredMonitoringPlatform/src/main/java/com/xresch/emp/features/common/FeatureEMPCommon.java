package com.xresch.emp.features.common;

import java.util.Locale;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.jobs.FeatureJobs;
import com.xresch.cfw.features.manual.FeatureManual;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.cfw.spi.CFWAppFeature;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureEMPCommon extends CFWAppFeature {
	
	public static final String PACKAGE_RESOURCE 	= "com.xresch.emp.features.common.resources";
	public static final String PACKAGE_MANUAL 		= "com.xresch.emp.features.common.manual";
		
	public static final ManualPage TOP_PAGE = CFW.Registry.Manual.addManualPage(null, new ManualPage("Engineered Monitoring Platform(EMP)").faicon("fa fa-desktop"));
	
	/**********************************************************************
	 * 
	 **********************************************************************/
	@Override
	public void register() {
		//----------------------------------
		// Register Package
		CFW.Files.addAllowedPackage(PACKAGE_RESOURCE);
		CFW.Files.addAllowedPackage(PACKAGE_MANUAL);
		
		//----------------------------------
		// Register Widgets
		CFW.Registry.Widgets.add(new WidgetCustomThresholdLegend());
		
		//----------------------------------
		// Register Locales
		CFW.Localization.registerLocaleFile(
				Locale.ENGLISH, 
				FeatureJobs.getJobsURI(), 
				new FileDefinition(HandlingType.JAR_RESOURCE, FeatureEMPCommon.PACKAGE_RESOURCE, "lang_en_emp_common.properties")
		);
		
		//----------------------------------
		// Register Manuals	
		TOP_PAGE.addPermission(FeatureManual.PERMISSION_MANUAL)
				.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "z_manual_intro.html");
				
	}
	
	/**********************************************************************
	 * 
	 **********************************************************************/
	@Override public void initializeDB() { /* do nothing */ }
	@Override public void addFeature(CFWApplicationExecutor app) { /* do nothing */ }
	@Override public void startTasks() { /* do nothing */ }
	@Override public void stopFeature() { /* do nothing */ }

	

	
	/**********************************************************************
	 * 
	 **********************************************************************/
	public void registerWidgetsManual(ManualPage parent) {
//		parent.addChild(
//				new ManualPage("Display Examples")
//					.faicon("fas fa-image")
//					.addPermission(FeatureManual.PERMISSION_MANUAL)
//					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "z_manual_widgets_displayexamples.html")
//			);
						
	}
			
}
