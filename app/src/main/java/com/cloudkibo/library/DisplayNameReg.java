package com.cloudkibo.library;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.content.Context;

//import com.cloudkibo.R;
//import com.cloudkibo.R.id;

import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.R.id;
import com.cloudkibo.database.BoundKiboSyncListener;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.database.KiboSyncService;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The Class Login is an Activity class that shows the login screen to users.
 * The current implementation simply start the MainActivity. You can write your
 * own logic for actual login and for login using Facebook and Twitter.
 */
public class DisplayNameReg extends Activity
{

    KiboSyncService kiboSyncService;
    boolean kiboServiceIsBound = false;

    Button btnRegister;
    EditText userNameText;

    String authtoken;

    Boolean regButtonPressed = false;

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    final ArrayList<String> contactList1 = new ArrayList<String>();
    final ArrayList<String> contactList1Phone = new ArrayList<String>();

    /* (non-Javadoc)
	 * @see com.chatt.custom.CustomActivity#onCreate(android.os.Bundle)
	 */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.display_name_reg);

        authtoken = getIntent().getExtras().getString("authtoken");

        userNameText = (EditText) findViewById(R.id.editTextDisplayName);
        btnRegister = (Button) findViewById(R.id.btnReg);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!userNameText.getText().toString().equals("")) {
                    if (!regButtonPressed) {
                        regButtonPressed = true;
                        registerDisplayName();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Display name is required", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Intent intentSync = new Intent(getApplicationContext(), KiboSyncService.class);
        bindService(intentSync, kiboSyncConnection, Context.BIND_AUTO_CREATE);
    }

    // todo this is temporary fix, fix it with 6.0 testing
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
                    "N/A");
        }

        kiboSyncService.startSyncWithoutAddressBookAccess(authtoken);

        try {
            JSONArray contacts = db.getContacts();
            contacts.toString();
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                loadContactsFromAddressBook(); // todo this is temporary fix, fix it with 6.0 testing
                //kiboSyncService.startSync(authtoken);
            } else {
                TextView hintText = (TextView) findViewById(id.hintText);
                hintText.setText("NOTE: For CloudKibo to work properly, permissions to contacts are necessary. You can give these permissions from settings of Android later. \n\n"+ hintText.getText());
                hintText.setText(hintText.getText() + "\n"+ "Setting conversations...");
                //loadCurrentContactsFromServer(); // todo this is temporary fix, fix it with 6.0 testing
                kiboSyncService.startSyncWithoutAddressBookAccess(authtoken);
            }
        }
    }

    public void registerDisplayName(){
        final TextView hintText = (TextView) findViewById(id.hintText);
        final EditText displayNameText = (EditText) findViewById(R.id.editTextDisplayName);


        new AsyncTask<String, String, Boolean>() {
            String displayName;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                hintText.setText("Setting your name ...");
                displayName = displayNameText.getText().toString();
            }

            @Override
            protected Boolean doInBackground(String... args) {

                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isConnected()) {
                    try {
                        URL url = new URL("http://www.google.com");
                        HttpURLConnection urlc = (HttpURLConnection) url
                                .openConnection();
                        urlc.setConnectTimeout(3000);
                        urlc.connect();
                        if (urlc.getResponseCode() == 200) {
                            return true;
                        }
                    } catch (MalformedURLException e1) {
                        e1.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return false;

            }

            @Override
            protected void onPostExecute(Boolean th) {

                if (th == true) {

                    new AsyncTask<String, String, JSONObject>() {

                        @Override
                        protected JSONObject doInBackground(String... args) {
                            UserFunctions userFunction = new UserFunctions();
                            List<NameValuePair> params = new ArrayList<NameValuePair>();
                            params.add(new BasicNameValuePair("display_name", displayName));
                            JSONObject json = userFunction.setDisplayName(params, authtoken);
                            return json;
                        }

                        @Override
                        protected void onPostExecute(JSONObject json) {
                            try {

                                if(json != null){

                                    DatabaseHandler db = new DatabaseHandler(
                                            getApplicationContext());

                                    // Clear all previous data in SQlite database.

                                    UserFunctions logout = new UserFunctions();
                                    logout.logoutUser(getApplicationContext());

                                    db.resetTables();
                                    db.resetContactsTable();
                                    db.resetChatsTable();

                                    db.addUser(json.getString("_id"),
                                            json.getString("display_name"),
                                            json.getString("phone"),
                                            json.getString("national_number"),
                                            json.getString("country_prefix"),
                                            json.getString("date"));

                                    if(!json.has("display_name")) {
                                        hintText.setText("Some error occurred. Please contact author.");
                                    } else {
                                        hintText.setText(hintText.getText() + "\n"+ "Setting contact list...");
                                        //HashMap<String,String> user = db.getUserDetails();
                                        //user.isEmpty();
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                                            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
                                            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
                                        } else {
                                            loadContactsFromAddressBook(); // todo this is temporary fix, fix it with 6.0 testing
                                            //kiboSyncService.startSync(authtoken);
                                        }
                                    }

                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }.execute();

                } else {
                    Toast.makeText(getApplicationContext(),
                            "Could not connect to Internet", Toast.LENGTH_SHORT)
                            .show();
                }
            }

        }.execute();
    }

    // todo this is temporary fix, fix it with 6.0 testing
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

                    ArrayList<String> contactList1Available = new ArrayList<String>();
                    ArrayList<String> contactList1PhoneAvailable = new ArrayList<String>();

                    if(json != null){

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

    private ServiceConnection kiboSyncConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            KiboSyncService.KiboSyncBinder binder = (KiboSyncService.KiboSyncBinder) service;
            kiboSyncService = binder.getService();
            kiboServiceIsBound = true;

            binder.setListener(new BoundKiboSyncListener() {

                @Override
                public void contactsLoaded() {
                    sendToNextScreen();
                }

                @Override
                public void chatLoaded() {


                }

                @Override
                public void sendPendingMessageUsingSocket(String contactPhone, String msg, String uniqueid) {

                }

                @Override
                public void sendMessageStatusUsingSocket(String contactPhone, String status, String uniqueid) {

                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            kiboServiceIsBound = false;
        }
    };

    public void sendToNextScreen(){
        unbindService(kiboSyncConnection);
        Intent i = new Intent(DisplayNameReg.this, MainActivity.class);
        i.putExtra("authtoken", authtoken);
        i.putExtra("sync", false);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }


    /** TODO What if user closes the app without giving display name */
    @Override
    public void onBackPressed() {
        finish();
    }

}
