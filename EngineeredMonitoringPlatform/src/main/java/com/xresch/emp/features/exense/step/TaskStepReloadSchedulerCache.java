package com.xresch.emp.features.exense.step;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.xresch.cfw.logging.CFWLog;
import com.xresch.cfw.schedule.CFWScheduledTask;

public class TaskStepReloadSchedulerCache extends CFWScheduledTask {
	
	private static Logger logger = CFWLog.getLogger(TaskStepReloadSchedulerCache.class.getName());

	public void execute() {
		
		CFWLog log = new CFWLog(logger).start(); 
		
			for(StepEnvironment env : StepEnvironmentManagement.getEnvironmentsAll().values()) {
				env.loadSchedulersCache();
			}
			
		log.end(Level.INFO, "Finished reloading Step Schedulers Caches");
	}
	

}
