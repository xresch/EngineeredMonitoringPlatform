package com.xresch.emp.features.spm;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xresch.cfw._main.CFW;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.features.contextsettings.ContextSettings;
import com.xresch.cfw.response.HTMLResponse;
import com.xresch.cfw.response.PlaintextResponse;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;
import com.xresch.cfw.utils.CFWHttp.CFWHttpResponse;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
 **************************************************************************************************************/
public class ServletTheusinator extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		if( CFW.Context.Request.hasPermission(FeatureSPM.PERMISSION_THEUSINATOR) ){
			
			String service = request.getParameter("service");
			
			if(service == null) {
				HTMLResponse html = new HTMLResponse("Theusinator SPM Dashboard");
				StringBuilder content = html.getContent();
	
				html.addCSSFile(HandlingType.JAR_RESOURCE, FeatureSPM.RESOURCE_PACKAGE, "theusinator.css");
				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureSPM.RESOURCE_PACKAGE, "plotly.min.js");
				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureSPM.RESOURCE_PACKAGE, "theusinator.js");

				content.append(CFW.Files.readPackageResource(FeatureSPM.RESOURCE_PACKAGE, "theusinator.html"));
				
				html.addJavascriptCode("startTheusinator();");
				
		        response.setContentType("text/html");
		        response.setStatus(HttpServletResponse.SC_OK);
			}else {
				handleDataRequest(request, response);
			}
			
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, CFW.L("cfw_core_error_accessdenied", "Access Denied!"));
		}
        
    }
	
	private void handleDataRequest(HttpServletRequest request, HttpServletResponse response) {
		
		PlaintextResponse plaintext = new PlaintextResponse();
		
		String service = request.getParameter("service");
		String method = request.getParameter("method");
		String sessionID = request.getParameter("sessionId");
		String env = request.getParameter("env");

		ContextSettings settings = CFW.DB.ContextSettings.selectByID(env);
		
		if(settings == null) {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The SPM environment seems not to be configured correctly.");
			return;
		}
		
		EnvironmentSPM spm = new EnvironmentSPM();
		spm.mapJsonFields(settings.settings());
						
		String queryString = request.getQueryString().replaceAll("service=.*?&|&service=.*?", "")
									.replaceAll("env=.*?&|&env=.*?", "");
		
		switch(service.toLowerCase()) {
		
			case "sccsystem": 			
				
				switch(method.toLowerCase()) {
					case "logonuser": 			sendGETRequest(request, plaintext, spm.url()+"/services/sccsystem?method=logonUser&username="+URLEncoder.encode(spm.apiUser())+"&plainPW="+URLEncoder.encode(spm.apiUserPassword()));			
												break;
	  										
					default: 					sendGETRequest(request, plaintext, spm.url()+"/services/"+service+"?"+queryString);	
												break;
				}
				break;
				
			case "sccentities": 			
				switch(method.toLowerCase()) {
					case "getcurrentuser": 			String username = CFW.Context.Request.getUser().username();
													sendGETRequest(request, plaintext, spm.url()+"/services/sccentities?method=getUsers&sessionId="+sessionID+"&login="+username);				
													break;
						
					
					default: 						sendGETRequest(request, plaintext, spm.url()+"/services/"+service+"?"+queryString);	
													break;
				}
				break;	


				
			default: 			sendGETRequest(request, plaintext, spm.url()+"/services/"+service+"?"+queryString);	
								break;
								
		}
		
	}
		
	/*************************************************************************************
	 * 
	 *************************************************************************************/
	private static void sendGETRequest(HttpServletRequest request, PlaintextResponse plaintext, String url) {
		
		CFWHttpResponse result = CFW.HTTP.sendGETRequest(url);
		if(result.getStatus() <= 299) {
			plaintext.getContent().append(result.getResponseBody());	
		} else {
			CFW.Context.Request.addAlertMessage(MessageType.WARNING, "The SPM API Call returned the HTTP Status "+result.getStatus());
		}
	}
	
}