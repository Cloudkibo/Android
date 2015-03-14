package com.cloudkibo.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cloudkibo.database.CloudKiboDatabaseContract.Contacts;
import com.cloudkibo.database.CloudKiboDatabaseContract.User;
import com.cloudkibo.database.CloudKiboDatabaseContract.UserChat;

public class DatabaseHandler extends SQLiteOpenHelper {
		
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "cloudkibo";
    
    
    
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
    	
        String CREATE_USER_TABLE = "CREATE TABLE " + User.TABLE_USER_NAME + "("
                + User.KEY_ID + " INTEGER PRIMARY KEY,"
                + User.KEY_FIRSTNAME + " TEXT,"
                + User.KEY_LASTNAME + " TEXT,"
                + User.KEY_EMAIL + " TEXT UNIQUE,"
                + User.KEY_USERNAME + " TEXT,"
                + User.KEY_UID + " TEXT,"
                + User.KEY_CREATED_AT + " TEXT" + ")";
        db.execSQL(CREATE_USER_TABLE);
        
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + Contacts.TABLE_CONTACTS + "("
                + Contacts.CONTACT_ID + " INTEGER PRIMARY KEY,"
                + Contacts.CONTACT_FIRSTNAME + " TEXT,"
                + Contacts.CONTACT_LASTNAME + " TEXT,"
                + Contacts.CONTACT_PHONE + " TEXT UNIQUE,"
                + Contacts.CONTACT_USERNAME + " TEXT,"
                + Contacts.CONTACT_UID + " TEXT,"
                + Contacts.SHARED_DETAILS + " TEXT,"
                + Contacts.CONTACT_STATUS + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
        
