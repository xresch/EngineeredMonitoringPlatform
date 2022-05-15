(function (){
	
	CFW.dashboard.registerWidget(
			"emp_genericjdbcquerystatus", 
			createDatabaseQueryStatusWidget(CFWL('emp_widget_genericjdbcquerystatus', "Generic JDBC Query Status")) 
		);
	
})();