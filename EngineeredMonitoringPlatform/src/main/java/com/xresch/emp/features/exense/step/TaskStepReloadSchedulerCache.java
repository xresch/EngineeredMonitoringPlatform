package com.xresch.emp.features.exense.step;

import java.util.logging.Logger;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.schedule.CFWScheduledTask;

public class TaskStepReloadSchedulerCache extends CFWScheduledTask {
	
	private static Logger logger = CFWLog.getLogger(TaskStepReloadSchedulerCache.class.getName());

	public void execute() {
		
		for(StepEnvironment env : StepEnvironmentManagement.getEnvironmentsAll().values()) {
			env.loadSchedulersCache();
		}
	}
	

}
