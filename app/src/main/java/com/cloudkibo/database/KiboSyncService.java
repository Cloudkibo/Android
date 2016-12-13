package com.cloudkibo.database;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.SplashScreen;
import com.cloudkibo.library.GroupUtility;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.library.Utility;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;


public class KiboSyncService extends Service {


    private final IBinder kiboSyncBinder = new KiboSyncBinder();
    private BoundKiboSyncListener mListener;

    String authtoken;

    Boolean startWithAddressBook = true;

    final ArrayList<String> contactList1 = new ArrayList<String>();
    final ArrayList<String> contactList1Phone = new ArrayList<String>();
    final Map<String, String> photo_uri = new HashMap<>();

    public void startSync (String token) {

        authtoken = token;

        doUpwardSync();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        Log.i("tag", "This'll run 3000 milliseconds later");
                        loadChatFromServer();
                    }
                },
                3000);

    }

    public void startSyncWithoutAddressBookAccess (String token) {

        authtoken = token;

        startWithAddressBook = false;

        doUpwardSync();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        Log.i("tag", "This'll run 3000 milliseconds later");
                        loadChatFromServer();
                    }
                },
                3000);

    }

    public void startIncrementalSyncWithoutAddressBookAccess (String token) {

        authtoken = token;

        startWithAddressBook = false;

        doUpwardSync();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        Log.i("tag", "This'll run 3000 milliseconds later");
                        loadPartialChatFromServer();
                    }
                },
                3000);

    }

    public void startIncrementalSyncWithAddressBookAccess (String token) {

        authtoken = token;

        startWithAddressBook = true;

        doUpwardSync();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        Log.i("tag", "This'll run 3000 milliseconds later");
                        loadPartialChatFromServer();
                    }
                },
                3000);

    }

    private void doUpwardSync(){
        DatabaseHandler db = new DatabaseHandler(
                getApplicationContext());
        try {
            JSONArray chats = db.getPendingChat();

            for (int i=0; i < chats.length(); i++) {
                JSONObject row = chats.getJSONObject(i);

                sendMessageUsingAPI(row.getString("toperson"),
                        row.getString("msg"), row.getString("uniqueid"));

            }

            JSONArray groupChats = db.getPendingGroupChat();
            GroupUtility utility = new GroupUtility(getApplicationContext());
            for (int i=0; i < groupChats.length(); i++) {
                JSONObject row = groupChats.getJSONObject(i);
                String group_id = row.getString("group_unique_id");
                String msg = row.getString("msg");
                String msg_unique_id = row.getString("unique_id");
                utility.syncGroupMessage(group_id,msg,msg_unique_id,authtoken);
            }

            JSONArray seenChats = db.getChatHistoryStatus();

            for (int i=0; i < seenChats.length(); i++) {
                JSONObject row = seenChats.getJSONObject(i);

                sendMessageStatusUsingAPI(
                        row.getString("status"),
                        row.getString("uniqueid"), row.getString("fromperson")
                );

            }

            JSONArray unSyncedCreatedGroups = db.getGroupsServerPending();

            for(int i=0; i<unSyncedCreatedGroups.length(); i++) {
                JSONObject row = unSyncedCreatedGroups.getJSONObject(i);

                JsonObject payload = new JsonObject();
                payload.addProperty("group_name", row.getString("group_name"));
                payload.addProperty("unique_id", row.getString("unique_id"));
                String array = row.getString("members").replace("\\", "");
                payload.add("members", new JsonParser().parse(array).getAsJsonArray());

                updateServerAboutGroups(payload);


            }

            JSONArray groupMembers = db.getGroupMembersServerPending();

            for (int i=0; i < groupMembers.length(); i++) {
                JSONObject row = groupMembers.getJSONObject(i);

                String member_phone[] = new String[]{row.getString("member_phone")};
                try {
                    JSONObject info = db.getGroupInfo(row.getString("group_unique_id"));
                    String group_name = info.getString("group_name");
//                            Toast.makeText(getContext(), "Add member: "+ groupUtility.getMemberData(group_name, group_id, member_phone).toString(), Toast.LENGTH_LONG).show();
                    GroupUtility groupUtility = new GroupUtility(getApplicationContext());
                    groupUtility.addMemberOnServer(group_name,row.getString("group_unique_id"),member_phone,authtoken);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            JSONArray groupMembersRemove = db.getGroupMembersRemovePending();

            for (int i=0; i < groupMembersRemove.length(); i++) {
                JSONObject row = groupMembersRemove.getJSONObject(i);


                //String member_phone[] = new String[]{row.getString("member_phone")};
                try {
                    JSONObject info = db.getGroupInfo(row.getString("group_unique_id"));
                    String group_name = info.getString("group_name");
//                            Toast.makeText(getContext(), "Add member: "+ groupUtility.getMemberData(group_name, group_id, member_phone).toString(), Toast.LENGTH_LONG).show();
                    GroupUtility groupUtility = new GroupUtility(getApplicationContext());
                    groupUtility.removeMember(row.getString("group_unique_id"),row.getString("member_phone"),authtoken);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            JSONArray sentMesssagesForStatus = db.getSentGroupMessagesForSync(db.getUserDetails().get("phone"));

            JSONArray array = new JSONArray();
            for (int i=0; i < sentMesssagesForStatus.length(); i++) {
                JSONObject row = sentMesssagesForStatus.getJSONObject(i);
                array.put(row.getString("unique_id"));
            }
            JSONObject data = new JSONObject();
            data.put("unique_ids", array);

            JsonObject payload = new JsonObject();
            payload.getAsJsonObject(data.toString());
            Ion.with(getApplicationContext())
                    .load("https://api.cloudkibo.com/api/groupchatstatus/checkStatus")
                    .setHeader("kibo-token", authtoken)
                    .setJsonObjectBody(payload)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            // do stuff with the result or error
                            if(e==null)
                            Log.d("KIBOSyncSERVICE", result.toString());
                        }
                    });

        }catch(JSONException e ){
            e.printStackTrace();
        }
    }

    private void updateServerAboutGroups(JsonObject body){
        // todo
        Ion.with(getApplicationContext())
                .load("https://api.cloudkibo.com/api/groupmessaging/")
                .setHeader("kibo-token", authtoken)
                .setJsonObjectBody(body)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error
                        if(e==null) {
                            DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                            db.deleteGroupServerPending(result.getAsJsonPrimitive("unique_id").getAsString());
                        } else {

                        }
                    }
                });
    }

    public void sendMessageUsingAPI(final String contactPhone, final String msg, final String uniqueid){
        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
                UserFunctions userFunction = new UserFunctions();
                JSONObject message = new JSONObject();
                HashMap<String, String> user;
                DatabaseHandler db = new DatabaseHandler(getApplicationContext());

                user = db.getUserDetails();

                try {
                    message.put("from", user.get("phone"));
                    message.put("to", contactPhone);
                    message.put("fromFullName", user.get("display_name"));
                    message.put("msg", msg);
                    message.put("date", Utility.getCurrentTimeInISO());
                    message.put("uniqueid", uniqueid);
                } catch (JSONException e){
                    e.printStackTrace();
                }

                return userFunction.sendChatMessageToServer(message, authtoken);
            }

            @Override
            protected void onPostExecute(JSONObject row) {
                try {

                    if (row != null) {
                        if(row.has("status")){
                            DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                            db.updateChat(row.getString("status"), row.getString("uniqueid"));
                            mListener.chatLoaded();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }.execute();
    }

    public void sendMessageStatusUsingAPI(final String status, final String uniqueid, final String sender){
        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
                UserFunctions userFunction = new UserFunctions();
                JSONObject message = new JSONObject();

                try {
                    message.put("sender", sender);
                    message.put("status", status);
                    message.put("uniqueid", uniqueid);
                } catch (JSONException e){
                    e.printStackTrace();
                }

                return userFunction.sendChatMessageStatusToServer(message, authtoken);
            }

            @Override
            protected void onPostExecute(JSONObject row) {
                try {

                    if (row != null) {
                        if(row.has("status")){
                            DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                            db.resetSpecificChatHistorySync(row.getString("uniqueid"));
                            db = new DatabaseHandler(getApplicationContext());
                            db.updateChat(status, row.getString("uniqueid"));
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }.execute();
    }

    private void loadContactsFromAddressBook(){

        new AsyncTask<String, String, JSONObject>() {
            private ProgressDialog nDialog;


            @Override
            protected JSONObject doInBackground(String... args) {

                List<NameValuePair> phones = new ArrayList<NameValuePair>();
                List<NameValuePair> emails = new ArrayList<NameValuePair>();

                ContentResolver cr = getApplicationContext().getContentResolver();
                Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                        null, null, null, null);
                if (cur.getCount() > 0) {
                    while (cur.moveToNext()) {
                        String id = cur.getString(
                                cur.getColumnIndex(ContactsContract.Contacts._ID));
                        String name = cur.getString(
                                cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        String image_uri = cur.getString(
                                cur.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));
                        //Log.w("Contact Name : ", "Name " + name + "");
                        if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                            Cursor pCur = cr.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                                    new String[]{id}, null);
                            while (pCur.moveToNext()) {
                                DatabaseHandler db = new DatabaseHandler(
                                        getApplicationContext());
                                String phone = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                if(phone.length() < 6) continue;
                                if(phone.charAt(0) != '+') {
                                    if(phone.charAt(0) == '0') phone = phone.substring(1, phone.length());
                                    if(phone.charAt(0) == '1') phone = "+" + phone;
                                    else phone = "+" + db.getUserDetails().get("country_prefix") + phone;
                                }
                                if(contactList1Phone.contains(phone)) continue;
                                //if(Character.isLetter(name.charAt(0)))
                                //    name = name.substring(0, 1).toUpperCase() + name.substring(1);
                                phone = phone.replaceAll("\\s+","");
                                phone = phone.replaceAll("\\p{P}","");
                                db = new DatabaseHandler(getApplicationContext());
                                String userPhone = db.getUserDetails().get("phone");
                                if(userPhone.equals(phone)) continue;
                                phones.add(new BasicNameValuePair("phonenumbers", phone));
                                Log.w("Phone Number: ", "Name : " + name + " Number : " + phone);
                                contactList1.add(name);
                                contactList1Phone.add(phone);
                                photo_uri.put(phone,image_uri);
                            }
                            pCur.close();
                        }
                        /*Cursor emailCur = cr.query(
                                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                                new String[]{id}, null);
                        while (emailCur.moveToNext()) {
                            // This would allow you get several email addresses
                            // if the email addresses were stored in an array
                            String email = emailCur.getString(
                                    emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                            String emailType = emailCur.getString(
                                    emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
                            emails.add(new BasicNameValuePair("emails", email));
                            Log.w("Email: ", "Name : " + name + " Email : " + email);
                            //contactList1.add(name);
                            //contactList1Phone.add(email);
                            break;
                        }
                        emailCur.close();*/
                    }
                }
                cur.close();

                UserFunctions userFunction = new UserFunctions();
                JSONObject json = userFunction.sendAddressBookPhoneContactsToServer(phones, authtoken);
                Log.w("SERVER SENT RESPONSE", json.toString());
                return json;
            }

            @Override
            protected void onPostExecute(JSONObject json) {

                try {

                    ArrayList<String> contactList1Available = new ArrayList<String>();
                    ArrayList<String> contactList1PhoneAvailable = new ArrayList<String>();

                    if(json != null){

                        JSONArray jArray = json.getJSONArray("available");

                        for(int i = 0; i<jArray.length(); i++){
                            contactList1Available.add(contactList1.get(contactList1Phone.indexOf(jArray.get(i).toString())));
                            contactList1PhoneAvailable.add(contactList1Phone.get(contactList1Phone.indexOf(jArray.get(i).toString())));
                            contactList1.remove(contactList1Phone.indexOf(jArray.get(i).toString()));
                            contactList1Phone.remove(contactList1Phone.indexOf(jArray.get(i).toString()));
                            Log.w("REMOVING", jArray.get(i).toString());
                        }

                    }
                    loadNotFoundContacts(contactList1, contactList1Phone);
                    loadFoundContacts(contactList1Available, contactList1PhoneAvailable);
                    Utility utility = new Utility();
                    utility.updateDatabaseWithContactImages(getApplicationContext(), contactList1Phone);
                    utility.updateDatabaseWithContactImages(getApplicationContext(), contactList1PhoneAvailable);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

        }.execute();

    }

    private void loadNotFoundContacts(ArrayList<String> contactList1, ArrayList<String> contactList1Phone) {

        DatabaseHandler db = new DatabaseHandler(
                getApplicationContext());

        db.resetContactsTable();

        db = new DatabaseHandler(
                getApplicationContext());

        for (int i = 0; i < contactList1.size(); i++) {
            db.addContact("false", "null",
                    contactList1Phone.get(i),
                    contactList1.get(i),
                    "null",
                    "No",
                    "N/A",
                    photo_uri.get(contactList1Phone.get(i)));
        }

        /*try {
            JSONArray contacts = db.getContacts();
            contacts.toString();
        }catch(JSONException e){
            e.printStackTrace();
        }*/
    }

    private void loadFoundContacts(ArrayList<String> contactList1, ArrayList<String> contactList1Phone) {

        DatabaseHandler db = new DatabaseHandler(
                getApplicationContext());

        for (int i = 0; i < contactList1.size(); i++) {
            db.addContact("true", "null",
                    contactList1Phone.get(i),
                    contactList1.get(i),
                    "null",
                    "Yes",
                    "N/A",
                    photo_uri.get(contactList1Phone.get(i)));
        }

        loadCurrentContactsFromServer();

        try {
            JSONArray contacts = db.getContacts();
            contacts.toString();
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    private void loadCurrentContactsFromServer(){

        new AsyncTask<String, String, JSONArray>() {

            @Override
            protected JSONArray doInBackground(String... args) {
                UserFunctions userFunction = new UserFunctions();
                JSONArray json = userFunction.getContactsList(authtoken);
                return json;
            }

            @Override
            protected void onPostExecute(JSONArray jsonA) {
                try {

                    if (jsonA != null) {

                        DatabaseHandler db = new DatabaseHandler(
                                getApplicationContext());

                        for (int i=0; i < jsonA.length(); i++) {
                            JSONObject row = jsonA.getJSONObject(i);

                            /*db.addContact("true", "null",
                                    row.getJSONObject("contactid").getString("phone"),
                                    row.getJSONObject("contactid").getString("display_name"),
                                    row.getJSONObject("contactid").getString("_id"),
                                    row.getString("detailsshared"),
                                    row.getJSONObject("contactid").getString("status"));*/

                            db.updateContact(row.getJSONObject("contactid").getString("status"),
                                    row.getJSONObject("contactid").getString("phone"),
                                    row.getJSONObject("contactid").getString("_id"));

                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                loadMyGroupsFromServer();
            }

        }.execute();

    }

    private void loadMyGroupsFromServer() {
        Ion.with(getApplicationContext())
                .load("https://api.cloudkibo.com/api/groupmessaginguser/mygroups")
                .setHeader("kibo-token", authtoken)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        // do stuff with the result or error
                        if(e==null) {
                            Log.d("KIBOSyncSERVICE", result.toString());

                            for(int i=0; i<result.size(); i++){
                                JsonObject group = result.get(i).getAsJsonObject().getAsJsonObject("group_unique_id");

                                DatabaseHandler db = new DatabaseHandler(getApplicationContext());

                                try {
                                    db.syncGroup(group.get("unique_id").getAsString(),
                                            group.get("group_name").getAsString(), 0, group.get("date_creation").getAsString());
                                }catch (NullPointerException exc){
                                    exc.printStackTrace();
                                }

                            }

                        }

                        loadMyGroupsMembersFromServer();
                    }
                });
    }

    private void loadMyGroupsMembersFromServer(){
        Ion.with(getApplicationContext())
                .load("https://api.cloudkibo.com/api/groupmessaginguser/mygroupsmembers")
                .setHeader("kibo-token", authtoken)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        // do stuff with the result or error
                        if(e==null) {
                            Log.d("KIBOSyncSERVICE", result.toString());

                            DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                            db.resetGroupMembers();
                            for(int i=0; i<result.size(); i++){
                                JsonObject group = result.get(i).getAsJsonObject().getAsJsonObject("group_unique_id");



                                try {
                                    String group_unique_id = group.get("unique_id").getAsString();
                                    String member_phone = result.get(i).getAsJsonObject().get("member_phone").getAsString();
                                    String display_name = result.get(i).getAsJsonObject().get("display_name").getAsString();
                                    int isAdmin = result.get(i).getAsJsonObject().get("isAdmin").getAsString().equals("Yes") ? 1 : 0;
                                    String membership_status = result.get(i).getAsJsonObject().get("membership_status").getAsString();
                                    String date_join = result.get(i).getAsJsonObject().get("date_join").getAsString();
                                    db.syncGroupMember(group_unique_id,member_phone,isAdmin,membership_status,date_join);
                                }catch (NullPointerException exc){
                                    exc.printStackTrace();
                                }

                            }

                        }

                        mListener.contactsLoaded();
                    }
                });
    }


    private void loadChatFromServer() {

        new AsyncTask<String, String, JSONArray>() {

            @Override
            protected JSONArray doInBackground(String... args) {
                DatabaseHandler db = new DatabaseHandler(
                        getApplicationContext());
                UserFunctions userFunction = new UserFunctions();
                JSONArray json = new JSONArray();
                try {
                    json = userFunction.getAllChatList(db.getUserDetails().get("phone"), authtoken).getJSONArray("msg");
                } catch(JSONException e){
                    e.printStackTrace();
                }
                return json;
            }

            @Override
            protected void onPostExecute(JSONArray jsonA) {
                try {

                    if (jsonA != null) {

                        if(jsonA.length() > 0) {
                            DatabaseHandler db = new DatabaseHandler(
                                    getApplicationContext());

                            db.resetChatsTable();

                            db = new DatabaseHandler(
                                    getApplicationContext());

                            HashMap<String, String> user = db.getUserDetails();

                            db = new DatabaseHandler(
                                    getApplicationContext());

                            for (int i=0; i < jsonA.length(); i++) {
                                JSONObject row = jsonA.getJSONObject(i);

                                if(db.getUserDetails().get("phone").equals(row.getString("from"))) {
                                    db.addChat(row.getString("to"), row.getString("from"), row.getString("fromFullName"),
                                            row.getString("msg"), row.getString("date"),
                                            row.has("status") ? row.getString("status") : "",
                                            row.has("uniqueid") ? row.getString("uniqueid") : "",
                                            row.has("type") ? row.getString("type") : "",
                                            row.has("file_type") ? row.getString("file_type") : "");
                                } else {
                                    db.addChat(row.getString("to"), row.getString("from"), row.getString("fromFullName"),
                                            row.getString("msg"), row.getString("date_server_received"),
                                            row.has("status") ? row.getString("status") : "",
                                            row.has("uniqueid") ? row.getString("uniqueid") : "",
                                            row.has("type") ? row.getString("type") : "",
                                            row.has("file_type") ? row.getString("file_type") : "");
                                }

                                if(row.has("status")){
                                    if(row.getString("to").equals(db.getUserDetails().get("phone")) && row.getString("status").equals("sent")){
                                        db = new DatabaseHandler(
                                                getApplicationContext());
                                        db.updateChat("delivered", row.getString("uniqueid"));
                                        mListener.sendMessageStatusUsingSocket(row.getString("from"),
                                                "delivered", row.getString("uniqueid"));

                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                                        String message = row.getString("msg");
                                        String subMsg = (message.length() > 15) ? message.substring(0, 15) : message;

                                        DatabaseHandler db2 = new DatabaseHandler(getApplicationContext());

                                        String senderName = db2.getSpecificContact(row.getString("from")).getJSONObject(0).getString("display_name");

                                        Notification n = new Notification.Builder(getApplicationContext())
                                                .setContentTitle(senderName)
                                                .setContentText(subMsg)
                                                .setSmallIcon(R.drawable.icon)
                                                .setContentIntent(pIntent)
                                                .setAutoCancel(true)
                                                .build();

                                        NotificationManager notificationManager =
                                                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                                        notificationManager.notify(0, n);

                                        try {
                                            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                                            r.play();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }

                            }

                        /*try {
                            JSONArray chat = db.getChat();
                            chat.toString();
                        }catch(JSONException e){
                            e.printStackTrace();
                        }*/

                            mListener.chatLoaded();
                        } else {
                            mListener.chatLoaded();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                GroupUtility utility = new GroupUtility(getApplicationContext());
                utility.syncGroupIcon(authtoken);

                if(startWithAddressBook)
                    loadContactsFromAddressBook();
                else
                    loadCurrentContactsFromServer();

            }

        }.execute();

    }

    // todo need to remove this... i am already doing the sync of my groups and members inside the group above.
    private void syncAllGroups(){
        GroupUtility groupUtility  = new GroupUtility(getApplicationContext());
        groupUtility.syncAllGroups(authtoken);
    }

    private void loadPartialChatFromServer() {

        new AsyncTask<String, String, JSONArray>() {

            @Override
            protected JSONArray doInBackground(String... args) {
                DatabaseHandler db = new DatabaseHandler(
                        getApplicationContext());
                UserFunctions userFunction = new UserFunctions();
                JSONArray json = new JSONArray();
                try {
                    json = userFunction.getPartialChatList(db.getUserDetails().get("phone"), authtoken).getJSONArray("msg");
                } catch(JSONException e){
                    e.printStackTrace();
                }
                return json;
            }

            @Override
            protected void onPostExecute(JSONArray jsonA) {
                try {

                    if (jsonA != null) {

                        if(jsonA.length() > 0) {
                            DatabaseHandler db = new DatabaseHandler(
                                    getApplicationContext());

                            HashMap<String, String> user = db.getUserDetails();

                            db = new DatabaseHandler(
                                    getApplicationContext());

                            for (int i=0; i < jsonA.length(); i++) {
                                JSONObject row = jsonA.getJSONObject(i);

                                JSONArray messageAlreadyThere = db.getSpecificChat(row.has("uniqueid") ? row.getString("uniqueid") : "");
                                if(messageAlreadyThere.length() < 1){
                                    db = new DatabaseHandler(
                                            getApplicationContext());

                                    if(db.getUserDetails().get("phone").equals(row.getString("from"))) {
                                        db.addChat(row.getString("to"), row.getString("from"), row.getString("fromFullName"),
                                                row.getString("msg"), row.getString("date"),
                                                row.has("status") ? row.getString("status") : "",
                                                row.has("uniqueid") ? row.getString("uniqueid") : "",
                                                row.has("type") ? row.getString("type") : "",
                                                row.has("file_type") ? row.getString("file_type") : "");
                                    } else {
                                        db.addChat(row.getString("to"), row.getString("from"), row.getString("fromFullName"),
                                                row.getString("msg"), row.getString("date_server_received"),
                                                row.has("status") ? row.getString("status") : "",
                                                row.has("uniqueid") ? row.getString("uniqueid") : "",
                                                row.has("type") ? row.getString("type") : "",
                                                row.has("file_type") ? row.getString("file_type") : "");
                                    }

                                    if(row.has("status")){
                                        if(row.getString("to").equals(db.getUserDetails().get("phone")) && row.getString("status").equals("sent")){
                                            db = new DatabaseHandler(
                                                    getApplicationContext());
                                            db.updateChat("delivered", row.getString("uniqueid"));
                                            mListener.sendMessageStatusUsingSocket(row.getString("from"),
                                                    "delivered", row.getString("uniqueid"));

                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                                            String message = row.getString("msg");
                                            String subMsg = (message.length() > 15) ? message.substring(0, 15) : message;

                                            DatabaseHandler db2 = new DatabaseHandler(getApplicationContext());

                                            String senderName = db2.getSpecificContact(row.getString("from")).getJSONObject(0).getString("display_name");

                                            Notification n = new Notification.Builder(getApplicationContext())
                                                    .setContentTitle(senderName)
                                                    .setContentText(subMsg)
                                                    .setSmallIcon(R.drawable.icon)
                                                    .setContentIntent(pIntent)
                                                    .setAutoCancel(true)
                                                    .build();

                                            NotificationManager notificationManager =
                                                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                                            notificationManager.notify(0, n);

                                            try {
                                                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                                                r.play();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                        }
                                    }
                                }

                            }

                        /*try {
                            JSONArray chat = db.getChat();
                            chat.toString();
                        }catch(JSONException e){
                            e.printStackTrace();
                        }*/


                            mListener.chatLoaded();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(startWithAddressBook)
                    loadContactsFromAddressBook();
                else
                    loadCurrentContactsFromServer();

                loadPartialGroupChatFromServer();

            }

        }.execute();

    }

    private void loadPartialGroupChatFromServer(){
        Ion.with(getApplicationContext())
                .load("https://api.cloudkibo.com/api/groupchatstatus/")
                .setHeader("kibo-token", authtoken)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        // todo messages are already given to us by push.. the sync don't get any undelivered message.. needs to test more

                        if(result != null){
                        Log.d("KiboSyncService", result.toString());
                        }


                    }
                });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return kiboSyncBinder;
    }




    public class KiboSyncBinder extends Binder {

        public KiboSyncService getService() {
            return KiboSyncService.this;
        }

        public void setListener(BoundKiboSyncListener listener) {
            mListener = listener;
        }

    }
}
