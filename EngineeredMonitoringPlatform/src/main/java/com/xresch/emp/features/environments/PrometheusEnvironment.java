package com.xresch.emp.features.environments;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.db.DBInterface;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.dashboard.DashboardWidget;
import com.xresch.cfw.features.dashboard.DashboardWidget.DashboardWidgetFields;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.utils.CFWHttp.CFWHttpResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class PrometheusEnvironment extends AbstractContextSettings {
	
	public static final String SETTINGS_TYPE = "Prometheus Environment";
	private String apiURL = null;
	
	public enum PrometheusEnvironmentFields{
		HOST,
		PORT,
		USE_HTTPS
	}
			
	private CFWField<String> host = CFWField.newString(FormFieldType.TEXT, PrometheusEnvironmentFields.HOST)
			.setDescription("The hostname of the prometheus instance.");
	
	private CFWField<Integer> port = CFWField.newInteger(FormFieldType.NUMBER, PrometheusEnvironmentFields.PORT)
			.setDescription("The port used to access the prometheus instance.");
	
	private CFWField<Boolean> useHttps = CFWField.newBoolean(FormFieldType.BOOLEAN, PrometheusEnvironmentFields.USE_HTTPS)
			.setDescription("Use HTTPS for calling the API.");
	
	public PrometheusEnvironment() {
		initializeFields();
	}
		
	private void initializeFields() {
		this.addFields(host, port, useHttps);
	}
		
			
	@Override
	public boolean isDeletable(int id) {

		int count = new DashboardWidget()
			.selectCount()
			.whereLike(DashboardWidgetFields.JSON_SETTINGS, "%\"environment\":"+id+"%")
			//.custom("AND \"TYPE\" LIKE 'emp_prometheus%'")
			.getCount();
		
		if(count == 0) {
			return true;
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The Prometheus environment cannot be deleted as it is still in use by "+count+"  widget(s).");
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
				.append("/api/v1");
			
			apiURL = builder.toString();
		}
		
		return apiURL;
	}
	
	public String host() {
		return host.getValue();
	}
	
	public PrometheusEnvironment host(String value) {
		this.host.setValue(value);
		return this;
	}
		
	public int port() {
		return port.getValue();
	}
	
	public PrometheusEnvironment port(int value) {
		this.port.setValue(value);
		return this;
	}
	
	public CFWHttpResponse query(String prometheusQuery) {
		
		String queryURL = getAPIUrlVersion1() + "/query?query="+CFW.HTTP.encode(prometheusQuery);
		System.out.println("queryURL: "+queryURL);
		return CFW.HTTP.sendGETRequest(queryURL);
	}
	
	
	
}
