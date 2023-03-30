package com.xresch.emp.features.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonObject;
import com.xresch.cfw.caching.FileDefinition;
import com.xresch.cfw.caching.FileDefinition.HandlingType;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWObject;
import com.xresch.cfw.datahandling.CFWTimeframe;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.dashboard.widgets.WidgetDefinition;
import com.xresch.cfw.features.dashboard.widgets.WidgetDataCache.WidgetDataCachePolicy;
import com.xresch.cfw.response.JSONResponse;
import com.xresch.cfw.validation.LengthValidator;
import com.xresch.emp.features.common.FeatureEMPCommon;
import com.xresch.emp.features.webex.FeatureWebex;

public class WidgetCustomThresholdLegend extends WidgetDefinition {

	@Override
	public String getWidgetType() {return "emp_customthresholdlegend";}


	@Override
	public WidgetDataCachePolicy getCachePolicy() {
		return WidgetDataCachePolicy.OFF;
	}
	
	@Override
	public CFWObject getSettings() {
		LengthValidator validator = new LengthValidator(0, 1024);
		return new CFWObject()
				.addField(CFWField.newString(FormFieldType.TEXT, "labelexcellent")
						.setLabel("{!emp_customthresholdlegend_labelexcellent!}")
						.setDescription("{!emp_customthresholdlegend_labelexcellent_desc!}")
						.addValidator(validator)
						.setValue("Excellent")
				)
				.addField(CFWField.newString(FormFieldType.TEXT, "labelgood")
						.setLabel("{!emp_customthresholdlegend_labelgood!}")
						.setDescription("{!emp_customthresholdlegend_labelgood_desc!}")
						.addValidator(validator)
						.setValue("Good")
				)
				.addField(CFWField.newString(FormFieldType.TEXT, "labelwarning")
						.setLabel("{!emp_customthresholdlegend_labelwarning!}")
						.setDescription("{!emp_customthresholdlegend_labelwarning_desc!}")
						.addValidator(validator)
						.setValue("Warning")
				)
				.addField(CFWField.newString(FormFieldType.TEXT, "labelemergency")
						.setLabel("{!emp_customthresholdlegend_labelemergency!}")
						.setDescription("{!emp_customthresholdlegend_labelemergency_desc!}")
						.addValidator(validator)
						.setValue("Emergency")
				)
				.addField(CFWField.newString(FormFieldType.TEXT, "labeldanger")
						.setLabel("{!emp_customthresholdlegend_labeldanger!}")
						.setDescription("{!emp_customthresholdlegend_labeldanger_desc!}")
						.addValidator(validator)
						.setValue("Danger")
				)
				.addField(CFWField.newString(FormFieldType.TEXT, "labelgray")
						.setLabel("{!emp_customthresholdlegend_labelgray!}")
						.setDescription("{!emp_customthresholdlegend_labelgray_desc!}")
						.addValidator(validator)
						.setValue("Unknown/No Data")
				)
				.addField(CFWField.newString(FormFieldType.TEXT, "labeldarkgray")
						.setLabel("{!emp_customthresholdlegend_labeldarkgray!}")
						.setDescription("{!emp_customthresholdlegend_labeldarkgray_desc!}")
						.addValidator(validator)
						.setValue("Disabled")
				)
				;
	}

	@Override
	public void fetchData(HttpServletRequest request, JSONResponse response, CFWObject settings, JsonObject jsonSettings, CFWTimeframe timeframe) { /* do nothing */ }

	@Override
	public ArrayList<FileDefinition> getJavascriptFiles() {
		FileDefinition js = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureEMPCommon.PACKAGE_RESOURCE, "emp_widget_customthresholdlegend.js");
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add(js);
		return array;
	}

	@Override
	public ArrayList<FileDefinition> getCSSFiles() { 
		
		FileDefinition css = new FileDefinition(HandlingType.JAR_RESOURCE, FeatureEMPCommon.PACKAGE_RESOURCE, "emp_widgets.css");
		ArrayList<FileDefinition> array = new ArrayList<FileDefinition>();
		array.add(css);
		return array; 
		
	}

	@Override
	public HashMap<Locale, FileDefinition> getLocalizationFiles() {
		HashMap<Locale, FileDefinition> map = new HashMap<Locale, FileDefinition>();
		map.put(Locale.ENGLISH, new FileDefinition(HandlingType.JAR_RESOURCE, FeatureEMPCommon.PACKAGE_RESOURCE, "lang_en_emp_common.properties"));
		return map;
	}

}
