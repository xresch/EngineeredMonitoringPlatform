
(function (){

	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_dynatrace_hostlogs",
		{
			category: DYNATRACE_WIDGET_CATEGORY,
			menuicon: "fas fa-file-signature",
			menulabel: CFWL('emp_widget_dynatrace_hostlogs', "Host Logs"),
			description: CFWL('emp_widget_dynatrace_hostlogs_desc', "List log entries for a specified host."), 
			usetimeframe: true,
			createWidgetInstance: function (widgetObject, params, callback) {
					
				CFW.dashboard.fetchWidgetData(widgetObject, params, function(data){
					emp_dynatrace_renderLogs(widgetObject, data, callback);
				});
			},
			
		}
	);	
	
})();