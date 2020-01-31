/*************************************************************************
 * 
 * @author Reto Scheiwiller, 2018
 * 
 * Distributed under the MIT license
 *************************************************************************/

/**************************************************************************************
 * GLOBAL VARIABLES
 *************************************************************************************/
var GLOBAL_COUNTER=0;
var SUMMARY;
var RULES;
var STATS_BY_TYPE;
var STATS_PRIMED_CACHE;

//object containing the url parameters as name/value pairs {"name": "value", "name2": "value2" ...}
var URL_PARAMETERS;

//used to store the current entry(of the HAR file) used for showing the details modal for the gantt chart
var CURRENT_DETAILS_ENTRY;

//-----------------------------------------
// Data Objects
var YSLOW_RESULT = null;
var RESULT_LIST = null;
var HAR_DATA = null;
var COMPARE_YSLOW = null;

var GRADE_CLASS = {
	A: "success",
	B: "success",
	C: "warning",
	D: "warning",
	E: "danger",
	F: "danger",
	None: "info"
};

/******************************************************************
 * Initialization function executed once when starting the page.
 * 
 * @param 
 * @return 
 ******************************************************************/
function initialize(){
	
	URL_PARAMETERS = CFW.http.getURLParams();
	
}


/******************************************************************
 * Method to fetch data from the server. The result is stored in
 * global variables.
 * If the data was already loaded, no additional request is made.
 * After the data is returned, the draw method is executed with 
 * the args-object passed to this function.
 * 
 * @param args the object containing the arguments
 * @return 
 *
 ******************************************************************/
function fetchData(args){
	
	//---------------------------------------
	// Check loading status and create URL
	//---------------------------------------
	var url = "./data";
	var params = {type: args.data};
	switch (args.data){
		case "yslowresult": 	if(YSLOW_RESULT != null) return;
								params.resultid = URL_PARAMETERS.resultid;
								break;
								
		case "resultlist":		if(RESULT_LIST != null) return;
								break;
		
		case "allresults":		if(RESULT_LIST != null) return;
								break;
								
		case "har":				if(HAR_DATA != null) return;
								params.resultid = URL_PARAMETERS.resultid;
								break;
								
		case "compareyslow":	if(COMPARE_YSLOW != null) return;
								params.resultids = URL_PARAMETERS.resultids;
								break;						
								
	}
	
	//---------------------------------------
	// Fetch and Return Data
	//---------------------------------------
	
	CFW.http.getJSON(url, params, 
			function(data) {
				    
			switch (args.data){
				case "yslowresult": 	YSLOW_RESULT = data.payload;
										prepareYSlowResults(YSLOW_RESULT);
										RULES = CFW.array.sortArrayByValueOfObject(RULES, "score");
										$(".result-view-tabs").css("display", "block");
										draw(args);
										break;
										
				case "resultlist":		RESULT_LIST = data.payload;
										draw(args);
										break;
										
				case "allresults":		RESULT_LIST = data.payload;
										draw(args);
										break;
										
				case "har":				HAR_DATA = data.payload;
										prepareGanttData(HAR_DATA);
										draw(args);
										break;
										
				case "compareyslow":	COMPARE_YSLOW = data.payload;
										draw(args);
										break;						
										
			}
		});
}

/**************************************************************************************
 * Get the Grade as A/B/C/D/E/F/None depending on the given YSlow score.
 * 
 * @param score the yslow score as number
 * @return the grade as A/B/C/D/E/F/None
 *************************************************************************************/
function getGrade(score){
	
	if		(score >= 90){return "A" }
	else if (score >= 80){return "B" }
	else if (score >= 70){return "C" }
	else if (score >= 60){return "D" }
	else if (score >= 50){return "E" }
	else if (score >= 0){return "F" }
	else {return "None" }
}


/******************************************************************
 * Prepare the fetched yslow results so they can be easily displayed.
 * This method doesn't return a value, everything is stored in 
 * global variables.
 * 
 * @param data the object containing the YSlow results. 
 * @return nothing
 ******************************************************************/
function prepareYSlowResults(data){
	
//	"w": "size",
//	"o": "overall score",
//	"u": "url",
//	"r": "total number of requests",
//	"s": "space id of the page",
//	"i": "id of the ruleset used",
//	"lt": "page load time",
//	"w_c": "page weight with primed cache",
//	"r_c": "number of requests with primed cache",
	
	//===================================================
	// Load Summary Values
	//===================================================
	SUMMARY = {};

	SUMMARY.url				= decodeURIComponent(data.u);
	SUMMARY.size			= data.w;
	SUMMARY.sizeCached		= data.w_c;
	SUMMARY.totalScore		= data.o;
	SUMMARY.grade			= getGrade(SUMMARY.totalScore);
	SUMMARY.requests		= data.r;
	SUMMARY.requestsCached	= data.r_c;
	SUMMARY.ruleset			= data.i;
	SUMMARY.loadtime		= data.resp;

	
	//===================================================
	// Load Rules
	//===================================================
	RULES = [];
	for(key in data.g){

		var rule = {};
		rule.name 			= key;
		rule.score 			= data.g[key].score;
		rule.grade 			= getGrade(rule.score);
		rule.title 			= data.dictionary.rules[key].name;
		rule.description 	= data.dictionary.rules[key].info;
		rule.message 		= data.g[key].message;
		rule.components 	= data.g[key].components;
		rule.url		 	= data.g[key].url;
		rule.weight 		= data.dictionary.rules[key].weight;
			
		if(rule.score == undefined || rule.score == null) rule.score = "-";
		
		RULES.push(rule);
	}
	
	//===================================================
	// LoadStats
	//===================================================
	STATS_BY_TYPE = [];
	for(key in data.stats){

		var stats = {};
		stats.type 		= key;
		stats.requests 	= data.stats[key].r;
		stats.size 		= data.stats[key].w;
		
		STATS_BY_TYPE.push(stats);
	}
	
	//===================================================
	// Load Stats with cache
	//===================================================
	STATS_PRIMED_CACHE = [];
	for(key in data.stats_c){
		
		var stats = {};
		stats.type 		= key;
		stats.requests 	= data.stats_c[key].r;
		stats.size 		= data.stats_c[key].w;
		
		STATS_PRIMED_CACHE.push(stats);
	}
		
	//===================================================
	// Load Stats with cache
	//===================================================
	COMPONENTS = [];
	for(key in data.comps){
		
		var comp = {};
		
		comp.type 			= data.comps[key].type;		
		comp.size			= data.comps[key].size;
		//comp.gzipsize		= data.comps[key].gzip;
		comp.responsetime	= Math.round(data.comps[key].resp);
		comp.type 			= data.comps[key].type;
		comp.expires		= data.comps[key].expires;
		comp.url 			= decodeURIComponent(data.comps[key].url);
		
		//what's that?
		//comp.cr 			= data.comps[key].cr;
		
		COMPONENTS.push(comp);
	}
}

