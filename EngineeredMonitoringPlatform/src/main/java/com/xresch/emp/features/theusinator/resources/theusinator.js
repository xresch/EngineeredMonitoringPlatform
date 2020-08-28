/**************************************************************************************
 * Global variables
 *************************************************************************************/

//-------------------------
// API Access 
var SPM_USER_ID;
var SPM_USER_ROLE;
var SESSION;

var URL_PARAMETERS = CFW.http.getURLParams();
var ENVIRONMENT = URL_PARAMETERS.env;

//-------------------------
// Dashboard Settings
var SORT_ORDER = (retrieveLocalValue('SORT_ORDER') == '')? 'fullName' : retrieveLocalValue('SORT_ORDER');
var CURRENT_MEASURE = (retrieveLocalValue('CURRENT_MEASURE') == '')? 'Accuracy' : retrieveLocalValue('CURRENT_MEASURE');
var CURRENT_MERGE_SPAN = (retrieveLocalValue('CURRENT_MERGE_SPAN') == '')? 10800 : retrieveLocalValue('CURRENT_MERGE_SPAN');
var CURRENT_MEASURE_DATA = (retrieveLocalValue('CURRENT_MEASURE_DATA') == '')? 'avg' : retrieveLocalValue('CURRENT_MEASURE_DATA');
var CURRENT_TIME_FRAME = (retrieveLocalValue('CURRENT_TIME_FRAME') == '')? 'oneyear' : retrieveLocalValue('CURRENT_TIME_FRAME');
var CURRENT_VIEW = (retrieveLocalValue('CURRENT_VIEW') == '')? 'dashboard' : retrieveLocalValue('CURRENT_VIEW');
var CURRENT_PROJECT = (retrieveLocalValue('CURRENT_PROJECT') == '')? 'start' : retrieveLocalValue('CURRENT_PROJECT');
var CURRENT_MODAL_PROJECT;
var CURRENT_CHART_TYPE = 'bar';

var BORDER_RADIUS = (retrieveLocalValue('BORDER_RADIUS') == '')? 5 : retrieveLocalValue('BORDER_RADIUS');
var RETRY_COUNTER = 0;
var ZOOM = (retrieveLocalValue('ZOOM') == '')? 1.0 : retrieveLocalValue('ZOOM');
var DISPLAY_MEASURE_NAME = 'Overall Health';

var ENV_BOX = (retrieveLocalValue('ENV_BOX') == '')? true : retrieveLocalValue('ENV_BOX');
var TID_BOX = (retrieveLocalValue('TID_BOX') == '')? true : retrieveLocalValue('TID_BOX');
var CUSTOM_TAG_BOX = (retrieveLocalValue('CUSTOM_TAG_BOX') == '')? true : retrieveLocalValue('CUSTOM_TAG_BOX');

var DISPLAY_MODE = (retrieveLocalValue('DISPLAY_MODE') == '')? "regular" : retrieveLocalValue('DISPLAY_MODE');
var TEXT_SIZE = (retrieveLocalValue('TEXT_SIZE') == '')? "small" : retrieveLocalValue('TEXT_SIZE');

//-------------------------
// Used to apply styleMode after all projects have been drawn
var PROJECT_COUNTER = 0;

//-------------------------
// Interval Settings
var REFRESH_TIME = (retrieveLocalValue('REFRESH_TIME') == '')? 'stop' : retrieveLocalValue('REFRESH_TIME');
var INTERVAL_ID;
var ALIVE;
var BLINK_INTERVALS = [];

//-------------------------
// Reduce jQuery DOM Access
var PROJECT_FILTER = $('#projectFilter');
var RESULTS_DIV = $('#result');


//-------------------------
// Data Structures
var SORTED_PROJECT_IDS = [];
var PROJECT_LIST = {};

//-------------------------
// TimeFrame Settings 
var DASHBOARD_TIME_FRAME = (retrieveLocalValue('DASHBOARD_TIME_FRAME') == '')? 24 : retrieveLocalValue('DASHBOARD_TIME_FRAME');
var DASHBOARD_TIME_FRAME_STRING = (retrieveLocalValue('DASHBOARD_TIME_FRAME_STRING') == '')? 'oneday' : retrieveLocalValue('DASHBOARD_TIME_FRAME_STRING');
var CURRENT_TIME = new Date().getTime();
var CURRENT_DATE = new Date(CURRENT_TIME);
var START_DATE;
var END_DATE;

//-------------------------
// Loading Texts
var LOADING_TEXTS = ["Getting Data...", "This might take a while...", "Working that API...", "Picking colors...", "Drawing content...", "No data point can hide from me!", "Decoding results...", "Analyzing...", "Doing stuff...", "I'd suggest grabbing a coffee right about now.", "Shovelling coal into the server...", "A few bits tried to escape, but we caught them.", "Don't think of a blue elephant while this is loading...", "Our other loading screen is much faster, but that's only for VIP's.", "Spinning up the hamster...", "Testing data on Timmy... ... ... We're going to need another Timmy.", "Loading humorous message ... Please Wait"];
	
//-------------------------
// Animations for optionPanel
// one time initialization
$('#toggleOptions').click(function() {
	$("#optionPanel").animate({'width': 'toggle'}, function() {
		toggleOptions();
	})
});
$('#toggleCloseOptions').click(function() {
	$("#optionPanel").animate({'width': 'toggle'});
});

/**************************************************************************************
 * Update data according to selected project
 * @param selector selected value of #projectSelector
 * @see storeLocalValue() 
 * @see draw()
 *************************************************************************************/
function fireProjectChange(selector) {
	
	CURRENT_PROJECT = $(selector).val();
	storeLocalValue('CURRENT_PROJECT', CURRENT_PROJECT);
	draw();
};

/**************************************************************************************
 * Update data according to selected measure
 * @param selector selected value of #measureSelector
 * @see storeLocalValue() 
 * @see draw()
 *************************************************************************************/
function fireMeasureChange(selector) {
	
	CURRENT_MEASURE = $(selector).val();
	DISPLAY_MEASURE_NAME = CURRENT_MEASURE;
	storeLocalValue('CURRENT_MEASURE', CURRENT_MEASURE);
	draw();
};

/**************************************************************************************
 * Update data according to selected merge span
 * @param selector selected value of #mergeSpanSelector
 * @see storeLocalValue()
 * @see draw()
 *************************************************************************************/
function fireMergeSpanChange(selector) {
	
	CURRENT_MERGE_SPAN = $(selector).val();
	storeLocalValue('CURRENT_MERGE_SPAN', CURRENT_MERGE_SPAN);
	draw();
};

/**************************************************************************************
 * Update data according to selected measure data
 * @param selector selected value of #measureDataSelector
 * @see storeLocalValue()
 * @see draw()
 *************************************************************************************/
function fireMeasureDataChange(selector) {
	
	CURRENT_MEASURE_DATA = $(selector).val();
	storeLocalValue('CURRENT_MEASURE_DATA', CURRENT_MEASURE_DATA);
	draw();
};

/**************************************************************************************
 * Update data according to selected time frame. Reset datepicker.
 * @param selector selected value of #timeFrameSelector
 * @see storeLocalValue()
 * @see draw()
 *************************************************************************************/
function fireTimeFrameChange(selector) {
	
	CURRENT_TIME_FRAME = $(selector).val();
	storeLocalValue('CURRENT_TIME_FRAME', CURRENT_TIME_FRAME);
	$('.datepickerField').val('');
	draw();
};

/**************************************************************************************
 * Update data according to selected time frame and reset refresh timer
 * @param selector selected value of #timeFrameSelector
 * @see storeLocalValue()
 * @see refreshTimer()
 * @see draw()
 *************************************************************************************/
function fireViewChange(selector) {
	
	var optionPanel = $('#optionPanel');
	
	if(optionPanel.css('display') != 'none') {
		optionPanel.css('display', 'none');
	}
	CURRENT_VIEW = $(selector).val();
	storeLocalValue('CURRENT_VIEW', CURRENT_VIEW);
	clearInterval(INTERVAL_ID);
	refreshTimer();
	draw();
};

/**************************************************************************************
 * Change size of boxes in dashboard
 * @param selector selected value of #zoomSelector
 * @see storeLocalValue()
 * @see draw()
 *************************************************************************************/
function fireZoomChange(selector) {
	
	ZOOM = $(selector).val();
	storeLocalValue('ZOOM', ZOOM);
        if (ZOOM == 0.33) {
		$('#tidBox').prop('checked', false);
		$('.tidDiv').css('display', 'none');
	}
	draw();
};

/**************************************************************************************
 * Change duration between refreshes
 * @param selector selected value of #refreshSelector
 * @see storeLocalValue()
 * @see refreshTimer()
 *************************************************************************************/
function fireRefreshChange(selector) {
	
	REFRESH_TIME = $(selector).val();
	storeLocalValue('REFRESH_TIME', REFRESH_TIME);
	clearInterval(INTERVAL_ID);
	refreshTimer();
};

/**************************************************************************************
 * Change type of chart in graph view
 * @param selector selected value of #chartSelector
 * @see drawChart()
 *************************************************************************************/
function fireChartTypeChange(selector) {
	
	var targetId = $(selector).parent().attr('id');
	
	CURRENT_CHART_TYPE = $(selector).val();
	
	if(CURRENT_VIEW == 'dashboard' || CURRENT_VIEW == 'status' || CURRENT_VIEW == 'box' || CURRENT_VIEW == 'history') {
		drawChart(CURRENT_CHART_TYPE, targetId, PROJECT_LIST[CURRENT_MODAL_PROJECT].timeseries[DASHBOARD_TIME_FRAME_STRING]);
	}else {
		drawChart(CURRENT_CHART_TYPE, targetId, PROJECT_LIST[CURRENT_PROJECT].timeseries[CURRENT_TIME_FRAME]);
	};
	selectStyleMode(document.getElementById('styleModeDropdown'));
};

/**************************************************************************************
 * Change order of projects in dashboard (by name, by tid, by environment, by custom tag)
 * @param selector selected value of #sortSelector
 * @see storeLocalValue()
 * @see startTheusinator();
 *************************************************************************************/
function fireSortChange(selector) {

	SORT_ORDER = $(selector).val();
	storeLocalValue('SORT_ORDER', SORT_ORDER);
	startTheusinator();
};

/**************************************************************************************
 * Update selected time frame
 * @param timeFrame used to set start and end dates
 *************************************************************************************/
function updateTimeFrame(timeFrame) {
	
	var oneYearsMillis = 1000 * 60 * 60 * 24 * 365;
	
	//-------------------------
	// Add one hour to current time to also get data of current hour
	// Needed since FormattedDate() will always round timestamps down
	// i.e. 15:33:32.123 => 15:00:00.000.
	var fullLastHour = 1000 * 60 * 60
	
	if(CURRENT_TIME_FRAME != 'custom') {
		
		CURRENT_TIME = new Date().getTime();
		CURRENT_DATE = new Date(CURRENT_TIME + fullLastHour);
		END_DATE = CURRENT_DATE;
	};
	
	var time;
	switch(timeFrame) {
		
		case "oneyear":
			time = CURRENT_TIME - oneYearsMillis;
			START_DATE = new Date(parseInt(time));
			break;
			
		case "onemonth":
			time = CURRENT_TIME + fullLastHour - (oneYearsMillis / 12);
			START_DATE = new Date(parseInt(time));
			break;
		
		case "oneweek":
			time = CURRENT_TIME + fullLastHour - ((oneYearsMillis / (12 * 30)) * 7);
			START_DATE = new Date(parseInt(time));
			break;
		
		case "oneday":
			time = CURRENT_TIME + fullLastHour - (oneYearsMillis / (12 * 30));
			START_DATE = new Date(parseInt(time));
			break;
		
		case "halfday":
			time = CURRENT_TIME + fullLastHour - (oneYearsMillis / (12 * 30 * 2));
			START_DATE = new Date(parseInt(time));
			break;
			
		case "onehour":
			time = CURRENT_TIME;
			START_DATE = new Date(parseInt(time));
			break;
	};
};

/**************************************************************************************
 * doFilter
 *************************************************************************************/
