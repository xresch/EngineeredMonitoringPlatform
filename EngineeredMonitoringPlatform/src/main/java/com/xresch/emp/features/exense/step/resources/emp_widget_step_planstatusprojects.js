(function (){
	
	EXENSE_CATEGORY = "Exense Step";
	
	CFW.dashboard.registerCategory("fas fa-database", EXENSE_CATEGORY);
	
	var widgetDefinition = createDatabaseQueryStatusWidget(CFWL('emp_widget_step_querystatus', "Step Plan Status By Projects")) ;
	
	widgetDefinition.category = EXENSE_CATEGORY;
	
	CFW.dashboard.registerWidget(
			"emp_step_planstatusprojects", 
			widgetDefinition
		);
	
})();