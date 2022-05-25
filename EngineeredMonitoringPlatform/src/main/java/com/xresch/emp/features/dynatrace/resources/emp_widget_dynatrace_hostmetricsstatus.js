
(function (){
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_dynatrace_hostmetricsstatus",
		{
			category: DYNATRACE_WIDGET_CATEGORY,
			menuicon: "fas fa-thermometer-half",
			menulabel: CFWL('emp_widget_dynatrace_hostmetricsstatus', "Host Metrics Status"),
			description: CFWL('emp_widget_dynatrace_hostmetricsstatus_desc', "Displays a chart for the selected metrics and the selected host."), 
			usetimeframe: true,
			createWidgetInstance: function (widgetObject, params, callback) {
					
				CFW.dashboard.fetchWidgetData(widgetObject, params, function(data){
					
					var settings = widgetObject.JSON_SETTINGS;				
					 
					//---------------------------------
					// Check for Data and Errors
					if(CFW.utils.isNullOrEmpty(data.payload) || typeof data.payload == 'string'){
						callback(widgetObject, '');
						return;
					}
					
					if(data.payload.error != null){
						callback(widgetObject, '<p> An error occured: '+data.payload.error.message);
						return;
					}
					
					//---------------------------------
					// Prepare data
					preparedData = emp_dynatrace_prepareMetricData(data.payload.result, true);
					
					console.log(preparedData);
					
					//---------------------------
					// Set Colors for Thresholds
					var excellentVal = settings.THRESHOLD_EXCELLENT;
					var goodVal = settings.THRESHOLD_GOOD;
					var warningVal = settings.THRESHOLD_WARNING;
					var emergencyVal = settings.THRESHOLD_EMERGENCY;
					var dangerVal = settings.THRESHOLD_DANGER;
					var isDisabled = settings.THRESHOLD_DISABLED;
					
					for(var key in preparedData){
						var current = preparedData[key];
						var duration = current["value"];

						current.alertstyle =  CFW.colors.getThresholdStyle(duration
							,excellentVal
							,goodVal
							,warningVal
							,emergencyVal
							,dangerVal
							,isDisabled);
						
						
						if(current.alertstyle != "cfw-none"){
							current.textstyle = "white"; 
						}
					}
					
					
					//---------------------------
					// Visible Fields
					
					//---------------------------
					// Render Settings
					var dataToRender = {
						data: preparedData,
						bgstylefield: 'alertstyle',
						textstylefield: 'textstyle',
						titlefields: ["value"], 
						visiblefields: [],
						titleformat: null, 
						
						labels: {},
						customizers: {
							metric:function(record, value) {
								if(value == null) return '';
								if(value.includes(':lastReal')){
									value = value.split(':lastReal')[0];
								}
								
								return value;
							},
							value: function(record, value) {
								if(value == null) return '';
								
								if(!isNaN(value)) value = value.toFixed(2);
								
								return (settings.suffix == null) ? value : value+settings.suffix;
							},
							
							time: function(record, value) { return (value != null) ? new  moment(value).format("YYYY-MM-DD HH:mm") : '';},
							
						},
						rendererSettings: CFW.dashboard.createStatusWidgetRendererSettings(settings) };
								
					//--------------------------
					// Render Widget
					var renderType = settings.renderer;
					var renderer = CFW.render.getRenderer(renderType);
					
					if(renderType != "tiles"){
						dataToRender.visiblefields = ["metric", "value"];
					}	
					
					callback(widgetObject, renderer.render(dataToRender));
				});
			},
		}
	);	
	
})();