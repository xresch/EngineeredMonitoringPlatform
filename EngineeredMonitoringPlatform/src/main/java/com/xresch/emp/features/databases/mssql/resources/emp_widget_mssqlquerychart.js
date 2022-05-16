(function (){
	
	CFW.dashboard.registerWidget(
			"emp_mssqlquerychart", 
			createDatabaseQueryChartWidget(CFWL('emp_widget_mssqlquerychart', "MSSQL Query Chart")) 
		);
	
})();