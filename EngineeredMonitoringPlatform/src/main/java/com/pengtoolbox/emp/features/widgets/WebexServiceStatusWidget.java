package com.pengtoolbox.emp.features.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw._main.CFWHttp.CFWHttpResponse;
import com.pengtoolbox.cfw.caching.FileDefinition;
import com.pengtoolbox.cfw.caching.FileDefinition.HandlingType;
import com.pengtoolbox.cfw.datahandling.CFWField;
import com.pengtoolbox.cfw.datahandling.CFWField.FormFieldType;
import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.features.dashboard.WidgetDefinition;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.cfw.response.JSONResponse;
import com.pengtoolbox.cfw.response.bootstrap.AlertMessage.MessageType;

public class WebexServiceStatusWidget extends WidgetDefinition {

	private static Logger logger = CFWLog.getLogger(WebexServiceStatusWidget.class.getName());
	@Override
	public String getWidgetType() {return "emp_webexservicestatus";}

	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				.addField(CFWField.newString(FormFieldType.TEXT, "url")
						.setLabel("{!cfw_widget_webexservicestatus_url!}")
						.setDescription("{!cfw_widget_webexservicestatus_url_desc!}")
						.setValue("")			
				)
				
//				.addField(CFWField.newString(FormFieldType.TEXTAREA, "service_filter")
//						.setLabel("{!cfw_widget_webexservicestatus_servicefilter!}")
//						.setDescription("{!cfw_widget_webexservicestatus_servicefilter_desc!}")
//						.setValue("")
//						
//				)
				.addField(CFWField.newString(FormFieldType.SELECT, "renderer")
						.setLabel("{!cfw_widget_displayas!}")
						.setDescription("{!cfw_widget_displayas_desc!}")
						.setOptions(new String[]{"Tiles", "Panels", "Table"})
						.setValue("Tiles")
				)								
				.addField(CFWField.newString(FormFieldType.SELECT, "sizefactor")
						.setLabel("{!cfw_widget_sizefactor!}")
						.setDescription("{!cfw_widget_sizefactor_desc!}")
						.setOptions(new String[]{"0.25", "0.5", "0.75", "1", "1.25", "1.5", "1.75", "2.0", "2.5", "3.0"})
						.setValue("0.5")
				)
				
				.addField(CFWField.newString(FormFieldType.SELECT, "borderstyle")
						.setLabel("{!cfw_widget_borderstyle!}")
						.setDescription("{!cfw_widget_borderstyle_desc!}")
						.setOptions(new String[]{"None", "Round", "Superround", "Asymmetric", "Superasymmetric", "Ellipsis"})
						.setValue("None")
				)
				
				.addField(CFWField.newBoolean(FormFieldType.BOOLEAN, "showlabels")
						.setLabel("{!cfw_widget_showlabels!}")
						.setDescription("{!cfw_widget_showlabels_desc!}")
						.setValue(false)
				)
				
				.addField(CFWField.newBoolean(FormFieldType.BOOLEAN, "disable")
						.setLabel("{!cfw_widget_disable!}")
						.setDescription("{!cfw_widget_disable_desc!}")
						.setValue(false)
				)
				
				.addField(CFWField.newBoolean(FormFieldType.BOOLEAN, "sampledata")
						.setLabel("{!cfw_widget_sampledata!}")
						.setDescription("{!cfw_widget_sampledata_desc!}")
						.setValue(false)
				)
	
		;
	}
		
	@Override
	public void fetchData(JSONResponse response, JsonObject settings) { 
		//---------------------------------
		// Example Data
		JsonElement sampleDataElement = settings.get("sampledata");
		
		if(sampleDataElement != null 
		&& !sampleDataElement.isJsonNull() 
		&& sampleDataElement.getAsBoolean()) {
			createSampleData(response);
			return;
		}
		 
		//---------------------------------
		// Resolve URL
		JsonElement urlElement = settings.get("url");
		String url = null;
		if(!urlElement.isJsonNull() && !urlElement.getAsString().isEmpty()) {
			url = urlElement.getAsString();
		}else {
			return;
		}
			
		//---------------------------------
		// Fetch Data
		CFWHttpResponse httpResponse = CFW.HTTP.sendGETRequest(url);
		if(httpResponse != null) {
			if(httpResponse.getStatus() <= 299) {
				String jsonResult = httpResponse.getResponseBody();
				response.getContent().append(jsonResult);
				response.setSuccess(true);
			}else {
				response.setSuccess(false);
				CFW.Context.Request.addAlertMessage(MessageType.WARNING, "The Webex Rest Service returned the HTTP Status "+httpResponse.getStatus());
			}
		}
		
		
	}
	
	public void createSampleData(JSONResponse response) { 
		
		response.getContent().append(CFW.Files.readPackageResource(FeatureEMPWidgets.RESOURCE_PACKAGE, "emp_widget_webexservicestatus_sample.json"));

	}
	
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureEMPWidgets.RESOURCE_PACKAGE, "emp_widget_webexservicestatus.js");
		array.add(js);
		return array;
	}

	@Override
	public ArrayList<FileDefinition> getCSSFiles() {
		return null;
	}

	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureEMPWidgets.RESOURCE_PACKAGE, "lang_en_emp_widgets.properties"));
		return map;
	}

}
