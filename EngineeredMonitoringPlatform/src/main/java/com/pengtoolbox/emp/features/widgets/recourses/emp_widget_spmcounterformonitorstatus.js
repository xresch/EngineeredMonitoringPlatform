(function (){
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerCategory("fas fa-desktop", "Monitoring");
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_spmcounterformonitorstatus",
		{
			category: "Monitoring",
			menuicon: "fas fa-cogs",
			menulabel: CFWL('cfw_widget_spmcounterformonitorstatus', "SPM Counters for Monitor"),
			description: CFWL('cfw_widget_spmcounterformonitorstatus_desc', "Fetches the current count for the defined counter of a single monitor and displays it as defined by the threshold options."), 
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

						current.VALUE = current.VALUE.toFixed(1);
						if(direction == 'HIGH_TO_LOW'){
							
							if(settings.disable) { current.alertstyle = "cfw-darkgray"; }
							else if (!CFW.utils.isNullOrEmpty(excellentVal) && current.VALUE >= excellentVal) 	{ current.alertstyle = "cfw-excellent"; } 
							else if (!CFW.utils.isNullOrEmpty(goodVal) && current.VALUE >= goodVal) 			{ current.alertstyle = "cfw-good"; } 
							else if (!CFW.utils.isNullOrEmpty(warningVal) && current.VALUE >= warningVal) 		{ current.alertstyle = "cfw-warning"; } 
							else if (!CFW.utils.isNullOrEmpty(emergencyVal) && current.VALUE >= emergencyVal) 	{ current.alertstyle = "cfw-emergency"; } 
							else if (!CFW.utils.isNullOrEmpty(dangerVal) && current.VALUE >= dangerVal)  		{ current.alertstyle = "cfw-danger"; } 
							else 																				{ current.alertstyle = "cfw-gray"; } 
						}else{
							if(settings.disable) { current.alertstyle = "cfw-darkgray"; }
							else if (!CFW.utils.isNullOrEmpty(dangerVal) && current.VALUE >= dangerVal)  		{ current.alertstyle = "cfw-danger"; } 
							else if (!CFW.utils.isNullOrEmpty(emergencyVal) && current.VALUE >= emergencyVal) 	{ current.alertstyle = "cfw-emergency"; } 
							else if (!CFW.utils.isNullOrEmpty(warningVal) && current.VALUE >= warningVal) 		{ current.alertstyle = "cfw-warning"; } 
							else if (!CFW.utils.isNullOrEmpty(goodVal) && current.VALUE >= goodVal) 			{ current.alertstyle = "cfw-good"; } 
							else if (!CFW.utils.isNullOrEmpty(excellentVal) && current.VALUE >= excellentVal) 	{ current.alertstyle = "cfw-excellent"; } 
							else 																				{ current.alertstyle = "cfw-gray"; } 
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