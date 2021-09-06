package com.xresch.emp.features.mssql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.features.dashboard.WidgetDefinition;
import com.xresch.cfw.features.dashboard.WidgetSettingsFactory;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.validation.CustomValidator;

public class WidgetMSSQLQueryStatus extends WidgetDefinition {

	private static Logger logger = CFWLog.getLogger(WidgetMSSQLQueryStatus.class.getName());
	@Override
	public String getWidgetType() {return "emp_mssqlquerystatus";}

	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				.addField( MSSQLSettingsFactory.createEnvironmentSelectorField() )
				
				.addField(CFWField.newString(FormFieldType.TEXTAREA, "sqlquery")
						.setLabel("{!emp_widget_mssqlquerystatus_sqlquery!}")
						.setDescription("{!emp_widget_mssqlquerystatus_sqlquery_desc!}")
						.setValue("")
						.addValidator(new CustomValidator() {
							
							@Override
							public boolean validate(Object value) {
								
								if(value == null) {
									return true;
								}
								
								if(Strings.isNullOrEmpty(value.toString()) || value.toString().trim().toLowerCase().startsWith("select")) {
									return true;
								}else {
									CFW.Messages.addErrorMessage("Query has to be a select statement.");
									return false;
								}
							}
						})
				)
				
				.addField(CFWField.newString(FormFieldType.TEXT, "valuecolumn")
						.setLabel("{!emp_widget_mssqlquerystatus_valuecolumn!}")
						.setDescription("{!emp_widget_mssqlquerystatus_valuecolumn_desc!}")
				)
				
				.addField(CFWField.newString(FormFieldType.TEXT, "labelcolumns")
						.setLabel("{!emp_widget_mssqlquerystatus_labelcolumns!}")
						.setDescription("{!emp_widget_mssqlquerystatus_labelcolumns_desc!}")
				)
				
				.addField(CFWField.newString(FormFieldType.TEXT, "detailcolumns")
						.setLabel("{!emp_widget_mssqlquerystatus_detailcolumns!}")
						.setDescription("{!emp_widget_mssqlquerystatus_detailcolumns_desc!}")
				)

				.addAllFields(WidgetSettingsFactory.createThresholdFields())
										
				.addField(WidgetSettingsFactory.createDefaultDisplayAsField())				
				.addAllFields(WidgetSettingsFactory.createTilesSettingsFields())
				.addField(WidgetSettingsFactory.createDisableBoolean())
				.addField(WidgetSettingsFactory.createSampleDataField())
									
		;
	}
		
	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings, long earliest, long latest) { 
		//---------------------------------
		// Example Data
		JsonElement sampleDataElement = jsonSettings.get("sampledata");
		
		if(sampleDataElement != null 
		&& !sampleDataElement.isJsonNull() 
		&& sampleDataElement.getAsBoolean()) {
			createSampleData(response);
			return;
		}
		
		//---------------------------------
		// Resolve Jobnames
		JsonElement sqlQueryElement = jsonSettings.get("sqlquery");
		if(sqlQueryElement.isJsonNull() || sqlQueryElement.getAsString().isEmpty()) {
			return;
		}
		
		String  sqlQueryString = sqlQueryElement.getAsString();
		if(sqlQueryString.isEmpty()) {
			return;
		}

		//---------------------------------
		// Get Environment & DB
		
		JsonElement environmentElement = jsonSettings.get("environment");
		if(environmentElement.isJsonNull()) {
			return;
		}
		MSSQLEnvironment environment = MSSQLEnvironmentManagement.getEnvironment(environmentElement.getAsInt());

		//---------------------------------
		// Get DB
		DBInterface db = environment.getDBInstance();
		
		if(db == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "MSSQL Query Status: The chosen environment seems not configured correctly.");
			return;
		}
			
		//---------------------------------
		// Fetch Data
		JsonArray resultArray = new JsonArray();

		ResultSet result = db.preparedExecuteQuerySilent(sqlQueryString);
		try {

			if(result != null) {
			
				while(result.next()){
				
					JsonObject object = new JsonObject();
					
					ResultSetMetaData metadata = result.getMetaData();
					int columnCount = metadata.getColumnCount();
					
					for(int i = 1; i <= columnCount; i++) {
						int type = metadata.getColumnType(i);
						String propertyName = metadata.getColumnLabel(i);
						
						switch(type) {
						
							case Types.SMALLINT:
							case Types.INTEGER:
							case Types.BIGINT:
								object.addProperty(propertyName, result.getInt(i));
								break;
								
							case Types.DECIMAL:
								object.addProperty(propertyName, result.getBigDecimal(i));
								break;	
								
							case Types.DOUBLE:
								object.addProperty(propertyName, result.getDouble(i));
								break;	
							
							case Types.FLOAT:
								object.addProperty(propertyName, result.getFloat(i));
								break;	
								
							default: object.addProperty(propertyName, result.getString(i));
						}
						
						
					}
					
					resultArray.add(object);
				}
			}		
			
		} catch (SQLException e) {
			new CFWLog(logger)
				.severe("Error fetching Widget data.", e);
		}finally {
			db.close(result);
		}
		
		response.getContent().append(resultArray.toString());
		
	}
	
	public void createSampleData(JSONResponse response) { 
		
		long currentTime = new Date().getTime();
		
		JsonArray array = new JsonArray();
				
		for(int i = 0; i < 12; i++) {
			
			JsonObject object = new JsonObject();
			
			object.addProperty("FIRSTNAME", CFW.Random.randomFirstnameOfGod());
			object.addProperty("LASTNAME", CFW.Random.randomLastnameSweden());
			object.addProperty("LOCATION", CFW.Random.randomMythicalLocation());
			object.addProperty("ID", CFW.Random.randomStringAlphaNumerical(16));
			object.addProperty("TIME", currentTime-(CFW.Random.randomIntegerInRange(100, 10000)*1000000) );
			object.addProperty("VALUE", CFW.Random.randomIntegerInRange(1, 100));
			
			array.add(object);
		}
		
		response.getContent().append(CFW.Random.randomJSONArrayOfMightyPeople(12).toString());

	}
	
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureMSSQL.PACKAGE_RESOURCE, "emp_widget_mssqlquerystatus.js") );
		return array;
	}

	@Override
	public ArrayList<FileDefinition> getCSSFiles() {
		return null;
	}

	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureMSSQL.PACKAGE_RESOURCE, "lang_en_emp_mssql.properties"));
		return map;
	}
	
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureMSSQL.PERMISSION_WIDGETS_MYSQL);
	}

}