/******************************************************************
 * Adds additional information to the entries needed to build the 
 * gantt chart.
 * 
 * @param data the object in HAR format
 * @return nothing
 ******************************************************************/
function prepareGanttData(data){
	

	//----------------------------------
	// Variables
	var entries = data.log.entries; 
	var entriesCount = entries.length;
	
	var dateStartTime;
	var dateEndTime;
	var totalTimeMillis;

	if(entriesCount > 0){
		dateStartTime = new Date(entries[0].startedDateTime);
		//console.log("FirstDate:"+dateStartTime.toString());
	}
	//----------------------------------
	// Find lastMillis
	var lastMillis = 0;
	for(var i = 0; i < entriesCount; i++ ){
		var entry = entries[i];
		
		dateEndTime = new Date(entry.startedDateTime);
		dateEndTime = new Date(dateEndTime.valueOf() + Math.ceil(entry.time));
		entry.endDateTime = dateEndTime.valueOf();

		if(lastMillis < entry.endDateTime){
			lastMillis = entry.endDateTime;
		}
		
	}
	
	totalTimeMillis = lastMillis - dateStartTime.valueOf();
	
	//----------------------------------
	// Loop Data

//   "timings": {
//       "blocked": 0,
//        "dns": -1,
//        "connect": -1,
//        "send": 0,
//        "wait": 265,
//        "receive": 5,
//        "ssl": -1
//    },

	for(var i = 0; i < entriesCount; i++ ){
		var entry = entries[i];
		var startDate = new Date(entry.startedDateTime);
		var deltaMillis = startDate.valueOf() - dateStartTime.valueOf();
		var duration = entry.time;
		var timings = entry.timings;
		
		entry.ganttdata = {
			"time": duration,	
			"delta": deltaMillis,
			"percentdelta": deltaMillis / totalTimeMillis * 100,
			"percentblocked": (entry.timings.blocked > 0) 	? entry.timings.blocked / duration * 100 : 0,
			"percentdns": (entry.timings.dns > 0) 			? entry.timings.dns / duration * 100 : 0,
			"percentconnect": (entry.timings.connect > 0) 	? entry.timings.connect / duration * 100 : 0,
			"percentsend": (entry.timings.send > 0) 		? entry.timings.send / duration * 100 : 0,
			"percentwait": (entry.timings.wait > 0) 		? entry.timings.wait / duration * 100 : 0,
			"percentreceive": (entry.timings.receive > 0) 	? entry.timings.receive / duration * 100 : 0,
			"percentssl": (entry.timings.ssl > 0) 			? entry.timings.ssl / duration * 100 : 0,
			"percenttime": duration / totalTimeMillis * 100
		}
		
		// Workaround: Delta + duration could not be greater than totalTime.
		// If it is, do this to avoid display issues.
		if(totalTimeMillis < (deltaMillis + duration)){
			duration = totalTimeMillis - deltaMillis;
			entry.ganttdata.percentTime = duration / totalTimeMillis * 100;
		}
			
		//console.log(entry.ganttdata);
		
	}

}

/**************************************************************************************
 * Filter the rows of a table by the value of the search field.
 * This method is best used by triggering it on the onchange-event on the search field
 * itself.
 * The search field has to have an attached JQuery data object($().data(name, value)), ¨
 * pointing to the table that should be filtered.
 * 
 * @param searchField 
 * @return nothing
 *************************************************************************************/
function filterTable(searchField){
	
	var table = $(searchField).data("table");
	var input = searchField;
	
	filter = input.value.toUpperCase();

	table.find("tbody tr, >tr").each(function( index ) {
		  //console.log( index + ": " + $(this).text() );
		  
		  if ($(this).html().toUpperCase().indexOf(filter) > -1) {
			  $(this).css("display", "");
		  } else {
			  $(this).css("display", "none");
			}
	});

}

/******************************************************************
 * Print a comparison table containing all the yslow results in
 * the given data.
 * 
 * @param parent JQuery object to append the comparison. 
 * @param data array containing multiple yslow results.
 *
 ******************************************************************/
function printComparison(parent, data){
	
	compareTableData = [];
	//-----------------------------------------
	// Get distinct List of Rules
	//-----------------------------------------
	uniqueRuleList = {};
	for(key in data){
		
		for(ruleName in data[key].JSON_RESULT.g){
			//console.log("RuleName"+ruleName);
			uniqueRuleList[ruleName] = {"Metric": ruleName};
		}
	}
	
	//-----------------------------------------
	// Create Rows
	//-----------------------------------------
	
	var resultNameRow = {"Metric": "Result Name"}; compareTableData.push(resultNameRow);
	var urlRow = {"Metric": "URL"}; compareTableData.push(urlRow);
	var scoreRow = {"Metric": "Score"}; compareTableData.push(scoreRow);
	var gradeRow = {"Metric": "Grade"}; compareTableData.push(gradeRow);
	var loadtimeRow = {"Metric": "Load Time"}; compareTableData.push(loadtimeRow);
	var sizeRow = {"Metric": "Page Size"}; compareTableData.push(sizeRow);
	var sizeCachedRow = {"Metric": "Page Size Cached"}; compareTableData.push(sizeCachedRow);
	var requestCountRow = {"Metric": "Total Requests"}; compareTableData.push(requestCountRow);
	var requestsCachedRow = {"Metric": "Cached Requests"}; compareTableData.push(requestsCachedRow);
	
	//-------------------------------
	// Push rules to table
	for(ruleName in uniqueRuleList){
		compareTableData.push(uniqueRuleList[ruleName]);
	}

	for(key in data){

		var time = CFW.format.epochToTimestamp(data[key].TIME_CREATED);
		var result = data[key].JSON_RESULT;
		
		//----------------------------
		// Name Row
		resultNameRow[time]	= '<p>'+data[key].NAME+'</p>';
		
		//----------------------------
		// URL Row
		url = CFW.http.secureDecodeURI(result.u);
		urlRow[time]	= '<a target="_blank" href="'+url+'">'+url+'</a>';
		
		//----------------------------
		// Score Row
		var score = result.o; 
		scoreRow[time]	 = score + "%";
		
		//----------------------------
		// Grade Row
		var grade = getGrade(score);
		gradeRow[time]	 = '<span class="badge btn-'+GRADE_CLASS[grade]+'">'+grade+'</span>';
		
		//----------------------------
		// Other Rows
		sizeRow[time]	 			= result.w + " Bytes";
		sizeCachedRow[time]	 		= result.w_c + " Bytes";
		loadtimeRow[time]	 		= result.resp + "ms";
		requestCountRow[time]	 	= result.r;
		requestsCachedRow[time]	 	= result.r_c;
		
		//----------------------------
		// Rule Rows
		for(ruleName in uniqueRuleList){
				if(typeof result.g !== 'undefined' && typeof result.g[ruleName] !== 'undefined'){
				var ruleScore = result.g[ruleName].score;
				var ruleGrade = getGrade(ruleScore);
				uniqueRuleList[ruleName][time] = '<span class="badge btn-'+GRADE_CLASS[ruleGrade]+'">'+ruleGrade+'&nbsp;&sol;&nbsp;' + ruleScore + "%</span>";
			}else{
				uniqueRuleList[ruleName][time] = "N/A";
			}
		}
	}
	
	printTable(parent,compareTableData, "Comparison");
	
}

