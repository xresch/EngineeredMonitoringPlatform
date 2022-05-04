(function (){
	
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_smplegend",
		{
			category: CATEGORY_SPM,
			menuicon: "fas fa-info-circle",
			menulabel: CFWL('emp_widget_smpstatuslegend', "SPM Status Legend"),
			description: CFWL('emp_widget_smpstatuslegend_desc', "Displays a legend for the colors used by the SPM status monitors."),
			defaulttitle: "",
			defaultwidth: 32,
			defaultheight: 4,
			createWidgetInstance: function (widgetObject, params, callback) {		
				var html = 
					 '<div class="legend-box">  <div class="cfw-color-box bg-cfw-excellent">&nbsp;</div> 100% </div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-good">&nbsp;</div> >= 75% </div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-warning">&nbsp;</div> >= 50% </div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-emergency">&nbsp;</div> >= 25% </div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-danger">&nbsp;</div> < 25% </div>'
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