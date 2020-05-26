(function (){
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerCategory("fas fa-desktop", "Monitoring | SPM");
	
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
	CFW.dashboard.registerWidget("emp_spmmeasurelegend",
		{
			category: "Monitoring | SPM",
			menuicon: "fas fa-info-circle",
			menulabel: CFWL('cfw_widget_smpmeasurelegend', "SPM Measure Legend"),
			description: CFWL('cfw_widget_smpmeasurelegend_desc', "Displays a legend for the colors used by the SPM monitors(Timers and Counters)."),
			defaulttitle: "",
			defaultwidth: 12,
			defaultheight: 1,
			createWidgetInstance: function (widgetObject, callback) {		
				var html = 
					 '<div class="legend-box">  <div class="cfw-color-box bg-cfw-excellent">&nbsp;</div> Excellent </div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-good">&nbsp;</div> Good </div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-warning">&nbsp;</div> Warning </div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-emergency">&nbsp;</div> Emergency </div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-danger">&nbsp;</div> Danger </div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-gray">&nbsp;</div> Unknown/No Data </div>'
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