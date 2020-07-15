/*********************************************************************
 * Takes the content of a CFW payload containing a prometheus response
 * and converts the data to a structure that can be used by CFWRenderer.
 * @param responsePayload
 * @returns array
*********************************************************************/
function emp_widget_prometheus_prepareData(responsePayload){

	var monitorStats = [];
	
	if(responsePayload.error != undefined){
		CFW.ui.addToastDanger("<p><b>Prometheus Error: </b>"+responsePayload.error+'</p>');
		return;
	}
	
	//---------------------------------
	// Prepare Prometheus data
	var prometheusData = responsePayload.data; 
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
	
	return monitorStats;
}
