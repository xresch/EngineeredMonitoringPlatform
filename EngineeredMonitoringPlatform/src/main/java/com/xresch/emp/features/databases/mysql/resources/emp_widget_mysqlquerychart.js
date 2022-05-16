(function (){
	
	CFW.dashboard.registerWidget(
			"emp_mysqlquerychart", 
			createDatabaseQueryChartWidget(CFWL('emp_widget_mysqlquerychart', "MySQL Query Chart")) 
		);
	
})();