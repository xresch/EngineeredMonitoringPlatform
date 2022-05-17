/******************************************************************
 * 
 ******************************************************************/
CATEGORY_EXENSE = "Exense Step";
	
CFW.dashboard.registerCategory("fas fa-desktop", CATEGORY_EXENSE);
	
	
/******************************************************************
 * 
 ******************************************************************/
function createStepStatusWidgetBase(widgetMenuLabel, widgetDescription){
	
	return {
		category: CATEGORY_EXENSE,
		menuicon: "fas fa-thermometer-half",
		menulabel: widgetMenuLabel,
		description: widgetDescription, 
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
				
				var stepURL = data.url;
				if(!CFW.utils.isNullOrEmpty(stepURL) && !stepURL.endsWith("/")){
					stepURL += "/";
				}
				
				//---------------------------
				// Set Colors for Thresholds
				var excellentVal = settings.THRESHOLD_EXCELLENT;
				var goodVal = settings.THRESHOLD_GOOD;
				var warningVal = settings.THRESHOLD_WARNING;
				var emergencyVal = settings.THRESHOLD_EMERGENCY;
				var dangerVal = settings.THRESHOLD_DANGER;
				var isDisabled = settings.THRESHOLD_DISABLED;
				
				for(var key in data.payload){
					var current = data.payload[key];
					var duration = current["duration"];
					if(current["duration"] == null && current.result != "RUNNING"){ 
						current.alertstyle = "cfw-gray";
					}else if(current.result == "PASSED"){
						current.alertstyle =  CFW.colors.getThresholdStyle(duration
								,excellentVal
								,goodVal
								,warningVal
								,emergencyVal
								,dangerVal
								,isDisabled);
						
						// if threshold is undefined do green
						if(current.alertstyle == "cfw-none" || isDisabled){
							current.alertstyle = "cfw-excellent"; 
						}
						
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
				// Visible Fields
				var visiblefields =
			        [
				         'projectname'
						,'planname'
						,'schedulername'
						,'status'
						,'result'
				        ,'duration'
				        ,'starttime'
				        ,'endtime'
			        ];
				
				//---------------------------
				// Render Settings
				var dataToRender = {
					data: data.payload,
					bgstylefield: 'alertstyle',
					textstylefield: 'textstyle',
					titlefields: ["planname"], 
					visiblefields: ["result"],
					titleformat: null, 
					
					labels: {},
					customizers: {
						
						value: function(record, value) {
							if(value == null) return '';
							return (settings.suffix == null) ? value : value+" "+settings.suffix;
						},
						
						starttime: function(record, value) { return (value != null) ? new  moment(value).format("YYYY-MM-DD HH:mm") : '';},
						
						endtime: function(record, value) { return (value != null) ? new  moment(value).format("YYYY-MM-DD HH:mm") : '';},
						
						duration: function(record, value) { return (value != null) ? CFW.format.millisToDuration(value) : '';},
						
						planid:  function(record, value) { 
							if(CFW.utils.isNullOrEmpty(stepURL)) return value;
							var tenant = record['projectname'];
							http://localhost:8080/#/root/plans/editor/62694473ee10d74e1b26d76e?tenant=AnotherTestProject
							return '<span>'+value+'&nbsp;(<a target="_blank" href="'+stepURL+'#/root/plans/editor/'+value+'?tenant='+tenant+'">Edit Plan</a>)</span>';
						},
						
						projectname:  function(record, value) { 
							if(CFW.utils.isNullOrEmpty(stepURL)) return value;
							return '<span>'+value+'&nbsp;(<a target="_blank" href="'+stepURL+'#/root/plans/list?tenant='+value+'">Show Project</a>)</span>';
						},
						
						schedulername:  function(record, value) { 
							if(CFW.utils.isNullOrEmpty(stepURL)) return value;
							var schedulerid = record['schedulerid'];
							return '<span>'+value+'&nbsp;(<a target="_blank" href="'+stepURL+'#/root/dashboards/__pp__RTMDashboard?__filter1__=text,taskId,'+schedulerid+'">Scheduler Statistics</a>)</span>';
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
							
				//--------------------------
				// Render Widget
				var renderer = CFW.render.getRenderer(renderType);
				
				if(renderType != "tiles"){
					dataToRender.visiblefields = visiblefields;
				}	
				
				if(renderType == "table"){
					delete dataToRender.customizers.projectname;
					delete dataToRender.customizers.schedulername;
				}
				callback(widgetObject, renderer.render(dataToRender));
			});
		},
		
	}
}	


/******************************************************************
 * 
 ******************************************************************/
function createStepChartWidgetBase(
		  widgetMenuLabel
		, widgetDescription
		, arrayTitleFields
		, timeField
		, valueField
	){
	return {
		category: CATEGORY_EXENSE,
		menuicon: "fas fa-chart-bar",
		menulabel: widgetMenuLabel,
		description: widgetDescription, 
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
								
				//---------------------------
				// Render Settings
				var dataToRender = {
					data: data.payload,
					//bgstylefield: 'alertstyle',
					//textstylefield: 'textstyle',
					titlefields: arrayTitleFields, 
					//visiblefields: visiblefields,
					titleformat: null, 
					labels: {
						"projectname": "Project"
						, "planname": "Plan"
					},
					customizers: {},
					rendererSettings:{
						chart: {
							// The type of the chart: line|steppedline|area|steppedarea|bar|scatter (to be done radar|pie|doughnut|polarArea|bubble)
							charttype: settings.chart_type,
							// How should the input data be handled groupbytitle|arrays 
							datamode: 'groupbytitle',
							xfield: timeField,
							yfield: valueField,
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
	
	