/******************************************************************
 * Create the dropdown for analyzing cookies or headers.
 * 
 * @param parent jQuery object to append the created dropdown
 * @param data HAR file data
 * @param type either "Cookies" or "Headers"
 ******************************************************************/
function createAnalyzeDropdown(parent, data, type){
		
	//---------------------------------------------
	// Loop entries and get distinct cookie names
	var entries = data.log.entries; 
	var entriesCount = entries.length;
	
	var distinctNames = {};
	for(var i = 0; i < entriesCount; i++ ){
		var currentEntry = entries[i];
		
		//------------------------------------
		// Loop request Cookies
		var requestArray = currentEntry.request[type.toLowerCase()];		
		if(requestArray != null && requestArray.length > 0){
			for(j = 0; j < requestArray.length; j++){
				var name = requestArray[j].name;
				distinctNames[name] = "";
			}
		}
		
		//------------------------------------
		// Loop Response Cookies
		var responseArray = currentEntry.response[type.toLowerCase()];		
		if(responseArray != null && responseArray.length > 0){
			for(j = 0; j < responseArray.length; j++){
				var name = responseArray[j].name;
				distinctNames[name] = "";
			}
		}
		
	}	
	
	//------------------------------------
	// Create Dropdown
	dropdownID = type+"Dropdown";
	var dropdownHTML = '<div class="dropdown" style="display: inline;" >' +
		'<button class="btn btn-primary dropdown-toggle" style="margin: 5px;" type="button" id="'+dropdownID+'" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">' +
	    'Analyze '+type+' <span class="caret"></span>' +
		'</button>' +
		'<ul class="dropdown-menu" aria-labelledby="'+dropdownID+'">';
			
	if(distinctNames != null){
		nameArray = Object.keys(distinctNames);
		//console.log(nameArray);
		for(i = 0; i < nameArray.length; i++){
			dropdownHTML += '<li class="dropdown-item" onclick="analyzeCookiesOrHeaders(\''+nameArray[i]+'\', \''+type+'\')"><a >'+nameArray[i]+'</a></li>';
		}
	}

	dropdownHTML += '</ul></div>';
	
	parent.append(dropdownHTML);
	$('#'+dropdownID).dropdown();    

}

/******************************************************************
 * Open a modal with details for one item in the gantt chart list.
 * 
 * @param key the key of the cookie or header to analies
 * @param "Cookies" or "Headers"
 ******************************************************************/
function analyzeCookiesOrHeaders(key, type){
	
	//-----------------------------------------
	// Initialize
	//-----------------------------------------

	resultHTML = '<table class="table table-striped">'
		+ '<thead><tr><td>Request Cookie Value</td><td>Response Cookie Value</td><td>URL</td></tr></thead>'
	
	//---------------------------------------------
	// Loop Entries
	data = HAR_DATA;
	var entries = data.log.entries; 
	var entriesCount = entries.length;
	
	for(var i = 0; i < entriesCount; i++ ){
		var currentEntry = entries[i];
		
		//------------------------------------
		// Loop Request Cookies and find Value
		var requestArray = currentEntry.request[type.toLowerCase()];	
		var requestValue = "";
		if(requestArray != null && requestArray.length > 0){
			for(j = 0; j < requestArray.length; j++){
				if(requestArray[j].name == key){
					requestValue = requestArray[j].value;
					break;
				}
			}
		}
		
		//--------------------------------------
		// Loop Response Cookies  and find Value
		var responseArray = currentEntry.response[type.toLowerCase()];	
		var responseValue = "";
		if(responseArray != null && responseArray.length > 0){
			for(j = 0; j < responseArray.length; j++){
				if(responseArray[j].name == key){
					responseValue = responseArray[j].value;
					break;
				}
			}
		}
		
		//--------------------------------------
		// Create Table Row
		if(requestValue != "" || responseValue != ""){
			resultHTML += '	<tr><td>'+requestValue+'</td><td>'+responseValue+'</td><td>'+CFW.http.secureDecodeURI(currentEntry.request.url)+'</td></tr>';
		}
	}	
	
	resultHTML += "</table>";
	
	CFW.ui.showModal('Values for '+type+' "'+key+'"',
			resultHTML);
	
	
}
/******************************************************************
 * Print the gantt chart for the entries.
 * 
 * @param parent JQuery object 
 * @param data HAR file data
 * 
 ******************************************************************/
