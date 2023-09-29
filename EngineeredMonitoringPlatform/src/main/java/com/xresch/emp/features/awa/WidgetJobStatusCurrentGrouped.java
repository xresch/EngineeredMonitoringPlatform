package com.xresch.emp.features.awa;

import java.util.ArrayList;

import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.dashboard.widgets.WidgetSettingsFactory;

public class WidgetJobStatusCurrentGrouped extends WidgetJobStatusCurrent {

	/************************************************************************************
	 * 
	 ************************************************************************************/
	@Override
	public String getWidgetType() {return "emp_awajobstatusgrouped";}
	
	/************************************************************
	 * 
	 ************************************************************/
	@Override
	public String widgetName() { return "Job Status Grouped"; }
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	@Override
	public CFWObject getSettings() {
		return 
			createJobSelectionFields()
				
				.addField(CFWField.newInteger(FormFieldType.TEXT, LAST_RUN_MINUTES)
						.setLabel("{!emp_widget_awajobstatus_last_run_minutes!}")
						.setDescription("{!emp_widget_awajobstatus_last_run_minutes_desc!}")
						.setValue(0)	
				)

				//.addField(WidgetSettingsFactory.createDefaultDisplayAsField())				
				.addAllFields(WidgetSettingsFactory.createTilesSettingsFields())
				.addField(WidgetSettingsFactory.createDisableBoolean())
				.addField(WidgetSettingsFactory.createSampleDataField())
	
		;
	}
	
	/************************************************************************************
	 * 
	 ************************************************************************************/
	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureAWA.PACKAGE_RESOURCE, "emp_awa_commonFunctions.js") );
		array.add( new FileDefinition(HandlingType.JAR_RESOURCE, FeatureAWA.PACKAGE_RESOURCE, "emp_widget_awajobstatus_currentGrouped.js") );
		return array;
	}

	
}
