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
public class API_Input_GetStatusForProject extends API_Input_Environment{
	
	private CFWField<Integer> projectID = CFWField.newInteger(FormFieldType.TEXT, "PROJECT_ID")
			.setDescription("The id of the project.")
			.setValue(0)
			.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
				@Override
				public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
					String envID = request.getParameter("ENVIRONMENT_ID");
					if(Strings.isNullOrEmpty(envID)) {
						CFW.Context.Request.addAlertMessage(MessageType.INFO, "Select an environment to get suggestions.");
						return null;
					}
					return EnvironmentManagerSPM.autocompleteProjects(Integer.parseInt(envID), searchValue, this.getMaxResults());
				}
			});
	
	private CFWField<String> measureName = CFWField.newString(FormFieldType.SELECT, "MEASURE")
			.setDescription("The measure to fetch.")
			.setOptions(new String[]{"Overall Health", "Availability", "Accuracy", "Performance"})
			.setValue("Overall Health");
	
	public API_Input_GetStatusForProject() {
		this.addFields(projectID, measureName);
	}
	
	public Integer projectID() {
		return projectID.getValue();
	}
	
	public API_Input_GetStatusForProject projectID(Integer value) {
		this.projectID.setValue(value);
		return this;
	}
	
	public String measureName() {
		return measureName.getValue();
	}
	
	public API_Input_GetStatusForProject measureName(String value) {
		this.measureName.setValue(value);
		return this;
	}
	
}