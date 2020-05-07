package com.pengtoolbox.emp.features.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import com.google.gson.JsonObject;
import com.pengtoolbox.cfw.caching.FileDefinition;
import com.pengtoolbox.cfw.caching.FileDefinition.HandlingType;
import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.features.dashboard.WidgetDefinition;
import com.pengtoolbox.cfw.response.JSONResponse;

public class WebexServiceStatusLegendWidget extends WidgetDefinition {

	@Override
	public String getWidgetType() {return "emp_webexservicestatus_legend";}

	@Override
	public CFWObject getSettings() {
		return new CFWObject();
	}

	@Override
	public void fetchData(JSONResponse response, JsonObject settings) { }

	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureEMPWidgets.RESOURCE_PACKAGE, "emp_widget_webexservicestatus_legend.js");
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add(js);
		return array;
	}

	@Override
	public ArrayList<FileDefinition> getCSSFiles() { 
		
		FileDefinition css = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureEMPWidgets.RESOURCE_PACKAGE, "emp_widgets.css");
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add(css);
		return array; 
		
	}

	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		return map;
	}

}
