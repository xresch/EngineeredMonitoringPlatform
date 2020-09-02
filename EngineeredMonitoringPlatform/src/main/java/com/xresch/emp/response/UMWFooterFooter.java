package com.xresch.emp.response;

import com.xresch.cfw.response.bootstrap.BTFooter;
import com.xresch.cfw.response.bootstrap.BTLink;


/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2019 
 * @license MIT-License
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
