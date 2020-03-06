package com.pengtoolbox.emp.features.theusinator;

import com.pengtoolbox.cfw._main.CFW;

public class STMAPI {
	
	public enum Environment{
		PROD, PRE_PROD
	}
	
	private static String prodSessionID = "";
	private static String preprodSessionID = "";
	
	private static String getSessionID(Environment env) {
		
		if(env == Environment.PROD) {
			return prodSessionID;
		}else {
			return preprodSessionID;
		}
	}
	
	
	//https://stm-uat.juliusbaer.com/services/sccentities?method=getUsers&sessionId=7964335573333709589&login=u42137
	
	public static void logonAndRetrieveSession(Environment environment) {
		
		//-------------------------
		// Retrieve Credentials
		String spmUser = CFW.DB.Config.getConfigAsString(FeatureTheusinator.CONFIG_SPM_PREPROD_APIUSER);
		String spmPassword = CFW.DB.Config.getConfigAsString(FeatureTheusinator.CONFIG_SPM_PREPROD_PASSWORD);
		

		if(environment == Environment.PROD) {
			spmUser = CFW.DB.Config.getConfigAsString(FeatureTheusinator.CONFIG_SPM_PROD_APIUSER);
			spmPassword = CFW.DB.Config.getConfigAsString(FeatureTheusinator.CONFIG_SPM_PROD_PASSWORD);
		}
		
		//-------------------------
		// Check is configured
		if(spmUser == null || spmUser.isEmpty()) {
			return;
		}
		
		//-------------------------
		// Call API
		String result = callAPI(environment, "sccsystem", "logonUser", 
				CFW.HTTP.encode("username", spmUser) 
				+ CFW.HTTP.encode("plainPW", spmPassword));
		
		System.out.println("===== Session reply =====");
		System.out.println(result);
		
		//-------------------------
		// Save SessionID
		
		if(environment == Environment.PROD) {
			
		}else {
			
		}
		
	}
	
	/*************************************************************************************
	 * 
	 *************************************************************************************/
	public static String callAPI(Environment environment, String service, String method, String queryString) {
		

		//-------------------------
		// Retrieve URL
		String spmURL = CFW.DB.Config.getConfigAsString(FeatureTheusinator.CONFIG_SPM_PREPROD_URL);

		if(environment == Environment.PROD) {
			spmURL = CFW.DB.Config.getConfigAsString(FeatureTheusinator.CONFIG_SPM_PROD_URL);
		}
		
		if(spmURL == null || spmURL.isEmpty()) {
			return null;
		}
		
		//-------------------------
		// Add method to URL
		queryString = CFW.HTTP.encode("method", method).replaceFirst("&","?") + queryString;
		
		//-------------------------
		// Add SessionID to URL
		if(!method.equals("logonUser")) {
			queryString = CFW.HTTP.encode("sessionId", getSessionID(environment)) + queryString;
		}
		
		//-------------------------
		// Get Result
		return sendGETRequest(spmURL+"/services/"+service+"?"+queryString);
												
	}
	
	
	/*************************************************************************************
	 * 
	 *************************************************************************************/
	private static String sendGETRequest(String url) {
		
		return CFW.HTTP.sendGETRequest(url);

	}

}
