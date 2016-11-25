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

public class DatabaseHandler extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 12;

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
                + "on_cloudkibo" + " TEXT"+ ")";
        db.execSQL(CREATE_CONTACTS_TABLE);

        String CREATE_USERCHAT_TABLE = "CREATE TABLE " + UserChat.TABLE_USERCHAT + "("
                + UserChat.USERCHAT_ID + " INTEGER PRIMARY KEY, "
                + UserChat.USERCHAT_TO + " TEXT, "
                + UserChat.USERCHAT_FROM + " TEXT, "
                + UserChat.USERCHAT_FROM_FULLNAME + " TEXT, "
                + UserChat.USERCHAT_MSG + " TEXT, "
                + UserChat.USERCHAT_DATE + " TEXT, "
                + "status" + " TEXT, "
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

        String CREATE_GROUP_CHAT = "CREATE TABLE GROUPCHAT ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "group_unique_id TEXT, "
                + "_from TEXT, "
                + "type TEXT, "
                + "msg TEXT, "
                + "isArchived" + " INTEGER DEFAULT 0 , "
                + "from_fullname TEXT, "
                + "date DATETIME DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME')), "
                + "unique_id TEXT UNIQUE"
                + ")";
        db.execSQL(CREATE_GROUP_CHAT);

        String CREATE_GROUP_CHAT_STATUS = "CREATE TABLE GROUPCHATSTATUS ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "msg_unique_id TEXT UNIQUE, "
                + "status TEXT, "
                + "user_phone TEXT, "
                + "read_date DATETIME, "
                + "delivered_date DATETIME "
                + ")";
        db.execSQL(CREATE_GROUP_CHAT_STATUS);

        String CREATE_GROUP_MUTE_SETTINGS = "CREATE TABLE MUTESETTING ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "groupid TEXT, "
                + "isMute TEXT, "
                + "muteTime TEXT, "
                + "unMuteTime TEXT "
                + ")";
        db.execSQL(CREATE_GROUP_MUTE_SETTINGS);

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
        db.execSQL("DROP TABLE IF EXISTS MUTESETTING");

        // Create tables again
        onCreate(db);
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
    public void syncGroup(String unique_id, String group_name, int isMute, String date) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("group_name", group_name); // values : Group Name
        values.put("unique_id", unique_id); // values : random string
        values.put("is_mute", isMute);// values : 0 or 1
        values.put("date_creation", date);// values : 0 or 1

        db.insert("GROUPINFO", null, values); // Inserting Row


        ContentValues args = new ContentValues();
        args.put("groupid", unique_id); // values : random string
        args.put("isMute", isMute);// values : 0 or 1
        args.put("muteTime", "");// values : 0 or 1
        args.put("unMuteTime", "");// values : 0 or 1
        db.insert("MUTESETTING", null, args);

        db.close(); // Closing database connection
    }
    /*
    * This Method is used to add group member to a group
    * */
    public void addGroupMember(String group_unique_id, String member_phone, int isAdmin, String membership_status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("group_unique_id", group_unique_id); // values : Group Name
        values.put("member_phone", member_phone); //
        values.put("isAdmin", isAdmin);// values : 0 or 1
        values.put("membership_status", membership_status);// values : left or joined
        // Inserting Row
        db.insert("GROUPMEMBER", null, values);
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

    public void updateGroupChatStatus(String msg_unique_id, String status){
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues args = new ContentValues();
            args.put("status", status);
            db.update("GROUPCHATSTATUS",args,"msg_unique_id='"+msg_unique_id+"'",null);
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

    public void syncGroupMember(String group_unique_id, String member_phone, int isAdmin, String membership_status, String date_joined) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("group_unique_id", group_unique_id); // values : Group Name
        values.put("member_phone", member_phone); //
        values.put("isAdmin", isAdmin);// values : 0 or 1
        values.put("membership_status", membership_status);// values : left or joined
        values.put("date_joined", date_joined);// values : left or joined
        // Inserting Row
        db.insert("GROUPMEMBER", null, values);
        db.close(); // Closing database connection
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

    public void makeGroupAdmin(String group_unique_id, String member_phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues args = new ContentValues();
        args.put("isAdmin", 1);
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


    public String getGroupMessageStatus(String msg_unique_id) throws JSONException {
                JSONArray groups = new JSONArray();

            String selectQuery = "SELECT status FROM GROUPCHATSTATUS WHERE msg_unique_id='" + msg_unique_id +"'";

            SQLiteDatabase db = this.getReadableDatabase();
                Cursor cursor = db.rawQuery(selectQuery, null);
                // Move to first row
                cursor.moveToFirst();
                if(cursor.getCount() > 0){

                while (cursor.isAfterLast() != true) {

                JSONObject contact = new JSONObject();
    //                contact.put("status", cursor.getString(0));
    //                contact.put("group_name", cursor.getString(1));
    //                contact.put("is_mute", cursor.getString(2));
    //                contact.put("date_creation", cursor.getString(3));
    //                groups.put(contact);
                    return cursor.getString(0);
    //                cursor.moveToNext();
                }
            }
                cursor.close();
                db.close();
                // return user
            return "";
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
        String selectQuery = "SELECT unique_id, group_name, is_mute, date_creation FROM GROUPINFO WHERE unique_id ='"+ group_id +"'" ;

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
                +" AND phone = member_phone" ;
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

    public JSONArray getGroupMemberDetail(String group_id, String member_phone) throws JSONException {
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
                contacts.put(contact);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        // return user
        return contacts;
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
        String selectQuery = "SELECT  * FROM " + Contacts.TABLE_CONTACTS +" where on_cloudkibo='true' AND contacts.phone NOT IN (SELECT  member_phone FROM GROUPMEMBER, "+ Contacts.TABLE_CONTACTS + "  where group_unique_id='"+ group_id + "')";

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

    public void addGroupMessage(String group_unique_id, String message, String from, String from_fullname, String unique_id) {
        SQLiteDatabase db = this.getWritableDatabase();


        ContentValues values = new ContentValues();
//        values.put(User.KEY_UID, id); // FirstName    //values.put(User.KEY_FIRSTNAME, id); // FirstName
        values.put("group_unique_id", group_unique_id); // LastName
        values.put("_from", from); // Email
        values.put("type", ""); // UserName
        values.put("msg", message); // Email
        values.put("from_fullname", from_fullname); // Email
        values.put("unique_id", unique_id); // Created At

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
                contact.put("_from", cursor.getString(0));
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

        // Inserting Row
        try {
            db.insert(Contacts.TABLE_CONTACTS, null, values);
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
                         String uniqueid) {

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
        values.put(UserChat.USERCHAT_FROM_FULLNAME, from_fullname); // FROM FULL NAME
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

        String updateQuery = "UPDATE GROUPINFO" +
                " SET isArchived="+ 1 +" WHERE unique_id='"+unique_id+"'";

        try {
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

        try {
            db.execSQL(updateQuery);
        } catch (Exception e){
            e.printStackTrace();
        }

        db.close();
    }


    public void updateContact(String status, String phone, String id) {

        SQLiteDatabase db = this.getWritableDatabase();

        String updateQuery = "UPDATE " + Contacts.TABLE_CONTACTS +
                " SET "+ Contacts.CONTACT_STATUS +"='"+ status +"', " + Contacts.CONTACT_UID +"='"+ id +"' "+
                " WHERE "+ Contacts.CONTACT_PHONE +"='"+phone+"'";

        try {
            db.execSQL(updateQuery);
        } catch (Exception e){
            e.printStackTrace();
        }

        db.close();
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
        db.close();
        // return user
        return user;
    }





    /////////////////////////////////////////////////////////////////////
    // Getting contacts data from database                             //
    /////////////////////////////////////////////////////////////////////


    public JSONArray getContacts() throws JSONException {
    	JSONArray contacts = new JSONArray();
        String selectQuery = "SELECT  * FROM " + Contacts.TABLE_CONTACTS +" where on_cloudkibo='true'";

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

    public JSONArray getContactsOnAddressBook() throws JSONException {
        JSONArray contacts = new JSONArray();
        String selectQuery = "SELECT  * FROM " + Contacts.TABLE_CONTACTS +" where on_cloudkibo='false'";

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

    public JSONArray getSpecificContact(String phone) throws JSONException {
        JSONArray contacts = new JSONArray();
        String selectQuery = "SELECT  * FROM " + Contacts.TABLE_CONTACTS +" where phone='"+ phone +"'";

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
        String selectQuery = "SELECT  * FROM " + UserChat.TABLE_USERCHAT +" where uniqueid='"+ uniqueid +"'";

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

    public JSONArray getPendingChat() throws JSONException {
        JSONArray chats = new JSONArray();
        String selectQuery = "SELECT  * FROM " + UserChat.TABLE_USERCHAT +" WHERE status='pending'";

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
                contact.put("fromperson", cursor.getString(2));

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

}
