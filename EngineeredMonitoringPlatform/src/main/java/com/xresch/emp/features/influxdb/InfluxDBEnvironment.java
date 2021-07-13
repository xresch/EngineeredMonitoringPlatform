package com.xresch.emp.features.influxdb;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw._main.CFW.Utils;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.core.AutocompleteItem;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.dashboard.DashboardWidget;
import com.xresch.cfw.features.dashboard.DashboardWidget.DashboardWidgetFields;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.utils.CFWHttp.CFWHttpResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2021
 * @license MIT-License
 **************************************************************************************************************/
public class InfluxDBEnvironment extends AbstractContextSettings {
	
	public static final String SETTINGS_TYPE = "InfluxDB Environment";
	private String apiURL = null;
	
	public enum InfluxDBEnvironmentFields{
		HOST,
		PORT,
		USE_HTTPS
	}
			
	private CFWField<String> host = CFWField.newString(FormFieldType.TEXT, InfluxDBEnvironmentFields.HOST)
			.setDescription("The hostname of the influxdb instance.");
	
	private CFWField<Integer> port = CFWField.newInteger(FormFieldType.NUMBER, InfluxDBEnvironmentFields.PORT)
			.setDescription("The port used to access the influxdb instance.");
	
	private CFWField<Boolean> useHttps = CFWField.newBoolean(FormFieldType.BOOLEAN, InfluxDBEnvironmentFields.USE_HTTPS)
			.setDescription("Use HTTPS for calling the API.");
	
	public InfluxDBEnvironment() {
		initializeFields();
	}
		
	private void initializeFields() {
		this.addFields(host, port, useHttps);
	}
		
			
	@Override
	public boolean isDeletable(int id) {

		int count = new DashboardWidget()
			.selectCount()
			.whereLike(DashboardWidgetFields.JSON_SETTINGS, "%\"environment\":"+id+"%")
			.getCount();
		
		if(count == 0) {
			return true;
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The InfluxDB environment cannot be deleted as it is still in use by "+count+"  widget(s).");
			return false;
		}

	}
	
	public boolean isDefined() {
		if(host.getValue() != null
		&& port.getValue() != null) {
			return true;
		}
		
		return false;
	}
	
	
	public String getAPIUrlVersion1() {
		
		if(apiURL == null) {
			StringBuilder builder = new StringBuilder();
			
			if(useHttps.getValue()) {
				builder.append("https://");
			}else {
				builder.append("http://");
			}
			builder.append(host.getValue())
				.append(":")
				.append(port.getValue())
				.append("/api/v1");
			
			apiURL = builder.toString();
		}
		
		return apiURL;
	}
	
	public String host() {
		return host.getValue();
	}
	
	public InfluxDBEnvironment host(String value) {
		this.host.setValue(value);
		return this;
	}
		
	public int port() {
		return port.getValue();
	}
	
