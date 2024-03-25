(function (){
		
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_step_schedulerstatushistory",
		{
			category: CATEGORY_EXENSE,
			menuicon: "fas fa-traffic-light",
			menulabel: CFWL('emp_widget_step_schedulerstatushistory', "Scheduler Status History"),
			description: CFWL('emp_widget_step_schedulerstatushistory_desc', "Displays a table with status history for the selected schedulers."),
			createWidgetInstance: function (widgetObject, params, callback) {
					
				CFW.dashboard.fetchWidgetData(widgetObject, params, function(data){
					
					var settings = widgetObject.JSON_SETTINGS;
					var renderType = (settings.renderer == null) ? "tiles" : settings.renderer.toLowerCase() ;
					
					//---------------------------------
					// Check for Data and Errors
					if(CFW.utils.isNullOrEmpty(data.payload) || typeof data.payload == 'string' || data.payload.length == 0){
						callback(widgetObject, '');
						return;
					}							
					
					var stepURL = data.url;
					if(!CFW.utils.isNullOrEmpty(stepURL) && !stepURL.endsWith("/")){
						stepURL += "/";
					}
					
					//---------------------------
					// Set Colors for Thresholds
					for(var key in data.payload){
						
						var current = data.payload[key];
						current.statusstyle = emp_step_getStatusStyle(
												  current.result
												, current.duration
												, settings
												, 'status'
											);
						current.performancestyle = emp_step_getStatusStyle(
												  current.result
												, current.duration
												, settings
												, 'duration'
											);
						
					}
					
					//---------------------------------
					// Group by Jobnames
					var groupedJobStats = emp_step_groupByScheduler(data.payload);
					
					//---------------------------------
					// Status Bar Renderer Settings
					var statusbarRendererSettings = {
						data: null,
						//bgstylefield: 'alertstyle',
						textstylefield: 'textstyle',
						titlefields: ['schedulername'], 
						titleformat: '{0}', 
						visiblefields: EMP_STEP_VISIBILEFIELDS, 
						labels: EMP_STEP_LABELS,
						customizers: emp_step_createDefaultCustomizers(stepURL, "statusbar"),
						rendererSettings: {
							statusbar: {	
								//height for the status bar
								height: "100%",
								// min height for the statusbar
								minheight: "20px",
							},
							table: {
								filterable: false
							}
						}
					};
										
					//---------------------------
					// Chart Render Settings
					var chartRendererSettings = {
						data: null,
						titlefields: ['projectname', 'schedulername'], 
						visiblefields: ['projectname', 'schedulername'], 
						titleformat: '{0} - {1}', 
						labels: EMP_STEP_LABELS,
						rendererSettings:{
							chart: {
								responsive: true,
								charttype: 'sparkarea',
								xfield: "endtime",
								yfield: 'duration',
								ymin: 0,
								ymax: null,
								pointradius: 1,
								//padding: 0
							}
					}};
					
					//---------------------------------
					// Table Renderer Settings
					let visibileFields = ['result','performance'];
					if (settings.showsparkline){ visibileFields.push('chart'); }
					visibileFields.push('schedulername');
					if (settings.showstatistics){ visibileFields.push('count', 'min', 'avg', 'max'); }
					
					
					var tableRendererSettings = {
						data: groupedJobStats,
						//bgstylefield: 'alertstyle',
						//textstylefield: 'textstyle',
						titlefields: ['schedulername'], 
						titleformat: '{0}', 
						visiblefields: visibileFields, 
						labels: EMP_STEP_LABELS,
						customizers: {
							min: function(record, value) { return CFW.format.millisToDuration(value);},
							avg: function(record, value) { return CFW.format.millisToDuration(value);},
							max: function(record, value) { return CFW.format.millisToDuration(value);},
							schedulername:  function(record, value) { 
								return '<span><a class="text-reset" target="_blank"  href="'+stepURL+'#/root/analytics?taskId='+record.schedulerid+'&refresh=1&relativeRange=86400000&tsParams=taskId,refresh,relativeRange&tenant='+encodeURIComponent(record.projectname)+'">'+value+'</a></span>'
							},
							
							result: function(record, value) {
								var clone = _.cloneDeep(statusbarRendererSettings);
								clone.data = value;
								clone.bgstylefield = 'statusstyle';
								var renderer = CFW.render.getRenderer('statusbar');
								return $('<div class="h-100" style="height: 10px; min-width: 5vw;  max-width: 10vw;">').append(renderer.render(clone));
							},
							performance: function(record, value) {
								var clone = _.cloneDeep(statusbarRendererSettings);
								clone.data = value;
								clone.bgstylefield = 'performancestyle';
								var renderer = CFW.render.getRenderer('statusbar');
								return $('<div class="h-100" style="height: 10px; min-width: 5vw;  max-width: 10vw;">').append(renderer.render(clone));
							},
							chart: function(record, value) {
								chartRendererSettings.data = record.result;
								var chartRenderer = CFW.render.getRenderer('chart');

								return $('<div class="w-100" style="height: 25px; min-width: 5vw; max-width: 10vw;">').append(chartRenderer.render(chartRendererSettings));
								//return chartRenderer.render(chartRendererSettings);
							},
							

						},
						rendererSettings:{
							table: {
								narrow: 	true,
								striped: 	true,
								hover: 		true,
								filterable: false
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