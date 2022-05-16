(function (){
	
	CFW.dashboard.registerWidget(
			"emp_genericjdbcquerychart", 
			createDatabaseQueryChartWidget(CFWL('emp_widget_genericjdbcquerychart', "Generic JDBC Query Chart")) 
		);
	
})();