(function (){
	
	var widgetMenuLabel = CFWL('emp_widget_step_schedulerstatususers', "Scheduler Status By User")
	var widgetDescription = CFWL('emp_widget_step_schedulerstatususers_desc', "Returns the last status in the selected time range, of all executed schedulers the selected users have access to.");
	
	var widgetDefinition = createStepStatusWidgetBase(widgetMenuLabel, widgetDescription);
		
	CFW.dashboard.registerWidget(
			"emp_step_schedulerstatususers", 
			widgetDefinition
		);
	
})();