(function (){
	
	var widgetMenuLabel = CFWL('emp_widget_step_schedulerstatus', "Scheduler Status")
	var widgetDescription = CFWL('emp_widget_step_schedulerstatus_desc', "Returns the last status in the selected time range, of all execution of the selected schedulers.");
	
	var widgetDefinition = createStepStatusWidgetBase(widgetMenuLabel, widgetDescription);
		
	CFW.dashboard.registerWidget(
			"emp_step_schedulerstatus", 
			widgetDefinition
		);
	
})();