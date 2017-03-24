package com.cloudkibo.database;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.content.ContentValues;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.acra.ACRA;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cloudkibo.database.CloudKiboDatabaseContract.Contacts;
import com.cloudkibo.database.CloudKiboDatabaseContract.User;
import com.cloudkibo.database.CloudKiboDatabaseContract.UserChat;
import com.cloudkibo.library.Utility;

public class DatabaseHandler extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 23;

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
                //+ User.KEY_FIRSTNAME + " TEXT,"
                //+ User.KEY_LASTNAME + " TEXT,"
                //+ User.KEY_EMAIL + " TEXT UNIQUE,"
                //+ User.KEY_USERNAME + " TEXT,"
                + User.KEY_UID + " TEXT,"
                + "display_name TEXT,"
                + "phone TEXT,"
                + "national_number TEXT,"
                + "country_prefix TEXT,"
                + User.KEY_CREATED_AT + " TEXT" + ")";
        db.execSQL(CREATE_USER_TABLE);

        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + Contacts.TABLE_CONTACTS + "("
                + Contacts.CONTACT_ID + " INTEGER PRIMARY KEY,"
                //+ Contacts.CONTACT_FIRSTNAME + " TEXT,"
                //+ Contacts.CONTACT_LASTNAME + " TEXT,"
                + Contacts.CONTACT_PHONE + " TEXT UNIQUE,"
                + "display_name" + " TEXT,"
                + Contacts.CONTACT_UID + " TEXT,"
                + Contacts.SHARED_DETAILS + " TEXT,"
                + Contacts.CONTACT_STATUS + " TEXT,"
                + "on_cloudkibo" + " TEXT,"
                + "image_uri TEXT DEFAULT NULL,"
                + "blocked_me" + " TEXT DEFAULT 'false'," // possible values : "true" or "false"
                + "blocked_by_me" + " TEXT DEFAULT 'false' " // possible values : "true" or "false"
                + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);

        String CREATE_USERCHAT_TABLE = "CREATE TABLE " + UserChat.TABLE_USERCHAT + "("
                + UserChat.USERCHAT_ID + " INTEGER PRIMARY KEY, "
                + UserChat.USERCHAT_TO + " TEXT, "
                + UserChat.USERCHAT_FROM + " TEXT, "
                + UserChat.USERCHAT_FROM_FULLNAME + " TEXT, "
                + UserChat.USERCHAT_MSG + " TEXT, "
                + UserChat.USERCHAT_DATE + " TEXT, "
                + "status" + " TEXT, "
                + "type" + " TEXT, " // possible values : "chat" or "file"
                + "file_type" + " TEXT, "
                + "uniqueid" + " TEXT, "
                + "isArchived" + " INTEGER DEFAULT 0 , "
                + "contact_phone" + " TEXT "+ ")";
        db.execSQL(CREATE_USERCHAT_TABLE);

        String CREATE_CALL_HISTORY_TABLE = "CREATE TABLE call_history ("
                + "id INTEGER PRIMARY KEY, "
                + "call_date DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + "type TEXT, " // values : placed, received, missed
                + "contact_phone TEXT "+ ")";
        db.execSQL(CREATE_CALL_HISTORY_TABLE);

        String CREATE_CHAT_HISTORY_SYNC_TABLE = "CREATE TABLE chat_history_sync ("
                + "id INTEGER PRIMARY KEY, "
                + "status TEXT, "
                + "uniqueid TEXT, "
                + "fromperson TEXT "+ ")";
        db.execSQL(CREATE_CHAT_HISTORY_SYNC_TABLE);

        String CREATE_GROUP_CHAT_HISTORY_SYNC_TABLE = "CREATE TABLE group_chat_history_sync ("
                + "id INTEGER PRIMARY KEY, "
                + "status TEXT, "
                + "uniqueid TEXT, "
                + "fromperson TEXT "+ ")";
        db.execSQL(CREATE_GROUP_CHAT_HISTORY_SYNC_TABLE);


        //Below are the tables for group chat.
        String CREATE_GROUP = "CREATE TABLE GROUPINFO ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "group_name TEXT, "
                + "group_icon BLOB, "
                + "date_creation DATETIME DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME')), "
                + "unique_id TEXT UNIQUE, "
                + "isArchived" + " INTEGER DEFAULT 0 , "
                + "is_mute INTEGER DEFAULT 0 "+ ")";
        db.execSQL(CREATE_GROUP);

        String CREATE_GROUP_SERVER_PENDING = "CREATE TABLE GROUPSERVERPENDING ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "group_name TEXT, "
                + "members TEXT, "
                + "unique_id TEXT UNIQUE "+ ")";
        db.execSQL(CREATE_GROUP_SERVER_PENDING);

        String CREATE_GROUP_MEMBER = "CREATE TABLE GROUPMEMBER ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "group_unique_id TEXT, "
                + "member_phone TEXT, "
                + "isAdmin TEXT, "
                + "date_joined DATETIME DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME')), "
                + "date_left DATETIME, "
                + "membership_status TEXT, "
                + "unique (group_unique_id, member_phone)"
                + ")";
        db.execSQL(CREATE_GROUP_MEMBER);

        String GROUP_MEMBER_SERVER_PENDING = "CREATE TABLE GROUPMEMBERSERVERPENDING ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "group_unique_id TEXT, "
                + "member_phone TEXT, "
                + "unique (group_unique_id, member_phone)"
                + ")";
        db.execSQL(GROUP_MEMBER_SERVER_PENDING);

        String GROUP_MEMBER_REMOVE_PENDING = "CREATE TABLE GROUPMEMBERREMOVEPENDING ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "group_unique_id TEXT, "
                + "member_phone TEXT, "
                + "unique (group_unique_id, member_phone)"
                + ")";
        db.execSQL(GROUP_MEMBER_REMOVE_PENDING);

        String CREATE_GROUP_CHAT = "CREATE TABLE GROUPCHAT ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "group_unique_id TEXT, "
                + "_from TEXT, "
                + "type TEXT, "
                + "msg TEXT, "
                + "isArchived" + " INTEGER DEFAULT 0 , "
                + "from_fullname TEXT, "
                + "date TEXT, "
                + "unique_id TEXT UNIQUE"
                + ")";
        db.execSQL(CREATE_GROUP_CHAT);

        String CREATE_GROUP_CHAT_STATUS = "CREATE TABLE GROUPCHATSTATUS ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "msg_unique_id TEXT, "
                + "status TEXT, "
                + "user_phone TEXT, "
                + "read_date DATETIME, "
                + "delivered_date DATETIME, "
                + "unique (msg_unique_id, user_phone)"
                + ")";
        db.execSQL(CREATE_GROUP_CHAT_STATUS);

        String CREATE_GROUP_MUTE_SETTINGS = "CREATE TABLE MUTESETTING ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "groupid TEXT UNIQUE, "
                + "isMute TEXT, "
                + "muteTime TEXT, "
                + "unMuteTime TEXT "
                + ")";
        db.execSQL(CREATE_GROUP_MUTE_SETTINGS);

        String CREATE_FILES_INFO = "CREATE TABLE FILESINFO ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "uniqueid TEXT, "
                + "file_name TEXT, "
                + "file_size TEXT, "
                + "file_type TEXT, "
                + "file_ext TEXT, "
                + "isBackup INTEGER DEFAULT 0 , "
                + "path TEXT "
                + ")";
        db.execSQL(CREATE_FILES_INFO);

        String CREATE_DRIVE_FOLDER_INFO = "CREATE TABLE DRIVEFOLDERINFO ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "folder_type TEXT, "
                + "folder_address "
                + ")";
        db.execSQL(CREATE_DRIVE_FOLDER_INFO);

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
        db.execSQL("DROP TABLE IF EXISTS call_history");
        db.execSQL("DROP TABLE IF EXISTS chat_history_sync");
        //Tables for group chat
        db.execSQL("DROP TABLE IF EXISTS GROUPINFO");
        db.execSQL("DROP TABLE IF EXISTS GROUPMEMBER");
        db.execSQL("DROP TABLE IF EXISTS GROUPCHAT");
        db.execSQL("DROP TABLE IF EXISTS GROUPCHATSTATUS");
        db.execSQL("DROP TABLE IF EXISTS GROUPMEMBERSERVERPENDING");
        db.execSQL("DROP TABLE IF EXISTS GROUPSERVERPENDING");
        db.execSQL("DROP TABLE IF EXISTS GROUPMEMBERREMOVEPENDING");
        db.execSQL("DROP TABLE IF EXISTS MUTESETTING");
        db.execSQL("DROP TABLE IF EXISTS FILESINFO");
        db.execSQL("DROP TABLE IF EXISTS DRIVEFOLDERINFO");
        db.execSQL("DROP TABLE IF EXISTS group_chat_history_sync");
        // Create tables again
        onCreate(db);
    }

    public void createFilesInfo(String unique_id, String file_name, String file_size, String file_type, String file_ext, String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("uniqueid", unique_id);
        values.put("file_name", file_name);
        values.put("file_size", file_size);
        values.put("file_type", file_type);
        values.put("file_ext", file_ext);
        values.put("path", path);
        // Inserting Row
        db.insert("FILESINFO", null, values);

        db.close(); // Closing database connection

    }

    public void createDriveFolderInfo(String folder_type, String folder_address){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("folder_type", folder_type);
        values.put("folder_address", folder_address);
        //Inserting Row
        db.insert("DRIVEFOLDERINFO", null, values);
    }

    public void updateFileInfo(String unique_id, String file_name, String file_size, String file_type, String file_ext, String path){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("file_name", file_name);
        values.put("file_size", file_size);
        values.put("file_type", file_type);
        values.put("file_ext", file_ext);
        values.put("path", path);
        db.update("FILESINFO",values,"uniqueid='"+unique_id+"'",null);
        db.close(); // Closing database connection
    }

    public JSONObject getFilesInfo(String unique_id) {
        JSONObject filesInfo = new JSONObject();
        String selectQuery = "SELECT uniqueid, file_name, file_size, file_type, file_ext, path FROM FILESINFO WHERE uniqueid='"+ unique_id +"'";
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);
            // Move to first row
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                filesInfo.put("uniqueid", cursor.getString(0));
                filesInfo.put("file_name", cursor.getString(1));
                filesInfo.put("file_size", cursor.getString(2));
                filesInfo.put("file_type", cursor.getString(3));
                filesInfo.put("file_ext", cursor.getString(4));
                filesInfo.put("path", cursor.getString(5));
            }
            cursor.close();
            db.close();
        }catch (JSONException e){
            e.printStackTrace();
        }

        return filesInfo;
    }

    public JSONArray getDriveFolderInfo() throws JSONException{
        JSONArray groups = new JSONArray();

        String selectQuery = "SELECT folder_type, folder_address from DRIVEFOLDERINFO";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        //Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {

                JSONObject contact = new JSONObject();
                contact.put("folder_type", cursor.getString(0));
                contact.put("folder_address", cursor.getString(1));
                groups.put(contact);

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return groups;

    }

    public JSONArray getAllFiles(String type) throws JSONException {
        JSONArray groups = new JSONArray();

        String selectQuery = "SELECT uniqueid, file_name, file_size, file_type, file_ext, path FROM FILESINFO " +
                "WHERE file_type='"+ type+"' AND isBackup=0";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {

                JSONObject contact = new JSONObject();
                contact.put("uniqueid", cursor.getString(0));
                contact.put("file_name", cursor.getString(1));
                contact.put("file_size", cursor.getString(2));
                contact.put("file_type", cursor.getString(3));
                contact.put("file_ext", cursor.getString(4));
                contact.put("path", cursor.getString(5));
                groups.put(contact);

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return groups;
    }

    public void setBackup(String unique_id) {

        SQLiteDatabase db = this.getWritableDatabase();

        String updateQuery = "UPDATE FILESINFO" +
                " SET isBackup="+ 1 +" WHERE uniqueid='"+unique_id+"'";

        try {
            db.execSQL(updateQuery);
        } catch (Exception e){
            e.printStackTrace();
        }

        db.close();
    }


    public void addContactImage(String phone, String image_uri){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues args = new ContentValues();
        args.put("image_uri", image_uri);
        db.update("contacts",args,"phone='"+phone+"'",null);
        db.close(); // Closing database connection
    }

    public String getContactImage(String phone){
        String selectQuery = "SELECT image_uri FROM contacts WHERE phone = '" + phone + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {
                    return  cursor.getString(0);
            }
        }
        cursor.close();
        db.close();
        // return user
        return null;
    }

    public String getContactName(String phone){
        String selectQuery = "SELECT display_name FROM contacts WHERE phone = '" + phone + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        String name = null;
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {
                name = cursor.getString(0);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return name;
    }
    /*
     * This method is called when we need to create a new group and store it in the database
     * */
    public void createGroup(String unique_id, String group_name, int isMute) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("group_name", group_name); // values : Group Name
        values.put("unique_id", unique_id); // values : random string
        values.put("is_mute", isMute);// values : 0 or 1

        // Inserting Row
        db.insert("GROUPINFO", null, values);

        ContentValues args = new ContentValues();
        args.put("groupid", unique_id); // values : random string
        args.put("isMute", isMute);// values : 0 or 1
        args.put("muteTime", "");// values : 0 or 1
        args.put("unMuteTime", "");// values : 0 or 1
        db.insert("MUTESETTING", null, args);

        db.close(); // Closing database connection

    }

    public void createGroupServerPending(String unique_id, String group_name, String members) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues information = new ContentValues();
        information.put("group_name", group_name); // values : Group Name
        information.put("unique_id", unique_id); // values : random string
        information.put("members", members);// values : 0 or 1

        // Inserting Row
        db.insert("GROUPSERVERPENDING", null, information);

        db.close(); // Closing database connection

    }

    public void syncGroup(String unique_id, String group_name, int isMute, String date) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("group_name", group_name); // values : Group Name
        values.put("unique_id", unique_id); // values : random string
        values.put("is_mute", isMute);// values : 0 or 1
        values.put("date_creation", date);// values : 0 or 1

        db.replace("GROUPINFO", null, values); // Inserting Row


        ContentValues args = new ContentValues();
        args.put("groupid", unique_id); // values : random string
        args.put("isMute", isMute);// values : 0 or 1
        args.put("muteTime", "");// values : 0 or 1
        args.put("unMuteTime", "");// values : 0 or 1
        db.replace("MUTESETTING", null, args);

        db.close(); // Closing database connection
    }
    /*
    * This Method is used to add group member to a group
    * */
    public void addGroupMember(String group_unique_id, String member_phone, String isAdmin, String membership_status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("group_unique_id", group_unique_id); // values : Group Name
        values.put("member_phone", member_phone); //
        values.put("isAdmin", isAdmin);// values : 0 or 1
        values.put("membership_status", membership_status);// values : left or joined
        // Inserting Row
        db.replace("GROUPMEMBER", null, values);
        db.close(); // Closing database connection
    }

    public void addGroupChatStatus(String msg_unique_id, String status, String user_phone){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("msg_unique_id", msg_unique_id); // values : Group Name
        values.put("status", status); //
        values.put("user_phone", user_phone);// values : 0 or 1
        // Inserting Row
        db.insert("GROUPCHATSTATUS", null, values);
        db.close(); // Closing database connection
    }

    public void addGroupChatStatus(String msg_unique_id, String status, String user_phone, String read_time, String delivered_time){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("msg_unique_id", msg_unique_id); // values : Group Name
        values.put("status", status); //
        values.put("user_phone", user_phone);
        values.put("read_date", read_time);// values : 0 or 1
        values.put("delivered_date", delivered_time);
        // Inserting Row
        db.insert("GROUPCHATSTATUS", null, values);
        db.close(); // Closing database connection
    }


    public void updateGroupMembershipStatus(String group_unique_id, String member_phone, String status){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues args = new ContentValues();
        args.put("membership_status", status);
        db.update("GROUPMEMBER",args,"group_unique_id='"+group_unique_id+"' and member_phone='"+member_phone+"'",null);
        db.close(); // Closing database connection
    }

    public void updateGroupName(String group_unique_id, String name){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues args = new ContentValues();
        args.put("group_name", name);
        db.update("GROUPINFO", args,"unique_id='"+group_unique_id+"'", null);
        db.close();
    }

    public void updateGroupChatStatus(String msg_unique_id, String status){
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues args = new ContentValues();
            args.put("status", status);
            db.update("GROUPCHATSTATUS",args,"msg_unique_id='"+msg_unique_id+"'",null);
            db.close(); // Closing database connection
    }
    public void updateGroupChatStatus(String msg_unique_id, String status, String phone){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues args = new ContentValues();
        args.put("status", status);
        db.update("GROUPCHATSTATUS",args,"msg_unique_id='"+msg_unique_id+"' AND user_phone='"+ phone +"'",null);
        db.close(); // Closing database connection
    }
    public void updateGroupChatStatusReadTime(String msg_unique_id, String status, String phone, String read_time){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues args = new ContentValues();
        args.put("status", status);
        args.put("read_date", read_time);// values : 0 or 1
        db.update("GROUPCHATSTATUS",args,"msg_unique_id='"+msg_unique_id+"' AND user_phone='"+ phone +"'",null);
        db.close(); // Closing database connection
    }
    public void updateGroupChatStatusDeliveredTime(String msg_unique_id, String status, String phone, String delivered_time){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues args = new ContentValues();
        args.put("status", status);
        args.put("delivered_date", delivered_time);
        db.update("GROUPCHATSTATUS",args,"msg_unique_id='"+msg_unique_id+"' AND user_phone='"+ phone +"'",null);
        db.close(); // Closing database connection
    }

    public void addGroupMemberServerPending(String group_unique_id, String member_phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("group_unique_id", group_unique_id); // values : Group Name
        values.put("member_phone", member_phone); //
        // Inserting Row
        db.insert("GROUPMEMBERSERVERPENDING", null, values);
        db.close(); // Closing database connection
    }

    public void addGroupMemberRemovePending(String group_unique_id, String member_phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("group_unique_id", group_unique_id); // values : Group Name
        values.put("member_phone", member_phone); //
        // Inserting Row
        db.insert("GROUPMEMBERREMOVEPENDING", null, values);
        db.close(); // Closing database connection
    }

    public void syncGroupMember(String group_unique_id, String member_phone, int isAdmin, String membership_status, String date_joined) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("group_unique_id", group_unique_id); // values : Group Name
        values.put("member_phone", member_phone); //
        values.put("isAdmin", isAdmin);// values : 0 or 1
        values.put("membership_status", membership_status);// values : left or joined
        values.put("date_joined", date_joined);
        // Inserting Row
        db.replace("GROUPMEMBER", null, values);
        db.close(); // Closing database connection
    }

    public void resetGroupMembers(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from GROUPMEMBER");
    }

    public void leaveGroup(String group_unique_id, String member_phone){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues args = new ContentValues();
        args.put("membership_status", "left");
        args.put("isAdmin", "0");
        db.update("GROUPMEMBER",args,"group_unique_id='"+group_unique_id+"' and member_phone='"+member_phone+"'",null);
        db.close(); // Closing database connection
    }

    public void leaveGroupServerPending(String group_unique_id, String member_phone){
        SQLiteDatabase db = this.getWritableDatabase();
        String deleteQuery = "DELETE FROM GROUPMEMBERSERVERPENDING WHERE group_unique_id='"+ group_unique_id +"' AND " +
                "member_phone='"+ member_phone+"'";

        db.execSQL(deleteQuery);
        db.close();
    }

    public void leaveGroupMemberRemovePending(String group_unique_id, String member_phone){
        SQLiteDatabase db = this.getWritableDatabase();

        String deleteQuery = "DELETE FROM GROUPMEMBERREMOVEPENDING WHERE group_unique_id='"+ group_unique_id +"' AND " +
                "member_phone='"+ member_phone+"'";

        db.execSQL(deleteQuery);
        db.close();
    }

    public JSONArray getGroupMembersServerPending() throws JSONException {
        JSONArray groups = new JSONArray();

        String selectQuery = "SELECT group_unique_id, member_phone FROM GROUPMEMBERSERVERPENDING";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {

                JSONObject contact = new JSONObject();
                contact.put("group_unique_id", cursor.getString(0));
                contact.put("member_phone", cursor.getString(1));
                groups.put(contact);

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return groups;
    }


    public JSONArray getGroupMembersRemovePending() throws JSONException {
        JSONArray groups = new JSONArray();

        String selectQuery = "SELECT group_unique_id, member_phone FROM GROUPMEMBERREMOVEPENDING";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {

                JSONObject contact = new JSONObject();
                contact.put("group_unique_id", cursor.getString(0));
                contact.put("member_phone", cursor.getString(1));
                groups.put(contact);

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return groups;
    }

    public int isGroupMembersRemovePending(String group_id, String member_phone){
        JSONArray groups = new JSONArray();

        String selectQuery = "SELECT group_unique_id, member_phone FROM GROUPMEMBERREMOVEPENDING WHERE group_unique_id='"+ group_id +"' AND member_phone='" + member_phone +"'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        int count = cursor.getCount();
        cursor.close();
        db.close();
        // return user
        return count;
    }

    public JSONArray getGroupsServerPending() throws JSONException {
        JSONArray groups = new JSONArray();

        String selectQuery = "SELECT group_name, unique_id, members FROM GROUPSERVERPENDING";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {

                JSONObject contact = new JSONObject();
                contact.put("group_name", cursor.getString(0));
                contact.put("unique_id", cursor.getString(1));
                contact.put("members", cursor.getString(2));
                groups.put(contact);

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return groups;
    }

    public void deleteGroupServerPending(String group_unique_id){
        SQLiteDatabase db = this.getWritableDatabase();

        String deleteQuery = "DELETE FROM GROUPSERVERPENDING WHERE unique_id='"+ group_unique_id +"'";

        db.execSQL(deleteQuery);
        db.close();
    }

    public void deleteGroupChatMessage(String message_unique_id){
        SQLiteDatabase db = this.getWritableDatabase();
        String deleteQuery = "DELETE FROM GROUPCHAT WHERE unique_id='"+ message_unique_id +"'";
        db.execSQL(deleteQuery);
        String deleteStatusQuery = "DELETE FROM GROUPCHATSTATUS WHERE msg_unique_id='"+ message_unique_id +"'";
        db.execSQL(deleteStatusQuery);
        db.close();
    }

    public void deleteNormalChatMessage(String message_unique_id){
        SQLiteDatabase db = this.getWritableDatabase();
        String deleteQuery = "DELETE FROM userchat WHERE uniqueid='"+ message_unique_id +"'";
        db.execSQL(deleteQuery);
        db.close();
    }

    public void makeGroupAdmin(String group_unique_id, String member_phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues args = new ContentValues();
        args.put("isAdmin", 1);
        db.update("GROUPMEMBER",args,"group_unique_id='"+group_unique_id+"' and member_phone='"+member_phone+"'",null);
        db.close(); // Closing database connection
    }

    public void updateAdminStatus(String group_unique_id, String member_phone, String isAdmin) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues args = new ContentValues();
        args.put("isAdmin", isAdmin);
        db.update("GROUPMEMBER",args,"group_unique_id='"+group_unique_id+"' and member_phone='"+member_phone+"'",null);
        db.close(); // Closing database connection
    }

    public void demoteGroupAdmin(String group_unique_id, String member_phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues args = new ContentValues();
        args.put("isAdmin", 0);
        db.update("GROUPMEMBER",args,"group_unique_id='"+group_unique_id+"' and member_phone='"+member_phone+"'",null);
        db.close(); // Closing database connection
    }




    public JSONArray getGroupMessageStatusSeen(String msg_unique_id) throws JSONException {
        JSONArray status = new JSONArray();

        String selectQuery = "SELECT status FROM GROUPCHATSTATUS WHERE msg_unique_id='" + msg_unique_id +"' AND status='seen'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {

                JSONObject contact = new JSONObject();
                contact.put("status", cursor.getString(0));
                //contact.put("user_phone", cursor.getString(1));
                //contact.put("read_date", cursor.getString(2));
                //contact.put("delivered_date", cursor.getString(3));
                status.put(contact);
                //return cursor.getString(0);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return status;

    }

    public JSONArray getGroupMessageStatusDelivered(String msg_unique_id) throws JSONException {
        JSONArray status = new JSONArray();

        String selectQuery = "SELECT status FROM GROUPCHATSTATUS WHERE msg_unique_id='" + msg_unique_id +"' AND status='delivered'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {

                JSONObject contact = new JSONObject();
                contact.put("status", cursor.getString(0));
                //contact.put("user_phone", cursor.getString(1));
                //contact.put("read_date", cursor.getString(2));
                //contact.put("delivered_date", cursor.getString(3));
                status.put(contact);
                //return cursor.getString(0);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return status;

    }

    public JSONArray getGroupMessageStatusPending(String msg_unique_id) throws JSONException {
        JSONArray status = new JSONArray();

        String selectQuery = "SELECT status FROM GROUPCHATSTATUS WHERE msg_unique_id='" + msg_unique_id +"' AND status='pending'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {

                JSONObject contact = new JSONObject();
                contact.put("status", cursor.getString(0));
                //contact.put("user_phone", cursor.getString(1));
                //contact.put("read_date", cursor.getString(2));
                //contact.put("delivered_date", cursor.getString(3));
                status.put(contact);
                //return cursor.getString(0);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return status;

    }

    public JSONArray getGroupMessageStatus(String msg_unique_id) throws JSONException {
                JSONArray status = new JSONArray();

            String selectQuery = "SELECT status FROM GROUPCHATSTATUS WHERE msg_unique_id='" + msg_unique_id +"'";

            SQLiteDatabase db = this.getReadableDatabase();
                Cursor cursor = db.rawQuery(selectQuery, null);
                // Move to first row
                cursor.moveToFirst();
                if(cursor.getCount() > 0){

                while (cursor.isAfterLast() != true) {

                JSONObject contact = new JSONObject();
                    contact.put("status", cursor.getString(0));
                    //contact.put("user_phone", cursor.getString(1));
                    //contact.put("read_date", cursor.getString(2));
                    //contact.put("delivered_date", cursor.getString(3));
                    status.put(contact);
                    //return cursor.getString(0);
                    cursor.moveToNext();
                }
            }
                cursor.close();
                db.close();
                 return status;

    }

    public JSONArray getGroupMessageStatus(String msg_unique_id, String user_phone) throws JSONException {
        JSONArray status = new JSONArray();

        String selectQuery = "SELECT status, read_date, delivered_date FROM GROUPCHATSTATUS WHERE msg_unique_id='" + msg_unique_id +"' AND user_phone='" + user_phone +"'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {

                JSONObject contact = new JSONObject();
                                contact.put("status", cursor.getString(0));
                                contact.put("read_date", cursor.getString(1));
                                contact.put("delivered_date", cursor.getString(2));
                                //contact.put("delivered_date", cursor.getString(3));

                                status.put(contact);
                //return cursor.getString(0);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
         return status;

    }

    public JSONArray getAllGroups() throws JSONException {
        JSONArray groups = new JSONArray();

        String selectQuery = "SELECT unique_id, group_name, is_mute, date_creation FROM GROUPINFO";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {

                JSONObject contact = new JSONObject();
                contact.put("unique_id", cursor.getString(0));
                contact.put("group_name", cursor.getString(1));
                contact.put("is_mute", cursor.getString(2));
                contact.put("date_creation", cursor.getString(3));
                groups.put(contact);

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return groups;
    }

    public JSONArray getMyGroups(String member_phone) throws JSONException {
        JSONArray groups = new JSONArray();

        String selectQuery = "SELECT unique_id, group_name, is_mute, date_creation FROM GROUPINFO WHERE isArchived=0 AND unique_id IN (SELECT group_unique_id FROM GROUPMEMBER WHERE membership_status = 'joined' AND member_phone = '"+ member_phone +"')";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {

                JSONObject contact = new JSONObject();
                contact.put("unique_id", cursor.getString(0));
                contact.put("group_name", cursor.getString(1));
                contact.put("is_mute", cursor.getString(2));
                contact.put("date_creation", cursor.getString(3));
                groups.put(contact);

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return groups;
    }

    public JSONArray getMyArchivedGroups(String member_phone) throws JSONException {
        JSONArray groups = new JSONArray();

        String selectQuery = "SELECT unique_id, group_name, is_mute, date_creation FROM GROUPINFO WHERE isArchived=1 AND unique_id IN (SELECT group_unique_id FROM GROUPMEMBER WHERE membership_status = 'joined' AND member_phone = '"+ member_phone +"')";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {

                JSONObject contact = new JSONObject();
                contact.put("unique_id", cursor.getString(0));
                contact.put("group_name", cursor.getString(1));
                contact.put("is_mute", cursor.getString(2));
                contact.put("date_creation", cursor.getString(3));
                groups.put(contact);

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return groups;
    }

    public boolean isMute(String group_id) throws JSONException {
        String selectQuery = "SELECT is_mute FROM GROUPINFO WHERE unique_id ='"+ group_id +"'" ;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        JSONObject contact = new JSONObject();
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {

                if(cursor.getString(0).equals("0")){
                    return false;
                }else{
                    return true;
                }
            }
        }
        cursor.close();
        db.close();
        // return user
        return false;
    }

    public void muteGroup(String group_id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues args = new ContentValues();
        args.put("is_mute", "1");
        db.update("GROUPINFO",args,"unique_id='"+group_id+"'",null);

        ContentValues values = new ContentValues();


        //Get Current Date
        Date date = new Date();
        SimpleDateFormat dateFormatWithZone = new SimpleDateFormat("hh:mm aa", Locale.getDefault());
        String currentDate = dateFormatWithZone.format(date);
        values.put("muteTime", currentDate);// values : 0 or 1
        values.put("unMuteTime", "");// values : left or joined
        values.put("isMute", "1"); //Muting
        // Inserting Row
        db.update("MUTESETTING", values, "groupid = '"+ group_id +"'", null);
        db.close(); // Closing database connection
    }

    public void unmuteGroup(String group_id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues args = new ContentValues();
        args.put("is_mute", "0");
        db.update("GROUPINFO",args,"unique_id='"+group_id+"'",null);

        ContentValues values = new ContentValues();
        Date date = new Date(); //Get Current Date
        SimpleDateFormat dateFormatWithZone = new SimpleDateFormat("hh:mm aa", Locale.getDefault());
        String currentTime = dateFormatWithZone.format(date);
        values.put("unMuteTime", currentTime);// values : left or joined
        values.put("isMute", "0"); //Unmuting
        db.update("MUTESETTING", values, "groupid = '" + group_id +"'" , null); // Updating Row
        db.close(); // Closing database connection
    }

    public JSONObject getGroupInfo(String group_id) throws JSONException {
        String selectQuery = "SELECT unique_id, group_name, is_mute, date_creation, isArchived FROM GROUPINFO WHERE unique_id ='"+ group_id +"'" ;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        JSONObject contact = new JSONObject();
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {

                contact.put("unique_id", cursor.getString(0));
                contact.put("group_name", cursor.getString(1));
                contact.put("is_mute", cursor.getString(2));
                contact.put("date_creation", cursor.getString(3));
                contact.put("isArchived", cursor.getString(4));



                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return contact;
    }

    public JSONObject getMyDetailsInGroup(String group_id) throws JSONException {
        String selectQuery = "SELECT  member_phone, isAdmin, date_joined, display_name  FROM GROUPMEMBER, "+ User.TABLE_USER_NAME +"  where group_unique_id='"+ group_id +"'"
                +" AND phone = member_phone AND membership_status != 'left'" ;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        JSONObject contact = new JSONObject();
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {
                contact.put(Contacts.CONTACT_PHONE, cursor.getString(0));
                contact.put("isAdmin", cursor.getString(1));
                contact.put("date_joined", cursor.getString(2));
                contact.put("display_name", cursor.getString(3));
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return contact;
    }

    public JSONObject getGroupMemberDetail(String group_id, String member_phone) throws JSONException {
        JSONArray contacts = new JSONArray();
        String selectQuery = "SELECT  member_phone, isAdmin, date_joined, display_name  FROM GROUPMEMBER, "+ Contacts.TABLE_CONTACTS +"  where group_unique_id='"+ group_id +"'"
                +" AND phone = '"+ member_phone +"'" ;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {
                JSONObject contact = new JSONObject();
                contact.put(Contacts.CONTACT_PHONE, cursor.getString(0));
                contact.put("isAdmin", cursor.getString(1));
                contact.put("date_joined", cursor.getString(2));
                contact.put("display_name", cursor.getString(3));
                return  contact;
                // contacts.put(contact);
//                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return null;
    }

    public JSONArray getGroupMembers(String group_id) throws JSONException {
        JSONArray contacts = new JSONArray();
        String selectQuery = "SELECT  member_phone, isAdmin, date_joined, display_name  FROM GROUPMEMBER LEFT JOIN "+ Contacts.TABLE_CONTACTS +" ON phone = member_phone where group_unique_id='"+ group_id +"' AND membership_status='joined'";
//        String selectQuery = "SELECT  member_phone, isAdmin, date_joined, display_name  FROM GROUPMEMBER, "+ Contacts.TABLE_CONTACTS +" where   group_unique_id='"+ group_id +"' AND phone = member_phone";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {
                JSONObject contact = new JSONObject();
                contact.put(Contacts.CONTACT_PHONE, cursor.getString(0));
                contact.put("isAdmin", cursor.getString(1));
                contact.put("date_joined", cursor.getString(2));
                contact.put("display_name", cursor.getString(3));
                contacts.put(contact);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return contacts;
    }

    public JSONArray getMembersNotInGroup(String group_id) throws JSONException {
        JSONArray contacts = new JSONArray();
        String selectQuery = "SELECT  * FROM " + Contacts.TABLE_CONTACTS +" where on_cloudkibo='true' AND contacts.phone NOT IN (SELECT  member_phone FROM GROUPMEMBER  where group_unique_id='"+ group_id + "' AND membership_status='joined')";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {

                JSONObject contact = new JSONObject();
                //contact.put(Contacts.CONTACT_FIRSTNAME, cursor.getString(1));
                //contact.put(Contacts.CONTACT_LASTNAME, cursor.getString(2));
                contact.put(Contacts.CONTACT_PHONE, cursor.getString(1));
                contact.put("display_name", cursor.getString(2));
                contact.put(Contacts.CONTACT_UID, cursor.getString(3));
                contact.put(Contacts.SHARED_DETAILS, cursor.getString(4));
                contact.put(Contacts.CONTACT_STATUS, cursor.getString(5));
                contact.put("on_cloudkibo", cursor.getString(6));

                contacts.put(contact);

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return contacts;
    }

    public JSONArray getGroupAdmins(String group_id) throws JSONException {
        JSONArray contacts = new JSONArray();
        String selectQuery = "SELECT  member_phone, isAdmin, date_joined FROM GROUPMEMBER WHERE group_unique_id='"+ group_id +"'"
                +" AND isAdmin = 1" ;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {
                JSONObject contact = new JSONObject();
                contact.put(Contacts.CONTACT_PHONE, cursor.getString(0));
                contact.put("isAdmin", cursor.getString(1));
                contact.put("date_joined", cursor.getString(2));
                contacts.put(contact);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return contacts;
    }

    public void addGroupMessage(String group_unique_id, String message, String from, String from_fullname, String unique_id, String msg_type) {
        SQLiteDatabase db = this.getWritableDatabase();


        // todo dayem siddiqui
        ContentValues values = new ContentValues();
//        values.put(User.KEY_UID, id); // FirstName    //values.put(User.KEY_FIRSTNAME, id); // FirstName
        values.put("group_unique_id", group_unique_id); // LastName
        values.put("_from", from); // Email
        values.put("type", msg_type); // UserName
        values.put("msg", message); // Email
        values.put("from_fullname", from_fullname); // Email
        values.put("unique_id", unique_id); // Created At
        values.put("date", Utility.getCurrentTimeInISO());

        // Inserting Row
        db.insert("GROUPCHAT", null, values);
        db.close(); // Closing database connection
    }

    public JSONArray getGroupMessages(String group_id) throws JSONException {
        JSONArray contacts = new JSONArray();
        String selectQuery = "SELECT  _from, type, msg, from_fullname, date, unique_id  FROM GROUPCHAT  where group_unique_id='"+ group_id +"'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {
                JSONObject contact = new JSONObject();
                contact.put("from", cursor.getString(0));
                contact.put("type", cursor.getString(1));
                contact.put("msg", cursor.getString(2));
                contact.put("from_fullname", cursor.getString(3));
                contact.put("date", cursor.getString(4));
                contact.put("unique_id", cursor.getString(5));
                contacts.put(contact);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return contacts;
    }

    public JSONArray getSentGroupMessagesForSync(String phone) throws JSONException {
        JSONArray contacts = new JSONArray();
        String selectQuery = "SELECT GROUPCHAT._from, GROUPCHAT.unique_id, GROUPCHATSTATUS.status  " +
                "FROM GROUPCHAT, GROUPCHATSTATUS  where GROUPCHAT.unique_id=GROUPCHATSTATUS.msg_unique_id " +
                "AND GROUPCHATSTATUS.status = 'sent' AND GROUPCHAT._from ='"+ phone +"'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {
                JSONObject contact = new JSONObject();
                contact.put("from", cursor.getString(0));
                contact.put("unique_id", cursor.getString(1));
                contact.put("status", cursor.getString(2));
                contacts.put(contact);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();

        selectQuery = "SELECT GROUPCHAT._from, GROUPCHAT.unique_id, GROUPCHATSTATUS.status  " +
                "FROM GROUPCHAT, GROUPCHATSTATUS  where GROUPCHAT.unique_id=GROUPCHATSTATUS.msg_unique_id " +
                "AND GROUPCHATSTATUS.status = 'delivered' AND GROUPCHAT._from ='"+ phone +"'";
        db = this.getReadableDatabase();
        cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {
                JSONObject contact = new JSONObject();
                contact.put("_from", cursor.getString(0));
                contact.put("unique_id", cursor.getString(1));
                contact.put("status", cursor.getString(2));
                contacts.put(contact);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();

        // return user
        return contacts;
    }

    public JSONArray getSentMessagesForSync(String phone) throws JSONException {
        JSONArray contacts = new JSONArray();
        String selectQuery = "SELECT uniqueid, status FROM userchat where status = 'sent' AND fromperson ='"+ phone +"'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {
                JSONObject contact = new JSONObject();
                contact.put("uniqueid", cursor.getString(0));
                contact.put("status", cursor.getString(1));
                contacts.put(contact);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();

        selectQuery = "SELECT uniqueid, status FROM userchat where status = 'delivered' AND fromperson ='"+ phone +"'";
        db = this.getReadableDatabase();
        cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {
                JSONObject contact = new JSONObject();
                contact.put("uniqueid", cursor.getString(0));
                contact.put("status", cursor.getString(1));
                contacts.put(contact);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();

        // return user
        return contacts;
    }

    /*
    * ===============================================
    * END OF GROUP DB LOGIC
    * ===============================================
    * */

    /////////////////////////////////////////////////////////////////////
    // Storing user details in database                                //
    /////////////////////////////////////////////////////////////////////


    public void addUser(String id, String display_name, String phone, String national_number, String country_prefix, String created_at) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(User.KEY_UID, id); // FirstName    //values.put(User.KEY_FIRSTNAME, id); // FirstName
        values.put("display_name", display_name); // LastName
        values.put("phone", phone); // Email
        values.put("national_number", national_number); // UserName
        values.put("country_prefix", country_prefix); // Email
        values.put(User.KEY_CREATED_AT, created_at); // Created At

        // Inserting Row
        db.insert(User.TABLE_USER_NAME, null, values);
        db.close(); // Closing database connection
    }






    /////////////////////////////////////////////////////////////////////
    // Storing contact details in database                             //
    /////////////////////////////////////////////////////////////////////


    public void addContact(String on_cloudkibo, String lname, String phone, String uname, String uid, String shareddetails,
    		String status) {


        ContentValues values = new ContentValues();
        //values.put(Contacts.CONTACT_FIRSTNAME, fname); // FirstName
        //values.put(Contacts.CONTACT_LASTNAME, lname); // LastName
        values.put(Contacts.CONTACT_PHONE, phone); // Phone
        values.put("display_name", uname); // UserName
        values.put(Contacts.CONTACT_UID, uid); // Uid
        values.put(Contacts.CONTACT_STATUS, status); // Status
        values.put(Contacts.SHARED_DETAILS, shareddetails); // Created At
        values.put("on_cloudkibo", on_cloudkibo);

        // Inserting Row
        try {
//            if(getContactName(phone) != null){
//                SQLiteDatabase db = this.getWritableDatabase();
//                db.update(Contacts.TABLE_CONTACTS,values,"phone='"+phone+"'",null);
//                db.close();
//            }else{
                SQLiteDatabase db = this.getWritableDatabase();
                db.replace(Contacts.TABLE_CONTACTS, null, values);
                db.close();
//            }
        } catch (android.database.sqlite.SQLiteConstraintException e){
            Log.e("SQLITE_CONTACTS", uname + " - " + phone);
            ACRA.getErrorReporter().handleSilentException(e);
        }
         // Closing database connection
    }

    public void addContact(String on_cloudkibo, String lname, String phone, String uname, String uid, String shareddetails,
                           String status, String image_uri) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        //values.put(Contacts.CONTACT_FIRSTNAME, fname); // FirstName
        //values.put(Contacts.CONTACT_LASTNAME, lname); // LastName
        values.put(Contacts.CONTACT_PHONE, phone); // Phone
        values.put("display_name", uname); // UserName
        values.put(Contacts.CONTACT_UID, uid); // Uid
        values.put(Contacts.CONTACT_STATUS, status); // Status
        values.put(Contacts.SHARED_DETAILS, shareddetails); // Created At
        values.put("on_cloudkibo", on_cloudkibo);
        values.put("image_uri", image_uri);


        // Inserting Row
        try {
//            String name  = getContactName(phone);
//            if(name != null){
//                db.update(Contacts.TABLE_CONTACTS,values,"phone='"+phone+"'",null);
//                Log.d("ADDED", "Updated the contact: " + name);
//            }else{
                db.replace(Contacts.TABLE_CONTACTS, null, values);
            Log.e("ADDED","Contact Added");
//            }
        } catch (android.database.sqlite.SQLiteConstraintException e){
            Log.e("SQLITE_CONTACTS", uname + " - " + phone);
            ACRA.getErrorReporter().handleSilentException(e);
        }
        db.close(); // Closing database connection
    }




    /////////////////////////////////////////////////////////////////////
    // Storing userchat details in database                            //
    /////////////////////////////////////////////////////////////////////


    public void addChat(String to, String from, String from_fullname, String msg, String date, String status,
                         String uniqueid, String type, String file_type) {

        String myPhone = getUserDetails().get("phone");
        String contactPhone = "";
        if(myPhone.equals(to))  contactPhone = from;
        if(myPhone.equals(from))  contactPhone = to;

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UserChat.USERCHAT_TO, to); // TO
        values.put(UserChat.USERCHAT_FROM, from); // FROM
        values.put(UserChat.USERCHAT_FROM_FULLNAME, from_fullname); // FROM FULL NAME
        values.put(UserChat.USERCHAT_MSG, msg); // CHAT MESSAGE
        values.put(UserChat.USERCHAT_DATE, date); // DATE
        values.put("status", status); // status: pending, sent, delivered, seen
        values.put("uniqueid", uniqueid);
        values.put("type", type);
        values.put("file_type", file_type);
        values.put("contact_phone", contactPhone); // Contact

        // Inserting Row
        db.insert(UserChat.TABLE_USERCHAT, null, values);
        db.close(); // Closing database connection
    }

    public void addGroupChat(String from, String from_fullname, String msg, String date, String type,
                        String uniqueid, String group_unique_id) {


        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("_from", from); // FROM
        values.put("from_fullname", from_fullname); // FROM FULL NAME
        values.put(UserChat.USERCHAT_MSG, msg); // CHAT MESSAGE
        values.put(UserChat.USERCHAT_DATE, date); // DATE
        values.put("unique_id", uniqueid);
        values.put("group_unique_id", group_unique_id); // Contact
        values.put("type", type); // Contact

        // Inserting Row
        db.insert("GROUPCHAT", null, values);
        db.close(); // Closing database connection
    }

    public void updateChat(String status, String uniqueid) {

        SQLiteDatabase db = this.getWritableDatabase();

        String updateQuery = "UPDATE " + UserChat.TABLE_USERCHAT +
                " SET status='"+ status +"' WHERE uniqueid='"+uniqueid+"'";

        try {
            db.execSQL(updateQuery);
        } catch (Exception e){
            e.printStackTrace();
        }

        db.close();
    }

    public void setArchive(String contactPhone) {

        SQLiteDatabase db = this.getWritableDatabase();

        String updateQuery = "UPDATE " + UserChat.TABLE_USERCHAT +
                " SET isArchived="+ 1 +" WHERE contact_phone='"+contactPhone+"'";

        try {
            db.execSQL(updateQuery);
        } catch (Exception e){
            e.printStackTrace();
        }

        db.close();
    }

    public void setArchiveGroup(String unique_id) {

        SQLiteDatabase db = this.getWritableDatabase();

        String updateQuery = "UPDATE GROUPCHAT" +
                " SET isArchived="+ 1 +" WHERE group_unique_id='"+unique_id+"'";
        String updateQuery1 = "UPDATE GROUPINFO" +
                " SET isArchived="+ 1 +" WHERE unique_id='"+unique_id+"'";

        try {
            db.execSQL(updateQuery1);
            db.execSQL(updateQuery);

        } catch (Exception e){
            e.printStackTrace();
        }

        db.close();
    }

    public void unArchive(String contactPhone) {

        SQLiteDatabase db = this.getWritableDatabase();

        String updateQuery = "UPDATE " + UserChat.TABLE_USERCHAT +
                " SET isArchived="+ 0 +" WHERE contact_phone='"+contactPhone+"'";

        try {
            db.execSQL(updateQuery);
        } catch (Exception e){
            e.printStackTrace();
        }

        db.close();
    }

    public void unArchiveGroup(String unique_id) {

        SQLiteDatabase db = this.getWritableDatabase();

        String updateQuery = "UPDATE GROUPINFO" +
                " SET isArchived="+ 0 +" WHERE unique_id='"+unique_id+"'";
        String updateQuery1 = "UPDATE GROUPCHAT" +
                " SET isArchived="+ 0 +" WHERE group_unique_id='"+unique_id+"'";

        try {
            db.execSQL(updateQuery1);
            db.execSQL(updateQuery);
        } catch (Exception e){
            e.printStackTrace();
        }

        db.close();
    }




    public void updateContact(String status, String phone, String id) {

        SQLiteDatabase db = this.getWritableDatabase();

        String updateQuery = "UPDATE " + Contacts.TABLE_CONTACTS +
                " SET "+ Contacts.CONTACT_STATUS +"='"+ status +"', " + Contacts.CONTACT_UID +"='"+ id +"', "+
                " blocked_me='false', blocked_by_me='false' "+
                " WHERE "+ Contacts.CONTACT_PHONE +"='"+phone+"'";

        try {
            db.execSQL(updateQuery);
        } catch (Exception e){
            e.printStackTrace();
        }

        db.close();
    }

    public void blockContact(String phone){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("blocked_by_me", "true");
        db.update(Contacts.TABLE_CONTACTS,values,"phone='"+phone+"'",null);
        db.close(); // Closing database connection
    }

    public void unBlockContact(String phone){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("blocked_by_me", "false");
        db.update(Contacts.TABLE_CONTACTS,values,"phone='"+phone+"'",null);
        db.close(); // Closing database connection
    }

    public void blockedMe(String phone){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("blocked_me", "true");
        db.update(Contacts.TABLE_CONTACTS,values,"phone='"+phone+"'",null);
        db.close(); // Closing database connection
    }

    public void unBlockedMe(String phone){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("blocked_me", "false");
        db.update(Contacts.TABLE_CONTACTS,values,"phone='"+phone+"'",null);
        db.close(); // Closing database connection
    }

    /////////////////////////////////////////////////////////////////////
    // Storing userchat details in database                            //
    /////////////////////////////////////////////////////////////////////


    public void addCallHistory(String type, String contact_phone) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("type", type); // values : placed, received, missed
        values.put("contact_phone", contact_phone); // FROM

        // Inserting Row
        db.insert("call_history", null, values);
        db.close(); // Closing database connection
    }

    public void addChatSyncHistory(String status, String uniqueid, String fromperson) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("status", status);
        values.put("uniqueid", uniqueid);
        values.put("fromperson", fromperson);

        // Inserting Row
        db.insert("chat_history_sync", null, values);
        db.close(); // Closing database connection
    }

    public void addGroupChatSyncHistory(String status, String uniqueid, String fromperson) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("status", status);
        values.put("uniqueid", uniqueid);
        values.put("fromperson", fromperson);

        // Inserting Row
        db.insert("group_chat_history_sync", null, values);
        db.close(); // Closing database connection
    }




    /////////////////////////////////////////////////////////////////////
    // Getting user data from database                                 //
    /////////////////////////////////////////////////////////////////////


    public HashMap<String, String> getUserDetails(){
        HashMap<String,String> user = new HashMap<String,String>();
        String selectQuery = "SELECT * FROM " + User.TABLE_USER_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            user.put(User.KEY_UID, cursor.getString(1));
            user.put("display_name", cursor.getString(2));
            user.put("phone", cursor.getString(3));
            user.put("national_number", cursor.getString(4));
            user.put("country_prefix", cursor.getString(5));
            user.put(User.KEY_CREATED_AT, cursor.getString(6));
        }
        cursor.close();
        //db.close();
        //
        // I am commenting this line here because when we try to access same db instance from different running threads, app crashes
                        //Please refer to this link
        /*
        * http://stackoverflow.com/questions/23293572/android-cannot-perform-this-operation-because-the-connection-pool-has-been-clos
        * second answer by 'handhand'
        * */
        // return user
        return user;
    }





    /////////////////////////////////////////////////////////////////////
    // Getting contacts data from database                             //
    /////////////////////////////////////////////////////////////////////


    public JSONArray getContactsWithImages() throws JSONException {
        JSONArray contacts = new JSONArray();
//        String selectQuery = "SELECT  member_phone, isAdmin, date_joined, display_name  FROM GROUPMEMBER LEFT JOIN "+ Contacts.TABLE_CONTACTS +" ON phone = member_phone where group_unique_id='"+ group_id +"' AND membership_status='joined'";
//        String selectQuery = "SELECT  contacts.phone, display_name, _id, detailsshared, status, on_cloudkibo, image_uri FROM " + Contacts.TABLE_CONTACTS +" LEFT JOIN CONTACT_IMAGE ON contacts.phone = CONTACT_IMAGE.phone where on_cloudkibo='true'";
        String selectQuery = "SELECT  contacts.phone, display_name, _id, detailsshared, status, on_cloudkibo, image_uri FROM " + Contacts.TABLE_CONTACTS +" where on_cloudkibo='true' and blocked_by_me='false' and blocked_me='false'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {

                JSONObject contact = new JSONObject();
                //contact.put(Contacts.CONTACT_FIRSTNAME, cursor.getString(1));
                //contact.put(Contacts.CONTACT_LASTNAME, cursor.getString(2));
                contact.put(Contacts.CONTACT_PHONE, cursor.getString(0));
                contact.put("display_name", cursor.getString(1));
                contact.put(Contacts.CONTACT_UID, cursor.getString(2));
                contact.put(Contacts.SHARED_DETAILS, cursor.getString(3));
                contact.put(Contacts.CONTACT_STATUS, cursor.getString(4));
                contact.put("on_cloudkibo", cursor.getString(5));
                contact.put("image_uri", cursor.getString(6));

                contacts.put(contact);

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return contacts;
    }

    public JSONArray getContacts() throws JSONException {
    	JSONArray contacts = new JSONArray();
        String selectQuery = "SELECT  * FROM " + Contacts.TABLE_CONTACTS +" where on_cloudkibo='true' and blocked_by_me='false' and blocked_me='false'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

        	while (cursor.isAfterLast() != true) {

        		JSONObject contact = new JSONObject();
        		//contact.put(Contacts.CONTACT_FIRSTNAME, cursor.getString(1));
        		//contact.put(Contacts.CONTACT_LASTNAME, cursor.getString(2));
        		contact.put(Contacts.CONTACT_PHONE, cursor.getString(1));
        		contact.put("display_name", cursor.getString(2));
        		contact.put(Contacts.CONTACT_UID, cursor.getString(3));
        		contact.put(Contacts.SHARED_DETAILS, cursor.getString(4));
        		contact.put(Contacts.CONTACT_STATUS, cursor.getString(5));
                contact.put("on_cloudkibo", cursor.getString(6));

        		contacts.put(contact);

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return contacts;
    }

    public JSONArray getContactsBlockedByMe() throws JSONException {
        JSONArray contacts = new JSONArray();
        String selectQuery = "SELECT _id, display_name, image_uri, phone, on_cloudkibo FROM " + Contacts.TABLE_CONTACTS +" where on_cloudkibo='true' and blocked_by_me='true'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {

                JSONObject contact = new JSONObject();
                contact.put("_id", cursor.getString(0));
                contact.put("display_name", cursor.getString(1));
                contact.put("image_uri", cursor.getString(2));
                contact.put(Contacts.CONTACT_PHONE, cursor.getString(3));
                contact.put("on_cloudkibo", cursor.getString(4));

                contacts.put(contact);

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return contacts;
    }

    public JSONArray getContactsWhoBlockedMe() throws JSONException {
        JSONArray contacts = new JSONArray();
        String selectQuery = "SELECT _id, display_name, image_uri, phone FROM " + Contacts.TABLE_CONTACTS +" where on_cloudkibo='true' and blocked_me='true'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {

                JSONObject contact = new JSONObject();
                contact.put("_id", cursor.getString(0));
                contact.put("display_name", cursor.getString(1));
                contact.put("image_uri", cursor.getString(2));
                contact.put(Contacts.CONTACT_PHONE, cursor.getString(3));

                contacts.put(contact);

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return contacts;
    }

    public String [] getContactsPhone() throws JSONException {
        String contacts[];
        String selectQuery = "SELECT  phone FROM " + Contacts.TABLE_CONTACTS + "";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
       contacts = new String [cursor.getCount()];
        int i = 0;
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {

                //contact.put(Contacts.CONTACT_FIRSTNAME, cursor.getString(1));
                //contact.put(Contacts.CONTACT_LASTNAME, cursor.getString(2));
//                contact.put(Contacts.CONTACT_PHONE, cursor.getString(1));
                contacts[i] = cursor.getString(0);
                i++;
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return contacts;
    }

    public JSONArray getContactsOnAddressBookWithImages() throws JSONException {
        JSONArray contacts = new JSONArray();
        //String selectQuery = "SELECT  contacts.phone, display_name, _id, detailsshared, status, on_cloudkibo, image_uri FROM " + Contacts.TABLE_CONTACTS +" LEFT JOIN CONTACT_IMAGE ON contacts.phone = CONTACT_IMAGE.phone where on_cloudkibo='false'";
        String selectQuery = "SELECT  contacts.phone, display_name, _id, detailsshared, status, on_cloudkibo, image_uri FROM " + Contacts.TABLE_CONTACTS +" where on_cloudkibo='false'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {

                JSONObject contact = new JSONObject();
                //contact.put(Contacts.CONTACT_FIRSTNAME, cursor.getString(1));
                //contact.put(Contacts.CONTACT_LASTNAME, cursor.getString(2));
                //contact.put(Contacts.CONTACT_FIRSTNAME, cursor.getString(1));
                //contact.put(Contacts.CONTACT_LASTNAME, cursor.getString(2));
                contact.put(Contacts.CONTACT_PHONE, cursor.getString(0));
                contact.put("display_name", cursor.getString(1));
                contact.put(Contacts.CONTACT_UID, cursor.getString(2));
                contact.put(Contacts.SHARED_DETAILS, cursor.getString(3));
                contact.put(Contacts.CONTACT_STATUS, cursor.getString(4));
                contact.put("on_cloudkibo", cursor.getString(5));
                contact.put("image_uri", cursor.getString(6));

                contacts.put(contact);

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return contacts;
    }

    public JSONArray getSpecificContact(String phone) throws JSONException {
        JSONArray contacts = new JSONArray();
        String selectQuery = "SELECT phone, display_name, _id, detailsshared, status, on_cloudkibo, blocked_by_me, blocked_me FROM " + Contacts.TABLE_CONTACTS +" where phone='"+ phone +"'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {

                JSONObject contact = new JSONObject();
                contact.put(Contacts.CONTACT_PHONE, cursor.getString(0));
                contact.put("display_name", cursor.getString(1));
                contact.put(Contacts.CONTACT_UID, cursor.getString(2));
                contact.put(Contacts.SHARED_DETAILS, cursor.getString(3));
                contact.put(Contacts.CONTACT_STATUS, cursor.getString(4));
                contact.put("on_cloudkibo", cursor.getString(5));
                contact.put("blocked_by_me", cursor.getString(6));
                contact.put("blocked_me", cursor.getString(7));

                contacts.put(contact);

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return contacts;
    }

    public String getDisplayName(String phone) throws JSONException {
        String selectQuery = "SELECT  display_name FROM " + Contacts.TABLE_CONTACTS +" where phone='"+ phone +"'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {
                return  cursor.getString(0);
            }
        }
        cursor.close();
        db.close();
        // return user
        return "";
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
        		UserChat.USERCHAT_FROM +" = '"+ user1 +"') order by date";

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
                contact.put(UserChat.USERCHAT_DATE, cursor.getString(5));
        		contact.put("status", cursor.getString(6));
                contact.put("type", cursor.getString(7));
                contact.put("file_type", cursor.getString(8));
        		contact.put("uniqueid", cursor.getString(9));
                contact.put("contact_phone", cursor.getString(11));

        		chats.put(contact);

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return chats;
    }

    // note don't use this, it seems incorrect
    public JSONArray getChat() throws JSONException {
        JSONArray chats = new JSONArray();
        String selectQuery = "SELECT  * FROM " + UserChat.TABLE_USERCHAT;

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
                contact.put("contact_phone", cursor.getString(7));

                chats.put(contact);

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return chats;
    }

    public JSONArray getSpecificChat(String uniqueid) throws JSONException {
        JSONArray chats = new JSONArray();
        String selectQuery = "SELECT id, toperson FROM " + UserChat.TABLE_USERCHAT +" where uniqueid='"+ uniqueid +"'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {

                JSONObject contact = new JSONObject();
                contact.put(UserChat.USERCHAT_TO, cursor.getString(1));

                chats.put(contact);

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return chats;
    }

    public JSONArray getPendingChat() throws JSONException {
        JSONArray chats = new JSONArray();
      
        String selectQuery = "SELECT  id, toperson, fromperson, fromFullName, msg, date, contact_phone, status, type, file_type, uniqueid FROM " + UserChat.TABLE_USERCHAT +" WHERE status='pending'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {

                JSONObject contact = new JSONObject();

                contact.put("to", cursor.getString(1));
                contact.put("from", cursor.getString(2));
                contact.put(UserChat.USERCHAT_FROM_FULLNAME, cursor.getString(3));
                contact.put(UserChat.USERCHAT_MSG, cursor.getString(4));
                contact.put(UserChat.USERCHAT_DATE, cursor.getString(5));
                contact.put("contact_phone", cursor.getString(6));
                contact.put("status", cursor.getString(7));
                contact.put("type", cursor.getString(8));
                contact.put("file_type", cursor.getString(9));
                contact.put("uniqueid", cursor.getString(10));

                chats.put(contact);

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return chats;
    }

    public JSONArray getPendingGroupChat() throws JSONException {
            JSONArray contacts = new JSONArray();
            String selectQuery = "SELECT  _from, type, msg, from_fullname, date, unique_id, group_unique_id  FROM GROUPCHAT, GROUPCHATSTATUS where unique_id = msg_unique_id AND status = 'pending'";
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);
            // Move to first row
            cursor.moveToFirst();
            if(cursor.getCount() > 0){

                while (cursor.isAfterLast() != true) {
                    JSONObject contact = new JSONObject();
                    contact.put("from", cursor.getString(0));
                    contact.put("type", cursor.getString(1));
                    contact.put("msg", cursor.getString(2));
                    contact.put("from_fullname", cursor.getString(3));
                    contact.put("date", cursor.getString(4));
                    contact.put("unique_id", cursor.getString(5));
                    contact.put("group_unique_id", cursor.getString(6));
                    contacts.put(contact);
                    cursor.moveToNext();
                }
            }
            cursor.close();
            db.close();
            // return user
            return contacts;
    }

    public JSONArray getChatHistoryStatus() throws JSONException {
        JSONArray chats = new JSONArray();
        String selectQuery = "SELECT uniqueid, status, fromperson FROM chat_history_sync";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {

                JSONObject contact = new JSONObject();
                contact.put("uniqueid", cursor.getString(0));
                contact.put("status", cursor.getString(1));
                contact.put("sender", cursor.getString(2));

                chats.put(contact);

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return chats;
    }

    public JSONArray getGroupChatHistoryStatus() throws JSONException {
        JSONArray chats = new JSONArray();
        String selectQuery = "SELECT uniqueid, status, fromperson FROM group_chat_history_sync";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {

                JSONObject contact = new JSONObject();
                contact.put("chat_unique_id", cursor.getString(0));
                contact.put("status", cursor.getString(1));
                contact.put("from", cursor.getString(2));

                chats.put(contact);

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return chats;
    }

    public JSONArray getChatListWithImages() throws JSONException {
        HashMap<String, String> userDetail = getUserDetails();
        JSONArray chats = new JSONArray();
//        String selectQuery = "SELECT  contacts.phone, display_name, _id, detailsshared, status, on_cloudkibo, image_uri FROM " + Contacts.TABLE_CONTACTS +" LEFT JOIN CONTACT_IMAGE ON contacts.phone = CONTACT_IMAGE.phone where on_cloudkibo='true'";
//        String selectQuery =
//                " SELECT "+ UserChat.USERCHAT_DATE +", contact_phone, " + UserChat.USERCHAT_MSG
//                        +", image_uri FROM " + UserChat.TABLE_USERCHAT
//                        +" LEFT JOIN CONTACT_IMAGE ON contact_phone = CONTACT_IMAGE.phone WHERE isArchived=0"
//                        +" GROUP BY contact_phone ORDER BY "+ UserChat.USERCHAT_DATE + " DESC";
        String selectQuery =
                " SELECT "+ UserChat.USERCHAT_DATE +", contact_phone, " + UserChat.USERCHAT_MSG
                        +", image_uri FROM " + UserChat.TABLE_USERCHAT
                        +" LEFT JOIN contacts ON contacts.phone = contact_phone WHERE"
                        +" isArchived=0 and blocked_me='false' and blocked_by_me='false'"
                        +" GROUP BY contact_phone ORDER BY "+ UserChat.USERCHAT_DATE + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {
                for(int i=0; i<cursor.getColumnCount(); i++)
                    Log.d("DatabaseHandler", cursor.getColumnName(i) +" - "+ cursor.getString(i));
                JSONArray contactInAddressBook = getSpecificContact(cursor.getString(1));
                JSONArray lastMessage = getLastMessageInChat(userDetail.get("phone"), cursor.getString(1));

                JSONObject contact = new JSONObject();
                contact.put("date", cursor.getString(0));
                contact.put("contact_phone", cursor.getString(1));
                contact.put("image_uri", cursor.getString(3));
                contact.put("msg", lastMessage.getJSONObject(0).getString("msg"));
                //contact.put("msg", cursor.getString(2));
                contact.put("pendingMsgs", getUnReadMessagesCount(cursor.getString(1)));
                if(contactInAddressBook.length() > 0) {
                    contact.put("display_name", contactInAddressBook.getJSONObject(0).getString("display_name"));
                } else {
                    contact.put("display_name", cursor.getString(1));
                }

                chats.put(contact);

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return chats;
    }


    public JSONArray getChatList() throws JSONException {
        HashMap<String, String> userDetail = getUserDetails();
        JSONArray chats = new JSONArray();
        String selectQuery =
                " SELECT "+ UserChat.USERCHAT_DATE +", contact_phone, " + UserChat.USERCHAT_MSG
                +" FROM " + UserChat.TABLE_USERCHAT
                        +" WHERE isArchived=0"
                +" GROUP BY contact_phone ORDER BY "+ UserChat.USERCHAT_DATE + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {
                JSONArray contactInAddressBook = getSpecificContact(cursor.getString(1));
                JSONArray lastMessage = getLastMessageInChat(userDetail.get("phone"), cursor.getString(1));

                JSONObject contact = new JSONObject();
                contact.put("date", cursor.getString(0));
                contact.put("contact_phone", cursor.getString(1));
                contact.put("msg", lastMessage.getJSONObject(0).getString("msg"));
                contact.put("msg", lastMessage.getJSONObject(0).getString("msg"));
                //contact.put("msg", cursor.getString(2));
                contact.put("pendingMsgs", getUnReadMessagesCount(cursor.getString(1)));
                if(contactInAddressBook.length() > 0) {
                    contact.put("display_name", contactInAddressBook.getJSONObject(0).getString("display_name"));
                } else {
                    contact.put("display_name", cursor.getString(1));
                }

                chats.put(contact);

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return chats;
    }

    public JSONArray getGroupChatList() throws JSONException {
        HashMap<String, String> userDetail = getUserDetails();
        JSONArray chats = new JSONArray();
        String selectQuery =
                " SELECT group_unique_id, date, msg"
                        +" FROM GROUPCHAT "
                        +" WHERE isArchived=0"
                        +" GROUP BY group_unique_id ORDER BY date DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {
                JSONObject groupInfo = getGroupInfo(cursor.getString(0));
                JSONArray lastMessage = getLastMessageInGroupChat(cursor.getString(0));
                JSONArray lastSender = getLastMessageSenderInGroupChat(cursor.getString(0));

                JSONObject group = new JSONObject();
                group.put("unique_id", cursor.getString(0));
                if(lastSender.length() > 0) {
                    JSONArray contactInAddressBook = getSpecificContact(lastSender.getJSONObject(0).getString("_from"));
                    if (contactInAddressBook.length() > 0) {
                        group.put("last_sender", contactInAddressBook.getJSONObject(0).getString("display_name"));
                    } else {
                        group.put("last_sender", lastSender.getJSONObject(0).getString("from_fullname"));
                    }
                } else {
                    group.put("last_sender", "New Group");
                }
                group.put("date_creation", cursor.getString(1));
                group.put("msg", lastMessage.getJSONObject(0).getString("msg"));
                //group.put("pendingMsgs", getUnReadMessagesCount(cursor.getString(1)));
                group.put("group_name", groupInfo.optString("group_name"));

                chats.put(group);

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return chats;
    }

    public JSONArray getArchivedChatList() throws JSONException {
        HashMap<String, String> userDetail = getUserDetails();
        JSONArray chats = new JSONArray();
        String selectQuery =
                " SELECT "+ UserChat.USERCHAT_DATE +", contact_phone, " + UserChat.USERCHAT_MSG
                        +" FROM " + UserChat.TABLE_USERCHAT
                        +" WHERE isArchived=1"
                        +" GROUP BY contact_phone ORDER BY "+ UserChat.USERCHAT_DATE + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {
                JSONArray contactInAddressBook = getSpecificContact(cursor.getString(1));
                JSONArray lastMessage = getLastMessageInChat(userDetail.get("phone"), cursor.getString(1));

                JSONObject contact = new JSONObject();
                contact.put("date", cursor.getString(0));
                contact.put("contact_phone", cursor.getString(1));
                contact.put("msg", lastMessage.getJSONObject(0).getString("msg"));
                contact.put("msg", lastMessage.getJSONObject(0).getString("msg"));
                //contact.put("msg", cursor.getString(2));
                contact.put("pendingMsgs", getUnReadMessagesCount(cursor.getString(1)));
                if(contactInAddressBook.length() > 0) {
                    contact.put("display_name", contactInAddressBook.getJSONObject(0).getString("display_name"));
                } else {
                    contact.put("display_name", cursor.getString(1));
                }

                chats.put(contact);

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return chats;
    }




    public JSONArray getCallHistory() throws JSONException {
        JSONArray chats = new JSONArray();

        String selectQuery = "SELECT * FROM call_history, "+ Contacts.TABLE_CONTACTS
                +" WHERE call_history.contact_phone = "+ Contacts.TABLE_CONTACTS +"."+ Contacts.CONTACT_PHONE
                +" ORDER BY call_date DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {

                JSONObject contact = new JSONObject();
                contact.put("call_date", cursor.getString(1));
                contact.put("type", cursor.getString(2));
                contact.put("contact_phone", cursor.getString(3));
                contact.put("display_name", cursor.getString(6));
                contact.put("contact_id", cursor.getString(7));

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

    public JSONArray getLastMessageInGroupChat(String unique_id) throws JSONException {
        JSONArray chats = new JSONArray();
        String selectQuery = "SELECT msg FROM GROUPCHAT WHERE group_unique_id='"+ unique_id+"' order by date DESC LIMIT 1";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {

                JSONObject contact = new JSONObject();
                contact.put("msg", cursor.getString(0));

                chats.put(contact);

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return chats;
    }

    public JSONArray getLastMessageSenderInGroupChat(String unique_id) throws JSONException {
        JSONArray chats = new JSONArray();
        String selectQuery = "SELECT _from, from_fullname FROM GROUPCHAT WHERE group_unique_id='"+ unique_id+"' AND _from!='"+ getUserDetails().get("phone")+"' order by date DESC LIMIT 1";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){

            while (cursor.isAfterLast() != true) {

                // todo _from should not be the user of app and sync all messages of group
                JSONObject contact = new JSONObject();
                contact.put("_from", cursor.getString(0));
                contact.put("from_fullname", cursor.getString(1));

                chats.put(contact);

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return chats;
    }

    public JSONArray getLastMessageInChat(String user1, String user2) throws JSONException {
        JSONArray chats = new JSONArray();
        String selectQuery = "SELECT  * FROM " + UserChat.TABLE_USERCHAT + " WHERE ("+
                UserChat.USERCHAT_TO +" = '"+ user1 +"' AND "+
                UserChat.USERCHAT_FROM +" = '"+ user2 +"') OR ("+
                UserChat.USERCHAT_TO +" = '"+ user2 +"' AND "+
                UserChat.USERCHAT_FROM +" = '"+ user1 +"') order by date DESC LIMIT 1";

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
                contact.put(UserChat.USERCHAT_DATE, cursor.getString(5));
                contact.put("status", cursor.getString(6));
                contact.put("uniqueid", cursor.getString(7));
                contact.put("contact_phone", cursor.getString(8));

                chats.put(contact);

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return chats;
    }

    public int getUnReadMessagesCount(String contact_phone) {
        String countQuery = "SELECT  * FROM " + UserChat.TABLE_USERCHAT + " WHERE status = 'delivered' AND fromperson = '"+ contact_phone +"'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int rowCount = cursor.getCount();
        db.close();
        cursor.close();

        // return row count
        return rowCount;
    }

    public int getUnReadMessagesCountInGroupChat(String unique_id) {
        // todo under construction: status needs to be fetched from other table by join
        String countQuery = "SELECT * FROM GROUPCHAT WHERE status = 'delivered' AND group_unique_id = '"+ unique_id +"'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int rowCount = cursor.getCount();
        db.close();
        cursor.close();

        // return row count
        return rowCount;
    }

    public int getAllMessagesCountInGroupChat(String unique_id) {
        // todo under construction: status needs to be fetched from other table by join
        String countQuery = "SELECT  _from, type, msg, from_fullname, date, unique_id  FROM GROUPCHAT  where group_unique_id='"+ unique_id +"'";
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
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            // Delete All Rows
            db.delete(User.TABLE_USER_NAME, null, null);
            db.close();
        }catch(SQLiteDatabaseLockedException e){
            e.printStackTrace();
        }
    }

    /**
     * Delete all contacts Table
     * */
    public void resetContactsTable(){
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            // Delete All Rows
            db.delete(Contacts.TABLE_CONTACTS, null, null);
            db.close();
        }catch(SQLiteDatabaseLockedException e){
            e.printStackTrace();
        }
    }

    /**
     * Delete all chats Table
     * */
    public void resetChatsTable(){
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            // Delete All Rows
            db.delete(UserChat.TABLE_USERCHAT, null, null);
            db.close();
        }catch(SQLiteDatabaseLockedException e){
            e.printStackTrace();
        }
    }

    public void resetCallHistoryTable(){
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            // Delete All Rows
            db.delete("call_history", null, null);
            db.close();
        }catch(SQLiteDatabaseLockedException e){
            e.printStackTrace();
        }
    }

    public void resetContactImageTable(){
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            // Delete All Rows
            db.delete("CONTACT_IMAGE", null, null);
            db.close();
        }catch(SQLiteDatabaseLockedException e){
            e.printStackTrace();
        }
    }

    public void resetChatHistorySync(){
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            // Delete All Rows
            db.delete("chat_history_sync", null, null);
            db.close();
        }catch(SQLiteDatabaseLockedException e){
            e.printStackTrace();
        }
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

    public void resetSpecificContact(String user1, String user2){
        SQLiteDatabase db = this.getWritableDatabase();

        String deleteQuery = "DELETE FROM " + Contacts.TABLE_CONTACTS + " WHERE "+ Contacts.CONTACT_USERNAME + "='"+ user2 +"'";

        db.execSQL(deleteQuery);
        db.close();
    }

    public void resetSpecificChatHistorySync(String uniqueid){
        SQLiteDatabase db = this.getWritableDatabase();

        String deleteQuery = "DELETE FROM chat_history_sync WHERE uniqueid='"+ uniqueid +"'";

        db.execSQL(deleteQuery);
        db.close();
    }

    public void resetSpecificGroupChatHistorySync(String uniqueid){
        SQLiteDatabase db = this.getWritableDatabase();

        String deleteQuery = "DELETE FROM group_chat_history_sync WHERE uniqueid='"+ uniqueid +"'";

        db.execSQL(deleteQuery);
        db.close();
    }

}
