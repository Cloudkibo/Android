package com.cloudkibo.library;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.cloudkibo.MainActivity;
import com.cloudkibo.database.DatabaseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by sojharo on 20/09/2016.
 */

public class Utility {

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
        Toast.makeText(context,"Starting Background Task", Toast.LENGTH_LONG).show();
        new AsyncTask<String, String, String>() {

            @Override
            protected String doInBackground(String... args) {

                for (int i=0; i < phone.size(); i++){
                    db.addContactImage(phone.get(i),loadImageUriFromPhoneContact(phone.get(i), context));
                }

                return "Images Synced Successfully";
            }

            @Override
            protected void onPostExecute(String row) {
                Toast.makeText(context, row, Toast.LENGTH_LONG).show();
                MainActivity.mainActivity.updateChatList();
                MainActivity.mainActivity.updateGroupUIChat();
            }

        }.execute();
    }

    public static String getCurrentTimeInISO(){
        TimeZone tZ = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tZ);
        return df.format(new Date());
    }

    public static void sendLogToServer(final String message){
        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
                UserFunctions userFunction = new UserFunctions();
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

}
