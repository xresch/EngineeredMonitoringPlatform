(function (){
	
	var widgetMenuLabel = CFWL('emp_widget_step_schedulerexecutionstimerange', "Scheduler Executions Time Range")
	var widgetDescription = CFWL('emp_widget_step_schedulerexecutionstimerange_desc', "List the executions during the selected time range for every selected scheduler.");
	
	var widgetDefinition = createStepStatusWidgetBase(widgetMenuLabel, widgetDescription);
		
	CFW.dashboard.registerWidget(
			"emp_step_schedulerexecutionstimerange", 
			widgetDefinition
		);
	
})();