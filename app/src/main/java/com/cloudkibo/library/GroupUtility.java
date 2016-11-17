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
import android.widget.Toast;

import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.SplashScreen;
import com.cloudkibo.database.CloudKiboDatabaseContract;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.ui.ChatList;
import com.cloudkibo.ui.GroupChat;
import com.cloudkibo.utils.IFragmentName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Date;

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

    public void updateGroupToLocalDatabase(final String group_id, final String sender_phone, final String group_name,  final String auth_token){

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

    public void updateGroupChat(final String payload, final String auth_token){
        try {
            JSONObject data = new JSONObject(payload);
            String message = data.getString("msg");
            String member_phone = data.getString("senderId");
            String unique_id = data.getString("unique_id");
            String group_id = data.getString("groupId");

            db.addGroupMessage(group_id,message,member_phone,member_phone,unique_id);
            if(MainActivity.isVisible){
                MainActivity.mainActivity.updateGroupMembers();
            }

        } catch (JSONException e) {
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

    public void sendGroupMessage(final String group_id, final  String message, final  String auth_token){
        final String unique_id = randomString();
        db.addGroupMessage(group_id,message, db.getUserDetails().get("phone"),"", unique_id);
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
                }
            }

        }.execute();

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
                return  user.removeMember(group_id, member_phone, authtoken);
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

    private void sendNotification(String header, String msg) {

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


