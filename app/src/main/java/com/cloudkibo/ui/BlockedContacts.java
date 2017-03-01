package com.cloudkibo.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cloudkibo.R;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.library.CircleTransform;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.library.Utility;
import com.cloudkibo.model.ContactItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by sojharo on 27/02/2017.
 */

public class BlockedContacts extends AppCompatActivity {

    private ArrayList<ContactItem> contactsItemsList;

    private BlockedContactsAdapter contactsAdapter;

    private String authtoken;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.blocked_contacts);

        authtoken = getIntent().getExtras().getString("token");

        loadBlockedContacts();

    }

    private void loadBlockedContacts() {
        loadBlockedContactsData();
        ListView settings_items = (ListView) findViewById(R.id.list);
        contactsAdapter = new BlockedContactsAdapter(getApplicationContext());
        settings_items.setAdapter(contactsAdapter);
        settings_items.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
                                    long arg3) {
                unBlockContact(pos);
            }
        });
        contactsAdapter.notifyDataSetChanged();
    }

    private void loadBlockedContactsData()
    {
        contactsItemsList = new ArrayList<ContactItem>();
        final DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        new AsyncTask<String, String, ArrayList<ContactItem>>() {

            @Override
            protected ArrayList<ContactItem> doInBackground(String... args) {

                try {
                    JSONArray jsonA = db.getContactsBlockedByMe();
                    ArrayList<ContactItem> contactList1 = new ArrayList<ContactItem>();

                    jsonA = UserFunctions.sortJSONArrayIgnoreCase(jsonA, "display_name");
                    String my_btmp;

                    for (int i=0; i < jsonA.length(); i++) {
                        JSONObject row = jsonA.getJSONObject(i);
                        my_btmp = row.optString("image_uri");
                        contactList1.add(new ContactItem(row.getString("_id"),
                                row.getString("display_name"), "", row.getString("on_cloudkibo"),
                                row.getString("phone"), 01, false, "", "", "", false
                        ).setProfile(my_btmp));
                    }
                    return contactList1;
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(ArrayList<ContactItem> contactList1) {
                if(contactList1 != null) {
                    contactsItemsList.clear();
                    contactsItemsList.addAll(contactList1);
                    if(contactsAdapter != null)
                        contactsAdapter.notifyDataSetChanged();
                }
            }

        }.execute();
    }

    private void unBlockContact(final int pos)
    {
        new AlertDialog.Builder(this)
                .setTitle("Unblock "+ contactsItemsList.get(pos).getUserName())
                .setMessage("Do you really want to unblock "+ contactsItemsList.get(pos).getUserName() +"?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {
                            JSONObject data = new JSONObject();
                            data.put("phone", contactsItemsList.get(pos).getPhone());
                            Utility.unBlockContact(getApplicationContext(), data, authtoken);
                            loadBlockedContactsData();
                        } catch (JSONException e) { e.printStackTrace(); }
                    }})
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private class BlockedContactsAdapter extends BaseAdapter
    {

        Context context;

        public BlockedContactsAdapter(Context appContext){
            context = appContext;
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getCount()
         */
        @Override
        public int getCount()
        {
            return contactsItemsList.size();
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getItem(int)
         */
        @Override
        public ContactItem getItem(int arg0)
        {
            return contactsItemsList.get(arg0);
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getItemId(int)
         */
        @Override
        public long getItemId(int position)
        {
            return position;
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
                convertView = LayoutInflater.from(this.context).inflate(
                        R.layout.settings_item, null);
            TextView lbl = (TextView) convertView.findViewById(R.id.lbl);
            lbl.setText(getItem(position).getUserName());

            ImageView img = (ImageView) convertView.findViewById(R.id.img);
            if (getItem(position).getProfileimg() != null) {
                Glide
                        .with(getApplicationContext())
                        .load(getItem(position).getProfileimg())
                        .thumbnail(0.1f)
                        .centerCrop()
                        .transform(new CircleTransform(getApplicationContext()))
                        .placeholder(R.drawable.avatar)
                        .into(img);

            }else{
                img.setImageResource(R.drawable.avatar);
            }
            return convertView;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


}
