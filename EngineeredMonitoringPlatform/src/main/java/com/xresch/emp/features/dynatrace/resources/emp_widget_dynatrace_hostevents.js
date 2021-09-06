
(function (){

	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_dynatrace_hostevents",
		{
			category: DYNATRACE_WIDGET_CATEGORY,
			menuicon: "fas fa-bolt",
			menulabel: CFWL('emp_widget_dynatrace_hostevents', "Host Events"),
			description: CFWL('emp_widget_dynatrace_hostevents_desc', "Lists the events for the selected host, occurred during the selected time frame."), 
			usetimeframe: true,
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