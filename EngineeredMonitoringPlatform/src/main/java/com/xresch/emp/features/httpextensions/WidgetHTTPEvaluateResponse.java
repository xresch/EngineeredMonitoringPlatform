package com.xresch.emp.features.httpextensions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.CFWFieldFlag;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.dashboard.WidgetDataCache;
import com.xresch.cfw.features.dashboard.WidgetDefinition;
import com.xresch.cfw.features.dashboard.WidgetSettingsFactory;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.utils.CFWHttp.CFWHttpRequestBuilder;
import com.xresch.cfw.utils.CFWHttp.CFWHttpResponse;
import com.xresch.cfw.validation.NumberRangeValidator;

/**************************************************************************************************************
 * 
 * @author Joel Laeubin (Base implementation)
 * @author Reto Scheiwiller (integration into EMP, ehancements etc...)
 * 
 * (c) Copyright 2022 
 * 
 * @license MIT-License
 **************************************************************************************************************/
public class WidgetHTTPEvaluateResponse extends WidgetDefinition {

	private static Logger logger = CFWLog.getLogger(WidgetHTTPEvaluateResponse.class.getName());
	
	private static final String PARAM_METHOD = "METHOD";
	private static final String PARAM_URLS = "URLS";
	private static final String PARAM_LABELS = "LABELS";
	private static final String PARAM_HEADERS = "JSON_HEADERS";
	private static final String PARAM_USERNAME = "USERNAME";
	private static final String PARAM_PASSWORD = "PASSWORD";
	
	private static final String PARAM_STATUS_CODE = "STATUS_CODE";
	private static final String PARAM_CHECK_FOR = "CHECK_FOR";
	private static final String PARAM_CHECK_TYPE = "CHECK_TYPE";
	
	private static final String PARAM_DEBUG_MODE = "DEBUG_MODE";



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

				.addField(CFWField.newString(CFWField.FormFieldType.SELECT, PARAM_METHOD)
						.setLabel("{!emp_widget_httpextensions_method!}")
						.setDescription("{!emp_widget_httpextensions_method_desc!}")
						.addOption("GET")
						.addOption("POST")
						.setValue("GET")
						.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
					)
				
				.addField(CFWField.newString(CFWField.FormFieldType.TEXTAREA, PARAM_URLS)
						.setLabel("{!emp_widget_httpextensions_urls!}")
						.setDescription("{!emp_widget_httpextensions_urls_desc!}")
						.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
						.setValue("")
					)
				
				.addField(CFWField.newString(CFWField.FormFieldType.TEXTAREA, PARAM_LABELS)
						.setLabel("{!emp_widget_httpextensions_labels!}")
						.setDescription("{!emp_widget_httpextensions_labels_desc!}")
						.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
						.setValue("")
						)
				
				.addField(CFWField.newValueLabel(PARAM_HEADERS)
						.setLabel("{!emp_widget_httpextensions_headers!}")
						.setDescription("{!emp_widget_httpextensions_headers_desc!}")
						.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
						)

				.addField(CFWField.newString(CFWField.FormFieldType.TEXT, PARAM_USERNAME)
						.setLabel("{!emp_widget_httpextensions_username!}")
						.setDescription("{!emp_widget_httpextensions_username_desc!}")
						.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
						.setValue(null)
					)
				
				.addField(CFWField.newString(CFWField.FormFieldType.PASSWORD, PARAM_PASSWORD)
						.setLabel("{!emp_widget_httpextensions_password!}")
						.setDescription("{!emp_widget_httpextensions_password_desc!}")
						// DO NOT TOUCH! Changing salt will corrupt all password stored in the database
						.enableEncryption("emp_httpextensions_encryptionSalt-fFDSgasTR1")
						.disableSanitization()
						.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
						.setValue(null)
					)
				
				// Labels for the URL ?
				.addField(CFWField.newString(CFWField.FormFieldType.SELECT, PARAM_CHECK_TYPE)
						.setLabel("{!emp_widget_httpextensions_checktype!}")
						.setDescription("{!emp_widget_httpextensions_checktype_desc!}")
						.setOptions(checkTypeOptions)
						.setValue("Contains")
						.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
					)

				.addField(CFWField.newString(CFWField.FormFieldType.TEXTAREA, PARAM_CHECK_FOR)
						.setLabel("{!emp_widget_httpextensions_matchfor!}")
						.setDescription("{!emp_widget_httpextensions_matchfor_desc!}")
						.setValue("")
						.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
					)

				.addField(CFWField.newInteger(CFWField.FormFieldType.NUMBER, PARAM_STATUS_CODE)
						.setLabel("{!emp_widget_httpextensions_statuscode!}")
						.setDescription("{!emp_widget_httpextensions_statuscode_desc!}")
						.addValidator(new NumberRangeValidator(0, 999))
						.setValue(200)
						.addFlag(CFWFieldFlag.SERVER_SIDE_ONLY)
					)
						
				.addField(WidgetSettingsFactory.createDefaultDisplayAsField())
				.addAllFields(WidgetSettingsFactory.createTilesSettingsFields())
				
