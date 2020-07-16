/*********************************************************************
 * Takes the content of a CFW payload containing a prometheus response
 * and converts the data to a structure that can be used by CFWRenderer.
 * @param responsePayload an array of prometheus payloads
 * @returns array
*********************************************************************/
function emp_widget_prometheus_prepareData(responsePayload){

	var monitorStats = [];
	
	//---------------------------------
	// Prepare Prometheus data
	for(var i = 0; i < responsePayload.length; i++){
		var prometheusData = responsePayload[i].data; 
		if(prometheusData != null && prometheusData.result != null){
		
			for(index in prometheusData.result){
		
				var current = prometheusData.result[index];
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
	}
	
	return monitorStats;
}

/*********************************************************************
 * Takes the content of a CFW payload containing a prometheus response
 * and converts the data to a structure that can be used by CFWRenderer.
 * @param responsePayload an array of prometheus payloads
 * @returns array
*********************************************************************/
function emp_widget_prometheus_getChartLabelFields(responsePayload){

	var chartLabelFields = [];
	
	//---------------------------------
	// Prepare Prometheus data
	for(var i = 0; i < responsePayload.length; i++){
		var prometheusData = responsePayload[i].data; 
		if(prometheusData != null && prometheusData.result != null){
			chartLabelFields = _.concat(chartLabelFields, Object.keys(prometheusData.result[0].metric) );
		}
	}
	
	return _.uniq(chartLabelFields);
}
