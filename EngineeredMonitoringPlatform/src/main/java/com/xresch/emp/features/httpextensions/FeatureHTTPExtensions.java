package com.xresch.emp.features.httpextensions;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFWApplicationExecutor;
import com.xresch.cfw.features.usermgmt.FeatureUserManagement;
import com.xresch.cfw.features.usermgmt.Permission;
import com.xresch.cfw.spi.CFWAppFeature;

public class FeatureHTTPExtensions extends CFWAppFeature {

    // Fields
    public static final String PACKAGE_RESOURCES = "com.xresch.emp.features.httpextensions.resources";

	public static final String PERMISSION_HTTP_EXTENSIONS = "HTTP Extensions";
	public static final String WIDGET_PREFIX = "emp_httpextensions";
	
	/************************************************************************************
	 * Override to make it managed and return something else then null.
	 ************************************************************************************/
	@Override
	public String getNameForFeatureManagement() {
		return "HTTP Extensions";
	};
	
	/************************************************************************************
	 * Register a description for the feature management.
	 ************************************************************************************/
	@Override
	public String getDescriptionForFeatureManagement() {
		return "Extensions to work with HTTP Requests.(Widgets, Source ...)";
	};
	
	/************************************************************************************
	 * Return if the feature is active by default or if the admin has to enable it.
	 ************************************************************************************/
	public boolean activeByDefault() {
		return true;
	};
	
	/************************************************************************************
	 *
	 ************************************************************************************/
    @Override
    public void register() {

        // Register packages
        CFW.Files.addAllowedPackage(PACKAGE_RESOURCES);

        // Register Widget
        CFW.Registry.Widgets.add(new WidgetHTTPResponseCheck());
    }

	/************************************************************************************
	 *
	 ************************************************************************************/
    @Override
    public void initializeDB() {
    	//----------------------------------
    			// Permissions
    			CFW.DB.Permissions.oneTimeCreate(
    					new Permission(PERMISSION_HTTP_EXTENSIONS, FeatureUserManagement.CATEGORY_USER)
    						.description("Use the HTTP Extensions(Widgets, Sources, JobTasks, etc...)."),
    					true,
    					true);
    }
    
	/************************************************************************************
	 *
	 ************************************************************************************/
    @Override
    public void addFeature(CFWApplicationExecutor cfwApplicationExecutor) {

    }

	/************************************************************************************
	 *
	 ************************************************************************************/
    @Override
    public void startTasks() {

    }

	/************************************************************************************
	 *
	 ************************************************************************************/
    @Override
    public void stopFeature() {

    }
}
