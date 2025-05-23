(function (){
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_prometheus_instant_threshold",
		{
			category: CATEGORY_PROMETHEUS,
			menuicon: "fas fa-thermometer-half",
			menulabel: CFWL('emp_widget_prometheus_instant_threshold', "Instant Threshold"),
			description: CFWL('emp_widget_prometheus_instant_threshold_desc', "A prometheus query that will be used to retrieve the last value(instant) of the matched metrics and colors it by the specified threshold."), 
			createWidgetInstance: function (widgetObject, params, callback) {
					
				CFW.dashboard.fetchWidgetData(widgetObject, params, function(data){
					
					var settings = widgetObject.JSON_SETTINGS;
					var renderType = (settings.renderer == null) ? "tiles" : settings.renderer.toLowerCase() ;

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
					var excellentVal 	= settings.THRESHOLD_GREEN;
					var goodVal 		= settings.THRESHOLD_LIMEGREEN;
					var warningVal 		= settings.THRESHOLD_YELLOW;
					var emergencyVal 	= settings.THRESHOLD_ORANGE;
					var dangerVal 		= settings.THRESHOLD_RED;
					
					for(var key in monitorStats){
						var current = monitorStats[key];
						
						current.alertstyle =  CFW.colors.getThresholdStyle(current.value
								,excellentVal
								,goodVal
								,warningVal
								,emergencyVal
								,dangerVal
								,settings.THRESHOLD_DISABLED);
						
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
						titleformat: '{0}', 
						
						labels: {},
						customizers: {
							value: function(record, value) {
								if(value == null) return '';
								return (settings.suffix == null) ? value : value+" "+settings.suffix;
							},
							time: function(record, value) { return (value != null) ? new  moment(value).format("YYYY-MM-DD HH:mm") : '';},
						},
						rendererSettings: CFW.dashboard.createStatusWidgetRendererSettings(settings)
					};
					
					//-----------------------------------
					// Adjust RenderSettings
					if(dataToRender.data.length > 0){
						if( (renderType == "table" 
							|| renderType == "panels"
							|| renderType == "cards"
							|| renderType == "csv"
							|| renderType == "json")){
							
							var visiblefields = Object.keys(dataToRender.data[0]);
							//remove alertstyle and textstyle
							visiblefields.pop();
							visiblefields.pop();
							dataToRender.visiblefields = visiblefields;
							// add first field to title
							dataToRender.titlefields.push(visiblefields[0]); 	
							dataToRender.titleformat = '{0} - {1}';
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