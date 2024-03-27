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
public class CFWQueryFunctionStepSetEnv extends CFWQueryFunction {

	public static final String FUNCTION_NAME = "stepsetenv";
	public static final String METAOBJECT_STEP_ENV = "MetaObject_EmpStep_Environment";
	
	public CFWQueryFunctionStepSetEnv(CFWQueryContext context) {
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
		return uniqueName()+"(environment)";
	}
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionShort() {
		return "Sets the environment used for all the step functions. Returns true if successful, false otherwise.";
	}
	
	/***********************************************************************************************
	 * 
	 ***********************************************************************************************/
	@Override
	public String descriptionSyntaxDetailsHTML() {
		return "<p><b>environment:&nbsp;</b>Specify the ID of the environment, either an integer, or a JSON object containing the field 'id'.</p>"
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
		
		//----------------------------------
		// Return empty string if no params
		if(parameters.size() == 0) { return QueryPartValue.newBoolean(false); }
		
		//----------------------------------
		// Get EnvironmentID
		QueryPartValue envValue = parameters.get(0);
		
		Integer id = null;
		if(envValue.isJsonObject()) {
			JsonElement idElement = envValue.getAsJsonObject().get("id");
			if(idElement != null && idElement.isJsonPrimitive()) {
				id = idElement.getAsInt();
			}
		}else if(envValue.isInteger()) {
			id = envValue.getAsInteger();
		}
		
		//----------------------------------
		// Get Instance
		if(id != null) {
			StepEnvironment env = StepEnvironmentManagement.getEnvironment(id);
			this.context.addMetaObject(METAOBJECT_STEP_ENV, env);
			
			return QueryPartValue.newBoolean(true); 
		}
		
		return QueryPartValue.newBoolean(false); 
	}
	
	
	/***********************************************************************************************
	 * Return the STEP environment for this query context.
	 ***********************************************************************************************/
	public static StepEnvironment getEnvironment(CFWQueryContext context) {
		
		StepEnvironment env = (StepEnvironment) context.getMetaObject(METAOBJECT_STEP_ENV);
		
		if(env == null) {
			context.addMessageWarning("function "+FUNCTION_NAME+"(): The step environment was null.");
		}
		
		return env;
	}

}
