(function (){
	
	CFW.dashboard.registerWidget(
			"emp_mongodb_querystatus", 
			createDatabaseQueryStatusWidget(CFWL('emp_widget_mongodb_querystatus', "MongoDB Query Status")) 
		);
	
})();;