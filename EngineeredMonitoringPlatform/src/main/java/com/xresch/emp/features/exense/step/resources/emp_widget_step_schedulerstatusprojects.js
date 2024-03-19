(function (){
	
	var widgetMenuLabel = CFWL('emp_widget_step_schedulerstatusprojects', "Scheduler Status By Projects")
	var widgetDescription = CFWL('emp_widget_step_schedulerstatusprojects_desc', "Returns the last status in the selected time range, of all schedulers listed in the selected projects.");
	
	var widgetDefinition = createStepStatusWidgetBase(widgetMenuLabel, widgetDescription);
		
	CFW.dashboard.registerWidget(
			"emp_step_schedulerstatusprojects", 
			widgetDefinition
		);
	
})();