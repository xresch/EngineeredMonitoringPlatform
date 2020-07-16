(function (){
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerCategory("fas fa-desktop", "Monitoring | Prometheus");
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_prometheus_range_chart",
		{
			category: "Monitoring | Prometheus",
			menuicon: "fas fa-chart-bar",
			menulabel: CFWL('emp_widget_prometheus_range_chart', "Range Chart"),
			description: CFWL('emp_widget_prometheus_range_chart_desc', "This widget uses a prometheus query to fetch the last value(instant) of the matched metrics and displays them as a chart."), 
			createWidgetInstance: function (widgetObject, callback) {
					
				CFW.dashboard.fetchWidgetData(widgetObject, function(data){
					
					var settings = widgetObject.JSON_SETTINGS;
					 null;
					
					
					//---------------------------------
					// Check for Data and Errors
					if(CFW.utils.isNullOrEmpty(data.payload) || typeof data.payload == 'string' || data.payload.length == null){
						callback(widgetObject, '');
						return;
					}
					
					//---------------------------------
					// Prepare Prometheus data
					var monitorStats = emp_widget_prometheus_prepareData(data.payload);
					var chartLabelFields = emp_widget_prometheus_getChartLabelFields(data.payload);

					//---------------------------
					// Render Settings
					var dataToRender = {
						data: monitorStats,
						titlefields: chartLabelFields, 
						titledelimiter: ' - ', 
						rendererSettings:{
							chart: {
								charttype: settings.chart_type.toLowerCase(),
								xfield: 'time',
								yfield: 'value',
								stacked: settings.stacked,
								showlegend: settings.show_legend,
								ymin: settings.ymin,
								ymax: settings.ymax,
							}
					}};
										
					//--------------------------
					// Render Widget
					var alertRenderer = CFW.render.getRenderer('chart');
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