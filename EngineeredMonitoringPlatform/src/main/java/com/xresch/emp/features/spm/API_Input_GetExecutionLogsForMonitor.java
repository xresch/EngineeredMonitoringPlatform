package com.xresch.emp.features.spm;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;


/********************************************************************
 *
 ********************************************************************/
public class API_Input_GetExecutionLogsForMonitor extends API_Input_EnvTimeframeProject {
	
	private CFWField<String> monitorName = CFWField.newString(FormFieldType.TEXT, "MONITOR_NAME")
			.setDescription("The name of the monitor.")
			.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
				@Override
				public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
					String environmentID = request.getParameter("ENVIRONMENT_ID");
					if(Strings.isNullOrEmpty(environmentID)) {
						CFW.Messages.addInfoMessage("Select an environment to get suggestions.");
						return null;
					}
					return EnvironmentManagerSPM.autocompleteMonitorName(Integer.parseInt(environmentID), searchValue, this.getMaxResults());
				}
			});
	
	public API_Input_GetExecutionLogsForMonitor() {
		this.addField(monitorName);
	}
	
	public String monitorName() {
		return monitorName.getValue();
	}
	
	public API_Input_GetExecutionLogsForMonitor monitorName(String value) {
		this.monitorName.setValue(value);
		return this;
	}
	
}