function doFilter(searchField){
	
	var table = $(searchField).data("table");
	var input = searchField;
	
	storeLocalValue("searchFilter", input.value);
	
	var filter = input.value.toUpperCase();
	
	//only filter direct children
	table.find("> tbody > tr").each(function( index ) {
		  var row = $(this);
		  //console.log( index + ": " + row.text() );
		  if (row.html().toUpperCase().indexOf(filter) > -1) {
			row.css("display", "");
			
		  } else {
			  //ignore headers
			  if (row.html().indexOf("standardGridTableHeader") == -1){
				row.css("display", "none");
			  }
		  }
	});
	
	//----------------------------
	// Filter Box View if available
	
	if($(".filterable").size() > 0){
		$(".filterable > div").each(function( index ) {
		  var div = $(this);
		  
		  if (div.html().toUpperCase().indexOf(filter) > -1) {
			div.css("display", "");
		  } else {
			div.css("display", "none"); 
		  }
	});
	}

}

/**************************************************************************************
 * Send API GET request to login the API user and get the sessionId
 *************************************************************************************/
function getSession(){
	$.get("./theusinator?env="+ENVIRONMENT+"&service=sccsystem&method=logonUser",	
		function(data) {
			
			SESSION = $(data).find('logonUserReturn').text();
			console.log("SESSION:"+SESSION)
		})
};

/**************************************************************************************
 * Send API GET request to get id of logged in spm user
 *************************************************************************************/
function getUserId() {
	
	$.get("./theusinator?env="+ENVIRONMENT+"&service=sccentities&method=getCurrentUser&sessionId="+SESSION,
	
		function(data) {
			SPM_USER_ID = $(data).find("id").text();
		})
};

/**************************************************************************************
 * Send API GET request to get all active projects for current user in current session
 * Create data structure used in entire application and prepare order according to
 * #sortSelector
 * @see retrieveLocalValue()
 * @see dynamicSort()
 *************************************************************************************/
function getProjectsForUser(){
	
	var url = "./theusinator?env="+ENVIRONMENT+"&service=sccentities&method=getProjectsForUser&sessionId=" + SESSION + "&userId=" + SPM_USER_ID + "&appModuleId=-1";
	
	$.get(url,
	
	function( data ) {
		
		$(data).find('multiRef').each(
			
			function(){
				
				if ($(this).find("active").text() == "true") {
					
					var project = {};
					var description = $(this).find("description").text().trim();
					project.id = parseInt($(this).find("id").text());
					project.fullName = $(this).find("name").text();
					project.Tag = retrieveLocalValue(project.id);
					project.hasConfigRights = false;
					
					try {
						description = JSON.parse(description);
						description.Tag = retrieveLocalValue(project.id);
						project.fullDescription = description;
						
						if(description.env != 'undefined') {
							project.environment = description.env;
						}
						
						if(description.tid != 'undefined') {
							project.tid = description.tid;
						}
						
						var nameWoEnv = project.fullName.replace('(' + description.env + ')', '');
						project.name = nameWoEnv.replace('[' + description.tid + ']', '').trim();
						
					}
					catch(err) {
						project.error = "Could not get project description. Exception: " + err.message;
					}
					
					//-------------------------
					// Prepare location for data
					project.timeseries = {
						'onehour': [],
						'oneday': [],
						'halfday': [],
						'oneweek': [],
						'onemonth': [],
						'oneyear': [],
						'custom': []
					};
					PROJECT_LIST[project.id] = project;
					
				}
			}
		);
		
	}).done(
	
		function(){
			
			var projectString = '<option value="start">Select project...</option>';
			var projectArray = [];
			var projectSelector = $("#projectSelector");
			
			//-------------------------
			//Prepare for sorting
			for(var index in PROJECT_LIST) {
				
				var sorter = {};
				sorter.id = PROJECT_LIST[index].id;
				sorter.fullName = PROJECT_LIST[index].fullName;
				sorter.name = PROJECT_LIST[index].name; 
				sorter.env = PROJECT_LIST[index].environment; 
				sorter.tid = PROJECT_LIST[index].tid;
				sorter.Tag = PROJECT_LIST[index].Tag;
				
				projectArray.push(sorter);
				
				
			}
			
			//-------------------------
			//Sort according to #sortSelector
			projectArray.sort(dynamicSort(SORT_ORDER));
			
			
			var loopable = projectArray.length;
			for (var i = 0; i < loopable; i++) {
				
				projectString += '<option value="' + projectArray[i].id + '">' + projectArray[i].fullName + '</option>';
				SORTED_PROJECT_IDS.push(projectArray[i].id);
			};
			
			projectSelector.find('option').remove();
			projectSelector.append(projectString);	
		}
	)
};

/**************************************************************************************
 * Send API GET request to obtain desired data for charts and tables and draw content
 * @param dataObject contains timeFrame, projectId, view, mergeSpan, measureName 
 * @see FormattedDate()
 * @see roundFLoat()
 * @see drawTile()
 * @see drawBoxTile()
 * @see drawHistoryTable()
 * @see drawStatusTile()
 * @see drawChart()
 * @see drawModalContent()
 *************************************************************************************/
function fetchDataAndDrawTimeseriesSingleProject(dataObject) {

	var url = "./theusinator?env="+ENVIRONMENT+"&service=svdata&method=getClientMeasureData&sessionId="
	+ SESSION + "&projectId=" + dataObject.projectId + "&measureName=" 
	+ encodeURIComponent(dataObject.measureName) + "&startTime=" + encodeURIComponent(new FormattedDate(START_DATE).FullDate()) + "&endTime=" + encodeURIComponent(new FormattedDate(END_DATE).FullDate()) + "&mergeSpan=" 
	+ dataObject.mergeSpan;
	
	//-------------------------
	//DISPLAY_MEASURE_NAME is used in labels on screen
	if (dataObject.measureName == 'Page round trip time/#Overall Response Time#') {
		DISPLAY_MEASURE_NAME = 'Page Round Trip Time';
	} else if(dataObject.measureName == 'TransactionResponseTime'){
		DISPLAY_MEASURE_NAME = 'Transaction Response Time';
	} else if(dataObject.measureName == 'Page HTML download time/#Overall Response Time#'){
		DISPLAY_MEASURE_NAME = 'Page HTML Download Time';
	} else if(dataObject.measureName == 'Page server busy time/#Overall Response Time#'){
		DISPLAY_MEASURE_NAME = 'Page Server Busy Time';
	}else if(dataObject.measureName == 'Custom timer/#Overall Response Time#') {
		DISPLAY_MEASURE_NAME = 'Custom Timers';
	} else {
		DISPLAY_MEASURE_NAME = dataObject.measureName;
	}
	
	//-------------------------
	//Delete old project data
	PROJECT_LIST[dataObject.projectId].timeseries = {
		'onehour': [],
		'oneday': [],
		'halfday': [],
		'oneweek': [],
		'onemonth': [],
		'oneyear': [],
		'custom': []
	};

	
	$.get(url,
	
		function(data) {
			
			$(data).find('multiRef').each(
			
			function() {
				
				var project = {};
				
				project.time = $(this).find("readableTime").text();
				
				if (CURRENT_MEASURE_DATA == "avg") {
					
					project.value = roundFloat(($(this).find("sum").text())/($(this).find("count").text()));					
				} else {
					
					project.value = roundFloat($(this).find(CURRENT_MEASURE_DATA).text());
				};
				project.max = roundFloat($(this).find("max").text());
				project.min = roundFloat($(this).find("min").text());
				
				PROJECT_LIST[dataObject.projectId].timeseries[dataObject.timeFrame].push(project);
				
			}
			)
		}	
	).fail(
	
		function() {
			
			if(RETRY_COUNTER <= 5) {
				
				fetchDataAndDrawTimeseriesSingleProject(dataObject);
				RETRY_COUNTER += 1;
			}
		}
	).done(
		
		function() {
			
			switch(dataObject.view) {
			
				case 'dashboard':
			
					drawTile(dataObject.projectId);
					break;
				
				case 'box':
			
					drawBoxTile(dataObject.projectId);
					break;
				
				case 'history':
			
					drawHistoryTable(dataObject.projectId);
					break;
					
				case 'status':
			
					drawStatusTile(dataObject.projectId);
					break;
				case 'graph':
					
					drawChart(CURRENT_CHART_TYPE, 'result',PROJECT_LIST[dataObject.projectId].timeseries[dataObject.timeFrame]);
					break;
				case 'modal':
					
					drawModalContent(dataObject.projectId);
					break;
			};
			
		PROJECT_COUNTER -= 1;
		if(PROJECT_COUNTER == 0) {
			//remove spm_custom.js: selectStyleMode(document.getElementById('styleModeDropdown'));
			
			applySpecialStyles();
			//-------------------------
			//load option panel to correctly display custom tags, tid and environment
			toggleOptions();
			handleCheckBoxClick();
		}
		}
	)
};

/**************************************************************************************
 * Get the Ids of the groups of which the user is administrator, super user or project 
 * owner and call function to set state in PROJECT_LIST
 * @see setProjectConfigRightsForProjects()
 *************************************************************************************/
function getGroupsWithConfigRights() {
	
	var userHasConfigRights = [];
	
	$.get("./theusinator?env="+ENVIRONMENT+"&service=sccentities&method=getMembershipsOfUser&sessionId=" + SESSION + "&userId=" + SPM_USER_ID,
	
		function(data) {
			$(data).find("multiRef").each(
			
				function() {
					var roleForGroup = $(this).find("roleName").text();
					
					if(roleForGroup == "Administrator" || roleForGroup == "SuperUser" || roleForGroup == "Project Manager") {
						userHasConfigRights.push($(this).find("groupId").text())
					}
				}
			)
		}).done(
		
				function() {
					setProjectConfigRightsForProjects(userHasConfigRights);
				}
			)
}

/**************************************************************************************
 * Set hasConfigRights of all projects to true where user has config rights
 * @param groupIds Ids of groups where user is administrator, super user or project owner
 *************************************************************************************/
function setProjectConfigRightsForProjects(groupIds){
	
	
	
	for(var i = 0; i < groupIds.length; i++) {
		
		$.get("./theusinator?env="+ENVIRONMENT+"&service=sccentities&method=getGroupById&sessionId=" + SESSION + "&groupId=" + groupIds[i],
	
		function(data) {
			$(data).find("projectAssignments").each(
			
				function() {
					var IdOfProjectWithConfigRights = $(this).text();
					
					if (typeof(PROJECT_LIST[IdOfProjectWithConfigRights]) != "undefined") {
						PROJECT_LIST[IdOfProjectWithConfigRights].hasConfigRights = true;
					}
				}
			)
		})
	}
}

/**************************************************************************************
 * Calculate n-th percentile of array
 * @param array unsorted array of values
 * @param percent specify which percentile is to be calculated
 * @return n-th percentile rounded to two decimals
 *************************************************************************************/
function calculatePercentile(array, percent){
	
	var index = percent * array.length;
	array = array.sort(function(a, b){return a-b});
	
	if (Math.floor(index) == index) {
		
		var percentile = (parseFloat(array[index-1]) + parseFloat(array[index])) / 2;
		return percentile;
	} else {
		
		percentile = parseFloat(array[Math.floor(index)]);
		return percentile;
	}
};

/**************************************************************************************
 * Calculate standard-deviation of array
 * @param array unsorted array of values
 * @return standard deviation rounded to two decimals
 *************************************************************************************/
function calculateStdDeviation(array){
	
	var average = calculateAverage(array);
	var diffs = array.map(function(value) {
		return value - average;
	});
	var squareDiffs = array.map(function(value) {
		var diff = value - average;
		return diff*diff;
	});
	
	var avgSquareDiff = calculateAverage(squareDiffs);
	return Math.sqrt(avgSquareDiff);
};

/**************************************************************************************
 * Calculate average of array
 * @param array unsorted array of values
 * @return average rounded to two decimals
 *************************************************************************************/
function calculateAverage(array) {
	
	var sum = 0;
	var loopable = array.length;
	
	for (var i= 0; i < loopable; i++) {
		
		sum = sum + parseFloat(array[i]);
	};
	
	return sum/array.length;
};

/**************************************************************************************
 * Calculate maximum of array
 * @param array unsorted array of values
 * @return maximum rounded to two decimals
 *************************************************************************************/
function calculateMaximum(array) {
	
	return Math.max.apply(null, array);
};

/**************************************************************************************
 * Calculate minimum of array
 * @param array unsorted array of values
 * @return minimum rounded to two decimals
 *************************************************************************************/
function calculateMinimum(array) {
	
	return Math.min.apply(null, array);
};

/**************************************************************************************
 * Set christmas mode
 *************************************************************************************/
