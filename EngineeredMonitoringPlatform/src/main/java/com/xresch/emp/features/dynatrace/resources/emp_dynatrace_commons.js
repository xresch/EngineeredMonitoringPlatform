
	
var DYNATRACE_WIDGET_CATEGORY =  "Dynatrace";

/******************************************************************
 * 
 ******************************************************************/
CFW.dashboard.registerCategory("fas fa-desktop", DYNATRACE_WIDGET_CATEGORY);

/******************************************************************
 * 
 ******************************************************************/
function emp_dynatrace_prepareMetricData(metricsArray, singleValues){
	
	dataToRender = [];
	for(let i = 0; i < metricsArray.length; i++ ){
		
		let currentMetric = metricsArray[i];
		
		if(currentMetric.data.length > 0){

			let dataLength = currentMetric.data.length;
			for(let j = 0; j < dataLength; j++ ){
				currentData = currentMetric.data[j];
				
				
				let dataset;
				if(!singleValues){
					dataset = {
						metric: 	 currentMetric.metricId.replace('builtin:', '') +"-"+ currentData.dimensions[currentData.dimensions.length-1],
						xvalues:	 currentData.timestamps,
						yvalues:	 currentData.values,
					}
				}else{
					dataset = {
						metric: 	currentMetric.metricId.replace('builtin:', '') +"-"+ currentData.dimensions[currentData.dimensions.length-1],
						time:	 	currentData.timestamps[0],
						value:	 	currentData.values[0],
					}
				}
		
				dataToRender.push(dataset);
			}
		}
	}
	return dataToRender;
}
/******************************************************************
 * 
 ******************************************************************/
function emp_dynatrace_renderLogs(widgetObject, data, callback){
	
	var settings = widgetObject.JSON_SETTINGS;				
	
	//---------------------------------
	// Check for Data and Errors
	if(CFW.utils.isNullOrEmpty(data.payload) || typeof data.payload == 'string' || data.payload.length == null){
		callback(widgetObject, '');
		return;
	}
	
	//---------------------------
	// Render Settings
	var dataToRender = {
		data: data.payload,
		visiblefields: ["timestamp", "logLevel", "text"],
		labels: {
			logLevel: "Level",
			text: "Message"
		},
		customizers:{
			timestamp: function(record, value) {  return CFW.format.epochToTimestamp(value); },
		},
		rendererSettings:{
			table: { verticalize: false, narrow: true, filterable: false}
			
	}};
	

	//--------------------------
	// Render Widget
	var alertRenderer = CFW.render.getRenderer('table');
	callback(widgetObject, alertRenderer.render(dataToRender));
	
}
/******************************************************************
 * 
 ******************************************************************/
function emp_dynatrace_renderEvents(widgetObject, data, callback){
	var settings = widgetObject.JSON_SETTINGS;				
	
	//---------------------------------
	// Check for Data and Errors
	if(CFW.utils.isNullOrEmpty(data.payload) || typeof data.payload == 'string' || data.payload.length == null){
		callback(widgetObject, '');
		return;
	}
	
	//---------------------------------
	// Add Styles
	for(var key in data.payload){
		var current = data.payload[key];
		
		if(settings.disable){
			current.alertstyle = 'cfw-darkgray';
			current.textstyle = "white"; 
			continue;
		}
		if(current.eventStatus == "CLOSED"){
				current.alertstyle = 'cfw-excellent';
		}else if(current.eventStatus == "OPEN"){
			current.alertstyle = 'cfw-emergency';
		}else {
			current.alertstyle = 'cfw-none';
		}  
		
		if(current.alertstyle != "cfw-none"){
			current.textstyle = "white"; 
		}
	}
	
	//---------------------------
	// Render Settings
	var dataToRender = {
		data: data.payload,
		visiblefields: ["entityName", "eventType", "eventStatus", "startTime", "endTime"],
		titlefields: ["eventStatus", "eventType"], 
		titleformat: '{0} - {1}', 
		bgstylefield: 'alertstyle',
		textstylefield: 'textstyle',
		labels:{
			entityName: 'Entity Name',
			eventType: 'Event Type',
			eventStatus: 'Status',
			startTime: 'Start',
			endTime: 'End',
		},
		customizers:{
			metadata: function(record, value) {  return JSON.stringify(value.executables); },
			startTime: function(record, value) {  return CFW.format.epochToTimestamp(value); },
			endTime: function(record, value) {  return (value == -1) ? '&nbsp;' : CFW.format.epochToTimestamp(value); },
		},
		rendererSettings:{
			tiles: {
				sizefactor: settings.sizefactor,
				showlabels: settings.showlabels,
				borderstyle: settings.borderstyle
			},
			table: {
				narrow: 	true,
				striped: 	false,
				hover: 		false,
				filterable: false,
			},
			panels: {
				narrow: 	true,
			},
			cards: {
				narrow: 	true,
				maxcolumns: 5,
			},
			
	}};
	
	//-----------------------------------
	// Adjust RenderSettings
	var renderType = (settings.renderer == null) ? "tiles" : settings.renderer.toLowerCase();
	if(renderType == "tiles"){
			dataToRender.visiblefields = ["eventStatus", "startTime", "endTime"];
	}
	
	//--------------------------
	// Render Widget
	var alertRenderer = CFW.render.getRenderer(renderType);
	 return callback(widgetObject, alertRenderer.render(dataToRender));
}