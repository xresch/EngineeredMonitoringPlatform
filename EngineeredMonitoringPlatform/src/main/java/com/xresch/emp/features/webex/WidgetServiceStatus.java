package com.xresch.emp.features.webex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.dashboard.WidgetDefinition;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.utils.CFWHttp.CFWHttpResponse;
import com.xresch.emp.features.common.FeatureEMPCommon;

public class WidgetServiceStatus extends WidgetDefinition {

	private static Logger logger = CFWLog.getLogger(WidgetServiceStatus.class.getName());
	@Override
	public String getWidgetType() {return "emp_webexservicestatus";}

	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				.addField(CFWField.newString(FormFieldType.TEXT, "url")
						.setLabel("{!cfw_widget_webexservicestatus_url!}")
						.setDescription("{!cfw_widget_webexservicestatus_url_desc!}")
						.allowHTML(true)
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
						.setLabel("{!cfw_widget_tilessizefactor!}")
						.setDescription("{!cfw_widget_tilessizefactor_desc!}")
						.setOptions(new String[]{"0.25", "0.5", "0.75", "1", "1.25", "1.5", "1.75", "2.0", "2.5", "3.0"})
						.setValue("0.5")
				)
				
				.addField(CFWField.newString(FormFieldType.SELECT, "borderstyle")
						.setLabel("{!cfw_widget_tilesborderstyle!}")
						.setDescription("{!cfw_widget_tilesborderstyle_desc!}")
						.setOptions(new String[]{"None", "Round", "Superround", "Asymmetric", "Superasymmetric", "Ellipsis"})
						.setValue("None")
				)
				
				.addField(CFWField.newBoolean(FormFieldType.BOOLEAN, "showlabels")
						.setLabel("{!cfw_widget_tilesshowlabels!}")
						.setDescription("{!cfw_widget_tilesshowlabels_desc!}")
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
	public void fetchData(HttpServletRequest request, JSONResponse response, JsonObject settings) { 
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
		
		response.getContent().append(CFW.Files.readPackageResource(FeatureWebex.RESOURCE_PACKAGE, "emp_widget_webexservicestatus_sample.json"));

	}
	
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureWebex.RESOURCE_PACKAGE, "emp_widget_webexservicestatus.js");
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
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureEMPCommon.PACKAGE_RESOURCE, "lang_en_emp_widgets.properties"));
		return map;
	}

}
