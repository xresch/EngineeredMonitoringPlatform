(function (){
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerCategory("fas fa-desktop", "Monitoring | SPM");
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_spmcounterforprojectstatus",
		{
			category: "Monitoring | SPM",
			menuicon: "fas fa-cogs",
			menulabel: CFWL('cfw_widget_spmcounterforprojectstatus', "SPM Counters for Project Status"),
			description: CFWL('cfw_widget_spmcounterforprojectstatus_desc', "Fetches the current count for the defined counters of a single project and displays it as defined by the threshold options."), 
			createWidgetInstance: function (widgetObject, callback) {
					
				CFW.dashboard.fetchWidgetData(widgetObject, function(data){
					var monitorStats = data.payload;
					var settings = widgetObject.JSON_SETTINGS;
					
					var excellentVal = settings.threshold_excellent;
					var goodVal = settings.threshold_good;
					var warningVal = settings.threshold_warning;
					var emergencyVal = settings.threshold_emergency;
					var dangerVal = settings.threshold_danger;

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
								,settings.disable);
						
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
						titledelimiter: ' - ', 
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