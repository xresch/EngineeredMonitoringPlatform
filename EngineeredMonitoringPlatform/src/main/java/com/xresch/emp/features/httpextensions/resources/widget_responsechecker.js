(
    function() {

        // Register your category, will be ignored if a category with this name already exists
        CFW.dashboard.registerCategory("fas fa-flask", "Monitoring");

        // Register the widget with an unique name - must be the same name specified as in the java widget class
        CFW.dashboard.registerWidget("emp_web_responsechecker",
            {
                category: 'Monitoring',
                menuicon: 'fas fa-flask',
                menulabel: 'Response Checker',
                description: CFWL('cfw_widget_responseChecker_url_desc', 'Takes an URL and performs' +
                ' different kinds of checks on the response.'),
                createWidgetInstance: function (widgetObject, params, callback) {

                    // This method is used to fetch data from serverside for this widget. This request will be forwarded
                    // to your serverside WidgetDefinition.fetchData() method.

                    // Callback function
                    callback(widgetObject, "");

                    CFW.dashboard.fetchWidgetData(widgetObject, params, function(data) {

                        var result = data.payload;

                        for(var key in result){
                            var current = result[key];
                            var success = current.success;
                            current.textstyle = "white";
                            if(success == null){
                                current.success = "UNKNOWN";
                            }

                            //--------------------
                            // Check Disabled
                            if(widgetObject.JSON_SETTINGS.disable) {
                                current.alertstyle = "cfw-darkgray";
                                continue;
                            }


                            //--------------------
                            // Get Status Color
                            switch (current.success) {
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
                            visiblefields: ["URL", 'responseStatus', 'success', 'matchType', 'matchString'],
                            labels: {
                                URL: 'URL',
                            },
                            customizers: {
                                "URL": function(record, value){
                                    return '<a class="text-white" target="_blank" href="'+value+'">'+value+'</a>';
                                }
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
                        if(widgetObject.JSON_SETTINGS.renderer == "Table"){

                            // Customise
                            //dataToRender.visiblefields = ['URL', 'CheckType', 'Match_For', 'ResponseStatusCode'];

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