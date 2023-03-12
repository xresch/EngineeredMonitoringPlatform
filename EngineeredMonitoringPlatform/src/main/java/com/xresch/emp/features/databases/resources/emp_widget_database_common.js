/******************************************************************
 * 
 ******************************************************************/
CFW.dashboard.registerCategory("fas fa-database", "Database");
	
/******************************************************************
 * 
 ******************************************************************/
function createDatabaseQueryStatusWidget(widgetMenuLabel){
	return {
		category: "Database",
		menuicon: "fas fa-thermometer-half",
		menulabel: widgetMenuLabel,
		description: CFWL('emp_widget_database_status_desc', "Executes a database query and displays the data. Records can be colored by applying a threshhold against the value of a column. Can have tasks that alerts when specific threasholds are reached."), 
		usetimeframe: true,
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
				
				//---------------------------------
				// Use last Column if not specified
				
				var valueColumn;
				if(CFW.utils.isNullOrEmpty(settings.valuecolumn)){
					let keys = Object.keys(data.payload[0]);
					valueColumn = keys[keys.length-1];
				}else{
					var valueColumn = settings.valuecolumn.trim();
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
				var visiblefields = Object.keys(data.payload[0]);
				var detailColumns = settings.detailcolumns;
				
				if(!CFW.utils.isNullOrEmpty(detailColumns)){
					visiblefields = detailColumns.trim().split(/ ?, ?/g);
				}
				
				//---------------------------
				// Set Colors for Thresholds
				var excellentVal = settings.THRESHOLD_GREEN;
				var goodVal = settings.THRESHOLD_LIMEGREEN;
				var warningVal = settings.THRESHOLD_YELLOW;
				var emergencyVal = settings.THRESHOLD_ORANGE;
				var dangerVal = settings.THRESHOLD_RED;
				
				for(var key in data.payload){
					var current = data.payload[key];
					
					current.alertstyle =  CFW.colors.getThresholdStyle(current[valueColumn]
							,excellentVal
							,goodVal
							,warningVal
							,emergencyVal
							,dangerVal
							,settings.THRESHOLD_DISABLED);
					
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
					rendererSettings: CFW.dashboard.createStatusWidgetRendererSettings(settings)
				};
				
				//--------------------------
				// Add URL Customizer
				if(!CFW.utils.isNullOrEmpty(settings.urlcolumn)){
					dataToRender.customizers[settings.urlcolumn] = 
						function(record, value) { 
							if(value != null && value != ""){
				 				return  '<a style="color: inherit;" role="button" target="_blank" href="'+value+'" ><i class="fas fa-external-link-square-alt"></i> Link</a>'; 
				 			}else {
				 				return "&nbsp;";
				 			}
						};
				}
									
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
}	

/******************************************************************
 * 
 ******************************************************************/
function createDatabaseQueryChartWidget(widgetMenuLabel){
	return {
		category: "Database",
		menuicon: "fas fa-chart-bar",
		menulabel: widgetMenuLabel,
		description: CFWL('emp_widget_database_chart_desc', "Executes a database query and displays the data as a chart."), 
		usetimeframe: true,
		createWidgetInstance: function (widgetObject, params, callback) {
				
			CFW.dashboard.fetchWidgetData(widgetObject, params, function(data){
				
				var settings = widgetObject.JSON_SETTINGS;

				//---------------------------------
				// Check for Data and Errors
				if(CFW.utils.isNullOrEmpty(data.payload) || typeof data.payload == 'string' || data.payload.length == 0){
					callback(widgetObject, '');
					return;
				}
				
				//---------------------------------
				// Prepare TitleFields
				var seriesColumns = settings.seriescolumns;
				var titlefields = null;
				if(!CFW.utils.isNullOrEmpty(seriesColumns)){
					titlefields = seriesColumns.trim().split(/ ?, ?/g);
				}else{
					// use second as default
					let keys = Object.keys(data.payload[0]);
					titlefields = [keys[1]];
				}
				
				//---------------------------------
				// Use first Column if not specified
				var xColumn;
				if(CFW.utils.isNullOrEmpty(settings.xcolumn)){
					let keys = Object.keys(data.payload[0]);
					xColumn = keys[0];
				}else{
					var xColumn = settings.xcolumn.trim();
				}
				
				
				//---------------------------------
				// Use last Column if not specified
				var yColumn;
				if(CFW.utils.isNullOrEmpty(settings.ycolumn)){
					let keys = Object.keys(data.payload[0]);
					yColumn = keys[keys.length-1];
				}else{
					var yColumn = settings.ycolumn.trim();
				}
								
				//---------------------------
				// Render Settings
				
				var chartsettings = Object.assign({}, settings.JSON_CHART_SETTINGS);
				chartsettings.padding = 2;
				chartsettings.xfield = xColumn;
				chartsettings.yfield =yColumn;
						
				var dataToRender = {
					data: data.payload,
					//bgstylefield: 'alertstyle',
					//textstylefield: 'textstyle',
					titlefields: titlefields, 
					//visiblefields: visiblefields,
					titleformat: null, 
					labels: {},
					customizers: {},
					rendererSettings:{
						chart: chartsettings
					}
				};
												
				//--------------------------
				// Render Widget
				var renderer = CFW.render.getRenderer('chart');
				callback(widgetObject, renderer.render(dataToRender));
			});
		},
	}
}	
	