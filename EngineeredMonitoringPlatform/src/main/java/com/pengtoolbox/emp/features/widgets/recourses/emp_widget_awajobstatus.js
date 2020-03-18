(function (){
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerCategory("fas fa-desktop", "Monitoring");
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_awajobstatus",
		{
			category: "Monitoring",
			menuicon: "fas fa-cogs",
			menulabel: CFWL('cfw_widget_awajobstatus', "AWA Job Status"),
			description: CFWL('cfw_widget_awajobstatus_desc', "Fetches the status of one or multiple AWA Jobs."),
			createWidgetInstance: function (widgetObject, callback) {
					
				CFW.dashboard.fetchWidgetData(widgetObject, function(data){
					var jobStats = data.payload;
					
					for(var key in jobStats){
						var current = jobStats[key];
						current.textstyle = "white"; 
						if(current.status == null){
							current.status == "UNKNOWN";
							current.alertstyle = "cfw-gray"; 
							continue;
						}
						if(widgetObject.JSON_SETTINGS.disable) { 
							current.alertstyle = "cfw-darkgray"; 
							continue;
						}
						
						switch(current.status.toUpperCase()){
							case "RUNNING": 	current.alertstyle = "cfw-warning"; 
												break;
							case "ENDED OK": 	current.alertstyle = "cfw-excellent"; 
												break;
							case "ISSUE": 		current.alertstyle = "cfw-danger"; 
												break;
						}
					}
					
					var dataToRender = {
						data: data.payload,
						bgstylefield: 'alertstyle',
						textstylefield: 'textstyle',
						titlefields: ['label'], 
						titledelimiter: ' - ', 
						visiblefields: ['jobname', 'status'], 
						rendererSettings:{
							tiles: {
								sizefactor: widgetObject.JSON_SETTINGS.sizefactor,
								showlabels: widgetObject.JSON_SETTINGS.showlabels
							},
							table: {
								narrow: 	true,
								striped: 	false,
								hover: 		false,
								filterable: false
							}
					}};
					
					if(widgetObject.JSON_SETTINGS.renderer == "Table"){
						dataToRender.visiblefields = ['label','jobname', 'status']; 
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