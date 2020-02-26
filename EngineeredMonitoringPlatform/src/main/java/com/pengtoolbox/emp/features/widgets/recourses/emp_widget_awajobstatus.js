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
						titlefields: ['label'], 
						titledelimiter: ' - ', 
						visiblefields: ['jobname', 'status'], 
						rendererSettings:{
							alerttiles: {
								sizefactor: widgetObject.JSON_SETTINGS.sizefactor,
								showlabels: widgetObject.JSON_SETTINGS.showlabels
							}
					}};
					
					//--------------------------
					// Create Tiles
					var alertRenderer = CFW.render.getRenderer('alerttiles');
	
					callback(widgetObject, alertRenderer.render(dataToRender));
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