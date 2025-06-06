package com.xresch.emp.features.awa;

import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.core.CFWAutocompleteHandler;
import com.xresch.cfw.features.parameter.ParameterDefinition;

public class ParameterDefinitionAWAJobname extends ParameterDefinition {

	public static final String LABEL = "AWA Jobnames";
	
	/***************************************************************
	 * 
	 ***************************************************************/
	@Override
	public String getParamUniqueName() { return LABEL; }

	/***************************************************************
	 * 
	 ***************************************************************/
	@Override
	public String descriptionShort() {
		return "Allows a user to select a AWA jobname with auto-complete while typing.";
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureAWA.PACKAGE_MANUAL, "parameter_"+LABEL.toLowerCase().replace(" ", "_")+".html");
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public CFWField getFieldForSettings(HttpServletRequest request, String dashboardid, Object fieldValue) {
		CFWField settingsField = CFWField.newString(FormFieldType.TAGS, LABEL)
				.allowHTML(true)
				.setAutocompleteHandler(new CFWAutocompleteHandler(10) {
					
					@Override
					public AutocompleteResult getAutocompleteData(HttpServletRequest request, String searchValue, int cursorPosition) {
						String environmentID = request.getParameter("environment");
						
						if(Strings.isNullOrEmpty(environmentID)) {
							CFW.Messages.addInfoMessage("Please choose an environment first.");
							return null;
						}
						
						;
						
						return AWAEnvironmentManagement.autocompleteJobname(Integer.parseInt(environmentID), searchValue, this.getMaxResults());
					}
				});
				
		
		if(fieldValue !=null) {
			settingsField.setValueConvert(fieldValue, true);
		}
	
		return settingsField;
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	@SuppressWarnings({ "rawtypes" })
	@Override
	public CFWField getFieldForWidget(HttpServletRequest request, String dashboardid, Object parameterValue, CFWTimeframe timeframe, JsonObject userSelectedParamValues) {

		return getFieldForSettings(request, dashboardid, parameterValue);
	}
	
	/***************************************************************
	 * 
	 ***************************************************************/
	@Override
	public boolean isDynamic() {
		return false;
	}
	
}
