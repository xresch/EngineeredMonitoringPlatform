(function (){
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerCategory("fas fa-desktop", "Webex");
	
	/******************************************************************
	 * 
	 ******************************************************************/
	function emp_widget_webex_prepareComponentStyles(components, isDisabled){
		
		for(var key in components){
			var current = components[key];
			current.textstyle = "white"; 
			if(current.status == null){
				current.status = "Unknown";
				current.alertstyle = "cfw-gray"; 
				continue;
			}
			
			if(isDisabled) { 
				current.alertstyle = "cfw-darkgray"; 
				continue;
			}
			
			switch(current.status.toLowerCase()){
				case "operational": current.alertstyle = "cfw-green"; 
									break;
				case "unknown": 	current.alertstyle = "cfw-gray"; 
									break;
				default: 			current.alertstyle = "cfw-red"; 
				break;
			}
			
			//----------------------------------
			// Iterate Children
			if(current.components.length > 0){
				emp_widget_webex_prepareComponentStyles(current.components, isDisabled);
			}
		}
	}
	/******************************************************************
	 * 
	 ******************************************************************/
	function emp_widget_webex_creatSubComponentHTML(rendererSettings, record, components){
		
		//-------------------------------
		// Check Data
		if( components == undefined || components.length == null){
			return '&nbsp;';
		}
		
		//-------------------------------
		// Check Data
		var deepCopyRenderSettings = JSON.parse(JSON.stringify(rendererSettings));
		
		deepCopyRenderSettings.data = components;
		
		deepCopyRenderSettings.rendererSettings.tiles.showlabels = false;
		deepCopyRenderSettings.rendererSettings.tiles.popover = true;
		deepCopyRenderSettings.rendererSettings.tiles.border = '1px solid #2f2f2f';
		
		//--------------------------
		// Render
		var renderType = 'tiles';
		
		var alertRenderer = CFW.render.getRenderer(renderType.toLowerCase());
		return alertRenderer.render(deepCopyRenderSettings);
		
	}
		
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_webexservicestatus",
		{
			category: "Webex",
			menuicon: "fas fa-cogs",
			menulabel: CFWL('emp_widget_webexservicestatus', "Webex Service Status"),
			description: CFWL('emp_widget_webexservicestatus_desc', "Fetches the status of Webex services from the given Webex REST URL. (Example: https://service-status.webex.com/customer/dashServices/123)"),
			createWidgetInstance: function (widgetObject, params, callback) {
					
				CFW.dashboard.fetchWidgetData(widgetObject, params, function(data){
					//-------------------------------
					// Check Data
					var settings = widgetObject.JSON_SETTINGS;
					var webexStatus = data.payload;
					
					if(webexStatus == null){
						callback(widgetObject, "");
						return;
					}
					
					//-------------------------------
					// Get Components
					var components = webexStatus.components;
					if( components == undefined || components.length == null){
						callback(widgetObject, "No data to display");
						return;
					}
										
					//-------------------------------
					// Prepare Styling
					emp_widget_webex_prepareComponentStyles(components, widgetObject.JSON_SETTINGS.disable);
					
					//-------------------------------
					// Prepare Rendering
					var dataToRender = {
						data: components,
						bgstylefield: 'alertstyle',
						textstylefield: 'textstyle',
						titlefields: ['componentName'], 
						titleformat: '{0}', 
						visiblefields: ['status', 'components'], 
						labels: {
							componentName: "Name",
							components: "Children"
						},
						customizers: {
							components: function(record, value) { return (value.length == 0) ? null : emp_widget_webex_creatSubComponentHTML(dataToRender, record, value); }
						},
						rendererSettings: CFW.dashboard.createStatusWidgetRendererSettings(settings)
					};
					
					//--------------------------
					// Adjust Settings for Table
					if(settings.renderer == "Table"){
						dataToRender.visiblefields = ['componentName', 'status', 'components']; 
					}					
					//--------------------------
					// Render
					var renderType = settings.renderer;
					if(renderType == null){ renderType = 'tiles'};
					
					var alertRenderer = CFW.render.getRenderer(renderType.toLowerCase());
					callback(widgetObject, alertRenderer.render(dataToRender));

				});
			},
			
			getEditForm: function (widgetObject) {
				return CFW.dashboard.getSettingsForm(widgetObject);
			},
			
			onSave: function (form, widgetObject) {
				widgetObject.JSON_SETTINGS = CFW.format.formToObject(form);
				return true;		
			},
		}
	);	
	
})();