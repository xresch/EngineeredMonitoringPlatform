(function (){
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerCategory("fas fa-desktop", "Monitoring | AWA");
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_awajobswithstatus",
		{
			category: "Monitoring | AWA",
			menuicon: "fas fa-traffic-light",
			menulabel: CFWL('cfw_widget_awajobswithstatus', "AWA Jobs with Status"),
			description: CFWL('cfw_widget_awajobswithstatus_desc', "Fetches the jobs with the specified status and the given filter(s)."),
			createWidgetInstance: function (widgetObject, callback) {
					
				CFW.dashboard.fetchWidgetData(widgetObject, function(data){
					var jobStats = data.payload;
					
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
						// Get Status Color
						current.alertstyle = emp_widget_awa_getStatusStyle(current.STATUS);
						
					}
					
					var dataToRender = {
						data: data.payload,
						bgstylefield: 'alertstyle',
						textstylefield: 'textstyle',
						titlefields: ['JOBNAME'], 
						titledelimiter: ' - ', 
						visiblefields: ['END_TIME', 'DURATION_SECONDS'], 
						labels: {
							DURATION_SECONDS: 'Duration(s)'
						},
						customizers: {
							START_TIME: function(record, value) { return (value != null) ? new CFWDate(value).getDateFormatted("YYYY-MM-DD HH:mm") : '';},
							END_TIME: function(record, value) { return (value != null) ? new CFWDate(value).getDateFormatted("YYYY-MM-DD HH:mm") : '';}
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
								filterable: false
							}
					}};
					
					//--------------------------
					// Adjust Settings for Table
					if(widgetObject.JSON_SETTINGS.renderer == "Table"){
						dataToRender.visiblefields = ['JOBNAME', 'START_TIME', 'END_TIME', 'DURATION_SECONDS']; 
					}
					
					//--------------------------
					// Create Tiles
					if(  data.payload == null || typeof data.payload == 'string'){
						callback(widgetObject, "");
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