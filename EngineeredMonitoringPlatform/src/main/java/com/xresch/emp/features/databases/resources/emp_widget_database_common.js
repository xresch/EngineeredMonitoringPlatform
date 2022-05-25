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
				var excellentVal = settings.THRESHOLD_EXCELLENT;
				var goodVal = settings.THRESHOLD_GOOD;
				var warningVal = settings.THRESHOLD_WARNING;
				var emergencyVal = settings.THRESHOLD_EMERGENCY;
				var dangerVal = settings.THRESHOLD_DANGER;
				
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
					titlefields = keys[1];
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
						chart: {
							// The type of the chart: line|steppedline|area|steppedarea|bar|scatter (to be done radar|pie|doughnut|polarArea|bubble)
							charttype: settings.chart_type,
							// How should the input data be handled groupbytitle|arrays 
							datamode: 'groupbytitle',
							xfield: xColumn,
							yfield: yColumn,
							//the type of the x axis: linear|logarithmic|category|time
							xtype: settings.x_axis_type,
							//the type of the y axis: linear|logarithmic|category|time
							ytype: settings.y_axis_type,
							stacked: settings.stacked,
							showlegend: settings.show_legend,
							showaxes: settings.show_axes,
							ymin: settings.ymin,
							ymax: settings.ymax,
							pointradius: settings.pointradius,
							padding: 2
						}
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
	