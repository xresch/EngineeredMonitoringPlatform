(function (){
	
	var widgetMenuLabel = CFWL('emp_widget_step_planexecutionstimerange', "Plan Executions Time Range")
	var widgetDescription = CFWL('emp_widget_step_planexecutionstimerange_desc', "List the executions during the selected time range for every selected plan.");
	
	var widgetDefinition = createStepStatusWidgetBase(widgetMenuLabel, widgetDescription);
		
	CFW.dashboard.registerWidget(
			"emp_step_planexecutionstimerange", 
			widgetDefinition
		);
	
})();