
(function (){

	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_dynatrace_hostevents",
		{
			category: DYNATRACE_WIDGET_CATEGORY,
			menuicon: "fas fa-bolt",
			menulabel: CFWL('emp_widget_dynatrace_hostevents', "Host Processes"),
			description: CFWL('emp_widget_dynatrace_hostevents_desc', "Displays a list of the processes running on a host monitored by Dynatrace."), 
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
						visiblefields: ["entityName", "eventType", "eventStatus", "impactLevel", "startTime", "endTime"],
						titlefields: ['entityName'], 
						titleformat: '{0}', 
						labels:{
							entityName: 'Entity Name',
							eventType: 'Event Type',
							eventStatus: 'Status',
							impactLevel: 'Impact Level',
							startTime: 'Start',
							endTime: 'End',
						},
						customizers:{
							metadata: function(record, value) {  return JSON.stringify(value.executables); },
							startTime: function(record, value) {  return CFW.format.epochToTimestamp(value); },
							endTime: function(record, value) {  return (value == -1) ? '&nbsp;' : CFW.format.epochToTimestamp(value); },
							eventStatus: function(record, value) { 
					 			var style = "" ;
					 			if(value == "CLOSED"){
					 				style = 'badge-success';
								}else if(value == "OPEN"){
									style = 'badge-warning text-white';
								}else {
									style = 'cfw-bg-unknown';
								}  
					 			
					 			return '<span class="badge '+style+' m-1">'+value+'</span>';
					 		}
						},
						rendererSettings:{
							table: { verticalize: false, narrow: true, filterable: false}
							
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