package com.pengtoolbox.emp._main;

import java.util.logging.Logger;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw._main.CFWAppInterface;
import com.pengtoolbox.cfw._main.CFWApplicationExecutor;
import com.pengtoolbox.cfw.features.manual.ManualPage;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.emp.features.environments.FeatureEnvironments;
import com.pengtoolbox.emp.features.theusinator.FeatureTheusinator;
import com.pengtoolbox.emp.features.widgets.FeatureEMPWidgets;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, � 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class Main implements CFWAppInterface {
	
	public static Logger logger = CFWLog.getLogger(Main.class.getName());
	protected static CFWLog log = new CFWLog(logger);
	
	public static final ManualPage TOP_MANUAL_PAGE = CFW.Registry.Manual.addManualPage(null, new ManualPage("Engineered Montitoring Platform(EMP)").faicon("fa fa-desktop"));
    public static void main( String[] args ) throws Exception
    {
    	CFW.initializeApp(new Main(), args);
    }
    
	@Override
	public void register() {
    	
		//----------------------------------
		// Register Objects
		CFW.Registry.Features.addFeature(FeatureEnvironments.class);
    	CFW.Registry.Features.addFeature(FeatureTheusinator.class);
    	CFW.Registry.Features.addFeature(FeatureEMPWidgets.class);
    	
		//----------------------------------
		// Register Manual PAges
		
	}

	@Override
	public void initializeDB() {
		
	}
	
	@Override
	public void stopApp() {
		
	}
	
	@Override
	public void startApp(CFWApplicationExecutor app) {
	    	
	        	        
	        //###################################################################
	        // Startup
	        //###################################################################
	        app.setDefaultURL("/dashboard/list", true);
	        
	        try {
				app.start();
			} catch (Exception e) {
				new CFWLog(logger)
				.method("startApp")
				.severe("Exception occured during startup.", e);
			}
		
	}


	@Override
	public void startTasks() {
		// TODO Auto-generated method stub
		
	}

}
