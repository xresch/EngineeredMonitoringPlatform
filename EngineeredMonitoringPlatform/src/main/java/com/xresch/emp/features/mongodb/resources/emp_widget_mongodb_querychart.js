(function (){
	
	CFW.dashboard.registerWidget(
			"emp_mongodb_querychart", 
			createDatabaseQueryChartWidget(CFWL('emp_widget_mongodb_querychart', "MongoDB Query Chart")) 
		);
	
})();