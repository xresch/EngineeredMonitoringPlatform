
(function (){

	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_dynatrace_processlogs",
		{
			category: DYNATRACE_WIDGET_CATEGORY,
			menuicon: "fas fa-file-signature",
			menulabel: CFWL('emp_widget_dynatrace_processlogs', "Process Logs"),
			description: CFWL('emp_widget_dynatrace_processlogs_desc', "List log entries for a specified process."), 
			usetimeframe: true,
			createWidgetInstance: function (widgetObject, params, callback) {
					
				CFW.dashboard.fetchWidgetData(widgetObject, params, function(data){
					emp_dynatrace_renderLogs(widgetObject, data, callback);
				});
			},
			
		}
	);	
	
})();