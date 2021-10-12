(function (){
	
	CFW.dashboard.registerWidget(
			"emp_mysqlquerystatus", 
			createDatabaseQueryStatusWidget(CFWL('emp_widget_mysqlquerystatus', "MySQL Query Status")) 
		);
	
})();;