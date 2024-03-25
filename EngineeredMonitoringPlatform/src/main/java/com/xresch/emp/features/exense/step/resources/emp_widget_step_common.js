/******************************************************************
 * 
 ******************************************************************/
CATEGORY_EXENSE = "Exense Step";
CFW.dashboard.registerCategory("fas fa-desktop", CATEGORY_EXENSE);

var EMP_STEP_VISIBILEFIELDS =
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
    
var EMP_STEP_LABELS  = {
		 projectname: "Project"
		,projectid: "Project ID"
		,planname: "Plan"
		,planID: "Plan ID"
		,schedulername: "Scheduler"
		,schedulerid: "Scheduler ID"
        ,starttime: "Start"
        ,endtime: "End"
	};
	
/******************************************************************
 * 
 ******************************************************************/
function emp_step_createDefaultCustomizers(stepURL, rendererName){
	var textClass = " ";
	if(rendererName == "table"){
		textClass = " text-reset ";
	}
	var customizers = {
								
		starttime: function(record, value) { return (value != null) ? CFW.format.epochToTimestamp(value) : '';},
		
		endtime: function(record, value) { return (value != null) ? CFW.format.epochToTimestamp(value) : '';},
		
		duration: function(record, value) { return (value != null) ? CFW.format.millisToDuration(value) : '';},
		
		planid:  function(record, value) { 
			if(CFW.utils.isNullOrEmpty(stepURL)) return value;
			var tenant = record['projectname'];
			//http://localhost:8080/#/root/plans/editor/62694473ee10d74e1b26d76e?tenant=AnotherTestProject
			return '<span>'+value+'&nbsp;(<a target="_blank" href="'+stepURL+'#/root/plans/editor/'+value+'?tenant='+tenant+'">Edit</a>)</span>';
		},
		
		projectname:  function(record, value) { 
			if(CFW.utils.isNullOrEmpty(stepURL)) return value;
			return '<span>'+value+'&nbsp;(<a target="_blank" href="'+stepURL+'#/root/plans/list?tenant='+value+'">Open</a>)</span>';
		},
		
		schedulername:  function(record, value) { 
			if(rendererName == "table"){
				return '<span><a class="'+textClass+'" target="_blank"  href="'+stepURL+'#/root/analytics?taskId='+record.schedulerid+'&refresh=1&relativeRange=86400000&tsParams=taskId,refresh,relativeRange&tenant='+encodeURIComponent(record.projectname)+'">'+value+'</a></span>'
			}else{
				return value;
			}
		},
		schedulerid:  function(record, value) { 
			//https://mystepinstance.io/#/root/analytics?taskId=65f41c46a68ef02f12ceeb63&refresh=1&relativeRange=86400000&tsParams=taskId,refresh,relativeRange&tenant=Test_Reto
			return '<span>'+value+'&nbsp;(<a target="_blank" href="'+stepURL+'#/root/analytics?taskId='+value+'&refresh=1&relativeRange=86400000&tsParams=taskId,refresh,relativeRange&tenant='+encodeURIComponent(record.projectname)+'">Stats</a>)</span>';
		},
		
	};
	;
	return customizers;
}
	
/******************************************************************
 * 
 * @param stepResult like PASSED, FAILED, TECHNICAL_ERROR etc...
 * @param duration in milliseconds
 * @param settings containing default threshold settings for performance rating
 * @param mode either "status", "duration" or "both" 
 ******************************************************************/
