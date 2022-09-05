package com.xresch.emp.features.httpextensions;

import java.util.ArrayList;
import java.util.HashMap;
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

public class WidgetHTTPResponseCheck extends WidgetDefinition {


	
    // Returns the unique name of the widget. Has to be the same unique name as used in the javascript part.
    @Override
    public String getWidgetType() {
        return "emp_web_responsechecker";
    }

    // Creates an object with fields that will be used as the settings for this particular widget.
    @Override
    public CFWObject getSettings() {
        return new CFWObject()

                // URL textarea which contains the URL to check the response for
                .addField(CFWField.newString(CFWField.FormFieldType.TEXTAREA, "URL")
                        .setLabel("{!cfw_widget_responseChecker_url_label!}")
                        .setDescription("{!cfw_widget_responseChecker_url_desc!}")
                        .setValue(""))

                // Labels for the URL ?

                // Check type radiobuttons
                .addField(CFWField.newString(CFWField.FormFieldType.SELECT, "CheckType")
                        .setLabel("{!cfw_widget_responseChecker_checktype_label!}")
                        .setDescription("{!cfw_widget_responseChecker_checktype_desc!}")
                        .setOptions(new String[]{"Starts with", "Ends With", "Contains", "Doesn't contain", "Equals", "Isn't equal to", "Regex"})
                        .setValue("Contains"))

                // Textarea that contains the String that we match for
                .addField(CFWField.newString(CFWField.FormFieldType.TEXTAREA, "Match_For")
                        .setLabel("{!cfw_widget_responseChecker_matchfor_label!}")
                        .setDescription("{!cfw_widget_responseChecker_matchfor_desc!}")
                        .setValue("Nebuchadnezzar"))

                // Textarea that contains the statuscode of the request
                .addField(CFWField.newString(CFWField.FormFieldType.TEXT, "ResponseStatusCode")
                        .setLabel("{!cfw_widget_responseChecker_statuscode_label!}")
                        .setDescription("{!cfw_widget_responseChecker_statuscode_desc!}"))

                .addField(WidgetSettingsFactory.createDefaultDisplayAsField())
                .addAllFields(WidgetSettingsFactory.createTilesSettingsFields())
                ;

    }

    @Override
    public WidgetDataCache.WidgetDataCachePolicy getCachePolicy() {
        return WidgetDataCache.WidgetDataCachePolicy.ALWAYS;
    }

    @Override
    public void fetchData(HttpServletRequest httpServletRequest, JSONResponse jsonResponse, CFWObject cfwObject, JsonObject jsonObject, long l, long l1) {

        // Get match string
        String url = (String) cfwObject.getField("URL").getValue();
        String[] splittedURLs = url.trim().split("\\r\\n|\\n");
        String matchString = (String) cfwObject.getField("Match_For").getValue();
        String matchType = (String) cfwObject.getField("CheckType").getValue();

        // If URL is empty, return
        if (Strings.isNullOrEmpty(url)) {
            return;
        }



        // Build new JsonArray that holds JsonObjects which are returned to the JavaScript
        JsonArray jsonArray = new JsonArray();

        for (String splittedURL : splittedURLs) {

            // Otherwise, send request
            CFWHttp.CFWHttpResponse response = CFW.HTTP.sendGETRequest(splittedURL);

//            CFWHttp.CFWHttpResponse response =
//                    CFW.HTTP.newRequestBuilder(splittedURL)
//                            .GET()
//                            .header("headerName", "value")
//                            .authenticationBasic(username, password)
//                    ;

            // Fill jsonArray with data
            JsonObject returnObject = new JsonObject();

            // Parse response status and add to returnObject
            int responseStatus = response.getStatus();
            returnObject.addProperty("responseStatus", responseStatus);
            returnObject.addProperty("URL", splittedURL);
            returnObject.addProperty("matchType", matchType);
            returnObject.addProperty("matchString", matchString);

            boolean result = false;
            switch (matchType) {

                case "Starts with":
                    result = response.getResponseBody().startsWith(matchString);
                    break;

                case "Ends with":
                    result = response.getResponseBody().endsWith(matchString);
                    break;

                case "Contains":
                    result = response.getResponseBody().contains(matchString);
                    break;

                case "Doesn't contain":
                    result = !response.getResponseBody().contains(matchString);
                    break;

                case "Equals":
                    result = response.getResponseBody().equals(matchString);
                    break;

                case "Isn't equal to":
                    result = !response.getResponseBody().equals(matchString);
                    break;

                case "Regex":
                    Pattern pattern = Pattern.compile(matchString, Pattern.MULTILINE | Pattern.DOTALL);
                    Matcher matcher = pattern.matcher(response.getResponseBody());

                    result = matcher.find();
                    break;

            }

            returnObject.addProperty("success", result);
            // Add object to the returnArray
            jsonArray.add(returnObject);

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
