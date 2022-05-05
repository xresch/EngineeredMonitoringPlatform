package com.xresch.emp.features.exense.step;

import java.util.HashMap;
import java.util.Locale;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.dashboard.CFWJobTaskWidgetTaskExecutor;
import com.xresch.cfw.features.dashboard.WidgetSettingsFactory;
import com.xresch.cfw.features.jobs.CFWJobTask;
import com.xresch.cfw.features.jobs.FeatureJobs;
import com.xresch.cfw.features.usermgmt.User;

public class CFWJobStepQueryStatus extends CFWJobTask {
	
	private WidgetPlanStatusByProject widget = new WidgetPlanStatusByProject();

	@Override
	public String uniqueName() {
		return "Alerting: Step Plans Status by Project";
	}

	@Override
	public String taskDescription() {
		return widget.getTaskDescription();
	}

	@Override
	public CFWObject getParameters() {
		return new CFWObject()
				.addField(CFWJobTaskWidgetTaskExecutor.createOffsetMinutesField())
				.addAllFields(widget.createQueryAndThresholdFields().getFields())
				.addField(WidgetSettingsFactory.createSampleDataField())
				.addAllFields(widget.getTasksParameters().getFields())
			;
	}

	@Override
	public int minIntervalSeconds() {
		return 15;
	}

	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		
		return widget.getLocalizationFiles();
	}
	
	@Override
	public boolean hasPermission(User user) {
		
		if(user.hasPermission(FeatureJobs.PERMISSION_JOBS_USER) 
		&& user.hasPermission(FeatureExenseStep.PERMISSION_STEP) ) {
			return true;
		}
		
		return false;
	}

	@Override
	public void executeTask(JobExecutionContext context) throws JobExecutionException {
		
		JobDataMap data = context.getMergedJobDataMap();
		CFWObject paramsAndSettings = this.getParameters();
		
		paramsAndSettings.mapJobExecutionContext(context);

		//------------------------------
		// Job Settings 
		
		CFWTimeframe offset = (CFWTimeframe)paramsAndSettings.getField(CFWJobTaskWidgetTaskExecutor.PARAM_TIMEFRAME_OFFSET).getValue();
		data.put("earliest", offset.getEarliest()); 
		data.put("latest", offset.getLatest()); 
		
		widget.executeTask(context, paramsAndSettings, null, paramsAndSettings);
		
	}
	
	
}