        String CREATE_USERCHAT_TABLE = "CREATE TABLE " + UserChat.TABLE_USERCHAT + "("
                + UserChat.USERCHAT_ID + " INTEGER PRIMARY KEY, "
                + UserChat.USERCHAT_TO + " TEXT, "
                + UserChat.USERCHAT_FROM + " TEXT, "
                + UserChat.USERCHAT_FROM_FULLNAME + " TEXT, "
                + UserChat.USERCHAT_MSG + " TEXT, "
                + UserChat.USERCHAT_UID + " TEXT, "
                + UserChat.USERCHAT_DATE + " TEXT" + ")";
        db.execSQL(CREATE_USERCHAT_TABLE);
    }
    
    
    
    
    /////////////////////////////////////////////////////////////////////
    // Upgrading Tables                                                //
    /////////////////////////////////////////////////////////////////////
    
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        
    	// Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + User.TABLE_USER_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contacts.TABLE_CONTACTS);
        db.execSQL("DROP TABLE IF EXISTS " + UserChat.TABLE_USERCHAT);

        // Create tables again
        onCreate(db);
    }
    
    
    
    
    
    /////////////////////////////////////////////////////////////////////
    // Storing user details in database                                //
    /////////////////////////////////////////////////////////////////////
    
    
    public void addUser(String fname, String lname, String email, String uname, String uid, String created_at) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(User.KEY_FIRSTNAME, fname); // FirstName
        values.put(User.KEY_LASTNAME, lname); // LastName
        values.put(User.KEY_EMAIL, email); // Email
        values.put(User.KEY_USERNAME, uname); // UserName
        values.put(User.KEY_UID, uid); // Email
        values.put(User.KEY_CREATED_AT, created_at); // Created At

        // Inserting Row
        db.insert(User.TABLE_USER_NAME, null, values);
        db.close(); // Closing database connection
    }
    
    
    
    
    /////////////////////////////////////////////////////////////////////
    // Storing contact details in database                             //
    /////////////////////////////////////////////////////////////////////
    
    
    public void addContact(String fname, String lname, String phone, String uname, String uid, String shareddetails,
    		String status) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Contacts.CONTACT_FIRSTNAME, fname); // FirstName
        values.put(Contacts.CONTACT_LASTNAME, lname); // LastName
        values.put(Contacts.CONTACT_PHONE, phone); // Phone
        values.put(Contacts.CONTACT_USERNAME, uname); // UserName
        values.put(Contacts.CONTACT_UID, uid); // Uid
        values.put(Contacts.CONTACT_STATUS, status); // Status
        values.put(Contacts.SHARED_DETAILS, shareddetails); // Created At

        // Inserting Row
        db.insert(Contacts.TABLE_CONTACTS, null, values);
        db.close(); // Closing database connection
    }
    
    
    
    
    /////////////////////////////////////////////////////////////////////
    // Storing userchat details in database                            //
    /////////////////////////////////////////////////////////////////////
    
    
    public void addChat(String to, String from, String from_fullname, String msg, String date) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UserChat.USERCHAT_TO, to); // TO
        values.put(UserChat.USERCHAT_FROM, from); // FROM
        values.put(UserChat.USERCHAT_FROM_FULLNAME, from_fullname); // FROM FULL NAME
        values.put(UserChat.USERCHAT_MSG, msg); // CHAT MESSAGE
        values.put(UserChat.USERCHAT_DATE, date); // DATE

        // Inserting Row
        db.insert(UserChat.TABLE_USERCHAT, null, values);
        db.close(); // Closing database connection
    }    
    
    
    
    
    /////////////////////////////////////////////////////////////////////
    // Getting user data from database                                 //
    /////////////////////////////////////////////////////////////////////


    public HashMap<String, String> getUserDetails(){
        HashMap<String,String> user = new HashMap<String,String>();
        String selectQuery = "SELECT  * FROM " + User.TABLE_USER_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            user.put(User.KEY_FIRSTNAME, cursor.getString(1));
            user.put(User.KEY_LASTNAME, cursor.getString(2));
            user.put(User.KEY_EMAIL, cursor.getString(3));
            user.put(User.KEY_USERNAME, cursor.getString(4));
            user.put(User.KEY_UID, cursor.getString(5));
            user.put(User.KEY_CREATED_AT, cursor.getString(6));
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
        String selectQuery = "SELECT  * FROM " + Contacts.TABLE_CONTACTS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
        	
        	while (cursor.isAfterLast() != true) {
        		
        		JSONObject contact = new JSONObject();
        		contact.put(Contacts.CONTACT_FIRSTNAME, cursor.getString(1));
        		contact.put(Contacts.CONTACT_LASTNAME, cursor.getString(2));
        		contact.put(Contacts.CONTACT_PHONE, cursor.getString(3));
        		contact.put(Contacts.CONTACT_USERNAME, cursor.getString(4));
        		contact.put(Contacts.CONTACT_UID, cursor.getString(5));
        		contact.put(Contacts.SHARED_DETAILS, cursor.getString(6));
        		contact.put(Contacts.CONTACT_STATUS, cursor.getString(7));
        		
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
    // Getting userchat data from database                             //
    /////////////////////////////////////////////////////////////////////


    public JSONArray getChat(String user1, String user2) throws JSONException {
    	JSONArray chats = new JSONArray();
        String selectQuery = "SELECT  * FROM " + UserChat.TABLE_USERCHAT + " WHERE ("+
        		UserChat.USERCHAT_TO +" = '"+ user1 +"' AND "+
        		UserChat.USERCHAT_FROM +" = '"+ user2 +"') OR ("+
        		UserChat.USERCHAT_TO +" = '"+ user2 +"' AND "+
        		UserChat.USERCHAT_FROM +" = '"+ user1 +"')";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
        	
        	while (cursor.isAfterLast() != true) {
        		
        		JSONObject contact = new JSONObject();
        		contact.put(UserChat.USERCHAT_TO, cursor.getString(1));
        		contact.put(UserChat.USERCHAT_FROM, cursor.getString(2));
        		contact.put(UserChat.USERCHAT_FROM_FULLNAME, cursor.getString(3));
        		contact.put(UserChat.USERCHAT_MSG, cursor.getString(4));
        		contact.put(UserChat.USERCHAT_UID, cursor.getString(5));
        		contact.put(UserChat.USERCHAT_DATE, cursor.getString(6));
        		
        		chats.put(contact);
        		
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return chats;
    }
    

    
    
    /////////////////////////////////////////////////////////////////////
    // Other functions                                                 //
    /////////////////////////////////////////////////////////////////////


    /**
     * Getting user login status
     * return true if rows are there in table
     * */
    public int getRowCount() {
        String countQuery = "SELECT  * FROM " + User.TABLE_USER_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int rowCount = cursor.getCount();
        db.close();
        cursor.close();

        // return row count
        return rowCount;
    }


    /**
     * Recreate database
     * Delete all tables and create them again
     * */
    public void resetTables(){
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(User.TABLE_USER_NAME, null, null);
        db.close();
    }
    
    /**
     * Delete all contacts Table
     * */
    public void resetContactsTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(Contacts.TABLE_CONTACTS, null, null);
        db.close();
    }
    
    /**
     * Delete all chats Table
     * */
    public void resetChatsTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(UserChat.TABLE_USERCHAT, null, null);
        db.close();
    }
    
    public void resetSpecificChat(String user1, String user2){
    	SQLiteDatabase db = this.getWritableDatabase();
    	
    	String deleteQuery = "DELETE FROM " + UserChat.TABLE_USERCHAT + " WHERE ("+
    			UserChat.USERCHAT_TO +" = '"+ user1 +"' AND "+
    			UserChat.USERCHAT_FROM +" = '"+ user2 +"') OR ("+
    			UserChat.USERCHAT_TO +" = '"+ user2 +"' AND "+
    			UserChat.USERCHAT_FROM +" = '"+ user1 +"')";
    	
    	db.execSQL(deleteQuery);
    	db.close();
    }

}
