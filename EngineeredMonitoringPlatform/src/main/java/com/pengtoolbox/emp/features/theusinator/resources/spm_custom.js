
/**************************************************************************************
 * Global VARS
 *************************************************************************************/
var FIRST_TOGGLE=true;
var INITIAL_VIEW = getCookie("selectedView");
var INITIAL_NAV_STATE = getCookie("navigationState");



/**************************************************************************************
 * 
 *************************************************************************************/
function clearTableFilters(){
	$('.tablefilter').val("").text("").trigger("keyup");
}

/**************************************************************************************
 * 
 *************************************************************************************/
function addFilterToTable(table){
	
	//=====================================
	// Add Filter
	//=====================================
	var parent = $('<div>');
	var filter = $('<input type="text" class="form-control tablefilter" style="margin: 5px; padding: 3px;" onkeyup="doFilter(this)" placeholder="Filter Table...">');

	filter.val(getCookie("searchFilter"));
	filter.text(getCookie("searchFilter"));
	
	//parent.append("<label>Filter:</label>")
	parent.append(filter);
	parent.append('<p style="font-size: xx-small;"><strong>Hint:</strong> Filters the displayed elements by the keyword. Use &quot;&gt;&quot; and &quot;&lt;&quot; to search for the beginning and end of an element text(e.g. &quot;&gt;Test&lt;&quot; )</p>');
	filter.data("table", $(table));
	parent.insertBefore(table);
		
}

/**************************************************************************************
 * 
 *************************************************************************************/
function selectHealthHistoryView(selectBox){
	toggleHealthHistoryView($(selectBox).val());
}
/**************************************************************************************
 * 
 *************************************************************************************/
function toggleHealthHistoryView(viewType){
		
	//=====================================
	// Table View
	var tableDiv = $(".custom-table");
	
	//=====================================
	// Box View
	var boxViewDiv = $('#boxViewDiv');
	if(boxViewDiv.size() > 0){
		boxViewDiv.css('display', 'none');
		boxViewDiv.empty();
	}else{
		boxViewDiv = $('<div id="boxViewDiv" class="flex-container filterable">');
		boxViewDiv.insertBefore(tableDiv);
	}
	
	//=====================================
	// Status View
	var statusViewDiv = $('#statusViewDiv');
	if(statusViewDiv.size() > 0){
		statusViewDiv.css('display', 'none');
		statusViewDiv.empty();
	}else{
		statusViewDiv = $('<div id="statusViewDiv" class="flex-container filterable">');
		statusViewDiv.insertBefore(tableDiv);
	}
	
	//=====================================
	// Render Switch
	switch(viewType){
		
		case 'table':   tableDiv.css('display', '');
						break;
						
		case 'box': 	tableDiv.css('display', 'none');
						 renderBoxView(boxViewDiv);
						boxViewDiv.css('display', '');
						break;
		
		case 'status': 	tableDiv.css('display', 'none');
						renderStatusOverview(statusViewDiv);
						statusViewDiv.css('display', '');
						break;		
						
						
	}
	
	setCookie('selectedView', viewType, 365);
	
	//=====================================
	// Trigger Filter Update
	$('.tablefilter').trigger("keyup");
	
	toggleStyleMode(getCookie("style"));
}

/**************************************************************************************
 * 
 *************************************************************************************/
function renderBoxView(parent){
		
	$(".custom-table > tbody > tr").each(
		function(index){
			if(index > 0 ){
				
				var currentRow = $(this);
				var chartbox = $('<div class="chartbox flex-item">');

				var innerFlexUpper = $('<div class="innerflex flex-container">');
				//var innerFlexLower = $('<div class="innerflex flex-container">');
				var TDs = currentRow.find("td > table tr td");
				
				var innerHTML = "";
				var lastcolor = "";
				TDs.each(
					function(index){
						if(index <= (TDs.size() / 2)-1){
							lastcolor = $(this).attr("bgcolor");
							innerHTML += '<div class="colordiv" style="background-color: '+lastcolor+';" ></div>';
							
						}else{
							//innerFlexLower.append('<div class="colordiv" style="background-color: '+$(this).attr("bgcolor")+';" >');
						}
						
					}
				);
				
				
				innerFlexUpper.append(innerHTML);
				
				chartbox.append(currentRow.find("td").html());
				chartbox.append(innerFlexUpper);
				
				var img = currentRow.find("td > img");
				
				if(img.size() != 0){
					if(img.attr('src').indexOf('red') > -1){
						chartbox.css('border', '5px solid red');
					}else if(img.attr('src').indexOf('yellow') > -1){
						chartbox.css('border', '5px solid gold');
					}
				}
				
				parent.append(chartbox);
				
			}
		}
	);
}

