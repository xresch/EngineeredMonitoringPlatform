
(function (){
	
	
	/******************************************************************
	 *
	 * @param data the data structure as returned by influx
	 ******************************************************************/
	function emp_influxdb_convertInfluxQLToDataviewerStructure(data){
		
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
				CFW.ui.addToastDanger(currentStatement.error);
				continue;
			}
			
			//------------------------
			// Convert Series 
			for(let seriesIndex in currentStatement.series){
				let currentSeries = currentStatement.series[seriesIndex];
				let seriesName = currentSeries.name;				
				
				
				
				
				//---------------------------
				// Loop Series
				let columns = currentSeries.columns;
				let tags = currentSeries.tags;
				for (let valueIndex in currentSeries.values){
					let currentValues = currentSeries.values[valueIndex];
					let dataviewerObject = {'series': seriesName};
					//---------------------------
					// Tags to Fields
					if(tags != null){
						for (let tagsKey in tags){
							dataviewerObject[tagsKey] = tags[tagsKey];
						}
					}
					
					//---------------------------
					// Columns & Values to Fields
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
	function emp_influxdb_convertInfluxQLToChartRendererStructure(data){
		
		var dataviewerFormat = [];
		
		for(let index in data.results){
			let currentStatement = data.results[index];
			
			//------------------------
			// Check Errors
			if(currentStatement.error != null){
				CFW.ui.addToastDanger(currentStatement.error);
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
	CFW.dashboard.registerWidget("emp_influxdb_influxql_chart",
		{
			category: "Monitoring | InfluxDB",
			menuicon: "fas fa-chart-bar",
			menulabel: CFWL('emp_widget_influxdb_influxql_chart', "InfluxQL Chart"),
			description: CFWL('emp_widget_influxdb_influxql_chart_desc', "This widget uses a InfluxQL query to fetch time series and displays them as a chart."), 
			usetimeframe: true,
			createWidgetInstance: function (widgetObject, params, callback) {
					
				CFW.dashboard.fetchWidgetData(widgetObject, params, function(data){
					//cfw_format_csvToObjectArray
					var settings = widgetObject.JSON_SETTINGS;				
					
					console.log('=========== Influx DB Data =========');
					console.log(data);
					
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
						dataArray = emp_influxdb_convertInfluxQLToDataviewerStructure(data.payload);
					}else{
						mode = 'arrays';
						chartLabelFields = ['series', 'column'];
						xfield = 'times';
						yfield = 'values';
						dataArray = emp_influxdb_convertInfluxQLToChartRendererStructure(data.payload);
					}
					
					console.log('=========== dataArray =========');
					console.log(dataArray);

					//---------------------------
					// Render Settings
					var dataToRender = {
						data: dataArray,
						titlefields: chartLabelFields, 
						titleformat: null, 
						rendererSettings:{
							chart: {
								charttype: settings.chart_type.toLowerCase(),
								datamode: mode,
								xfield: xfield,
								yfield: yfield,
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
							
					console.log('=========== dataToRender =========');
					console.log(dataToRender);			
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