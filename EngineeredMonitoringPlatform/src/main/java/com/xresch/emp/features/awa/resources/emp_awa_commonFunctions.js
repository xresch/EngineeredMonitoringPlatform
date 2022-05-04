
/******************************************************************
 * Register Category
 ******************************************************************/
var CATEGORY_AWA="AWA";
CFW.dashboard.registerCategory("fas fa-desktop", CATEGORY_AWA);

/*********************************************************************
 * Returns a partial CSS style for the given AWA status.
 * @returns style like "cfw-warning"
 * 
*********************************************************************/
function emp_widget_awa_getStatusStyle(status){
	
	var style = "";
	
	//--------------------
	// Add Colors
	switch(status.toUpperCase()){
		case "RUNNING": 	style = "cfw-warning"; 
							break;
		case "WAITING": 	style = "cfw-cyan"; 
							break;					
		case "ENDED OK": 	style = "cfw-excellent"; 
							break;
		case "ABNORMAL ENDING": 		style = "cfw-danger"; 
							break;
		case "UNKNOWN": 	style = "cfw-gray"; 
							break;
		case "OVERDUE (RUNNING)":
		case "OVERDUE (WAITING)":	
		case "OVERDUE (ENDED OK)":
		case "OVERDUE (ABNORMAL ENDING)":
		case "OVERDUE (UNKNOWN)":
		case "OVERDUE":	
							style = "cfw-emergency"; 
							break;
	}
	
	return style;
}

/*********************************************************************
 * 
 * @returns style like "cfw-warning"
 * 
*********************************************************************/
function emp_widget_awa_groupByJobname(statusArray){
	
	var groupedResults = {};
	
	for(let index in statusArray){
		let currentStatus = statusArray[index];
		let jobname = currentStatus.JOBNAME;
		let duration = currentStatus.DURATION_SECONDS;
		console.log(duration)
		//--------------------------------
		// Initialize group if not exists
		if(groupedResults[jobname] == undefined){{
			groupedResults[jobname] = {
					JOBNAME: jobname,
					LABEL: currentStatus.LABEL,
					AVG: 0,
					COUNT: 0,
					SUM: 0,
					MIN: 999999999,
					MAX: 0,
					STATUSES: []
				}
			}
		}
		
		//--------------------------------
		// Fill Data
		if(!isNaN(duration) && currentStatus.END_TIME != null){
			groupedResults[jobname].COUNT += 1;
			groupedResults[jobname].SUM += duration;
			groupedResults[jobname].MIN = (groupedResults[jobname].MIN <= duration) ? groupedResults[jobname].MIN : duration;
			groupedResults[jobname].MAX = (groupedResults[jobname].MAX >= duration) ? groupedResults[jobname].MAX : duration;
		}
		
		groupedResults[jobname].STATUSES.push(currentStatus);
		
	}
	
	//--------------------------------
	// create result array
	let finalArray = [];
	for(let key in groupedResults){
		let currentGroup = groupedResults[key];
		currentGroup.AVG = currentGroup.SUM / currentGroup.COUNT;
		_.reverse(currentGroup.STATUSES);
		finalArray.push(currentGroup);
	}
	
	return finalArray;
	
}





