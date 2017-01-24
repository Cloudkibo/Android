package com.cloudkibo.push;

/**
 * Created by sojharo on 20/08/2016.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.SplashScreen;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.library.GroupUtility;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.library.Utility;
import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.AccountKit;
import com.microsoft.windowsazure.notifications.NotificationsHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MyHandler extends NotificationsHandler {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    Context ctx;

    HashMap<String, String> userDetail;

    @Override
    public void onReceive(Context context, Bundle bundle) {
        ctx = context;
        AccountKit.initialize(ctx.getApplicationContext());
        String nhMessage = bundle.getString("message");
        userDetail = new DatabaseHandler(ctx.getApplicationContext()).getUserDetails();
        //sendNotification("Test Push Notification", nhMessage); // todo remove this
        Utility.sendLogToServer(""+ userDetail.get("phone") +" gets push notification payload : "+ nhMessage);
        JSONObject payload;
        try {
            payload = new JSONObject(nhMessage);
            Log.v("MyHandler", "Push received: "+ payload.toString());
            if (payload.has("type")) {
                if(payload.getString("type").equals("group:you_are_added")){
                    if (MainActivity.isVisible) {
                        MainActivity.mainActivity.ToastNotify2("You are added to a group.");
                    }
                    GroupUtility groupUtility = new GroupUtility(context);
                    final AccessToken accessToken = AccountKit.getCurrentAccessToken();
                    groupUtility.updateGroupToLocalDatabase(payload.getString("groupId"), payload.getString("group_name"),
                            accessToken.getToken(), payload.getString("senderId"), payload.getString("msg"));

                    DatabaseHandler db = new DatabaseHandler(ctx);
                    if(!db.isMute(payload.getString("groupId")))
                        sendNotification(payload.getString("group_name"), payload.getString("msg"));

                }

                if(payload.getString("type").equals("group:chat_received")){
                    GroupUtility groupUtility = new GroupUtility(context);
                    final AccessToken accessToken = AccountKit.getCurrentAccessToken();
                    groupUtility.updateGroupChat(payload.toString(), accessToken.getToken());
                }
                if(payload.getString("type").equals("group:added_to_group")){
                    GroupUtility groupUtility = new GroupUtility(context);
                    final AccessToken accessToken = AccountKit.getCurrentAccessToken();
                    groupUtility.updateGroupMembers(payload.toString(), accessToken.getToken());
                }
                if(payload.getString("type").equals("group:member_left_group")){
                    GroupUtility groupUtility = new GroupUtility(context);
                    groupUtility.memberLeftGroup(payload.toString());
                }

                if(payload.getString("type").equals("group:removed_from_group")){
                    GroupUtility groupUtility = new GroupUtility(context);
                    final AccessToken accessToken = AccountKit.getCurrentAccessToken();
                    groupUtility.removedFromGroup(payload.toString(), accessToken.getToken());
                }
                if(payload.getString("type").equals("group:icon_update")){
                    GroupUtility groupUtility = new GroupUtility(context);
                    final AccessToken accessToken = AccountKit.getCurrentAccessToken();
                    groupUtility.downloadGroupIcon(payload.toString(), context, accessToken.getToken());
                }
                if(payload.getString("type").equals("group:msg_status_changed")){
                    GroupUtility groupUtility = new GroupUtility(context);
                    final AccessToken accessToken = AccountKit.getCurrentAccessToken();
                    groupUtility.updateGroupMessageStatus(payload.toString(), accessToken.getToken());
                }
                if(payload.getString("type").equals("group:role_updated")){
                    GroupUtility groupUtility = new GroupUtility(context);
                    final AccessToken accessToken = AccountKit.getCurrentAccessToken();
                    groupUtility.updateAdminStatus(payload.toString(), accessToken.getToken());
                }
            }
            if(!payload.has("uniqueId")) {
                return;
            }
            if (payload.has("type")) {

                if(payload.getString("type").equals("status")){
                    try {
                        DatabaseHandler db = new DatabaseHandler(ctx.getApplicationContext());
                        db.updateChat(payload.getString("status"), payload.getString("uniqueId"));
                        Utility.sendLogToServer(""+ userDetail.get("phone") +" gets push notification payload to update status of sent message");
                        if(MainActivity.isVisible){
                            JSONObject statusData = new JSONObject();
                            statusData.put("status", payload.getString("status"));
                            statusData.put("uniqueid", payload.getString("uniqueId"));
                            MainActivity.mainActivity.handleIncomingStatusForSentMessage("status", statusData);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return ;
                } else if(payload.getString("type").equals("chat") || payload.getString("type").equals("contact")
                        || payload.getString("type").equals("location")) {
                    // todo work on this for files: image, document, audio and video

                    loadSpecificChatFromServer(payload.getString("uniqueId"));

                }


//                else if(payload.getString("type").equals("group:chat_received")){
//                    if (MainActivity.isVisible) {
//                        loadSpecificGroupChatFromServer(payload.getString("unique_id"));
//                        MainActivity.mainActivity.ToastNotify(nhMessage);
//                        MainActivity.mainActivity.ToastNotify2("got push notification for chat message.");
//                    } else {
//                        String displayName = "";
//                        DatabaseHandler db = new DatabaseHandler(context);
//                        JSONArray contactInAddressBook = db.getSpecificContact(payload.getString("senderId"));
//                        if(contactInAddressBook.length() > 0) {
//                            displayName = contactInAddressBook.getJSONObject(0).getString("display_name");
//                        } else {
//                            displayName = payload.getString("senderId");
//                        }
//
//
//                        sendNotification(
//                                displayName + "In group Chat receieced",
//                                payload.getString("msg")
//                        );
//                        loadSpecificGroupChatFromServer(payload.getString("unique_id"));
//                    }
//                }


            }

        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendNotification(String header, String msg) {

        Utility.sendLogToServer(""+ userDetail.get("phone") +" is showing alert and chime now.");

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

    private void loadSpecificChatFromServer(final String uniqueid) {

        final AccessToken accessToken = AccountKit.getCurrentAccessToken();

        Utility.sendLogToServer(""+ userDetail.get("phone") +" is going to fetch the message using API.");
        if (accessToken == null) {
            Utility.sendLogToServer(""+ userDetail.get("phone") +" could not get the message using API as Facebook accountkit did not give auth token.");
            return ;
        }

        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
                UserFunctions userFunction = new UserFunctions();
                return userFunction.getSingleChat(uniqueid, accessToken.getToken());
            }

            @Override
            protected void onPostExecute(JSONObject row) {
                try {

                    if (row != null) {
                        DatabaseHandler db = new DatabaseHandler(
                                ctx.getApplicationContext());

                        Log.i("MyHandler", row.toString());
                        //sendNotification("Single Chat Received", row.toString());
                        row = row.getJSONArray("msg").getJSONObject(0);

                        db.addChat(row.getString("to"), row.getString("from"), row.getString("fromFullName"),
                                row.getString("msg"), row.getString("date_server_received"),
                                row.has("status") ? row.getString("status") : "",
                                row.has("uniqueid") ? row.getString("uniqueid") : "",
                                row.has("type") ? row.getString("type") : "",
                                row.has("file_type") ? row.getString("file_type") : "");

                        Utility.sendLogToServer(""+ userDetail.get("phone") +" got the message using API and saved to Database: "+ row.toString());

                        if (MainActivity.isVisible) {
                            MainActivity.mainActivity.handleIncomingChatMessage("im", row);
                        } else {
                            String displayName = "";
                            db = new DatabaseHandler(ctx.getApplicationContext());
                            JSONArray contactInAddressBook = db.getSpecificContact(row.getString("from"));
                            if(contactInAddressBook.length() > 0) {
                                displayName = contactInAddressBook.getJSONObject(0).getString("display_name");
                            } else {
                                displayName = row.getString("from");
                            }
                            sendNotification(displayName, row.getString("msg"));
                        }

                    } else {
                        Utility.sendLogToServer(""+ userDetail.get("phone") +" did not get message from API. SERVER gave NULL");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }.execute();

    }

    private void loadSpecificGroupChatFromServer(final String uniqueid) {

        final AccessToken accessToken = AccountKit.getCurrentAccessToken();

        Utility.sendLogToServer(""+ userDetail.get("phone") +" is going to fetch the group message using API.");
        if (accessToken == null) {
            Utility.sendLogToServer(""+ userDetail.get("phone") +" could not get the group message using API as Facebook accountkit did not give auth token.");
            return ;
        }

        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
                UserFunctions userFunction = new UserFunctions();
                return userFunction.getSingleGroupChat(uniqueid, accessToken.getToken());
            }

            @Override
            protected void onPostExecute(JSONObject row) {
                try {

                    if (row != null) {
                        DatabaseHandler db = new DatabaseHandler(
                                ctx.getApplicationContext());

                        Log.i("MyHandler", row.toString());

                        // todo @dayem please test following when you are ready to send messsage, this is saving the received chat message

                        db.addGroupChat(row.getString("from"), row.getString("from_fullname"), row.getString("msg"),
                                row.getString("date"), row.has("type") ? row.getString("type") : "",
                                row.getString("unique_id"),
                                row.getString("group_unique_id"));

                        Utility.sendLogToServer(""+ userDetail.get("phone") +" got the group message using API and saved to Database: "+ row.toString());

                        if (MainActivity.isVisible) {
                            // todo @dayem please update the UI for incoming group chat when UI logic is done
                            ///MainActivity.mainActivity.handleIncomingChatMessage("im", row);
                            MainActivity.mainActivity.updateGroupUIChat();
                        } else {

                        }

                    } else {
                        Utility.sendLogToServer(""+ userDetail.get("phone") +" did not get group message from API. SERVER gave NULL");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }.execute();

    }

}