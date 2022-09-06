(
	function() {

		// Register your category, will be ignored if a category with this name already exists
		CFW.dashboard.registerCategory("fas fa-flask", "HTTP");

		// Register the widget with an unique name - must be the same name specified as in the java widget class
		CFW.dashboard.registerWidget("emp_httpextensions_evaluateresponse",
			{
				category: 'HTTP',
				menuicon: 'fas fa-flask',
				menulabel: 'Check Response',
				description: CFWL('emp_widget_evaluateresponse_url_desc', 'Takes an URL and performs' +
				' different kinds of checks on the response.'),
				createWidgetInstance: function (widgetObject, params, callback) {

					// This method is used to fetch data from serverside for this widget. This request will be forwarded
					// to your serverside WidgetDefinition.fetchData() method.

					// Callback function
					callback(widgetObject, "");

					CFW.dashboard.fetchWidgetData(widgetObject, params, function(data) {

						var result = data.payload;

						//--------------------
						// PREPARE COLORS
						for(var key in result){
							
							//--------------------
							// Prepare Parameters
							var current = result[key];
							var success = current.CHECK_RESULT;
							
							console.log("current.STATUS_CODE_VALID: "+current.STATUS_CODE_VALID)
							if(current.STATUS_CODE_VALID == false){
								success = false;
							}
							
							current.textstyle = "white";

							//--------------------
							// Check Disabled
							if(widgetObject.JSON_SETTINGS.disable) {
								current.alertstyle = "cfw-darkgray";
								continue;
							}

							//--------------------
							// Get Status Color
							switch (success) {
								case true:
									current.alertstyle = "cfw-excellent";
									break;

								case false:
									current.alertstyle = "cfw-danger";
									break;

								case "failed":
									current.alertstyle = "cfw-gray";

							}

						}

						// Check if data is null
						if (data.payload == null) {
							callback(widgetObject, "No Data");
						}

						// Copied from BlackboxExporter
						var dataToRender = {

							data: data.payload,
							bgstylefield: 'alertstyle',
							textstylefield: 'textstyle',
							titlefields: ['URL'],
							titledelimiter: ' - ',
							visiblefields: ['URL', 'CHECK_TYPE', 'CHECK_FOR', 'CHECK_RESULT', 'STATUS_CODE', 'STATUS_CODE_VALID', 'STATUS_CODE_MESSAGE' ] ,
							labels: {
								URL: 'URL'
							},
							customizers: {
								URL: function(record, value){
									return '<a class="text-white" target="_blank" href="'+value+'">'+value+'</a>';
								},
								CHECK_TYPE: function(record, value){
									return CFW.format.fieldNameToLabel(value);
								},
								CHECK_RESULT: CFW.customizer.booleanFormat,
								STATUS_CODE_VALID: CFW.customizer.booleanFormat
							},
							rendererSettings:{
								tiles: {
									sizefactor: widgetObject.JSON_SETTINGS.sizefactor,
									showlabels: widgetObject.JSON_SETTINGS.showlabels,
									borderstyle: widgetObject.JSON_SETTINGS.borderstyle
								},
								table: {
									narrow: 	true,
									striped: 	false,
									hover: 		false,
									filterable: false,
								},
								panels: {
									narrow: 	true,
								},
								cards: {
									narrow: 	true,
									maxcolumns: 5,
								},
							}};

						//-----------------------------------
						// Adjust RenderSettings for Table
						if(widgetObject.JSON_SETTINGS.renderer == "tiles"){
							dataToRender.visiblefields = ['CHECK_TYPE', 'STATUS_CODE'];
						}
						//-----------------------------------
						// Adjust RenderSettings for Table
						if(widgetObject.JSON_SETTINGS.renderer == "table"){
							dataToRender.bgstylefield = null;
							dataToRender.textstylefield = null;
						}

						//--------------------------
						// Create Tiles
						if(  data.payload == null || typeof data.payload == 'string'){
							callback(widgetObject, "unknown");
						}else{

							var renderType = widgetObject.JSON_SETTINGS.renderer;
							if(renderType == null){ renderType = 'tiles'}

							var alertRenderer = CFW.render.getRenderer(renderType.toLowerCase());
							callback(widgetObject, alertRenderer.render(dataToRender));
						}

					})

				}
			})

	}
)();