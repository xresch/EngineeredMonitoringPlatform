
(function (){

	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_dynatrace_hostlogs",
		{
			category: DYNATRACE_WIDGET_CATEGORY,
			menuicon: "fas fa-microchip",
			menulabel: CFWL('emp_widget_dynatrace_hostlogs', "Host Logs"),
			description: CFWL('emp_widget_dynatrace_hostlogs_desc', "List log entries for a specified host."), 
			usetimeframe: true,
			createWidgetInstance: function (widgetObject, callback) {
					
				CFW.dashboard.fetchWidgetData(widgetObject, function(data){
					
					var settings = widgetObject.JSON_SETTINGS;				
					
					//---------------------------------
					// Check for Data and Errors
					if(CFW.utils.isNullOrEmpty(data.payload) || typeof data.payload == 'string' || data.payload.length == null){
						callback(widgetObject, '');
						return;
					}
					
					//---------------------------
					// Render Settings
					var dataToRender = {
						data: data.payload,
						visiblefields: ["timestamp", "logLevel", "text"],
						//titlefields: ['displayName'], 
						//titleformat: '{0}', 
						labels: {
						},
						customizers:{
							timestamp: function(record, value) {  return CFW.format.epochToTimestamp(value); },
						},
						rendererSettings:{
							table: { verticalize: true, narrow: true, filterable: false}
							
					}};
					

					//--------------------------
					// Render Widget
					var alertRenderer = CFW.render.getRenderer('table');
					callback(widgetObject, alertRenderer.render(dataToRender));
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