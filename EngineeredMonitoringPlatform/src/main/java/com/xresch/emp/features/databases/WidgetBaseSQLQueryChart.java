package com.xresch.emp.features.databases;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

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
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.features.dashboard.DashboardWidget;
import com.xresch.cfw.features.dashboard.WidgetDefinition;
import com.xresch.cfw.features.dashboard.WidgetSettingsFactory;
import com.xresch.cfw.features.dashboard.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject;
import com.xresch.cfw.features.jobs.CFWJobsAlertObject.AlertType;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.utils.CFWConditions;
import com.xresch.cfw.utils.CFWConditions.ThresholdCondition;
import com.xresch.cfw.validation.CustomValidator;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public abstract class WidgetBaseSQLQueryChart extends WidgetDefinition {

	private static final String FIELDNAME_SERIESCOLUMNS = "seriescolumns";
	private static final String FIELDNAME_XCOLUMN = "xcolumn";
	private static final String FIELDNAME_YCOLUMN = "ycolumn";
	private static final String FIELDNAME_SQLQUERY = "sqlquery";
	
	private String FIELDNAME_ENVIRONMENT = this.createEnvironmentSelectorField().getName();
		
	private static Logger logger = CFWLog.getLogger(WidgetBaseSQLQueryChart.class.getName());

	
	@SuppressWarnings("rawtypes")
	public abstract CFWField createEnvironmentSelectorField();
	
	public abstract DBInterface getDatabaseInterface(String environmentID);
	
	@Override
	public WidgetDataCachePolicy getCachePolicy() {
		return WidgetDataCachePolicy.TIMEPRESET_BASED;
	}
	
	@Override
	public CFWObject getSettings() {
		
		return createQueryFields()						
				.addAllFields(WidgetSettingsFactory.createDefaultChartFields(true, false))
				.addField(WidgetSettingsFactory.createSampleDataField())
									
		;
	}
	
	public CFWObject createQueryFields() {
		return new CFWObject()
				.addField( this.createEnvironmentSelectorField())
				
				.addField(CFWField.newString(FormFieldType.TEXTAREA, FIELDNAME_SQLQUERY)
						.setLabel("{!emp_widget_database_sqlquery!}")
						.setDescription("{!emp_widget_database_sqlquery_desc!}")
						.disableSanitization() // Do not convert character like "'" to &#x27; etc...
						.setValue("")
				)
				
				.addField(CFWField.newString(FormFieldType.TEXT, FIELDNAME_SERIESCOLUMNS)
						.setLabel("{!emp_widget_database_seriescolumns!}")
						.setDescription("{!emp_widget_database_seriescolumns_desc!}")
				)
				
				.addField(CFWField.newString(FormFieldType.TEXT, FIELDNAME_XCOLUMN)
						.setLabel("{!emp_widget_database_xcolumn!}")
						.setDescription("{!emp_widget_database_xcolumn_desc!}")
				)
				
				.addField(CFWField.newString(FormFieldType.TEXT, FIELDNAME_YCOLUMN)
						.setLabel("{!emp_widget_database_ycolumn!}")
						.setDescription("{!emp_widget_database_ycolumn_desc!}")
				)
				;
	}
	
	/*********************************************************************
	 * 
	 *********************************************************************/
	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings, long earliest, long latest) { 
		//---------------------------------
		// Example Data
		Boolean isSampleData = (Boolean)settings.getField(WidgetSettingsFactory.FIELDNAME_SAMPLEDATA).getValue();
		if(isSampleData != null && isSampleData) {
			response.setPayLoad(createSampleData(earliest, latest));
			return;
		}
		//---------------------------------
		// Real Data		
		response.setPayLoad(loadDataFromDBInferface(settings));
		
	}
	
	/*********************************************************************
	 * 
	 * @param latest time in millis of which to fetch the data.
	 *********************************************************************/
	@SuppressWarnings("unchecked")
	public JsonArray loadDataFromDBInferface(CFWObject widgetSettings){
		
		//---------------------------------
		// Resolve Query		
		String sqlQueryString = (String)widgetSettings.getField(FIELDNAME_SQLQUERY).getValue();
		
		if(Strings.isNullOrEmpty(sqlQueryString)) {
			return null;
		}
		
		//---------------------------------
		// Get Environment
		String environmentID = (String)widgetSettings.getField(FIELDNAME_ENVIRONMENT).getValue();		

		//---------------------------------
		// Get DB
		DBInterface db =  this.getDatabaseInterface(environmentID);
		
		if(db == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "Database Query Status: The chosen environment seems not configured correctly.");
			return null;
		}
			
		//---------------------------------
		// Fetch Data
		JsonArray resultArray = new JsonArray();

		ResultSet result = db.preparedExecuteQuery(sqlQueryString);
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
								
							case Types.DATE:
							case Types.TIME:
							case Types.TIME_WITH_TIMEZONE:
							case Types.TIMESTAMP:
							case Types.TIMESTAMP_WITH_TIMEZONE:
								object.addProperty(propertyName, result.getTimestamp(i).getTime());
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
		
		return resultArray;
		
	}
	
	public JsonArray createSampleData( long earliest, long latest) { 	
		return CFW.Random.randomJSONArrayOfSeriesData(3,24, earliest, latest);
	}
	
//	@Override
//	public ArrayList<FileDefinition> getJavascriptFiles() {
//		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
//		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDatabases.PACKAGE_RESOURCE, "emp_widget_mysqlquerystatus.js") );
//		return array;
//	}

	@Override
	public ArrayList<FileDefinition> getCSSFiles() {
		return null;
	}
	
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDatabases.PACKAGE_RESOURCE, "emp_widget_database_common.js") );
		return array;
	}

	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		// doesn'twork, get's overriden
		//map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureDatabases.PACKAGE_RESOURCE, "lang_en_emp_database.properties"));
		return map;
	}
	
}


