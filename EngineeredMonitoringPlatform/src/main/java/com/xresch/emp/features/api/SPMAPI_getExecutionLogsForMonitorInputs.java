package com.xresch.emp.features.api;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.emp.features.environments.SPMEnvironmentManagement;

/********************************************************************
 *
 ********************************************************************/
public class SPMAPI_getExecutionLogsForMonitorInputs extends SPMAPI_EnvTimeframeProjectInputs {
	
	private CFWField<String> monitorName = CFWField.newString(FormFieldType.TEXT, "MONITOR_NAME")
			.setDescription("The name of the monitor.")
			.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
				@Override
				public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue) {
					String environmentID = request.getParameter("ENVIRONMENT_ID");
					if(Strings.isNullOrEmpty(environmentID)) {
						CFW.Context.Request.addAlertMessage(MessageType.INFO, "Select an environment to get suggestions.");
						return null;
					}
					return SPMEnvironmentManagement.autocompleteMonitorName(Integer.parseInt(environmentID), searchValue, this.getMaxResults());
				}
			});
	public SPMAPI_getExecutionLogsForMonitorInputs() {
		this.addField(monitorName);
	}
	
	public String monitorName() {
		return monitorName.getValue();
	}
	
	public SPMAPI_getExecutionLogsForMonitorInputs monitorName(String value) {
		this.monitorName.setValue(value);
		return this;
	}
	
}