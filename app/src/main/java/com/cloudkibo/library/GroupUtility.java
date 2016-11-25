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
import android.support.v4.os.EnvironmentCompat;
import android.util.Log;
import android.widget.Toast;

import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.SplashScreen;
import com.cloudkibo.database.CloudKiboDatabaseContract;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.ui.ChatList;
import com.cloudkibo.ui.GroupChat;
import com.cloudkibo.utils.IFragmentName;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by root on 11/14/16.
 */
public class GroupUtility {
    UserFunctions user = new UserFunctions();
    DatabaseHandler db;
    private NotificationManager mNotificationManager;
    public static final int NOTIFICATION_ID = 1;
    Context ctx;

    public GroupUtility(Context ctx){
        this.ctx = ctx;
        db = new DatabaseHandler(ctx);
    }

    public void updateGroupToLocalDatabase(final String group_id, final String group_name,  final String auth_token){

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

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

        }.execute();

    }

    public void updateMessageStatusToSeen(final String unique_id, final String auth_token){

            new AsyncTask<String, String, JSONObject>() {

                @Override
                protected JSONObject doInBackground(String... args) {
                    return user.updateGroupChatStatus(unique_id,auth_token);
                }

                @Override
                protected void onPostExecute(JSONObject row) {
                    if(row != null){
                        sendNotification("Message Status Update To Seen", row.toString());
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
            sendNotification("New Member Added", payload.toString());

            for (int i = 0; i < persons.length() ; i++) {
                db.addGroupMember(group_id,persons.getString(i).toString(),0,"joined");
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
            sendNotification("A Member was removed from a group.", payload.toString());

            db.leaveGroup(group_id, person_phone);
            if(MainActivity.isVisible){
                MainActivity.mainActivity.updateGroupMembers();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void downloadGroupIcon(final String payload, Context context, final String auth_token){
        try {
            JSONObject data = new JSONObject(payload);
            Ion.with(context)
                    .load("https://api.cloudkibo.com/api/groupmessaging/uploadIcon")
                    .setHeader("kibo-token", auth_token)
                    .setBodyParameter("unique_id", data.getString("groupId"))
                    .write(new File(context.getFilesDir().getPath() + "" + data.getString("groupId")))
                    .setCallback(new FutureCallback<File>() {
                        @Override
                        public void onCompleted(Exception e, File file) {
                            // download done...
                            // do stuff with the File or error

                            Log.d("GROUPFILE", "Downloaded icon");
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void updateGroupChat(final String payload, final String auth_token){
        try {
            JSONObject data = new JSONObject(payload);
            String message = data.getString("msg");
            String member_phone = data.getString("senderId");
            String unique_id = data.getString("unique_id");
            String group_id = data.getString("groupId");
            sendNotification(message, message);

            db.addGroupMessage(group_id,message,member_phone,member_phone,unique_id);
            if(MainActivity.isVisible){
                updateMessageStatusToSeen(unique_id, auth_token);
                MainActivity.mainActivity.updateGroupUIChat();
            }
            loadSpecificGroupChat(unique_id, auth_token);
        } catch (JSONException e) {
            MainActivity.mainActivity.updateGroupUIChat();
            e.printStackTrace();
        }

    }

    public void syncAllGroups(final String auth_token){

        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
                return user.getAllGroupInfo(auth_token);
            }

            @Override
            protected void onPostExecute(JSONObject row) {
                if(row != null){
                    sendNotification("All Groups Synced", row.toString());
                }
            }

        }.execute();

    }

    public String sendGroupMessage(final String group_id, final  String message, final  String auth_token){
        final String unique_id = randomString();
        db.addGroupMessage(group_id,message, db.getUserDetails().get("phone"),"", unique_id);
        db.addGroupChatStatus(unique_id, "pending", db.getUserDetails().get("phone"));
        Toast.makeText(ctx, "Local Database Updated Successfully", Toast.LENGTH_LONG).show();
        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
                return user.sendGroupChat(group_id,db.getUserDetails().get("phone"),"",message,db.getUserDetails().get("display_name"),unique_id, auth_token);
            }

            @Override
            protected void onPostExecute(JSONObject row) {
                if(row != null){
                    sendNotification("Message Sent To Server", row.toString());
                    db.updateGroupChatStatus(unique_id,"sent");
                    MainActivity.mainActivity.updateGroupUIChat();
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
                UserFunctions userFunctions = new UserFunctions();
//                getMemberData(group_name, group_id, phone)
                return userFunctions.addGroupMembers(getMemberData(group_name, group_id, phone), authtoken);
            }

            @Override
            protected void onPostExecute(JSONObject row) {
                if(row != null){
                    Toast.makeText(ctx, row.toString() + "Member added successfully" + getMemberData(group_name, group_id, phone), Toast.LENGTH_LONG).show();
//                    Toast.makeText(getContext(), "Group Successfully Created On Server", Toast.LENGTH_LONG).show();
                    for(int i = 0; i<phone.length; i++){
                        DatabaseHandler db = new DatabaseHandler(ctx);
                        db.leaveGroupServerPending(group_id, phone[i]);
                    }
                }
            }

        }.execute();

    }


    public void leaveGroup(final String group_id, String member_phone, final String authtoken){
        db.leaveGroup(group_id,member_phone);

        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
                UserFunctions userFunctions = new UserFunctions();
                return  user.leaveGroup(group_id,authtoken);
            }

            @Override
            protected void onPostExecute(JSONObject row) {
                if(row != null){
                    Toast.makeText(ctx, row.toString(), Toast.LENGTH_LONG).show();
//                    Toast.makeText(getContext(), "Group Successfully Created On Server", Toast.LENGTH_LONG).show();
                }
            }

        }.execute();


    }

    public void removeMember(final String group_id, final String member_phone, final String authtoken){
        db.leaveGroup(group_id,member_phone);

        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
                UserFunctions userFunctions = new UserFunctions();
                return  userFunctions.removeMember(group_id, member_phone, authtoken);
            }

            @Override
            protected void onPostExecute(JSONObject row) {
                if(row != null){
                    Toast.makeText(ctx, row.toString(), Toast.LENGTH_LONG).show();
//                    Toast.makeText(getContext(), "Group Successfully Created On Server", Toast.LENGTH_LONG).show();
                }
            }

        }.execute();


    }

    public void memberLeftGroup(final String payload){
        try {
            JSONObject data = new JSONObject(payload);
            String member_phone = data.getString("senderId");
            String group_id = data.getString("groupId");
            sendNotification("Member Left", payload.toString());

            db.leaveGroup(group_id, member_phone);
            if(MainActivity.isVisible){
                MainActivity.mainActivity.updateGroupMembers();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateGroupMessageStatus(String payload, String auth_token){
           try {
               JSONObject body = new JSONObject(payload);
                String msg_unique_id = body.getString("uniqueId");
                String status = body.getString("status");
                String current_status = db.getGroupMessageStatus(msg_unique_id);
               if(!current_status.equals("seen")){
                   db.updateGroupChatStatus(msg_unique_id, status);
                   Toast.makeText(ctx, "Updated Chat Status to Seen", Toast.LENGTH_LONG).show();
               }
                MainActivity.mainActivity.updateGroupUIChat();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void loadSpecificGroupChat(final String uniqueid, final String authtoken){

                final HashMap<String, String> userDetail = new DatabaseHandler(ctx.getApplicationContext()).getUserDetails();

                new AsyncTask<String, String, JSONObject>() {

                    @Override
                    protected JSONObject doInBackground(String... args) {
                        UserFunctions userFunction = new UserFunctions();
                        return userFunction.getSingleGroupChat(uniqueid, authtoken);
                    }

                    @Override
                    protected void onPostExecute(JSONObject row) {


                            if (row != null) {

                                Log.i("MyHandler", row.toString());

                                // todo @dayem please test following when you are ready to send messsage, this is saving the received chat message

                                Utility.sendLogToServer(""+ userDetail.get("phone") +" got the group message using API and saved to Database: "+ row.toString());

                                if (MainActivity.isVisible) {
                                    // todo @dayem please update the UI for incoming group chat when UI logic is done
                                    ///MainActivity.mainActivity.handleIncomingChatMessage("im", row);
                                }

                            } else {
                                Utility.sendLogToServer(""+ userDetail.get("phone") +" did not get group message from API. SERVER gave NULL");
                            }

                    }

                }.execute();

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

    String randomString() {
        String uniqueid = Long.toHexString(Double.doubleToLongBits(Math.random()));
        uniqueid += (new Date().getYear()) + "" + (new Date().getMonth()) + "" + (new Date().getDay());
        uniqueid += (new Date().getHours()) + "" + (new Date().getMinutes()) + "" + (new Date().getSeconds());
        return uniqueid;
    }
}


