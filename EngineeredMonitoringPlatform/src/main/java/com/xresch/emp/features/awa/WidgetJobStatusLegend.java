package com.xresch.emp.features.awa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.dashboard.widgets.WidgetDefinition;
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.emp.features.common.FeatureEMPCommon;

public class WidgetJobStatusLegend extends WidgetDefinition {
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String getWidgetType() {return "emp_awajobstatus_legend";}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public WidgetDataCachePolicy getCachePolicy() {
		return WidgetDataCachePolicy.OFF;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String widgetCategory() {
		return FeatureAWA.WIDGET_CATEGORY_AWA;
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String widgetName() { return "Job Status Legend"; }
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureAWA.PACKAGE_MANUAL, "widget_"+getWidgetType()+".html");
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public CFWObject getSettings() {
		return new CFWObject();
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings, CFWTimeframe timeframe) { /* do nothing */ }
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureAWA.PACKAGE_RESOURCE, "emp_widget_awajobstatus_legend.js");
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add(js);
		return array;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public ArrayList<FileDefinition> getCSSFiles() { 
		
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		return array; 
		
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		return map;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureAWA.PERMISSION_WIDGETS_AWA);
	}
}
