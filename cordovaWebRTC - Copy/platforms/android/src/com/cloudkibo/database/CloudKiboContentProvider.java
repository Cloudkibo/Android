package com.cloudkibo.database;

import com.cloudkibo.database.CloudKiboDatabaseContract.Contacts;
import com.cloudkibo.database.CloudKiboDatabaseContract.User;
import com.cloudkibo.database.CloudKiboDatabaseContract.UserChat;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;


public class CloudKiboContentProvider extends ContentProvider {
	
	DatabaseHandler databasehelper;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {

		SQLiteDatabase db = databasehelper.getWritableDatabase();
		int delCount = 0;
		
		switch(sUriMatcher.match(uri)){
        case USER:
        	delCount = db.delete(
                    User.TABLE_USER_NAME, 
                    selection,
                    selectionArgs);
            break;
        case CONTACT:
        	delCount = db.delete(
                    Contacts.TABLE_CONTACTS, 
                    selection,
                    selectionArgs);
            break;
        case USER_CHAT:
        	delCount = db.delete(
                    UserChat.TABLE_USERCHAT, 
                    selection,
                    selectionArgs);
            break;
        case USER_ID:
        	String idStr = uri.getLastPathSegment();
            String where = User._ID + " = " + idStr;
            if (!TextUtils.isEmpty(selection)) {
               where += " AND " + selection;
            }
            delCount = db.delete(
                    User.TABLE_USER_NAME, 
                    where,
                    selectionArgs);
            break;
        case CONTACT_ID:
        	idStr = uri.getLastPathSegment();
            where = Contacts._ID + " = " + idStr;
            if (!TextUtils.isEmpty(selection)) {
               where += " AND " + selection;
            }
            delCount = db.delete(
                    Contacts.TABLE_CONTACTS, 
                    where,
                    selectionArgs);
            break;
        case USER_CHAT_ID:
        	idStr = uri.getLastPathSegment();
            where = UserChat._ID + " = " + idStr;
            if (!TextUtils.isEmpty(selection)) {
               where += " AND " + selection;
            }
            delCount = db.delete(
                    UserChat.TABLE_USERCHAT, 
                    where,
                    selectionArgs);
            break;	
        default:
            	throw new IllegalArgumentException("Unknown URI " + uri);
        }
		
		// notify all listeners of changes:
	   if (delCount > 0) {
	      getContext().getContentResolver().notifyChange(uri, null);
	   }
	   return delCount;
		
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		   case USER:
		      return User.CONTENT_TYPE;
		   case USER_ID:
		      return User.CONTENT_ITEM_TYPE;
		   case CONTACT:
			  return Contacts.CONTENT_TYPE;
		   case CONTACT_ID:
			  return Contacts.CONTENT_ITEM_TYPE;
		   case USER_CHAT:
			  return UserChat.CONTENT_TYPE;
		   case USER_CHAT_ID:
			  return UserChat.CONTENT_ITEM_TYPE;
				  
		   default:
		      return null;
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (sUriMatcher.match(uri) != USER && sUriMatcher.match(uri) != USER_ID
				&& sUriMatcher.match(uri) != CONTACT && sUriMatcher.match(uri) != CONTACT_ID
				&& sUriMatcher.match(uri) != USER_CHAT && sUriMatcher.match(uri) != USER_CHAT_ID) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
 
        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }
 
        SQLiteDatabase db = databasehelper.getWritableDatabase();
        
