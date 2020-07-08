(function (){
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerCategory("fas fa-desktop", "Monitoring | Prometheus | General");
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_prometheus_general_query",
		{
			category: "Monitoring | Prometheus | General",
			menuicon: "fas fa-cogs",
			menulabel: CFWL('emp_widget_prometheus_general_query', "Query"),
			description: CFWL('emp_widget_prometheus_general_query_desc', "This widget uses a prometheus query to fetch the last value(instant) of the matched metrics."), 
			createWidgetInstance: function (widgetObject, callback) {
					
				CFW.dashboard.fetchWidgetData(widgetObject, function(data){
					var monitorStats = [];
					
					//---------------------------------
					// Check for Error
					if(data.payload.error != undefined){
						callback(widgetObject, "<p><b>Prometheus Error: </b>"+data.payload.error+'</p>');
						return
					}
					
					//---------------------------------
					// Prepare Prometheus data
					var prometheusData = data.payload.data; 
					if(prometheusData != null && prometheusData.result != null){

						for(index in prometheusData.result){

							var current = prometheusData.result[index];
							console.log(current);
							var item = Object.assign({}, current.metric);
							item.time = current.value[0];
							
							item.value = current.value[1];
							if(!isNaN(item.value)){
								item.value = parseFloat(item.value).toFixed(1);
							}
							monitorStats.push(item);
						}
					}


					var settings = widgetObject.JSON_SETTINGS;
					var excellentVal = settings.threshold_excellent;
					var goodVal = settings.threshold_good;
					var warningVal = settings.threshold_warning;
					var emergencyVal = settings.threshold_emergency;
					var dangerVal = settings.threshold_danger;
					
					//---------------------------
					// Find Threshold direction
					var direction = 'HIGH_TO_LOW';
					var thresholds = [excellentVal, goodVal, warningVal, emergencyVal, dangerVal];
					var firstDefined = null;
					
					for(var i = 0; i < thresholds.length; i++){
						var current = thresholds[i];
						if (!CFW.utils.isNullOrEmpty(current)){
							if(firstDefined == null){
								firstDefined = current;
							}else{
								if(current != null && firstDefined < current ){
									direction = 'LOW_TO_HIGH';
								}
								break;
							}
						}
					}
					
					//---------------------------
					// Set Colors for Thresholds
					
					for(var key in monitorStats){
						var current = monitorStats[key];
						current.textstyle = "white"; 
						current.alertstyle = "cfw-gray";
						
						if(!isNaN(current.value)){
							if(direction == 'HIGH_TO_LOW'){
								
								if(settings.disable) { current.alertstyle = "cfw-darkgray"; }
								else if (!CFW.utils.isNullOrEmpty(excellentVal) && current.value>= excellentVal) 	{ current.alertstyle = "cfw-excellent"; } 
								else if (!CFW.utils.isNullOrEmpty(goodVal) && current.value>= goodVal) 			{ current.alertstyle = "cfw-good"; } 
								else if (!CFW.utils.isNullOrEmpty(warningVal) && current.value>= warningVal) 		{ current.alertstyle = "cfw-warning"; } 
								else if (!CFW.utils.isNullOrEmpty(emergencyVal) && current.value>= emergencyVal) 	{ current.alertstyle = "cfw-emergency"; } 
								else if (!CFW.utils.isNullOrEmpty(dangerVal) && current.value>= dangerVal)  		{ current.alertstyle = "cfw-danger"; } 
								else 																				{ current.alertstyle = "cfw-gray"; } 
							}else{
								if(settings.disable) { current.alertstyle = "cfw-darkgray"; }
								else if (!CFW.utils.isNullOrEmpty(dangerVal) && current.value>= dangerVal)  		{ current.alertstyle = "cfw-danger"; } 
								else if (!CFW.utils.isNullOrEmpty(emergencyVal) && current.value>= emergencyVal) 	{ current.alertstyle = "cfw-emergency"; } 
								else if (!CFW.utils.isNullOrEmpty(warningVal) && current.value>= warningVal) 		{ current.alertstyle = "cfw-warning"; } 
								else if (!CFW.utils.isNullOrEmpty(goodVal) && current.value>= goodVal) 			{ current.alertstyle = "cfw-good"; } 
								else if (!CFW.utils.isNullOrEmpty(excellentVal) && current.value>= excellentVal) 	{ current.alertstyle = "cfw-excellent"; } 
								else 																				{ current.alertstyle = "cfw-gray"; } 
							}
						}
					}
					
					//---------------------------
					// Render Settings
					var dataToRender = {
						data: monitorStats,
						bgstylefield: 'alertstyle',
						textstylefield: 'textstyle',
						titlefields: ['value'], 
						titledelimiter: ' - ', 
						visiblefields: [], 
						labels: {},
						customizers: {
							time: function(record, value) { return (value != null) ? new CFWDate(value).getDateFormatted("YYYY-MM-DD HH:mm") : '';},
						},
						rendererSettings:{
							tiles: {
								sizefactor: widgetObject.JSON_SETTINGS.sizefactor,
								showlabels: widgetObject.JSON_SETTINGS.showlabels,
								borderstyle: widgetObject.JSON_SETTINGS.borderstyle
							},
							table: {
								narrow: 	true,
								striped: 	false,
								hover: 		false,
								filterable: false,
							}
					}};
					
					//-----------------------------------
					// Adjust RenderSettings for Table
					if(widgetObject.JSON_SETTINGS.renderer == "Table"){
						dataToRender.visiblefields = ['PROJECT_NAME', 'MONITOR_NAME', 'MEASURE_NAME', 'VALUE']; 
						dataToRender.customizers.PROJECT_NAME = function(record, value) { 
				 			if(value != null && value != ""){
				 				return  '<a style="color: inherit;" target="_blank" href="'+record.PROJECT_URL+'" >'+value+'</a>'; 
				 			}else {
				 				return "&nbsp;";
				 			}
						};
					}
					
					//--------------------------
					// Create Tiles
					if(  data.payload == null || typeof data.payload == 'string'){
						callback(widgetObject, "unknown");
					}else{
						
						var renderType = widgetObject.JSON_SETTINGS.renderer;
						if(renderType == null){ renderType = 'tiles'};
						
						var alertRenderer = CFW.render.getRenderer(renderType.toLowerCase());
						callback(widgetObject, alertRenderer.render(dataToRender));
					}
					
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