function printGanttChart(parent, data){
	
	//----------------------------------
	// Add title and description.
	parent.append("<h2>Gantt Chart</h2>");

	createAnalyzeDropdown(parent, data, "Cookies");
	createAnalyzeDropdown(parent, data, "Headers");

	
	//----------------------------------
	// Create Table Header
	var cfwTable = CFW.ui.createTable();
	cfwTable.isNarrow = true;
	cfwTable.isHover = true;
	cfwTable.isResponsive = true;
	cfwTable.isStriped = true;
	cfwTable.tableFilter = true;
	cfwTable.addHeaders(['&nbsp;','Timings','Status','Duration','URL']);

	//----------------------------------
	// Create Rows
	var entries = data.log.entries; 
	var entriesCount = entries.length;
	for(var i = 0; i < entriesCount; i++ ){
		var currentEntry = entries[i];
		
		var row = $('<tr>');
		
		//--------------------------
		// Details Link
		var detailsLinkTD = $('<td>');
		var detailsLink = $('<a alt="Show Details" onclick="showGanttDetails(this)"><i class="fa fa-search"></i></a>');
		detailsLink.data("entry", currentEntry);
		detailsLinkTD.append(detailsLink);
		
		
		row.append(detailsLinkTD);
		
		//--------------------------
		// Gantt Chart Column
		var  rowString = '';
		var gd = currentEntry.ganttdata;
		//workaround for wrong timing deviations
		var shortPercentDelta = (gd.percentdelta >= 1) ? Math.floor(gd.percentdelta-1): gd.percentdelta;
		var shortPercentTime = (gd.percenttime >= 2) ? Math.floor(gd.percenttime-1): gd.percenttime;
		
		rowString += '<td> <div class="ganttWrapper" style="width: 500px;">';
			rowString += '<div class="ganttBlock percentdelta" style="width: '+shortPercentDelta+'%">&nbsp;</div>';
			rowString += '<div class="ganttBlock ganttTimings" style="width: '+shortPercentTime+'%">';
				rowString += createGanttBar(currentEntry, "blocked");
				rowString += createGanttBar(currentEntry, "dns");
				rowString += createGanttBar(currentEntry, "connect");
				//rowString += createGanttBar(currentEntry, "ssl");
				rowString += createGanttBar(currentEntry, "send");
				rowString += createGanttBar(currentEntry, "wait");
				rowString += createGanttBar(currentEntry, "receive");
			rowString += '</div>';
		rowString += 	'</div></td>';
		
		// --------------------------
		// Other Columns
		rowString += '<td>'+createHTTPStatusBadge(currentEntry.response.status)+'</td>';
		rowString += '<td>'+Math.round(currentEntry.time)+' ms</td>';
		rowString += '<td>'+CFW.http.secureDecodeURI(currentEntry.request.url)+'</td>';
		
		row.append(rowString);
		
		
		cfwTable.addRow(row);
	}
	
	var legendHTML = createGanttChartLegend();
	parent.append(legendHTML);
	cfwTable.appendTo(parent);
	parent.append(legendHTML);
	
}

/******************************************************************
 * Create the gantt chart part for the given metric.
 * 
 * @param entry the HAR entry
 * @param metric 
 * @return the HTML for the bar in the gantt chart
 ******************************************************************/
function createGanttBar(entry, metric){
	
	var percentString = "percent"+metric;
	
	// Workaround: Reduce size by 5% with "/100*95" to minimize display issues
	if(entry.ganttdata[percentString] > 0){ 
		return '<div class="ganttBlock '+percentString+'" alt="test" style="width: '+Math.floor(entry.ganttdata[percentString]/100*95)+'%">&nbsp;</div>'
	}else{
		return "";
	}
}

/******************************************************************
 * Open a modal with details for one item in the gantt chart list.
 * 
 * @param element the DOM element which was the source of the onclick
 * event. Has an attached har-entry with JQuery $().data()
 * 
 ******************************************************************/
function showGanttDetails(element){
	
	//-----------------------------------------
	// Initialize
	//-----------------------------------------
	var entry = $(element).data('entry');

	//console.log(entry);
	CURRENT_DETAILS_ENTRY = entry;
	
	//-----------------------------------------
	// Print Tabs
	//-----------------------------------------
	htmlString  = '<ul id="tabs" class="nav nav-tabs">';
	htmlString += '    <li class="nav-item"><a class="nav-link" href="#" onclick="updateGanttDetails(\'request\')">Request</a></li>';
	htmlString += '    <li class="nav-item"><a class="nav-link" href="#" onclick="updateGanttDetails(\'response\')">Response</a></li>';
	htmlString += '    <li class="nav-item"><a class="nav-link" href="#" onclick="updateGanttDetails(\'cookies\')">Cookies</a></li>';
	htmlString += '    <li class="nav-item"><a class="nav-link" href="#" onclick="updateGanttDetails(\'headers\')">Headers</a></li>';
	htmlString += '    <li class="nav-item"><a class="nav-link" href="#" onclick="updateGanttDetails(\'timings\')">Timings</a></li>';
	htmlString += '</ul>';
	
	htmlString += '<div id="ganttDetails"></div>';
	
	//-----------------------------------------
	// Show Modal and Update
	//-----------------------------------------
	CFW.ui.showModal('Details', htmlString);
			
	updateGanttDetails('request');

	
}

/******************************************************************
 * Update the gantt details modal when clicking on a tab.
 * 
 * @param tab specify what tab should be printed.
 * 
 ******************************************************************/
function updateGanttDetails(tab){
	
	var target = $('#ganttDetails');
	target.html('');
	
	entry = CURRENT_DETAILS_ENTRY;
	
	var details = '';
	if(tab === 'request'){
		var request = entry.request;
		details += '<table class="table table-striped">';
		details += '	<tr><td><b>Timestamp:</b></td><td>'+entry.startedDateTime+'</td></tr>';
		details += '	<tr><td><b>Duration:</b></td><td>'+Math.ceil(entry.time)+'ms</td></tr>';
		details += '	<tr><td><b>Version:</b></td><td>'+request.httpVersion+'</td></tr>';
		details += '	<tr><td><b>Method:</b></td><td>'+request.method+'</td></tr>';
		details += '	<tr><td><b>URL:</b></td><td>'+request.url+'</td></tr>';
		
		details += convertNameValueArrayToRow("QueryParameters", request.queryString);
		details += convertNameValueArrayToRow("Headers", request.headers);
		details += convertNameValueArrayToRow("Cookies", request.cookies);
		
		if( typeof request.postData != "undefined" ){
			details += '	<tr><td><b>MimeType:</b></td><td>'+request.postData.mimeType+'</td></tr>';
			details += '	<tr><td><b>Content:</b></td><td><pre><code>'+request.postData.text.replace(/</g, "&lt;")+'</code></pre></td></tr>';
		}
		details += '</table>';
		
	}else 	if(tab === 'response'){
		var response = entry.response;
		details += '<table class="table table-striped">';
		details += '	<tr><td><b>Version:</b></td><td>'+response.httpVersion+'</td></tr>';
		details += '	<tr><td><b>Status:</b></td><td>'+createHTTPStatusBadge(response.status)+' '+response.statusText+'</td></tr>';
		details += '	<tr><td><b>RedirectURL:</b></td><td>'+response.redirectURL+'</td></tr>';
		details += '	<tr><td><b>ContentType:</b></td><td>'+response.content.mimeType+'</td></tr>';
		details += '	<tr><td><b>ContentSize:</b></td><td>'+response.content.size+' Bytes</td></tr>';
		details += '	<tr><td><b>TransferSize:</b></td><td>'+response._transferSize+' Bytes</td></tr>';
		details += convertNameValueArrayToRow("Headers", response.headers);
		details += convertNameValueArrayToRow("Cookies", response.cookies);
		
		if( typeof response.content.text != "undefined" ){
			details += '	<tr><td><b>Content:</b></td><td><pre><code>'+response.content.text.replace(/</g, "&lt;")+'</code></pre></td></tr>';
		}
		details += '</table>';
	
	}
	else if(tab === 'headers'){
		details += '<table class="table table-striped">';
		details += convertNameValueArrayToRow("Request Headers", entry.request.headers);
		details += convertNameValueArrayToRow("Response Headers", entry.response.headers);
		details += '</table>';
		
	}else if(tab === 'cookies'){
		details += '<table class="table table-striped">';
		details += convertNameValueArrayToRow("Request Cookies", entry.request.cookies);
		details += convertNameValueArrayToRow("Response Cookies", entry.response.cookies);
		details += '</table>';
		
	}else if(tab === 'timings'){
		
		details += createGanttChartLegend();
		
		details += '<div class="ganttBlock ganttTimings" style="width: 100%">';
		details += createGanttBar(entry, "blocked");
		details += createGanttBar(entry, "dns");
		details += createGanttBar(entry, "connect");
		//details += createGanttBar(currentEntry, "ssl");
		details += createGanttBar(entry, "send");
		details += createGanttBar(entry, "wait");
		details += createGanttBar(entry, "receive");
		details += '</div>';
		
		details += '<table class="table table-striped">';
		details += '<thead><th>Metric</th><th>Time</th><th>Percent</th></thead>'
		var metrics = ['blocked','dns','connect','ssl','send','wait','receive'];
		for(i = 0; i < metrics.length; i++){
			var metric = metrics[i];
			details += '<tr>';
			details += '	<td><b>'+metric+':</b></td>';
			details += '	<td>'+Math.round(entry.timings[metric] * 100) / 100+' ms</td>';
			details += '	<td>'+Math.round(entry.ganttdata["percent"+metric] * 100) / 100+'%</td>'
			details += '</tr>'; 
		}
		details += '</table>';
		details += '<p><b>TOTAL TIME: '+Math.round(entry.time * 100) / 100+' ms</b></p>';
		
	}
	
	target.append(details);
	
}

