package com.xresch.emp.features.spm;

import java.sql.Timestamp;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.validation.NotNullOrEmptyValidator;

/********************************************************************
 *
 ********************************************************************/
public class API_Input_EnvTimeframeProject extends API_Input_Environment {
	
	private CFWField<Timestamp> earliestTime = CFWField.newTimestamp(FormFieldType.DATETIMEPICKER, "EARLIEST_TIME")
			.setDescription("The unix time(milliseconds since 01.01.1970 UTC) of the earliest time included in the results.")
			.addValidator(new NotNullOrEmptyValidator());		
	
	private CFWField<Timestamp> latestTime = CFWField.newTimestamp(FormFieldType.DATETIMEPICKER, "LATEST_TIME")
			.setDescription("The unix time(milliseconds since 01.01.1970 UTC) of the earliest time included in the results.")
			.addValidator(new NotNullOrEmptyValidator());
				
	private CFWField<Integer> projectID = CFWField.newInteger(FormFieldType.TEXT, "PROJECT_ID")
			.setDescription("The id of the project.")
			.setValue(0)
			.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
				@Override
				public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue) {
					String envID = request.getParameter("ENVIRONMENT_ID");
					if(Strings.isNullOrEmpty(envID)) {
						CFW.Context.Request.addAlertMessage(MessageType.INFO, "Select an environment to get suggestions.");
						return null;
					}
					return EnvironmentManagerSPM.autocompleteProjects(Integer.parseInt(envID), searchValue, this.getMaxResults());
				}
			});
	
	public API_Input_EnvTimeframeProject() {
		this.addFields(earliestTime, latestTime, projectID);
	}
	
	public Timestamp earliestTime() {
		return earliestTime.getValue();
	}
	
	public API_Input_EnvTimeframeProject earliestTime(Timestamp value) {
		this.earliestTime.setValue(value);
		return this;
	}
	
	public Timestamp latestTime() {
		return latestTime.getValue();
	}
	
	public API_Input_EnvTimeframeProject latestTime(Timestamp value) {
		this.latestTime.setValue(value);
		return this;
	}
		
	public Integer projectID() {
		return projectID.getValue();
	}
	
	public API_Input_EnvTimeframeProject projectID(Integer value) {
		this.projectID.setValue(value);
		return this;
	}
	
}