function emp_step_getStatusStyle(stepResult, duration, settings, mode){
	
	var style = "cfw-none";
		
	//---------------------------
	// Get Mode
	if(mode == null){
		mode = 'both';
	}
	mode = mode.trim().toLowerCase();

	//---------------------------
	// Gray if duration is null
	if(duration == null && current.result != "RUNNING"){ 
		style = "cfw-gray";
	}
	
	//---------------------------
	// Evaluate Duration
	if(mode == 'duration' 
	||(mode == 'both' && stepResult == "PASSED" ) 
	){	
		style =  CFW.colors.getThresholdStyle(duration
				,settings.THRESHOLD_GREEN
				,settings.THRESHOLD_LIMEGREEN
				,settings.THRESHOLD_YELLOW
				,settings.THRESHOLD_ORANGE
				,settings.THRESHOLD_RED
				,settings.THRESHOLD_DISABLED
			);
		
		// if threshold is undefined do green
		if(style == "cfw-none" || settings.THRESHOLD_DISABLED){
			style = "cfw-green"; 
		}
					
		return style;
	}
	
	//---------------------------
	// Evaluate Status
	// mode is either 'status' or'both'
	switch(stepResult){
		
		case "PASSED":				return "cfw-green"; 
		
		
		case "VETOED":	
		case "SKIPPED":		
		case "INTERRUPTED":			return "cfw-orange";
		
		
		case "FAILED":				return "cfw-red";	
		
		
		case "IMPORT_ERROR":		
		case "TECHNICAL_ERROR":		return "cfw-black";
		
		
		case "RUNNING":				return "cfw-blue";
		
		default:
			return "cfw-gray";
	}

}
	
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
				for(var key in data.payload){
					
					var current = data.payload[key];
					current.alertstyle = emp_step_getStatusStyle(
											  current.result
											, current.duration
											, settings
										);
					
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
					titlefields: ["schedulername"], 
					visiblefields: ["result"],
					titleformat: null, 
					labels: EMP_STEP_LABELS,
					customizers: emp_step_createDefaultCustomizers(stepURL, renderType),
					rendererSettings: CFW.dashboard.createStatusWidgetRendererSettings(settings) 
				};
				
				//--------------------------
				// Render Widget
				var renderer = CFW.render.getRenderer(renderType);
				
				if(renderType != "tiles"){
					dataToRender.visiblefields = EMP_STEP_VISIBILEFIELDS;
				}	
				
				if(renderType == "table"){
					delete dataToRender.customizers.projectname;
					var visis = dataToRender.visiblefields;
					dataToRender.visiblefields = visis.filter(item => item !== 'planname');
					
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
				
				//---------------------------------
				// URL
				var stepURL = data.url;
				if(!CFW.utils.isNullOrEmpty(stepURL) && !stepURL.endsWith("/")){
					stepURL += "/";
				}
								
				//---------------------------
				// Render Settings
				
				var chartsettings = Object.assign({}, settings.JSON_CHART_SETTINGS);
				chartsettings.xfield = timeField;
				chartsettings.yfield = valueField;
				chartsettings.padding = 2;
					
				var dataToRender = {
					data: data.payload,
					titlefields: arrayTitleFields, 
					titleformat: null, 
					labels: EMP_STEP_LABELS,
					customizers: emp_step_createDefaultCustomizers(stepURL, "chart"),
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


/*********************************************************************
 * 
*********************************************************************/
function emp_step_groupByScheduler(statusArray){
	
	var groupedResults = {};
	
	for(let index in statusArray){
		let currentStatus = statusArray[index];
		let groupID = currentStatus.schedulerid;
		let duration = currentStatus.duration;

		//--------------------------------
		// Initialize group if not exists
		if(groupedResults[groupID] == undefined){{
			groupedResults[groupID] = {
				 	  projectname: currentStatus.projectname
					, projectid: currentStatus.projectid
					, planname: currentStatus.planname
					, planID: currentStatus.planID
					, schedulername: currentStatus.schedulername
					, schedulerid: currentStatus.schedulerid
					, avg: 0
					, count: 0
					, sum: 0
					, min: 999999999
					, max: 0
					, chart: null // empty field to create column for sparkline
					, result: []
					, performance: []
				}
			}
		}
		
		//--------------------------------
		// Fill Data
		if(!isNaN(duration) && currentStatus.endtime != null){
			groupedResults[groupID].count += 1;
			groupedResults[groupID].sum += duration;
			groupedResults[groupID].min = (groupedResults[groupID].min <= duration) ? groupedResults[groupID].min : duration;
			groupedResults[groupID].max = (groupedResults[groupID].max >= duration) ? groupedResults[groupID].max : duration;
		}
		
		
		groupedResults[groupID].result.push(currentStatus);

		
	}
	
	//--------------------------------
	// create result array
	let finalArray = [];
	for(let key in groupedResults){
		let currentGroup = groupedResults[key];
		
		currentGroup.avg = currentGroup.sum / currentGroup.count;
		_.reverse(currentGroup.result);
		currentGroup.performance = currentGroup.result;
		finalArray.push(currentGroup);
		
		if(currentGroup.min == 999999999){
			currentGroup.min = null;
			currentGroup.avg = null;
			currentGroup.max = null;
			currentGroup.sum = null;
		}
	}
	
	
	return finalArray;
	
}
	
	