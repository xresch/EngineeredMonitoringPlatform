(function (){
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerCategory("fas fa-desktop", "Monitoring | Prometheus");
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_prometheus_instant_threshold",
		{
			category: "Monitoring | Prometheus",
			menuicon: "fas fa-thermometer-half",
			menulabel: CFWL('emp_widget_prometheus_instant_threshold', "Instant Threshold"),
			description: CFWL('emp_widget_prometheus_instant_threshold_desc', "A prometheus query that will be used to retrieve the last value(instant) of the matched metrics and colors it by the specified threshold."), 
			createWidgetInstance: function (widgetObject, callback) {
					
				CFW.dashboard.fetchWidgetData(widgetObject, function(data){
					
					var settings = widgetObject.JSON_SETTINGS;
					var renderType = (settings.renderer == null) ? "tiles" : settings.renderer.toLowerCase() ;
					var monitorStats = [];
					
					//---------------------------------
					// Check for Data and Errors
					if(CFW.utils.isNullOrEmpty(data.payload) || typeof data.payload == 'string' || data.payload.length == null){
						callback(widgetObject, '');
						return;
					}
					
					//---------------------------------
					// Prepare Prometheus data
					var monitorStats = emp_widget_prometheus_prepareData(data.payload);
					
					//---------------------------
					// Set Colors for Thresholds
					var excellentVal = settings.threshold_excellent;
					var goodVal = settings.threshold_good;
					var warningVal = settings.threshold_warning;
					var emergencyVal = settings.threshold_emergency;
					var dangerVal = settings.threshold_danger;
					
					for(var key in monitorStats){
						var current = monitorStats[key];
						
						current.alertstyle =  CFW.colors.getThresholdStyle(current.value
								,excellentVal
								,goodVal
								,warningVal
								,emergencyVal
								,dangerVal
								,settings.disable);
						
						if(current.alertstyle != "cfw-none"){
							current.textstyle = "white"; 
						}
					}
					
					//---------------------------
					// Render Settings
					var dataToRender = {
						data: monitorStats,
						bgstylefield: 'alertstyle',
						textstylefield: 'textstyle',
						titlefields: ['value'], 
						visiblefields: [],
						titledelimiter: ' - ', 
						
						labels: {},
						customizers: {
							value: function(record, value) {
								if(value == null) return '';
								return (settings.suffix == null) ? value : value+" "+settings.suffix;
							},
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
							},
					}};
					
					//-----------------------------------
					// Adjust RenderSettings
					if(dataToRender.data.length > 0){
						if( (renderType == "table" || renderType == "panels")){
							
							var visiblefields = Object.keys(dataToRender.data[0]);
							//remove alertstyle and textstyle
							visiblefields.pop();
							visiblefields.pop();
							dataToRender.visiblefields = visiblefields;
							// add first field to title
							dataToRender.titlefields.push(visiblefields[0]); 			
						}
					}
					
					
					//--------------------------
					// Render Widget
					var alertRenderer = CFW.render.getRenderer(renderType);
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
	
})();;