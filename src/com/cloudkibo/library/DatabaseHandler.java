package com.cloudkibo.library;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DatabaseHandler extends SQLiteOpenHelper {
	
	

	
	/////////////////////////////////////////////////////////////////////
	// All Static Variables                                            //
    /////////////////////////////////////////////////////////////////////
	
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "cloudkibo";

    // Table names
    private static final String TABLE_LOGIN = "login";
    private static final String TABLE_CONTACTS = "contacts";
    
    

    // Login Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_FIRSTNAME = "firstname";
    private static final String KEY_LASTNAME = "lastname";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_UID = "_id";
    private static final String KEY_CREATED_AT = "date";
    
    // Contacts Table Columns names
    private static final String CONTACT_ID = "id";
    private static final String CONTACT_FIRSTNAME = "firstname";
    private static final String CONTACT_LASTNAME = "lastname";
    private static final String CONTACT_PHONE = "email";
    private static final String CONTACT_USERNAME = "username";
    private static final String CONTACT_UID = "_id";
    private static final String SHARED_DETAILS = "detailsshared";
    private static final String CONTACT_STATUS = "status";
    
    
    
    


    
    
	/////////////////////////////////////////////////////////////////////
	// Constructor                                                     //
	/////////////////////////////////////////////////////////////////////
    
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    
    
    
    
    
    
    

   /////////////////////////////////////////////////////////////////////
   // Creating Tables                                                 //
   /////////////////////////////////////////////////////////////////////
    
    
    @Override
    public void onCreate(SQLiteDatabase db) {
    	
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_LOGIN + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_FIRSTNAME + " TEXT,"
                + KEY_LASTNAME + " TEXT,"
                + KEY_EMAIL + " TEXT UNIQUE,"
                + KEY_USERNAME + " TEXT,"
                + KEY_UID + " TEXT,"
                + KEY_CREATED_AT + " TEXT" + ")";
        db.execSQL(CREATE_LOGIN_TABLE);
        
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + CONTACT_ID + " INTEGER PRIMARY KEY,"
                + CONTACT_FIRSTNAME + " TEXT,"
                + CONTACT_LASTNAME + " TEXT,"
                + CONTACT_PHONE + " TEXT UNIQUE,"
                + CONTACT_USERNAME + " TEXT,"
                + CONTACT_UID + " TEXT,"
                + SHARED_DETAILS + " TEXT,"
                + CONTACT_STATUS + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }
    
    
    
    
    
    
    
    
    
    /////////////////////////////////////////////////////////////////////
    // Upgrading Tables                                                //
    /////////////////////////////////////////////////////////////////////
    
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        
    	// Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

        // Create tables again
        onCreate(db);
    }
    
    
    
    
    
    
    
    
    
    
    
    /////////////////////////////////////////////////////////////////////
    // Storing user details in database                                //
    /////////////////////////////////////////////////////////////////////
    
    
    public void addUser(String fname, String lname, String email, String uname, String uid, String created_at) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FIRSTNAME, fname); // FirstName
        values.put(KEY_LASTNAME, lname); // LastName
        values.put(KEY_EMAIL, email); // Email
        values.put(KEY_USERNAME, uname); // UserName
        values.put(KEY_UID, uid); // Email
        values.put(KEY_CREATED_AT, created_at); // Created At

        // Inserting Row
        db.insert(TABLE_LOGIN, null, values);
        db.close(); // Closing database connection
    }
    
    
    
    
    
    
    
    
    
    /////////////////////////////////////////////////////////////////////
    // Storing contact details in database                             //
    /////////////////////////////////////////////////////////////////////
    
    
    public void addContact(String fname, String lname, String phone, String uname, String uid, String shareddetails,
    		String status) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CONTACT_FIRSTNAME, fname); // FirstName
        values.put(CONTACT_LASTNAME, lname); // LastName
        values.put(CONTACT_PHONE, phone); // Email
        values.put(CONTACT_USERNAME, uname); // UserName
        values.put(CONTACT_UID, uid); // Uid
        values.put(CONTACT_STATUS, status); // Status
        values.put(SHARED_DETAILS, shareddetails); // Created At

        // Inserting Row
        db.insert(TABLE_CONTACTS, null, values);
        db.close(); // Closing database connection
    }
    
    
    
    
    
    
    
    
    
    
    /////////////////////////////////////////////////////////////////////
    // Getting user data from database                                 //
    /////////////////////////////////////////////////////////////////////


    public HashMap<String, String> getUserDetails(){
        HashMap<String,String> user = new HashMap<String,String>();
        String selectQuery = "SELECT  * FROM " + TABLE_LOGIN;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            user.put("firstname", cursor.getString(1));
            user.put("lastname", cursor.getString(2));
            user.put("email", cursor.getString(3));
            user.put("username", cursor.getString(4));
            user.put("_id", cursor.getString(5));
            user.put("date", cursor.getString(6));
        }
        cursor.close();
        db.close();
        // return user
        return user;
    }


    
    
    
    
    
    
    
    
    
    /////////////////////////////////////////////////////////////////////
    // Getting contacts data from database                             //
    /////////////////////////////////////////////////////////////////////


    public JSONArray getContacts() throws JSONException {
    	JSONArray contacts = new JSONArray();
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
        	
        	while (cursor.isAfterLast() != true) {
        		
        		JSONObject contact = new JSONObject();
        		contact.put("firstname", cursor.getString(1));
        		contact.put("lastname", cursor.getString(2));
        		contact.put("phone", cursor.getString(3));
        		contact.put("username", cursor.getString(4));
        		contact.put("_id", cursor.getString(5));
        		contact.put("detailsshared", cursor.getString(6));
        		contact.put("status", cursor.getString(7));
        		
        		contacts.put(contact);
        		
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return contacts;
    }
    
    
    
    
    
    
    
    
    
    

    
    
    /////////////////////////////////////////////////////////////////////
    // Other functions                                                 //
    /////////////////////////////////////////////////////////////////////


    /**
     * Getting user login status
     * return true if rows are there in table
     * */
    public int getRowCount() {
        String countQuery = "SELECT  * FROM " + TABLE_LOGIN;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int rowCount = cursor.getCount();
        db.close();
        cursor.close();

        // return row count
        return rowCount;
    }


    /**
     * Re crate database
     * Delete all tables and create them again
     * */
    public void resetTables(){
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_LOGIN, null, null);
        db.close();
    }
    
    /**
     * Delete all contacts Table
     * */
    public void resetContactsTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_CONTACTS, null, null);
        db.close();
    }

}
