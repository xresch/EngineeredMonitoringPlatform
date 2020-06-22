package com.pengtoolbox.emp.features.environments;

import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.datahandling.CFWAutocompleteHandler;
import com.pengtoolbox.cfw.datahandling.CFWField;
import com.pengtoolbox.cfw.datahandling.CFWField.FormFieldType;
import com.pengtoolbox.cfw.db.DBInterface;
import com.pengtoolbox.cfw.features.contextsettings.AbstractContextSettings;
import com.pengtoolbox.cfw.features.dashboard.DashboardWidget;
import com.pengtoolbox.cfw.features.dashboard.DashboardWidget.DashboardWidgetFields;
import com.pengtoolbox.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class AWAEnvironment extends AbstractContextSettings {
	
	public static final String SETTINGS_TYPE = "AWA Environment";
	
	private DBInterface dbInstance = null;
	
	public enum AWAEnvironmentFields{
		URL,
		DB_HOST,
		DB_PORT,
		DB_NAME,
		DB_TYPE,
		DB_USER,
		DB_PASSWORD,
		CLIENT_ID
	}
	
	private CFWField<String> url = CFWField.newString(FormFieldType.TEXT, AWAEnvironmentFields.URL)
			.setDescription("The url of the AWA web application including port number if required.");	
	
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
			.disableSecurity()
			.enableEncryption("awa_DB_PW_Salt");
	
	private CFWField<Integer> clientID = CFWField.newInteger(FormFieldType.NUMBER, AWAEnvironmentFields.CLIENT_ID)
			.setDescription("The ID of the client. This relates to the db column AH_CLIENT. Start typing to get a complete list of client IDs.")
			.setValue(0)
			.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
				
				@Override
				public LinkedHashMap<Object, Object> getAutocompleteData(HttpServletRequest request, String searchValue) {
					return AWAEnvironmentManagement.autocompleteClient(request);
				}
			});
	
	public AWAEnvironment() {
		initializeFields();
	}
		
	private void initializeFields() {
		this.addFields(url, dbHost, dbPort, dbName, dbType, dbUser, dbPassword, clientID);
	}
		
	
	@Override
	public boolean isDeletable(int id) {
		
		int count = new DashboardWidget()
			.selectCount()
			.whereLike(DashboardWidgetFields.JSON_SETTINGS, "%\"environment\":"+id+"%")
			.and(DashboardWidgetFields.TYPE, "emp_awajobstatus")
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
	
	public String url() {
		return url.getValue();
	}
	
	public AWAEnvironment url(String url) {
		this.url.setValue(url);
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