        switch(sUriMatcher.match(uri)){
        case USER:
        	long rowId = db.insert(User.TABLE_USER_NAME, null, values);
            if (rowId > 0) {
                Uri gotUri = ContentUris.withAppendedId(User.CONTENT_URI, rowId);
                getContext().getContentResolver().notifyChange(gotUri, null);
                return gotUri;
            }
            throw new SQLException("Failed to insert row into " + uri);
        case CONTACT:
        	rowId = db.insert(Contacts.TABLE_CONTACTS, null, values);
            if (rowId > 0) {
                Uri gotUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, rowId);
                getContext().getContentResolver().notifyChange(gotUri, null);
                return gotUri;
            }
            throw new SQLException("Failed to insert row into " + uri);
        case USER_CHAT:
        	rowId = db.insert(UserChat.TABLE_USERCHAT, null, values);
            if (rowId > 0) {
                Uri gotUri = ContentUris.withAppendedId(UserChat.CONTENT_URI, rowId);
                getContext().getContentResolver().notifyChange(gotUri, null);
                return gotUri;
            }
            throw new SQLException("Failed to insert row into " + uri);
        }
        
        throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		
		databasehelper = new DatabaseHandler(getContext());
		
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		SQLiteDatabase db = databasehelper.getReadableDatabase();
		
		switch(sUriMatcher.match(uri)){
        case USER:
        	queryBuilder.setTables(User.TABLE_USER_NAME);
            break;
        case CONTACT:
        	queryBuilder.setTables(Contacts.TABLE_CONTACTS);
            break;
        case USER_CHAT:
        	queryBuilder.setTables(UserChat.TABLE_USERCHAT);
            break;
        case USER_ID:
        	queryBuilder.setTables(User.TABLE_USER_NAME);
        	queryBuilder.appendWhere(User._ID + " = " +
                    uri.getLastPathSegment());
            break;
        case CONTACT_ID:
        	queryBuilder.setTables(Contacts.TABLE_CONTACTS);
        	queryBuilder.appendWhere(Contacts._ID + " = " +
                    uri.getLastPathSegment());
            break;
        case USER_CHAT_ID:
        	queryBuilder.setTables(UserChat.TABLE_USERCHAT);
        	queryBuilder.appendWhere(UserChat._ID + " = " +
                    uri.getLastPathSegment());
            break;	
        default:
            	throw new IllegalArgumentException("Unknown URI " + uri);
        }
		
		Cursor cursor = 
		         queryBuilder.query(
		         db, 
		         projection, 
		         selection, 
		         selectionArgs,
		         null, 
		         null, 
		         sortOrder);
		
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

		SQLiteDatabase db = databasehelper.getWritableDatabase();
		int updateCount = 0;
		
		switch(sUriMatcher.match(uri)){
        case USER:
        	updateCount = db.update(
                    User.TABLE_USER_NAME, 
                    values, 
                    selection,
                    selectionArgs);
            break;
        case CONTACT:
        	updateCount = db.update(
                    Contacts.TABLE_CONTACTS, 
                    values, 
                    selection,
                    selectionArgs);
            break;
        case USER_CHAT:
        	updateCount = db.update(
                    UserChat.TABLE_USERCHAT, 
                    values, 
                    selection,
                    selectionArgs);
            break;
        case USER_ID:
        	String idStr = uri.getLastPathSegment();
            String where = User._ID + " = " + idStr;
            if (!TextUtils.isEmpty(selection)) {
               where += " AND " + selection;
            }
        	updateCount = db.update(
                    User.TABLE_USER_NAME, 
                    values, 
                    where,
                    selectionArgs);
            break;
        case CONTACT_ID:
        	idStr = uri.getLastPathSegment();
            where = Contacts._ID + " = " + idStr;
            if (!TextUtils.isEmpty(selection)) {
               where += " AND " + selection;
            }
        	updateCount = db.update(
                    Contacts.TABLE_CONTACTS, 
                    values, 
                    where,
                    selectionArgs);
            break;
        case USER_CHAT_ID:
        	idStr = uri.getLastPathSegment();
            where = UserChat._ID + " = " + idStr;
            if (!TextUtils.isEmpty(selection)) {
               where += " AND " + selection;
            }
        	updateCount = db.update(
                    UserChat.TABLE_USERCHAT, 
                    values, 
                    where,
                    selectionArgs);
            break;	
        default:
            	throw new IllegalArgumentException("Unknown URI " + uri);
        }
		
		// notify all listeners of changes:
	   if (updateCount > 0) {
	      getContext().getContentResolver().notifyChange(uri, null);
	   }
	   return updateCount;
	}
	
	
	private static final int USER = 1;
    private static final int USER_ID = 2;
    
    private static final int CONTACT = 3;
    private static final int CONTACT_ID = 4;
    
    private static final int USER_CHAT = 5;
    private static final int USER_CHAT_ID = 6;
	
	private static final UriMatcher sUriMatcher;
	
	static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        
        sUriMatcher.addURI(CloudKiboDatabaseContract.AUTHORITY, 
        		CloudKiboDatabaseContract.User.TABLE_USER_NAME, USER);
        sUriMatcher.addURI(CloudKiboDatabaseContract.AUTHORITY, 
        		CloudKiboDatabaseContract.User.TABLE_USER_NAME + "/#", USER_ID);
        
        sUriMatcher.addURI(CloudKiboDatabaseContract.AUTHORITY, 
        		CloudKiboDatabaseContract.Contacts.TABLE_CONTACTS, CONTACT);
        sUriMatcher.addURI(CloudKiboDatabaseContract.AUTHORITY, 
        		CloudKiboDatabaseContract.Contacts.TABLE_CONTACTS + "/#", CONTACT_ID);
        
        sUriMatcher.addURI(CloudKiboDatabaseContract.AUTHORITY, 
        		CloudKiboDatabaseContract.UserChat.TABLE_USERCHAT, USER_CHAT);
        sUriMatcher.addURI(CloudKiboDatabaseContract.AUTHORITY, 
        		CloudKiboDatabaseContract.UserChat.TABLE_USERCHAT + "/#", USER_CHAT_ID);
    }

}
