(function (){
		
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_step_statuslegend",
		{
			category: CATEGORY_EXENSE,
			menuicon: "fas fa-info-circle",
			menulabel: CFWL('emp_widget_statuslegend', "Status Legend"),
			description: CFWL('emp_widget_statuslegend_desc', "Displays a legend for the colors used by the Step widgets."),
			defaulttitle: "",
			defaultwidth: 32,
			defaultheight: 4,
			createWidgetInstance: function (widgetObject, params, callback) {		
				var html = 
					 '<div class="legend-box">  <div class="cfw-color-box bg-cfw-excellent">&nbsp;</div> Excellent/Passed </div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-good">&nbsp;</div> Good </div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-warning">&nbsp;</div> Warning </div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-emergency">&nbsp;</div> Emergency </div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-danger">&nbsp;</div> Danger/Failed </div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-blue">&nbsp;</div> Running</div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-gray">&nbsp;</div> Unknown/No Data </div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-darkgray">&nbsp;</div> Disabled </div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-black">&nbsp;</div> Technical Error </div>'
					;
				callback(widgetObject, html);
			},
			getEditForm: function (widgetObject) {
				return null;
			},
			onSave: function (form, widgetObject) {
				widgetObject.JSON_SETTINGS = CFW.format.formToObject(form);
				return true;
			}
			
		}
	);
})();