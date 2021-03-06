package com.xresch.emp.features.spm;

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
import com.xresch.cfw.response.JSONResponse;
import com.xresch.emp.features.common.FeatureEMPCommon;

public class WidgetMeasureLegend extends WidgetDefinition {

	@Override
	public String getWidgetType() {return "emp_spmmeasurelegend";}

	@Override
	public CFWObject getSettings() {
		return new CFWObject();
	}

	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, JsonObject settings) { /* do nothing */ }

	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureSPM.PACKAGE_RESOURCE, "emp_widget_spmmeasurelegend.js");
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add(js);
		return array;
	}

	@Override
	public ArrayList<FileDefinition> getCSSFiles() { 
		
		FileDefinition css = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureEMPCommon.PACKAGE_RESOURCE, "emp_widgets.css");
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add(css);
		return array; 
		
	}

	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		return map;
	}
	
	@Override
	public boolean hasPermission() {
		return CFW.Context.Request.hasPermission(FeatureSPM.PERMISSION_WIDGETS_SPM);
	}

}
