(function (){
	
	var widgetMenuLabel = CFWL('emp_widget_step_schedulerdurationchart', "Scheduler Duration Chart")
	var widgetDescription = CFWL('emp_widget_step_schedulerdurationchart_desc', "Shows a duration chart for the selected schedulers.");
	var arrayTitleFields = ["projectname", "schedulername"];
	var timeField = "endtime";
	var valueField = "duration";
	
	var widgetDefinition = createStepChartWidgetBase(
			  widgetMenuLabel
			, widgetDescription
			, arrayTitleFields
			, timeField
			, valueField
		);
		
	CFW.dashboard.registerWidget(
			"emp_step_schedulerdurationchart", 
			widgetDefinition
		);
	
})();