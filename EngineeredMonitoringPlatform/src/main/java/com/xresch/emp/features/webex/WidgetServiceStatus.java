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
import com.xresch.cfw.features.dashboard.WidgetSettingsFactory;
import com.xresch.cfw.features.dashboard.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.utils.CFWHttp.CFWHttpResponse;
import com.xresch.emp.features.common.FeatureEMPCommon;
import com.xresch.emp.features.spm.FeatureSPM;

public class WidgetServiceStatus extends WidgetDefinition {

	private static Logger logger = CFWLog.getLogger(WidgetServiceStatus.class.getName());
	@Override
	public String getWidgetType() {return "emp_webexservicestatus";}
	
	@Override
	public WidgetDataCachePolicy getCachePolicy() {
		return WidgetDataCachePolicy.ALWAYS;
	}	
	
	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				.addField(CFWField.newString(FormFieldType.TEXT, "url")
						.setLabel("{!emp_widget_webexservicestatus_url!}")
						.setDescription("{!emp_widget_webexservicestatus_url_desc!}")
						.allowHTML(true)
						.setValue("")			
				)
				
//				.addField(CFWField.newString(FormFieldType.TEXTAREA, "service_filter")
//						.setLabel("{!emp_widget_webexservicestatus_servicefilter!}")
//						.setDescription("{!emp_widget_webexservicestatus_servicefilter_desc!}")
//						.setValue("")
//						
//				)
				
				.addField(WidgetSettingsFactory.createDefaultDisplayAsField())				
				.addAllFields(WidgetSettingsFactory.createTilesSettingsFields())
				.addField(WidgetSettingsFactory.createDisableBoolean())
				.addField(WidgetSettingsFactory.createSampleDataField())
	
		;
	}
		
	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings, long earliest, long latest, int timezoneOffsetMinutes) { 
		//---------------------------------
		// Example Data
		JsonElement sampleDataElement = jsonSettings.get("sampledata");
		
		if(sampleDataElement != null 
		&& !sampleDataElement.isJsonNull() 
		&& sampleDataElement.getAsBoolean()) {
			createSampleData(response);
			return;
		}
		 
		//---------------------------------
		// Resolve URL
		JsonElement urlElement = jsonSettings.get("url");
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
		
		response.getContent().append(CFW.Files.readPackageResource(FeatureWebex.PACKAGE_RESOURCE, "emp_widget_webexservicestatus_sample.json"));

	}
	
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureWebex.PACKAGE_RESOURCE, "emp_widget_webexservicestatus.js");
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
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureWebex.PACKAGE_RESOURCE, "lang_en_emp_webex.properties"));
		return map;
	}
	
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureWebex.PERMISSION_WIDGETS_WEBEX);
	}

}
