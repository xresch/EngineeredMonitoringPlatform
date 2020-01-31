package com.pengtoolbox.emp._main;

import java.util.logging.Logger;

import org.eclipse.jetty.servlet.ServletContextHandler;

import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw._main.CFWAppInterface;
import com.pengtoolbox.cfw._main.CFWApplication;
import com.pengtoolbox.cfw.logging.CFWLog;
import com.pengtoolbox.emp.features.theusinator.FeatureTheusinator;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, � 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class Main implements CFWAppInterface {
	
	public static Logger logger = CFWLog.getLogger(Main.class.getName());
	protected static CFWLog log = new CFWLog(logger);
	
    public static void main( String[] args ) throws Exception
    {
    	CFW.initializeApp(new Main(), args);
    }
    
	@Override
	public void register() {
    	
		//----------------------------------
		// Register Objects
    	CFW.Registry.Features.addFeature(FeatureTheusinator.class);
    	
	}

	@Override
	public void initializeDB() {
		
	}
	
	@Override
	public void stopApp() {
		
	}
	
	@Override
	public void startApp(CFWApplication app) {
	    	
	        //###################################################################
	        // Create API ServletContext, no login needed
	        //################################################################### 
	    	ServletContextHandler apiContext = app.getUnsecureContext("/api");
	    	
	        //###################################################################
	        // Create authenticatedServletContext
	        //###################################################################    	
	    	ServletContextHandler appContext = app.getSecureContext();
	        	        
	        //###################################################################
	        // Startup
	        //###################################################################
	        app.setDefaultURL("/app/theusinator?env=preprod");
	        
	        try {
				app.start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}


	@Override
	public void startTasks() {
		// TODO Auto-generated method stub
		
	}

}
