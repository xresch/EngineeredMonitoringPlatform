(function (){
	
	var widgetMenuLabel = CFWL('emp_widget_step_schedulermetricschart', "Scheduler Metric Chart")
	var widgetDescription = CFWL('emp_widget_step_schedulermetricschart_desc', "Shows a chart for the selected schedulers and metrics.");
	var arrayTitleFields = ["metric", "group"];
	var timeField = "time";
	var valueField = "val";
	
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