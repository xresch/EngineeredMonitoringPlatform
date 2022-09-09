(
	function() {

		// Register your category, will be ignored if a category with this name already exists
		CFW.dashboard.registerCategory("fas fa-flask", "HTTP");

		// Register the widget with an unique name - must be the same name specified as in the java widget class
		CFW.dashboard.registerWidget("emp_httpextensions_evaluateresponse",
			{
				category: 'HTTP',
				menuicon: 'fas fa-flask',
				menulabel: CFWL('emp_widget_httpextensions_evaluateresponse', 'Evaluate Response'),
				description: CFWL('emp_widget_httpextensions_evaluateresponse_desc', 'Calls one or multiple URLs and performs different kinds of checks on the responses.'),
				createWidgetInstance: function (widgetObject, params, callback) {

					CFW.dashboard.fetchWidgetData(widgetObject, params, function(data) {

						var settings = widgetObject.JSON_SETTINGS;
						var result = data.payload;

						//--------------------
						// Handle Empty Response
						if (data.payload == null) {
							callback(widgetObject, "No Data");
							return;
						}

						//--------------------
						// Handle Debug Response
						console.log("settings.DEBUG_MODE: "+settings.DEBUG_MODE)
						if(settings.DEBUG_MODE){
							
							console.log("a");
							var debugObject = data.payload[0];
							
							//-------------------------------
							// Build Info List	
							debugHTML = '<ul>';
								debugHTML += '<li><b>URL:&nbsp;</b>'+debugObject.URL+'</li>';
								debugHTML += '<li><b>Response Status:&nbsp;</b>'+debugObject.RESPONSE_STATUS+'</li>';
								debugHTML += '<li><b>Response Headers:&nbsp;</b>';
									debugHTML += '<ul>';
										for(var key in debugObject.RESPONSE_HEADERS){
											var value = debugObject.RESPONSE_HEADERS[key];
											debugHTML += '<li><b>'+key+':&nbsp;</b>'+value+'</li>';
										}
									debugHTML += '</li></ul>';
							debugHTML += '</li></ul>';
								
							//-------------------------------
							// Create Respones Body Code Block
							var pre = $("<pre>");
							var code = $("<code>");
							code.text(debugObject.RESPONSE_BODY == null ? "" : debugObject.RESPONSE_BODY)
							pre.append(code);
							
							//-------------------------------
							// Sewing Lesson: Stitch stuff together
							var debugInfoWrapper = $('<div id="debugInfo-widget-'+widgetObject.PK_ID+'">');	
							debugInfoWrapper.append(debugHTML);
							debugInfoWrapper.append('<p><b>Response Body:</b></p>');
							debugInfoWrapper.append(pre);
			
							console.log(debugInfoWrapper);
							
							callback(widgetObject, debugInfoWrapper);
							//callback(widgetObject, "Debug Info");
							return;
						}
						
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
							if(settings.disable) {
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

							}

						}

						// Copied from BlackboxExporter
						var dataToRender = {

							data: data.payload,
							bgstylefield: 'alertstyle',
							textstylefield: 'textstyle',
							titlefields: ['LABEL'],
							visiblefields: ['LABEL', 'URL', 'CHECK_TYPE', 'CHECK_FOR', 'CHECK_RESULT', 'STATUS_CODE', 'STATUS_CODE_VALID', 'STATUS_CODE_MESSAGE' ] ,
							labels: {
								URL: 'URL',
								STATUS_CODE_MESSAGE: 'Message'
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
									sizefactor: settings.sizefactor,
									showlabels: settings.showlabels,
									borderstyle: settings.borderstyle
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
						if(settings.renderer == "tiles"){
							dataToRender.visiblefields = ['CHECK_TYPE', 'STATUS_CODE'];
						}
						//-----------------------------------
						// Adjust RenderSettings for Table
						if(settings.renderer == "table"){
							dataToRender.bgstylefield = null;
							dataToRender.textstylefield = null;
						}

						//--------------------------
						// Create Tiles
						if(  data.payload == null || typeof data.payload == 'string'){
							callback(widgetObject, "unknown");
						}else{

							var renderType = settings.renderer;
							if(renderType == null){ renderType = 'tiles'}

							var alertRenderer = CFW.render.getRenderer(renderType.toLowerCase());
							callback(widgetObject, alertRenderer.render(dataToRender));
						}

					})

				}
			})

	}
)();