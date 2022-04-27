(function (){
	
	var widgetMenuLabel = CFWL('emp_widget_step_planstatusprojects', "Step Plan Status By Projects")
	var widgetDescription = CFWL('emp_widget_step_planstatusprojects_desc', "Returns the last status in the selected time range, of all execution plans listed in the selected projects.");
	
	var widgetDefinition = createStepStatusWidgetBase(widgetMenuLabel, widgetDescription);
		
	CFW.dashboard.registerWidget(
			"emp_step_planstatusprojects", 
			widgetDefinition
		);
	
})();