(function (){
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerCategory("fas fa-desktop", "Monitoring | SPM");
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_spmmonitorstatus",
		{
			category: "Monitoring | SPM",
			menuicon: "fas fa-cogs",
			menulabel: CFWL('emp_widget_spmmonitorstatus', "SPM Monitor Status"),
			description: CFWL('emp_widget_spmmonitorstatus_desc', "Fetches the status of one or multiple SPM Monitors."),
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
						titleformat: '{0}', 
						visiblefields: ['PROJECT_NAME', 'VALUE'], 
						labels: {
							PROJECT_NAME: 'Project',
							PROJECT_URL: 'Link'
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
					// Adjust RenderSettings for Table
					if(widgetObject.JSON_SETTINGS.renderer == "Table"){
						dataToRender.visiblefields = ['PROJECT_NAME', 'MONITOR_NAME', 'VALUE']; 
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