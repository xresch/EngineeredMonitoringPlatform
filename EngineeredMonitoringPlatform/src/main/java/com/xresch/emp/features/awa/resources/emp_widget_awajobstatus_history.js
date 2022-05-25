(function (){
		
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_awajobstatus_history",
		{
			category: CATEGORY_AWA,
			menuicon: "fas fa-traffic-light",
			menulabel: CFWL('emp_widget_awajobstatus_history', "AWA Job Status - History"),
			description: CFWL('emp_widget_awajobstatus_history_desc', "Fetches the last X job statuses of one or multiple AWA Jobs."),
			createWidgetInstance: function (widgetObject, params, callback) {
					
				CFW.dashboard.fetchWidgetData(widgetObject, params, function(data){
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
							END_TIME: function(record, value) { return (value != null) ? new moment(value).format("YYYY-MM-DD HH:mm") : '';},
							URL: function(record, value) { 
					 			if(value != null && value != ""){
					 				return  '<a class="btn btn-sm btn-primary ml-2" role="button" target="_blank" href="'+value+'" ><i class="fas fa-external-link-square-alt"></i>&nbsp;Open in AWA</a>'; 
					 			}else {
					 				return "&nbsp;";
					 			}
							},
						},
						rendererSettings:{
							tiles: {
								sizefactor: settings.sizefactor,
								showlabels: false,
								borderstyle: settings.borderstyle
							},
							statuslist: {
								sizefactor: settings.sizefactor,
								showlabels: false,
								borderstyle: settings.borderstyle
							},
							table: {
								narrow: 	true,
								striped: 	true,
								hover: 		false,
								filterable: false
							},
							panels: {
								narrow: 	true,
							},
							cards: {
								narrow: 	true,
								maxcolumns: 5,
							},
					}};
										
					//---------------------------
					// Chart Render Settings
					var chartRendererSettings = {
						data: null,
						titlefields: ['JOBNAME', 'LABEL'], 
						visiblefields: ['JOBNAME', 'LABEL'], 
						titleformat: '{0} - {1}', 
						rendererSettings:{
							chart: {
								responsive: true,
								charttype: 'area',
								xfield: "END_TIME",
								yfield: 'DURATION_SECONDS',
								stacked: false,
								showlegend: false,
								showaxes: false,
								ymin: 0,
								ymax: null,
								pointradius: 1,
								padding: 0
							}
					}};
					
					//---------------------------------
					// Table Renderer Settings
					let visibileFields = ['JOBNAME'];
					if (settings.showlabels){ visibileFields.push('LABEL'); }
					if (settings.showstatistics){ visibileFields.push('MIN', 'AVG', 'MAX'); }
					if (settings.showsparkline){ visibileFields.push('SUM'); /* Use sum field to display graph */ }
					visibileFields.push('STATUSES');
					
					var tableRendererSettings = {
						data: groupedJobStats,
						bgstylefield: 'alertstyle',
						textstylefield: 'textstyle',
						titlefields: ['LABEL'], 
						titleformat: '{0}', 
						visiblefields: visibileFields, 
						labels: {
							SUM: "Sparkline",
						},
						customizers: {
							MIN: function(record, value) { return CFW.format.millisToClock(value*1000);},
							AVG: function(record, value) { return CFW.format.millisToClock(value*1000);},
							MAX: function(record, value) { return CFW.format.millisToClock(value*1000);},
							STATUSES: function(record, value) {
								tilesRendererSettings.data = value;
								var tilesRenderer = CFW.render.getRenderer('tiles');
								return tilesRenderer.render(tilesRendererSettings);
							},
							SUM: function(record, value) {
								chartRendererSettings.data = record.STATUSES;
								var chartRenderer = CFW.render.getRenderer('chart');

								return $('<div class="w-100" style="height: 20px; max-width: 200px">').append(chartRenderer.render(chartRendererSettings));
								//return chartRenderer.render(chartRendererSettings);
							},
							URL: function(record, value) { 
					 			if(value != null && value != ""){
					 				return  '<a class="btn btn-sm btn-primary ml-2" role="button" target="_blank" href="'+value+'" ><i class="fas fa-external-link-square-alt"></i>&nbsp;Open in AWA</a>'; 
					 			}else {
					 				return "&nbsp;";
					 			}
							},
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