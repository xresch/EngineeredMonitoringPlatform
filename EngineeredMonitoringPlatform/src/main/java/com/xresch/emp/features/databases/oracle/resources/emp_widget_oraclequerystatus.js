(function (){
	
	CFW.dashboard.registerWidget(
			"emp_oraclequerystatus", 
			createDatabaseQueryStatusWidget(CFWL('emp_widget_oraclequerystatus', "Oracle Query Status")) 
		);
	
})();
