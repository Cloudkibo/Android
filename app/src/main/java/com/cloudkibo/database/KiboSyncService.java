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
import com.cloudkibo.file.filechooser.utils.FileUtils;
import com.cloudkibo.library.GroupUtility;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.library.Utility;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.parser.JSONArrayParser;
import com.koushikdutta.ion.Ion;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
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


    public void startSyncWithoutAddressBookAccess (String token) {

        authtoken = token;

        startWithAddressBook = false;

        mListener.contactsLoaded();

    }

    public void startIncrementalSyncWithoutAddressBookAccess (String token) {

        authtoken = token;

        startWithAddressBook = false;

        doUpwardSync();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        Log.i("tag", "This'll run 3000 milliseconds later");
                        //loadPartialChatFromServer();
                    }
                },
                3000);

    }

    public void startIncrementalSyncWithAddressBookAccess (String token) {

        authtoken = token;

        startWithAddressBook = true;

        doUpwardSync();

    }

    private void doUpwardSync(){
        DatabaseHandler db = new DatabaseHandler(
                getApplicationContext());
        try {

            JSONObject completePayload = new JSONObject();

            JSONArray chats = db.getPendingChat();

            completePayload.put("unsentMessages", chats);

            for (int i=0; i < chats.length(); i++) {
                JSONObject row = chats.getJSONObject(i);

                sendMessageUsingAPI(row.getString("to"),
                        row.getString("msg"), row.getString("uniqueid"), row.getString("type"), row.getString("file_type"));

            }

            JSONArray groupChats = db.getPendingGroupChat();

            completePayload.put("unsentGroupMessages", groupChats);

            /*GroupUtility utility = new GroupUtility(getApplicationContext());
            for (int i=0; i < groupChats.length(); i++) {
                JSONObject row = groupChats.getJSONObject(i);
                String group_id = row.getString("group_unique_id");
                String msg = row.getString("msg");
                String msg_unique_id = row.getString("unique_id");
                utility.syncGroupMessage(group_id,msg,msg_unique_id,authtoken);
            }*/

            JSONArray seenChats = db.getChatHistoryStatus();

            completePayload.put("unsentChatMessageStatus", seenChats);

            /*for (int i=0; i < seenChats.length(); i++) {
                JSONObject row = seenChats.getJSONObject(i);

                sendMessageStatusUsingAPI(
                        row.getString("status"),
                        row.getString("uniqueid"), row.getString("fromperson")
                );

            }*/

            JSONArray seenGroupChats = db.getGroupChatHistoryStatus();

            completePayload.put("unsentGroupChatMessageStatus", seenGroupChats);

            /*for (int i=0; i < seenGroupChats.length(); i++) {
                JSONObject row = seenGroupChats.getJSONObject(i);

                sendGroupMessageStatusUsingAPI(
                        row.getString("status"),
                        row.getString("uniqueid")
                );

            }*/

            JSONArray unSyncedCreatedGroups = db.getGroupsServerPending();

            JSONArray payloadCreatedGroups = new JSONArray();

            for(int i=0; i<unSyncedCreatedGroups.length(); i++) {
                JSONObject row = unSyncedCreatedGroups.getJSONObject(i);

                JSONObject payloadJ = new JSONObject();
                payloadJ.put("group_name", row.getString("group_name"));
                payloadJ.put("unique_id", row.getString("unique_id"));
                String array = row.getString("members").replace("\\", "");
                payloadJ.put("members", new JSONArray(array));

                payloadCreatedGroups.put(payloadJ);

            }

            completePayload.put("unsentGroups", payloadCreatedGroups);

            JSONArray groupMembers = db.getGroupMembersServerPending();

            completePayload.put("unsentAddedGroupMembers", groupMembers);

            /*for (int i=0; i < groupMembers.length(); i++) {
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
            }*/

            JSONArray groupMembersRemove = db.getGroupMembersRemovePending();

            completePayload.put("unsentRemovedGroupMembers", groupMembersRemove);

            /*for (int i=0; i < groupMembersRemove.length(); i++) {
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

            }*/



            /*checkStatusOfChatMessage(dataChat);*/

            Utility.sendLogToServer(getApplicationContext(), "NEW UPWARD SYNC PAYLOAD"+ completePayload.toString());

            syncToServer(completePayload);

        }catch(JSONException e ){
            e.printStackTrace();
        }
    }

    private void downWardSync() {
        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {

                JSONObject completePayload = new JSONObject();

                try {
                    DatabaseHandler db = new DatabaseHandler(
                            getApplicationContext());

                    JSONArray sentGroupMesssagesForStatus = db.getSentGroupMessagesForSync(db.getUserDetails().get("phone"));

                    JSONArray array = new JSONArray();
                    for (int i = 0; i < sentGroupMesssagesForStatus.length(); i++) {
                        JSONObject row = sentGroupMesssagesForStatus.getJSONObject(i);
                        array.put(row.getString("unique_id"));
                    }
                    JSONObject data = new JSONObject();
                    data.put("unique_ids", array);

                    completePayload.put("statusOfSentGroupMessages", data);

                    //checkStatusOfGroupMessage(data);*/

                    JSONArray sentChatMesssagesForStatus = db.getSentMessagesForSync(db.getUserDetails().get("phone"));

                    JSONArray arrayChat = new JSONArray();
                    for (int i = 0; i < sentChatMesssagesForStatus.length(); i++) {
                        JSONObject row = sentChatMesssagesForStatus.getJSONObject(i);
                        arrayChat.put(row.getString("uniqueid"));
                    }
                    JSONObject dataChat = new JSONObject();
                    dataChat.put("unique_ids", arrayChat);

                    completePayload.put("statusOfSentMessages", dataChat);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return new UserFunctions(getApplicationContext()).downwardSync(completePayload, authtoken);
            }

            @Override
            protected void onPostExecute(JSONObject payload) {
                try {

                     if (payload != null) {

                         JSONArray partialChat = payload.getJSONObject("partialChat").getJSONArray("msg");
                         JSONArray contactsUpdate = payload.getJSONArray("contactsUpdate");
                         JSONArray contactsWhoBlockedYou = payload.getJSONArray("contactsWhoBlockedYou");
                         JSONArray contactsBlockedByMe = payload.getJSONArray("contactsBlockedByMe");
                         JSONArray myGroups = payload.getJSONArray("myGroups");
                         JSONArray myGroupsMembers = payload.getJSONArray("myGroupsMembers");
                         JSONArray partialGroupChat = payload.getJSONArray("partialGroupChat");

                         loadPartialChat(partialChat);
                         loadCurrentContacts(contactsUpdate);
                         loadContactsWhoBlockedMe(contactsWhoBlockedYou);
                         loadContactsBlockedByMe(contactsBlockedByMe);
                         loadMyGroups(myGroups);
                         loadMyGroupsMembers(myGroupsMembers);
                         // todo partial group chat work here

                         JSONArray subpayload = payload.getJSONArray("statusOfSentMessages");
                         for (int i=0; i < subpayload.length(); i++) {
                             JSONObject row = subpayload.getJSONObject(i);
                             DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                             String uniqueid = row.getString("uniqueid");
                             String status = row.getString("status");
                             db.updateChat(status, uniqueid);
                         }

                         JSONArray subpayload2 = payload.getJSONArray("statusOfSentGroupMessages");
                         for (int i=0; i < subpayload2.length(); i++) {
                             JSONObject row = subpayload2.getJSONObject(i);
                             DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                             String msg_unique_id = row.getString("chat_unique_id");
                             String status = row.getString("status");
                             String user_phone = row.getString("user_phone");
                             String current_status = db.getGroupMessageStatus(msg_unique_id,user_phone).getJSONObject(0).getString("status");
                             if(!current_status.equals("seen")){
                                 if(status.equals("delivered")){
                                     db.updateGroupChatStatusDeliveredTime(msg_unique_id, status, user_phone, row.getString("delivered_date"));
                                 }
                                 if(status.equals("seen")){
                                     db.updateGroupChatStatusReadTime(msg_unique_id, status, user_phone, row.getString("read_date"));
                                 }
                             }
                         }

                     }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }.execute();
    }

    private void syncToServer(final JSONObject body) {
        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
                return new UserFunctions(getApplicationContext()).upwardSync(body, authtoken);
            }

            @Override
            protected void onPostExecute(JSONObject jsonA) {
                try {

                    if (jsonA != null) {
                        if(jsonA.getString("status").equals("success")) {
                            Utility.sendLogToServer(getApplicationContext(), "Sync Data reached server");

                            DatabaseHandler db = new DatabaseHandler(getApplicationContext());

                            JSONArray chats = db.getPendingChat();

                            for (int i=0; i < chats.length(); i++) {
                                JSONObject row = chats.getJSONObject(i);
                                db.updateChat("sent", row.getString("uniqueid"));
                                if(MainActivity.isVisible) {
                                    row.put("status", "sent");
                                    MainActivity.mainActivity.handleIncomingStatusForSentMessage("status", row);
                                }
                            }

                            JSONArray groupChats = db.getPendingGroupChat();
                            for (int i=0; i < groupChats.length(); i++) {
                                JSONObject row = groupChats.getJSONObject(i);
                                db.updateGroupChatStatus(row.getString("unique_id"), "sent");
                                if(MainActivity.isVisible) {
                                    MainActivity.mainActivity.updateGroupUIChat(row.getString("unique_id"));
                                }
                            }

                            JSONArray seenChats = db.getChatHistoryStatus();

                            for (int i=0; i < seenChats.length(); i++) {
                                JSONObject row = seenChats.getJSONObject(i);
                                db.resetSpecificChatHistorySync(row.getString("uniqueid"));
                                db = new DatabaseHandler(getApplicationContext());
                                db.updateChat(row.getString("status"), row.getString("uniqueid"));
                            }

                            JSONArray seenGroupChats = db.getGroupChatHistoryStatus();

                            for (int i=0; i < seenGroupChats.length(); i++) {
                                JSONObject row = seenGroupChats.getJSONObject(i);
                                db.resetSpecificGroupChatHistorySync(row.getString("uniqueid"));
                                db = new DatabaseHandler(getApplicationContext());
                                db.updateGroupChatStatus("sent", row.getString("uniqueid"));
                            }

                            JSONArray unSyncedCreatedGroups = db.getGroupsServerPending();

                            for(int i=0; i<unSyncedCreatedGroups.length(); i++) {
                                JSONObject row = unSyncedCreatedGroups.getJSONObject(i);
                                db.deleteGroupServerPending(row.getString("unique_id"));
                            }


                            //if(subtype.equals("unsentAddedGroupMembers")) {
                                // todo discuss with dayem
                                //JSONObject subpayload = payload.getJSONObject("payload");
                            //}
                            //if(subtype.equals("unsentRemovedGroupMembers")) {
                                // todo discuss with dayem
                                //JSONObject subpayload = payload.getJSONObject("payload");
                            //}

                        }
                    }

                    downWardSync();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }.execute();
    }

    private void updateServerAboutGroups(JsonObject body){
        UserFunctions userFunctions = new UserFunctions(getApplicationContext());
        Ion.with(getApplicationContext())
                .load(userFunctions.getBaseURL() + "/api/groupmessaging/")
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

    public void sendMessageUsingAPI(final String contactPhone, final String msg, final String uniqueid,
                                    final String type, final String file_type){

        try {
            if (type.equals("file")) {
                DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                JSONObject fileInfo = db.getFilesInfo(uniqueid);
                HashMap<String, String> user;
                user = db.getUserDetails();
                UserFunctions userFunctions = new UserFunctions(getApplicationContext());
                Ion.with(getApplicationContext())
                        .load(userFunctions.getBaseURL() + "/api/filetransfers/upload")
                        //.uploadProgressBar(uploadProgressBar)
                        .setHeader("kibo-token", authtoken)
                        .setMultipartParameter("filetype", file_type)
                        .setMultipartParameter("from", user.get("phone"))
                        .setMultipartParameter("to", contactPhone)
                        .setMultipartParameter("uniqueid", uniqueid)
                        .setMultipartParameter("filename", fileInfo.getString("file_name"))
                        .setMultipartParameter("filesize", fileInfo.getString("file_size"))
                        .setMultipartFile("file", FileUtils.getExtension(fileInfo.getString("path")), new File(fileInfo.getString("path")))
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                // do stuff with the result or error
                                if (e == null) {
                                    if (MainActivity.isVisible)
                                        MainActivity.mainActivity.ToastNotify2("Uploaded the file to server.");
                                    //sendMessageUsingAPIforChatOnly(contactPhone, msg, uniqueid, type, file_type);
                                } else {
                                    if (MainActivity.isVisible)
                                        MainActivity.mainActivity.ToastNotify2("Some error has occurred or Internet not available. Please try later.");
                                    e.printStackTrace();
                                }
                            }
                        });
            } else {
                //sendMessageUsingAPIforChatOnly(contactPhone, msg, uniqueid, type, file_type);
            }
        } catch ( JSONException e ) {
            e.printStackTrace();
        }

    }

    public void sendGroupMessageStatusUsingAPI(final String status, final String uniqueid){
        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
                UserFunctions userFunction = new UserFunctions(getApplicationContext());

                return userFunction.updateGroupChatStatusToSeen(uniqueid, authtoken);
            }

            @Override
            protected void onPostExecute(JSONObject row) {
                try {

                    if (row != null) {
                        if(row.has("status")) {
                            String demo = row.getString("uniqueid");
                            DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                            db.resetSpecificGroupChatHistorySync(uniqueid);
                            db = new DatabaseHandler(getApplicationContext());
                            db.updateGroupChatStatus(status, uniqueid);
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }.execute();
    }

    public void sendMessageUsingAPIforChatOnly(final String contactPhone, final String msg, final String uniqueid,
                                    final String type, final String file_type) {
        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
                UserFunctions userFunction = new UserFunctions(getApplicationContext());
                JSONObject message = new JSONObject();
                DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                HashMap<String, String> user;
                user = db.getUserDetails();

                try {
                    message.put("from", user.get("phone"));
                    message.put("to", contactPhone);
                    message.put("fromFullName", user.get("display_name"));
                    message.put("msg", msg);
                    message.put("date", Utility.getCurrentTimeInISO());
                    message.put("uniqueid", uniqueid);
                    message.put("type", type);
                    message.put("file_type", file_type);
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
                            HashMap<String, String> user;
                            user = db.getUserDetails();
                            mListener.chatLoaded();

                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }.execute();
    }



    public void checkStatusOfChatMessage(final JSONObject data){
        new AsyncTask<String, String, JSONArray>() {

            @Override
            protected JSONArray doInBackground(String... args) {
                return new UserFunctions(getApplicationContext()).checkStatusOfSentChatMessages(data, authtoken);
            }

            @Override
            protected void onPostExecute(JSONArray jsonA) {
                try {

                    if (jsonA != null) {
                        for (int i=0; i < jsonA.length(); i++) {
                            JSONObject row = jsonA.getJSONObject(i);
                            DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                            String uniqueid = row.getString("uniqueid");
                            String status = row.getString("status");
                            db.updateChat(status, uniqueid);
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
                UserFunctions userFunction = new UserFunctions(getApplicationContext());
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

    public void checkStatusOfGroupMessage(final JSONObject data){
        new AsyncTask<String, String, JSONArray>() {

            @Override
            protected JSONArray doInBackground(String... args) {
                return new UserFunctions(getApplicationContext()).checkStatusOfGroupMessages(data, authtoken);
            }

            @Override
            protected void onPostExecute(JSONArray jsonA) {
                try {

                    if (jsonA != null) {
                        for (int i=0; i < jsonA.length(); i++) {
                            JSONObject row = jsonA.getJSONObject(i);
                            DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                            String msg_unique_id = row.getString("chat_unique_id");
                            String status = row.getString("status");
                            String user_phone = row.getString("user_phone");
                            String current_status = db.getGroupMessageStatus(msg_unique_id,user_phone).getJSONObject(0).getString("status");
                            if(!current_status.equals("seen")){
                                if(status.equals("delivered")){
                                    db.updateGroupChatStatusDeliveredTime(msg_unique_id, status, user_phone, row.getString("delivered_date"));
                                }
                                if(status.equals("seen")){
                                    db.updateGroupChatStatusReadTime(msg_unique_id, status, user_phone, row.getString("read_date"));
                                }
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }.execute();
    }



    private void loadPartialChat(JSONArray jsonA){

        try {
            if (jsonA.length() > 0) {
                DatabaseHandler db = new DatabaseHandler(
                        getApplicationContext());

                HashMap<String, String> user = db.getUserDetails();

                db = new DatabaseHandler(
                        getApplicationContext());

                for (int i = 0; i < jsonA.length(); i++) {
                    JSONObject row = jsonA.getJSONObject(i);

                    JSONArray messageAlreadyThere = db.getSpecificChat(row.has("uniqueid") ? row.getString("uniqueid") : "");
                    if (messageAlreadyThere.length() < 1) {
                        db = new DatabaseHandler(
                                getApplicationContext());

                        if (db.getUserDetails().get("phone").equals(row.getString("from"))) {
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

                        if (row.has("status")) {
                            if (row.getString("to").equals(db.getUserDetails().get("phone")) && row.getString("status").equals("sent")) {
                                db = new DatabaseHandler(
                                        getApplicationContext());
                                db.updateChat("delivered", row.getString("uniqueid"));
                                // todo REALLY DO THIS
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

                        String[] links = Utility.extractLinks(row.getString("msg"));

                        if (links.length > 0) {
                            Utility.getURLInfo(getApplicationContext(), links[0], row.getString("uniqueid"), true);
                        }
                    }

                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void loadCurrentContacts(JSONArray jsonA){

        try {
            if (jsonA != null) {

                DatabaseHandler db = new DatabaseHandler(
                        getApplicationContext());

                for (int i = 0; i < jsonA.length(); i++) {
                    JSONObject row = jsonA.getJSONObject(i);

                    db.updateContact(row.getJSONObject("contactid").getString("status"),
                            row.getJSONObject("contactid").getString("phone"),
                            row.getJSONObject("contactid").getString("_id"));

                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void loadContactsWhoBlockedMe(JSONArray jsonA){

        try {

            if (jsonA != null) {

                DatabaseHandler db = new DatabaseHandler(
                        getApplicationContext());

                for (int i=0; i < jsonA.length(); i++) {
                    JSONObject row = jsonA.getJSONObject(i);

                    db.blockedMe(row.getJSONObject("contactid").getString("phone"));

                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void loadContactsBlockedByMe(JSONArray jsonA){

        try {

            if (jsonA != null) {

                DatabaseHandler db = new DatabaseHandler(
                        getApplicationContext());

                for (int i=0; i < jsonA.length(); i++) {
                    JSONObject row = jsonA.getJSONObject(i);

                    db.blockContact(row.getJSONObject("contactid").getString("phone"));

                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void loadMyGroups(JSONArray result) {
        Log.d("KIBOSyncSERVICE", result.toString());

        try {
            for (int i = 0; i < result.length(); i++) {
                JSONObject group = result.getJSONObject(i).getJSONObject("group_unique_id");

                DatabaseHandler db = new DatabaseHandler(getApplicationContext());

                try {
                    db.syncGroup(group.getString("unique_id"),
                            group.getString("group_name"), 0, group.getString("date_creation"));

                } catch (NullPointerException exc) {
                    exc.printStackTrace();
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadMyGroupsMembers(JSONArray result){

        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        db.resetGroupMembers();
        for(int i=0; i<result.length(); i++){

            try {
                JSONObject group = result.getJSONObject(i).getJSONObject("group_unique_id");
                String group_unique_id = group.getString("unique_id");
                String member_phone = result.getJSONObject(i).getString("member_phone");
                String display_name = result.getJSONObject(i).getString("display_name");
                int isAdmin = result.getJSONObject(i).getString("isAdmin").equals("Yes") ? 1 : 0;
                String membership_status = result.getJSONObject(i).getString("membership_status");
                String date_join = result.getJSONObject(i).getString("date_join");
                db.syncGroupMember(group_unique_id,member_phone,isAdmin,membership_status,date_join);
                if(isAdmin == 1){
                    if(db.getAllMessagesCountInGroupChat(group_unique_id) < 1) {
                        JSONArray specificContact = db.getSpecificContact(member_phone);
                        if(specificContact.length() > 0) {
                            display_name = specificContact.getJSONObject(0).getString("display_name");
                        }
                        String message = "";
                        if(db.getUserDetails().get("phone").equals(member_phone)){
                            message = "You created this group";
                        } else {
                            message = display_name + " added you to the group "+ db.getGroupInfo(group_unique_id).getString("group_name");
                        }
                        String uniqueid = Long.toHexString(Double.doubleToLongBits(Math.random()));
                        uniqueid += (new Date().getYear()) + "" + (new Date().getMonth()) + "" + (new Date().getDay());
                        uniqueid += (new Date().getHours()) + "" + (new Date().getMinutes()) + "" + (new Date().getSeconds());

//                                            db.addGroupMessage(group_unique_id,message,member_phone,display_name,uniqueid, "log");
                        db.addGroupChat(member_phone,display_name, message,
                                date_join, "log",
                                uniqueid,
                                group_unique_id);

                    }
                }
            }catch (NullPointerException exc){
                exc.printStackTrace();
            }catch (JSONException exc){
                exc.printStackTrace();
            }

        }

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
