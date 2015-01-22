package com.cloudkibo.database;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

//We use this class to keep track of database schema information like table and column names
public final class CloudKiboDatabaseContract {

	private CloudKiboDatabaseContract() {}
	
	
	// Authority of CloudKibo Content Provider
	public static final String AUTHORITY = "com.cloudkibo.database.CloudKiboContentProvider";
	
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

	// User table
	public static final class User implements BaseColumns {
		
		private User() {}
		
		// Name of User Table
		public static final String TABLE_USER_NAME = "user";
		
		// User Table Columns names
	    public static final String KEY_ID = "id";
	    public static final String KEY_FIRSTNAME = "firstname";
	    public static final String KEY_LASTNAME = "lastname";
	    public static final String KEY_EMAIL = "email";
	    public static final String KEY_USERNAME = "username";
	    public static final String KEY_UID = "_id"; 
	    public static final String KEY_CREATED_AT = "date";
		
	    // Content URI for this this table
 		public static final Uri CONTENT_URI = 
 				Uri.withAppendedPath(
 						CloudKiboDatabaseContract.CONTENT_URI, TABLE_USER_NAME);
	    
 		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
  				+ "/vnd.cloudkibo.database." + TABLE_USER_NAME;
  		
  		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
  				+ "/vnd.cloudkibo.database." + TABLE_USER_NAME;
 	    
	}

	// Contacts table // 24 hours // timestamps
	public static final class Contacts implements BaseColumns {
		
		private Contacts() {}
		
		// Name of Contacts Table
		public static final String TABLE_CONTACTS = "contacts";
		
		// Contacts Table Columns names
	    public static final String CONTACT_ID = "id";
	    public static final String CONTACT_FIRSTNAME = "firstname";
	    public static final String CONTACT_LASTNAME = "lastname";
	    public static final String CONTACT_PHONE = "email";
	    public static final String CONTACT_USERNAME = "username";
	    public static final String CONTACT_UID = "_id";
	    public static final String SHARED_DETAILS = "detailsshared";
	    public static final String CONTACT_STATUS = "status";
		
	    // Content URI for this this table
  		public static final Uri CONTENT_URI = 
  				Uri.withAppendedPath(
  						CloudKiboDatabaseContract.CONTENT_URI, TABLE_CONTACTS);
 	    
  		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
  				+ "/vnd.cloudkibo.database." + TABLE_CONTACTS;
  		
  		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
  				+ "/vnd.cloudkibo.database." + TABLE_CONTACTS;
 	    
	}
	
	// UserChat table // we don't need sync now, they will stay in local db
	// 500 chats per conversation, // algorithms 
	public static final class UserChat implements BaseColumns {
		
		private UserChat() {}
		
		// Name of UserChat Table
		public static final String TABLE_USERCHAT = "userchat";
		
		// UserChat Table Columns names
	    public static final String USERCHAT_ID = "id"; // sqlite
	    public static final String USERCHAT_TO = "toperson";
	    public static final String USERCHAT_FROM = "fromperson";
	    public static final String USERCHAT_FROM_FULLNAME = "fromFullName";
	    public static final String USERCHAT_MSG = "msg";
	    public static final String USERCHAT_DATE = "date";
	    public static final String USERCHAT_UID = "_id"; // mongodb
		
	    // Content URI for this this table
  		public static final Uri CONTENT_URI = 
  				Uri.withAppendedPath(
  						CloudKiboDatabaseContract.CONTENT_URI, TABLE_USERCHAT);
  		
  		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
  				+ "/vnd.cloudkibo.database." + TABLE_USERCHAT;
  		
  		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
  				+ "/vnd.cloudkibo.database." + TABLE_USERCHAT;
 	    
	}
}