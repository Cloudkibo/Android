package com.cloudkibo.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.SplashScreen;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.model.AddressBookContactItem;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sojharo on 10/2/2015.
 */
public class Invite_Friends extends Activity {
    Button button;
    ListView listView;
    ArrayAdapter<String> adapter;

    private String authtoken;

    String[] emails;

    final ArrayList<String> contactList1 = new ArrayList<String>();
    final ArrayList<String> contactList1Email = new ArrayList<String>();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invite_contacts);

        authtoken = getIntent().getExtras().getString("authtoken");

        listView = (ListView) findViewById(R.id.invite_contact_list);
        button = (Button) findViewById(R.id.testbutton);

        loadContactsFromAddressBook();

        emails = new String[0];

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SparseBooleanArray checked = listView.getCheckedItemPositions();
                final List<NameValuePair> emails = new ArrayList<NameValuePair>();
                ArrayList<String> selectedItems = new ArrayList<String>();
                for (int i = 0; i < checked.size(); i++) {
                    int position = checked.keyAt(i);
                    if (checked.valueAt(i)) {
                        selectedItems.add(adapter.getItem(position));
                        emails.add(new BasicNameValuePair("emails", contactList1Email.get(position)));
                    }
                }

                String[] outputStrArr = new String[selectedItems.size()];

                for (int i = 0; i < selectedItems.size(); i++) {
                    outputStrArr[i] = selectedItems.get(i);
                }

                new AsyncTask<String, String, JSONObject>() {
                    private ProgressDialog nDialog;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        nDialog = new ProgressDialog(Invite_Friends.this);
                        nDialog.setTitle("Inviting your friends to CloudKibo...");
                        nDialog.setMessage("Just a moment");
                        nDialog.setIndeterminate(false);
                        nDialog.setCancelable(true);
                        nDialog.show();
                    }

                    @Override
                    protected JSONObject doInBackground(String... args) {

                        UserFunctions userFunction = new UserFunctions();
                        JSONObject json = userFunction.sendEmailsOfInvitees(emails, authtoken);
                        Log.w("SERVER SENT RESPONSE", json.toString());
                        return json;
                    }

                    @Override
                    protected void onPostExecute(JSONObject json) {

                        try {

                            if(json != null){

                                String response = json.getString("status");

                                if(response.equals("success")) {
                                    Intent i = new Intent(Invite_Friends.this, MainActivity.class);
                                    i.putExtra("authtoken", authtoken);
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(i);
                                    finish();
                                }

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        nDialog.dismiss();

                    }

                }.execute();


                /*Intent intent = new Intent(getApplicationContext(),
                        ResultActivity.class);

                // Create a bundle object
                Bundle b = new Bundle();
                b.putStringArray("selectedItems", outputStrArr);

                // Add the bundle to the intent.
                intent.putExtras(b);

                // start the ResultActivity
                startActivity(intent);*/

            }
        });
    }

    public void loadContactsFromAddressBook(){

        new AsyncTask<String, String, JSONObject>() {
            private ProgressDialog nDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                nDialog = new ProgressDialog(Invite_Friends.this);
                nDialog.setTitle("Preparing things for you...");
                nDialog.setMessage("Hold on");
                nDialog.setIndeterminate(false);
                nDialog.setCancelable(true);
                nDialog.show();
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
                        //Log.w("Contact Name : ", "Name " + name + "");
                        if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                            Cursor pCur = cr.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                                    new String[]{id}, null);
                            while (pCur.moveToNext()) {
                                String phone = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                phones.add(new BasicNameValuePair("phonenumbers", phone));
                                //Log.w("Phone Number: ", "Name : "+ name +" Number : "+ phone);
                            }
                            pCur.close();
                        }
                        Cursor emailCur = cr.query(
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
                            contactList1.add(name);
                            contactList1Email.add(email);
                            break;
                        }
                        emailCur.close();
                    }
                }
                cur.close();

                UserFunctions userFunction = new UserFunctions();
                JSONObject json = userFunction.sendAddressBookEmailContactsToServer(emails, authtoken);
                Log.w("SERVER SENT RESPONSE", json.toString());
                return json;
            }

            @Override
            protected void onPostExecute(JSONObject json) {

                try {

                    if(json != null){

                        JSONArray jArray = json.getJSONArray("available");

                        for(int i = 0; i<jArray.length(); i++){
                            contactList1.remove(contactList1Email.indexOf(jArray.get(i).toString()));
                            contactList1Email.remove(contactList1Email.indexOf(jArray.get(i).toString()));
                            Log.w("REMOVING", jArray.get(i).toString());
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                loadNewContacts(contactList1);
                nDialog.dismiss();

            }

        }.execute();

    }

    public void loadNewContacts(final ArrayList<String> contactList1) {

        runOnUiThread(new Runnable() {
            public void run() {
                emails = new String[contactList1.size()];
                for (int i = 0; i < contactList1.size(); i++)
                    emails[i] = contactList1.get(i);
                //adapter.notifyDataSetChanged();
                adapter = new ArrayAdapter<String>(getApplicationContext(),
                        android.R.layout.simple_list_item_multiple_choice, emails);
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                listView.setAdapter(adapter);

            }
        });
    }




}
