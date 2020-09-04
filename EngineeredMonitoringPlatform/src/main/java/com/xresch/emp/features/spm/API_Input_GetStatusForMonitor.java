package com.xresch.emp.features.spm;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/********************************************************************
 *
 ********************************************************************/
public class API_Input_GetStatusForMonitor extends API_Input_Environment{
	
	private CFWField<Integer> monitorID = CFWField.newInteger(FormFieldType.TEXT, "MONITOR_ID")
			.setDescription("The id of the monitor.")
			.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
				@Override
				public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue) {
					String environmentID = request.getParameter("ENVIRONMENT_ID");
					if(Strings.isNullOrEmpty(environmentID)) {
						CFW.Context.Request.addAlertMessage(MessageType.INFO, "Select an environment to get suggestions.");
						return null;
					}
					return EnvironmentManagerSPM.autocompleteMonitors(Integer.parseInt(environmentID), searchValue, this.getMaxResults());
				}
			});
	
	private CFWField<String> measureName = CFWField.newString(FormFieldType.SELECT, "MEASURE")
			.setDescription("The measure to fetch.")
			.setOptions(new String[]{"Overall Health", "Availability", "Accuracy", "Performance"})
			.setValue("Overall Health");
	
	public API_Input_GetStatusForMonitor() {
		this.addFields(monitorID, measureName);
	}
	
	public Integer monitorID() {
		return monitorID.getValue();
	}
	
	public API_Input_GetStatusForMonitor monitorID(Integer value) {
		this.monitorID.setValue(value);
		return this;
	}
	
	public String measureName() {
		return measureName.getValue();
	}
	
	public API_Input_GetStatusForMonitor measureName(String value) {
		this.measureName.setValue(value);
		return this;
	}
	
}