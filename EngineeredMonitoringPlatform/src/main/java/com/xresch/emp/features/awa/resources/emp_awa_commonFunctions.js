
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
		case "ISSUE": 		style = "cfw-danger"; 
							break;
		case "UNKNOWN": 	style = "cfw-gray"; 
							break;
		case "OVERDUE (RUNNING)":
		case "OVERDUE (WAITING)":	
		case "OVERDUE (ENDED OK)":
		case "OVERDUE (ISSUE)":
		case "OVERDUE (UNKNOWN)":
		case "OVERDUE":	
							style = "cfw-emergency"; 
							break;
	}
	
	return style;
}