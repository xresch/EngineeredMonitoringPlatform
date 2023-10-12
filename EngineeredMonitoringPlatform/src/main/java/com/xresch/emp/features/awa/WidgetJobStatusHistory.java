package com.xresch.emp.features.awa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
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
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.features.dashboard.widgets.WidgetDefinition;
import com.xresch.cfw.features.dashboard.widgets.WidgetSettingsFactory;
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.validation.IntegerValidator;
import com.xresch.emp.features.common.FeatureEMPCommon;

public class WidgetJobStatusHistory extends WidgetDefinition {

	private static Logger logger = CFWLog.getLogger(WidgetJobStatusHistory.class.getName());
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String getWidgetType() {return "emp_awajobstatus_history";}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public WidgetDataCachePolicy getCachePolicy() {
		return WidgetDataCachePolicy.ALWAYS;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String widgetCategory() {
		return FeatureAWA.WIDGET_CATEGORY_AWA;
	}

	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String widgetName() { return "Job Status History"; }
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureAWA.PACKAGE_MANUAL, "widget_"+getWidgetType()+".html");
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public CFWObject getSettings() {
		return new CFWObject()
				
				.addField(AWASettingsFactory.createEnvironmentSelectorField())
				
				
				.addField(CFWField.newString(FormFieldType.TEXTAREA, "jobnames")
						.setLabel("{!emp_widget_awajobstatus_jobnames!}")
						.setDescription("{!emp_widget_awajobstatus_jobnames_desc!}")
						.setValue("")			
				)
				
				.addField(CFWField.newString(FormFieldType.TEXTAREA, "joblabels")
						.setLabel("{!emp_widget_awajobstatus_joblabels!}")
						.setDescription("{!emp_widget_awajobstatus_joblabels_desc!}")
						.setValue("")
						
				)
				
				.addField(CFWField.newInteger(FormFieldType.TEXT, "statuscount")
					.setLabel("{!emp_widget_awajobstatus_statuscount!}")
					.setDescription("{!emp_widget_awajobstatus_statuscount_desc!}")
					.setValue(7)
					.addValidator(new IntegerValidator().setNullAllowed(false))
				)
							
				.addField(CFWField.newString(FormFieldType.SELECT, "sizefactor")
						.setLabel("{!cfw_widget_tilessizefactor!}")
						.setDescription("{!cfw_widget_tilessizefactor_desc!}")
						.setOptions(new String[]{"0.25", "0.5", "0.75", "1", "1.25", "1.5", "1.75", "2.0", "2.5", "3.0"})
						.setValue("1")
				)
				
				.addField(CFWField.newString(FormFieldType.SELECT, "borderstyle")
						.setLabel("{!cfw_widget_tilesborderstyle!}")
						.setDescription("{!cfw_widget_tilesborderstyle_desc!}")
						.setOptions(new String[]{"None", "Round", "Superround", "Asymmetric", "Superasymmetric", "Ellipsis"})
						.setValue("None")
				)
				
				.addField(CFWField.newBoolean(FormFieldType.BOOLEAN, "showstatistics")
						.setLabel("{!emp_widget_awajobstatus_showstatistics!}")
						.setDescription("{!emp_widget_awajobstatus_showstatistics_desc!}")
						.setValue(true)
				)
				
				.addField(CFWField.newBoolean(FormFieldType.BOOLEAN, "showsparkline")
						.setLabel("{!emp_widget_awajobstatus_showsparkline!}")
						.setDescription("{!emp_widget_awajobstatus_showsparkline_desc!}")
						.setValue(true)
				)
				
				.addField(CFWField.newBoolean(FormFieldType.BOOLEAN, "showlabels")
						.setLabel("{!emp_widget_awajobstatus_showlabels!}")
						.setDescription("{!emp_widget_awajobstatus_showlabels_desc!}")
						.setValue(true)
				)
								
				
				.addField(WidgetSettingsFactory.createDisableBoolean())
				.addField(WidgetSettingsFactory.createSampleDataField())
	
		;
	}
	
