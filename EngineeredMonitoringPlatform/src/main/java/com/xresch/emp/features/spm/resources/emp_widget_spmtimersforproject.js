(function (){
		
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_spmtimersforprojectstatus",
		{
			category: CATEGORY_SPM,
			menuicon: "fas fa-cogs",
			menulabel: CFWL('emp_widget_spmtimersforprojectstatus', "SPM Timers for Project"),
			description: CFWL('emp_widget_spmtimersforprojectstatus_desc', "Fetches the current value for the defined timers of a single project and displays it as defined by the threshold options."), 
			createWidgetInstance: function (widgetObject, params, callback) {
					
				CFW.dashboard.fetchWidgetData(widgetObject, params, function(data){
					var monitorStats = data.payload;
					var settings = widgetObject.JSON_SETTINGS;
					
					var excellentVal = settings.THRESHOLD_GREEN;
					var goodVal = settings.THRESHOLD_LIMEGREEN;
					var warningVal = settings.THRESHOLD_YELLOW;
					var emergencyVal = settings.THRESHOLD_ORANGE;
					var dangerVal = settings.THRESHOLD_RED;
					
					//---------------------------
					// Set Colors for Thresholds
					for(var key in monitorStats){
						var current = monitorStats[key];
						current.VALUE = current.VALUE.toFixed(1);
						
						current.alertstyle =  CFW.colors.getThresholdStyle(current.VALUE
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
						data: data.payload,
						bgstylefield: 'alertstyle',
						textstylefield: 'textstyle',
						titlefields: ['MEASURE_NAME'], 
						titleformat: '{0}', 
						visiblefields: ['MONITOR_NAME', 'VALUE'], 
						labels: {
							COUNTER_NAME: 'Counter',
							MONITOR_NAME: 'Monitor',
							PROJECT_NAME: 'Project',
							PROJECT_URL: 'Link'
						},
						customizers: {
					 		VALUE: function(record, value) { 
					 			if(value >= 0){
					 				return  value; 
					 			}else if(value == -1){
					 				return "No Data";
					 			}else {
					 				return "Unknown";
					 			}
							},
							PROJECT_URL: function(record, value) { 
					 			if(value != null && value != ""){
					 				return  '<a class="btn btn-sm btn-primary ml-2" role="button" target="_blank" href="'+value+'" ><i class="fas fa-external-link-square-alt"></i> Open SPM Project</a>'; 
					 			}else {
					 				return "&nbsp;";
					 			}
							},
					 	},
						rendererSettings: CFW.dashboard.createStatusWidgetRendererSettings(settings)
					};
					
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