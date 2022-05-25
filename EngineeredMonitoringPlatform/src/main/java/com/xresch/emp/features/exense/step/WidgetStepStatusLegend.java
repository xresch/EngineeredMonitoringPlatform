package com.xresch.emp.features.exense.step;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.dashboard.WidgetDefinition;
import com.xresch.cfw.features.dashboard.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.emp.features.common.FeatureEMPCommon;

public class WidgetStepStatusLegend extends WidgetDefinition {

	@Override
	public String getWidgetType() {return FeatureExenseStep.WIDGET_PREFIX+"_statuslegend";}
	
	@Override
	public WidgetDataCachePolicy getCachePolicy() {
		return WidgetDataCachePolicy.OFF;
	}
	
	@Override
	public CFWObject getSettings() {
		return new CFWObject();
	}

	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings, long earliest, long latest) { /* do nothing */ }

	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureExenseStep.PACKAGE_RESOURCE,  "emp_widget_step_common.js") );
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureExenseStep.PACKAGE_RESOURCE,  "emp_widget_step_statuslegend.js") );
		return array;
	}

	@Override
	public ArrayList<FileDefinition> getCSSFiles() { 
		
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		return array; 
		
	}

	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		return map;
	}
	
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureExenseStep.PERMISSION_STEP);
	}

}
