(function (){
		
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_spmmeasurelegend",
		{
			category: CATEGORY_SPM,
			menuicon: "fas fa-info-circle",
			menulabel: CFWL('emp_widget_smpmeasurelegend', "SPM Measure Legend"),
			description: CFWL('emp_widget_smpmeasurelegend_desc', "Displays a legend for the colors used by the SPM monitors(Timers and Counters)."),
			defaulttitle: "",
			defaultwidth: 32,
			defaultheight: 4,
			createWidgetInstance: function (widgetObject, params, callback) {		
				var html = 
					 '<div class="legend-box">  <div class="cfw-color-box bg-cfw-green">&nbsp;</div> Excellent </div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-limegreen">&nbsp;</div> Good </div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-yellow">&nbsp;</div> Warning </div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-orange">&nbsp;</div> Emergency </div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-red">&nbsp;</div> Danger </div>'
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