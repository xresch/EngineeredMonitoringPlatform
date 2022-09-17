package com.xresch.emp.features.awa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.icu.text.SimpleDateFormat;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.features.dashboard.DashboardWidget;
import com.xresch.cfw.features.dashboard.DashboardWidget.DashboardWidgetFields;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.utils.CFWHttp.CFWHttpResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class AWAEnvironment extends AbstractContextSettings {
	
	private static Logger logger = CFWLog.getLogger(AWAEnvironment.class.getName());
	
	public static final String SETTINGS_TYPE = "AWA Environment";
	public static final String SOURCE_DATABASE = "Database";
	public static final String SOURCE_REST_API = "REST API";
	
	private DBInterface dbInstance = null;
	
	private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	public enum AWAEnvironmentFields{
		SOURCE,
		URL,
		URL_DB_PART,
		API_URL,
		API_USERNAME,
		API_PASSWORD,
		DB_HOST,
		DB_PORT,
		DB_NAME,
		DB_TYPE,
		DB_USER,
		DB_PASSWORD,
		CLIENT_ID
	}
	
	private CFWField<String> source = CFWField.newString(FormFieldType.SELECT, AWAEnvironmentFields.SOURCE)
			.setDescription("The source from which to fetch the data.")
			.addOption(SOURCE_DATABASE, "Database (Recommended)")
			.addOption(SOURCE_REST_API, "REST API (Experimental)")
			.setValue(SOURCE_DATABASE);
	
	private CFWField<String> url = CFWField.newString(FormFieldType.TEXT, AWAEnvironmentFields.URL)
			.setDescription("The url of the AWA web application including port number(if required).");
	
	private CFWField<String> urlDBPart = CFWField.newString(FormFieldType.TEXT, AWAEnvironmentFields.URL_DB_PART)
			.setDescription("The db part of the url, needed to have direct links in dashboard tiles.");	
	
	private CFWField<String> apiUrl = CFWField.newString(FormFieldType.TEXT, AWAEnvironmentFields.API_URL)
			.setDescription("The url of the AWA rest interface including port number.(e.g. https://awa.mycompany.com:8080)");
	
	private CFWField<String> apiUsername = CFWField.newString(FormFieldType.TEXT, AWAEnvironmentFields.API_USERNAME)
			.setDescription("The username for accessing the REST API.");
	
	private CFWField<String> apiPassword = CFWField.newString(FormFieldType.PASSWORD, AWAEnvironmentFields.API_PASSWORD)
			.setDescription("The password of the DB user.")
			.disableSanitization()
			.enableEncryption("awa_REST_API_Password_Salt");

	private CFWField<String> dbHost = CFWField.newString(FormFieldType.TEXT, AWAEnvironmentFields.DB_HOST)
			.setDescription("The server name of the database host.");
	
	private CFWField<Integer> dbPort = CFWField.newInteger(FormFieldType.NUMBER, AWAEnvironmentFields.DB_PORT)
			.setDescription("The port used to access the database.");
	
	private CFWField<String> dbName = CFWField.newString(FormFieldType.TEXT, AWAEnvironmentFields.DB_NAME)
			.setDescription("The name of the user for accessing the database.");
	
	private CFWField<String> dbType = CFWField.newString(FormFieldType.SELECT, AWAEnvironmentFields.DB_TYPE)
			.setDescription("The type of the oracle service.")
			.setOptions(new String[] {"Service Name", "SID"})
			.setValue("SID");
	
	private CFWField<String> dbUser = CFWField.newString(FormFieldType.TEXT, AWAEnvironmentFields.DB_USER)
			.setDescription("The name of the user for accessing the database.");
	
	private CFWField<String> dbPassword = CFWField.newString(FormFieldType.PASSWORD, AWAEnvironmentFields.DB_PASSWORD)
			.setDescription("The password of the DB user.")
			.disableSanitization()
			.enableEncryption("awa_DB_PW_Salt");
	
	private CFWField<Integer> clientID = CFWField.newInteger(FormFieldType.NUMBER, AWAEnvironmentFields.CLIENT_ID)
			.setDescription("The ID of the client. This relates to the db column AH_CLIENT. Start typing to get a complete list of client IDs.")
			.setValue(0)
			.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
				
				@Override
				public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
					return AWAEnvironmentManagement.autocompleteClient(request);
				}
			});
	
	public AWAEnvironment() {
		initializeFields();
	}
		
	private void initializeFields() {
		this.addFields(source, url, urlDBPart, apiUrl, apiUsername, apiPassword, dbHost, dbPort, dbName, dbType, dbUser, dbPassword, clientID);
	}
		
	@Override
	public boolean isDeletable(int id) {
		
		int count = new DashboardWidget()
			.selectCount()
			.whereLike(DashboardWidgetFields.JSON_SETTINGS, "%\"environment\":"+id+"%")
			.and().like(DashboardWidgetFields.TYPE, "emp_awa%")
			.executeCount();
		
		if(count == 0) {
			return true;
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The AWA Environment cannot be deleted as it is still in use by "+count+"  widget(s).");
			return false;
		}

	}
	
	public boolean isDBDefined() {
		if(dbHost.getValue() != null
		&& dbPort.getValue() != null
		&& dbUser.getValue() != null) {
			return true;
		}
		
		return false;
	}
	
	public String getJobWorkflowURL(String jobname, String type, int runID) {
		String urlString = url.getValue();
		String urlDBPartString = urlDBPart.getValue();
		if( !Strings.isNullOrEmpty(urlString) 
		&&	!Strings.isNullOrEmpty(urlDBPartString)
				) {
			if(!urlString.endsWith("/")) { urlString += "/"; url.setValue(urlString); };
			
			String finalClientID = "00"+clientID.getValue();
			finalClientID = finalClientID.substring(finalClientID.length() - 4);
			
			String jobURL = urlString+"awi/#"+urlDBPartString+":"+finalClientID
						+"@executions/executions/EXECUTION/"+CFW.HTTP.encode(jobname)
						+"&id="+runID
						+"&type="+type
						+"&src=eh";
						
			return jobURL;
		}
		
		return null;
	}
	
	public String source() {
		return source.getValue();
	}
	
	public AWAEnvironment source(String value) {
		this.source.setValue(value);
		return this;
	}
	
	public String url() {
		return url.getValue();
	}
	
	public AWAEnvironment url(String value) {
		this.url.setValue(value);
		return this;
	}
	
	
	public String urlDBPart() {
		return urlDBPart.getValue();
	}
	
	public AWAEnvironment urlDBPart(String value) {
		this.urlDBPart.setValue(value);
		return this;
	}
	
	public String apiUrl() {
		return apiUrl.getValue();
	}
	
	public AWAEnvironment apiUrl(String value) {
		this.apiUrl.setValue(value);
		return this;
	}
	
	public String apiUsername() {
		return apiUsername.getValue();
	}
	
	public AWAEnvironment apiUsername(String value) {
		this.apiUsername.setValue(value);
		return this;
	}
	
	public String apiPassword() {
		return apiPassword.getValue();
	}
	
	public AWAEnvironment apiPassword(String value) {
		this.apiPassword.setValue(value);
		return this;
	}

	public String dbType() {
		return dbType.getValue();
	}
	
	public AWAEnvironment dbType(String value) {
		this.dbType.setValue(value);
		return this;
	}
			
	public String dbHost() {
		return dbHost.getValue();
	}
	
	public AWAEnvironment dbHost(String value) {
		this.dbHost.setValue(value);
		return this;
	}
		
	public int dbPort() {
		return dbPort.getValue();
	}
	
	public AWAEnvironment dbPort(int value) {
		this.dbPort.setValue(value);
		return this;
	}
	
	public String dbName() {
		return dbName.getValue();
	}
	
	public AWAEnvironment dbName(String value) {
		this.dbName.setValue(value);
		return this;
	}
	
	public String dbUser() {
		return dbUser.getValue();
	}
	
	public AWAEnvironment dbUser(String value) {
		this.dbUser.setValue(value);
		return this;
	}
	
	public String dbPassword() {
		return dbPassword.getValue();
	}
	
	public AWAEnvironment dbPassword(String value) {
		this.dbPassword.setValue(value);
		return this;
	}
	
	public Integer clientID() {
		return clientID.getValue();
	}
	
	public String clientIDWithLeadingZeros() {
		
		String clientIDWithZeros = ""+clientID.getValue();
		while(clientIDWithZeros.length() < 4) {
			clientIDWithZeros = "0"+clientIDWithZeros;
		}
		return clientIDWithZeros;
	}
	
	public AWAEnvironment clientID(int value) {
		this.clientID.setValue(value);
		return this;
	}
	

	public DBInterface getDBInstance() {
		return dbInstance;
	}

	public void setDBInstance(DBInterface dbInstance) {
		this.dbInstance = dbInstance;
	}
	
	
	/************************************************************************************
	 * Returns the URL to the rest API including the client ID.
	 ************************************************************************************/
	private String getAPIUrlVersion1() {
		String baseURL = this.apiUrl();
		if(baseURL.endsWith("?") || baseURL.endsWith("/")) {
			baseURL = baseURL.substring(0, baseURL.length()-1);
		}
		
		return  baseURL+"/ae/api/v1/"+this.clientIDWithLeadingZeros();
	}
	
	/************************************************************************************
	 * Fetches the last execution from the API
	 ************************************************************************************/
	public JsonArray fetchFromAPILast(String[] jobnames, String[] joblabels, long earliest, long latest) {
		
		
		String earliestString = formatter.format(new Date(earliest));
		String latestString = formatter.format(new Date(latest));
		
		System.out.println("earliest: "+earliest);
		System.out.println("earliestString: "+earliestString);
		System.out.println("latest: "+latest);
		System.out.println("latestString: "+latestString);
		
		//---------------------------------
		// Fetch Data
		JsonArray resultArray = new JsonArray();
		for(int i = 0; i < jobnames.length; i++) {

			String currentJobname = jobnames[i].trim();
			
			//---------------------------------
			// Send Request
			CFWHttpResponse queryResult = 
				CFW.HTTP.newRequestBuilder(getAPIUrlVersion1()+"/executions")
						.GET()
						.authenticationBasic(this.apiUsername(), this.apiPassword())
						.header("Content-Type", "application/json")
						.param("name", currentJobname)
						//.param("include_deactivated", "true")
						.param("max_results", "1")
						.param("time_frame_from", "1970-01-01T00:00:00Z")
						.param("time_frame_to", latestString)
						//.param("time_frame_option", "all")
						.send()
					;
			
			
			//---------------------------------
			// Create Response Object
			JsonObject object = new JsonObject();
			object.addProperty("JOBNAME", currentJobname);
			
			if(joblabels != null && i < joblabels.length) {
				object.addProperty("LABEL", joblabels[i]);
			}else {
				object.addProperty("LABEL", currentJobname);
			}
			
			if(queryResult != null){
				
				//---------------------------------
				// Get Last execution Object
				JsonObject resultObject = queryResult.getResponseBodyAsJsonObject();
				
				JsonElement errorElement = resultObject.get("error");
				
				if(errorElement != null) {
					new CFWLog(logger).severe("AWA: Error occured while fetching data from REST API: "+queryResult.getResponseBody() );
					object.addProperty("STATUS", "UNKNOWN");
					resultArray.add(object);
					continue;
				}
				
				System.out.println("=============================");
				System.out.println("Jobname: "+currentJobname);
				System.out.println("ResultObject: "+resultObject);
				
				JsonElement dataElement = resultObject.get("data");
				if(dataElement == null || !dataElement.isJsonArray()) {
					System.out.println("A");
					object.addProperty("STATUS", "UNKNOWN");
					resultArray.add(object);
					continue;
				}
				
				JsonArray dataArray = dataElement.getAsJsonArray();
				if(dataArray.isEmpty()) {
					System.out.println("B");
					object.addProperty("STATUS", "UNKNOWN");
					resultArray.add(object);
					continue;
				}
				
				JsonObject executionObject = dataArray.get(0).getAsJsonObject();

				//---------------------------------
				// Prepare Data
				int runID = executionObject.get("run_id").getAsInt();
				int statusCode = executionObject.get("status").getAsInt();
				String type = executionObject.get("type").getAsString();
				String URL = this.getJobWorkflowURL(currentJobname, type, runID);
				
				//---------------------------------
				// Evaluate Monitoring Status
				String status = "";
				if(statusCode == 0) 		{ status = "ENDED OK"; }
				else if(statusCode <= 1654)	{ status = "RUNNING"; }
				else if(statusCode <= 1799)	{ status = "WAITING"; }
				else if(statusCode <= 1899)	{ status = "ABNORMAL ENDING"; }
				else if(statusCode <= 1999)	{ status = "ENDED OK"; }
				else 						{ status = "UNKNOWN"; }
				
				//---------------------------------
				// Get Times
				JsonElement activationTimeElement = executionObject.get("activation_time");
				String activationTime = (activationTimeElement == null || activationTimeElement.isJsonNull()) ? null : activationTimeElement.getAsString();
				
				JsonElement startTimeElement = executionObject.get("start_time");
				String startTime = (startTimeElement == null || startTimeElement.isJsonNull()) ? null : startTimeElement.getAsString();
				
				JsonElement endTimeElement = executionObject.get("end_time");
				String endTime = (endTimeElement == null || endTimeElement.isJsonNull()) ? null : endTimeElement.getAsString();
				
				//---------------------------------
				// Build Response
				object.addProperty("STATUS", status);
				object.addProperty("STATUS_TEXT", executionObject.get("status_text").getAsString());
				object.addProperty("CLIENT_ID", this.clientID());
				object.addProperty("TYPE", type);
				object.addProperty("ACTIVATION_TIME", activationTime);
				object.addProperty("START_TIME", startTime);
				object.addProperty("END_TIME", endTime);
				object.addProperty("DURATION_SECONDS", executionObject.get("estimated_runtime").getAsInt());
				object.addProperty("STATUS_CODE", statusCode);
				object.addProperty("URL", URL);

				resultArray.add(object);
			}else {
				object.addProperty("STATUS", "UNKNOWN");
				resultArray.add(object);
			}
			
			
		}
		
		return resultArray;
		
	}


	/************************************************************************************
	 * 
	 ************************************************************************************/
	public JsonArray fetchFromDatabase(String[] jobnames, String[] joblabels) {
		
		//---------------------------------
		// Get DB
		DBInterface db = this.getDBInstance();
		
		if(db == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "AWA Job Status: The chosen environment seems not configured correctly.");
			return null;
		}
		
		//---------------------------------
		// Fetch Data
		JsonArray resultArray = new JsonArray();
		for(int i = 0; i < jobnames.length; i++) {

			String currentJobname = jobnames[i].trim();
			ResultSet result = db.preparedExecuteQuerySilent(
					CFW.Files.readPackageResource(FeatureAWA.PACKAGE_RESOURCE, "emp_awa_last_jobstatus.sql"),
					this.clientID(),
					currentJobname);
			try {
				JsonObject object = new JsonObject();
				object.addProperty("JOBNAME", currentJobname);
				
				if(joblabels != null && i < joblabels.length) {
					object.addProperty("LABEL", joblabels[i]);
				}else {
					object.addProperty("LABEL", currentJobname);
				}
				
				if(result != null && result.next()){

					OffsetDateTime startTime = result.getObject("START_TIME", OffsetDateTime.class);
					OffsetDateTime endTime = result.getObject("END_TIME", OffsetDateTime.class);

					int runID = result.getInt("ID");
					String type = result.getString("TYPE");
					String URL = this.getJobWorkflowURL(currentJobname, type, runID);
					
					object.addProperty("STATUS", result.getString("STATUS"));
					object.addProperty("CLIENT_ID", result.getString("CLIENT_ID"));
					object.addProperty("TYPE", type);
					object.addProperty("START_TIME", (startTime != null) ? startTime.toInstant().toEpochMilli() : null );
					object.addProperty("END_TIME", (endTime != null) ? endTime.toInstant().toEpochMilli() : null);
					object.addProperty("HOST_DESTINATION", result.getString("HOST_DESTINATION"));
					object.addProperty("HOST_SOURCE", result.getString("HOST_SOURCE"));
					object.addProperty("DURATION_SECONDS", result.getInt("DURATION_SECONDS"));
					object.addProperty("STATUS_CODE", result.getInt("STATUS_CODE"));
					object.addProperty("URL", URL);

				}else {
					object.addProperty("status", "UNKNOWN");
				}
				
				resultArray.add(object);
				
			} catch (SQLException e) {
				new CFWLog(logger)
					.severe("Error fetching Widget data.", e);
			}finally {
				db.close(result);
			}
		}
		return resultArray;
	}
}
