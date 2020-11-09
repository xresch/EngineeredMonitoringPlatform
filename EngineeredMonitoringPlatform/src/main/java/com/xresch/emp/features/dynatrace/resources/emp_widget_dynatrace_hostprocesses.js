
(function (){

	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_dynatrace_hostprocesses",
		{
			category: DYNATRACE_WIDGET_CATEGORY,
			menuicon: "fas fa-microchip",
			menulabel: CFWL('emp_widget_dynatrace_hostprocesses', "Host Processes"),
			description: CFWL('emp_widget_dynatrace_hostprocesses_desc', "Displays a list of the processes running on a host monitored by Dynatrace."), 
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
						visiblefields: ["discoveredName", "tags", "firstSeenTimestamp", "lastSeenTimestamp", "metadata"],
						titlefields: ['displayName'], 
						titleformat: '{0}', 
						labels: {
							metadata: 'Executables'
						},
						customizers:{
							metadata: function(record, value) {  return JSON.stringify(value.executables); },
							firstSeenTimestamp: function(record, value) {  return CFW.format.epochToTimestamp(value); },
							lastSeenTimestamp: function(record, value) {  return CFW.format.epochToTimestamp(value); },
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