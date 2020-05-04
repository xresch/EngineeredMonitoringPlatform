package com.pengtoolbox.emp.features.theusinator;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
	
import com.pengtoolbox.cfw._main.CFW;
import com.pengtoolbox.cfw.caching.FileDefinition.HandlingType;
import com.pengtoolbox.cfw.features.contextsettings.ContextSettings;
import com.pengtoolbox.cfw.response.HTMLResponse;
import com.pengtoolbox.cfw.response.PlaintextResponse;
import com.pengtoolbox.cfw.response.bootstrap.AlertMessage.MessageType;
import com.pengtoolbox.emp.features.environments.SPMEnvironment;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, � 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class ServletTheusinator extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	
	public ServletTheusinator() {
	
	}
	
	/*****************************************************************
	 *
	 ******************************************************************/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
		if( CFW.Context.Request.hasPermission(FeatureTheusinator.PERMISSION_THEUSINATOR) ){
			
			String service = request.getParameter("service");
			
			if(service == null) {
				HTMLResponse html = new HTMLResponse("Theusinator SPM Dashboard");
				StringBuffer content = html.getContent();
	
				html.addCSSFile(HandlingType.JAR_RESOURCE, FeatureTheusinator.RESOURCE_PACKAGE, "theusinator.css");
				
				//html.addJSFileBottomSingle(new FileDefinition(HandlingType.JAR_RESOURCE, FileDefinition.CFW_JAR_RESOURCES_PATH+".js", "cfw_usermgmt.js"));
				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureTheusinator.RESOURCE_PACKAGE, "plotly.min.js");
				//html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureTheusinator.RESOURCE_PACKAGE, "jquery-ui.min.js");
				html.addJSFileBottom(HandlingType.JAR_RESOURCE, FeatureTheusinator.RESOURCE_PACKAGE, "theusinator.js");
				//html.addJSFileBottomAssembly(HandlingType.JAR_RESOURCE, FeatureTheusinator.RESOURCE_PACKAGE, "spm_custom.js");
				
				content.append(CFW.Files.readPackageResource(FeatureTheusinator.RESOURCE_PACKAGE, "theusinator.html"));
				
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
		
		SPMEnvironment spm = new SPMEnvironment();
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
		
		String result = CFW.HTTP.sendGETRequest(url);
		plaintext.getContent().append(result);	
		//System.out.println("URL: "+url);
		//System.out.println("result: "+result);
	}
	
}