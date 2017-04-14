package com.cloudkibo.library;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.SplashScreen;
import com.cloudkibo.database.DatabaseHandler;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by root on 11/14/16.
 */
public class GroupUtility {
    UserFunctions user;
    public DatabaseHandler db;
    private NotificationManager mNotificationManager;
    public static final int NOTIFICATION_ID = 1;
    Context ctx;

    public GroupUtility(Context ctx){
        this.ctx = ctx;
        user = new UserFunctions(ctx);
        db = new DatabaseHandler(ctx);
    }

    public void updateGroupToLocalDatabase(final String group_id, final String group_name, final String auth_token, final String sender_id, final String msg, final Context context){

        db.createGroup(group_id, group_name, 0);

        new AsyncTask<String, String, JSONArray>() {

            @Override
            protected JSONArray doInBackground(String... args) {
                return user.getGroupMembers(group_id, auth_token);
            }

            @Override
            protected void onPostExecute(JSONArray row) {
                if(row != null){
//                        sendNotification("Local Database Updated", row.toString());
                    try {
                        JSONObject t = row.getJSONObject(0);
                        JSONObject groupinfo  = t.getJSONObject("group_unique_id");

                        //Sync Group
                        db.syncGroup(groupinfo.getString("unique_id"),groupinfo.getString("group_name"),0,groupinfo.getString("date_creation"));

                        //Sync Members
                        for (int i = 0; i < row.length(); i++) {
                            JSONObject memberInfo = row.getJSONObject(i);
                            String group_unique_id = memberInfo.getJSONObject("group_unique_id").getString("unique_id");
                            String member_phone = memberInfo.getString("member_phone");
                            String display_name = memberInfo.getString("display_name");
                            int isAdmin = memberInfo.getString("isAdmin").equals("Yes") ? 1 : 0;
                            String membership_status = memberInfo.getString("membership_status");
                            String date_join = memberInfo.getString("date_join");
                            db.syncGroupMember(group_unique_id,member_phone,isAdmin,membership_status,date_join);
                            if(MainActivity.isVisible){
                                MainActivity.mainActivity.updateChatList();
                            }
                        }

                        String member_phone = sender_id;

                        String personWhoAdded = "";
                        JSONArray specificContact = db.getSpecificContact(member_phone);
                        if(specificContact.length() > 0) {
                            personWhoAdded = specificContact.getJSONObject(0).getString("display_name");
                        } else {
                            personWhoAdded = member_phone;
                        }

                        String message = personWhoAdded +" added you to the group";
                        String uniqueid = Long.toHexString(Double.doubleToLongBits(Math.random()));
                        uniqueid += (new Date().getYear()) + "" + (new Date().getMonth()) + "" + (new Date().getDay());
                        uniqueid += (new Date().getHours()) + "" + (new Date().getMinutes()) + "" + (new Date().getSeconds());

                        db.addGroupMessage(group_id,message,member_phone,personWhoAdded,uniqueid, "log");


                            final String unique_id = randomString();
                            UserFunctions userFunctions = new UserFunctions(ctx.getApplicationContext());
                            Ion.with(context)
                                    .load(userFunctions.getBaseURL() + "/api/groupmessaging/downloadIcon")
                                    .setHeader("kibo-token", auth_token)
                                    .setBodyParameter("unique_id", group_id)
                                    .write(new File(context.getFilesDir().getPath() + "" + group_id))
                                    .setCallback(new FutureCallback<File>() {
                                        @Override
                                        public void onCompleted(Exception e, File file) {
                                            // download done...
                                            // do stuff with the File or error

                                            try {
                                                FileOutputStream outputStream;
                                                outputStream = context.openFileOutput(group_id, Context.MODE_PRIVATE);
                                                outputStream.write(com.cloudkibo.webrtc.filesharing.Utility.convertFileToByteArray(file));
                                                outputStream.close();
                                            }catch (IOException e2){
                                                e2.printStackTrace();
                                            }
                                            file.delete();

                                            Log.d("GROUPFILE", "Downloaded icon");
                                            if(MainActivity.isVisible){
                                                MainActivity.mainActivity.updateGroupUIChat(unique_id);
                                            }
                                        }
                                    });



                        if (MainActivity.isVisible) {
                            MainActivity.mainActivity.refreshGroupsOnDesktop();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

        }.execute();

    }

    public void updateGroupMembers(final String payload, final String auth_token){
        try {
            JSONObject data = new JSONObject(payload);
            JSONArray persons = data.getJSONArray("personsAdded");
            String admin_phone = data.getString("senderId");
            String group_id = data.getString("groupId");
            // todo remove this developer message
            if(!db.isMute(group_id))
                sendNotification("New Member Added", payload.toString());

            String personWhoAdded = "";
            JSONArray specificContact = db.getSpecificContact(admin_phone);
            if(specificContact.length() > 0) {
                personWhoAdded = specificContact.getJSONObject(0).getString("display_name");
            } else {
                personWhoAdded = admin_phone;
            }

            for (int i = 0; i < persons.length() ; i++) {
                db.addGroupMember(group_id,persons.getString(i).toString(),"0","joined");

                String personBeingAdded = "";
                JSONArray specificAddedContact = db.getSpecificContact(persons.getString(i).toString());
                if(specificAddedContact.length() > 0) {
                    personBeingAdded = specificAddedContact.getJSONObject(0).getString("display_name");
                } else {
                    personBeingAdded = persons.getString(i).toString();
                }

                String message = ""+ personWhoAdded +" added "+ personBeingAdded;
                String uniqueid = Long.toHexString(Double.doubleToLongBits(Math.random()));
                uniqueid += (new Date().getYear()) + "" + (new Date().getMonth()) + "" + (new Date().getDay());
                uniqueid += (new Date().getHours()) + "" + (new Date().getMinutes()) + "" + (new Date().getSeconds());

                DatabaseHandler db = new DatabaseHandler(ctx.getApplicationContext());
                db.addGroupChat(admin_phone, personWhoAdded, message,
                        Utility.getCurrentTimeInISO(), "log",
                        uniqueid,
                        group_id);
            }
            if(MainActivity.isVisible){
                MainActivity.mainActivity.updateGroupMembers();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void removedFromGroup(final String payload, final String auth_token){
        try {
            JSONObject data = new JSONObject(payload);
            String person_phone = data.getString("personRemoved");
            String admin_phone = data.getString("senderId");
            String group_id = data.getString("groupId");
            String membership_type = data.getString("membership_status");
            // todo remove this developer message
            if(!db.isMute(group_id))
                sendNotification("A Member was removed from a group.", payload.toString());

            db.leaveGroup(group_id, person_phone);
            if(MainActivity.isVisible){
                MainActivity.mainActivity.updateGroupMembers();
            }

            String personWhoRemoved = "";
            JSONArray specificContact = db.getSpecificContact(admin_phone);
            if(specificContact.length() > 0) {
                personWhoRemoved = specificContact.getJSONObject(0).getString("display_name");
            } else {
                personWhoRemoved = admin_phone;
            }

            String personBeingRemoved = "";
            JSONArray specificAddedContact = db.getSpecificContact(person_phone);
            if(specificAddedContact.length() > 0) {
                personBeingRemoved = specificAddedContact.getJSONObject(0).getString("display_name");
            } else {
                personBeingRemoved = person_phone;
            }
            if(person_phone.equals(db.getUserDetails().get("phone"))) personBeingRemoved = "you";

            String message = ""+ personWhoRemoved +" removed "+ personBeingRemoved;
            String uniqueid = Long.toHexString(Double.doubleToLongBits(Math.random()));
            uniqueid += (new Date().getYear()) + "" + (new Date().getMonth()) + "" + (new Date().getDay());
            uniqueid += (new Date().getHours()) + "" + (new Date().getMinutes()) + "" + (new Date().getSeconds());

            DatabaseHandler db = new DatabaseHandler(ctx.getApplicationContext());
            db.addGroupChat(admin_phone, personWhoRemoved, message,
                    Utility.getCurrentTimeInISO(), "log",
                    uniqueid,
                    group_id);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void downloadGroupIcon(final String payload, final Context context, final String auth_token){
        try {
            final JSONObject data = new JSONObject(payload);
            final String unique_id = randomString();
            db.addGroupMessage(data.getString("groupId"), "Group Icon was updated by " + db.getContactName(data.getString("senderId")) , db.getUserDetails().get("phone"),"", unique_id, "log");
            UserFunctions userFunctions = new UserFunctions(ctx.getApplicationContext());
            Ion.with(context)
                    .load(userFunctions.getBaseURL() + "/api/groupmessaging/downloadIcon")
                    .setHeader("kibo-token", auth_token)
                    .setBodyParameter("unique_id", data.getString("groupId"))
                    .write(new File(context.getFilesDir().getPath() + "" + data.getString("groupId")))
                    .setCallback(new FutureCallback<File>() {
                        @Override
                        public void onCompleted(Exception e, File file) {
                            // download done...
                            // do stuff with the File or error

                            try {
                                FileOutputStream outputStream;
                                outputStream = context.openFileOutput(data.getString("groupId"), Context.MODE_PRIVATE);
                                outputStream.write(com.cloudkibo.webrtc.filesharing.Utility.convertFileToByteArray(file));
                                outputStream.close();
                            }catch (IOException e2){
                                e2.printStackTrace();
                            }catch (JSONException e3){
                                e3.printStackTrace();
                            }

                            file.delete();

                            Log.d("GROUPFILE", "Downloaded icon");
                            if(MainActivity.isVisible){
                                MainActivity.mainActivity.updateGroupUIChat(unique_id);
                            }
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    // @// TODO: 12/1/16 Sojharo I need to discuss the url of the downloadGroupIcon function
    // todo @dayem let me know when to discuss
    public void syncGroupIcon(final String auth_token){
//            JSONObject data = new JSONObject(payload);
        try {
            JSONArray groups =   db.getMyGroups(db.getUserDetails().get("phone"));

            UserFunctions userFunctions = new UserFunctions(ctx.getApplicationContext());
            for (int i = 0; i < groups.length(); i++) {
                String group_id = groups.getJSONObject(i).getString("unique_id");
                Ion.with(ctx)
                        .load(userFunctions.getBaseURL() + "/api/groupmessaging/downloadIcon")
                        .setHeader("kibo-token", auth_token)
                        .setBodyParameter("unique_id", group_id)
                        .write(new File(ctx.getFilesDir().getPath() + "" + group_id))
                        .setCallback(new FutureCallback<File>() {
                            @Override
                            public void onCompleted(Exception e, File file) {
                                // download done...
                                // do stuff with the File or error

                                Log.d("GROUPFILE", "Downloaded icon");
                            }
                        });
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void updateGroupChat(final String payload, final String auth_token){
        try {
            JSONObject data = new JSONObject(payload);
            String message = data.getString("msg");
            String member_phone = data.getString("from");
            final String unique_id = data.getString("unique_id");
            String group_id = data.getString("group_unique_id");
            String msg_type = data.getString("type");
            if(!isGroupMember(group_id)){
                return;
            }
            if(!db.isMute(group_id))
                sendNotification(message, message);

            db.addGroupMessage(group_id,message,member_phone,member_phone,unique_id, msg_type);
            db.addGroupChatStatus(unique_id, "delivered", db.getUserDetails().get("phone"));
            if(MainActivity.isVisible){
                MainActivity.mainActivity.updateGroupUIChat(unique_id);
            } else {
                final String temp_auth_token = auth_token;
                new AsyncTask<String, String, JSONObject>() {
                    @Override
                    protected JSONObject doInBackground(String... args) {
                        UserFunctions userFunctions = new UserFunctions(ctx.getApplicationContext());
                        return userFunctions.updateGroupChatStatusToDelivered(unique_id, temp_auth_token);
                    }
                    @Override
                    protected void onPostExecute(JSONObject row) {
                        if(row != null){
                            // todo remove this developer message
                            //sendNotification("Updated message status on server to seen", row.toString());
                            DatabaseHandler db= new DatabaseHandler(ctx);
                            db.updateGroupChatStatus(unique_id, "delivered");
                        }
                    }
                }.execute();
            }
        } catch (JSONException e) {

            e.printStackTrace();
        }

    }


    public String syncGroupMessage(final String group_id, final String message, final String msg_unique_id, final  String auth_token){
        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
                return user.sendGroupChat(group_id,db.getUserDetails().get("phone"),"",message,db.getUserDetails().get("display_name"),msg_unique_id, auth_token);
            }

            @Override
            protected void onPostExecute(JSONObject row) {
                if(!row.optString("group_unique_id").equals("")){
                    sendNotification("Message Sent To Server", "Your message was sync to server");
                    db.updateGroupChatStatus(msg_unique_id,"sent");
                    if(MainActivity.isVisible) {
                        MainActivity.mainActivity.updateGroupUIChat(msg_unique_id);
                    }
                }else if(row.optString("Error").equals("No Internet")){
                    sendNotification("No Internet Connection", "Message will be sent as soon as the device gets connected to internet");
                }else{
                    sendNotification("Failed to Send Message", "Oops message was not synced due to some reason");
                }
            }

        }.execute();

        return msg_unique_id;
    }

    public String sendGroupMessage(final String group_id, final  String message, final  String auth_token, final String type){
        final String unique_id = randomString();
        db.addGroupMessage(group_id,message, db.getUserDetails().get("phone"),"", unique_id, type);
        try {
            JSONArray group_members = db.getGroupMembers(group_id);
            for (int i = 0; i < group_members.length(); i++)
            {
                JSONObject member = group_members.getJSONObject(i);
                db.addGroupChatStatus(unique_id, "pending", member.getString("phone"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // todo remove later, good for development
        //Toast.makeText(ctx, "Local Database Updated Successfully", Toast.LENGTH_LONG).show();
        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
                return user.sendGroupChat(group_id,db.getUserDetails().get("phone"),type,message,db.getUserDetails().get("display_name"),unique_id, auth_token);
            }

            @Override
            protected void onPostExecute(JSONObject row) {
                if(!row.optString("group_unique_id").equals("")){
                    sendNotification("Message Sent To Server", row.toString());
                    try {
                        JSONArray group_members = db.getGroupMembers(group_id);
                        for (int i = 0; i < group_members.length(); i++)
                        {
                            JSONObject member = group_members.getJSONObject(i);
                            db.updateGroupChatStatus(unique_id, "sent", member.getString("phone"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if(MainActivity.isVisible) {
                        MainActivity.mainActivity.updateGroupUIChat();
                    }
                }else if(row.optString("Error").equals("No Internet")){
                    // todo good for debug but remove in release
                    sendNotification("No Internet Connection", "Message will be sent as soon as the device gets connected to internet");
                }else{
                    // todo good for debug but remove in release
                    sendNotification("Failed to Send Message", "Oops message was not sent due to some reason");
                }
            }

        }.execute();

        return unique_id;
    }



    public JSONObject getMemberData(String group_name, String group_id, String phone[]){
        JSONObject groupPost = new JSONObject();
        JSONObject body = new JSONObject();
        try {
            body.put("group_name", group_name);
            body.put("group_unique_id",  group_id);
            body.put("members",  new JSONArray(Arrays.asList(phone)));
            groupPost.put("body", body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return body;
    }

    public void addMemberOnServer(final String group_name, final String group_id,final String phone[], final String authtoken){


        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
                UserFunctions userFunctions = new UserFunctions(ctx);
//                getMemberData(group_name, group_id, phone)
                return userFunctions.addGroupMembers(getMemberData(group_name, group_id, phone), authtoken);
            }

            @Override
            protected void onPostExecute(JSONObject row) {
                if(row != null){
                    Toast.makeText(ctx, row.toString() + ctx.getString(R.string.group_utility_member_added) + getMemberData(group_name, group_id, phone), Toast.LENGTH_LONG).show();
//                    Toast.makeText(getContext(), "Group Successfully Created On Server", Toast.LENGTH_LONG).show();
                    for(int i = 0; i<phone.length; i++){
                        DatabaseHandler db = new DatabaseHandler(ctx);
                        db.leaveGroupServerPending(group_id, phone[i]);
                        Log.e("Group", group_id + " " + phone[i]);
                        Toast.makeText(ctx, group_id + " " + phone[i], Toast.LENGTH_LONG).show();
                    }
                }
            }

        }.execute();

    }


    public void leaveGroup(final String group_id,final String member_phone, final String authtoken){
        db.leaveGroup(group_id,member_phone);
        db.addGroupMemberRemovePending(group_id, member_phone);
        final String unique_id = randomString();
        if(member_phone.equals(db.getUserDetails().get("phone"))){
            db.addGroupMessage(group_id,"You left the group", member_phone,"", unique_id, "log");
        }else{
            db.addGroupMessage(group_id,db.getContactName(member_phone) + " left the group", member_phone,"", unique_id, "log");
        }
        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
                UserFunctions userFunctions = new UserFunctions(ctx);
                return  userFunctions.leaveGroup(group_id, authtoken);
            }

            @Override
            protected void onPostExecute(JSONObject row) {
                if(row != null){
                    if(row.optString("Error").equals("No Internet")){
                        // todo use Toast for this
                        sendNotification("No Internet Connection", "Server will be update that u left the group with id:" + group_id + " once the internet is restored");
                    }else {
                        db.leaveGroupMemberRemovePending(group_id, member_phone);
                        Toast.makeText(ctx, row.toString(), Toast.LENGTH_LONG).show();
                        Toast.makeText(ctx, ctx.getString(R.string.group_utility_member_leave), Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(ctx, "Oops something went wrong!", Toast.LENGTH_LONG).show();
                }
            }

        }.execute();


    }

    public void updateAdminStatus(String payload, String authtoken){
        try {
            JSONObject body = new JSONObject(payload);
            String group_id = body.getString("groupId");
            String admin_phone = body.getString("senderId");
            String person_phone = body.getString("personUpdated");
            String isAdmin = body.getString("isAdmin").equals("Yes") ? "1" : "0";
            db.updateAdminStatus(group_id,person_phone,isAdmin);

            String personWhoUpdates = "";
            JSONArray specificContact = db.getSpecificContact(admin_phone);
            if(specificContact.length() > 0) {
                personWhoUpdates = specificContact.getJSONObject(0).getString("display_name");
            } else {
                personWhoUpdates = admin_phone;
            }

            String personBeingUpdated = "";
            JSONArray specificAddedContact = db.getSpecificContact(person_phone);
            if(specificAddedContact.length() > 0) {
                personBeingUpdated = specificAddedContact.getJSONObject(0).getString("display_name");
            } else {
                personBeingUpdated = person_phone;
            }
            if(person_phone.equals(db.getUserDetails().get("phone"))) personBeingUpdated = "you";

            String message = ""+ personWhoUpdates +" made "+ personBeingUpdated +" an admin of this group";
            if(isAdmin.equals("0"))
                message = ""+ personWhoUpdates +" removed "+ personBeingUpdated +" as admin of this group";

            String uniqueid = Long.toHexString(Double.doubleToLongBits(Math.random()));
            uniqueid += (new Date().getYear()) + "" + (new Date().getMonth()) + "" + (new Date().getDay());
            uniqueid += (new Date().getHours()) + "" + (new Date().getMinutes()) + "" + (new Date().getSeconds());

            DatabaseHandler db = new DatabaseHandler(ctx.getApplicationContext());
            db.addGroupChat(admin_phone, personWhoUpdates, message,
                    Utility.getCurrentTimeInISO(), "log",
                    uniqueid,
                    group_id);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void makeAdmin(final String group_id, final String member_phone, final String makeAdmin, final String authtoken){
        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
                UserFunctions userFunctions = new UserFunctions(ctx);
                return  userFunctions.updateMemberRole(group_id,member_phone, makeAdmin,authtoken);
            }

            @Override
            protected void onPostExecute(JSONObject row) {
                if(row != null){
                    if(row.optString("Error").equals("No Internet")){
                        // todo use Toast for this.
                        sendNotification("No Internet Connection", "Try again when internet is available");
                    }else{
                        db.makeGroupAdmin(group_id, member_phone);
                        Toast.makeText(ctx, ctx.getString(R.string.group_utility_member_role_update) + " " + row.toString(), Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(ctx, ctx.getString(R.string.group_utility_member_role_update_failed), Toast.LENGTH_LONG).show();
                }
            }

        }.execute();


    }

    public void demoteAdmin(final String group_id, final String member_phone, final String makeAdmin, final String authtoken){
        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
                UserFunctions userFunctions = new UserFunctions(ctx);
                return  userFunctions.updateMemberRole(group_id,member_phone, makeAdmin,authtoken);
            }

            @Override
            protected void onPostExecute(JSONObject row) {
                if(row != null){
                    if(row.optString("Error").equals("No Internet")){
                        // todo use Toast for this.
                        sendNotification("No Internet Connection", "Try again when internet is available");
                    }else{
                        db.demoteGroupAdmin(group_id,member_phone);
                        Toast.makeText(ctx, ctx.getString(R.string.group_utility_member_role_update) + " " + row.toString(), Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(ctx, ctx.getString(R.string.group_utility_member_role_update_failed), Toast.LENGTH_LONG).show();
                }
            }

        }.execute();


    }

    public void removeMember(final String group_id, final String member_phone, final String authtoken){
        db.leaveGroup(group_id,member_phone);
        db.addGroupMemberRemovePending(group_id, member_phone);
        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
                UserFunctions userFunctions = new UserFunctions(ctx);
                return  userFunctions.removeMember(group_id, member_phone, authtoken);
            }

            @Override
            protected void onPostExecute(JSONObject row) {
                if(row != null){
                    if(row.optString("Error").equals("No Internet")){
                        // todo remove this developer message
                        sendNotification("No Internet Connection", "Message will be sent as soon as the device gets connected to internet");
                    }else{
                    db.leaveGroupMemberRemovePending(group_id, member_phone);
                    Toast.makeText(ctx, ctx.getString(R.string.group_utility_member_removed)  + "Member pending: " + db.isGroupMembersRemovePending(group_id, member_phone), Toast.LENGTH_LONG).show();
                        if(db.isGroupMembersRemovePending(group_id, member_phone) > 0){
                            Toast.makeText(ctx, "Error Member still in pending table", Toast.LENGTH_LONG).show();
                        }
                 }
                }else{
                    // todo remove this developer message
                    Toast.makeText(ctx, "Member will be reomved from server when internet is restored!", Toast.LENGTH_LONG).show();
                }
            }

        }.execute();


    }

    public void memberLeftGroup(final String payload){
        try {
            JSONObject data = new JSONObject(payload);
            String member_phone = data.getString("senderId");
            String group_id = data.getString("groupId");
            // todo remove this developer message
            if(!db.isMute(group_id))
                sendNotification("Member Left", payload.toString());

            db.leaveGroup(group_id, member_phone);
            if(MainActivity.isVisible){
                MainActivity.mainActivity.updateGroupMembers();
            }

            String personWhoLeft = "";
            JSONArray specificContact = db.getSpecificContact(member_phone);
            if(specificContact.length() > 0) {
                personWhoLeft = specificContact.getJSONObject(0).getString("display_name");
            } else {
                personWhoLeft = member_phone;
            }

            String message = ""+ personWhoLeft +" left";
            String uniqueid = Long.toHexString(Double.doubleToLongBits(Math.random()));
            uniqueid += (new Date().getYear()) + "" + (new Date().getMonth()) + "" + (new Date().getDay());
            uniqueid += (new Date().getHours()) + "" + (new Date().getMinutes()) + "" + (new Date().getSeconds());

            DatabaseHandler db = new DatabaseHandler(ctx.getApplicationContext());
            db.addGroupChat(member_phone, personWhoLeft, message,
                    Utility.getCurrentTimeInISO(), "log",
                    uniqueid,
                    group_id);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateGroupMessageStatus(String payload, String auth_token){
           try {
               JSONObject body = new JSONObject(payload);
                String msg_unique_id = body.getString("uniqueId");
                String status = body.getString("status");

                String user_phone = body.getString("user_phone");
                String current_status = db.getGroupMessageStatus(msg_unique_id,user_phone).getJSONObject(0).getString("status");
                String read_time = "";
                String delivered_time = "";
                String current_time = Utility.getCurrentTimeInISO();
               if(!current_status.equals("seen")){
                   if(status.equals("delivered")){
                       delivered_time = current_time;
                       db.updateGroupChatStatusDeliveredTime(msg_unique_id, status, user_phone, delivered_time);
                   }
                   if(status.equals("seen")){
                       read_time = current_time;
                       db.updateGroupChatStatusReadTime(msg_unique_id, status, user_phone, read_time);
                   }
                   // todo remove this developer message
                  Toast.makeText(ctx, "Updated Chat Status to: " + status, Toast.LENGTH_LONG).show();
               }
               if(MainActivity.isVisible) {
                   MainActivity.mainActivity.updateGroupUIChat();
               }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void updateGroupName(String payload, String auth_token){
        try {
            JSONObject body = new JSONObject(payload);
            final String unique_id = randomString();
            db.addGroupMessage(body.getString("groupId"), "Group Name was updated to " +body.getString("new_name")+ " by " + db.getContactName(body.getString("senderId")) , db.getUserDetails().get("phone"),"", unique_id, "log");
            db.updateGroupName(body.getString("groupId"), body.getString("new_name"));

        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    public int adminCount(String group_id){
        try {
            JSONArray admins = db.getGroupAdmins(group_id);
//            Toast.makeText(ctx, admins.toString(), Toast.LENGTH_LONG).show();
            return admins.length();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean isAdmin(String group_id){
        DatabaseHandler db = new DatabaseHandler(ctx);
        try {
            JSONObject details = db.getMyDetailsInGroup(group_id);
            if(details.getString("isAdmin").equals("1")){
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isAdmin(String group_id, String member_phone){
        DatabaseHandler db = new DatabaseHandler(ctx);
        try {
            JSONObject details = db.getGroupMemberDetail(group_id, member_phone);
            if(details.getString("isAdmin").equals("1")){
                Toast.makeText(ctx, details.getString("display_name") + " " + member_phone + " is admin", Toast.LENGTH_LONG).show();
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isGroupMember(String group_id){
        DatabaseHandler db = new DatabaseHandler(ctx);
        try {
            JSONObject details = db.getMyDetailsInGroup(group_id);
            if(details.length() <= 0){
                return false;
            }else {
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void sendNotification(String header, String msg) {

//        Utility.sendLogToServer(""+ userDetail.get("phone") +" is showing alert and chime now.");

        Intent intent = new Intent(ctx, SplashScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        mNotificationManager = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
                intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx)
                        .setSmallIcon(R.drawable.icon)
                        .setContentTitle(header)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setSound(defaultSoundUri)
                        .setAutoCancel(true)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    public String randomString() {
        String uniqueid = Long.toHexString(Double.doubleToLongBits(Math.random()));
        uniqueid += (new Date().getYear()) + "" + (new Date().getMonth()) + "" + (new Date().getDay());
        uniqueid += (new Date().getHours()) + "" + (new Date().getMinutes()) + "" + (new Date().getSeconds());
        return uniqueid;
    }

    public boolean isMember(String group_id){
        try {
            JSONObject details = db.getMyDetailsInGroup(group_id);
            Toast.makeText(ctx, "Details Size: " + details.length(), Toast.LENGTH_LONG).show();
            if(details.length() > 0){
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  false;
    }
}


