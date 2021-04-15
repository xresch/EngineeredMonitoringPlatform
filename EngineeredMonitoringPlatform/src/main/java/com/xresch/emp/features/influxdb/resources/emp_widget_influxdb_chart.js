
(function (){
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerCategory("fas fa-database", "Monitoring | InfluxDB");
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_influxdb_chart",
		{
			category: "Monitoring | InfluxDB",
			menuicon: "fas fa-chart-bar",
			menulabel: CFWL('emp_widget_influxdb_chart', "InfluxDB Chart"),
			description: CFWL('emp_widget_influxdb_chart_desc', "This widget uses a influxdb query to fetch the last value(instant) of the matched metrics and displays them as a chart."), 
			usetimeframe: true,
			createWidgetInstance: function (widgetObject, callback) {
					
				CFW.dashboard.fetchWidgetData(widgetObject, function(data){
					//cfw_format_csvToObjectArray
					var settings = widgetObject.JSON_SETTINGS;				
					
					//---------------------------------
					// Check for Data and Errors
					if(CFW.utils.isNullOrEmpty(data.payload)){
						callback(widgetObject, '');
						return;
					}
					
					//---------------------------------
					// Prepare InfluxDB data
					var dataArray = CFW.format.csvToObjectArray(data.payload, ",");
					console.log('=========== dataArray =========');
					console.log(dataArray);
					
					//var monitorStats = emp_widget_influxdb_prepareData(data.payload);
					var chartLabelFields = ['_measurement', 'result', '_field', 'path'];
					
//					if(!CFW.utils.isNullOrEmpty(settings.labels)){
//						chartLabelFields = settings.labels.trim().split(/[, ]+/);
//						console.log('chartLabelFields: '); 
//						console.log(chartLabelFields); 
//					}else{
//						chartLabelFields = emp_widget_influxdb_getChartLabelFields(data.payload);
//					}

					//---------------------------
					// Render Settings
					var dataToRender = {
						data: dataArray,
						titlefields: chartLabelFields, 
						titleformat: null, 
						rendererSettings:{
							chart: {
								charttype: settings.chart_type.toLowerCase(),
								xfield: '_time',
								yfield: '_value',
								stacked: settings.stacked,
								showlegend: settings.show_legend,
								// if not set make true
								showaxes: (settings.show_axes == null) ? true : settings.show_axes,
								ymin: settings.ymin,
								ymax: settings.ymax,
								pointradius: settings.pointradius,
								padding: 2
							}
					}};
										
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