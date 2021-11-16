
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerCategory("fas fa-database", "Monitoring | InfluxDB");

	/******************************************************************
	 *
	 * @param data the data structure as returned by influx
	 * @param latestValueOnly boolean to define if only the latest value of a series 
	 *        should be taken
	 * @param valueColumn to check if it is not null or empty
	 ******************************************************************/
	function emp_influxdb_convertInfluxQLToDataviewerStructure(data, latestValueOnly, valueColumn){
		
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
			// Loop Series 
			for(let seriesIndex in currentStatement.series){
				let currentSeries = currentStatement.series[seriesIndex];
				let seriesName = currentSeries.name;				
				let columns = currentSeries.columns;
				
				//------------------------
				// Filter Series By Latest 
				var valuesToConvert = currentSeries.values;
				if(latestValueOnly){
					let valueIndex = columns.indexOf(valueColumn);
					let timeIndex = columns.indexOf('time');
					valuesToConvert = [_.maxBy(currentSeries.values, function(o){
						return ( CFW.utils.isNullOrEmpty(o[valueIndex]) ) ? -1 : o[timeIndex];
					})];
				}
				console.log('=========== valuesToConvert ============')
				console.log(latestValueOnly)
				console.log(valuesToConvert)

				//---------------------------
				// Loop Values
				
				let tags = currentSeries.tags;
				for (let valueIndex in valuesToConvert){
					let currentValues = valuesToConvert[valueIndex];
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
						dataviewerObject[columns[fieldIndex]] = CFW.utils.setFloatPrecision(currentValues[fieldIndex], 2);
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
						
						objectForColumns[columnName].values.push(
							CFW.utils.setFloatPrecision(currentValues[fieldIndex], 2)
						);
					}
					
				}
				
			}
		}
		
		return dataviewerFormat;

	}
	