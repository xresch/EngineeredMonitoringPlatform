package com.pengtoolbox.emp.features.environments;

import com.pengtoolbox.cfw.datahandling.CFWField;
import com.pengtoolbox.cfw.datahandling.CFWField.FormFieldType;
import com.pengtoolbox.cfw.datahandling.CFWObject;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, � 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class EnvironmentSPM extends CFWObject {
	
	public static final String SETTINGS_TYPE = "SPM Environment";
	
	public enum EnvironmentFields{
		URL,
		API_USER,
		API_PASSWORD,
		DB_HOST,
		DB_PORT,
		DB_NAME,
		DB_USER,
		DB_PASSWORD
	}
		
	private CFWField<String> url = CFWField.newString(FormFieldType.TEXT, EnvironmentFields.URL)
			.setDescription("The url of the SPM web application including port number if required.");
	
	private CFWField<String> apiUser = CFWField.newString(FormFieldType.TEXT, EnvironmentFields.API_USER)
			.setDescription("The name of the user for fetching the API.");
	
	private CFWField<String> apiUserPassword = CFWField.newString(FormFieldType.PASSWORD, EnvironmentFields.API_PASSWORD)
			.setDescription("The password of the API user.");
	
	private CFWField<String> dbHost = CFWField.newString(FormFieldType.TEXT, EnvironmentFields.DB_HOST)
			.setDescription("The server name of the database host.");
	
	private CFWField<Integer> dbPort = CFWField.newInteger(FormFieldType.NUMBER, EnvironmentFields.DB_PORT)
			.setDescription("The port used to access the database.");
	
	private CFWField<String> dbName = CFWField.newString(FormFieldType.TEXT, EnvironmentFields.DB_NAME)
			.setDescription("The name of the user for accessing the database.");
	
	private CFWField<String> dbUser = CFWField.newString(FormFieldType.TEXT, EnvironmentFields.DB_USER)
			.setDescription("The name of the user for accessing the database.");
	
	private CFWField<String> dbPassword = CFWField.newString(FormFieldType.PASSWORD, EnvironmentFields.DB_PASSWORD)
			.setDescription("The password of the DB user.");
	
	public EnvironmentSPM() {
		initializeFields();
	}
		
	private void initializeFields() {
		this.addFields(url, apiUser, apiUserPassword, dbHost, dbPort, dbName, dbUser, dbPassword);
	}
		
			
	public String name() {
		return url.getValue();
	}
	
	public EnvironmentSPM name(String name) {
		this.url.setValue(name);
		return this;
	}
		
}
