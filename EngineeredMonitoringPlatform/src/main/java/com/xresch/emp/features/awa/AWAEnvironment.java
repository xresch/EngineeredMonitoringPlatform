package com.xresch.emp.features.awa;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.features.dashboard.DashboardWidget;
import com.xresch.cfw.features.dashboard.DashboardWidget.DashboardWidgetFields;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class AWAEnvironment extends AbstractContextSettings {
	
	public static final String SETTINGS_TYPE = "AWA Environment";
	
	private DBInterface dbInstance = null;
	
	public enum AWAEnvironmentFields{
		URL,
		URL_DB_PART,
		DB_HOST,
		DB_PORT,
		DB_NAME,
		DB_TYPE,
		DB_USER,
		DB_PASSWORD,
		CLIENT_ID
	}
	
	private CFWField<String> url = CFWField.newString(FormFieldType.TEXT, AWAEnvironmentFields.URL)
			.setDescription("The url of the AWA web application including port number(if required).");
	
	private CFWField<String> urlDBPart = CFWField.newString(FormFieldType.TEXT, AWAEnvironmentFields.URL_DB_PART)
			.setDescription("The db part of the url, needed to have direct links in dashboard tiles.");	
	
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
				public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue) {
					return AWAEnvironmentManagement.autocompleteClient(request);
				}
			});
	
	public AWAEnvironment() {
		initializeFields();
	}
		
	private void initializeFields() {
		this.addFields(url, urlDBPart, dbHost, dbPort, dbName, dbType, dbUser, dbPassword, clientID);
	}
		
	
	@Override
	public boolean isDeletable(int id) {
		
		int count = new DashboardWidget()
			.selectCount()
			.whereLike(DashboardWidgetFields.JSON_SETTINGS, "%\"environment\":"+id+"%")
			.and().like(DashboardWidgetFields.TYPE, "emp_awa%")
			.getCount();
		
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
						+"@pm/monitor/"+CFW.HTTP.encode(jobname)
						+"&id="+runID
						+"&type="+type
						+"&src=eh";
						
			return jobURL;
		}
		
		return null;
	}
	
	public String url() {
		return url.getValue();
	}
	
	public AWAEnvironment url(String url) {
		this.url.setValue(url);
		return this;
	}
	
	
	public String urlDBPart() {
		return urlDBPart.getValue();
	}
	
	public AWAEnvironment urlDBPart(String value) {
		this.urlDBPart.setValue(value);
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
	
}
