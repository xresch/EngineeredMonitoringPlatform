(function (){
	
	var widgetMenuLabel = CFWL('emp_widget_step_planstatus', "Plan Status")
	var widgetDescription = CFWL('emp_widget_step_planstatus_desc', "Returns the last status in the selected time range, of all execution of the selected plans.");
	
	var widgetDefinition = createStepStatusWidgetBase(widgetMenuLabel, widgetDescription);
		
	CFW.dashboard.registerWidget(
			"emp_stepmongodb_planstatus", 
			widgetDefinition
		);
	
})();