/**************************************************************************************
 * 
 *************************************************************************************/
function renderStatusOverview(parent){
	
	$(".custom-table > tbody > tr").each(
		function(index){
			if(index > 0 ){
				
				var alertDiv = $('<div class="alertDiv">');
				var img = $(this).find("td > img");
				
				var bgcolor = "red";
				var textcolor = "white";
				if(img.size() == 0){
					bgcolor = "lightgray";
					textcolor = "black";
				}else if(img.attr('src').indexOf('green') > -1){
					bgcolor = "green";
				}else if(img.attr('src').indexOf('yellow') > -1){
					bgcolor = "gold";
					textcolor = "black";
				}
				
				alertDiv.css('background-color', bgcolor);
				
				link = $($(this).find("td").html());
				link.html(link.find('font').text());
				link.css('color', textcolor);
				link.css('background-color', bgcolor);
				
				alertDiv.append(link);
				
				parent.append(alertDiv);
				
			}
		}
	);
	
}

/**************************************************************************************
 * 
 *************************************************************************************/
function saveNavigationState(){
	setCookie('navigationState', $('#treeLayer').attr('class'), 365);
}


/**************************************************************************************
 * 
 *************************************************************************************/
function selectStyleMode(selectBox){
	toggleStyleMode($(selectBox).val());
}

/**************************************************************************************
 * 
 *************************************************************************************/
function toggleStyleMode(style){
	
	//remove line from selected tab
	$('#selectedTab').css('height','auto');
	
	var bgcolorSelector = '.bgcolormodevolatile, .logoTable td, .dialog td, .closeableContainerTitle, .closeableContainerHeader, .closeableContainerHeader .selected, .standardGridTable > tbody > tr > td, body, .tabbedPane > a, #treeLayer, .tablefilter';
	var textcolorSelector = '.textcolorvolatile, table td, p, .closeableContainerTitle a, td a, .closeableContainerHeader a, font, .tabbedPane > a, #treeLayer a, .tablefilter, h4';
	
	$('.chartbox').css('background-color',''); 
	$('.logoTable').attr('cellspacing','0');
	
	switch(style){

		case 'standard':	$(bgcolorSelector).css('background-color', '');
							$(textcolorSelector).css('color', '');
							$('.bgtile').css('background-color', 'white');
							
							break;
							
		case 'darkblue':  	$(bgcolorSelector).css('background-color', 'rgba(0, 43, 112, 1)');
							$(textcolorSelector).css('color', 'rgba(240, 240, 240, 1)');
							$('.bgtile, .chartbox, .tabbedPane > a, .custom-table > tbody > tr > td').css('background-color','rgba(54, 54, 54, 1)'); 
							$('.logoTable td').css('background-color','rgba(0, 166, 251, 1)'); 
							$('.product-logo').css('background-color','rgba(0, 140, 219, 1)');							
							break;
							
		case 'darkgray':   	$(bgcolorSelector).css('background-color', 'rgba(54, 54, 54, 1)');
							$(textcolorSelector).css('color', 'rgba(240, 240, 240, 1)');
							$('.bgtile, .chartbox, .tabbedPane > a, .custom-table > tbody > tr > td').css('background-color','rgba(36, 36, 36, 1)'); 
							$('.logoTable td').css('background-color','rgba(0, 166, 251, 1)'); 
							$('.product-logo').css('background-color','rgba(0, 140, 219, 1)');
							break;
							
		case 'night':		$(bgcolorSelector).css('background-color', 'rgba(36, 36, 36, 1)');
							$(textcolorSelector).css('color', 'rgba(240, 240, 240, 1)');
							$('.bgtile, .chartbox, .tabbedPane > a, .custom-table > tbody > tr > td').css('background-color','rgba(54, 54, 54, 1)'); 
							$('.logoTable td').css('background-color','rgba(0, 166, 251, 1)'); 
							$('.product-logo').css('background-color','rgba(0, 140, 219, 1)');
							break;
										
	}
		
	//Fix certain components
	$('.actionButtons').css('color','');
	$('input, select, .btnCell a, .loginLabel').css('color','black');
	$('.btnCell').css('background-color','');
	
	if(style == 'standard'){
		$('.tablefilter').css('color', 'black');
	}else{
		$('.tablefilter').css('color', 'white');
	}
	
	
	setCookie("style", style, 365);
	
}
/**************************************************************************************
 * General UI Adjustments
 *************************************************************************************/
