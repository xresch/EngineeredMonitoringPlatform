package com.xresch.emp.features.mongodb;

import java.util.ArrayList;
import java.util.Arrays;

import org.bson.BsonArray;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.google.common.base.Strings;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.datahandling.CFWField;
import com.xresch.cfw.datahandling.CFWField.FormFieldType;
import com.xresch.cfw.features.contextsettings.AbstractContextSettings;
import com.xresch.cfw.features.core.AutocompleteList;
import com.xresch.cfw.features.core.AutocompleteResult;
import com.xresch.cfw.features.dashboard.DashboardWidget;
import com.xresch.cfw.features.dashboard.DashboardWidget.DashboardWidgetFields;
import com.xresch.cfw.response.bootstrap.AlertMessage.MessageType;

/**************************************************************************************************************
 * 
 * @author Reto Scheiwiller, (c) Copyright 2022
 * @license MIT-License
 **************************************************************************************************************/
public class MongoDBEnvironment extends AbstractContextSettings {
	
	public static final String SETTINGS_TYPE = "MongoDB Environment";
	
	private MongoDatabase dbInstance = null;
	
	public enum MongoDBEnvironmentFields{
		DB_HOST,
		DB_PORT,
		DB_NAME,
		DB_USER,
		DB_PASSWORD,
		TIME_ZONE,
	}
		
	protected CFWField<String> dbHost = CFWField.newString(FormFieldType.TEXT, MongoDBEnvironmentFields.DB_HOST)
			.setDescription("The server name of the database host.");
	
	protected CFWField<Integer> dbPort = CFWField.newInteger(FormFieldType.NUMBER, MongoDBEnvironmentFields.DB_PORT)
			.setDescription("The port used to access the database.")
			.setValue(27017);
	
	protected CFWField<String> dbName = CFWField.newString(FormFieldType.TEXT, MongoDBEnvironmentFields.DB_NAME)
			.setDescription("The name of the database.");
	
	protected CFWField<String> dbUser = CFWField.newString(FormFieldType.TEXT, MongoDBEnvironmentFields.DB_USER)
			.setDescription("The name of the user for accessing the database.");
	
	protected CFWField<String> dbPassword = CFWField.newString(FormFieldType.PASSWORD, MongoDBEnvironmentFields.DB_PASSWORD)
			.setDescription("The password of the DB user.")
			.disableSanitization()
			.enableEncryption("mongodb_DB_PW_Salt");
	
	protected CFWField<String> timezone = CFWField.newString(FormFieldType.TIMEZONEPICKER, MongoDBEnvironmentFields.TIME_ZONE)
			.setDescription("The timezone the database is using. Needed to manage differences from GMT properly.");
	
	public MongoDBEnvironment() {
		initializeFields();
	}
		
	protected void initializeFields() {
		this.addFields(dbHost, dbPort, dbName, dbUser, dbPassword, timezone);
	}
		
	
	@Override
	public boolean isDeletable(int id) {
		
		int count = new DashboardWidget()
			.selectCount()
			.whereLike(DashboardWidgetFields.JSON_SETTINGS, "%\"environment\":"+id+"%")
			.and().like(DashboardWidgetFields.TYPE, "emp_mongodb%")
			.executeCount();
		
		if(count == 0) {
			return true;
		}else {
			CFW.Context.Request.addAlertMessage(MessageType.ERROR, "The MongoDB Environment cannot be deleted as it is still in use by "+count+"  widget(s).");
			return false;
		}

	}
	
	public boolean isDBDefined() {
		if(dbHost.getValue() != null
		&& dbPort.getValue() != null
		&& dbName.getValue() != null) {
			return true;
		}
		
		return false;
	}
			
	public String dbHost() {
		return dbHost.getValue();
	}
	
	public MongoDBEnvironment dbHost(String value) {
		this.dbHost.setValue(value);
		return this;
	}
		
	public int dbPort() {
		return dbPort.getValue();
	}
	
	public MongoDBEnvironment dbPort(int value) {
		this.dbPort.setValue(value);
		return this;
	}
	
