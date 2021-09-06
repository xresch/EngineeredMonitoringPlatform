(function (){
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerCategory("fas fa-desktop", "Monitoring | AWA");
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_awajobstatus_legend",
		{
			category: "Monitoring | AWA",
			menuicon: "fas fa-info-circle",
			menulabel: CFWL('emp_widget_awajobstatuslegend', "AWA Job Status Legend"),
			description: CFWL('emp_widget_awajobstatuslegend_desc', "Displays a legend for the colors used by the AWA job status widget."),
			defaulttitle: "",
			defaultwidth: 32,
			defaultheight: 4,
			createWidgetInstance: function (widgetObject, params, callback) {		
				var html = 
					 '<div class="legend-box">  <div class="cfw-color-box bg-cfw-excellent">&nbsp;</div>Ended OK</div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-cyan">&nbsp;</div>Waiting</div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-warning">&nbsp;</div>Running</div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-emergency">&nbsp;</div>Overdue</div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-danger">&nbsp;</div>Issue</div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-gray">&nbsp;</div>Unknown</div>'
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