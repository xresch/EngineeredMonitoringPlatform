(function (){
	
	var widgetMenuLabel = CFWL('emp_widget_step_plandurationchart', "Plan Duration Chart")
	var widgetDescription = CFWL('emp_widget_step_plandurationchart_desc', "Shows duration statistics for the executions of the selected plans.");
	var arrayTitleFields = ["projectname", "planname"];
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
			"emp_step_plandurationchart", 
			widgetDefinition
		);
	
})();