	public String dbName() {
		return dbName.getValue();
	}
	
	public MongoDBEnvironment dbName(String value) {
		this.dbName.setValue(value);
		return this;
	}
	
	public String dbUser() {
		return dbUser.getValue();
	}
	
	public MongoDBEnvironment dbUser(String value) {
		this.dbUser.setValue(value);
		return this;
	}
	
	public String dbPassword() {
		return dbPassword.getValue();
	}
	
	public MongoDBEnvironment dbPassword(String value) {
		this.dbPassword.setValue(value);
		return this;
	}	
	
	public String timezone() {
		return timezone.getValue();
	}
	
	public MongoDBEnvironment timezone(String value) {
		this.timezone.setValue(value);
		return this;
	}

	public MongoDatabase getMongoDB() {
		return dbInstance;
	}

	public void setMongoDB(MongoDatabase dbInstance) {
		this.dbInstance = dbInstance;
	}
	
	/*********************************************************************
	 * Create autocomplete for collections.
	 *********************************************************************/
    public AutocompleteResult autocompleteCollection(String searchValue, int maxResults) {
    	MongoDatabase mongoDB = this.getMongoDB();
    	
    	String lowerSearch = searchValue.toLowerCase();
    	AutocompleteList list = new AutocompleteList();
    	
    	int count = 0;
    	for(String name : mongoDB.listCollectionNames()) {
    		if(name.toLowerCase().contains(lowerSearch)) {
    			list.addItem(name, name);
    			count++;
    			if(count >= maxResults) {
    				break;
    			}
    		}
    	}
    	
    	return new AutocompleteResult(list);
    }
    
	/*********************************************************************
	 * Executes a MongoDB find on the given collection and the corresponding
	 * filtering documents.
	 * @param collectionName
	 * @param findDocString
	 * @param sortDocString, can be null or empty
	 * @param limit positive value to limit, 0 or negative to ignore
	 * @return
	 *********************************************************************/
	public FindIterable<Document> find(String collectionName
									 , String findDocString
									 , String sortDocString
									 , int limit) {
		
		MongoDatabase mongoDB = this.getMongoDB();
	
		if(mongoDB == null) { return null; }
		
		MongoCollection<Document> collection = mongoDB.getCollection(collectionName);

		//-----------------------------
		// Fetch Data
		FindIterable<Document> result;
		if(!Strings.isNullOrEmpty(findDocString)) {
			Document findDoc = Document.parse(findDocString);		
			result = collection.find(findDoc);
		}else {
			result = collection.find();
		}
		
		//-----------------------------
		// Sort and Limit
		if(!Strings.isNullOrEmpty(sortDocString)) {
			Document docSort = Document.parse(sortDocString);
			result.sort(docSort);
		}
		
		if(limit > 0) {
			result.limit(limit);
		}
		
		return result;
	}
	
	/*********************************************************************
	 * Executes a MongoDB find on the given collection and the corresponding
	 * filtering documents.
	 * @param collectionName
	 * @param aggregateDocString
	 * @return
	 *********************************************************************/
	public AggregateIterable<Document> aggregate(String collectionName
									 , String aggregateDocString) {
		
		MongoDatabase mongoDB = this.getMongoDB();
	
		if(mongoDB == null) { return null; }
		
		MongoCollection<Document> collection = mongoDB.getCollection(collectionName);

		//-----------------------------
		// Fetch Data
		AggregateIterable<Document> result;
		if(!Strings.isNullOrEmpty(aggregateDocString)) {

		    BsonArray array = BsonArray.parse(aggregateDocString);
		    
		    ArrayList<Bson> bsonList = new ArrayList<>();
		    for(int i = 0; i < array.size(); i++) {
		    	if(array.get(i) instanceof Bson) {
		    		bsonList.add((Bson)array.get(i));
		    	}
		    }
		    
			result = collection.aggregate(bsonList);
		}else {
			return null;
		}
						
		return result;
	}
	
}
