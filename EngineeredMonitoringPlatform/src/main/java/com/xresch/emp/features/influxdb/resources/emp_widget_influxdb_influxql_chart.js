
(function (){
		
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_influxdb_influxql_chart",
		{
			category: CATEGORY_INFLUXDB,
			menuicon: "fas fa-chart-bar",
			menulabel: CFWL('emp_widget_influxdb_influxql_chart', "InfluxQL Chart"),
			description: CFWL('emp_widget_influxdb_influxql_chart_desc', "This widget uses a InfluxQL query to fetch time series and displays them as a chart."), 
			usetimeframe: true,
			createWidgetInstance: function (widgetObject, params, callback) {
					
				CFW.dashboard.fetchWidgetData(widgetObject, params, function(data){
					//cfw_format_csvToObjectArray
					var settings = widgetObject.JSON_SETTINGS;				

					
					//---------------------------------
					// Check for Data and Errors
					if(CFW.utils.isNullOrEmpty(data.payload) || data.payload.results == undefined ){
						callback(widgetObject, '');
						return;
					}

					//---------------------------------
					// Prepare InfluxDB data
					var mode = 'groupbytitle';
					var chartLabelFields = null;
					var xfield = 'time';
					var yfield = settings.valuecolumn;
					var dataArray = null;
					
					
					if( !CFW.utils.isNullOrEmpty(settings.labels) ){
						chartLabelFields = settings.labels.trim().split(/[, ]+/);
						dataArray = emp_influxdb_convertInfluxQLToDataviewerStructure(data.payload, false);
					}else{
						mode = 'arrays';
						chartLabelFields = ['series', 'column'];
						xfield = 'times';
						yfield = 'values';
						dataArray = emp_influxdb_convertInfluxQLToChartRendererStructure(data.payload, false);
					}
					
					//---------------------------
					// Render Settings
					
					var chartsettings = Object.assign({}, settings.JSON_CHART_SETTINGS);
					chartsettings.datamode = mode;
					chartsettings.xfield = xfield;
					chartsettings.yfield = yfield;
					chartsettings.padding = 2;
					
					var dataToRender = {
						data: dataArray,
						titlefields: chartLabelFields, 
						titleformat: null, 
						rendererSettings:{
							chart: chartsettings
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