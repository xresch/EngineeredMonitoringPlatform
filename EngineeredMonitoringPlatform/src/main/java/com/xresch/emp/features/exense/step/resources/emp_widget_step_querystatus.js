(function (){
	
	CFW.dashboard.registerWidget(
			"emp_step_querystatus", 
			createDatabaseQueryStatusWidget(CFWL('emp_widget_step_querystatus', "Step Query Status")) 
		);
	
})();