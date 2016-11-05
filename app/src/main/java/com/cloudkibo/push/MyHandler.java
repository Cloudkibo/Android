package com.cloudkibo.push;

/**
 * Created by sojharo on 20/08/2016.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.SplashScreen;
import com.cloudkibo.database.DatabaseHandler;
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
        sendNotification("Test Push Notification", nhMessage); // todo remove this
        Utility.sendLogToServer(""+ userDetail.get("phone") +" gets push notification payload : "+ nhMessage);
        JSONObject payload;
        try {
            payload = new JSONObject(nhMessage);
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
                }

                if(payload.getString("type").equals("group:you_are_added")){
                    if (MainActivity.isVisible) {
                        MainActivity.mainActivity.ToastNotify2("You are added to a group.");
                    }
                }
                else if(payload.getString("type").equals("group:chat_received")){
                    if (MainActivity.isVisible) {
                        loadSpecificGroupChatFromServer(payload.getString("unique_id"));
                        MainActivity.mainActivity.ToastNotify(nhMessage);
                        MainActivity.mainActivity.ToastNotify2("got push notification for chat message.");
                    } else {
                        String displayName = "";
                        DatabaseHandler db = new DatabaseHandler(context);
                        JSONArray contactInAddressBook = db.getSpecificContact(payload.getString("senderId"));
                        if(contactInAddressBook.length() > 0) {
                            displayName = contactInAddressBook.getJSONObject(0).getString("display_name");
                        } else {
                            displayName = payload.getString("senderId");
                        }
                        sendNotification(
                                displayName,
                                payload.getString("msg")
                        );
                        loadSpecificGroupChatFromServer(payload.getString("unique_id"));
                    }
                }


            }
            if (MainActivity.isVisible) {
                loadSpecificChatFromServer(payload.getString("uniqueId"));
                MainActivity.mainActivity.ToastNotify(nhMessage);
                MainActivity.mainActivity.ToastNotify2("got push notification for chat message.");
            } else {
                String displayName = "";
                DatabaseHandler db = new DatabaseHandler(context);
                JSONArray contactInAddressBook = db.getSpecificContact(payload.getString("senderId"));
                if(contactInAddressBook.length() > 0) {
                    displayName = contactInAddressBook.getJSONObject(0).getString("display_name");
                } else {
                    displayName = payload.getString("senderId");
                }
                sendNotification(
                        displayName,
                        payload.getString("msg")
                );
                loadSpecificChatFromServer(payload.getString("uniqueId"));
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

                        row = row.getJSONArray("msg").getJSONObject(0);

                        db.addChat(row.getString("to"), row.getString("from"), row.getString("fromFullName"),
                                row.getString("msg"), row.getString("date"),
                                row.has("status") ? row.getString("status") : "",
                                row.has("uniqueid") ? row.getString("uniqueid") : "");

                        Utility.sendLogToServer(""+ userDetail.get("phone") +" got the message using API and saved to Database: "+ row.toString());

                        if (MainActivity.isVisible) {
                            MainActivity.mainActivity.handleIncomingChatMessage("im", row);
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
                                row.getString("date"), row.getString("type"),
                                row.getString("unique_id"),
                                row.getString("group_unique_id"));

                        Utility.sendLogToServer(""+ userDetail.get("phone") +" got the group message using API and saved to Database: "+ row.toString());

                        if (MainActivity.isVisible) {
                            // todo @dayem please update the UI for incoming group chat when UI logic is done
                            ///MainActivity.mainActivity.handleIncomingChatMessage("im", row);
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