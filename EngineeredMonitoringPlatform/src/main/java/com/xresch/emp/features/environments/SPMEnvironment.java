package com.xresch.emp.features.environments;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.dashboard.DashboardWidget;
import com.xresch.cfw.features.dashboard.DashboardWidget.DashboardWidgetFields;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class SPMEnvironment extends AbstractContextSettings {
	
	public static final String SETTINGS_TYPE = "SPM Environment";
	
	private DBInterface dbInstance = null;
	
	public enum SPMEnvironmentFields{
		URL,
		API_USER,
		API_PASSWORD,
		DB_HOST,
		DB_PORT,
		DB_NAME,
		DB_USER,
		DB_PASSWORD
	}
		
	private CFWField<String> url = CFWField.newString(FormFieldType.TEXT, SPMEnvironmentFields.URL)
			.setDescription("The url of the SPM web application, including port number if required. Make sure to include http/https protocol to make sure links will work correctly.");
	
	private CFWField<String> apiUser = CFWField.newString(FormFieldType.TEXT, SPMEnvironmentFields.API_USER)
			.setDescription("The name of the user for fetching the API.");
	
	private CFWField<String> apiUserPassword = CFWField.newString(FormFieldType.PASSWORD, SPMEnvironmentFields.API_PASSWORD)
			.setDescription("The password of the API user.")
			.disableSecurity()
			.enableEncryption("spm_API_PW_Salt");
	
	private CFWField<String> dbHost = CFWField.newString(FormFieldType.TEXT, SPMEnvironmentFields.DB_HOST)
			.setDescription("The server name of the database host.");
	
	private CFWField<Integer> dbPort = CFWField.newInteger(FormFieldType.NUMBER, SPMEnvironmentFields.DB_PORT)
			.setDescription("The port used to access the database.");
	
	private CFWField<String> dbName = CFWField.newString(FormFieldType.TEXT, SPMEnvironmentFields.DB_NAME)
			.setDescription("The name of the user for accessing the database.");
	
	private CFWField<String> dbUser = CFWField.newString(FormFieldType.TEXT, SPMEnvironmentFields.DB_USER)
			.setDescription("The name of the user for accessing the database.");
	
	private CFWField<String> dbPassword = CFWField.newString(FormFieldType.PASSWORD, SPMEnvironmentFields.DB_PASSWORD)
			.setDescription("The password of the DB user.")
			.disableSecurity()
			.enableEncryption("spm_DB_PW_Salt");
	
	public SPMEnvironment() {
		initializeFields();
	}
		
	private void initializeFields() {
		this.addFields(url, apiUser, apiUserPassword, dbHost, dbPort, dbName, dbUser, dbPassword);
	}
		
			
	@Override
	public boolean isDeletable(int id) {

		int count = new DashboardWidget()
			.selectCount()
			.whereLike(DashboardWidgetFields.JSON_SETTINGS, "%\"environment\":"+id+"%")
			.custom("AND (\"TYPE\"='emp_spmprojectstatus' OR \"TYPE\"='emp_spmmonitorstatus')")
			.getCount();
		
		if(count == 0) {
			return true;
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The SPM Environment cannot be deleted as it is still in use by "+count+"  widget(s).");
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
	
	public SPMEnvironment url(String url) {
		this.url.setValue(url);
		return this;
	}
	
	public String apiUser() {
		return apiUser.getValue();
	}
	
	public SPMEnvironment apiUser(String value) {
		this.apiUser.setValue(value);
		return this;
	}
	
	public String apiUserPassword() {
		return apiUserPassword.getValue();
	}
	
	public SPMEnvironment apiUserPassword(String value) {
		this.apiUserPassword.setValue(value);
		return this;
	}
		
	public String dbHost() {
		return dbHost.getValue();
	}
	
	public SPMEnvironment dbHost(String value) {
		this.dbHost.setValue(value);
		return this;
	}
		
	public int dbPort() {
		return dbPort.getValue();
	}
	
	public SPMEnvironment dbPort(int value) {
		this.dbPort.setValue(value);
		return this;
	}
	
	public String dbName() {
		return dbName.getValue();
	}
	
	public SPMEnvironment dbName(String value) {
		this.dbName.setValue(value);
		return this;
	}
	
	public String dbUser() {
		return dbUser.getValue();
	}
	
	public SPMEnvironment dbUser(String value) {
		this.dbUser.setValue(value);
		return this;
	}
	
	public String dbPassword() {
		return dbPassword.getValue();
	}
	
	public SPMEnvironment dbPassword(String value) {
		this.dbPassword.setValue(value);
		return this;
	}

	public DBInterface getDBInstance() {
		return dbInstance;
	}

	public void setDBInstance(DBInterface dbInstance) {
		this.dbInstance = dbInstance;
	}
	
	
	
}
