package com.xresch.emp.features.httpextensions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.dashboard.WidgetDataCache;
import com.xresch.cfw.features.dashboard.WidgetDefinition;
import com.xresch.cfw.features.dashboard.WidgetSettingsFactory;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.utils.CFWHttp;
import com.xresch.cfw.validation.NumberRangeValidator;

public class WidgetHTTPEvaluateResponse extends WidgetDefinition {


	private enum CheckType {
		CONTAINS
		, DOES_NOT_CONTAIN
		, STARTS_WITH
		, ENDS_WITH
		, EQUALS
		, NOT_EQUALS
		, MATCH_REGEX
		, DO_NOT_MATCH_REGEX
	}
	
	LinkedHashMap<String, String> checkTypeOptions = new LinkedHashMap<>();
	
	{
		checkTypeOptions.put(CheckType.CONTAINS.toString(), "Contains");
		checkTypeOptions.put(CheckType.DOES_NOT_CONTAIN.toString(), "Does Not Contain");
		checkTypeOptions.put(CheckType.STARTS_WITH.toString(), "Starts With");
		checkTypeOptions.put(CheckType.ENDS_WITH.toString(), "Ends With");
		checkTypeOptions.put(CheckType.EQUALS.toString(), "Equals");
		checkTypeOptions.put(CheckType.NOT_EQUALS.toString(), "Not Equals");
		checkTypeOptions.put(CheckType.MATCH_REGEX.toString(), "Match Regex");
		checkTypeOptions.put(CheckType.DO_NOT_MATCH_REGEX.toString(), "Does Not Match Regex");
	}
	
	// Returns the unique name of the widget. Has to be the same unique name as used in the javascript part.
	@Override
	public String getWidgetType() {
		return FeatureHTTPExtensions.WIDGET_PREFIX+"_evaluateresponse";
	}
	
	@Override
	public WidgetDataCache.WidgetDataCachePolicy getCachePolicy() {
		return WidgetDataCache.WidgetDataCachePolicy.ALWAYS;
	}

	// Creates an object with fields that will be used as the settings for this particular widget.
	@Override
	public CFWObject getSettings() {
		return new CFWObject()

				// URL textarea which contains the URL to check the response for
				.addField(CFWField.newString(CFWField.FormFieldType.TEXTAREA, "URL")
						.setLabel("{!emp_widget_evaluateresponse_url_label!}")
						.setDescription("{!emp_widget_evaluateresponse_url_desc!}")
						.setValue("")
					)

				// Labels for the URL ?

				.addField(CFWField.newString(CFWField.FormFieldType.SELECT, "CHECK_TYPE")
						.setLabel("{!emp_widget_evaluateresponse_checktype_label!}")
						.setDescription("{!emp_widget_evaluateresponse_checktype_desc!}")
						.setOptions(checkTypeOptions)
						.setValue("Contains")
					)

				.addField(CFWField.newString(CFWField.FormFieldType.TEXTAREA, "CHECK_FOR")
						.setLabel("{!emp_widget_evaluateresponse_matchfor_label!}")
						.setDescription("{!emp_widget_evaluateresponse_matchfor_desc!}")
						.setValue("")
					)

				// Textarea that contains the statuscode of the request
				.addField(CFWField.newInteger(CFWField.FormFieldType.NUMBER, "STATUS_CODE")
						.setLabel("{!emp_widget_evaluateresponse_statuscode_label!}")
						.setDescription("{!emp_widget_evaluateresponse_statuscode_desc!}")
						.addValidator(new NumberRangeValidator(0, 999))
						.setValue(200)
					)
						
				.addField(WidgetSettingsFactory.createDefaultDisplayAsField())
				.addAllFields(WidgetSettingsFactory.createTilesSettingsFields())
				;

	}