/******************************************************************
 * Create the Legend for the gantt chart colors.
 * 
 * @return html string
 ******************************************************************/
function createGanttChartLegend(){
	
	var metrics = ['blocked','dns','connect',/*'ssl,*/'send','wait','receive'];
	
	var legend = '<div class="gantt-legend">';
	
	for(i = 0; i < metrics.length; i++){
		legend += '<div class="gantt-legend-group">';
		legend += '		<div class="gantt-legend-square percent'+metrics[i]+'">&nbsp;</div>';
		legend += '		<span>'+metrics[i]+'</span>';
		legend += '</div>';
	}
	
	legend += '</div>';
	
	return legend;
	
}
/******************************************************************
 * Converts a array with name/value pairs to a two column table row.
 * 
 * @param title the title that will be printed in the first column.
 * @param array containing objects with name/value pairs like
 * [{name: value}, {name: value} ...] 
 * @return html string
 ******************************************************************/
function convertNameValueArrayToRow(title, array){
	result = "";
	if(array != null && array.length > 0){
		result += '	<tr><td><b>'+title+':</b></td>';
		result += '<td><table class="table table-striped">';
		for(i = 0; i < array.length; i++){
			result += '	<tr><td><b>'+array[i].name+'</b></td><td>'+array[i].value+'</td></tr>';
		}
		result += '</table></td></tr>';
	}
	
	return result;
}

/******************************************************************
 * Returns a colored badge for the given HTTP status.
 * 
 * @param status the http status as integer 
 * @return badge as html
 ******************************************************************/
function createHTTPStatusBadge(status){
	
	var style = "";
	if		(status < 200)	{style = "info"; }
	else if (status < 300)	{style = "success"; }
	else if (status < 400)	{style = "warning"; }
	else 					{style = "danger"; }
	
	return '<span class="badge btn-'+style+'">'+status+"</span>";
}
/******************************************************************
 * Print the list of results found in the database.
 * 
 * @param parent JQuery object
 * @param data object containing the list of results.
 * 
 ******************************************************************/
