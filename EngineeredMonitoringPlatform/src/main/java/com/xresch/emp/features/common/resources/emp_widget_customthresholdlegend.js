(function (){
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerCategory("fas fa-desktop", "Monitoring");
	CFW.dashboard.registerCategory("fas fa-star", "Monitoring | Common");
	
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_customthresholdlegend",
		{
			category: "Monitoring | Common",
			menuicon: "fas fa-info-circle",
			menulabel: CFWL('emp_widget_customthresholdlegend', "Custom Threshold Legend"),
			description: CFWL('emp_widget_customthresholdlegend_desc', "Displays a legend for the threshold colors with custom labels."),
			defaulttitle: "",
			defaultwidth: 32,
			defaultheight: 4,
			createWidgetInstance: function (widgetObject, callback) {		
				
				var settings = widgetObject.JSON_SETTINGS;
				var html = '<div class="w-100 d-flex flex-wrap word-break-all">';
				if(!CFW.utils.isNullOrEmpty(settings.labelexcellent)){ 	html += '<div class="legend-box">  <div class="cfw-color-box bg-cfw-excellent">&nbsp;</div> '+settings.labelexcellent+' </div>'; }
				if(!CFW.utils.isNullOrEmpty(settings.labelgood)){ 		html += '<div class="legend-box">  <div class="cfw-color-box bg-cfw-good">&nbsp;</div> '+settings.labelgood+' </div>'; }
				if(!CFW.utils.isNullOrEmpty(settings.labelwarning)){ 		html += '<div class="legend-box">  <div class="cfw-color-box bg-cfw-warning">&nbsp;</div> '+settings.labelwarning+' </div>'; }
				if(!CFW.utils.isNullOrEmpty(settings.labelemergency)){ 		html += '<div class="legend-box">  <div class="cfw-color-box bg-cfw-emergency">&nbsp;</div> '+settings.labelemergency+' </div>'; }
				if(!CFW.utils.isNullOrEmpty(settings.labeldanger)){ 		html += '<div class="legend-box">  <div class="cfw-color-box bg-cfw-danger">&nbsp;</div> '+settings.labeldanger+' </div>'; }
				if(!CFW.utils.isNullOrEmpty(settings.labelgray)){ 		html += '<div class="legend-box">  <div class="cfw-color-box bg-cfw-gray">&nbsp;</div> '+settings.labelgray+' </div>'; }
				if(!CFW.utils.isNullOrEmpty(settings.labeldarkgray)){ 		html += '<div class="legend-box">  <div class="cfw-color-box bg-cfw-darkgray">&nbsp;</div> '+settings.labeldarkgray+' </div>'; }

				html += '</div>';
					
				callback(widgetObject, html);
			},
			
		}
	);
})();