function applySpecialStyles(){
	
	switch(DISPLAY_MODE){
		
		case "christmas":

			//$('#styleModeDropdown').val('night');
			//selectStyleMode(document.getElementById('styleModeDropdown'));
			
			$('#result').css('background-image', 'url(/resources/images/snowflakes.png)')
			.css('background-size', '15%');
			
			$('.tile').css('border-radius', '50%')
				.css('border', '2px solid gold');
				
			$('.tidDiv').css('text-align', 'center')
				.css('margin-left', '15%')
				.css('margin-right', '15%')
				.css('position', 'unset')
				.css('width', '70%')
				.css('bottom', 15*ZOOM);
			
			$('.envDiv').css('text-align', 'center')
				.css('margin-left', '15%')
				.css('margin-right', '15%')
				.css('position', 'relative')
				.css('width', '70%')
				.css('top', 15*ZOOM);
				
			PROJECT_FILTER.val('');
			break;
				
		case "easter":
		
			$('#result').css('background-image', 'url(/resources/images/sky.jpg)')
				.css('position', 'relative')
				.css('width', '100%')
				.css('min-height', '100%')
				.css('background-size', 'cover')
				.css('overflow', 'hidden');
			
			var threeDString = 'inset ' + 20 * ZOOM + 'px ' + 20 * ZOOM + 'px ' + 50 * ZOOM + 'px rgba(255,255,255,.5), inset ' + -20 * ZOOM +'px ' + -20 * ZOOM + 'px ' + 50 * ZOOM + 'px rgba(0,0,0,.5)';
			
			$('.tile').css('height', 200 * ZOOM)
				.css('-webkit-border-radius', '50%/60% 60% 40% 40%')
				.css('-moz-border-radius', '50%/60% 60% 40% 40%')
				.css('border-radius', '50%/60% 60% 40% 40%')
				.css('transform', 'rotate(15deg)')
				.css('box-shadow', threeDString)
				.css('border', '0px');
				
			$('.tidDiv').css('text-align', 'center')
				.css('margin-left', '15%')
				.css('margin-right', '15%')
				.css('position', 'unset')
				.css('width', '70%')
				.css('bottom', 15*ZOOM);
			
			$('.envDiv').css('text-align', 'center')
				.css('margin-left', '15%')
				.css('margin-right', '15%')
				.css('position', 'relative')
				.css('width', '70%')
				.css('top', 15*ZOOM);
			break;
		
		case "april":
			$('.tile').css('border', '0.5px solid #91160d');
			$('.tile').css('background-color', '#dd1506');
			PROJECT_FILTER.val(''); 
			break;
		case "regular":  
			$('#result').css('background-image', 'none');
			break; 
			
		default: break; //do nothing; 
	}
}

/**************************************************************************************
 * Call function to draw data according to selected view
 * @see retrieveLocalValue() 
 * @see showLoader()
 * @see drawDashboardView()
 * @see drawBoxView()
 * @see drawHealthHistoryView()
 * @see drawStatusView()
 * @see drawGraphView()
 * @see filterProjects()
 *************************************************************************************/
function draw() {
	
	var datepicker = $('.datepickerField');

	if($("#startDate").val() == '' && $("#endDate").val() == '') {
		if (retrieveLocalValue('CURRENT_TIME_FRAME') != '') {
			$('#timeFrameSelector option[value="' + retrieveLocalValue("CURRENT_TIME_FRAME") + '"]').attr('selected', 'selected');
			CURRENT_TIME_FRAME = retrieveLocalValue("CURRENT_TIME_FRAME");
		}
	}
	
	RESULTS_DIV.html('');
	datepicker.datepicker();
	
	showLoader(true);
	window.setTimeout(
	
		function() {
			switch(CURRENT_VIEW) {
		
				case "dashboard":
				
					PROJECT_COUNTER = SORTED_PROJECT_IDS.length;
					drawDashboardView();
					$("#tidBoxText").text(" Project Name");
					break;
				
				case "box":
					
					PROJECT_COUNTER = SORTED_PROJECT_IDS.length;
					drawBoxView();
					$("#tidBoxText").text(" TID");
					break;
				
				case "history":
					
					PROJECT_COUNTER = SORTED_PROJECT_IDS.length;
					drawHealthHistoryView();
					break;
				
				case "status":
					
					PROJECT_COUNTER = SORTED_PROJECT_IDS.length;
					drawStatusView();
					break;
					
				case "graph":
					PROJECT_COUNTER = 1;
					drawGraphView();					
					break;				
			}
			filterProjects(PROJECT_FILTER);
			showLoader(false);
			
		}, 100
	)
};

/**************************************************************************************
 * Get data and draw dashboard view
 * @see updateTimeFrame() 
 * @see fetchDataAndDrawTimeseriesSingleProject()
 *************************************************************************************/
function drawDashboardView() {
	
	//-------------------------
	//Dashboard settings
	var datepicker = $('.datepickerField');
	$('#toggleOptions').css('display', '');
	$('#toggleTextSize').css('display', 'none');
	datepicker.val('');
	RETRY_COUNTER = 0;
	$(".hideable").css("display", "none");
	$("#zoomDiv").css("display", "");
	$("#filterDiv").css("display", "");
	CURRENT_MEASURE_DATA = 'avg';
	CURRENT_TIME_FRAME = 'onehour';
	updateTimeFrame('onehour');
	
	var loopable = SORTED_PROJECT_IDS.length;
	
	//-------------------------
	//Get data for every project
	for(var i = 0; i< loopable; i++) {
		
		var dataObject = {
		timeFrame: 'onehour',
		projectId: SORTED_PROJECT_IDS[i],
		view: 'dashboard',
		mergeSpan: 60,
		measureName: "Overall Health"
		};
		RESULTS_DIV.append('<div class="tile filterable" id=' + dataObject.projectId + '></div>');
		fetchDataAndDrawTimeseriesSingleProject(dataObject);	
	};
	
	//-------------------------
	//Tile settings
	var tile = $('.tile');
	tile.html('');
	tile.css('background-color', '#e5e5e5');
	tile.css('color', '#222;');
	tile.css("height", 150 * ZOOM);
	tile.css("width", 150 * ZOOM);
	tile.css("fontSize", 55 * ZOOM);
	tile.css("padding", 2 * ZOOM);
	tile.css("border-radius", BORDER_RADIUS * ZOOM);
	
};

/**************************************************************************************
 * Get data and draw box view
 * @see updateTimeFrame() 
 * @see fetchDataAndDrawTimeseriesSingleProject()
 *************************************************************************************/
function drawBoxView() {
	
	//-------------------------
	//Box settings
	var datepicker = $('.datepickerField');
	$('#toggleOptions').css('display', '');
	$('#toggleTextSize').css('display', '');
	datepicker.val('');
	RETRY_COUNTER = 0;
	$(".hideable").css("display", "none");
	$("#zoomDiv").css("display", "");
	$("#filterDiv").css("display", "");
	CURRENT_TIME_FRAME = DASHBOARD_TIME_FRAME_STRING;
	CURRENT_MEASURE_DATA = 'avg';
	updateTimeFrame(DASHBOARD_TIME_FRAME_STRING);

	var loopable = SORTED_PROJECT_IDS.length;
	
	//-------------------------
	//Get data for every project
	for(var i = 0; i< loopable; i++) {
		
		var dataObject = {
		timeFrame: DASHBOARD_TIME_FRAME_STRING,
		projectId: SORTED_PROJECT_IDS[i],
		view: 'box',
		mergeSpan: 60,
		measureName: "Overall Health"
		};
		RESULTS_DIV.append('<div class="tile bgtile filterable" id=' + dataObject.projectId + '></div>');
		fetchDataAndDrawTimeseriesSingleProject(dataObject);	
	};
	
	//-------------------------
	//Tile settings
	var tile = $('.tile');
	tile.html('');
	tile.css('background-color', '#e5e5e5');
	tile.css("height", 150 * ZOOM);
	tile.css("width", 150 * ZOOM);
	tile.css("fontSize", 11 * ZOOM);
	tile.css("padding", 2 * ZOOM);
	tile.css("border", "5px solid #e5e5e5");
	tile.css("border-radius", BORDER_RADIUS * ZOOM);
};

/**************************************************************************************
 * Get data and draw health history view
 * @see updateTimeFrame() 
 * @see fetchDataAndDrawTimeseriesSingleProject()
 *************************************************************************************/
function drawHealthHistoryView() {
	
	//-------------------------
	//Health History settings
	var datepicker = $('.datepickerField');
	var table = $('<table id="historyTable"><tr><th><p>Project</p></br></th><th style="padding-right: 20px;" id="historyLegend"><p>Health History (last ' + DASHBOARD_TIME_FRAME + ' hours)</p></th><th><p>Current Health</p></br></th></tr></table>');
	
	$('#toggleOptions').css('display', '');
	$('#toggleTextSize').css('display', '');

	datepicker.val('');
	RETRY_COUNTER = 0;
	$(".hideable").css("display", "none");
	$("#zoomDiv").css("display", "none");
	$("#filterDiv").css("display", "");
	CURRENT_TIME_FRAME = DASHBOARD_TIME_FRAME_STRING;
	CURRENT_MEASURE_DATA = 'avg';
	updateTimeFrame(DASHBOARD_TIME_FRAME_STRING);

	var loopable = SORTED_PROJECT_IDS.length;
	
	//-------------------------
	//Get data or every project
	for(var i = 0; i< loopable; i++) {
		
		var dataObject = {
		timeFrame: DASHBOARD_TIME_FRAME_STRING,
		projectId: SORTED_PROJECT_IDS[i],
		view: 'history',
		mergeSpan: 60,
		measureName: "Overall Health"
		};
		table.append('<tr onclick="toggleModal(this)" style="cursor: pointer;" id="' + dataObject.projectId + '" class="filterable historyText"><td style="width: 23%; text-align: right;">' + PROJECT_LIST[SORTED_PROJECT_IDS[i]].fullName + '</td><td id="history" style="padding-right: 20px;"></td><td id="health"></td></tr>');
		fetchDataAndDrawTimeseriesSingleProject(dataObject);	
	};
	
	RESULTS_DIV.append(table);
	
	//-------------------------
	//Table settings
	var headerString = '<div style="display: flex; flex-wrap: nowrap; flex-direction: row-reverse; width: 100%;">';
	
	for(var j = 0; j < DASHBOARD_TIME_FRAME; j++) {
		headerString += '<div style="height: 12px; width: 30px; flex: auto; margin: 3px; text-align: center;"><p>-' + j + '</p></div>';
	};
	
	headerString += '</div>'
	$('#historyLegend').append(headerString);
	
	if(TEXT_SIZE == 'big') {
		$("#historyTable tr td").css("font-weight", "bold");
		$("#historyTable tr td").css("font-size", "18px");
	}
};

/**************************************************************************************
 * Get data and draw status view
 * @see updateTimeFrame() 
 * @see fetchDataAndDrawTimeseriesSingleProject()
 *************************************************************************************/
function drawStatusView() {
	
	//-------------------------
	//Status settings
	var datepicker = $('.datepickerField');
	$('#toggleOptions').css('display', '');
	$('#toggleTextSize').css('display', 'none');
	datepicker.val('');
	RETRY_COUNTER = 0;
	$(".hideable").css("display", "none");
	$("#zoomDiv").css("display", "");
	$("#filterDiv").css("display", "");
	CURRENT_TIME_FRAME = DASHBOARD_TIME_FRAME_STRING;
	CURRENT_MEASURE_DATA = 'avg';
	updateTimeFrame(DASHBOARD_TIME_FRAME_STRING);

	var loopable = SORTED_PROJECT_IDS.length;
	
	//-------------------------
	//Get data for every project
	for(var i = 0; i< loopable; i++) {
		
		var dataObject = {
		timeFrame: DASHBOARD_TIME_FRAME_STRING,
		projectId: SORTED_PROJECT_IDS[i],
		view: 'status',
		mergeSpan: 60,
		measureName: "Overall Health"
		};
		RESULTS_DIV.append('<div class="tile bgtile filterable" id=' + dataObject.projectId + '></div>');
		fetchDataAndDrawTimeseriesSingleProject(dataObject);	
	};
	
	//-------------------------
	//Tile settings
	var tile = $('.tile');
	tile.html('');
	tile.css('background-color', '#e5e5e5');
	tile.css("height", 150 * ZOOM);
	tile.css("width", 150 * ZOOM);
	tile.css("fontSize", 10 * ZOOM);
	tile.css("padding", 2 * ZOOM);
	tile.css("border", "5px solid #e5e5e5");
	tile.css("border-radius", BORDER_RADIUS * ZOOM);
};

