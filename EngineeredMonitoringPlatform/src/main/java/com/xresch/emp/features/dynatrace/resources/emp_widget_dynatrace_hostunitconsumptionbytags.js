
(function (){
	/******************************************************************
	 * 
	 ******************************************************************/
	function emp_widget_dynatrace_calculateTagConsumption(dataArray,  tagsfilter) {
		
		//-------------------------------------
		// Initialize Variables
		let statistics = {
			hostcount: 0,
			filteredhostcount: 0,
			hostunitssum: 0,
			filteredsum: 0,
			fulltotals: {},
			distributedtotals:  {},
		}
		
		let regexObj = null;
		if(!CFW.utils.isNullOrEmpty(tagsfilter)){
			regexObj = new RegExp(tagsfilter, ''); 
		}
		
		//-------------------------------------
		// Filter Tags
		for(let hostIndex in dataArray){
			let currentHost = dataArray[hostIndex];
			currentHost.filteredTags = [];
			
			for(let tagIndex in currentHost.tags){
				currentTag = currentHost.tags[tagIndex];
				let tagString = currentTag.key + ((currentTag.value != null) ? ":"+currentTag.value : "");
				if(CFW.utils.isNullOrEmpty(tagsfilter) 
				|| regexObj.exec(tagString) !== null){
					currentHost.filteredTags.push(tagString);
				}
			}

		}

		//-------------------------------------
		// Calculate statistics
		statistics.hostcount = dataArray.length;
		for(let hostIndex in dataArray){
			let currentHost = dataArray[hostIndex];
			let hostUnits = currentHost.consumedHostUnits;
			let filteredTagsCount = currentHost.filteredTags.length;
			
			statistics.hostunitssum += hostUnits;
			if(filteredTagsCount != 0){
				statistics.filteredsum += hostUnits;
				statistics.filteredhostcount += 1;
			}
			
			for(let tagIndex in currentHost.filteredTags){
				let currentTagString = currentHost.filteredTags[tagIndex];
				
				if(statistics.fulltotals[currentTagString] == undefined){
					statistics.fulltotals[currentTagString] = hostUnits;
					statistics.distributedtotals[currentTagString] = hostUnits / filteredTagsCount;
				}else{
					statistics.fulltotals[currentTagString] += hostUnits;
					statistics.distributedtotals[currentTagString] += hostUnits / filteredTagsCount;
				}

			}

		}
		
		return statistics
	}
	
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("emp_dynatrace_hostunitconsumptionbytags",
		{
			category: DYNATRACE_WIDGET_CATEGORY,
			menuicon: "fas fa-file-invoice-dollar",
			menulabel: CFWL('emp_widget_dynatrace_hostunitconsumptionbytags', "Host Unit Consumption By Tags"),
			description: CFWL('emp_widget_dynatrace_hostunitconsumptionbytags_desc', "Calculates the host unit consumption by tags. This widget can be useful for accounting and finding out how many units are consumed by which application."), 
			usetimeframe: true,

			createWidgetInstance: function (widgetObject, params, callback) {
					
				CFW.dashboard.fetchWidgetData(widgetObject, function(data){
					
					var settings = widgetObject.JSON_SETTINGS;				
					
					//---------------------------------
					// Check for Data and Errors
					if(CFW.utils.isNullOrEmpty(data.payload) || typeof data.payload == 'string' || data.payload.length == null){
						callback(widgetObject, '');
						return;
					}
					
					//---------------------------
					// Prepare Statistics
					var consumptionStatistics = emp_widget_dynatrace_calculateTagConsumption(data.payload, settings.tagsfilter);
					
					var targetDiv = $('<div class="w-100">');
					targetDiv.append('<h4>Totals</h4>');
					targetDiv.append('<p>The total host units and the number of units considered after filtering(useful in case you want to verify all host units are considered in the filter).</p>');

					targetDiv.append('<ul>'
								+'<li><strong>Tag Filter: </strong>"'+settings.tagsfilter+'" </li>'
								+'<li> <strong>Total Hosts: </strong>'+consumptionStatistics.hostcount+' </li>'
								+'<li> <strong>Hosts Counter after Filtering: </strong>'+consumptionStatistics.filteredhostcount+' </li>'
								+'<li> <strong>Total Units: </strong>'+consumptionStatistics.hostunitssum+' </li>'
								+'<li> <strong>Filtered Units: </strong>'+consumptionStatistics.filteredsum+' </li>'
							+'</ul>')
					
					//---------------------------
					// Render Distribution Table
					var dataToRender = {
						data: consumptionStatistics.distributedtotals,
						labels: { name: "Tag", value: "Units"},
						customizers: { value: function(record, value) {  return value.toFixed(3); } },
						rendererSettings:{ table: { verticalize: true, verticalizelabelize: false, narrow: true, filterable: false}}
					};
					
					var renderer = CFW.render.getRenderer('table');
					targetDiv.append('<h4>Distributed Totals</h4>');
					targetDiv.append('<p>If more than one tag per host matched the filter criteria, the host units are distributed equally between the tags.</p>');
					
					targetDiv.append(renderer.render(dataToRender));
					
					//---------------------------
					// Render Totals Table
					var dataToRender = {
						data: consumptionStatistics.fulltotals,
						labels: { name: "Tag", value: "Units"},
						customizers: { value: function(record, value) {  return value.toFixed(3); } },
						rendererSettings:{ table: { verticalize: true, verticalizelabelize: false, narrow: true, filterable: false}}
					};
					
					var renderer = CFW.render.getRenderer('table');
					targetDiv.append('<h4>Full Totals</h4>');
					targetDiv.append('<p>The full unit consumption of the host is added to each tag matching the search criteria.</p>');
					
					targetDiv.append(renderer.render(dataToRender));
					callback(widgetObject, targetDiv);
				});
			},
			
		}
	);	
	
})();