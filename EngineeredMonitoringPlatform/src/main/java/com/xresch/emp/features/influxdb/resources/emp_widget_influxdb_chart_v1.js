
(function (){
	
	
	/******************************************************************
	 * 
	 ******************************************************************/
	function emp_influxdb_convertInfluxToDataviewerStructure(data){
		
		/*		{ "results": [ { "statement_id": 0,
			"series": [
				{
					"name": "runtime",
					"columns": [
						"time",
						"...",
					],
					"values": [
						[
							"2021-11-10T13:28:10Z",
							2962944,
							260309,
							2962944,
							61218816,
							5005312,
*/

		var dataviewerFormat = [];
		
		for(let index in data.results){
			let currentStatement = data.results[index];
			
			//------------------------
			// Check Errors
			if(currentStatement.error != null){
				CFW.ui.addToastError(currentStatement.error);
				continue;
			}
			
			//------------------------
			// Convert Series
			for(let seriesIndex in currentStatement.series){
				let currentSeries = currentStatement.series[seriesIndex];
				let seriesName = currentSeries.name;
				let columns = currentSeries.columns;
				
				for (let valueIndex in currentSeries.values){
					let currentValues = currentSeries.values[valueIndex];
					
					let dataviewerObject = {'series': seriesName}
					for(let fieldIndex in currentValues){
						dataviewerObject[columns[fieldIndex]] = currentValues[fieldIndex];
					}
					dataviewerFormat.push(dataviewerObject);
				}
				
			}
		}
		
		return dataviewerFormat;

	}
	
	
		/******************************************************************
	 * 
	 ******************************************************************/
	function emp_influxdb_convertInfluxToChartRendererStructure(data){
		
		var dataviewerFormat = [];
		
		for(let index in data.results){
			let currentStatement = data.results[index];
			
			//------------------------
			// Check Errors
			if(currentStatement.error != null){
				CFW.ui.addToastError(currentStatement.error);
				continue;
			}
			
			//------------------------
			// Convert Series
			for(let seriesIndex in currentStatement.series){
				let currentSeries = currentStatement.series[seriesIndex];
				let seriesName = currentSeries.name;
				let columns = currentSeries.columns;
				let timeIndex = 0;
				
				let objectForColumns = {};
				for (let columnIndex in columns){
					let columnName = columns[columnIndex];
					
					//skip time column
					if(columnName == 'time'){ timeIndex = columnIndex; continue; }
					
					objectForColumns[columnName] = {series: seriesName, column : columnName, times: [], values: [] }
					dataviewerFormat.push(objectForColumns[columnName]);
				}
					
				for (let valueIndex in currentSeries.values){
					let currentValues = currentSeries.values[valueIndex];
					
					for(let fieldIndex in currentValues){
						let columnName = columns[fieldIndex];
						if(columnName == 'time'){ continue; }
						objectForColumns[columnName].times.push(currentValues[timeIndex]);
						objectForColumns[columnName].values.push(currentValues[fieldIndex]);
					}
					
				}
				
			}
		}
		
		return dataviewerFormat;

	}
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerCategory("fas fa-database", "Monitoring | InfluxDB");
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_influxdb_chart_v1",
		{
			category: "Monitoring | InfluxDB",
			menuicon: "fas fa-chart-bar",
			menulabel: CFWL('emp_widget_influxdb_chart', "InfluxQL Chart"),
			description: CFWL('emp_widget_influxdb_chart_desc', "This widget uses a InfluxQL query to fetch time series and displays them as a chart."), 
			usetimeframe: true,
			createWidgetInstance: function (widgetObject, params, callback) {
					
				CFW.dashboard.fetchWidgetData(widgetObject, params, function(data){
					//cfw_format_csvToObjectArray
					var settings = widgetObject.JSON_SETTINGS;				
					
					console.log('=========== Influx DB Data =========');
					console.log(data);
					
					//---------------------------------
					// Check for Data and Errors
					if(CFW.utils.isNullOrEmpty(data.payload) || data.payload[0].results == undefined ){
						callback(widgetObject, '');
						return;
					}

					//---------------------------------
					// Prepare InfluxDB data
					var dataArray = emp_influxdb_convertInfluxToChartRendererStructure(data.payload[0]);
					console.log('=========== dataArray =========');
					console.log(dataArray);
					
					//var monitorStats = emp_widget_influxdb_prepareData(data.payload);
					var chartLabelFields = ['series', 'column'];
					
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
								datamode: 'arrays',
								xfield: 'times',
								yfield: 'values',
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