/**************************************************************************************
 * Get data and draw graph view
 * @see setMergeSpanOptions()
 * @see updateTimeFrame() 
 * @see fetchDataAndDrawTimeseriesSingleProject()
 *************************************************************************************/
function drawGraphView() {
	
	//-------------------------
	//Graph settings
	$('#toggleOptions').css('display', 'none');
	$('#toggleTextSize').css('display', 'none');
	$(".hideable").css("display", "block");
	$("#zoomDiv").css("display", "none");
	$("#filterDiv").css("display", "none");
	if(CURRENT_PROJECT != 'start') {
		setMergeSpanOptions();
		if(retrieveLocalValue('CURRENT_TIME_FRAME') == '') {
			CURRENT_TIME_FRAME = 'oneyear';
			storeLocalValue('CURRENT_TIME_FRAME', CURRENT_TIME_FRAME);
		}else{
			
			if($('#startDate').val() == '') {
				CURRENT_TIME_FRAME = retrieveLocalValue('CURRENT_TIME_FRAME');
			}
		}
		updateTimeFrame(CURRENT_TIME_FRAME);
		
		//-------------------------
		//Get data for project
		var dataObject = {
			timeFrame: CURRENT_TIME_FRAME,
			projectId: CURRENT_PROJECT,
			view: 'graph',
			mergeSpan: CURRENT_MERGE_SPAN,
			measureName: CURRENT_MEASURE
			};
		if(CURRENT_TIME_FRAME == 'oneyear' && (CURRENT_MERGE_SPAN == "60" || CURRENT_MERGE_SPAN == "15")) {
			RESULTS_DIV.append('<p>Please select valid merge span / time frame combination.</p>');
		}else{
			fetchDataAndDrawTimeseriesSingleProject(dataObject);
		}
	}else{
		RESULTS_DIV.append('<p>Please select a project.</p>');
	}
};

/**************************************************************************************
 * Add a custom tag to a project
 * @param selector button with id of project for which to set a custom tag
 * @see storeLocalValue()
 * @see draw()
 *************************************************************************************/
function addTag(selector) {
	
	var projectId = CURRENT_MODAL_PROJECT;
	var input = $(selector);
	$('#warningText').remove();
	if(PROJECT_LIST[projectId].Tag == '') {
		
		var Tag = input.val();
		
		if(Tag != '' && Tag.length < 9) {
			PROJECT_LIST[projectId].Tag = Tag;
			$('#descriptionTable').append('<tr><td>Tag: </td><td>' + Tag + '</td></tr>');
			try{
			PROJECT_LIST[projectId].fullDescription.Tag = Tag;
			}catch(err){
			
			}
			storeLocalValue(projectId, Tag);
			draw();
			input.val('');
		}else{
			$('#rightGraph').append('<p id="warningText" style="font-size: 12px; font-weight: normal; text-align: left;">Invalid input. Tags must be between 1 and 8 characters long.</p>');
			input.val('');
		}
	}else{
		$('#rightGraph').append('<p id="warningText" style="font-size: 12px; font-weight: normal; text-align: left;">Only one custom tag per project.</p>');
		input.val('');
	}
	
};

/**************************************************************************************
 * Remove a custom tag from a project
 * @see storeLocalValue()
 * @see draw()
 *************************************************************************************/
function removeTag(selector) {
	
	var projectId = $(selector).val();
	
	$('#warningText').remove();
	
	if(PROJECT_LIST[projectId].Tag != '') {
		
		PROJECT_LIST[projectId].Tag = '';
		$('#descriptionTable tr:last').remove();
		try{
			PROJECT_LIST[projectId].fullDescription.Tag = '';
			storeLocalValue(projectId, '');
		}catch(err){
			
		}
		storeLocalValue(projectId, '');
		draw();
	}
};

/**************************************************************************************
 * Set visibility of option panel options and set default
 *************************************************************************************/
function toggleOptions() {
	
	ENV_BOX = JSON.parse(ENV_BOX);
	TID_BOX = JSON.parse(TID_BOX);
	CUSTOM_TAG_BOX = JSON.parse(CUSTOM_TAG_BOX);
	
	if(CURRENT_VIEW != 'dashboard' && CURRENT_VIEW != 'box') {
		
		$('#checkBoxPanel').css('visibility', 'hidden');
	}else {
		
		$('#checkBoxPanel').css('visibility', 'visible');
		$('#envBox').prop('checked', ENV_BOX);
		$('#tidBox').prop('checked', TID_BOX);
		$('#customTagBox').prop('checked', CUSTOM_TAG_BOX);
	}
	
	if(CURRENT_VIEW == 'dashboard') {
		$('#variableText').text(' Project Name');
	} else {
		$('#variableText').text(' TID');
	}
	
	if(DASHBOARD_TIME_FRAME_STRING == 'oneday') {
		$('#oneDayRadio').prop('checked', true);
	} else {
		$('#halfDayRadio').prop('checked', true);
	}
};

/**************************************************************************************
 * Logic for checkboxes to hide custom tags, tid and environment in dashboard & box view
 *************************************************************************************/
function handleCheckBoxClick() {	
	
	var envBox = $('#envBox');
	var tidBox = $('#tidBox');
	var customTagBox = $('#customTagBox');
	var envDiv = $('.envDiv');
	var tidDiv = $('.tidDiv');
	var customTagDiv = $('.customTagDiv');
	
	if(envBox.prop('checked')) {
		envDiv.css('display', '');
		ENV_BOX = true;
		storeLocalValue('ENV_BOX', true);
	}else {
		envDiv.css('display', 'none');
		ENV_BOX = false;
		storeLocalValue('ENV_BOX', false);
	};
	
	if(tidBox.prop('checked')) {
		tidDiv.css('display', '');
		TID_BOX = true;
		storeLocalValue('TID_BOX', true);
	}else {
		tidDiv.css('display', 'none');
		TID_BOX = false;
		storeLocalValue('TID_BOX', false);
	};
	
	if(customTagBox.prop('checked')) {
		customTagDiv.css('display', '');
		CUSTOM_TAG_BOX = true;
		storeLocalValue('CUSTOM_TAG_BOX', true);
	}else {
		customTagDiv.css('display', 'none');
		CUSTOM_TAG_BOX = false;
		storeLocalValue('CUSTOM_TAG_BOX', false);
	}
};

/**************************************************************************************
 * Change timeFrame in dashboard and status view to the last 12 or 24 hours
 * @param selector selected radio button
 * @see draw()
 *************************************************************************************/
function handleRadioButtonClick(selector) {
	
	var radioButton = $(selector);
	
	if(radioButton.val() == 'halfday') {
		radioButton.attr('checked', true);
		$('#oneDayRadio').removeAttr('checked');
		DASHBOARD_TIME_FRAME = 12;
		storeLocalValue('DASHBOARD_TIME_FRAME', 12);
		DASHBOARD_TIME_FRAME_STRING = 'halfday';
		storeLocalValue('DASHBOARD_TIME_FRAME_STRING', 'halfday');
		draw();
	}else{
		radioButton.attr('checked', true);
		$('#halfDayRadio').removeAttr('checked');
		DASHBOARD_TIME_FRAME = 24;
		storeLocalValue('DASHBOARD_TIME_FRAME', 24);
		DASHBOARD_TIME_FRAME_STRING = 'oneday';
		storeLocalValue('DASHBOARD_TIME_FRAME_STRING', 'oneday');
		draw();
	}
};
/**************************************************************************************
 * Set custom timeframe from a jquery-ui datepicker component
 * @param selector date string
 * @see setMergeSpanOptions()
 * @see fetchDataAndDrawTimeseriesSingleProject()
 *************************************************************************************/
function getDate(selector) {
	
	var now = new Date().getTime();
	var input = $(selector);	
	
	RESULTS_DIV.html('');
	
	if(input.attr('id') == 'startDate') {
		START_DATE = new Date(input.val());
	}else if(input.attr('id') == 'endDate'){
		END_DATE = new Date(input.val());
	}
	CURRENT_TIME_FRAME = 'custom';
	
	if($('#startDate').val() != '' && $('#endDate').val() != '') {
		
		var dataObject = {
		timeFrame: CURRENT_TIME_FRAME,
		projectId: CURRENT_PROJECT,
		view: CURRENT_VIEW,
		mergeSpan: CURRENT_MERGE_SPAN,
		measureName: CURRENT_MEASURE
		};
		
		var timeFrame = END_DATE.getTime() - START_DATE.getTime();
		var threeMonthsMs = 1000 * 60 * 60 * 24 * 30 * 3;
		var twoMonthsMs = 1000 * 60 * 60 * 24 * 30 * 2;
		
		setMergeSpanOptions();
		
		if(START_DATE.getTime() >= END_DATE.getTime()) {
			RESULTS_DIV.append('<p>Start date must be before end date.</p></br>');
		}else if(END_DATE.getTime() > now || START_DATE.getTime() > now) {
			RESULTS_DIV.append('<p>No future dates allowed.</p></br>');
		}else if((timeFrame > threeMonthsMs && (CURRENT_MERGE_SPAN == "60" || CURRENT_MERGE_SPAN == "15")) || (timeFrame > twoMonthsMs && CURRENT_MERGE_SPAN == '15')) {
			RESULTS_DIV.append('<p>Please select valid merge span / time frame combination.</p>');
		}else{
			fetchDataAndDrawTimeseriesSingleProject(dataObject);
		}
	}
};

/**************************************************************************************
 * Color drawn tiles in dashboard view with color according to current health
 * Current health = overall health from the last hour with merge span 60
 * @param projectId used to get current health and target correct tile
 * @see roundFloat
 * @see getColor()
 * @see blink()
 *************************************************************************************/
function drawTile(projectId) {
	
	//-------------------------
	//Prepare data
	var currentHealth = 0;
	var htmlString = '';
	var id = projectId;
	var count = PROJECT_LIST[projectId].timeseries['onehour'].length;			
	var currentTile = $(".tile#" + id);
	
	var name = PROJECT_LIST[projectId].name; 
	if (typeof(PROJECT_LIST[projectId].name) == 'undefined') {
		name = PROJECT_LIST[projectId].fullName;
	}
	
	var tid = PROJECT_LIST[projectId].tid;
	if (typeof(PROJECT_LIST[projectId].tid) == 'undefined') {
		var tid = '';
	}

	var loopable = PROJECT_LIST[projectId].timeseries['onehour'];
	
	for(var i = 0; i < loopable.length; i++) {
		currentHealth += parseFloat(loopable[i].value);
	};
	
	currentHealth = roundFloat(currentHealth/count);
	
	//-------------------------
	// Prepare HTML string and add to DOM
	if(typeof(PROJECT_LIST[projectId].environment) != 'undefined') {
		htmlString += '<div class="envDiv" style="width: 100%; height: 10%; text-align: left; font-size: ' + 12 * ZOOM + 'px; display: none">' + PROJECT_LIST[projectId].environment + '</div>';
	};
	
	htmlString += '<div style="width: 100%; height: 60%; text-align: center; padding-top: ' + 15 * ZOOM + 'px;">' + tid + '</div>';
	
	if (ZOOM != "0.33") {
		htmlString += '<div class="tidDiv" style="width: 50%; max-height: 40%; text-align: left; position: absolute; bottom: ' + 3 * ZOOM + 'px; font-size: ' + 9 * ZOOM + 'px; display: none">' + name + '</div>';
	}

	if(typeof(PROJECT_LIST[projectId].Tag) != '') {
		htmlString += '<div class="customTagDiv" style="width: 50%; height: 10%; text-align: right; position: absolute; bottom: ' + 3 * ZOOM + 'px; font-size: ' + 12 * ZOOM + 'px; right:' + 4 * ZOOM +'px; display: none">' + PROJECT_LIST[projectId].Tag + '</div>';
	};
	currentTile.html(htmlString);
	
	if(!isNaN(currentHealth)) {
		currentTile.attr("title", "Current Health: " + currentHealth);
	}else{
		currentTile.attr("title", "Offline");
	}
	
	var color = getColor('Overall Health', currentHealth);
	currentTile.css('background-color', color);
	currentTile.attr('onclick', 'toggleModal(this)');
	
	var oldValue = retrieveLocalValue("p" + projectId);
	if (oldValue != "") {
		if(oldValue >= 25.00 && currentHealth < 25.00) {

			var blinkInterval = setInterval("blink('#" + projectId + "')", 1500);
			BLINK_INTERVALS.push(blinkInterval);
		}
	}
	
	storeLocalValue("p" + projectId, currentHealth);
};

