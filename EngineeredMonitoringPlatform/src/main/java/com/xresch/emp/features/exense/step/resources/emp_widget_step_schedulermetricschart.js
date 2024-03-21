(function (){
	
	var widgetMenuLabel = CFWL('emp_widget_step_schedulermetricschart', "Scheduler Metric Chart")
	var widgetDescription = CFWL('emp_widget_step_schedulermetricschart_desc', "Shows a chart for the selected schedulers and metrics.");
	var arrayTitleFields = ["projectname", "schedulername"];
	var timeField = "time";
	var valueField = "avg";
	
	var widgetDefinition = createStepChartWidgetBase(
			  widgetMenuLabel
			, widgetDescription
			, arrayTitleFields
			, timeField
			, valueField
		);
		
	CFW.dashboard.registerWidget(
			"emp_step_schedulermetricschart", 
			widgetDefinition
		);
	
})();