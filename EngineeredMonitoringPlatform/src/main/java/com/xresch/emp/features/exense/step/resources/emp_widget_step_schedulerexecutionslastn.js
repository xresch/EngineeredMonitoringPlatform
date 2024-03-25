(function (){
	
	var widgetMenuLabel = CFWL('emp_widget_step_schedulerexecutionslast', "Scheduler Executions Last N")
	var widgetDescription = CFWL('emp_widget_step_schedulerexecutionslast_desc', "List the last N executions for every selected scheduler in the selected timeframe.");
	
	var widgetDefinition = createStepStatusWidgetBase(widgetMenuLabel, widgetDescription);
		
	CFW.dashboard.registerWidget(
			"emp_step_schedulerexecutionslastn", 
			widgetDefinition
		);
	
})();