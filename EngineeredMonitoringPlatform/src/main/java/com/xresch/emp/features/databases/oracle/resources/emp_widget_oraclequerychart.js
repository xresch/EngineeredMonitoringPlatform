(function (){
	
	CFW.dashboard.registerWidget(
			"emp_oraclequerychart", 
			createDatabaseQueryChartWidget(CFWL('emp_widget_oraclequerychart', "Oracle Query Chart")) 
		);
	
})();