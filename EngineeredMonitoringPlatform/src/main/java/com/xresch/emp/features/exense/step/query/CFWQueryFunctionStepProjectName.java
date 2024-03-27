package com.xresch.emp.features.exense.step.query;

import java.util.ArrayList;
import java.util.TreeSet;

import com.google.gson.JsonElement;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;
import com.xresch.cfw.features.query.CFWQueryFunction;
import com.xresch.cfw.features.query.EnhancedJsonObject;
import com.xresch.cfw.features.query.FeatureQuery;
import com.xresch.cfw.features.query.parse.QueryPartValue;
import com.xresch.emp.features.exense.step.FeatureExenseStep;
import com.xresch.emp.features.exense.step.StepEnvironment;
import com.xresch.emp.features.exense.step.StepEnvironmentManagement;

/************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2023 
 * @license MIT-License
 ************************************************************************************************************/
public class CFWQueryFunctionStepProjectName extends CFWQueryFunction {

	public static final String FUNCTION_NAME = "stepprojectname";
	public static final String METAOBJECT_STEP_ENV = "MetaObject_EmpStep_Environment";
	
	public CFWQueryFunctionStepProjectName(CFWQueryContext context) {
		super(context);
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String uniqueName() {
		return FUNCTION_NAME;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public TreeSet<String> getTags(){
		TreeSet<String> tags = new TreeSet<>();
		tags.add(FeatureExenseStep.QUERY_FUNCTION_TAG);
		return tags;
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntax() {
		return uniqueName()+"(projectID)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Returns the name for the given ProjectID.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>projectID:&nbsp;</b>The id of the project.</p>"
			;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionHTML() {
		return CFW.Files.readPackageResource(FeatureQuery.PACKAGE_MANUAL+".functions", "manual_function_"+FUNCTION_NAME+".html");
	}


	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public boolean supportsAggregation() {
		return false;
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public void aggregate(EnhancedJsonObject object,ArrayList<QueryPartValue> parameters) {
		// not supported
	}

	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public QueryPartValue execute(EnhancedJsonObject object, ArrayList<QueryPartValue> parameters) {
		
		QueryPartValue result = QueryPartValue.newNull();
		//----------------------------------
		// Return empty string if no params
		if(parameters.size() == 0) { return result; }
		
		//----------------------------------
		// Get EnvironmentID
		QueryPartValue idValue = parameters.get(0);
		
		String id = null;
		if(idValue.isString()) {
			id = idValue.getAsString();
		}else {
			return result;
		}
		
		//----------------------------------
		// Get Instance
		StepEnvironment env = CFWQueryFunctionStepSetEnv.getEnvironment(context);
		if(env == null) {
			return result;
		}else {
			result = QueryPartValue.newString( env.getProjectName(id) );
			
		}
		
		return result; 
	}

}
