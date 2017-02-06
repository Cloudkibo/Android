package com.cloudkibo.push;

/**
 * Created by sojharo on 20/08/2016.
 */

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.SplashScreen;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.file.filechooser.utils.FileUtils;
import com.cloudkibo.library.GroupUtility;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.library.Utility;
import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.AccountKit;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.microsoft.windowsazure.notifications.NotificationsHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import static com.cloudkibo.file.filechooser.utils.FileUtils.getExternalStoragePublicDirForDocuments;
import static com.cloudkibo.file.filechooser.utils.FileUtils.getExternalStoragePublicDirForDownloads;
import static com.cloudkibo.file.filechooser.utils.FileUtils.getExternalStoragePublicDirForImages;
import static com.cloudkibo.file.filechooser.utils.FileUtils.getMimeType;
import static com.cloudkibo.webrtc.filesharing.Utility.getFileMetaData;

public class MyHandler extends NotificationsHandler {

    Context ctx;

    HashMap<String, String> userDetail;

    @Override
    public void onReceive(Context context, Bundle bundle) {
        ctx = context;
        AccountKit.initialize(ctx.getApplicationContext());
        String nhMessage = bundle.getString("message");
        userDetail = new DatabaseHandler(ctx.getApplicationContext()).getUserDetails();
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
                        Utility.sendNotification(ctx, payload.getString("group_name"), payload.getString("msg"));

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
                        || payload.getString("type").equals("location") || payload.getString("type").equals("file")) {

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
                            Utility.sendNotification(ctx, displayName, row.getString("msg"));
                        }

                        if (row.getString("type").equals("file")) {
                            final JSONObject rowTemp = row;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ctx.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                Utility.sendNotification(ctx, "KiboChat", "File attachment was not downloaded in background due to permission. Please go to settings to allow this app to store files on storage.");
                                db.createFilesInfo(uniqueid,
                                        "",
                                        "notDownloaded",
                                        rowTemp.getString("file_type"),
                                        "", "");
                            } else {
                                Ion.with(ctx.getApplicationContext())
                                        .load("https://api.cloudkibo.com/api/filetransfers/download")
                                        .setHeader("kibo-token", accessToken.getToken())
                                        .setBodyParameter("uniqueid", row.getString("uniqueid"))
                                        .write(new File(ctx.getApplicationContext().getFilesDir().getPath() + "" + row.getString("uniqueid")))
                                        .setCallback(new FutureCallback<File>() {
                                            @Override
                                            public void onCompleted(Exception e, File file) {
                                                // download done...
                                                // do stuff with the File or error

                                                try {
                                                    DatabaseHandler db = new DatabaseHandler(ctx.getApplicationContext());
                                                    File folder= getExternalStoragePublicDirForImages(ctx.getString(R.string.app_name));
                                                    if (rowTemp.getString("file_type").equals("document")){
                                                        folder= getExternalStoragePublicDirForDocuments(ctx.getString(R.string.app_name));
                                                    }
                                                    if (rowTemp.getString("file_type").equals("audio")){
                                                        folder= getExternalStoragePublicDirForDownloads(ctx.getString(R.string.app_name));
                                                    }
                                                    FileOutputStream outputStream;
                                                    outputStream = new FileOutputStream(folder.getPath() +"/"+ rowTemp.getString("msg"));
                                                    outputStream.write(com.cloudkibo.webrtc.filesharing.Utility.convertFileToByteArray(file));
                                                    outputStream.close();

                                                    JSONObject fileMetaData = getFileMetaData(folder.getPath() +"/"+ rowTemp.getString("msg"));
                                                    db.createFilesInfo(uniqueid,
                                                            fileMetaData.getString("name"),
                                                            fileMetaData.getString("size"),
                                                            rowTemp.getString("file_type"),
                                                            fileMetaData.getString("filetype"), folder.getPath() +"/"+ rowTemp.getString("msg"));

                                                    if (MainActivity.isVisible) {
                                                        MainActivity.mainActivity.handleDownloadedFile(rowTemp);
                                                    }

                                                    new AsyncTask<String, String, JSONObject>() {
                                                        @Override
                                                        protected JSONObject doInBackground(String... args) {
                                                            try {
                                                                return (new UserFunctions()).confirmFileDownload(rowTemp.getString("uniqueid"), accessToken.getToken());
                                                            } catch (JSONException e5){
                                                                e5.printStackTrace();
                                                            }
                                                            return null;
                                                        }
                                                        @Override
                                                        protected void onPostExecute(JSONObject row) {
                                                            if(row != null){
                                                                // todo see if server couldn't get the confirmation
                                                            }
                                                        }
                                                    }.execute();

                                                    file.delete();

                                                    Log.d("chat attachment", "Downloaded file attachment");

                                                }catch (IOException e2){
                                                    e2.printStackTrace();
                                                }catch (JSONException e3){
                                                    e3.printStackTrace();
                                                }


                                            }
                                        });
                            }

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
                           // MainActivity.mainActivity.updateGroupUIChat();
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