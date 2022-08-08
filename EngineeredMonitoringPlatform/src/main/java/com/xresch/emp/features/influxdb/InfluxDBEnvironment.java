package com.xresch.emp.features.influxdb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;

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
		USE_HTTPS,
		USERNAME,
		PASSWORD
	}
			
	private CFWField<String> host = CFWField.newString(FormFieldType.TEXT, InfluxDBEnvironmentFields.HOST)
			.setDescription("The hostname of the influxdb instance.");
	
	private CFWField<Integer> port = CFWField.newInteger(FormFieldType.NUMBER, InfluxDBEnvironmentFields.PORT)
			.setDescription("The port used to access the influxdb instance.");
	
	private CFWField<Boolean> useHttps = CFWField.newBoolean(FormFieldType.BOOLEAN, InfluxDBEnvironmentFields.USE_HTTPS)
			.setDescription("Use HTTPS for calling the API.");
	
	private CFWField<String> username = CFWField.newString(FormFieldType.TEXT, InfluxDBEnvironmentFields.USERNAME)
			.setDescription("(Optional)The username used for authentication.");
	
	private CFWField<String> password = CFWField.newString(FormFieldType.PASSWORD, InfluxDBEnvironmentFields.PASSWORD)
			.disableSanitization() //do not sanitize passwords
			.enableEncryption("emp_influxdb_pw_encryption_salt_653hgjkj76jk5g7u64zYay!")
			.setDescription("(Optional)The Password used for authentication.");
	
	public InfluxDBEnvironment() {
		initializeFields();
	}
		
	private void initializeFields() {
		this.addFields(host, port, useHttps, username, password);
	}
		
			
	@Override
	public boolean isDeletable(int id) {

		int count = new DashboardWidget()
			.selectCount()
			.whereLike(DashboardWidgetFields.JSON_SETTINGS, "%\"environment\":"+id+"%")
			.executeCount();
		
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
	
	public String username() {
		return username.getValue();
	}
	
	public InfluxDBEnvironment username(String value) {
		this.username.setValue(value);
		return this;
	}
	
	public String password() {
		return username.getValue();
	}
	
	public InfluxDBEnvironment password(String value) {
		this.password.setValue(value);
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
	public JsonObject queryInfluxQL(String database, String influxdbQuery) {
		
		//---------------------------
		// Prepare Query
		String queryURL = getAPIUrlVersion1() + "/query";
		LinkedHashMap<String,String> params = new LinkedHashMap<>();
		params.put("q", influxdbQuery);
		params.put("epoch", "ms");
		
		if(!Strings.isNullOrEmpty(database)) 		{  params.put("db", database); }
		if(!Strings.isNullOrEmpty(this.username())) {  params.put("u", this.username()); }
		if(!Strings.isNullOrEmpty(this.password())) {  params.put("p", this.password()); }
		
		//---------------------------
		// Execute API Call
		CFWHttpResponse queryResult = CFW.HTTP.sendPOSTRequest(queryURL, params, null);
		
		System.out.println("=========== TEST =========");
		
		if(queryResult != null) {
			JsonElement jsonElement = CFW.JSON.fromJson(queryResult.getResponseBody());
			
			JsonObject json = jsonElement.getAsJsonObject();
			
			System.out.println(queryResult.getResponseBody());
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
	public JsonObject queryRangeInfluxQL(String database, String influxdbQuery,  long earliestMillis, long latestMillis) {
		
		//---------------------------
		// Prepare Query
		String interval = CFW.Utils.Time.calculateDatapointInterval(earliestMillis, latestMillis, 100);
		
		influxdbQuery = influxdbQuery.replace("[interval]", interval )
									 .replace("$interval$", interval )
									 .replace("[earliest]", ""+earliestMillis*1000000)
									 .replace("$earliest$", ""+earliestMillis*1000000)
									 .replace("[latest]", ""+latestMillis*1000000)
									 .replace("$latest$", ""+latestMillis*1000000)
									 .replace("\r\n", " ")
									 .replace('\n', ' ')
									 .replace('\r', ' ')
									 ;

		return queryInfluxQL(database, influxdbQuery);
		//System.out.println(influxdbQuery);
		
//		String queryURL = getAPIUrlVersion1() 
//				+ "/query?q="+CFW.HTTP.encode(influxdbQuery)
//				+"&epoch=ms";
//		
//		if(!Strings.isNullOrEmpty(database)) {
//			queryURL += "&db="+CFW.HTTP.encode(database);
//		}
//
//		//---------------------------
//		// Execute API Call
//		CFWHttpResponse queryResult = CFW.HTTP.sendGETRequest(queryURL);
//		if(queryResult != null) {
//			JsonElement jsonElement = CFW.JSON.fromJson(queryResult.getResponseBody());
//			
//			JsonObject json = jsonElement.getAsJsonObject();
////			if(json.get("error") != null) {
////				CFW.Context.Request.addAlertMessage(MessageType.ERROR, "InfluxDB Error: "+json.get("error").getAsString());
////				return null;
////			}
//			
//			return json;
//		}
//		return null;
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

		JsonObject result = environment.queryInfluxQL(null, "SHOW DATABASES");
	
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