/**************************************************************************************
 * When used in combination with setInterval(), this function can be used to make 
 * projects blink red. This is used to draw attention to projects which have turned 
 * red with the last refresh.
 * @param selector string which identifies a tile
 *************************************************************************************/
function blink(selector){
	
	$(selector).fadeOut('slow');
	$(selector).fadeIn('slow');
}

/**************************************************************************************
 * When used in combination with setInterval(), this function can be used to make tiles
 * blink red. This is used to draw attention to tiles which have turned red with the
 * last refresh.
 * @param selector string which identifies a tile
 *************************************************************************************/
function blinkBorder(selector, color){
	
	setTimeout("$(selector).css('border', '5px solid white')", 600);
	setTimeout("$(selector).css('border', '5px solid " + color + "')", 600);
}


/**************************************************************************************
 * Returns the appropriate color for a given value. Switch with fallthrough used
 * @param measureName string used to get correct boundaries for colors
 * @param value actual value to set color by
 *************************************************************************************/
function getColor(measureName, value) {
	
	//-------------------------
	//Dashboard colors
	var darkGreen = "rgb(130, 185, 105)";
	var lightGreen = "rgb(148, 186, 100)";
	var yellow = "rgb(231, 196, 80)";
	var orange = "rgb(255, 179, 71)";
	var darkOrange = "rgb(255, 116, 64)";
	var red = "rgb(255, 57, 57)";
	var grey = 'rgb(229, 229, 229)';
	
	switch(measureName) {
		
		case 'Transaction Response Time':
			
			if (value == -1) {
				
				return grey;
				
			} else if (5 >= value) {
				
				return darkGreen;
				
			} else if (8 >= value) {
				
				return yellow;
				
			} else if (value > 8) {
				
				return red;
				
			}
			break;
			
		case 'Page Round Trip Time':
		
		case 'Page HTML Download Time':
		
		case 'Page Server Busy Time':
		
		case 'Custom Timers':
			
			if (value == -1) {
				
				return grey;
				
			} else if (2 >= value) {
				
				return darkGreen;
				
			} else if (3 >= value) {
				
				return lightGreen;
				
			} else if (5 >= value) {
				
				return yellow;
				
			} else if (value > 5) {
				
				return red;
				
			}
			break;
		
		default:
		
			if (value == 100) {
		
				return darkGreen;
				
			} else if (value == 'NaN' || value == -1) {
				
				return grey;
				
			} else if (75 <= value) {
				
				return lightGreen;
				
			} else if (50 < value) {
				
				return yellow;
				
			} else if (value == 50.00) {
				
				return orange;
				
			} else if (25 < value) {
				
				return darkOrange;
				
			} else if (value <= 25) {
				
				return red;
				
			}
			break;
	};
};
/**************************************************************************************
 * Fill tile with donut chart with data from last 12/24 hours
 * @param projectId Id of project to draw tile for
 * @see drawChart()
 *************************************************************************************/
function drawStatusTile(projectId) {
	
	var currentTile = $(".tile#" + projectId);
	var results = [];
	
	drawChart('donut', projectId, results);
	currentTile.attr('onclick', 'toggleModal(this)');
	currentTile.css('background-color', '');
	
};

/**************************************************************************************
 * Fill tile with boxes with data from last 12/24 hours
 * @param projectId Id of project to draw tile for
 * @see fillMissingChartValues()
 * @see getColor()
 *************************************************************************************/
function drawBoxTile(projectId) {
	
	//-------------------------
	// Prepare data
	var name = PROJECT_LIST[projectId].name;
	if (typeof(PROJECT_LIST[projectId].name) == 'undefined') {
		name = PROJECT_LIST[projectId].fullName;
	}
	
	var currentTile = $(".tile#" + projectId);
	var htmlString = '';
	var results = fillMissingChartValues(projectId);
	var boxColors = [];
	
	currentTile.attr('onclick', 'toggleModal(this)');
	currentTile.css('background-color', '');
	
	//-------------------------
	// Build HTML of tile
	if(typeof(PROJECT_LIST[projectId].environment) != 'undefined') {
		htmlString += '<div class="envDiv" style="width: 100%; height: 10%; text-align: left; font-size: ' + 9 * ZOOM + 'px; display: none"><p>' + PROJECT_LIST[projectId].environment + '</p></div>';
	};
	
	htmlString += '<div style="width: 100%;"><p style="margin: 2px;">' + name +'</p></div><div style="display: flex; flex-wrap: wrap; margin-left: ' + 4 * ZOOM + 'px;">';

	for(var i = 0; i< results.length; i++) {
		
		var color = getColor('Overall Health', results[i].value);
		boxColors.push(color);
		htmlString += '<div class="smallBoxes" style="height: ' + 15 * ZOOM + 'px; width: ' + 15 * ZOOM + 'px; float: left; margin: ' + ZOOM + 'px; background-color: ' + color + '"></div>'
		
	};
	
	htmlString += '<div>'
	
	if(typeof(PROJECT_LIST[projectId].tid) != 'undefined') {
		htmlString += '<div class="tidDiv" style="width: 50%; height: 10%; text-align: left; position: absolute; bottom: ' + 7 * ZOOM + 'px; font-size: ' + 16 * ZOOM + 'px; left: ' + 4 * ZOOM + 'px; display: none"><p>' + PROJECT_LIST[projectId].tid + '</p></div>';
	};
	if(typeof(PROJECT_LIST[projectId].Tag) != '') {
		htmlString += '<div class="customTagDiv" style="width: 50%; height: 10%; text-align: right; position: absolute; bottom: ' + 7 * ZOOM + 'px; font-size: ' + 16 * ZOOM + 'px; right:' + 4 * ZOOM +'px; display: none"><p>' + PROJECT_LIST[projectId].Tag + '</p></div>';
	};

	//-------------------------
	// Set tile attributes
	if(results[results.length -1].value != -1) {
		currentTile.attr("title", "Current Health: " + results[results.length -1].value);
	}else{
		currentTile.attr("title", "Offline");
	}
	
	if(results[results.length -1].value < 75 && results[results.length -1].value > -1) {
		currentTile.css('border', '5px solid ' + boxColors[boxColors.length -1]);
	}
	
	var oldValue = retrieveLocalValue("p" + projectId);
	if (oldValue != "") {

		if(oldValue >= 25.00 && results[results.length -1].value < 25.00) {

			var blinkInterval = setInterval("blink('#" + projectId + "')", 1500);
			BLINK_INTERVALS.push(blinkInterval);
		}
	}
	
	storeLocalValue("p" + projectId, results[results.length -1].value);
	
	//-------------------------
	// Add HTML to DOM
	currentTile.html(htmlString);
	
	if(TEXT_SIZE == 'big') {
		currentTile.css('font-size', 15 * ZOOM);
		$('.tidDiv').css('font-size', 9 * ZOOM);
		$('.tidDiv').css('bottom', 0);
		$('.customTagDiv').css('font-size', 9 * ZOOM);
		$('.customTagDiv').css('bottom', ZOOM);
		$('.smallBoxes').css('height', 12 * ZOOM);
		$('.smallBoxes').css('width', 12 * ZOOM);
	}
};

/**************************************************************************************
 * For charts from the last 12 or 24 hours in dashboard/status views: get missing 
 * timestamps. Remove superfluous time stamps.
 * @param projectId current project
 * @see FormattedDate()
 * @see search()
 * @see dynamicSort()
 * @return results sorted array of objects with complete set of timestamps
 *************************************************************************************/
function fillMissingChartValues(projectId) {
	
	var timestamps = [];
	var hourDiffInMs = 1000 * 60 * 60;
	var endTime = CURRENT_TIME;
	var formatDate;
	var loopable = PROJECT_LIST[projectId].timeseries[DASHBOARD_TIME_FRAME_STRING]

	
	for (var i = 0; i < DASHBOARD_TIME_FRAME; i++) {
		
		formatDate = new FormattedDate(new Date(endTime - (i * hourDiffInMs)));
		timestamps.push(formatDate.FullDate());
	};

	
	for (var j = 0; j < timestamps.length; j++) {
		
		var timestamp = timestamps[j];
		
		if (!(search(timestamp, loopable) ) ) {
			
			var project = {};
			project.time = timestamp;
			project.value = -1;
			loopable.push(project);
		}
	};
	
	loopable.sort(dynamicSort('time'));
	
	//-------------------------
	// Remove superfluous timestamps
	if(loopable.length == 25 || loopable.length == 13) {
		loopable.shift();
	}
	
	return loopable;
};

/**************************************************************************************
 * Draw a table with health of the last 12/24 hours (like SPM Health History)
 * @param projectId Id of project in row
 * @see fillMissingChartValues()
 * @see getColor()
 *************************************************************************************/ 
function drawHistoryTable(projectId) {
	
	var historyField = $('#historyTable #' + projectId + ' #history');
	var healthField = $('#historyTable #' + projectId + ' #health');
	var historyString = '<div style="display: flex; flex-wrap: nowrap; align-items: center; width: 100%;">';
	var results = fillMissingChartValues(projectId);
	

	for(var i = 0; i< results.length; i++) {
		
		var color = getColor('Overall Health', results[i].value);
		
		if(results[i].value == -1) {
			results[i].value = 'Offline';
		}
		
		historyString += '<div title="' + results[i].value + '" style="min-height: 15px; max-height: 30px; width: 30px; flex: auto; margin: 3px; background-color: ' + color + '"></div>';
	};
	
	historyString += '</div>';
	
	historyField.append(historyString);
	healthField.append('<div style="display: flex; flex-wrap: nowrap; align-items: center; width: 100%;"><div style="height: 14px; width: 14px; margin: 3px; background-color: ' + color + '; border: 1px solid #e5e5e5"></div>' + results[results.length - 1].value + '</div>');
	
	var oldValue = retrieveLocalValue("p" + projectId);
	if (oldValue != "") {
		if(oldValue >= 25.00 &&  results[results.length - 1].value < 25.00) {

			blinkInterval = setInterval("blink('#" + projectId + "')", 1500);
			BLINK_INTERVALS.push(blinkInterval);
		}
	}
	
	storeLocalValue("p" + projectId,  results[results.length - 1].value);
};

/**************************************************************************************
 * Call functions to draw different charts
 * @param graphType bar/scatter/pie
 * @param targetId div in which to draw the chart
 * @param vals data values to chart
 * @see drawChartBarOrLine()
 * @see drawChartDonutOrPie()
 * @see drawChartArea()
 *************************************************************************************/
function drawChart(graphType, targetId, vals) {
	
	switch(graphType) {
		
		case 'bar':
		
			drawChartBarOrLine(graphType, targetId, vals);
			break;
			
		case 'scatter':
			
			drawChartBarOrLine(graphType, targetId, vals);
			break;
			
		case 'pie':
		
			drawChartDonutOrPie(graphType, targetId, vals);
			break;
			
		case 'donut':
		
			drawChartDonutOrPie(graphType, targetId, vals);
			break;
		
		case 'area':
		
			drawChartArea(targetId, vals);
			break;
	};
};

/**************************************************************************************
 * Draw bar or line chart
 * @param graphType draw either bar or line chart
 * @param targetId id of div in which to draw chart
 * @param vals array with data to be charted
 * @see fillMissingChartValues()
 * @see dynamicSort()
 *************************************************************************************/
