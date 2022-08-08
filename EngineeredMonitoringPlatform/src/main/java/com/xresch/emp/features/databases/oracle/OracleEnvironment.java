package com.xresch.emp.features.databases.oracle;

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
 * @author Reto Scheiwiller, (c) Copyright 2021
 * @license MIT-License
 **************************************************************************************************************/
public class OracleEnvironment extends AbstractContextSettings {
	
	public static final String SETTINGS_TYPE = "Oracle Environment";
	
	private DBInterface dbInstance = null;
	
	public enum OracleEnvironmentFields{
		DB_HOST,
		DB_PORT,
		DB_NAME,
		DB_TYPE,
		DB_USER,
		DB_PASSWORD,
		TIME_ZONE,
	}
		
	private CFWField<String> dbHost = CFWField.newString(FormFieldType.TEXT, OracleEnvironmentFields.DB_HOST)
			.setDescription("The server name of the database host.");
	
	private CFWField<Integer> dbPort = CFWField.newInteger(FormFieldType.NUMBER, OracleEnvironmentFields.DB_PORT)
			.setDescription("The port used to access the database.");
	
	private CFWField<String> dbName = CFWField.newString(FormFieldType.TEXT, OracleEnvironmentFields.DB_NAME)
			.setDescription("The name of the user for accessing the database.");
	
	private CFWField<String> dbType = CFWField.newString(FormFieldType.SELECT, OracleEnvironmentFields.DB_TYPE)
			.setDescription("The type of the oracle service.")
			.setOptions(new String[] {"Service Name", "SID"})
			.setValue("SID");
	
	private CFWField<String> dbUser = CFWField.newString(FormFieldType.TEXT, OracleEnvironmentFields.DB_USER)
			.setDescription("The name of the user for accessing the database.");
	
	private CFWField<String> dbPassword = CFWField.newString(FormFieldType.PASSWORD, OracleEnvironmentFields.DB_PASSWORD)
			.setDescription("The password of the DB user.")
			.disableSanitization()
			.enableEncryption("oracle_DB_PW_Salt");
	
	private CFWField<String> timezone = CFWField.newString(FormFieldType.TIMEZONEPICKER, OracleEnvironmentFields.TIME_ZONE)
			.setDescription("The timezone the database is using. Needed to manage differences from GMT properly.");
	
	
	public OracleEnvironment() {
		initializeFields();
	}
		
	private void initializeFields() {
		this.addFields(dbHost, dbPort, dbName, dbType, dbUser, dbPassword, timezone);
	}
		
	
	@Override
	public boolean isDeletable(int id) {
		
		int count = new DashboardWidget()
			.selectCount()
			.whereLike(DashboardWidgetFields.JSON_SETTINGS, "%\"environment\":"+id+"%")
			.and().like(DashboardWidgetFields.TYPE, "emp_oracle%")
			.executeCount();
		
		if(count == 0) {
			return true;
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The Oracle Environment cannot be deleted as it is still in use by "+count+"  widget(s).");
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
	

	public String dbType() {
		return dbType.getValue();
	}
	
	public OracleEnvironment dbType(String value) {
		this.dbType.setValue(value);
		return this;
	}
			
	public String dbHost() {
		return dbHost.getValue();
	}
	
	public OracleEnvironment dbHost(String value) {
		this.dbHost.setValue(value);
		return this;
	}
		
	public int dbPort() {
		return dbPort.getValue();
	}
	
	public OracleEnvironment dbPort(int value) {
		this.dbPort.setValue(value);
		return this;
	}
	
	public String dbName() {
		return dbName.getValue();
	}
	
	public OracleEnvironment dbName(String value) {
		this.dbName.setValue(value);
		return this;
	}
	
	public String dbUser() {
		return dbUser.getValue();
	}
	
	public OracleEnvironment dbUser(String value) {
		this.dbUser.setValue(value);
		return this;
	}
	
	public String dbPassword() {
		return dbPassword.getValue();
	}
	
	public OracleEnvironment dbPassword(String value) {
		this.dbPassword.setValue(value);
		return this;
	}	
	
	public String timezone() {
		return timezone.getValue();
	}
	
	public OracleEnvironment timezone(String value) {
		this.timezone.setValue(value);
		return this;
	}	

	public DBInterface getDBInstance() {
		return dbInstance;
	}

	public void setDBInstance(DBInterface dbInstance) {
		this.dbInstance = dbInstance;
	}
	
}
