(function (){
	
	CFW.dashboard.registerWidget(
			"emp_mssqlquerystatus", 
			createDatabaseQueryStatusWidget(CFWL('emp_widget_mssqlquerystatus', "MSSQL Query Status")) 
		);
	
})();

