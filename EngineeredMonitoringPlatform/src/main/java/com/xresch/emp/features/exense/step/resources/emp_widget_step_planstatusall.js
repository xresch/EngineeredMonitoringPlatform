(function (){
	
	var widgetMenuLabel = CFWL('emp_widget_step_planstatusall', "Plan Status All")
	var widgetDescription = CFWL('emp_widget_step_planstatusall_desc', "Returns the last status in the selected time range of all plans.");
	
	var widgetDefinition = createStepStatusWidgetBase(widgetMenuLabel, widgetDescription);
		
	CFW.dashboard.registerWidget(
			"emp_step_planstatusall", 
			widgetDefinition
		);
	
})();