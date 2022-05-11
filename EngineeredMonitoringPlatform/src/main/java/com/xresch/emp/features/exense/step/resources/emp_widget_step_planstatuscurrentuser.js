(function (){
	
	var widgetMenuLabel = CFWL('emp_widget_step_planstatuscurrentuser', "Plan Status By Current User")
	var widgetDescription = CFWL('emp_widget_step_planstatuscurrentuser_desc', "Returns the last status in the selected time range, of all executed plans the current users has access to.");
	
	var widgetDefinition = createStepStatusWidgetBase(widgetMenuLabel, widgetDescription);
		
	CFW.dashboard.registerWidget(
			"emp_step_planstatuscurrentuser", 
			widgetDefinition
		);
	
})();