	public InfluxDBEnvironment port(int value) {
		this.port.setValue(value);
		return this;
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public JsonObject getTargets() {
		
		String queryURL = getAPIUrlVersion1() + "/targets";
		
		CFWHttpResponse queryResult = CFW.HTTP.sendGETRequest(queryURL);
		if(queryResult != null) {
			JsonElement jsonElement = CFW.JSON.fromJson(queryResult.getResponseBody());
			JsonObject json = jsonElement.getAsJsonObject();
			if(json.get("error") != null) {
				CFW.Context.Request.addAlertMessage(MessageType.ERROR, "InfluxDB Error: "+json.get("error").getAsString());
				return null;
			}
			
			return json;
		}
		return null;
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public JsonObject getMetrics() {
		
		String queryURL = getAPIUrlVersion1() + "/metadata";
		
		CFWHttpResponse queryResult = CFW.HTTP.sendGETRequest(queryURL);
		if(queryResult != null) {
			JsonElement jsonElement = CFW.JSON.fromJson(queryResult.getResponseBody());
			JsonObject json = jsonElement.getAsJsonObject();
			if(json.get("error") != null) {
				CFW.Context.Request.addAlertMessage(MessageType.ERROR, "InfluxDB Error: "+json.get("error").getAsString());
				return null;
			}
			
			return json;
		}
		return null;
	}
	
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public JsonObject query(String influxdbQuery, long latestMillis) {
		
		String queryURL = getAPIUrlVersion1() 
				+ "/query?query="+CFW.HTTP.encode(influxdbQuery)
				+ "&time="+(latestMillis/1000);
		
		CFWHttpResponse queryResult = CFW.HTTP.sendGETRequest(queryURL);
		if(queryResult != null) {
			JsonElement jsonElement = CFW.JSON.fromJson(queryResult.getResponseBody());
			JsonObject json = jsonElement.getAsJsonObject();
			if(json.get("error") != null) {
				CFW.Context.Request.addAlertMessage(MessageType.ERROR, "InfluxDB Error: "+json.get("error").getAsString());
				return null;
			}
			
			return json;
		}
		return null;
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public JsonObject queryRange(String influxdbQuery, long earliestMillis, long latestMillis) {
		
		String interval = Utils.Time.calculateDatapointInterval(earliestMillis, latestMillis, 100);
		
		influxdbQuery = influxdbQuery.replace("[interval]", "["+( (interval.endsWith("s")) ? "1m" : interval )+"]");

		String queryURL = getAPIUrlVersion1() 
				+ "/query_range?query="+CFW.HTTP.encode(influxdbQuery)
				+"&start="+(earliestMillis/1000)
				+"&end="+(latestMillis/1000)
				+"&step="+interval;
		
		CFWHttpResponse queryResult = CFW.HTTP.sendGETRequest(queryURL);
		if(queryResult != null) {
			JsonElement jsonElement = CFW.JSON.fromJson(queryResult.getResponseBody());
			JsonObject json = jsonElement.getAsJsonObject();
			if(json.get("error") != null) {
				CFW.Context.Request.addAlertMessage(MessageType.ERROR, "InfluxDB Error: "+json.get("error").getAsString());
				return null;
			}
			
			return json;
		}
		return null;
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public AutocompleteResult autocompleteInstance(String searchValue, int limit) {
		
		if(searchValue.length() < 3) {
			return null;
		}
		
		JsonObject result = getTargets();
	
//		{ "data": { "activeTargets": [ { "labels": {
//        "instance": "127.0.0.1:9090",
//        "job": "influxdb"
//      },
//    }
		
		AutocompleteList list = new AutocompleteList();
		if(result != null) {
			
			JsonArray targets = result.get("data").getAsJsonObject().get("activeTargets").getAsJsonArray();
			String lowerSearch = searchValue.toLowerCase();
			for(JsonElement target : targets) {
				JsonObject labels = target.getAsJsonObject().get("labels").getAsJsonObject();
				if(labels.has("instance")) {
					String instance = labels.get("instance").getAsString();
					if(instance.toLowerCase().contains(lowerSearch)) {
						list.addItem(instance);
					}
					
					if(list.size() == limit) { break; }
				
				}
			}
		}
		
		return new AutocompleteResult(list); 

	}
	

	/************************************************************************************
	 * 
	 ************************************************************************************/
	public static AutocompleteResult autocompleteQuery(String searchValue, int limit) {
		
		if(searchValue.length() < 3) {
			return null;
		}
		
		String[] splitted = searchValue.split("\r\n|\n");
		String lastLine = splitted[splitted.length-1];
		if(lastLine.length() > 50) {
			return null;
		}
		
		String likeString = "%"+lastLine+"%";
		
		ResultSet resultSet = new CFWSQL(null)
			.loadSQLResource(FeatureInfluxDB.PACKAGE_RESOURCE, "emp_widget_influxdb_autocompleteQuery.sql", 
					"%query\":\""+likeString, 
					likeString, 
					limit)
			.getResultSet();
		
		AutocompleteList suggestions = new AutocompleteList();
		if(resultSet != null) {
			try {
				while(resultSet.next()) {
					String dashboardName = resultSet.getString("DASHBOARD_NAME");
					String widgetName = resultSet.getString("TITLE");
					JsonElement json = CFW.JSON.fromJson(resultSet.getString("JSON_SETTINGS"));
					
					JsonElement queryElement = json.getAsJsonObject().get("query");
					if(!queryElement.isJsonNull()) {
						String query = queryElement.getAsString();
						if(!Strings.isNullOrEmpty(widgetName)) {
							suggestions.addItem(new AutocompleteItem(query, query, "Source Dashboard: "+dashboardName+", Widget: "+widgetName).setMethodReplace(lastLine));
						}else {
							suggestions.addItem(new AutocompleteItem(query, query, "Source Dashboard: "+dashboardName).setMethodReplace(lastLine));
						}
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				CFW.DB.close(resultSet);
			}
		}
		
		return new AutocompleteResult(suggestions);
	}
	
}
