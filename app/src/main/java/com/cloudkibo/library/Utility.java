package com.cloudkibo.library;

import android.app.ActionBar;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.annotation.BoolRes;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.SplashScreen;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.ui.GroupChat;
import com.github.nkzawa.socketio.client.IO;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;

/**
 * Created by sojharo on 20/09/2016.
 */

public class Utility {

    public static final int NOTIFICATION_ID = 1;
    private static NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public static String convertDateToLocalTimeZoneAndReadable(String dStr) throws ParseException {

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        Date date = format.parse(dStr);

        TimeZone tZ = TimeZone.getDefault();
        tZ = TimeZone.getTimeZone(tZ.getID());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tZ);
        int currentOffsetFromUTC = tZ.getRawOffset() + (tZ.inDaylightTime(date) ? tZ.getDSTSavings() : 0);
        String result = df.format(date.getTime() + currentOffsetFromUTC);

        return result;

        /*
        TimeZone tz = TimeZone.getDefault();
        String gmt1= TimeZone.getTimeZone(tz.getID()).getDisplayName(false, TimeZone.SHORT);
        String gmt2= TimeZone.getTimeZone(tz.getID()).getDisplayName(false, TimeZone.LONG);
        Log.d("Tag","TimeZone : "+gmt1+"\t"+gmt2);

        boolean additionRequired = (gmt1.contains("+"));
        String hourOffset = gmt1.substring(4, 6);
        String minuteOffset = gmt1.substring(7, 9);

        Calendar cal = Calendar.getInstance(); // creates calendar
        cal.setTime(date); // sets calendar time/date
        if(additionRequired) {
            cal.add(Calendar.HOUR_OF_DAY, Integer.parseInt(hourOffset)); // adds hours
            cal.add(Calendar.MINUTE, Integer.parseInt(minuteOffset)); // adds minutes
        } else {
            cal.add(Calendar.HOUR_OF_DAY, Integer.parseInt(hourOffset) * (-1)); // adds hours
            cal.add(Calendar.MINUTE, Integer.parseInt(minuteOffset) * (-1)); // adds minutes
        }
        Date date2 = cal.getTime();

        cal.setTime(date2);

        return  String.format("%02d", cal.get(Calendar.DATE)) +"-"+
                String.format("%02d", (cal.get(Calendar.MONTH)+1)) +"-"+
                String.format("%02d", cal.get(Calendar.YEAR)) +" "+
                String.format("%02d", cal.get(Calendar.HOUR_OF_DAY)) +":"+
                String.format("%02d", cal.get(Calendar.MINUTE));
        */
    }

    public String loadImageUriFromPhoneContact(String address, Context context) {
        Uri contactUri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, Uri.encode(address));
        // querying contact data store
        Cursor phones = context.getContentResolver().query(contactUri, new String[]{ContactsContract.CommonDataKinds.Phone.PHOTO_URI}, null, null, null);

        String image_uri = "";
        int phoneColumnIndex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI);
        while (phones.moveToNext()) {
            image_uri = phones.getString(phoneColumnIndex);

        }
        phones.close();
        return image_uri;
    }

    public void updateDatabaseWithContactImages(final Context context, final ArrayList<String> phone){

        final DatabaseHandler db = new DatabaseHandler(context);

        new AsyncTask<String, String, String>() {

            @Override
            protected String doInBackground(String... args) {
                final String phone_array[];
                try {
                    phone_array = db.getContactsPhone();
                    for (int i=0; i < phone_array.length; i++){
                        db.addContactImage(phone_array[i],loadImageUriFromPhoneContact(phone_array[i], context));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                return "Images Synced Successfully";
            }

            @Override
            protected void onPostExecute(String row) {
                Toast.makeText(context, row, Toast.LENGTH_LONG).show();
                MainActivity.mainActivity.updateChatList();
                //MainActivity.mainActivity.updateGroupUIChat();
            }

        }.execute();
    }

    public static String getCurrentTimeInISO(){
        TimeZone tZ = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tZ);
        return df.format(new Date());
    }

    public static String[] extractLinks(String text) {
        List<String> links = new ArrayList<String>();
        Matcher m = Patterns.WEB_URL.matcher(text);
        while (m.find()) {
            String url = m.group();
            Log.d("EXTRACT LINK", "URL extracted: " + url);
            links.add(url);
        }

        return links.toArray(new String[links.size()]);
    }

    public static void sendLogToServer(final Context ctx, final String message){
        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
                UserFunctions userFunction = new UserFunctions(ctx);
                return userFunction.sendLog("ANDROID : "+message);
            }

            @Override
            protected void onPostExecute(JSONObject row) {
                try {

                    if (row != null) {
                        if(row.has("status")){
                            if(!row.getString("status").equals("success")){

                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }.execute();
    }

    public static void blockContact(final Context ctx, final JSONObject body, final String authtoken){
        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
                UserFunctions userFunction = new UserFunctions(ctx);
                return userFunction.blockContact(body, authtoken);
            }

            @Override
            protected void onPostExecute(JSONObject row) {
                try {

                    if (row != null) {
                        if(row.has("status")){
                            if(row.getString("status").equals("Successfully blocked.")){
                                DatabaseHandler db = new DatabaseHandler(ctx);
                                db.blockContact(body.getString("phone"));
                                sendNotification(ctx, "Blocked Contact", "You blocked one contact.");
                                if (MainActivity.isVisible) {
                                    MainActivity.mainActivity.handleBlockUnblock(body.getString("phone"), true);
                                }
                            }
                        } else if (row.has("Error")) {
                            if (row.getString("Error").equals("No Internet")) {
                                sendNotification(ctx, "No Internet", "Contact was not blocked.");
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }.execute();
    }

    public static void unBlockContact(final Context ctx, final JSONObject body, final String authtoken){
        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
                UserFunctions userFunction = new UserFunctions(ctx);
                return userFunction.unBlockContact(body, authtoken);
            }

            @Override
            protected void onPostExecute(JSONObject row) {
                try {

                    if (row != null) {
                        if(row.has("status")){
                            if(row.getString("status").equals("Successfully blocked.")){
                                DatabaseHandler db = new DatabaseHandler(ctx);
                                db.unBlockContact(body.getString("phone"));
                                sendNotification(ctx, "Unblocked Contact", "You unblocked one contact.");
                                if (MainActivity.isVisible) {
                                    MainActivity.mainActivity.handleBlockUnblock(body.getString("phone"), false);
                                }
                            }
                        } else if (row.has("Error")) {
                            if (row.getString("Error").equals("No Internet")) {
                                sendNotification(ctx, "No Internet", "Contact was not unblocked.");
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }.execute();
    }

    public static void getURLInfo(final Context ctx, final String url, final String unique_id, final Boolean isGroupMessage){

        new AsyncTask<String, String, String>() {

            @Override
            protected String doInBackground(String... args) {
                String result = null;
                try{
                    result = TitleExtractor.getPageTitle(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                if(result != null){
                    DatabaseHandler db = new DatabaseHandler(ctx);
                    db.createLinksInfo(unique_id, url, result, "", "");
                    if(isGroupMessage) {
                        db.updateChatForTypeGroup("link", unique_id);
                        if(MainActivity.isVisible) {
                            MainActivity.mainActivity.updateGroupUIChatForLink(unique_id, url, result);
                        }
                    } else {
                        db.updateChatForType("link", unique_id);
                        MainActivity.mainActivity.updateGroupChatForLink(unique_id, url, result);
                    }
                }
            }

        }.execute();

    }

    public static String dateConversion(String time){

        try {
            final SimpleDateFormat sdf = new SimpleDateFormat("H:mm");
            final Date dateObj = sdf.parse(time);
//            System.out.println(dateObj);
//            System.out.println();
            return new SimpleDateFormat("K:mm a").format(dateObj);
        } catch (final ParseException e) {
            e.printStackTrace();
        }
        return time;
    }

    public static String getContact(Context context, Intent data) {
        Uri contactData = data.getData();

        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(contactData,
                null, null, null, null);
        String name = "";
        if (cur.moveToFirst()) {
            name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }
        return name;

    }

    public static void getLastSeenStatus(final Context ctx, final String phone, final String authtoken, final ActionBar actbar){
        Log.e("Last Seen on", "In last seen");
        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
                UserFunctions userFunctions = new UserFunctions(ctx);
                return userFunctions.getUserStatus(phone, authtoken);
            }

            @Override
            protected void onPostExecute(JSONObject row) {
                Log.e("Last Seen on", "post executed");
                if(row != null){
                    Log.e("Last Seen on", "Fetched data");



                    try {
                        actbar.setSubtitle(Html.fromHtml("<font color='#ffffff'>"+"Last Seen on " + convertDateToLocalTimeZoneAndReadable(row.getString("last_seen"))+"</font>"));
                        String text = Html.toHtml(Html.fromHtml("<font color='#ffffff'>"+"Last Seen on " + convertDateToLocalTimeZoneAndReadable(row.getString("last_seen"))+"</font>"));
                        Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Log.e("Last Seen on", "Inside TRY Statement");


                }
                Log.e("Last Seen on", "leaving post executed");
            }

        }.execute();
    }

    public static void sendNotification(Context ctx, String header, String msg) {

        //Utility.sendLogToServer(""+ userDetail.get("phone") +" is showing alert and chime now.");

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

}