function printResultList(parent, data){
	
	//----------------------------------
	// Add title and description.
	parent.append("<h2>Result History</h2>");
	parent.append("<p>Click on the eye symbol to open a result. Select multiple results and hit compare to get a comparison.</p>");

	//----------------------------------
	// Create Table Header
	
	var cfwTable = CFW.ui.createTable();
	cfwTable.isSticky = true;
	
	cfwTable.addHeaders(['&nbsp;', 'Timestamp']);
	if(data[0] != null && data[0].USER_ID != undefined){
		cfwTable.addHeader('User');
	}
	
	cfwTable.addHeaders(['Name', 'URL', '&nbsp;', '&nbsp;', '&nbsp;', '&nbsp;']);
	if (CFW.hasPermission("Download HAR")){ cfwTable.addHeaders(['&nbsp;', '&nbsp;']); }
	if (CFW.hasPermission("Delete Result")){ cfwTable.addHeaders(['&nbsp;']); }
	//----------------------------------
	// Create Rows
	var resultCount = data.length;
	
	if(resultCount == 0){
		CFW.ui.addAlert("info", "Hmm... seems you don't have any results. Try to upload a HAR file oe analyze a URL.");
	}
	for(var i = 0; i < resultCount; i++ ){
		var currentData = data[i];
		var rowString = '<tr>';
		
		rowString += '<td><input class="resultSelectionCheckbox" type="checkbox" onchange="resultSelectionChanged();" value="'+currentData.PK_ID+'" /></td>';
		rowString += '<td>'+CFW.format.epochToTimestamp(currentData.TIME_CREATED)+'</td>';
		
		if(data[0] != null && data[0].USERNAME != undefined){
			rowString += '<td>'+currentData.USERNAME+'</td>';;
		}
		
		resultName = (currentData.NAME == "null") ? "" : currentData.NAME;
		rowString += '<td>'+resultName+'</td>';
		// URL Column
		url = CFW.http.secureDecodeURI(currentData.PAGE_URL);
		rowString += '<td>'+url+'</td>';
		
		// View Result Icon
		rowString += '<td><a class="btn btn-primary btn-sm" alt="View Result" title="View Result" href="./resultview?resultid='+currentData.PK_ID+'"><i class="fa fa-eye"></i></a></td>';
		
		// Gantt Chart Icon
		if (CFW.hasPermission("Download HAR")){
			rowString += '<td><a class="btn btn-primary btn-sm" alt="View Gantt Chart" title="View Gantt Chart" href="./ganttchart?resultid='+currentData.PK_ID+'"><i class="fas fa-signal fa-rotate-90"></i></a></td>';
		}
		// Link Icon
		rowString += '<td><a class="btn btn-primary btn-sm" target="_blank" alt="Open URL" title="Open URL" href="'+url+'"><i class="fa fa-link"></i></a></td>';
		
		// Save Result
		var regex = /.*?http.?:\/\/([^\/]*)/g;
		var matches = regex.exec(CFW.http.secureDecodeURI(currentData.PAGE_URL));
		
		var resultName = "result";
		if(matches != null){
			resultName = matches[1];
		}
		

		rowString += '<td><a class="btn btn-primary btn-sm" target="_blank" alt="Download Result" title="Download Result" href=./data?type=yslowresult&resultid='+currentData.PK_ID+' download="'+resultName+'_yslow_results.json"><i class="fa fa-save"></i></a></td>';
		
		//Download HAR
		if (CFW.hasPermission("Download HAR")){
			rowString += '<td><a class="btn btn-primary btn-sm" target="_blank" alt="Dowload HAR" title="Dowload HAR" href=./data?type=har&resultid='+currentData.PK_ID+' download="'+resultName+'.har"><i class="fa fa-download"> HAR</i></a></td>';
		}
		
		//Delete Result
		if (CFW.hasPermission("Delete Result")){
			rowString += '<td><button class="btn btn-danger btn-sm" alt="Delete Result" title="Delete Result" onclick="CFW.ui.confirmExecute(\'Do you want to delete the results?\', \'Delete\', \'deleteResults('+currentData.PK_ID+')\')"><i class="fa fa-trash"></i></button></td>';
		}
		rowString += "</tr>";
		
		cfwTable.addRow(rowString);
	}
	
	cfwTable.appendTo(parent);
	
	//----------------------------------
	// Create Button
	var selectAllButton = $('<button id="selectAllButton" class="btn btn-secondary" onclick="'+"$('.resultSelectionCheckbox').prop('checked', true);resultSelectionChanged();"+'">Select All</button>');
	var deselectAllButton = $('<button id="deselectAllButton" class="btn btn-secondary" onclick="'+"$('.resultSelectionCheckbox').prop('checked', false);resultSelectionChanged();"+'">Deselect All</button>');
	var compareButton = $('<button id="resultCompareButton" class="btn btn-primary" onclick="compareResults();" disabled="disabled">Compare</button>');
	var deleteButton = $('<button id="resultDeleteButton" class="btn btn-danger" disabled="disabled">Delete</button>');
	deleteButton.attr('onclick', "CFW.ui.confirmExecute('Do you want to delete the selected results?', 'Delete', 'deleteResults(null)')");
	parent.append(selectAllButton);
	parent.append(deselectAllButton);
	parent.append(compareButton);
	if (CFW.hasPermission("Delete Result")){
		parent.append(deleteButton);
	}
}

/**************************************************************************************
 * Enables/Disables buttons on the result list depending on how many checkboxes are
 * selected.
 * 
 *************************************************************************************/
function resultSelectionChanged(){
		
	if($(".resultSelectionCheckbox:checked").length > 1){
		$("#resultCompareButton").attr("disabled", false);
	}else{
		$("#resultCompareButton").attr("disabled", "disabled");
	}
	
	if($(".resultSelectionCheckbox:checked").length > 0){
		$("#resultDeleteButton").attr("disabled", false);
	}else{
		$("#resultDeleteButton").attr("disabled", "disabled");
	}
}

/**************************************************************************************
 * Load the comparison page for the selected results.
 *************************************************************************************/
function compareResults(){
		
	var resultIDs = "";
	$.each($(".resultSelectionCheckbox:checked"), function(){
		resultIDs += $(this).val()+",";
	});
	resultIDs = resultIDs.slice(0,-1);
	
	self.location = "./compare?resultids="+resultIDs;
	
}

/**************************************************************************************
 * Delete the selected results.
 * 
 *************************************************************************************/
function deleteResults(resultIDs){
	
	if(resultIDs == null){
		var resultIDs = "";
		$.each($(".resultSelectionCheckbox:checked"), function(){
			resultIDs += $(this).val()+",";
		});
		resultIDs = resultIDs.slice(0,-1);
	}
	
	
	self.location = "./delete?resultids="+resultIDs;
	
}

/**************************************************************************************
 * Print the details for the rule.
 * 
 * @param parent JQuery object
 * @param rule the rule from the yslow results to print the details for.
 *************************************************************************************/
function printRuleDetails(parent, rule){
	
	if(rule.grade != null){ 			parent.append('<p><strong>Grade:&nbsp;<span class="btn btn-'+GRADE_CLASS[rule.grade]+'">'+rule.grade+'</span></strong></p>');}	
	if(rule.score != null){ 			parent.append('<p><strong>Score:&nbsp;</strong>'+rule.score+'</p>');}
	if(rule.name != null){ 				parent.append('<p><strong>Name:&nbsp;</strong>'+rule.name+'</p>');}
	if(rule.title != null){ 			parent.append('<p><strong>Title:&nbsp;</strong>'+rule.title+'</p>');}
	if(rule.description != null){ 		parent.append('<p><strong>Description:&nbsp;</strong>'+rule.description+'</p>');}
	if(rule.weight != null){ 			parent.append('<p><strong>Weight:&nbsp;</strong>'+rule.weight+'</p>');}
	
	if(rule.message != null  
	&& rule.message != undefined
	&& rule.message.length > 0 ){  		parent.append('<p><strong>Message:&nbsp;</strong>'+rule.message+'</p>');}
	
	if(rule.components.length > 0){ 			
		parent.append('<p><strong>Details:</strong></p>');
		var list = $('<ul>');
		parent.append(list);
		for(var key in rule.components){
			var compText = "";
			try{
				compText = decodeURIComponent(rule.components[key]);
			}catch(err){
				compText = rule.components[key];
			}
			list.append('<li>'+compText+'</li>');
		}
	}
	
	if(rule.url != null){ parent.append('<p><strong>Read More:&nbsp;</strong><a target="_blank" href="'+rule.url+'">'+rule.url+'</a></p>');}
}
/**************************************************************************************
 * Create the panel for the given rule.
 * 
 * @param rule the rule from the yslow results to print the details for.
 * 
 *************************************************************************************/
