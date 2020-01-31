package com.pengtoolbox.emp.response;

import com.pengtoolbox.cfw.response.bootstrap.BTFooter;
import com.pengtoolbox.cfw.response.bootstrap.BTLink;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, © 2019 
 * @license Creative Commons: Attribution-NonCommercial-NoDerivatives 4.0 International
 **************************************************************************************************************/
public class UMWFooterFooter extends BTFooter {

	public UMWFooterFooter() {
		
		this.addChild(
				new BTLink("Support Info", "#")
				.addAttribute("data-toggle", "modal")
				.addAttribute("data-target", "#supportInfo")
			)
			.addChild(new BTLink("Custom", "./custom"))
			;
		
		
	}

}
