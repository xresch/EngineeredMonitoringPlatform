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
		
			for(var index in prometheusData.result){
		
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
					for(var subindex in current.values){
						var value = current.values[subindex];
						var subitem = Object.assign({}, current.metric);
						subitem.time = value[0] * 1000;
						subitem.value = value[1];
						if(!isNaN(subitem.value)){
							subitem.value = parseFloat(subitem.value).toFixed(1);
						}
						monitorStats.push(subitem);
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
			var firstResult = prometheusData.result[0];
			if(firstResult != null && firstResult.metric != null){
				chartLabelFields = _.concat(chartLabelFields, Object.keys(firstResult.metric) );
			}
		}
	}
	
	return _.uniq(chartLabelFields);
}