function createRulePanel(rule){
	
	GLOBAL_COUNTER++;
	
	var gradeClass = GRADE_CLASS[rule.grade];
	var panel = $(document.createElement("div"));
	panel.addClass("card border-"+gradeClass);
	
//	<div class="card">
//	  <div class="card-header">
//	    Featured
//	  </div>
//	  <div class="card-body">
//	    <h5 class="card-title">Special title treatment</h5>
//	    <p class="card-text">With supporting text below as a natural lead-in to additional content.</p>
//	    <a href="#" class="btn btn-primary">Go somewhere</a>
//	  </div>
//	</div>
	//----------------------------
	// Create Header
	var panelHeader = $(document.createElement("div"));
	panelHeader.addClass("card-header bg-"+gradeClass);
	panelHeader.attr("id", "panelHead"+GLOBAL_COUNTER);
	//panelHeader.attr("role", "tab");
	panelHeader.append(
		'<span class="card-title text-light">'+
		/*style.icon+*/
		'<a role="button" data-toggle="collapse" data-target="#collapse'+GLOBAL_COUNTER+'">'+
		'<strong>Grade '+rule.grade+' ('+rule.score+'%):</strong>&nbsp;'+rule.title+
		'</a></span>'
	); 
	panelHeader.append(
			'<span class="text-light" style="float: right;">(Rule: ' + rule.name+ ')</span>'
		); 
	
	panel.append(panelHeader);
	
	//----------------------------
	// Create Collapse Container
	var collapseContainer = $(document.createElement("div"));
	collapseContainer.addClass("collapse");
	collapseContainer.attr("id", "collapse"+GLOBAL_COUNTER);
	//collapseContainer.attr("role", "tabpanel");
	collapseContainer.attr("aria-labelledby", "panelHead"+GLOBAL_COUNTER);
	
	panel.append(collapseContainer);
	
	//----------------------------
	// Create Body
	var panelBody = $(document.createElement("div"));
	panelBody.addClass("card-body");
	collapseContainer.append(panelBody);
	
	printRuleDetails(panelBody, rule);
	
	return {
		panel: panel,
		panelHeader: panelHeader,
		panelBody: panelBody
	};
}


/******************************************************************
 * Format the yslow results as plain text.
 * 
 * @param parent JQuery object
 * 
 ******************************************************************/
function printPlainText(parent){
	parent.append("<h3>Plain Text</h3>");
	
	var ruleCount = RULES.length;
	for(var i = 0; i < ruleCount; i++){
		var rule = RULES[i];
		var div = $("<div>") ;
		
		div.append('<h2 class="text-'+GRADE_CLASS[rule.grade]+'"><strong>'+rule.grade+'('+rule.score+'%):</strong>&nbsp;'+rule.title+'</h2>');
		
		printRuleDetails(div, rule);
		parent.append(div);
		
	}
}

/******************************************************************
 * Format the yslow results for a JIRA ticket.
 * 
 * @param parent JQuery object
 * 
 ******************************************************************/
function printJIRAText(parent){
	parent.append("<h3>JIRA Ticket Text</h3>");
	parent.append("<p>The text for each rule can be copy &amp; pasted into a JIRA ticket description, it will be formatted accordingly.</p>");
	
	var ruleCount = RULES.length;
	for(var i = 0; i < ruleCount; i++){
		var rule = RULES[i];
		var div = $("<div>") ;
		
		div.append('<h2 class="text-'+GRADE_CLASS[rule.grade]+'"><strong>'+rule.grade+'('+rule.score+'%):</strong>&nbsp;'+rule.title+'</h2>');
		
		if(rule.title != null){ 			div.append('*Title:*&nbsp;'+rule.title+'</br>');}
		if(rule.grade != null){ 			div.append('*Grade:*&nbsp;'+rule.grade+'</br>');}	
		if(rule.score != null){ 			div.append('*Score:*&nbsp;'+rule.score+'%</br>');}
		if(rule.description != null){ 		div.append('*Description:*&nbsp;</strong>'+rule.description+'</br>');}
		
		if(rule.message != null  
	    && rule.message != undefined
	    && rule.message.length > 0 ){ 		div.append('*Message:*&nbsp;'+rule.message+'</br>');}
		
		if(rule.components.length > 0){ 			
			div.append('*Details:*</br>');
			for(var key in rule.components){
				var compText = "";
				try{
					compText = decodeURIComponent(rule.components[key]);
				}catch(err){
					compText = rule.components[key];
				}
				div.append('<li>'+compText+'</li>');
			}
		}
		
		if(rule.url != null){ div.append('*Read More:*&nbsp;</strong>'+rule.url+'</br>');}
		
		parent.append(div);
		
	}
}

/******************************************************************
 * Format the yslow results as a CSV file.
 * 
 * @param parent JQuery object
 * @param data the data to be printed.
 ******************************************************************/
function printCSV(parent, data){
	
	parent.append("<h2>CSV Export</h2>");
	parent.append("<p>Click on the text to select everything.</p>");
	
	var pre = $('<pre>');
	parent.append(pre);
	
	var code = $('<code>');
	code.attr("onclick", "CFW.selection.selectElementContent(this)");
	pre.append(code);
	
	var headerRow = "";

		
	for(var key in data[0]){
		if(key != "components" && key != "description"){
			headerRow += key+';';
		}
	}
	code.append(headerRow+"</br>");
	
	var rowCount = data.length;
	for(var i = 0; i < rowCount; i++ ){
		var currentData = data[i];
		var row = "";
		
		for(var cellKey in currentData){
			if(cellKey != "components" && cellKey != "description"){
				row += '&quot;'+currentData[cellKey]+'&quot;;';
			}
		}
		code.append(row+"</br>");
	}
	parent.append(pre);
	
}

/******************************************************************
 * Format the yslow results as a JSON file.
 * 
 * @param parent JQuery object
 * @param data the data to be printed.
 ******************************************************************/
function printJSON(parent, data){
	
	parent.append("<h2>JSON</h2>");
	parent.append("<p>Click on the text to select everything.</p>");
	
	var pre = $('<pre>');
	parent.append(pre);
	
	var code = $('<code>');
	code.attr("onclick", "CFW.selection.selectElementContent(this)");
	pre.append(code);
	
	code.text(JSON.stringify(data, 
		function(key, value) {
	    if (key == 'description') {
            // Ignore description to reduce output size
            return;
	    }
	    return value;
	},2));
	
}

/******************************************************************
 * Format the yslow results as a html table.
 * 
 * @param parent JQuery object
 * @param data the data to be printed.
 * 
 ******************************************************************/
