package com.xresch.emp.features.exense.step;

import java.util.HashMap;
import java.util.Locale;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.features.dashboard.CFWJobTaskWidgetTaskExecutor;
import com.xresch.cfw.features.dashboard.widgets.WidgetSettingsFactory;
import com.xresch.cfw.features.jobs.CFWJobTask;
import com.xresch.cfw.features.jobs.FeatureJobs;
import com.xresch.cfw.features.usermgmt.User;
import com.xresch.cfw.utils.CFWMonitor;

public class CFWJobTaskSchedulerStatusByProject extends CFWJobTask {
	
	private WidgetSchedulerStatusByProject widget = new WidgetSchedulerStatusByProject();

	@Override
	public String uniqueName() {
		return "Alerting: Step Scheduler Status by Project";
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
	public void executeTask(JobExecutionContext context, CFWMonitor monitor) throws JobExecutionException {
		
		CFWObject jobsettings = this.getParameters();
		jobsettings.mapJobExecutionContext(context);
		
		CFWTimeframe offset = CFWJobTaskWidgetTaskExecutor.getOffsetFromJobSettings(jobsettings); 
		
		widget.executeTask(context, jobsettings, null, jobsettings, monitor, offset);
		
	}
	
	
}