				.addField(CFWField.newBoolean(CFWField.FormFieldType.BOOLEAN, PARAM_DEBUG_MODE)
						.setLabel("{!emp_widget_httpextensions_debugmode!}")
						.setDescription("{!emp_widget_httpextensions_debugmode_desc!}")
						.setValue(false)
					)
				;

	}

	@SuppressWarnings("unchecked")
	@Override
	public void fetchData(HttpServletRequest httpServletRequest, JSONResponse jsonResponse, CFWObject cfwObject, JsonObject jsonObject, long l, long l1) {

		//------------------------------------
		// Get Parameters
		String method = (String) cfwObject.getField(PARAM_METHOD).getValue();
		if(Strings.isNullOrEmpty(method)){
			method = "GET";
		}
		
		String urls = (String) cfwObject.getField(PARAM_URLS).getValue();
		if (Strings.isNullOrEmpty(urls)) {
			return;
		}
		String[] splittedURLs = urls.trim().split("\\r\\n|\\n");
		
		
		String labels = (String) cfwObject.getField(PARAM_LABELS).getValue();
		String[] splittedLabels = new String[] {};
		if (!Strings.isNullOrEmpty(labels)) {
			splittedLabels = labels.trim().split("\\r\\n|\\n");
		}
		
		String username = (String) cfwObject.getField(PARAM_USERNAME).getValue();
		String password = (String) cfwObject.getField(PARAM_PASSWORD).getValue();
		
		LinkedHashMap<String, String> headers = (LinkedHashMap<String, String>) cfwObject.getField(PARAM_HEADERS).getValue();
		
		String checkType = (String) cfwObject.getField(PARAM_CHECK_TYPE).getValue();
		String checkFor = (String) cfwObject.getField(PARAM_CHECK_FOR).getValue();

		Integer expectedResponseCode = (Integer) cfwObject.getField(PARAM_STATUS_CODE).getValue();
		
		boolean debugMode = (Boolean) cfwObject.getField(PARAM_DEBUG_MODE).getValue();
		
		//------------------------------------
		// Iterate URLs and Build Response
		JsonArray jsonArray = new JsonArray();

		int index = 0;
		for (String splittedURL : splittedURLs) {

			//----------------------------------------
			// Check URL
			if(!splittedURL.contains("://") 
			&& !splittedURL.startsWith("http") ) {
				splittedURL = "https://"+splittedURL;
			}
			//----------------------------------------
			// Build Request and Call URL

			CFWHttpRequestBuilder requestBuilder = CFW.HTTP.newRequestBuilder(splittedURL);
			
			if(method.trim().toUpperCase().equals("POST")) {
				requestBuilder.POST();
			}else {
				requestBuilder.GET();
			}
			
			if(!Strings.isNullOrEmpty(username)) {
				requestBuilder.authenticationBasic(username, password);
			}
			
			requestBuilder.headers(headers);
			

			CFWHttpResponse response = requestBuilder.send();

			if(response.errorOccured()) {
				CFW.Messages.addInfoMessage("Hint: Check if your URLs include the right protocol(http/https).");
				CFW.Messages.addInfoMessage("Another Hint: The application server might not have access to the URL. Check with the application support.");
				return;
			}
			
			
			//------------------------------------
			// Handle Debug Mode
			if(debugMode) {
				JsonObject debugObject = new JsonObject();
								
				debugObject.addProperty("URL", requestBuilder.buildURLwithParams());
				debugObject.addProperty("RESPONSE_STATUS", response.getStatus());
				debugObject.add("RESPONSE_HEADERS", response.getHeadersAsJson());
				debugObject.addProperty("RESPONSE_BODY", response.getResponseBody());
				
				
				jsonArray.add(debugObject);
				jsonResponse.setPayLoad(jsonArray);
				return;
			}
			

			//----------------------------------------
			// Create Data Object
			JsonObject returnObject = new JsonObject();

			if(index < splittedLabels.length 
			&& !Strings.isNullOrEmpty(splittedLabels[index])) {
				returnObject.addProperty("LABEL", splittedLabels[index]);
			}else {
				
				try {
					returnObject.addProperty("LABEL", new URL(splittedURL).getHost());
				} catch (MalformedURLException e) {
					returnObject.addProperty("LABEL", splittedURL);
				}
			}
			
			returnObject.addProperty("URL", splittedURL);
			returnObject.addProperty(PARAM_CHECK_TYPE, checkType);
			returnObject.addProperty(PARAM_CHECK_FOR, checkFor);

			//----------------------------------------
			// Check response content
			boolean result = false;
			if(Strings.isNullOrEmpty(checkFor)) {
				result = true;
			}else {
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
			}
			
			returnObject.addProperty("CHECK_RESULT", result);

			//----------------------------------------
			// Check response code
			int actualResponseStatusCode = response.getStatus();
			
			returnObject.addProperty(PARAM_STATUS_CODE, actualResponseStatusCode);
			
			if(expectedResponseCode != null) {
				boolean isValid =  (expectedResponseCode == actualResponseStatusCode);
				returnObject.addProperty("STATUS_CODE_VALID", isValid);
				if(!isValid) {
					returnObject.addProperty("STATUS_CODE_MESSAGE", "HTTP Status Code '"+expectedResponseCode
							+"' was expected but '"+actualResponseStatusCode+"' was received.");
				}
				
			}
			
			//----------------------------------------
			// Add Object to the Result Array
			jsonArray.add(returnObject);
			index++;

		}

		// Add jsonArray to the payload
		jsonResponse.setPayLoad(jsonArray);

	}

	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> fileDefinitions = new ArrayList<>();
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE,
				FeatureHTTPExtensions.PACKAGE_RESOURCES, "widget_evaluateresponse.js");
		fileDefinitions.add(js);
		return fileDefinitions;
	}

	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureHTTPExtensions.PACKAGE_RESOURCES, "lang_en_httpextensions.properties"));
		map.put(Locale.GERMAN, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureHTTPExtensions.PACKAGE_RESOURCES, "lang_de_httpextensions.properties"));
		return map;
	}
}
