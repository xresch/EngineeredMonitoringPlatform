(function (){
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerCategory("fas fa-desktop", "Monitoring");
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_spmmonitorstatus",
		{
			category: "Monitoring",
			menuicon: "fas fa-cogs",
			menulabel: CFWL('cfw_widget_spmmonitorstatus', "SPM Monitor Status"),
			description: CFWL('cfw_widget_spmmonitorstatus_desc', "Fetches the status of one or multiple SPM Monitors."),
			createWidgetInstance: function (widgetObject, callback) {
					
				CFW.dashboard.fetchWidgetData(widgetObject, function(data){
					var monitorStats = data.payload;
					
					for(var key in monitorStats){
						var current = monitorStats[key];
						current.textstyle = "white"; 

						current.VALUE = current.VALUE.toFixed(1);
						if(widgetObject.JSON_SETTINGS.disable) { current.alertstyle = "cfw-darkgray"; }
						else if (current.VALUE == 100) 		{ current.alertstyle = "cfw-excellent"; } 
						
						else if (current.VALUE >= 75) 	{ current.alertstyle = "cfw-good"; } 
						else if (current.VALUE >= 50) 	{ current.alertstyle = "cfw-warning"; } 
						else if (current.VALUE >= 25) 	{ current.alertstyle = "cfw-emergency"; } 
						else if (current.VALUE >= 0)  	{ current.alertstyle = "cfw-danger"; } 
						else if (current.VALUE == 'NaN' 
							  || current.VALUE < 0) { 		  current.alertstyle = "cfw-gray"; } 
						
					}

					var dataToRender = {
						data: data.payload,
						bgstylefield: 'alertstyle',
						textstylefield: 'textstyle',
						titlefields: ['MONITOR_NAME'], 
						titledelimiter: ' - ', 
						visiblefields: ['PROJECT_NAME', 'VALUE'], 
						labels: {
							PROJECT_NAME: 'Project'
						},
						customizers: {
					 		VALUE: function(record, value) { 
					 			if(value >= 0){
					 				return  value + '%'; 
					 			}else if(value == -1){
					 				return "No Data";
					 			}else {
					 				return "Unknown";
					 			}
							}
					 	},
						rendererSettings:{
							tiles: {
								sizefactor: widgetObject.JSON_SETTINGS.sizefactor,
								showlabels: widgetObject.JSON_SETTINGS.showlabels
							}
					}};
					
					//--------------------------
					// Create Tiles
					if(  data.payload == null || typeof data.payload == 'string'){
						callback(widgetObject, "unknown");
					}else{
						var alertRenderer = CFW.render.getRenderer('tiles');
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