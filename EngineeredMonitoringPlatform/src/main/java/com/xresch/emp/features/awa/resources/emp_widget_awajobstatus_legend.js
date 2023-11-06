(function (){
		
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_awajobstatus_legend",
		{
			category: CATEGORY_AWA,
			menuicon: "fas fa-info-circle",
			menulabel: CFWL('emp_widget_awajobstatuslegend', "AWA Job Status Legend"),
			description: CFWL('emp_widget_awajobstatuslegend_desc', "Displays a legend for the colors used by the AWA job status widget."),
			defaultsettings: {
				TITLE: "",
				WIDTH: 32,
				HEIGHT: 4,
			},
			createWidgetInstance: function (widgetObject, params, callback) {		
				var html = 
					 '<div class="legend-box">  <div class="cfw-color-box bg-cfw-green">&nbsp;</div>Ended OK</div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-cyan">&nbsp;</div>Waiting</div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-yellow">&nbsp;</div>Running</div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-orange">&nbsp;</div>Overdue</div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-red">&nbsp;</div>Issue</div>'
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