/******************************************************************
 * 
 ******************************************************************/
CFW.dashboard.registerCategory("fas fa-database", "Monitoring | Database");
	
/******************************************************************
 * 
 ******************************************************************/
function createDatabaseQueryStatusWidget(widgetMenuLabel){
	return {
		category: "Monitoring | Database",
		menuicon: "fas fa-thermometer-half",
		menulabel: widgetMenuLabel,
		description: CFWL('emp_widget_database_desc', "Executes a database query and displays the data. Records can be colored by applying a threshhold against the value of a column. Can have tasks that alerts when specific threasholds are reached."), 
		usetimeframe: true,
		createWidgetInstance: function (widgetObject, params, callback) {
				
			CFW.dashboard.fetchWidgetData(widgetObject, params, function(data){
				
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
				var valueColumn = settings.valuecolumn.trim();
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
	