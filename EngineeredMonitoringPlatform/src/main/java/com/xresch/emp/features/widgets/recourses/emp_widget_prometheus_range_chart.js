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
					var chartLabelFields = null;
					var monitorStats = [];
					
					//---------------------------------
					// Check for Data and Errors
					if(CFW.utils.isNullOrEmpty(data.payload) || typeof data.payload == 'string'){
						callback(widgetObject, '');
						return;
					}
					if(data.payload.error != undefined){
					
						callback(widgetObject, "<p><b>Prometheus Error: </b>"+data.payload.error+'</p>');
						return;
					}
					
					//---------------------------------
					// Prepare Prometheus data
					var prometheusData = data.payload.data; 
					if(prometheusData != null && prometheusData.result != null){

						for(index in prometheusData.result){

							var current = prometheusData.result[index];
							chartLabelFields = Object.keys(current.metric);
							
							if(current.value != undefined){
								//----------------------------------
								// handle Instant Value
								var item = Object.assign({}, current.metric);
								item.time = current.value[0] * 1000;
								item.value = current.value[1];
								if(!isNaN(item.value)){
									item.value = parseFloat(item.value).toFixed(1);
								}
								monitorStats.push(item);
							}else if(current.values != undefined){
								//----------------------------------
								// Handle Range Values
								for(index in current.values){
									value = current.values[index];
									var item = Object.assign({}, current.metric);
									item.time = value[0] * 1000;
									item.value = value[1];
									if(!isNaN(item.value)){
										item.value = parseFloat(item.value).toFixed(1);
									}
									monitorStats.push(item);
								}
							}
						}
					}

					
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
								yfield: 'value'
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