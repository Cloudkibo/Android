package com.cloudkibo.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudkibo.MainActivity;
import com.cloudkibo.NewChat;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomContactAdapter;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.database.CloudKiboDatabaseContract;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.library.Utility;
import com.cloudkibo.model.ChatItem;
import com.cloudkibo.model.ContactItem;
import com.cloudkibo.utils.IFragmentName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */


public class AddMembers extends CustomFragment implements IFragmentName
{


    private String authtoken;
    GridView gv;
    Context context;
    ArrayList prgmName;
    private DatabaseHandler db;
    private String group_id = "";
    private String group_name = "";
    CustomContactAdapter contactAdapter;
    ArrayList<String> selected_contacts;
    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.add_members, null);
        setHasOptionsMenu(true);
        authtoken = getActivity().getIntent().getExtras().getString("authtoken");
        db = new DatabaseHandler(getContext());
        gv=(GridView) v.findViewById(R.id.gridView1);
        FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fab);
        fab.bringToFront();
        final EditText group_name = (EditText) v.findViewById(R.id.group_name);
        contactAdapter = new CustomContactAdapter(inflater, selected_contacts, getContext());
        gv.setAdapter(contactAdapter);
        final AddMembers temp = this;
        context = getContext();
//        Button add_contacts = (Button) v.findViewById(R.id.add_contacts);
//        Bundle args = getArguments();
//
//        if (args  != null){
//            group_id = args.getString("group_id");
//            group_name = args.getString("group_name");
//            Toast.makeText(getContext(), group_id, Toast.LENGTH_LONG).show();
//        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                group_id = randomString(10);
                Toast.makeText(getContext(), "Group Name: " + group_name.getText().toString(), Toast.LENGTH_LONG).show();
                db.createGroup(group_id, group_name.getText().toString(), 0);
                addMembers(selected_contacts);

//                try {
//                  Toast.makeText(getContext(), "Total Members are: " + db.getGroupMembers(group_id).length(), Toast.LENGTH_LONG).show();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }

                createGroupOnServer(group_name.getText().toString(), group_id,authtoken);
                GroupChatUI nextFrag= new GroupChatUI();
                Bundle args = new Bundle();
                args.putString("group_id", group_id);
                nextFrag.setArguments(args);
                temp.getFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, nextFrag,null)
                        .addToBackStack(null)
                        .commit();
            }
        });
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.findItem(R.id.archived).setVisible(false);
        }
        inflater.inflate(R.menu.newchat, menu);  // Use filter.xml from step 1
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.archived){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* (non-Javadoc)
     * @see com.socialshare.custom.CustomFragment#onClick(android.view.View)
     */

    public void createGroupOnServer(final String group_name, final String group_id, final String authtoken){



        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
                UserFunctions userFunctions = new UserFunctions();
                try {
                    DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
                    db.createGroupServerPending(group_id, group_name, getGroupCreationData(group_name, group_id).getJSONArray("members").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return userFunctions.sendCreateGroupToServer(getGroupCreationData(group_name, group_id), authtoken);
            }

            @Override
            protected void onPostExecute(JSONObject row) {

                if(row != null){
                    if(row.has("Error")){
                        Log.d("Add Members", "No Internet. Group information saved in pending groups table.");
                    } else {
                        try {
                            DatabaseHandler db = new DatabaseHandler(getContext());
                            db.deleteGroupServerPending(row.getString("unique_id"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(getContext(), row.toString(), Toast.LENGTH_LONG).show();
                        Toast.makeText(getContext(),"New Group Created Successfully", Toast.LENGTH_LONG).show();
                    }
//                    Toast.makeText(getContext(), "Group Successfully Created On Server", Toast.LENGTH_LONG).show();
                }
            }

        }.execute();

    }

    @Override
    public void onClick(View v)
    {
        super.onClick(v);
    }


    public String[] getContacts()
    {

        String [] contactList;
        String [] phoneList;

        DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());

        try {

            JSONArray jsonA = db.getContacts();

            jsonA = UserFunctions.sortJSONArray(jsonA, "display_name");

            contactList = new String[jsonA.length()];
            phoneList  = new String[jsonA.length()];

            //This loop adds contacts to the display list which are on cloudkibo
            for (int i=0; i < jsonA.length(); i++) {
                JSONObject row = jsonA.getJSONObject(i);
                contactList[i] = row.getString("display_name");
                phoneList[i] = row.getString(CloudKiboDatabaseContract.Contacts.CONTACT_PHONE);
            }
            return contactList;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String[] getPhone()
    {
        String [] phoneList;
        DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
        try {
            JSONArray jsonA = db.getContacts();
            jsonA = UserFunctions.sortJSONArray(jsonA, "display_name");
            phoneList  = new String[jsonA.length()];
            //This loop adds contacts to the display list which are on cloudkibo
            for (int i=0; i < jsonA.length(); i++) {
                JSONObject row = jsonA.getJSONObject(i);
                phoneList[i] = row.getString(CloudKiboDatabaseContract.Contacts.CONTACT_PHONE);
            }
            return phoneList;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject getGroupCreationData(String group_name, String group_id){
        JSONObject groupPost = new JSONObject();
        JSONObject body = new JSONObject();
        try {
            body.put("group_name", group_name);
            body.put("unique_id",  group_id);
            body.put("members",  new JSONArray(selected_contacts));
            groupPost.put("body", body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return body;
    }

    public void addMembers(ArrayList<String> phones){

        for (int i = 0; i< phones.size(); i++){
            db.addGroupMember(group_id,phones.get(i),0,"joined");
            Toast.makeText(context, phones.get(i) + "Added", Toast.LENGTH_SHORT).show();
        }
        db.addGroupMember(group_id,db.getUserDetails().get("phone"),1,"joined");
        Toast.makeText(context,db.getUserDetails().get("phone") + " added as admin", Toast.LENGTH_SHORT).show();
    }

    public String getFragmentName()
    {
        return "Add Group Members";
    }

    public String getFragmentContactPhone()
    {
        return "About Chat";
    }

    public void setSelectedContacts(ArrayList<String> selected_contacts){
        this.selected_contacts = selected_contacts;
    }

    String randomString(final int length) {
        String uniqueid = Long.toHexString(Double.doubleToLongBits(Math.random()));
        uniqueid += (new Date().getYear()) + "" + (new Date().getMonth()) + "" + (new Date().getDay());
        uniqueid += (new Date().getHours()) + "" + (new Date().getMinutes()) + "" + (new Date().getSeconds());
        return uniqueid;
    }
}
