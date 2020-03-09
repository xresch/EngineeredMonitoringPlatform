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
			menuicon: "fas fa-gears",
			menulabel: CFWL('cfw_dashboard_widget_awajobstatus', "AWA Job Status"),
			description: CFWL('cfw_dashboard_widget_awajobstatus_desc', "Fetches the status of one or multiple AWA Jobs."),
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
				var settingsForm = $(form);
				
				widgetObject.JSON_SETTINGS.jobnames = settingsForm.find('input[name="jobnames"]').val();
				widgetObject.JSON_SETTINGS.joblabels = settingsForm.find('input[name="joblabels"]').val();
				widgetObject.JSON_SETTINGS.environment = settingsForm.find('select[name="environment"]').val();
				widgetObject.JSON_SETTINGS.sizefactor = settingsForm.find('select[name="sizefactor"]').val();
				widgetObject.JSON_SETTINGS.showlabels = ( settingsForm.find('input[name="showlabels"]:checked').val() == "true" )
				
				return true;		
			}
		}
	);	
	
})();