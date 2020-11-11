
(function (){
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_dynatrace_hostmetricschart",
		{
			category: DYNATRACE_WIDGET_CATEGORY,
			menuicon: "fas fa-chart-bar",
			menulabel: CFWL('emp_widget_dynatrace_hostmetricschart', "Host Metrics Chart"),
			description: CFWL('emp_widget_dynatrace_hostmetricschart_desc', "Displays a chart for the selected metrics and the selected host."), 
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
						//visiblefields: ["entityId", "displayName", "discoveredName", "tags", "osType", "osArchitecture", "osVersion", "cpuCores", "ipAddresses", "logicalCpuCores","monitoringMode","networkZoneId", "agentVersion","consumedHostUnits", "bitness", "oneAgentCustomHostName" ],
						//titlefields: ['displayName'], 
						titleformat: '{0}', 
						customizers:{
							tags: function(record, value) {  return JSON.stringify(value); },
							ipAddresses: function(record, value) {  return JSON.stringify(value); },
							agentVersion: function(record, value) {  return JSON.stringify(value); },
						},
						rendererSettings:{
							table: { verticalize: true, narrow: true, filterable: false}
							
					}};
					

					//--------------------------
					// Render Widget
					var renderer = CFW.render.getRenderer('json');
					callback(widgetObject, renderer.render(dataToRender));
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