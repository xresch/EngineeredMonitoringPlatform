(function (){
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerCategory("fas fa-desktop", "Monitoring | SPM");
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_spmprojectstatus",
		{
			category: "Monitoring | SPM",
			menuicon: "fas fa-cogs",
			menulabel: CFWL('emp_widget_spmprojectstatus', "SPM Project Status"),
			description: CFWL('emp_widget_spmprojectstatus_desc', "Fetches the status of one or multiple SPM Ponitors."),
			createWidgetInstance: function (widgetObject, params, callback) {
					
				CFW.dashboard.fetchWidgetData(widgetObject, params, function(data){
					var monitorStats = data.payload;
					var settings = widgetObject.JSON_SETTINGS;
					var renderer = settings.renderer.toLowerCase();
					
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
						titlefields: ['PROJECT_NAME'], 
						titleformat: '{0}', 
						visiblefields: ['PROJECT_ID', 'VALUE'], 
						labels: {
							PROJECT_NAME: 'Project',
							PROJECT_ID: 'ID',
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
								sizefactor: settings.sizefactor,
								showlabels: settings.showlabels,
								borderstyle: settings.borderstyle
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
					if(renderer == "table"
					|| renderer == "panels"
					|| renderer == "cards"
					|| renderer == "csv"){
						dataToRender.visiblefields = ['PROJECT_NAME', 'VALUE']; 
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

						if(renderer == null){ renderType = 'tiles'};
						
						var alertRenderer = CFW.render.getRenderer(renderer);
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