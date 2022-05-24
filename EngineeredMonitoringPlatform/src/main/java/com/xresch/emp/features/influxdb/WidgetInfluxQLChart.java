package com.xresch.emp.features.influxdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.features.dashboard.WidgetDefinition;
import com.xresch.cfw.features.dashboard.WidgetSettingsFactory;
import com.xresch.cfw.features.dashboard.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.CSVResponse;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.PlaintextResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.emp.features.prometheus.PrometheusEnvironment;
import com.xresch.emp.features.prometheus.PrometheusEnvironmentManagement;
import com.xresch.emp.features.prometheus.PrometheusSettingsFactory;
import com.xresch.emp.features.spm.SPMSettingsFactory;

public class WidgetInfluxQLChart extends WidgetDefinition {

	private static Logger logger = CFWLog.getLogger(WidgetInfluxQLChart.class.getName());
	@Override
	public String getWidgetType() {return "emp_influxdb_influxql_chart";}
	
	@Override
	public WidgetDataCachePolicy getCachePolicy() {
		return WidgetDataCachePolicy.TIME_BASED;
	}
	
	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				
				.addField(InfluxDBSettingsFactory.createEnvironmentSelectorField())
				
				.addField(InfluxDBSettingsFactory.createDatabaseSelectorField())
				
				.addField(InfluxDBSettingsFactory.createQueryField(
						"SELECT mean(*)\r\n"
						+ "FROM runtime \r\n"
						+ "WHERE time >= [earliest] and time < [latest] group by time([interval]);"
					)
				)
			
				.addField(CFWField.newString(FormFieldType.TEXT, "valuecolumn")
						.setLabel("{!emp_widget_influxdb_influxql_valuecolumn!}")
						.setDescription("{!emp_widget_influxdb_influxql_valuecolumn_desc!}")
				)
				
				.addField(CFWField.newString(FormFieldType.TEXT, "labels")
						.setLabel("{!emp_widget_influxdb_influxql_labels!}")
						.setDescription("{!emp_widget_influxdb_influxql_labels_desc!}")
				)
				.addAllFields(WidgetSettingsFactory.createDefaultChartFields(false, false))
				.addField(WidgetSettingsFactory.createSampleDataField())
		;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings, long earliest, long latest) { 
		
		//---------------------------------
		// Example Data
		Boolean isSampleData = (Boolean)settings.getField(WidgetSettingsFactory.FIELDNAME_SAMPLEDATA).getValue();
		if(isSampleData != null && isSampleData) {
			createSampleData(response);
			return;
		}
		
		//---------------------------------
		// Resolve Database		
		LinkedHashMap<String,String> databaseNameMap = (LinkedHashMap<String,String>)settings.getField(InfluxDBSettingsFactory.FIELDNAME_DATABASE).getValue();
		if(databaseNameMap == null || databaseNameMap.isEmpty()) {
			return;
		}

		String databaseName = databaseNameMap.values().toArray(new String[]{})[0];
		
		//---------------------------------
		// Resolve Query		
		String influxdbQuery = (String)settings.getField(InfluxDBSettingsFactory.FIELDNAME_QUERY).getValue();
		
		if(Strings.isNullOrEmpty(influxdbQuery)) {
			return;
		}
		
		//---------------------------------
		// Get Environment
		String environmentID = (String)settings.getField(InfluxDBSettingsFactory.FIELDNAME_ENVIRONMENT).getValue();
		InfluxDBEnvironment environment;
		if(environmentID != null) {
			environment = InfluxDBEnvironmentManagement.getEnvironment(Integer.parseInt(environmentID));
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "Influx DB Threshold: The chosen environment seems not configured correctly.");
			return;
		}
				
		//---------------------------------
		// Fetch Data
		JsonObject queryResult = environment.queryRangeInfluxQL(databaseName, influxdbQuery, earliest, latest);

		response.getContent().append(queryResult);	
	}
	
	public void createSampleData(JSONResponse response) { 

		response.append(CFW.Files.readPackageResource(FeatureInfluxDB.PACKAGE_RESOURCE, "emp_widget_influxdb_influxql_chart_sample_v1.json") );
		
	}
	
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureInfluxDB.PACKAGE_RESOURCE, "emp_influxdb_commonFunctions.js") );
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureInfluxDB.PACKAGE_RESOURCE, "emp_widget_influxdb_influxql_chart.js") );
		return array;
	}

	@Override
	public ArrayList<FileDefinition> getCSSFiles() {
		return null;
	}

	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureInfluxDB.PACKAGE_RESOURCE, "lang_en_emp_influxdb.properties"));
		return map;
	}
	
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureInfluxDB.PERMISSION_WIDGETS_INFLUXDB);
	}

}
