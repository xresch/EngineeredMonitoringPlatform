/******************************************************************
 * 
 ******************************************************************/
EXENSE_CATEGORY = "Exense Step";
	
CFW.dashboard.registerCategory("fas fa-database", EXENSE_CATEGORY);
	
	
/******************************************************************
 * 
 ******************************************************************/
function createStepStatusWidgetBase(widgetMenuLabel, widgetDescription){
	
	
	return {
		category: EXENSE_CATEGORY,
		menuicon: "fas fa-thermometer-half",
		menulabel: widgetMenuLabel,
		description: widgetDescription, 
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
				
				//---------------------------
				// Set Colors for Thresholds
				var excellentVal = settings.THRESHOLD_EXCELLENT;
				var goodVal = settings.THRESHOLD_GOOD;
				var warningVal = settings.THRESHOLD_WARNING;
				var emergencyVal = settings.THRESHOLD_EMERGENCY;
				var dangerVal = settings.THRESHOLD_DANGER;
				
				for(var key in data.payload){
					var current = data.payload[key];
					
					if(current.result == "PASSED"){
						current.alertstyle =  CFW.colors.getThresholdStyle(current["duration"]
								,excellentVal
								,goodVal
								,warningVal
								,emergencyVal
								,dangerVal
								,settings.THRESHOLD_DISABLED);
						
						
					}else if(current.result == "FAILED"){
						current.alertstyle = "cfw-danger";
					}else if(current.result == "TECHNICAL_ERROR"){
						current.alertstyle = "cfw-black";
					}else if(current.result == "RUNNING"){
						current.alertstyle = "cfw-blue";
					}
					
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
					titlefields: ["planname"], 
					visiblefields: ["projectname", "result"],
					titleformat: null, 
					
					labels: {},
					customizers: {
						value: function(record, value) {
							if(value == null) return '';
							return (settings.suffix == null) ? value : value+" "+settings.suffix;
						},
						starttime: function(record, value) { return (value != null) ? new  moment(value).format("YYYY-MM-DD HH:mm") : '';},
						endtime: function(record, value) { return (value != null) ? new  moment(value).format("YYYY-MM-DD HH:mm") : '';},
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
	