function drawChartBarOrLine(graphType, targetId, vals) {
	
	var targetDiv = $('#' + targetId);
	var div = $('<div id="graph"></div>');
	var chartSelector = $('<select id="chartSelector" class="form-control form-control-sm col-md-2" onchange="fireChartTypeChange(this)" title="Choose Chart Type">');
	var chartHeight = (targetDiv.height()) * 0.9;
	var chartWidth = (targetDiv.width());
	
	targetDiv.html('');
	targetDiv.append(div);
	//-------------------------
	//Set chart selector
	chartSelector.html('<option value="area">Area Chart</option><option value="bar">Bar Chart</option><option value="donut">Donut Chart</option><option value="scatter">Line Chart</option><option value="pie">Pie Chart</option>');
	chartSelector.insertBefore(div);
	$('#' + targetId + ' #chartSelector option[value="' + graphType + '"]').attr('selected', 'selected');
	div.height(chartHeight);
	div.width(chartWidth);
	
	//-------------------------
	// Get data for chart
	if (!$.isEmptyObject(vals)) {
		
		if(CURRENT_VIEW == 'dashboard' || CURRENT_VIEW == 'status' || CURRENT_VIEW == 'box' || CURRENT_VIEW == 'history') {
			vals = fillMissingChartValues(CURRENT_MODAL_PROJECT);
		}
		
		var TESTER = div.get(0);
		
		var xVals = [];
		var yVals = [];
		
		vals.sort(dynamicSort('time'));
		
		for(var index in vals) {
			xVals.push(vals[index].time)
			
			if(vals[index].value != -1){
				
				yVals.push(vals[index].value)
			}else {
				yVals.push(undefined);
			}
		};
		
		//-------------------------
		// draw chart
		Plotly.plot( TESTER, [{
			x: xVals,
			y: yVals, 
			type: graphType
			}], 
			{ 	margin: { t: 0 },
				height: chartHeight,
				width: chartWidth,
				plot_bgcolor: 'transparent',
				paper_bgcolor: 'transparent',
				xaxis: {showgrid: false, linecolor: '#b7b7b7', tickfont: {color: '#b7b7b7'}},
				yaxis: {showgrid: false, showline: true, zeroline: false, linecolor: '#b7b7b7', tickfont: {color: '#b7b7b7'}}
			},
			{displayModeBar: false}
		);	
	} else {
		targetDiv.append("No data available.");
	};
};

/**************************************************************************************
 * Draw donut/pie chart with current data
 * @param graphType draw either donut or pie chart
 * @param targetId id of div in which to draw graph
 * @param vals array with data to be charted
 * @see fillMissingChartValues()
 * @see dynamicSort()
 * @see getColor()
 *************************************************************************************/
 function drawChartDonutOrPie(graphType, targetId, vals) {
	
	var targetDiv = $('#' + targetId);
	var hoverInfo = '';
	var div = $('<div class="pie"></div>');
	var chartHeight = (targetDiv.height() * 0.75);
	var chartWidth = (targetDiv.width());
	var pieHole = 0.7;
	var textArray = [];
	var labelArray = [];
	var colorArray = [];
	var equalSlices = [];
	var htmlString = '';
	
	targetDiv.html('');
	
	//-------------------------
	// Prepare chart selector if necessary
	if(targetDiv.attr('class') != 'tile bgtile filterable' && targetDiv.attr('id') != 'rightGraph') {
		
		var chartSelector = $('<select id="chartSelector" class="form-control form-control-sm col-md-2" onchange="fireChartTypeChange(this)" title="Choose Chart Type">');
		chartSelector.html('<option value="area">Area Chart</option><option value="bar">Bar Chart</option><option value="donut">Donut Chart</option><option value="scatter">Line Chart</option><option value="pie">Pie Chart</option>');
		targetDiv.append(chartSelector);
		$('#' + targetId + ' #chartSelector option[value="' + graphType + '"]').attr('selected', 'selected');
	}
	
	//-------------------------
	// Set chart layout and get data
	if(targetDiv.attr('id') == 'result') {
		div.css({'position': 'relative', 'bottom': ''});
		hoverInfo = 'label+text';
	} else {
		
		if(targetId == 'rightGraph' || targetId == 'leftGraph') {
			
			if(targetId == 'rightGraph') {
				htmlString += '<div class="fa fa-info-circle" id="toggleHelp" aria-hidden="true" style="cursor: pointer; data-toggle="tooltip" data-placement="bottom" title="The chart shows the data of the last ' + DASHBOARD_TIME_FRAME + ' hours merged into hourly timestamps, going back in time counter clockwise. Hover over the individual slices to see the timestamp and the value for that timestamp."></div>';
			}
			htmlString += '<p>' + PROJECT_LIST[CURRENT_MODAL_PROJECT].fullName + '</p>';
			vals = fillMissingChartValues(CURRENT_MODAL_PROJECT);
			hoverInfo = 'label+text';
			div.css('bottom', '20px');
		}else{
			
			htmlString += '<p>' + PROJECT_LIST[targetId].fullName + '</p>';
			vals = fillMissingChartValues(targetId);
			hoverInfo = 'none';
		}
	}
	if(CURRENT_VIEW == 'graph') {
		vals.sort(dynamicSort('time'));
	}
	
	if(graphType == 'pie') {
		pieHole = 0;
	}
	
	for (index in vals) {
		
		var color = getColor(DISPLAY_MEASURE_NAME, vals[index].value);
		
		colorArray.push(color);
		labelArray.push(vals[index].time);
		equalSlices.push(1);
		
		if (vals[index].value != -1) {
			
			textArray.push(vals[index].value);
		} else {
			textArray.push(NaN);
		}
		
	};
	
	if(typeof(targetId) == 'number') {
		
		var health = textArray[textArray.length -1];
		
		if(vals[vals.length -1].value < 75 && vals[vals.length -1].value > -1){
			targetDiv.css('border', 'solid 5px ' + colorArray[colorArray.length -1]);
		}
		
		if(!isNaN(health)) {
			targetDiv.attr('title', 'Current Health: ' + textArray[textArray.length -1]);
		}else{
			targetDiv.attr('title', 'Offline');
		}
		
		var oldValue = retrieveLocalValue("p" + projectId);
		if (oldValue != "") {
			if(oldValue >= 25.00 && textArray[textArray.length -1] < 25.00) {
	
				blinkInterval = setInterval("blink('#" + targetId + "')", 1500);
				BLINK_INTERVALS.push(blinkInterval);
			}
		}
		
		storeLocalValue("p" + targetId, textArray[textArray.length -1]);
			
	}
	
	//-------------------------
	// Set variables for chart and draw
	var layout = {
		height: chartHeight,
		width: chartWidth,
		showlegend: false,
		textinfo: 'outside',
		plot_bgcolor: 'transparent',
		paper_bgcolor: 'transparent',
		margin: {
				l: 0,
				r: 0,
				b: 5,
				t: 5,
				pad: 0
				}
	};
	var data = [{
		values: equalSlices,
		labels: labelArray,
		direction: 'clockwise',
		text: textArray,
		textinfo: 'none',
		hoverinfo: hoverInfo,
		type: 'pie',
		marker: {
			colors: colorArray,
			line: {
				color: 'black',
				width: 0.2
			}
		
		},
		sort: false,
		hole: pieHole,
	
	}];
	
	var PIE = div.get(0);
	Plotly.newPlot(PIE, data, layout, {displayModeBar: false});
	targetDiv.append(htmlString);
	targetDiv.append(div);
	
	$('#toggleHelp').tooltip();
};

/**************************************************************************************
 * Draw area chart
 * @param targetId id of div in which to draw chart
 * @param vals array with data to be charted
 * @see fillMissingChartValues()
 * @see dynamicSort()
 *************************************************************************************/
function drawChartArea(targetId, vals) {
	
	var targetDiv = $('#' + targetId);
	var div = $('<div id="graph"></div>');
	var chartSelector = $('<select id="chartSelector" class="form-control form-control-sm col-md-2" onchange="fireChartTypeChange(this)" title="Choose Chart Type">');
	var chartHeight = (targetDiv.height()) * 0.9;
	var chartWidth = (targetDiv.width());
	
	targetDiv.html('');
	targetDiv.append(div);
	
	//-------------------------
	// Set chart selector
	chartSelector.html('<option value="area">Area Chart</option><option value="bar">Bar Chart</option><option value="donut">Donut Chart</option><option value="scatter">Line Chart</option><option value="pie">Pie Chart</option>');
	chartSelector.insertBefore(div);
	$('#' + targetId + ' #chartSelector option[value="area"]').attr('selected', 'selected');
	
	
	
	//-------------------------
	// Get data for chart
	if (!$.isEmptyObject(vals)) {
		
		if(CURRENT_VIEW == 'dashboard' || CURRENT_VIEW == 'status' || CURRENT_VIEW == 'box' || CURRENT_VIEW == 'history') {
			vals = fillMissingChartValues(CURRENT_MODAL_PROJECT);
		}
		
		var TESTER = div.get(0);
		
		var xVals = [];
		var yVals = [];
		
		vals.sort(dynamicSort('time'));
		
		for(index in vals) {
			
			xVals.push(vals[index].time)
			
			if(vals[index].value != -1){
				
				yVals.push(vals[index].value)
			}else {
				yVals.push(0);
			}
		};
		
		//-------------------------
		// Draw chart
	Plotly.plot( TESTER, [{
			x: xVals,
			y: yVals,
			fill: 'tonexty',
			type: 'scatter',
			mode: 'none'
			}], 
			{ 	margin: { t: 0 },
				height: chartHeight,
				width: chartWidth,
				plot_bgcolor: 'transparent',
				paper_bgcolor: 'transparent',
				xaxis: {showgrid: false, linecolor: '#b7b7b7', tickfont: {color: '#b7b7b7'}},
				yaxis: {showgrid: false, showline: true, linecolor: '#b7b7b7', tickfont: {color: '#b7b7b7'}}
			},
			{displayModeBar: false}
		);	
	} else {
		targetDiv.append("No data available.");
	};
};

/**************************************************************************************
 * Toggle a modal to display further measures, stop refreshing the dashboard
 * @param selector gets passed the html element (tile) which was clicked
 * @see updateModalBody()
 *************************************************************************************/
function toggleModal(selector) {
	
	for(var i = 0; i < BLINK_INTERVALS.length; i++) {
		clearInterval(BLINK_INTERVALS[i]);
	}
	BLINK_INTERVALS = [];
	CURRENT_MODAL_PROJECT = $(selector).attr('id');
	var projectName = PROJECT_LIST[CURRENT_MODAL_PROJECT].fullName;
	
	$('#tabs a:first').tab('show'); 
	$('.modal-title').text(projectName + ' - Last ' + DASHBOARD_TIME_FRAME + ' hours');
	$('#modal').modal({'show': true, 'backdrop': false});
	
	if(PROJECT_LIST[CURRENT_MODAL_PROJECT].hasConfigRights) {
		$('#configButton').attr('onclick', "window.location.href='/silk/DEF/Monitoring/Configuration?pId=" + CURRENT_MODAL_PROJECT + "'");
	} else {
		$('#configButton').css('display', 'none');
	}
	
	$('#monitorButton').attr('onclick', "window.location.href='/silk/DEF/Monitoring/Monitoring?pId=" + CURRENT_MODAL_PROJECT + "'");
	updateModalBody('Overall Health');
	
};

/**************************************************************************************
 * Restarts the refresh timer after closing a modal
 * @see refreshTimer()
 *************************************************************************************/
function modalRestartRefresh() {
	
	refreshTimer();
};

/**************************************************************************************
 * Update modal body according to current tab
 * @param tab string identifying the current tab
 * @see drawProjectDescription()
 * @see updateTimeFrame()
 * @see fetchDataAndDrawTimeseriesSingleProject()
 *************************************************************************************/
function updateModalBody(tab) {
	
	PROJECT_COUNTER = 1;
	
	if(tab == 'description') {
		drawProjectDescription();
		selectStyleMode(document.getElementById('styleModeDropdown'));
	}else{
		updateTimeFrame(DASHBOARD_TIME_FRAME_STRING);
		var dataObject = {
			timeFrame: DASHBOARD_TIME_FRAME_STRING,
			projectId: CURRENT_MODAL_PROJECT,
			view: 'modal',
			mergeSpan: 60,
			measureName: tab
		};
		fetchDataAndDrawTimeseriesSingleProject(dataObject);
	}	
};


/**************************************************************************************
 * Draw a table with information from the project description. Draw options for adding
 * and removing tags
 *************************************************************************************/
