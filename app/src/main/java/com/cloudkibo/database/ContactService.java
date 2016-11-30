package com.cloudkibo.database;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;

import com.cloudkibo.R;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.ui.ContactList;
import com.cloudkibo.utils.IFragmentName;
import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.AccountKit;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sojharo on 26/09/2016.
 */
public class ContactService extends Service {

    private int mContactCount;
    private String authtoken;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContactCount = getContactCount();

        Log.d("Contact Service", mContactCount + "");

        this.getContentResolver().registerContentObserver(
                ContactsContract.Contacts.CONTENT_URI, true, mObserver);
    }

    private int getContactCount() {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                    ContactsContract.Contacts.CONTENT_URI, null, null, null,
                    null);
            if (cursor != null) {
                return cursor.getCount();
            } else {
                return 0;
            }
        } catch (Exception ignore) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }

    private ContentObserver mObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            final int currentCount = getContactCount();
            if (currentCount < mContactCount) {
                // CONTACT DELETED.
                loadContactsFromAddressBook();

                Log.d("Contact Service", currentCount + "");

            } else if (currentCount == mContactCount) {
                // CONTACT UPDATED.
                Log.d("Contact Service", currentCount+"");

            } else {
                // NEW CONTACT.
                Log.d("Contact Service", currentCount + "");
                loadContactsFromAddressBook();

            }
            mContactCount = currentCount;
        }

    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Contact Service", "On Start Method Called");
        try {
            authtoken = intent.getExtras().getString("authtoken");
        } catch(NullPointerException e){
            AccountKit.initialize(getApplicationContext());
            AccessToken accessToken = null;
            try {
                accessToken = AccountKit.getCurrentAccessToken();
            } catch (Exception ee) {
                ee.printStackTrace();
            }

            if (accessToken != null) {
                authtoken = accessToken.getToken();
            }
        }
        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Contact Service", "On Destroy Method Called");
        getContentResolver().unregisterContentObserver(mObserver);
    }

    private void loadContactsFromAddressBook(){
        final ArrayList<String> contactList1 = new ArrayList<String>();
        final ArrayList<String> contactList1Phone = new ArrayList<String>();

        new AsyncTask<String, String, JSONObject>() {
            private ProgressDialog nDialog;

            @Override
            protected void onPreExecute() {

            }

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
                        String image_uri = cur.getString(
                                cur.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));
                        //Log.w("Contact Name : ", "Name " + name + "");
                        if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                            Cursor pCur = cr.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                                    new String[]{id}, null);
                            while (pCur.moveToNext()) {
                                DatabaseHandler db = new DatabaseHandler(
                                        getApplicationContext());
                                String phone = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                if(phone.length() < 6) continue;
                                if(phone.charAt(0) != '+') {
                                    if(phone.charAt(0) == '0') phone = phone.substring(1, phone.length());
                                    if(phone.charAt(0) == '1') phone = "+" + phone;
                                    else phone = "+" + db.getUserDetails().get("country_prefix") + phone;
                                }
                                if(contactList1Phone.contains(phone)) continue;
                                //if(Character.isLetter(name.charAt(0)))
                                //    name = name.substring(0, 1).toUpperCase() + name.substring(1);
                                phone = phone.replaceAll("\\s+","");
                                phone = phone.replaceAll("\\p{P}","");
                                db = new DatabaseHandler(getApplicationContext());
                                String userPhone = db.getUserDetails().get("phone");
                                if(userPhone.equals(phone)) continue;
                                phones.add(new BasicNameValuePair("phonenumbers", phone));
                                Log.w("Phone Number: ", "Name : " + name + " Number : " + phone);
                                contactList1.add(name);
                                contactList1Phone.add(phone);
                                db.addContactImage(phone, image_uri);
                            }
                            pCur.close();
                        }
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

                    ArrayList<String> contactList1Available = new ArrayList<String>();
                    ArrayList<String> contactList1PhoneAvailable = new ArrayList<String>();

                    if(json != null){

                        if(json.has("Error")){
                            return ;
                        }

                        JSONArray jArray = json.getJSONArray("available");

                        for(int i = 0; i<jArray.length(); i++){
                            contactList1Available.add(contactList1.get(contactList1Phone.indexOf(jArray.get(i).toString())));
                            contactList1PhoneAvailable.add(contactList1Phone.get(contactList1Phone.indexOf(jArray.get(i).toString())));
                            contactList1.remove(contactList1Phone.indexOf(jArray.get(i).toString()));
                            contactList1Phone.remove(contactList1Phone.indexOf(jArray.get(i).toString()));
                            Log.w("REMOVING", jArray.get(i).toString());
                        }

                    }
                    loadNotFoundContacts(contactList1, contactList1Phone);
                    loadFoundContacts(contactList1Available, contactList1PhoneAvailable);

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

        try {
            JSONArray contacts = db.getContacts();
            contacts.toString();
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    private void loadFoundContacts(ArrayList<String> contactList1, ArrayList<String> contactList1Phone) {

        DatabaseHandler db = new DatabaseHandler(
                getApplicationContext());

        for (int i = 0; i < contactList1.size(); i++) {
            db.addContact("true", "null",
                    contactList1Phone.get(i),
                    contactList1.get(i),
                    "null",
                    "Yes",
                    "I am on CloudKibo");
            // todo work for status here
        }

        //kiboSyncService.startSyncWithoutAddressBookAccess(authtoken);

        try {
            JSONArray contacts = db.getContacts();
            contacts.toString();
        }catch(JSONException e){
            e.printStackTrace();
        }
    }
}