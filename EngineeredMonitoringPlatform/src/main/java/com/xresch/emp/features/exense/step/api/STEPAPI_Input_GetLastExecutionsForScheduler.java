package com.xresch.emp.features.exense.step.api;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.utils.CFWTime.CFWTimeUnit;
import com.xresch.emp.features.exense.step.StepEnvironment;
import com.xresch.emp.features.exense.step.StepEnvironmentManagement;

/********************************************************************
 *
 ********************************************************************/
public class STEPAPI_Input_GetLastExecutionsForScheduler extends STEPAPI_Input_Environment{
	
	private CFWField<String> schedulerID = CFWField.newString(FormFieldType.TEXT, STEPAPI_Factory.INPUT_SCHEDULER_ID)
			.setDescription("The id of the monitor.")
			.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
				@Override
				public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
					String environmentID = request.getParameter(STEPAPI_Factory.INPUT_ENVIRONMENT);
					if(Strings.isNullOrEmpty(environmentID)) {
						CFW.Context.Request.addAlertMessage(MessageType.INFO, "Select an environment to get suggestions.");
						return null;
					}
					StepEnvironment environment = StepEnvironmentManagement.getEnvironment( Integer.parseInt(environmentID) );
					return environment.autocompleteSchedulers(searchValue, 10);
				}
			});
	

	private CFWField<CFWTimeframe> timeframe = 
			CFWField.newTimeframe(STEPAPI_Factory.INPUT_TIMEFRAME)
			.setLabel("Timeframe")
			.setDescription("The timeframe that should be fetched. If you set an offset, earliest and latest is ignored. The value clientTimezoneOffset is ignored.")
			.setValue(
				new CFWTimeframe()
					.setOffset(1, CFWTimeUnit.h)
			);
	
	private CFWField<Integer> executions = 
			CFWField.newInteger(FormFieldType.NUMBER, STEPAPI_Factory.INPUT_EXECUTIONS)
			.setDescription("The number of executions that should be fetched.")
			.setValue(1);
	
	public STEPAPI_Input_GetLastExecutionsForScheduler() {
		this.addFields(schedulerID, executions, timeframe);
	}
	
	public String schedulerID() {
		return schedulerID.getValue();
	}
	
	public Integer executions() {
		return executions.getValue();
	}
	
	public CFWTimeframe timeframe() {
		return timeframe.getValue();
	}

		
}