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
import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.core.AutocompleteItem;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.dashboard.DashboardWidget;
import com.xresch.cfw.features.dashboard.DashboardWidget.DashboardWidgetFields;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.utils.CFWHttp.CFWHttpResponse;
import com.xresch.emp.features.spm.EnvironmentManagerSPM;

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
				;
			
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
	public JsonObject v1_query(String database, String influxdbQuery) {
		
		//---------------------------
		// Prepare Query
		String queryURL = getAPIUrlVersion1() 
				+ "/query?q="+CFW.HTTP.encode(influxdbQuery)
				+"&epoch=ms"
				;
		
		if(!Strings.isNullOrEmpty(database)) {
			queryURL += "&db="+CFW.HTTP.encode(database);
		}
		

		//---------------------------
		// Execute API Call
		CFWHttpResponse queryResult = CFW.HTTP.sendGETRequest(queryURL);
		if(queryResult != null) {
			JsonElement jsonElement = CFW.JSON.fromJson(queryResult.getResponseBody());
			
			JsonObject json = jsonElement.getAsJsonObject();
//			if(json.get("error") != null) {
//				CFW.Context.Request.addAlertMessage(MessageType.ERROR, "InfluxDB Error: "+json.get("error").getAsString());
//				return null;
//			}
			
			return json;
		}
		return null;
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public JsonObject v1_queryRange(String database, String influxdbQuery,  long earliestMillis, long latestMillis) {
		
		//---------------------------
		// Prepare Query
		String interval = CFW.Utils.Time.calculateDatapointInterval(earliestMillis, latestMillis, 100);
		
		influxdbQuery = influxdbQuery.replace("[interval]", "["+( (interval.endsWith("s")) ? "1m" : interval )+"]")
									 .replace("[earliest]", ""+earliestMillis*1000000)
									 .replace("[latest]", ""+latestMillis*1000000)
									 ;

		String queryURL = getAPIUrlVersion1() 
				+ "/query?q="+CFW.HTTP.encode(influxdbQuery)
				+"&epoch=ms";
		
		if(!Strings.isNullOrEmpty(database)) {
			queryURL += "&db="+CFW.HTTP.encode(database);
		}
		

		//---------------------------
		// Execute API Call
		CFWHttpResponse queryResult = CFW.HTTP.sendGETRequest(queryURL);
		if(queryResult != null) {
			JsonElement jsonElement = CFW.JSON.fromJson(queryResult.getResponseBody());
			
			JsonObject json = jsonElement.getAsJsonObject();
//			if(json.get("error") != null) {
//				CFW.Context.Request.addAlertMessage(MessageType.ERROR, "InfluxDB Error: "+json.get("error").getAsString());
//				return null;
//			}
			
			return json;
		}
		return null;
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	public static AutocompleteResult autocompleteDatabaseOrBucket(int environmentID, String searchValue, int limit) {
		
		if(searchValue.length() < 3) {
			return null;
		}
		
		//---------------------------
		// Get Environment	
		InfluxDBEnvironment environment = InfluxDBEnvironmentManagement.getEnvironment(environmentID);
		
		if(environment == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "The chosen environment seems not configured correctly.");
			return null;
		}

		JsonObject result = environment.v1_query(null, "SHOW DATABASES");
	
//		{"results": [{"statement_id": 0,"series": [
//						{
//							"name": "databases",
//							"columns": [
//								"name"
//							],
//							"values": [
//								[
//									"_internal"
//								],
//								[...]
//							]

		
		AutocompleteList list = new AutocompleteList();
		if(result != null) {
			
			JsonArray values = result.get("results")
									.getAsJsonArray().get(0).getAsJsonObject()
										.get("series").getAsJsonArray().get(0)
											.getAsJsonObject().get("values").getAsJsonArray()
										;
			
			String lowerSearch = searchValue.toLowerCase();
			for(JsonElement valuesArray : values) {
				String databaseName = valuesArray.getAsJsonArray().get(0).getAsString();

				if(lowerSearch.equals("%%%") || databaseName.toLowerCase().contains(lowerSearch)) {
					list.addItem(databaseName);
				}
				
				if(list.size() == limit) { break; }
			
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
