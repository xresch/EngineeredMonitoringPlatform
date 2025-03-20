package com.xresch.emp.features.prometheus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map.Entry;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.db.CFWSQL;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.core.AutocompleteItem;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.dashboard.DashboardWidget;
import com.xresch.cfw.features.dashboard.DashboardWidget.DashboardWidgetFields;
import com.xresch.cfw.utils.web.CFWHttp.CFWHttpResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class PrometheusEnvironment extends AbstractContextSettings {
	
	public static final String SETTINGS_TYPE = "Prometheus Environment";
	private String apiURL = null;
	
	public enum PrometheusEnvironmentFields{
		URL,
		HOST,
		PORT,
		USE_HTTPS
	}
	
	private CFWField<String> url = CFWField.newString(FormFieldType.TEXT, PrometheusEnvironmentFields.URL)
			.setLabel("URL")
			.setDescription("The API URL including proctol(http/https), port and without '/api/v1' at the end.");
	
	private CFWField<String> host = CFWField.newString(FormFieldType.TEXT, PrometheusEnvironmentFields.HOST)
			.setDescription("(Ignored if URL is defined) The hostname of the prometheus instance.");
	
	private CFWField<Integer> port = CFWField.newInteger(FormFieldType.NUMBER, PrometheusEnvironmentFields.PORT)
			.setDescription("(Ignored if URL is defined) The port used to access the prometheus instance. If this is not defined, the port is expected to be defined the hostname field.");
	
	private CFWField<Boolean> useHttps = CFWField.newBoolean(FormFieldType.BOOLEAN, PrometheusEnvironmentFields.USE_HTTPS)
			.setDescription("(Ignored if URL is defined) Use HTTPS for calling the API.");
	
	public PrometheusEnvironment() {
		initializeFields();
	}
		
	private void initializeFields() {
		this.addFields(url, host, port, useHttps);
	}
		
			
	@Override
	public boolean isDeletable(int id) {

		int count = new DashboardWidget()
			.selectCount()
			.whereLike(DashboardWidgetFields.JSON_SETTINGS, "%\"environment\":"+id+"%")
			//.custom("AND \"TYPE\" LIKE 'emp_prometheus%'")
			.executeCount();
		
		if(count == 0) {
			return true;
		}else {
			CFW.Messages.addErrorMessage("The Prometheus environment cannot be deleted as it is still in use by "+count+"  widget(s).");
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
		
		//------------------------------------
		// Create URL once
		if(apiURL == null) {

			if( !Strings.isNullOrEmpty(url.getValue()) ) {
				//-------------------------------
				// Use URL
				apiURL = url.getValue() + "/api/v1";
				apiURL = apiURL.replace("//api", "/api");
				
			}else {
				//-------------------------------
				// Use Legacy host and port definition
				
				StringBuilder builder = new StringBuilder();
				if(useHttps.getValue()) {
					builder.append("https://");
				}else {
					builder.append("http://");
				}
				builder.append(host.getValue());
				
				if(port.getValue() != null) {
					builder.append(":")
						   .append(port.getValue());
				}
				builder.append("/api/v1");
				apiURL = builder.toString();
			}
			
			
		}
		
		//------------------------------------
		// Return URL
		return apiURL;
	}
	
	public String host() {
		return host.getValue();
	}
	
	public PrometheusEnvironment host(String value) {
		this.host.setValue(value);
		return this;
	}
		
	public int port() {
		return port.getValue();
	}
	
	public PrometheusEnvironment port(int value) {
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
				CFW.Messages.addErrorMessage("getTargets() - Prometheus Error: "+json.get("error").getAsString());
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
				CFW.Messages.addErrorMessage("getMetrics() - Prometheus Error: "+json.get("error").getAsString());
				return null;
			}
			
			return json;
		}
		return null;
	}
	
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public JsonObject query(String prometheusQuery, long latestMillis) {
				
		CFWHttpResponse queryResult = 
				CFW.HTTP.newRequestBuilder(getAPIUrlVersion1()+"/query")
						.param("query", prometheusQuery)
						.param("time", ""+(latestMillis/1000))
						.send()
						;
		
		if(queryResult != null) {
			JsonElement jsonElement = CFW.JSON.fromJson(queryResult.getResponseBody());
			JsonObject json = jsonElement.getAsJsonObject();
			if(json.get("error") != null) {
				CFW.Messages.addErrorMessage("query() - Prometheus Error: "+json.get("error").getAsString());
				return null;
			}
			
			return json;
		}
		return null;
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public JsonObject queryRange(String prometheusQuery, long earliestMillis, long latestMillis) {
		
		String interval = CFW.Time.calculateDatapointInterval(earliestMillis, latestMillis, 100, "");
		
		prometheusQuery = prometheusQuery.replace("[interval]", "["+( (interval.endsWith("s")) ? "1m" : interval )+"]");
		
		CFWHttpResponse queryResult = 
				CFW.HTTP.newRequestBuilder(getAPIUrlVersion1()+"/query_range")
						.param("query", prometheusQuery)
						.param("start", ""+(earliestMillis/1000))
						.param("end", ""+(latestMillis/1000))
						.param("step", interval)
						.send()
						;

		if(queryResult != null) {
			JsonElement jsonElement = CFW.JSON.fromJson(queryResult.getResponseBody());
			JsonObject json = jsonElement.getAsJsonObject();
			if(json.get("error") != null) {
				CFW.Messages.addErrorMessage("queryRange() - Prometheus Error: "+json.get("error").getAsString());
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
//        "job": "prometheus"
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
	public AutocompleteResult autocompleteLabels(String searchValue, int limit) {
		
		if(searchValue.length() < 3) {
			return null;
		}
		
		String[] splitted = searchValue.split(" ");
		String lastword = splitted[splitted.length-1];
		if(lastword.length() > 50) {
			return null;
		}
		JsonObject result = getTargets();
		
		AutocompleteList list = new AutocompleteList();
		if(result != null) {
			
			JsonArray targets = result.get("data").getAsJsonObject().get("activeTargets").getAsJsonArray();
			String lowerSearch = searchValue.toLowerCase();
			for(JsonElement target : targets) {
				JsonObject labels = target.getAsJsonObject().get("labels").getAsJsonObject();
				for(Entry<String, JsonElement> entry : labels.entrySet()) {
					String entryString = entry.getKey() + "=\""+entry.getValue().getAsString()+"\"";
					if(entryString.toLowerCase().contains(lastword)) {
						AutocompleteItem item = new AutocompleteItem(entryString);
						item.setMethodReplaceLast(lastword);
						list.addItem(item);
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
	public AutocompleteResult autocompleteMetrics(String searchValue, int limit) {
		
		if(searchValue.length() < 3) {
			return null;
		}
		
		String[] splitted = searchValue.split(" ");
		String lastword = splitted[splitted.length-1].toLowerCase();
		if(lastword.length() > 50) {
			return null;
		}
		JsonObject result = getMetrics();
		
		AutocompleteList list = new AutocompleteList();
		if(result != null) {
			
			for(Entry<String, JsonElement> entry : result.get("data").getAsJsonObject().entrySet()) {
				String metricName = entry.getKey();
				if(metricName.toLowerCase().contains(lastword)) {
					AutocompleteItem item = new AutocompleteItem(metricName);
					list.addItem(item);
				}
				
				if(list.size() == limit) {
					break;
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
			.loadSQLResource(FeaturePrometheus.PACKAGE_RESOURCE, "emp_widget_prometheus_autocompleteQuery.sql", 
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
							suggestions.addItem(new AutocompleteItem(query, query, "Source Dashboard: "+dashboardName+", Widget: "+widgetName).setMethodReplaceLast(lastLine));
						}else {
							suggestions.addItem(new AutocompleteItem(query, query, "Source Dashboard: "+dashboardName).setMethodReplaceLast(lastLine));
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
