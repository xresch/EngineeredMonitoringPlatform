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
	CFW.dashboard.registerWidget("emp_smplegend",
		{
			category: "Monitoring",
			menuicon: "fas fa-info-circle",
			menulabel: CFWL('cfw_widget_smplegend', "SPM Legend"),
			description: CFWL('cfw_widget_smplegend_desc', "Displays a legend."),
			createWidgetInstance: function (widgetObject, callback) {		
				var html = '<div class="spm-legend-box">  <div class="cfw-color-box bg-cfw-excellent">&nbsp;</div> 100% </div>'
					+'<div class="spm-legend-box">  <div class="cfw-color-box bg-cfw-good">&nbsp;</div> >= 75% </div>'
					+'<div class="spm-legend-box">  <div class="cfw-color-box bg-cfw-warning">&nbsp;</div> >= 50% </div>'
					+'<div class="spm-legend-box">  <div class="cfw-color-box bg-cfw-emergency">&nbsp;</div> >= 25% </div>'
					+'<div class="spm-legend-box">  <div class="cfw-color-box bg-cfw-danger">&nbsp;</div> < 25% </div>'
					+'<div class="spm-legend-box">  <div class="cfw-color-box bg-cfw-gray">&nbsp;</div> No Data </div>'
					;
				callback(widgetObject, html);
			},
			getEditForm: function (widgetObject) {
				return CFW.dashboard.getSettingsForm(widgetObject);
			},
			onSave: function (form, widgetObject) {
				var settingsForm = $(form);
				widgetObject.JSON_SETTINGS = CFW.format.formToObject(form);
				return true;
			}
			
		}
	);
})();