function printTable(parent, data, title){
	
	parent.append("<h3>"+title+"</h3>");
	
	var cfwTable = CFW.ui.createTable();

	// add all keys from the first object in the array as headers
	for(var key in data[0]){
		cfwTable.addHeader(key);
	}
	
	var ruleCount = data.length;
	
	for(var i = 0; i < ruleCount; i++ ){
		var currentData = data[i];
		var row = $('<tr>');
		
		for(var cellKey in currentData){
			if(cellKey != "components"){
				row.append('<td>'+currentData[cellKey]+'</td>');
			}else{
				var list = $('<ul>');
				for(var key in currentData.components){
					var compText = "";
					try{
						compText = decodeURIComponent(currentData.components[key]);
					}catch(err){
						compText = currentData.components[key];
					}
					list.append('<li>'+compText+'</li>');
				}
				var cell = $("<td>");
				cell.append(list);
				row.append(cell);
			}
		}
		cfwTable.addRow(row);
	}
	cfwTable.appendTo(parent);
	
}

/******************************************************************
 * Format the yslow results as panels.
 * 
 * @param parent JQuery object
 * 
 ******************************************************************/
function printPanels(parent){
	
	parent.append("<h3>Panels</h3>");
	parent.append("<p>Click on the panel title to expand for more details.</p>");
	
	var ruleCount = RULES.length;
	for(var i = 0; i < ruleCount; i++){
		var panelObject = createRulePanel(RULES[i]);
		
		if(parent != null){
			parent.append(panelObject.panel);
		}else{
			$("#content").append(panelObject.panel);
		}
	}
}

/**************************************************************************************
 * Print the summary for the yslow results.
 * 
 * @param parent JQuery object
 * 
 *************************************************************************************/
function printSummary(parent){
	
	parent.append("<h3>Summary</h3>");
	
	var list = $("<ul>");
	
	if(SUMMARY.grade != null){ 				list.append('<li><strong>Grade:&nbsp;<span class="btn btn-'+GRADE_CLASS[SUMMARY.grade]+'">'+SUMMARY.grade+'</strong></li>');}
	if(SUMMARY.totalScore != null){ 		list.append('<li><strong>Total Score:&nbsp;</strong>'+SUMMARY.totalScore+'%</li>');}
	if(SUMMARY.url != null){ 				list.append('<li><strong>URL:&nbsp;</strong><a href="'+SUMMARY.url+'">'+SUMMARY.url+'</a></li>');}
	if(SUMMARY.size != null){ 				list.append('<li><strong>Page Size:&nbsp;</strong>'+SUMMARY.size+' Bytes</li>');}
	if(SUMMARY.sizeCached != null){ 		list.append('<li><strong>Page Size(cached):&nbsp;</strong>'+SUMMARY.sizeCached+' Bytes</li>');}
	if(SUMMARY.requests != null){ 			list.append('<li><strong>Request Count:&nbsp;</strong>'+SUMMARY.requests+'</li>');}
	if(SUMMARY.requestsCached != null){ 	list.append('<li><strong>Cached Requests Count:&nbsp;</strong>'+SUMMARY.requestsCached+'</li>');}
	if(SUMMARY.loadtime != null 
	&& SUMMARY.loadtime != "-1"){ 			list.append('<li><strong>Load Time:&nbsp;</strong>'+SUMMARY.loadtime+' ms</li>');}
	
	if(SUMMARY.ruleset != null){ 			list.append('<li><strong>YSlow Ruleset:&nbsp;</strong>'+SUMMARY.ruleset+'</li>');}
	
	parent.append(list);
	
}
/******************************************************************
 * 
 ******************************************************************/
function reset(){
	GLOBAL_COUNTER=0;
	$("#results").html("");
}

/******************************************************************
 * Main method for building the different views.
 * 
 * @param options Array with arguments:
 * 	{
 * 		data: 'yslowresult|resultlist|har|comparyslow', 
 * 		info: 'overview|grade|stats|resultlist|ganttchart|compareyslow|', 
 * 		view: 'table|panels|plaintext|jira|csv|json', 
 * 		stats: 'type|type_cached|components'
 *  }
 * @return 
 ******************************************************************/
function draw(options){
	
	reset();
	
	CFW.ui.toogleLoader(true);
	
	window.setTimeout( 
	function(){
	
		RESULTS_DIV = $("#results");
		
		//----------------------------------
		// Fetch Data if not already done
		//----------------------------------
		switch (options.data){
			case "yslowresult": 	if(YSLOW_RESULT == null) { fetchData(options);  return;} break;
			case "resultlist":		if(RESULT_LIST == null) { fetchData(options); return;} break;
			case "allresults":		if(RESULT_LIST == null) { fetchData(options); return;} break;
			case "har":				if(HAR_DATA == null) { fetchData(options); return;} break;
			case "compareyslow":	if(COMPARE_YSLOW == null) { fetchData(options); return;} break;
			
		}
		
		//----------------------------------
		// Fetch Data if not already done
		//----------------------------------
		switch(options.info + options.view){
		
			case "resultlist":		printResultList($(RESULTS_DIV), RESULT_LIST);
									break;
									
			case "ganttchart":		printGanttChart($(RESULTS_DIV), HAR_DATA);
									break;	
			
			case "compareyslow":	printComparison($(RESULTS_DIV), COMPARE_YSLOW);
									break;	
									
			case "overview": 		printSummary(RESULTS_DIV);
									printTable(RESULTS_DIV, STATS_BY_TYPE, "Statistics by Component Type(Empty Cache)");
									printTable(RESULTS_DIV, STATS_PRIMED_CACHE, "Statistics by Component Type(Primed Cache)");
									printPanels(RESULTS_DIV);
									break;
									
			case "gradepanels": 	printPanels(RESULTS_DIV);
						  			break;
						  			
			case "gradetable": 		printTable(RESULTS_DIV, RULES, "Table: Grade by Rules");
	  								break;
	  								
			case "gradeplaintext":	printPlainText(RESULTS_DIV);
									break;
									
			case "gradejira":		printJIRAText(RESULTS_DIV);
									break;
									
			case "gradecsv":		printCSV(RESULTS_DIV, RULES);
									break;
									
			case "gradejson":		printJSON(RESULTS_DIV, RULES);
									break;						
									
			case "statstable":		
				switch(options.stats){
					case "type": 			printTable(RESULTS_DIV, STATS_BY_TYPE, "Statistics by Component Type(Empty Cache)");
											break;
									
					case "type_cached": 	printTable(RESULTS_DIV, STATS_PRIMED_CACHE, "Statistics by Component Type(Primed Cache)");
											break;
											
					case "components": 		printTable(RESULTS_DIV, COMPONENTS, "Components");
											break;
				}
				break;
								
			default:				RESULTS_DIV.text("Sorry some error occured, be patient while nobody is looking into it.");
		}
		
		CFW.ui.toogleLoader(false);
	}, 100);
	
	
}