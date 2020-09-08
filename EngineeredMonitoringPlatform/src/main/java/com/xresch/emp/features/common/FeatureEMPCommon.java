package com.xresch.emp.features.common;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWAppFeature;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.manual.FeatureManual;
import com.xresch.cfw.features.manual.ManualPage;
import com.xresch.emp.features.spm.FeatureSPM;
import com.xresch.emp.features.webex.FeatureWebex;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class FeatureEMPCommon extends CFWAppFeature {
	
	public static final String PACKAGE_RESOURCE 	= "com.xresch.emp.features.common.resources";
	public static final String PACKAGE_MANUAL 		= "com.xresch.emp.features.common.manual";
		
	public static final ManualPage TOP_PAGE = CFW.Registry.Manual.addManualPage(null, new ManualPage("Engineered Monitoring Platform(EMP)").faicon("fa fa-desktop"));
	public static final ManualPage WIDGET_PAGE = new ManualPage("Dashboard Widgets").faicon("fas fa-th-large");
	
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
		// Register Manuals	
		WIDGET_PAGE.addPermission(FeatureManual.PERMISSION_MANUAL)
				.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "z_manual_intro.html");
		
		TOP_PAGE.addChild(WIDGET_PAGE);
		registerWidgetsManual(WIDGET_PAGE);
		
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
		parent.addChild(
				new ManualPage("Display Examples")
					.faicon("fas fa-image")
					.addPermission(FeatureManual.PERMISSION_MANUAL)
					.content(HandlingType.JAR_RESOURCE, PACKAGE_MANUAL, "z_manual_widgets_displayexamples.html")
			);
						
	}
			
}
