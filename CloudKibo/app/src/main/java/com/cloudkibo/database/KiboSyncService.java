package com.cloudkibo.database;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.TextView;

import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.SplashScreen;
import com.cloudkibo.library.UserFunctions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class KiboSyncService extends Service {


    private final IBinder kiboSyncBinder = new KiboSyncBinder();
    private BoundKiboSyncListener mListener;

    String authtoken;

    Boolean startWithAddressBook = true;

    final ArrayList<String> contactList1 = new ArrayList<String>();
    final ArrayList<String> contactList1Phone = new ArrayList<String>();

    public void startSync (String token) {

        authtoken = token;

        doUpwardSync();

        loadChatFromServer();

    }

    public void startSyncWithoutAddressBookAccess (String token) {

        authtoken = token;

        startWithAddressBook = false;

        doUpwardSync();

        loadChatFromServer();

    }

    private void doUpwardSync(){
        DatabaseHandler db = new DatabaseHandler(
                getApplicationContext());
        try {
            JSONArray chats = db.getPendingChat();

            for (int i=0; i < chats.length(); i++) {
                JSONObject row = chats.getJSONObject(i);

                mListener.sendPendingMessageUsingSocket(
                        row.getString("toperson"),
                        row.getString("msg"), row.getString("uniqueid")
                );

            }
        }catch(JSONException e ){
            e.printStackTrace();
        }
    }


    private void loadContactsFromAddressBook(){

        new AsyncTask<String, String, JSONObject>() {
            private ProgressDialog nDialog;


            @Override
            protected JSONObject doInBackground(String... args) {

                List<NameValuePair> phones = new ArrayList<NameValuePair>();
                List<NameValuePair> emails = new ArrayList<NameValuePair>();

                ContentResolver cr = getApplicationContext().getContentResolver();
                Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                        null, null, null, null);
                if (cur.getCount() > 0) {
                    while (cur.moveToNext()) {
                        String id = cur.getString(
                                cur.getColumnIndex(ContactsContract.Contacts._ID));
                        String name = cur.getString(
                                cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        //Log.w("Contact Name : ", "Name " + name + "");
                        if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                            Cursor pCur = cr.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                                    new String[]{id}, null);
                            while (pCur.moveToNext()) {
                                String phone = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                if(phone.charAt(0) != '+') continue;
                                if(contactList1Phone.contains(phone)) continue;
                                if(Character.isLetter(name.charAt(0)))
                                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
                                phone = phone.replaceAll("\\s+","");
                                phones.add(new BasicNameValuePair("phonenumbers", phone));
                                Log.w("Phone Number: ", "Name : " + name + " Number : " + phone);
                                contactList1.add(name);
                                contactList1Phone.add(phone);
                            }
                            pCur.close();
                        }
                        /*Cursor emailCur = cr.query(
                                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                                new String[]{id}, null);
                        while (emailCur.moveToNext()) {
                            // This would allow you get several email addresses
                            // if the email addresses were stored in an array
                            String email = emailCur.getString(
                                    emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                            String emailType = emailCur.getString(
                                    emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
                            emails.add(new BasicNameValuePair("emails", email));
                            Log.w("Email: ", "Name : " + name + " Email : " + email);
                            //contactList1.add(name);
                            //contactList1Phone.add(email);
                            break;
                        }
                        emailCur.close();*/
                    }
                }
                cur.close();

                UserFunctions userFunction = new UserFunctions();
                JSONObject json = userFunction.sendAddressBookPhoneContactsToServer(phones, authtoken);
                Log.w("SERVER SENT RESPONSE", json.toString());
                return json;
            }

            @Override
            protected void onPostExecute(JSONObject json) {

                try {

                    if(json != null){

                        JSONArray jArray = json.getJSONArray("available");

                        for(int i = 0; i<jArray.length(); i++){
                            contactList1.remove(contactList1Phone.indexOf(jArray.get(i).toString()));
                            contactList1Phone.remove(contactList1Phone.indexOf(jArray.get(i).toString()));
                            Log.w("REMOVING", jArray.get(i).toString());
                        }

                        loadNotFoundContacts(contactList1, contactList1Phone);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

        }.execute();

    }

    private void loadNotFoundContacts(ArrayList<String> contactList1, ArrayList<String> contactList1Phone) {

        DatabaseHandler db = new DatabaseHandler(
                getApplicationContext());

        db.resetContactsTable();

        db = new DatabaseHandler(
                getApplicationContext());

        for (int i = 0; i < contactList1.size(); i++) {
            db.addContact("false", "null",
                    contactList1Phone.get(i),
                    contactList1.get(i),
                    "null",
                    "No",
                    "N/A");
        }

        loadCurrentContactsFromServer();

        /*try {
            JSONArray contacts = db.getContacts();
            contacts.toString();
        }catch(JSONException e){
            e.printStackTrace();
        }*/
    }

    private void loadCurrentContactsFromServer(){

        new AsyncTask<String, String, JSONArray>() {

            @Override
            protected JSONArray doInBackground(String... args) {
                UserFunctions userFunction = new UserFunctions();
                JSONArray json = userFunction.getContactsList(authtoken);
                return json;
            }

            @Override
            protected void onPostExecute(JSONArray jsonA) {
                try {

                    if (jsonA != null) {

                        DatabaseHandler db = new DatabaseHandler(
                                getApplicationContext());

                        for (int i=0; i < jsonA.length(); i++) {
                            JSONObject row = jsonA.getJSONObject(i);

                            db.addContact("true", "null",
                                    row.getJSONObject("contactid").getString("phone"),
                                    row.getJSONObject("contactid").getString("display_name"),
                                    row.getJSONObject("contactid").getString("_id"),
                                    row.getString("detailsshared"),
                                    row.getJSONObject("contactid").getString("status"));

                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                            if(row.getBoolean("unreadMessage")){
                                Notification n = new Notification.Builder(getApplicationContext())
                                        .setContentTitle(row.getJSONObject("contactid").getString("display_name"))
                                        .setContentText("Unread Message")
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

                        /*try {
                            JSONArray contacts = db.getContacts();
                            contacts.toString();
                        }catch(JSONException e){
                            e.printStackTrace();
                        }*/

                        mListener.contactsLoaded();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }.execute();

    }

    private void loadChatFromServer() {

        new AsyncTask<String, String, JSONArray>() {

            @Override
            protected JSONArray doInBackground(String... args) {
                DatabaseHandler db = new DatabaseHandler(
                        getApplicationContext());
                UserFunctions userFunction = new UserFunctions();
                JSONArray json = new JSONArray();
                try {
                    json = userFunction.getAllChatList(db.getUserDetails().get("phone"), authtoken).getJSONArray("msg");
                } catch(JSONException e){
                    e.printStackTrace();
                }
                return json;
            }

            @Override
            protected void onPostExecute(JSONArray jsonA) {
                try {

                    if (jsonA != null) {

                        if(jsonA.length() == 0) return;

                        DatabaseHandler db = new DatabaseHandler(
                                getApplicationContext());

                        db.resetChatsTable();

                        db = new DatabaseHandler(
                                getApplicationContext());

                        HashMap<String, String> user = db.getUserDetails();

                        db = new DatabaseHandler(
                                getApplicationContext());

                        for (int i=0; i < jsonA.length(); i++) {
                            JSONObject row = jsonA.getJSONObject(i);

                            db.addChat(row.getString("to"), row.getString("from"), row.getString("fromFullName"),
                                    row.getString("msg"), row.getString("date"),
                                    row.has("status") ? row.getString("status") : "",
                                    row.has("uniqueid") ? row.getString("uniqueid") : "");

                            if(row.has("status")){
                                if(row.getString("to").equals(db.getUserDetails().get("phone")) && row.getString("status").equals("sent")){
                                    db = new DatabaseHandler(
                                            getApplicationContext());
                                    db.updateChat("delivered", row.getString("uniqueid"));
                                    mListener.sendMessageStatusUsingSocket(row.getString("from"),
                                            "delivered", row.getString("uniqueid"));

                                }
                            }

                        }

                        /*try {
                            JSONArray chat = db.getChat();
                            chat.toString();
                        }catch(JSONException e){
                            e.printStackTrace();
                        }*/

                        if(startWithAddressBook)
                            loadContactsFromAddressBook();
                        else {
                            loadCurrentContactsFromServer();
                        }

                        mListener.chatLoaded();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }.execute();

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