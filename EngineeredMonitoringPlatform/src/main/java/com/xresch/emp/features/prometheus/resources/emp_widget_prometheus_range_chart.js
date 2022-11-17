
(function (){
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_prometheus_range_chart",
		{
			category: CATEGORY_PROMETHEUS,
			menuicon: "fas fa-chart-bar",
			menulabel: CFWL('emp_widget_prometheus_range_chart', "Range Chart"),
			description: CFWL('emp_widget_prometheus_range_chart_desc', "This widget uses a prometheus query to fetch the last value(instant) of the matched metrics and displays them as a chart."), 
			usetimeframe: true,
			createWidgetInstance: function (widgetObject, params, callback) {
					
				CFW.dashboard.fetchWidgetData(widgetObject, params, function(data){
					
					var settings = widgetObject.JSON_SETTINGS;				
					
					//---------------------------------
					// Check for Data and Errors
					if(CFW.utils.isNullOrEmpty(data.payload) || typeof data.payload == 'string' || data.payload.length == null){
						callback(widgetObject, '');
						return;
					}
					
					//---------------------------------
					// Prepare Prometheus data
					var monitorStats = emp_widget_prometheus_prepareData(data.payload);
					var chartLabelFields;
					
					if(!CFW.utils.isNullOrEmpty(settings.labels)){
						chartLabelFields = settings.labels.trim().split(/[, ]+/);

					}else{
						chartLabelFields = emp_widget_prometheus_getChartLabelFields(data.payload);
					}

					//---------------------------
					// Render Settings
					
					var chartsettings = Object.assign({}, settings.JSON_CHART_SETTINGS);
					chartsettings.xfield = 'time';
					chartsettings.yfield = 'value';
					chartsettings.padding = 2;
					
					var dataToRender = {
						data: monitorStats,
						titlefields: chartLabelFields, 
						titleformat: null, 
						rendererSettings:{
							chart: chartsettings /*{
								charttype: settings.chart_type.toLowerCase(),
								xfield: 'time',
								yfield: 'value',
								ytype: settings.y_axis_type,
								stacked: settings.stacked,
								showlegend: settings.show_legend,
								// if not set make true
								showaxes: (settings.show_axes == null) ? true : settings.show_axes,
								ymin: settings.ymin,
								ymax: settings.ymax,
								pointradius: settings.pointradius,
								padding: 2
							}*/
						}
					};
										
					//--------------------------
					// Render Widget
					var renderer = CFW.render.getRenderer('chart');
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