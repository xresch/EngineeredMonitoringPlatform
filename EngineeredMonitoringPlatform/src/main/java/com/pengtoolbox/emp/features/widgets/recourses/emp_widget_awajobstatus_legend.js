(function (){
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerCategory("fas fa-desktop", "Monitoring");
	
//	if (current.VALUE == 100) 		{ current.alertstyle = "cfw-excellent"; } 
//	
//	else if (current.VALUE >= 75) 	{ current.alertstyle = "cfw-good"; } 
//	else if (current.VALUE >= 50) 	{ current.alertstyle = "cfw-warning"; } 
//	else if (current.VALUE >= 25) 	{ current.alertstyle = "cfw-emergency"; } 
//	else if (current.VALUE >= 0)  	{ current.alertstyle = "cfw-danger"; } 
//	else if (current.VALUE == 'NaN' 
//		  || current.VALUE < 0) { 		  current.alertstyle = "cfw-gray"; } 
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_awajobstatus_legend",
		{
			category: "Monitoring",
			menuicon: "fas fa-info-circle",
			menulabel: CFWL('cfw_widget_awajobstatuslegend', "AWA Job Status Legend"),
			description: CFWL('cfw_widget_awajobstatuslegend_desc', "Displays a legend for the colors used by the AWA job status widget."),
			createWidgetInstance: function (widgetObject, callback) {		
				var html = '<div class="spm-legend-box">  <div class="cfw-color-box bg-cfw-excellent">&nbsp;</div> Ended OK </div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-warning">&nbsp;</div> Running </div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-danger">&nbsp;</div> Issue </div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-gray">&nbsp;</div> No Data </div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-darkgray">&nbsp;</div> Disabled </div>'
					;
				callback(widgetObject, html);
			},
			getEditForm: function (widgetObject) {
				return null;
				//return CFW.dashboard.getSettingsForm(widgetObject);
			},
			onSave: function (form, widgetObject) {
				widgetObject.JSON_SETTINGS = CFW.format.formToObject(form);
				return true;
			}
			
		}
	);
})();