function drawProjectDescription() {
	
	var leftGraph = $('#leftGraph');
	var rightGraph = $('#rightGraph');
	var leftTable = $('#leftTable');
	var table = $('<table id="descriptionTable" class="customTable"></table>');
	var rowstring = '';
	var loopable = PROJECT_LIST[CURRENT_MODAL_PROJECT].fullDescription;
	
	leftTable.html('');
	leftGraph.html('');
	rightGraph.html('');
	rightGraph.append('<p style="font-size: 12px; font-weight: normal; text-align: left; margin-bottom: -5px;">You can set one custom tag per project. A custom tag can be at most eight characters long.</p></br>');
	rightGraph.append('<input class="form-control tablefilter" id="tagInput" placeholder="Add Tag..." style="margin-bottom: 2px;"></input>');
	rightGraph.append('<button class="btn btn-sm btn-primary m-2" id="addTagButton" value="'+ CURRENT_MODAL_PROJECT +'" >Add Tag</button>');
	rightGraph.append('<button class="btn btn-sm btn-primary m-2" id="removeTagButton" value="'+ CURRENT_MODAL_PROJECT +'" onclick="removeTag(this)">Remove Tag</button>');
	$('#addTagButton').click(function() {addTag(document.getElementById('tagInput'))});

	for(index in loopable) {
		
		if(loopable[index] != '' && loopable[index] != undefined) {
			rowstring += '<tr><td>' + index + '</td><td class="value">' + loopable[index] + '</td></tr>';
		}
	};
	
	if(rowstring.length == 0 &&  PROJECT_LIST[CURRENT_MODAL_PROJECT].Tag != '') {
			rowstring += '<tr><td>Tag: </td><td class="value">' + PROJECT_LIST[CURRENT_MODAL_PROJECT].Tag + '</td></tr>';
		}
		
	table.append(rowstring);	
	leftTable.append(table);
	
};

/**************************************************************************************
 * Draw content of modal. Default is table, line chart, donut chart; left charts may be changed
 * @param projectId id of current project
 * @see drawModalTable()
 * @see drawChart()
 *************************************************************************************/
function drawModalContent(projectId) {
	
	var leftGraph = $('#leftGraph');
	var rightGraph = $('#rightGraph');
	var leftTable = $('#leftTable');
	
	leftTable.html('');
	leftGraph.html('');
	rightGraph.html('');
	
	if (PROJECT_LIST[projectId].timeseries[DASHBOARD_TIME_FRAME_STRING] != 0) {	
				
				drawModalTable('leftTable');
				drawChart('donut', 'rightGraph', PROJECT_LIST[projectId].timeseries[DASHBOARD_TIME_FRAME_STRING]);
				drawChart(CURRENT_CHART_TYPE, 'leftGraph', PROJECT_LIST[projectId].timeseries[DASHBOARD_TIME_FRAME_STRING]);
				
				
			} else {
				leftTable.append("No data available.");
			};
};

/**************************************************************************************
 * Create table with additional measurements
 * @param targetId id of target div
 * @see calculateAverage()
 * @see calculateMinimum()
 * @see calculateMaximum()
 * @see calculatePercentile()
 * @see calculateStdDeviation()
 *************************************************************************************/
function drawModalTable(targetId) {
	
	var targetDiv = $('#' + targetId);
	var sumArray = [];
	var loopable = PROJECT_LIST[CURRENT_MODAL_PROJECT].timeseries[DASHBOARD_TIME_FRAME_STRING];
	
	targetDiv.html('');
	
	for(var i = 0; i < loopable.length; i++) {
		
		sumArray.push(loopable[i].value);
	};
	
	var avg = roundFloat(calculateAverage(sumArray));
	var min = roundFloat(calculateMinimum(sumArray));
	var max = roundFloat(calculateMaximum(sumArray));
	var median = roundFloat(calculatePercentile(sumArray, 0.5));
	var percent = roundFloat(calculatePercentile(sumArray, 0.9));
	var stdDev = roundFloat(calculateStdDeviation(sumArray));
	
	var rowstring = '<table class="customTable" style="border-bottom: 1px solid #e5e5e5;"><tr><td>Average: </td><td class="value">' + avg + 
	'</td></tr><tr><td>Maximum: </td><td class="value">' + max + 
	'</td></tr><tr><td>Minimum: </td><td class="value">' + min + 
	'</td></tr><tr><td>Median: </td><td class="value">' + median +
	'</td></tr><tr><td>90th Percentile: </td><td class="value">' + percent +
	'</td></tr><tr><td>Standard Deviation: </td><td class="value">' + stdDev +
	'</td></tr></table>';
	
	targetDiv.append(rowstring);
};

/**************************************************************************************
 * Check if value is in array of objects
 * @param searchKey key to be found
 * @param myArray array to search in
 * @return true if the value was found
 *************************************************************************************/
function search(searchKey, myArray){
	
	var loopable = myArray.length;
    
	for (var i=0; i < loopable; i++) {
		
        if (myArray[i].time === searchKey) {
            return true;
        }
    };
};

/**************************************************************************************
 * Round floats to two decimals
 * @param value value to be rounded
 * @return value rounded to two decimals
 *************************************************************************************/
function roundFloat(value){
	
	return  parseFloat(value).toFixed(2);
};

/**************************************************************************************
 * Function to sort array of objects by selected key
 * By using a '-' as a prefix to the string, the sort order can be reversed
 * @param property string of object key by which to sort
 * @return array of objects sorted by selected key
 *************************************************************************************/
function dynamicSort(property) {
	
    var sortOrder = 1;
	
    if(property[0] === "-") {
		
        sortOrder = -1;
        property = property.substr(1);
    };
	
    return function (a,b) {
		var result;
		if((a[property] == undefined && b[property] == undefined) || (a[property] == '' && b[property] == '')) {
			result = 0;
		} else if(a[property] == undefined || a[property] == '') {
			result = 1;
		} else if(b[property] == undefined || b[property] == '') {
			result = -1
		}else {
			result = (a[property].toUpperCase() < b[property].toUpperCase()) ? -1 : (a[property].toUpperCase() > b[property].toUpperCase()) ? 1 : 0;
		}

		return result * sortOrder;
    };
};

/**************************************************************************************
 * Main function; gets session, the SPM user's ID, their projects, draws a view and 
 * starts an interval to regularly update the current view and keep the session alive
 * getSession(), getUserId() and getProjectsForUser() can't be called asynchronously
 * as each requires a return value from the prior function.
 * Sets all selectors to the right values by getting the right cookies.
 * @see retrieveLocalValue()
 * @see getSession()
 * @see getUserId()
 * @see getProjectsForUser()
 * @see getGroupsWithConfigRights()
 * @see draw()
 * @see drawLegend()
 * @see refreshTimer()
 * @see keepSessionAlive()
 *************************************************************************************/
function startTheusinator(){
	
	clearInterval(INTERVAL_ID);
	clearInterval(ALIVE);
	PROJECT_FILTER.val(retrieveLocalValue('searchFilter'));
	PROJECT_FILTER.text(retrieveLocalValue('searchFilter'));
	SORTED_PROJECT_IDS = [];
	PROJECT_LIST = {};
	$.ajaxSetup({async:false});
		getSession();
		getUserId();
		getProjectsForUser();
	$.ajaxSetup({async:true});
	getGroupsWithConfigRights();
	draw();
	drawLegend();
	refreshTimer();
	keepSessionAlive();
	
	$('.checkBoxes').prop('checked', false);
	//-------------------------
	// Set all selectors
	if (retrieveLocalValue('ZOOM') != '') {
		$('#zoomSelector option[value="' + retrieveLocalValue("ZOOM") + '"]').attr('selected', 'selected');
	}
	
	if (retrieveLocalValue('REFRESH_TIME') != '') {
		$('#refreshSelector option[value="' + retrieveLocalValue("REFRESH_TIME") + '"]').attr('selected', 'selected');
	}
	
	if (retrieveLocalValue('CURRENT_VIEW') != '') {
		$('#viewSelector option[value="' + retrieveLocalValue("CURRENT_VIEW") + '"]').attr('selected', 'selected');
	}
	
	if (retrieveLocalValue('CURRENT_PROJECT') != '') {
		$('#projectSelector option[value="' + retrieveLocalValue("CURRENT_PROJECT") + '"]').attr('selected', 'selected');
	}
	
	if (retrieveLocalValue('CURRENT_MEASURE') != '') {
		$('#measureSelector option[value="' + retrieveLocalValue("CURRENT_MEASURE") + '"]').attr('selected', 'selected');
	}
	
	if (retrieveLocalValue('CURRENT_MERGE_SPAN') != '') {
		$('#mergeSpanSelector option[value="' + retrieveLocalValue("CURRENT_MERGE_SPAN") + '"]').attr('selected', 'selected');
	}
	
	if (retrieveLocalValue('CURRENT_MEASURE_DATA') != '') {
		$('#measureDataSelector option[value="' + retrieveLocalValue("CURRENT_MEASURE_DATA") + '"]').attr('selected', 'selected');
	}
	
	if (retrieveLocalValue('SORT_ORDER') != '') {
		$('#sortSelector option[value="' + retrieveLocalValue("SORT_ORDER") + '"]').attr('selected', 'selected');
	}

};

/**************************************************************************************
* Object; Guarantees correct format of timestamps for use in AJAX calls to SPM API
* Converts all dates to UTC
* @param Date Date object to be converted to formatted string
* @return either specific parts of the given date, or whole formatted date 
 *************************************************************************************/
function FormattedDate(Date) {
	
	this.date = Date
	
	this.Year = function() {
		return (this.date.getUTCFullYear()).toString();
	};
	
	this.Month = function() {
		if ((this.date.getUTCMonth() + 1) < 10) {
			return "0" + (this.date.getUTCMonth() + 1);
		} else {
			return (this.date.getUTCMonth() + 1);
		}
	};
	
	this.Day = function() {
		if (this.date.getUTCDate() < 10) {
			return "0" + (this.date.getUTCDate());
		} else {
			return (this.date.getUTCDate());
		}
	};
	
	this.Hours = function() {
		if (this.date.getUTCHours() < 10) {
			return "0" + (this.date.getUTCHours());
		} else {
			return (this.date.getUTCHours());
		}
	};
	
	this.FullDate = function() {
		return this.Year() + "-" + this.Month() + "-" + this.Day() + " " + this.Hours() + ":00:00.000";
	};
};

/**************************************************************************************
* Function to set the refresh interval and update the data on refresh without redrawing
* the entire results div for dashboard, box and status views
* @see updateTimeFrame()
* @see fetchDataAndDrawTimeseriesSingleProject()
* @see draw()
 *************************************************************************************/
function refreshTimer() {
	
	if(REFRESH_TIME != 'stop') {
		INTERVAL_ID = setInterval( function() {	
			
			PROJECT_COUNTER = SORTED_PROJECT_IDS.length;
			for(i = 0; i < BLINK_INTERVALS.length; i++) {
				clearInterval(BLINK_INTERVALS[i]);
			}
			BLINK_INTERVALS = [];
			
			var i;
			var loopable;
			var dataObject;
			var tile;
			switch(CURRENT_VIEW) {
				
				case 'dashboard':
				
					updateTimeFrame('onehour');
					loopable = SORTED_PROJECT_IDS.length;
					
					for(i = 0; i< loopable; i++) {
						
						dataObject = {
						timeFrame: 'onehour',
						projectId: SORTED_PROJECT_IDS[i],
						view: 'dashboard',
						mergeSpan: 60,
						measureName: "Overall Health"
						};
						fetchDataAndDrawTimeseriesSingleProject(dataObject);	
					};
					
					tile = $('.tile');
						tile.css('background-color', '#e5e5e5');
	
					break;
					
				case 'box':
				
					updateTimeFrame(DASHBOARD_TIME_FRAME_STRING);
					loopable = SORTED_PROJECT_IDS.length;
					
					for(i = 0; i< loopable; i++) {
						
						dataObject = {
						timeFrame: DASHBOARD_TIME_FRAME_STRING,
						projectId: SORTED_PROJECT_IDS[i],
						view: 'box',
						mergeSpan: 60,
						measureName: "Overall Health"
						};
						fetchDataAndDrawTimeseriesSingleProject(dataObject);	
					};
					
					tile = $('.tile');
					tile.css('background-color', '#e5e5e5');
					break;
					
				case 'status':
				
					updateTimeFrame(DASHBOARD_TIME_FRAME_STRING);
					loopable = SORTED_PROJECT_IDS.length;
					
					for(i = 0; i< loopable; i++) {
						
						dataObject = {
						timeFrame: DASHBOARD_TIME_FRAME_STRING,
						projectId: SORTED_PROJECT_IDS[i],
						view: 'status',
						mergeSpan: 60,
						measureName: "Overall Health"
						};
						fetchDataAndDrawTimeseriesSingleProject(dataObject);	
					};
					
					tile = $('.tile');
					tile.css('background-color', '#e5e5e5');
					tile.css('border', 'solid 0.5px #e5e5e5');
					break;
					
				default:
				
					draw();
			}
		}, REFRESH_TIME);
	}
};