function generalAdjustments(selectedTabName, currentURL){
	//=====================================
	// Table: Add filters and clear button
	// exclude on pages where it doesn't make 
	// sense
	if( selectedTabName != "Client Health" 
	 &&	selectedTabName != "Service Target"
	 && currentURL.indexOf("/Monitoring/Reports") == -1
	 && currentURL.indexOf("/Administration/System") == -1
	 && currentURL.indexOf("/DEF/Help") == -1){
		 
		$(".standardGridTable, .defaulttable").each(
			function (){
				addFilterToTable(this);
			}
		);
		
		var clearFilters = $('<span class="btnCell margin5px" onclick="clearTableFilters()">Clear Filters</span>');
		$('.actionButtons').append(clearFilters);
	}
	
	//=====================================
	// Night Mode Button
	var styleModeDropdown = $('<select class="margin5px" id="styleModeDropdown" onclick="selectStyleMode(this)" >');
	styleModeDropdown.append('<option value="standard">Standard Mode</option>');
	styleModeDropdown.append('<option value="darkblue">Dark Blue Mode</option>');
	styleModeDropdown.append('<option value="darkgray">Dark Gray Mode</option>');
	styleModeDropdown.append('<option value="night">Night Mode</option>');
	
	var goToDashboard = $('<span class="btnCell margin5px">Dashboard</span>');
	goToDashboard.attr('onclick', "window.location.href='/silk/DEF/Monitoring/Projects?pTab=5'");
	$('.actionButtons').append(goToDashboard);
	
	$('.actionButtons').append(styleModeDropdown);
	
	styleModeDropdown.val((getCookie('style') == '')? 'standard' : getCookie('style'));
	
	//=====================================
	// Table: Add custom-table class
	$(".standardGridTable, .defaulttable").addClass("custom-table");
	
	//=====================================
	// Rename Tab Snapshot
	$('a:contains("Snapshot")').text("Dashboard");
	
	//=====================================
	// Handle Navigation State
	$('#toggleNavigationTreeLink').attr('onclick', 'toggleNavigationMenu(); saveNavigationState();');
	
	var currentState = $('#treeLayer').attr('class');

	if(INITIAL_NAV_STATE != currentState){
		toggleNavigationMenu();
		saveNavigationState();
	}
			
}

/**************************************************************************************
 * HealthHistoryAdjustments
 *************************************************************************************/
function healthHistoryAdjustments(){
	var boxViewDropdown = $('<select id="boxViewDropdown" class="margin5px" onclick="selectHealthHistoryView(this)" >');
	boxViewDropdown.append('<option value="table">Table View</option>');
	boxViewDropdown.append('<option value="box">Box View</option>');
	boxViewDropdown.append('<option value="status">Status View</option>');
	$('.actionButtons').append(boxViewDropdown);
	
	boxViewDropdown.val(INITIAL_VIEW);
	
}

/**************************************************************************************
 * Main Entry Method
 *************************************************************************************/
function main(){
	
	var selectedTabName = $('#selectedTab').text();
	var currentURL = window.location.href;
	
	generalAdjustments(selectedTabName, currentURL);
	
	if(selectedTabName == 'Health History'){
		healthHistoryAdjustments();
		
		if(INITIAL_VIEW != "table"){
			toggleHealthHistoryView(INITIAL_VIEW);
		}
	}
	
	//=====================================
	// Trigger Updates with Cookie Value
	
	//style mode
	var style = getCookie("style");
	if(style != "standard"){
		toggleStyleMode(style);
	}
	

}

main();
