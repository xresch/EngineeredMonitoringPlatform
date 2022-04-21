package com.xresch.emp.features.exense.step;

import java.util.HashMap;
import java.util.Locale;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.features.dashboard.WidgetSettingsFactory;
import com.xresch.cfw.features.jobs.CFWJobTask;
import com.xresch.cfw.features.jobs.FeatureJobs;
import com.xresch.cfw.features.usermgmt.User;

public class CFWJobStepQueryStatus extends CFWJobTask {
	
	private WidgetStepQueryStatus widget = new WidgetStepQueryStatus();

	@Override
	public String uniqueName() {
		return "Alerting: Step";
	}

	@Override
	public String taskDescription() {
		return "Checks if any of the records retrieved from the Step database exceeds the specified thresholds.";
	}

	@Override
	public CFWObject getParameters() {
		return widget.createQueryAndThresholdFields()
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
		CFWObject paramsAndSettings = this.getParameters();
		
		paramsAndSettings.mapJobExecutionContext(context);
		
		widget.executeTask(context, paramsAndSettings, null, paramsAndSettings);
	}
	
}
