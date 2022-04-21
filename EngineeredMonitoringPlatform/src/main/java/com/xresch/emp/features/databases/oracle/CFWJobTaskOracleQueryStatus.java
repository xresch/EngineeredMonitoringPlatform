package com.xresch.emp.features.databases.oracle;

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

public class CFWJobTaskOracleQueryStatus extends CFWJobTask {
	
	private WidgetOracleQueryStatus widget = new WidgetOracleQueryStatus();

	@Override
	public String uniqueName() {
		return "Alerting: Oracle";
	}

	@Override
	public String taskDescription() {
		return "Checks if any of the records retrieved with the database query exceeds the specified thresholds.";
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
		&& user.hasPermission(FeatureOracle.PERMISSION_ORACLE) ) {
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
