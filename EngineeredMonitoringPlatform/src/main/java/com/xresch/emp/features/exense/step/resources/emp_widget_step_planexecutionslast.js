(function (){
	
	var widgetMenuLabel = CFWL('emp_widget_step_planexecutionslast', "Plan Executions Last N")
	var widgetDescription = CFWL('emp_widget_step_planexecutionslast_desc', "List the last N executions for every selected plan, before the selected date.");
	
	var widgetDefinition = createStepStatusWidgetBase(widgetMenuLabel, widgetDescription);
		
	CFW.dashboard.registerWidget(
			"emp_step_planexecutionslast", 
			widgetDefinition
		);
	
})();