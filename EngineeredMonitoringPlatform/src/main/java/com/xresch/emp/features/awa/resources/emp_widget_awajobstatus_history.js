(function (){
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerCategory("fas fa-desktop", "Monitoring | AWA");
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_awajobstatus_history",
		{
			category: "Monitoring | AWA",
			menuicon: "fas fa-traffic-light",
			menulabel: CFWL('cfw_widget_awajobstatus_history', "AWA Job Status - History"),
			description: CFWL('cfw_widget_awajobstatus_history_desc', "Fetches the last X job statuses of one or multiple AWA Jobs."),
			createWidgetInstance: function (widgetObject, callback) {
					
				CFW.dashboard.fetchWidgetData(widgetObject, function(data){
					var jobStats = data.payload;
					var settings =  widgetObject.JSON_SETTINGS;
					
					//---------------------------------
					// Evaluate Colors
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
					
					//---------------------------------
					// Group by Jobnames
					var groupedJobStats = emp_widget_awa_groupByJobname(jobStats);
					
					//---------------------------------
					// Tile Renderer Settings
					var tilesRendererSettings = {
						data: null,
						bgstylefield: 'alertstyle',
						textstylefield: 'textstyle',
						titlefields: ['LABEL'], 
						titleformat: '{0}', 
						visiblefields: ['END_TIME', 'STATUS'], 
						customizers: {
							START_TIME: function(record, value) { return (value != null) ? new moment(value).format("YYYY-MM-DD HH:mm") : '';},
							END_TIME: function(record, value) { return (value != null) ? new moment(value).format("YYYY-MM-DD HH:mm") : '';}
						},
						rendererSettings:{
							tiles: {
								sizefactor: settings.sizefactor,
								showlabels: false,
								borderstyle: settings.borderstyle
							},
					}};
					
					console.log(groupedJobStats)
					//---------------------------------
					// Table Renderer Settings
					let visibileFields = ['JOBNAME'];
					if (settings.showlabels){ visibileFields.push('LABEL'); }
					if (settings.showstatistics){ visibileFields.push('MIN', 'AVG', 'MAX'); }
					visibileFields.push('STATUSES');
					
					var tableRendererSettings = {
						data: groupedJobStats,
						bgstylefield: 'alertstyle',
						textstylefield: 'textstyle',
						titlefields: ['LABEL'], 
						titleformat: '{0}', 
						visiblefields: visibileFields, 
						customizers: {
							MIN: function(record, value) { return CFW.format.millisToClock(value*1000);},
							AVG: function(record, value) { return CFW.format.millisToClock(value*1000);},
							MAX: function(record, value) { return CFW.format.millisToClock(value*1000);},
							STATUSES: function(record, value) {
								tilesRendererSettings.data = value;
								var tilesRenderer = CFW.render.getRenderer('tiles');
								return tilesRenderer.render(tilesRendererSettings);
							}
						},
						rendererSettings:{
							table: {
								narrow: 	true,
								striped: 	true,
								hover: 		false,
								filterable: true
							}
					}};
										
					//--------------------------
					// Render
					if(  data.payload == null || typeof data.payload == 'string'){
						callback(widgetObject, "");
					}else{						
						var tableRenderer = CFW.render.getRenderer('table');
						callback(widgetObject, tableRenderer.render(tableRendererSettings));
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