	@Override
	public void fetchData(HttpServletRequest httpServletRequest, JSONResponse jsonResponse, CFWObject cfwObject, JsonObject jsonObject, long l, long l1) {

		//------------------------------------
		// Get Parameters
		String url = (String) cfwObject.getField("URL").getValue();
		if (Strings.isNullOrEmpty(url)) {
			return;
		}

		String[] splittedURLs = url.trim().split("\\r\\n|\\n");
		String checkType = (String) cfwObject.getField("CHECK_TYPE").getValue();
		String checkFor = (String) cfwObject.getField("CHECK_FOR").getValue();
		
		Integer expectedResponseCode = (Integer) cfwObject.getField("STATUS_CODE").getValue();


		//------------------------------------
		// Iterate URLs and Build Response
		JsonArray jsonArray = new JsonArray();

		for (String splittedURL : splittedURLs) {

			//----------------------------------------
			// Call URL
			CFWHttp.CFWHttpResponse response = CFW.HTTP.sendGETRequest(splittedURL);

//			CFWHttp.CFWHttpResponse response =
//					CFW.HTTP.newRequestBuilder(splittedURL)
//							.GET()
//							.header("headerName", "value")
//							.authenticationBasic(username, password)
//					;

			//----------------------------------------
			// Create Data Object
			JsonObject returnObject = new JsonObject();

			returnObject.addProperty("URL", splittedURL);
			returnObject.addProperty("CHECK_TYPE", checkType);
			returnObject.addProperty("CHECK_FOR", checkFor);

			
			//----------------------------------------
			// Check response content
			boolean result = false;
			switch (CheckType.valueOf(checkType) ) {

				case STARTS_WITH:
					result = response.getResponseBody().startsWith(checkFor);
					break;

				case ENDS_WITH:
					result = response.getResponseBody().endsWith(checkFor);
					break;

				case CONTAINS:
					result = response.getResponseBody().contains(checkFor);
					break;
						
				case DOES_NOT_CONTAIN:
					result = !response.getResponseBody().contains(checkFor);
					break;

				case EQUALS:
					result = response.getResponseBody().equals(checkFor);
					break;

				case NOT_EQUALS:
					result = !response.getResponseBody().equals(checkFor);
					break;

				case MATCH_REGEX:
					Pattern pattern = Pattern.compile(checkFor, Pattern.MULTILINE | Pattern.DOTALL);
					Matcher matcher = pattern.matcher(response.getResponseBody());
					
					result = matcher.find();
					break;
					
				case DO_NOT_MATCH_REGEX:
					Pattern pattern2 = Pattern.compile(checkFor, Pattern.MULTILINE | Pattern.DOTALL);
					Matcher matcher2 = pattern2.matcher(response.getResponseBody());
					
					result = !matcher2.find();
					break;
					
		   
			}

			returnObject.addProperty("CHECK_RESULT", result);
			// Add object to the returnArray
			jsonArray.add(returnObject);
			
			//----------------------------------------
			// Check response code
			int actualResponseStatusCode = response.getStatus();
			
			returnObject.addProperty("STATUS_CODE", actualResponseStatusCode);
			
			if(expectedResponseCode != null) {
				boolean isValid =  (expectedResponseCode == actualResponseStatusCode);
				returnObject.addProperty("STATUS_CODE_VALID", isValid);
				if(!isValid) {
					returnObject.addProperty("STATUS_CODE_MESSAGE", "HTTP Status Code '"+expectedResponseCode
							+"' was expected but '"+actualResponseStatusCode+"' was received.");
				}
				
			}

		}

		// Add jsonArray to the payload - this will be the data.payload in JavaScript
		jsonResponse.setPayLoad(jsonArray);

	}

	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> fileDefinitions = new ArrayList<>();
		FileDefinition js = new FileDefinition(FileDefinition.HandlingType.JAR_RESOURCE,
				FeatureHTTPExtensions.PACKAGE_RESOURCES, "widget_responsechecker.js");
		fileDefinitions.add(js);
		return fileDefinitions;
	}

	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		map.put(Locale.ENGLISH, new FileDefinition(FileDefinition.HandlingType.JAR_RESOURCE, FeatureHTTPExtensions.PACKAGE_RESOURCES, "lang_en_widget_responsechecker.properties"));
		map.put(Locale.GERMAN, new FileDefinition(FileDefinition.HandlingType.JAR_RESOURCE, FeatureHTTPExtensions.PACKAGE_RESOURCES, "lang_de_widget_responsechecker.properties"));
		return map;
	}
}