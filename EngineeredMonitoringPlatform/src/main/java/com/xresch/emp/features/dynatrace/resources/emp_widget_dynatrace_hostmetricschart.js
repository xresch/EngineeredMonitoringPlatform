
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
					if(CFW.utils.isNullOrEmpty(data.payload) || typeof data.payload == 'string'){
						callback(widgetObject, '');
						return;
					}
					
					//---------------------------------
					// Prepare data
					console.log(data.payload);
					dataToRender = [];
					var metrics = data.payload.result;
					for(let i = 0; i < metrics.length; i++ ){
						let currentMetric = metrics[i];
						let dataset = {
								metric: 	 currentMetric.metricId.replace('builtin:', ''),
								xvalues:	 currentMetric.data[0].timestamps,
								yvalues:	 currentMetric.data[0].values,
						}
						
						dataToRender.push(dataset);
					}
					
					//---------------------------
					// Render Settings
					var renderParams = {
						data: dataToRender,
						//visiblefields: ["entityId", "displayName", "discoveredName", "tags", "osType", "osArchitecture", "osVersion", "cpuCores", "ipAddresses", "logicalCpuCores","monitoringMode","networkZoneId", "agentVersion","consumedHostUnits", "bitness", "oneAgentCustomHostName" ],
						titlefields: ['metric'], 
						titleformat: '{0}', 
						customizers:{
							tags: function(record, value) {  return JSON.stringify(value); },
							ipAddresses: function(record, value) {  return JSON.stringify(value); },
							agentVersion: function(record, value) {  return JSON.stringify(value); },
						},
						rendererSettings:{
							chart: {
								//charttype: settings.chart_type.toLowerCase(),
								datamode: 'arrays',
								xfield: 'xvalues',
								yfield: 'yvalues',
								//stacked: settings.stacked,
								//showlegend: settings.show_legend,
								//ymin: settings.ymin,
								//ymax: settings.ymax,
								//pointradius: settings.pointradius,
							}
							
					}};
					

					//--------------------------
					// Render Widget
					var renderer = CFW.render.getRenderer('chart');
					callback(widgetObject, renderer.render(renderParams));
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