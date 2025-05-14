(function (){
		
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_awajobstatus",
		{
			category: CATEGORY_AWA,
			menuicon: "fas fa-traffic-light",
			menulabel: CFWL('emp_widget_awajobstatus', "AWA Job Status - Current"),
			description: CFWL('emp_widget_awajobstatus_desc', "Fetches the current status of one or multiple AWA Jobs."),
			createWidgetInstance: function (widgetObject, params, callback) {
					
				CFW.dashboard.fetchWidgetData(widgetObject, params, function(data){
					var jobStats = data.payload;
					
					if(data.payload == null){
						return callback(widgetObject, "");
					}
					
					for(var key in jobStats){
						var current = jobStats[key];
						var status = current.STATUS;
						current.textstyle = "white"; 
						if(status == null){
							current.STATUS = "UNKNOWN"; 
						}
						
						//--------------------
						// Check Disabled
						if(widgetObject.JSON_SETTINGS.disable) { 
							current.alertstyle = "cfw-darkgray"; 
							continue;
						}
						
						//--------------------
						// Check Last Run
						var lastRunMinutes = widgetObject.JSON_SETTINGS.last_run_minutes;
						if (lastRunMinutes != null && lastRunMinutes > 0){
							var currentTimeMinutes = new Date().getTime() / 1000 / 60;
							var endTimeMinutes = current.END_TIME /1000 / 60;
							var delta = currentTimeMinutes - endTimeMinutes;
							if(delta > lastRunMinutes){
								current.STATUS = "OVERDUE ("+current.STATUS+")";
							}
						}
						
						//--------------------
						// Get Status Color
						current.alertstyle = emp_widget_awa_getStatusStyle(current.STATUS);
						
					}
					
					var dataToRender = {
						data: data.payload,
						bgstylefield: 'alertstyle',
						textstylefield: 'textstyle',
						titlefields: ['LABEL'], 
						titleformat: '{0}', 
						visiblefields: ['END_TIME', 'STATUS'], 
						customizers: {
							START_TIME: function(record, value) { return (value != null) ? new moment(value).format("YYYY-MM-DD HH:mm") : '';},
							END_TIME: function(record, value) { return (value != null) ? new moment(value).format("YYYY-MM-DD HH:mm") : '';},
							URL: function(record, value) { 
					 			if(value != null && value != ""){
					 				return  '<a class="btn btn-sm btn-primary ml-2" role="button" target="_blank" href="'+value+'" ><i class="fas fa-external-link-square-alt"></i>&nbsp;Open in AWA</a>'; 
					 			}else {
					 				return "&nbsp;";
					 			}
							},
						},
						rendererSettings:CFW.dashboard.createStatusWidgetRendererSettings(widgetObject.JSON_SETTINGS)
					};
					
					//--------------------------
					// Adjust Settings for Renderers
					var renderType = widgetObject.JSON_SETTINGS.renderer;
					if(renderType == null){ renderType = 'tiles'};
					renderType = renderType.toLowerCase();
					
					if(renderType == "table"){
						dataToRender.visiblefields = ['LABEL','JOBNAME', 'STATUS']; 
					}else if(renderType != "tiles"){
						dataToRender.visiblefields = ['LABEL','JOBNAME', 'STATUS', 'STATUS_CODE', 'START_TIME', 'END_TIME', 'TYPE', 'URL']; 
					}
					
					//--------------------------
					// Create Tiles
					
					if(  data.payload == null || typeof data.payload == 'string'){
						callback(widgetObject, "");
					}else{
						
						
						var alertRenderer = CFW.render.getRenderer(renderType);
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