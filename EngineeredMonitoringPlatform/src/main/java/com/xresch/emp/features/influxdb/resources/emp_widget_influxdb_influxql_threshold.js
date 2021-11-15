
(function (){
		
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_influxdb_influxql_threshold",
		{
			category: "Monitoring | InfluxDB",
			menuicon: "fas fa-thermometer-half",
			menulabel: CFWL('emp_widget_influxdb_influxql_threshold', "InfluxQL Chart"),
			description: CFWL('emp_widget_influxdb_influxql_threshold', "This widget uses a InfluxQL query to fetch time series and displays them as a chart."), 
			usetimeframe: true,
			createWidgetInstance: function (widgetObject, params, callback) {
					
				CFW.dashboard.fetchWidgetData(widgetObject, params, function(data){
					//cfw_format_csvToObjectArray
					var settings = widgetObject.JSON_SETTINGS;				
					
					console.log('=========== Influx DB Data =========');
					console.log(data);
					
					//---------------------------------
					// Check for Data and Errors
					if(CFW.utils.isNullOrEmpty(data.payload) || data.payload.results == undefined ){
						callback(widgetObject, '');
						return;
					}

					//---------------------------------
					// Prepare InfluxDB data
					var labelFields = [];
					var valueColumn = settings.valuecolumn;
					var dataArray = emp_influxdb_convertInfluxQLToDataviewerStructure(data.payload, true);
					
					if( !CFW.utils.isNullOrEmpty(settings.labels) ){
						labelFields = settings.labels.trim().split(/[, ]+/);
					}
					
					console.log('=========== dataArray =========');
					console.log(dataArray);

					//---------------------------
					// Set Colors for Thresholds
					var excellentVal 	= settings.THRESHOLD_EXCELLENT;
					var goodVal 		= settings.THRESHOLD_GOOD;
					var warningVal 		= settings.THRESHOLD_WARNING;
					var emergencyVal 	= settings.THRESHOLD_EMERGENCY;
					var dangerVal 		= settings.THRESHOLD_DANGER;
					
					for(var key in dataArray){
						var current = dataArray[key];
						
						current.alertstyle =  CFW.colors.getThresholdStyle(current[valueColumn]
								,excellentVal
								,goodVal
								,warningVal
								,emergencyVal
								,dangerVal
								,settings.THRESHOLD_DISABLED);
						
						if(current.alertstyle != "cfw-none"){
							current.textstyle = "white"; 
						}
					}
					
					//---------------------------
					// Render Settings
					var renderType = (settings.renderer == null) ? "tiles" : settings.renderer.toLowerCase() ;

					var dataToRender = {
						data: dataArray,
						bgstylefield: 'alertstyle',
						textstylefield: 'textstyle',
						titlefields: labelFields, 
						visiblefields: [],
						titleformat: null, 
						
						labels: {},
						customizers: {
							value: function(record, value) {
								if(value == null) return '';
								return (settings.suffix == null) ? value : value+" "+settings.suffix;
							},
							time: function(record, value) { return (value != null) ? new  moment(value).format("YYYY-MM-DD HH:mm") : '';},
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
					// Adjust RenderSettings
					if(dataToRender.data.length > 0){
						if( (renderType == "table" 
							|| renderType == "panels"
							|| renderType == "cards"
							|| renderType == "csv"
							|| renderType == "json")){
							
							var visiblefields = Object.keys(dataToRender.data[0]);
							//remove alertstyle and textstyle
							visiblefields.pop();
							visiblefields.pop();
							dataToRender.visiblefields = visiblefields;
							// add first field to title
							dataToRender.titlefields.push(visiblefields[0]); 	
							dataToRender.titleformat = '{0} - {1}';
						}
					}
					
					
					//--------------------------
					// Render Widget
					var alertRenderer = CFW.render.getRenderer(renderType);
					callback(widgetObject, alertRenderer.render(dataToRender));
				});
			},
			
			getEditForm: function (widgetObject) {
				return CFW.dashboard.getSettingsForm(widgetObject);
			},
			
			onSave: function (form, widgetObject) {
				widgetObject.JSON_SETTINGS = CFW.format.formToObject(form);
				return true;	
			}
		}
	);	
	
})();