
(function (){

	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_dynatrace_processevents",
		{
			category: DYNATRACE_WIDGET_CATEGORY,
			menuicon: "fas fa-bolt",
			menulabel: CFWL('emp_widget_dynatrace_processevents', "Process Events"),
			description: CFWL('emp_widget_dynatrace_processevents_desc', "Lists the events for the selected process, occurred during the selected time frame."), 
			createWidgetInstance: function (widgetObject, params, callback) {
					
				CFW.dashboard.fetchWidgetData(widgetObject, params, function(data){
					
					emp_dynatrace_renderEvents(widgetObject, data, callback);
					
				});
			},
			
			getEditForm: function (widgetObject) {
				return CFW.dashboard.getSettingsForm(widgetObject);
			},
			
			onSave: function (form, widgetObject) {
				widgetObject.JSON_SETTINGS = CFW.format.formToObject(form);
				return true;	
			}
		}
	);	
	
})();