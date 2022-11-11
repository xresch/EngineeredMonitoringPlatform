(function (){
		
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_awajobstatusgrouped",
		{
			category: CATEGORY_AWA,
			menuicon: "fas fa-traffic-light",
			menulabel: CFWL('emp_awajobstatusgrouped', "AWA Job Status - Current Grouped"),
			description: CFWL('emp_awajobstatusgrouped_desc', "Fetches the current status of one or multiple AWA Jobs and displays them as a single status."),
			usetimeframe: true,
			createWidgetInstance: function (widgetObject, params, callback) {
					
				CFW.dashboard.fetchWidgetData(widgetObject, params, function(data){

					//---------------------------------
					// Check Payload
					var jobStats = data.payload;
					if(data.payload == null || typeof data.payload == 'string'){
						return callback(widgetObject, "");
					}
					
					//---------------------------------
					// Iterate All Jobs
					var worstStatusStyle = 'cfw-excellent';
					var worstStatusText  = "ENDED OK";
					
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
						
						if(worstStatusStyle != 'cfw-danger'){
							switch(current.alertstyle){
								
								case 'cfw-danger': 
									worstStatusStyle = 'cfw-danger';
									worstStatusText  = "ABEND / BLOCKED";
								break;
								
								case 'cfw-emergency': 
									worstStatusStyle = 'cfw-emergency';
									worstStatusText  = "OVERDUE";
								break;
								
								default:
									//do nothing
								break;
							}
						}
					}
					
					//---------------------------------
					// Create Grouped Data for Rendering
					var groupedData = [{
						textstyle: "white",
						alertstyle: worstStatusStyle,
						WORST_STATUS: worstStatusText,
						JOBS: jobStats
					}];
					
					//---------------------------------
					// Create Tiles Renderer
					var dataToRender = {
						data: groupedData,
						bgstylefield: 'alertstyle',
						textstylefield: 'textstyle',
						titlefields: ['WORST_STATUS'], 
						titleformat: '{0}', 
						visiblefields: ['NONE'], 
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
							JOBS: function(record, value) { 
					 			if(value != null && value != ""){
						
									var joblistRenderData = Object.assign({}, dataToRender);
									joblistRenderData.data = value;
									joblistRenderData.titlefields = ['LABEL'];
									joblistRenderData.visiblefields = ['END_TIME', 'STATUS']; 
									joblistRenderData.rendererSettings.tiles.showlabels = false; 
									joblistRenderData.rendererSettings.tiles.sizefactor = 0.5; 
									
					 				return  CFW.render.getRenderer("tiles").render(joblistRenderData); 
								
					 			}else {
					 				return "&nbsp;";
					 			}
							},
						},
						rendererSettings: CFW.dashboard.createStatusWidgetRendererSettings(widgetObject.JSON_SETTINGS)
					};

					//--------------------------
					// Create Tiles and Status Bar

					var tiles = CFW.render.getRenderer('tiles').render(dataToRender);
					tiles.removeClass('h-100');
					tiles.css('height', '90%');
					tiles.find('div').css('margin', '00px');
					
					var joblistStatusBarData = Object.assign({}, dataToRender);
					joblistStatusBarData.data = jobStats;
					var statusbar = CFW.render.getRenderer('statusbar').render(joblistStatusBarData);
					statusbar.css('height', '10%');
					
					var tileAndBarWrapper = $('<div class="d-flex-column w-100 h-100">');
					tileAndBarWrapper.append(tiles);
					tileAndBarWrapper.append(statusbar);
					
					callback(widgetObject, tileAndBarWrapper);

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