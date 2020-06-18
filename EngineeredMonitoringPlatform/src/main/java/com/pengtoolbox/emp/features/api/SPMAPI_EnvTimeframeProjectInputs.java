package com.pengtoolbox.emp.features.api;

import java.sql.Timestamp;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.datahandling.CFWAutocompleteHandler;
import com.pengtoolbox.cfw.datahandling.CFWField;
import com.pengtoolbox.cfw.datahandling.CFWField.FormFieldType;
import com.pengtoolbox.cfw.datahandling.CFWObject;
import com.pengtoolbox.cfw.response.bootstrap.AlertMessage.MessageType;
import com.pengtoolbox.cfw.validation.NotNullOrEmptyValidator;
import com.pengtoolbox.emp.features.environments.SPMEnvironmentManagement;

/********************************************************************
 *
 ********************************************************************/
public class SPMAPI_EnvTimeframeProjectInputs extends CFWObject {
	
	private CFWField<Timestamp> earliestTime = CFWField.newTimestamp(FormFieldType.DATETIMEPICKER, "EARLIEST_TIME")
			.setDescription("The unix time(milliseconds since 01.01.1970 UTC) of the earliest time included in the results.")
			.addValidator(new NotNullOrEmptyValidator());
			//.setValue(new Timestamp(System.currentTimeMillis()-1440*60000));
	
	private CFWField<Timestamp> latestTime = CFWField.newTimestamp(FormFieldType.DATETIMEPICKER, "LATEST_TIME")
			.setDescription("The unix time(milliseconds since 01.01.1970 UTC) of the earliest time included in the results.")
			.addValidator(new NotNullOrEmptyValidator());
			//.setValue(new Timestamp(System.currentTimeMillis()));
	
	private CFWField<Integer> environmentID = CFWField.newInteger(FormFieldType.SELECT, "ENVIRONMENT_ID")
			.setDescription("The id of the environment.")
			.setValue(0)
			.setOptions(SPMEnvironmentManagement.getEnvironmentsAsSelectOptions());
			
	private CFWField<Integer> projectID = CFWField.newInteger(FormFieldType.TEXT, "PROJECT_ID")
			.setDescription("The id of the project.")
			.setValue(0)
			.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
				@Override
				public LinkedHashMap<Object, Object> getAutocompleteData(HttpServletRequest request, String searchValue) {
					String environmentID = request.getParameter("ENVIRONMENT_ID");
					if(Strings.isNullOrEmpty(environmentID)) {
						CFW.Context.Request.addAlertMessage(MessageType.INFO, "Select an environment to get suggestions.");
						return null;
					}
					return SPMEnvironmentManagement.autocompleteProjects(Integer.parseInt(environmentID), searchValue, this.getMaxResults());
				}
			});
	
	public SPMAPI_EnvTimeframeProjectInputs() {
		this.addFields(earliestTime, latestTime, environmentID, projectID);
	}
	
	public Timestamp earliestTime() {
		return earliestTime.getValue();
	}
	
	public SPMAPI_EnvTimeframeProjectInputs earliestTime(Timestamp value) {
		this.earliestTime.setValue(value);
		return this;
	}
	
	public Timestamp latestTime() {
		return latestTime.getValue();
	}
	
	public SPMAPI_EnvTimeframeProjectInputs latestTime(Timestamp value) {
		this.latestTime.setValue(value);
		return this;
	}
	
	public Integer environmentID() {
		return environmentID.getValue();
	}
	
	public SPMAPI_EnvTimeframeProjectInputs environmentID(Integer value) {
		this.environmentID.setValue(value);
		return this;
	}
	
	public Integer projectID() {
		return projectID.getValue();
	}
	
	public SPMAPI_EnvTimeframeProjectInputs projectID(Integer value) {
		this.projectID.setValue(value);
		return this;
	}
	
}