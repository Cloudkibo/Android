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

import com.cloudkibo.R;
import com.cloudkibo.SplashScreen;
import com.cloudkibo.database.CloudKiboDatabaseContract;
import com.cloudkibo.database.DatabaseHandler;

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

    public void syncGroupToLocalDatabase(final String group_id, final String auth_token){

        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
               JSONObject temp = user.getGroupInfo(group_id, auth_token);
                sendNotification("Local Database Updated", temp.toString());
                return temp;
            }

            @Override
            protected void onPostExecute(JSONObject row) {
                if(row != null){
//                    sendNotification("Local Database Updated", row.toString());
                }
            }

        }.execute();

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
            body.put("unique_id",  group_id);
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
                return userFunctions.addGroupMembers(getMemberData(group_name, group_id, phone), authtoken);
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


