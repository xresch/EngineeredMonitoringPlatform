(function (){
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerCategory("fas fa-database", "Monitoring | Oracle");
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_oraclequerystatus",
		{
			category: "Monitoring | Oracle",
			menuicon: "fas fa-thermometer-half",
			menulabel: CFWL('emp_widget_oraclequerystatus', "Query Status"),
			description: CFWL('emp_widget_oraclequerystatus', "Executes an SQL Query and displays a threshhold based status."), 
			usetimeframe: true,
			createWidgetInstance: function (widgetObject, callback) {
					
				CFW.dashboard.fetchWidgetData(widgetObject, function(data){
					
					var settings = widgetObject.JSON_SETTINGS;
					var renderType = (settings.renderer == null) ? "tiles" : settings.renderer.toLowerCase() ;
					console.log(settings);
					
					//---------------------------------
					// Check for Data and Errors
					if(CFW.utils.isNullOrEmpty(data.payload) || typeof data.payload == 'string' || data.payload.length == 0){
						callback(widgetObject, '');
						return;
					}
					
					//---------------------------------
					// Use last Column if not specified
					var valueColumn = settings.valuecolumn;
					if(CFW.utils.isNullOrEmpty(valueColumn)){
						let keys = Object.keys(data.payload[0]);
						valueColumn = keys[keys.length-1];
					}
					
					//---------------------------------
					// Prepare TitleFields
					var labelColumns = settings.labelcolumns;
					var titlefields = null;
					if(!CFW.utils.isNullOrEmpty(labelColumns)){
						titlefields = labelColumns.trim().split(/ ?, ?/g);
					}
					
					//---------------------------------
					// Prepare visible fields
					var visiblefields = [];
					var detailColumns = settings.detailcolumns;
					
					if(!CFW.utils.isNullOrEmpty(detailColumns)){
						visiblefields = detailColumns.trim().split(/ ?, ?/g);
					}
					console.log("valueColumn:"+valueColumn);
					console.log("detailColumns:"+detailColumns);
					console.log("labelColumns:"+labelColumns);
					console.log("titlefields:"+titlefields);
					console.log("visiblefields:"+visiblefields);
					
					//---------------------------
					// Set Colors for Thresholds
					var excellentVal = settings.threshold_excellent;
					var goodVal = settings.threshold_good;
					var warningVal = settings.threshold_warning;
					var emergencyVal = settings.threshold_emergency;
					var dangerVal = settings.threshold_danger;
					
					for(var key in data.payload){
						var current = data.payload[key];
						
						current.alertstyle =  CFW.colors.getThresholdStyle(current[valueColumn]
								,excellentVal
								,goodVal
								,warningVal
								,emergencyVal
								,dangerVal
								,settings.disable);
						
						if(current.alertstyle != "cfw-none"){
							current.textstyle = "white"; 
						}
					}
					
					//---------------------------
					// Render Settings
					var dataToRender = {
						data: data.payload,
						bgstylefield: 'alertstyle',
						textstylefield: 'textstyle',
						titlefields: titlefields, 
						visiblefields: visiblefields,
						titleformat: null, 
						
						labels: {},
						customizers: {
							value: function(record, value) {
								if(value == null) return '';
								return (settings.suffix == null) ? value : value+" "+settings.suffix;
							},
							time: function(record, value) { return (value != null) ? new  moment(value).format("YYYY-MM-DD HH:mm") : '';},
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
					}};
					
					
					//--------------------------
					// Render Widget
					var renderer = CFW.render.getRenderer(renderType);
					callback(widgetObject, renderer.render(dataToRender));
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
	
})();;