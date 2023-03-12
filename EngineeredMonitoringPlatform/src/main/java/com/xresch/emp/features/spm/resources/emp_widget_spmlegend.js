(function (){
	
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_smplegend",
		{
			category: CATEGORY_SPM,
			menuicon: "fas fa-info-circle",
			menulabel: CFWL('emp_widget_smplegend', "SPM Legend"),
			description: CFWL('emp_widget_smplegend_desc', "Displays a legend for the colors used by the SPM monitors."),
			defaulttitle: "",
			defaultwidth: 10,
			defaultheight: 1,
			createWidgetInstance: function (widgetObject, params, callback) {		
				var html = 
					 '<div class="legend-box">  <div class="cfw-color-box bg-cfw-green">&nbsp;</div> 100% </div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-limegreen">&nbsp;</div> >= 75% </div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-yellow">&nbsp;</div> >= 50% </div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-orange">&nbsp;</div> >= 25% </div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-red">&nbsp;</div> < 25% </div>'
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