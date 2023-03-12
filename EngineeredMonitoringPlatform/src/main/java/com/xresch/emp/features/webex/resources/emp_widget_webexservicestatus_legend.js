(function (){
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerCategory("fas fa-desktop", "Webex");
		
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_webexservicestatus_legend",
		{
			category: "Webex",
			menuicon: "fas fa-info-circle",
			menulabel: CFWL('emp_widget_webexservicestatuslegend', "Webex Service Status Legend"),
			description: CFWL('emp_widget_webexservicestatuslegend_desc', "Displays a legend for the colors used by the Webex Service Status widget."),
			defaulttitle: "",
			defaultwidth: 32,
			defaultheight: 4,
			createWidgetInstance: function (widgetObject, params, callback) {		
				var html = 
					 '<div class="legend-box">  <div class="cfw-color-box bg-cfw-green">&nbsp;</div>Operational</div>'
					+'<div class="legend-box">  <div class="cfw-color-box bg-cfw-red">&nbsp;</div>Other</div>'
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