	/************************************************************
	 * 
	 ************************************************************/	
	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings, CFWTimeframe timeframe) { 
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
		JsonElement jobnamesElement = jsonSettings.get("jobnames");
		if(jobnamesElement.isJsonNull() || jobnamesElement.getAsString().isEmpty()) {
			return;
		}
		
		String  jobnamesString = jobnamesElement.getAsString();
		if(jobnamesString.isEmpty()) {
			return;
		}
		String[] jobnames = jobnamesString.trim().split("[,\t\r\n]+");

		//---------------------------------
		// Resolve Joblabels
		JsonElement joblabelsElement = jsonSettings.get("joblabels");
		String[] joblabels = null;
		if(!joblabelsElement.isJsonNull() && !joblabelsElement.getAsString().isEmpty()) {
			joblabels = joblabelsElement.getAsString().trim().split("[,\t\r\n]+");
		}
		
		//---------------------------------
		// Resolve Status Count
		JsonElement statuscountElement = jsonSettings.get("statuscount");
		Integer statuscount = statuscountElement.getAsInt();
		//---------------------------------
		// Get Environment & DB
		
		JsonElement environmentElement = jsonSettings.get("environment");
		if(environmentElement.isJsonNull()) {
			return;
		}
		AWAEnvironment environment = AWAEnvironmentManagement.getEnvironment(environmentElement.getAsInt());

		//---------------------------------
		// Get DB
		DBInterface db = environment.getDBInstance();
		
		if(db == null) {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "AWA Job Status: The chosen environment seems not configured correctly.");
			return;
		}
			
		//---------------------------------
		// Fetch Data
		JsonArray resultArray = new JsonArray();
		for(int i = 0; i < jobnames.length; i++) {

			String currentJobname = jobnames[i].trim();
			ResultSet result = db.preparedExecuteQuerySilent(
					CFW.Files.readPackageResource(FeatureAWA.PACKAGE_RESOURCE, "emp_awa_last_jobstatus_history.sql"),
					environment.clientID(),
					currentJobname,
					statuscount);
			try {
				
				if(result != null) { 
					while(result.next()){
						JsonObject object = new JsonObject();
						object.addProperty("JOBNAME", currentJobname);
						
						int runID = result.getInt("ID");
						String type = result.getString("TYPE");
						String URL = environment.getJobWorkflowURL(currentJobname, type, runID);
						
						
						if(joblabels != null && i < joblabels.length) {
							object.addProperty("LABEL", joblabels[i]);
						}else {
							String title =  result.getString("TITLE");
							if(!Strings.isNullOrEmpty(title)) {
								object.addProperty("LABEL", title);
							}else {
								object.addProperty("LABEL", currentJobname);
							}
						}
						
						OffsetDateTime startTime = result.getObject("START_TIME", OffsetDateTime.class);
						OffsetDateTime endTime = result.getObject("END_TIME", OffsetDateTime.class);
	
						object.addProperty("STATUS", result.getString("STATUS"));
						object.addProperty("CLIENT_ID", result.getString("CLIENT_ID"));
						object.addProperty("TYPE", result.getString("TYPE"));
						object.addProperty("START_TIME", (startTime != null) ? startTime.toInstant().toEpochMilli() : null );
						object.addProperty("END_TIME", (endTime != null) ? endTime.toInstant().toEpochMilli() : null);
						object.addProperty("HOST_DESTINATION", result.getString("HOST_DESTINATION"));
						object.addProperty("HOST_SOURCE", result.getString("HOST_SOURCE"));
						object.addProperty("DURATION_SECONDS", result.getInt("DURATION_SECONDS"));
						object.addProperty("STATUS_CODE", result.getInt("STATUS_CODE"));
						object.addProperty("URL", URL);
						resultArray.add(object);
					}
				}else {
					JsonObject object = new JsonObject();
					object.addProperty("JOBNAME", jobnames[i]);
					
					if(joblabels != null && i < joblabels.length) {
						object.addProperty("LABEL", joblabels[i]);
					}else {
						object.addProperty("LABEL", jobnames[i]);
					}
					object.addProperty("status", "UNKNOWN");
					resultArray.add(object);
				}
				
				
				
			} catch (SQLException e) {
				new CFWLog(logger)
					.severe("Error fetching Widget data.", e);
			}finally {
				db.close(result);
			}
		}
		
		response.getContent().append(resultArray.toString());
		
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	public void createSampleData(JSONResponse response) { 
		
		long currentTime = new Date().getTime();
		int i = 2;
		response.getContent().append("["
			+ "{ \"JOBNAME\":\"JP_0003_225\", \"LABEL\":\"JP_0003_225\", \"STATUS\":\"RUNNING\", \"END_TIME\": null, \"DURATION_SECONDS\": 0},"
			+ "{ \"JOBNAME\":\"JP_0003_225\", \"LABEL\":\"JP_0003_225\", \"STATUS\":\"ENDED OK\", \"DURATION_SECONDS\": 1234, \"END_TIME\":"+((currentTime-(120*60000))+(1000*i++))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_225\", \"LABEL\":\"JP_0003_225\", \"STATUS\":\"ABNORMAL ENDING\", \"DURATION_SECONDS\": 123, \"END_TIME\":"+((currentTime-(120*60000))+(1000*i++))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_225\", \"LABEL\":\"JP_0003_225\", \"STATUS\":\"ENDED OK\", \"DURATION_SECONDS\": 12354, \"END_TIME\":"+((currentTime-(120*60000))+(1000*i++))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_225\", \"LABEL\":\"JP_0003_225\", \"STATUS\":\"ABNORMAL ENDING\", \"DURATION_SECONDS\": 15234, \"END_TIME\":"+((currentTime-(120*60000))+(1000*i++))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_225\", \"LABEL\":\"JP_0003_225\", \"STATUS\":\"ENDED OK\", \"DURATION_SECONDS\": 51234, \"END_TIME\":"+((currentTime-(120*60000))+(1000*i++))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_225\", \"LABEL\":\"JP_0003_225\", \"STATUS\":\"ENDED OK\", \"DURATION_SECONDS\": 71234, \"END_TIME\":"+((currentTime-(120*60000))+(1000*i++))+"},"
			+ "{ \"JOBNAME\":\"JP_0002_B\", \"LABEL\":\"Job B\", \"STATUS\":\"ENDED OK\", \"DURATION_SECONDS\": 81234, \"END_TIME\":"+((currentTime-(120*60000))+(1000*i++))+"},"
			+ "{ \"JOBNAME\":\"JP_0002_B\", \"LABEL\":\"Job B\", \"STATUS\":\"ENDED OK\", \"DURATION_SECONDS\": 91234, \"END_TIME\":"+((currentTime-(120*60000))+(1000*i++))+"},"
			+ "{ \"JOBNAME\":\"JP_0002_B\", \"LABEL\":\"Job B\", \"STATUS\":\"ENDED OK\", \"DURATION_SECONDS\": 12934, \"END_TIME\":"+((currentTime-(120*60000))+(1000*i++))+"},"
			+ "{ \"JOBNAME\":\"JP_0002_B\", \"LABEL\":\"Job B\", \"STATUS\":\"RUNNING\", \"DURATION_SECONDS\": 123477, \"END_TIME\":"+((currentTime-(120*60000))+(1000*i++))+"},"
			+ "{ \"JOBNAME\":\"JP_0002_B\", \"LABEL\":\"Job B\", \"STATUS\":\"ENDED OK\", \"DURATION_SECONDS\": 331234, \"END_TIME\":"+((currentTime-(120*60000))+(1000*i++))+"},"
			+ "{ \"JOBNAME\":\"JP_0002_B\", \"LABEL\":\"Job B\", \"STATUS\":\"ENDED OK\", \"DURATION_SECONDS\": 1256734, \"END_TIME\":"+((currentTime-(120*60000))+(1000*i++))+"},"
			+ "{ \"JOBNAME\":\"JP_0002_B\", \"LABEL\":\"Job B\", \"STATUS\":\"ENDED OK\", \"DURATION_SECONDS\": 12934, \"END_TIME\":"+((currentTime-(120*60000))+(1000*i++))+"},"
			+ "{ \"JOBNAME\":\"JP_8008_88\", \"LABEL\":\"Crazy Job\", \"STATUS\":\"RUNNING\", \"END_TIME\": null},"
			+ "{ \"JOBNAME\":\"JP_8008_88\", \"LABEL\":\"Crazy Job\", \"STATUS\":\"ABNORMAL ENDING\", \"DURATION_SECONDS\": 3214, \"END_TIME\": null},"
			+ "{ \"JOBNAME\":\"JP_8008_88\", \"LABEL\":\"Crazy Job\", \"STATUS\":\"ABNORMAL ENDING\", \"DURATION_SECONDS\": 2143, \"END_TIME\":"+((currentTime-(120*60000))+(1000*i++))+"},"
			+ "{ \"JOBNAME\":\"JP_8008_88\", \"LABEL\":\"Crazy Job\", \"STATUS\":\"ABNORMAL ENDING\", \"DURATION_SECONDS\": 4444, \"END_TIME\":"+((currentTime-(120*60000))+(1000*i++))+"},"
			+ "{ \"JOBNAME\":\"JP_8008_88\", \"LABEL\":\"Crazy Job\", \"STATUS\":\"ABNORMAL ENDING\", \"DURATION_SECONDS\": 55, \"END_TIME\":"+((currentTime-(120*60000))+(1000*i++))+"},"
			+ "{ \"JOBNAME\":\"JP_8008_88\", \"LABEL\":\"Crazy Job\", \"STATUS\":\"ABNORMAL ENDING\", \"DURATION_SECONDS\": 9999, \"END_TIME\":"+((currentTime-(120*60000))+(1000*i++))+"},"
			+ "{ \"JOBNAME\":\"JP_8008_88\", \"LABEL\":\"Crazy Job\", \"STATUS\":\"ABNORMAL ENDING\", \"DURATION_SECONDS\": 8888, \"END_TIME\":"+((currentTime-(120*60000))+(1000*i++))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_C\", \"LABEL\":\"Some Very Long Label with blanks for breaks\", \"STATUS\":\"WAITING\", \"END_TIME\":"+((currentTime-(120*60000))+(1000*i++))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_C\", \"LABEL\":\"Some Very Long Label with blanks for breaks\", \"STATUS\":\"ENDED OK\", \"DURATION_SECONDS\": 222, \"END_TIME\":"+((currentTime-(120*60000))+(1000*i++))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_C\", \"LABEL\":\"Some Very Long Label with blanks for breaks\", \"STATUS\":\"ENDED OK\", \"DURATION_SECONDS\": 3333, \"END_TIME\":"+((currentTime-(120*60000))+(1000*i++))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_C\", \"LABEL\":\"Some Very Long Label with blanks for breaks\", \"STATUS\":\"ENDED OK\", \"DURATION_SECONDS\": 44444, \"END_TIME\":"+((currentTime-(120*60000))+(1000*i++))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_C\", \"LABEL\":\"Some Very Long Label with blanks for breaks\", \"STATUS\":\"ENDED OK\", \"DURATION_SECONDS\": 45678, \"END_TIME\":"+((currentTime-(120*60000))+(1000*i++))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_C\", \"LABEL\":\"Some Very Long Label with blanks for breaks\", \"STATUS\":\"ENDED OK\", \"DURATION_SECONDS\": 98765, \"END_TIME\":"+((currentTime-(120*60000))+(1000*i++))+"},"
			+ "{ \"JOBNAME\":\"JP_0003_C\", \"LABEL\":\"Some Very Long Label with blanks for breaks\", \"STATUS\":\"ENDED OK\", \"DURATION_SECONDS\": 54222, \"END_TIME\":"+((currentTime-(120*60000))+(1000*i++))+"}"
			+"]");

	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureAWA.PACKAGE_RESOURCE, "emp_awa_commonFunctions.js") );
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureAWA.PACKAGE_RESOURCE, "emp_widget_awajobstatus_history.js") );
		return array;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public ArrayList<FileDefinition> getCSSFiles() {
		return null;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureAWA.PACKAGE_RESOURCE, "lang_en_emp_awa.properties"));
		return map;
	}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public boolean hasPermission(User user) {
		return user.hasPermission(FeatureAWA.PERMISSION_WIDGETS_AWA);
	}

}