/**************************************************************************************
* Function to store a value in the local storage.
* @param cname name of the cookie
* @param cvalue value of the cookie
* @param exdays number of days after which the cookie gets deleted
 *************************************************************************************/
function storeLocalValue(cname, cvalue) {
	window.localStorage.setItem(ENVIRONMENT+"-"+cname, cvalue);
};

/**************************************************************************************
* Function to get a cookie
* @param cname name of the cookie to get
* @return either the value of the cookie or an empty string
 *************************************************************************************/
function retrieveLocalValue(cname) {
	
	var item = window.localStorage.getItem(ENVIRONMENT+"-"+cname);
    if(item != null){
    	return item;
    }
	return "";
};

/*******************************************************************************
 * Show Loading Animation
 * @param isVisible boolean to show or hide the loader
 ******************************************************************************/
function showLoader(isVisible){
	
	if(isVisible){
		
		$("#loading").css("visibility", "visible");
		$("#loadingText").text(LOADING_TEXTS[Math.floor(Math.random()*LOADING_TEXTS.length)]);
	}else{
		$("#loading").css("visibility", "hidden");
	}
};

/*******************************************************************************
 * Filter projects in dashboard
 * @param selector string to filter by
 * @see storeLocalValue()
 * @see draw()
 * @see refreshTimer()
 ******************************************************************************/
function filterProjects(selector) {
	
	var input = $(selector).val();
	var filterable = $('.filterable');
	
	filterable.show();
	
	storeLocalValue("searchFilter", input);

	if(input != '') {
		
		//-------------------------
		// Easter eggs
		if(input == 'magic') {
			
			storeLocalValue('BORDER_RADIUS', 15 * ZOOM);
			BORDER_RADIUS = 15 * ZOOM;
			PROJECT_FILTER.val('');
			draw();
		}else if(input == 'frankinator'){
			
			storeLocalValue('BORDER_RADIUS', 5);
			BORDER_RADIUS = 5;
			PROJECT_FILTER.val('');
			draw();
		}else if(input == 'manager mode'){
			
			$('.tile').text("Don't worry about it! Everything is perfect!");
			$('.tile').css('font-size', 20 * ZOOM);
			$('.tile').css('border', '0.5px solid #e5e5e5');
			$('.tile').css('background-color', '#01c615');
			PROJECT_FILTER.val(''); 
		}else if(input == 'aprilapril'){
			$('.tile').css('border', '0.5px solid #91160d');
			$('.tile').css('background-color', '#dd1506');
			PROJECT_FILTER.val(''); 
		}else if(input == 'merry christmas'){
			$('#result').css('background-image', 'url(/resources/images/snowflakes.png)')
			.css('background-size', '15%');
			
			$('.tile').css('border-radius', '50%')
				.css('border', '2px solid gold');
				
			$('.tidDiv').css('text-align', 'center')
				.css('margin-left', 'auto')
				.css('margin-right', 'auto')
				.css('position', 'unset')
				.css('width', '70%')
				.css('bottom', 15*ZOOM);
			
			$('.envDiv').css('text-align', 'center')
				.css('margin-left', 'auto')
				.css('margin-right', 'auto')
				.css('position', 'relative')
				.css('width', '70%')
				.css('top', 15*ZOOM);
				
			PROJECT_FILTER.val('');
			
		}else if(input == 'reality mode'){
			
			PROJECT_FILTER.val('');
			draw();
		}else if(input == 'fast refresh'){
			
			PROJECT_FILTER.val('');
			clearInterval(INTERVAL_ID);
			REFRESH_TIME = 30000;
			refreshTimer();
			draw();
		}else if(input == 'normal refresh'){
			
			PROJECT_FILTER.val('');
			clearInterval(INTERVAL_ID);
			REFRESH_TIME = $('#refreshSelector').val();
			refreshTimer();
			draw();
		}else if(input == 'easter') {
			
			$('#result').css('background-image', 'url(/resources/images/grass.jpg)')
				.css('position', 'relative')
				.css('width', '100%')
				.css('min-height', '100%')
				.css('background-size', 'cover')
				.css('overflow', 'hidden');
			
			$('.tile').css('height', 200 * ZOOM)
				.css('-webkit-border-radius', '50%/60% 60% 40% 40%')
				.css('-moz-border-radius', '50%/60% 60% 40% 40%')
				.css('border-radius', '50%/60% 60% 40% 40%')
				.css('transform', 'rotate(15deg)');
				
			$('.tidDiv').css('text-align', 'center')
				.css('margin-left', 'auto')
				.css('margin-right', 'auto')
				.css('position', 'unset')
				.css('width', '70%')
				.css('bottom', 15*ZOOM);
			
			$('.envDiv').css('text-align', 'center')
				.css('margin-left', 'auto')
				.css('margin-right', 'auto')
				.css('position', 'relative')
				.css('width', '70%')
				.css('top', 15*ZOOM);
				
			PROJECT_FILTER.val('');
			
		}
		
		//-------------------------
		// Actual filtering
		for (index in PROJECT_LIST) {
			if(PROJECT_LIST[index].fullName.toUpperCase().indexOf(input.toUpperCase()) == -1 && PROJECT_LIST[index].Tag.toUpperCase().indexOf(input.toUpperCase()) == -1) {
				$('#' + PROJECT_LIST[index].id).css('display', 'none');
			}
		}
	}
};

/*******************************************************************************
 * Easter egg: set zoom to ridiculous level
 * @see retrieveLocalValue()
 * @see draw()
 ******************************************************************************/
function zoooom() {
	
	if (ZOOM == 4) {
		if(retrieveLocalValue('ZOOM') != ''){
			ZOOM = retrieveLocalValue('ZOOM');
		}else{
			ZOOM = 1;
		}
		
	} else {
		ZOOM = 4;
	}
	
	draw();
};

/*******************************************************************************
 * Easter egg: Change display to draw a christmas design.
 * @see retrieveLocalValue()
 * @see draw()
 ******************************************************************************/
function hohoho() {

	if (DISPLAY_MODE != "christmas") {
		
		DISPLAY_MODE = "christmas";
		storeLocalValue('DISPLAY_MODE', "christmas");
		
	} else {
		
		DISPLAY_MODE = "regular";
		storeLocalValue('DISPLAY_MODE', "regular");
		
	}
	
	draw();
};

/*******************************************************************************
 * Easter egg: Change display to draw a april fools day design.
 * @see retrieveLocalValue()
 * @see draw()
 ******************************************************************************/
function primus_aprilis() {

	if (DISPLAY_MODE != "april") {
		
		DISPLAY_MODE = "april";
		storeLocalValue('DISPLAY_MODE', "april");
		
	} else {
		
		DISPLAY_MODE = "regular";
		storeLocalValue('DISPLAY_MODE', "regular");
		
	}
	
	draw();
};

/*******************************************************************************
 * Easter egg: Change display to draw an easter design.
 * @see retrieveLocalValue()
 * @see draw()
 ******************************************************************************/
function bunny() {

	if (DISPLAY_MODE != "easter") {
		
		DISPLAY_MODE = "easter";
		storeLocalValue('DISPLAY_MODE', "easter");
		
	} else {
		
		DISPLAY_MODE = "regular";
		storeLocalValue('DISPLAY_MODE', "regular");
		
	}
	
	draw();
};
/*******************************************************************************
 * Draws a legend which explains the color mapping in status/dashboard view
 ******************************************************************************/
function drawLegend(){
	
	var legend = $("#legend");
	var rowstring = '<table class="customTable" style="padding-top: 10px; padding-left: 19px;"><tr><td><div style="height: 10px; width: 10px; border: solid 1px #e5e5e5; background-color: rgb(130, 185, 105)"></div></td><td>100%</td><td><div style="height: 10px; width: 10px; border: solid 1px #e5e5e5; background-color: rgb(148, 186, 100)"></div></td><td>>75%</td><td><div style="height: 10px; width: 10px; border: solid 1px #e5e5e5; background-color: rgb(231, 196, 80)"></div></td><td>>50%</td><td><div style="height: 10px; width: 10px; border: solid 1px #e5e5e5; background-color: rgb(255, 179, 71)"></div></td><td>50%</td><td><div style="height: 10px; width: 10px; border: solid 1px #e5e5e5; background-color: rgb(255, 116, 64)"></div></td><td>>25%</td><td><div style="height: 10px; width: 10px; border: solid 1px #e5e5e5; background-color: rgb(255, 57, 57)"></div></td><td><25%</td><td><div style="height: 10px; width: 10px; border: solid 1px #e5e5e5; background-color: #e5e5e5"></div></td><td>Offline</td></tr></table>';
	
	legend.attr("title", "Health");
	legend.html(rowstring);
};

/*******************************************************************************
 * Make an API call every 5 minutes to prevent the session from timing out.
 * Stops all intervals/requests if there is no response from the server
 ******************************************************************************/
function keepSessionAlive() {
	
	ALIVE = setInterval(function() {	
		
		var url = "./theusinator?env="+ENVIRONMENT+"&service=svdata&method=getLastClientData&sessionId="
		+ SESSION + "&measureName=Overall%20Health&projectId=" + SORTED_PROJECT_IDS[0];
		
		$.get(url
		).always(
		
			function(data) {
				if($(data).find('readableTime').length == 0) {
					clearInterval(ALIVE);
					clearInterval(INTERVAL_ID);
					RESULTS_DIV.html('<p>Session terminated. Please reload page.</p>');
				}
			}
		)
	}, 300000);
};

/*******************************************************************************
 * Limit mergeSpan options to only provide sensible data and limit strain on 
 * the server. mergeSpanSelector will only show possible options as active.
 * Switch with fallthrough
 ******************************************************************************/
function setMergeSpanOptions() {
	
	var mergeOptions = $("#mergeSpanSelector option");
	
	mergeOptions.attr('disabled', 'disabled');
	
	if(CURRENT_TIME_FRAME == 'custom') {
		
		var timeFrame = END_DATE.getTime() - START_DATE.getTime();
		var threeMonthsMs = 1000 * 60 * 60 * 24 * 30 * 3;
		var twoMonthsMs = 1000 * 60 * 60 * 24 * 30 * 2;
		
		if(timeFrame > threeMonthsMs) {
			
			mergeOptions.eq(0).removeAttr('disabled');
			mergeOptions.eq(1).removeAttr('disabled');
		}else if(timeFrame > twoMonthsMs){
			
			mergeOptions.eq(0).removeAttr('disabled');
			mergeOptions.eq(1).removeAttr('disabled');
			mergeOptions.eq(2).removeAttr('disabled');
		}else {
			
			mergeOptions.eq(0).removeAttr('disabled');
			mergeOptions.eq(1).removeAttr('disabled');
			mergeOptions.eq(2).removeAttr('disabled');
			mergeOptions.eq(3).removeAttr('disabled');
		}
		
	}else{
		
		switch(CURRENT_TIME_FRAME) {
			
			case 'onehour':
				mergeOptions.eq(0).removeAttr('disabled');
				// fall through
				
			case 'oneday':
				mergeOptions.eq(0).removeAttr('disabled');
				mergeOptions.eq(1).removeAttr('disabled');
				// fall through
				
			case 'oneweek':
				mergeOptions.eq(0).removeAttr('disabled');
				mergeOptions.eq(1).removeAttr('disabled');
				mergeOptions.eq(2).removeAttr('disabled');
				// fall through
				
			case 'onemonth':
				mergeOptions.eq(0).removeAttr('disabled');
				mergeOptions.eq(1).removeAttr('disabled');
				mergeOptions.eq(2).removeAttr('disabled');
				mergeOptions.eq(3).removeAttr('disabled');
				// fall through
				
			case 'oneyear':
				mergeOptions.eq(0).removeAttr('disabled');
				mergeOptions.eq(1).removeAttr('disabled');
				break;
		}
	}
};

/*******************************************************************************
 * Toggles text sizes in box and heaklth history view
 * @see retrieveLocalValue()
 * @see draw()
 ******************************************************************************/
function toggleTextSize() {
	if(TEXT_SIZE == 'big') {
		TEXT_SIZE = 'small';
		storeLocalValue('TEXT_SIZE', 'small');
		draw();
	} else {
		TEXT_SIZE = 'big';
		storeLocalValue('TEXT_SIZE', 'big');
		draw();
	}
}