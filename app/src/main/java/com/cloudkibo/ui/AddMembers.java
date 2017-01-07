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
    AddMembers temp = this;
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
        Button create_button = (Button) v.findViewById(R.id.create_group);
//        gv=(GridView) v.findViewById(R.id.gridView1);
//        FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fab);
//        fab.bringToFront();
        final EditText group_name = (EditText) v.findViewById(R.id.group_name);
//        contactAdapter = new CustomContactAdapter(inflater, selected_contacts, getContext());
//        gv.setAdapter(contactAdapter);
//        final AddMembers temp = this;
//        context = getContext();
//        Button add_contacts = (Button) v.findViewById(R.id.add_contacts);
//        Bundle args = getArguments();
//
//        if (args  != null){
//            group_id = args.getString("group_id");
//            group_name = args.getString("group_name");
//            Toast.makeText(getContext(), group_id, Toast.LENGTH_LONG).show();
//        }

        create_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                group_id = randomString(10);
                Toast.makeText(getContext(), "Group Name: " + group_name.getText().toString(), Toast.LENGTH_LONG).show();
                db.createGroup(group_id, group_name.getText().toString(), 0);

                String message = "You created group "+ group_name.getText().toString();
                String member_name = db.getUserDetails().get("display_name");
                String member_phone = db.getUserDetails().get("phone");
                String uniqueid = Long.toHexString(Double.doubleToLongBits(Math.random()));
                uniqueid += (new Date().getYear()) + "" + (new Date().getMonth()) + "" + (new Date().getDay());
                uniqueid += (new Date().getHours()) + "" + (new Date().getMinutes()) + "" + (new Date().getSeconds());
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                db.addGroupMessage(group_id,message,member_phone,member_name,uniqueid, "log");

//                addMembers(selected_contacts);
                CreateGroup nextFrag= new CreateGroup();
                Bundle args = new Bundle();
                args.putString("group_id", group_id);
                args.putString("group_name", group_name.getText().toString());
                nextFrag.setArguments(args);
                temp.getFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, nextFrag,null)
                        .addToBackStack(null)
                        .commit();
            }
        });

//                try {
//                  Toast.makeText(getContext(), "Total Members are: " + db.getGroupMembers(group_id).length(), Toast.LENGTH_LONG).show();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//                createGroupOnServer(group_name.getText().toString(), group_id,authtoken);
//                GroupChatUI nextFrag= new GroupChatUI();
//                Bundle args = new Bundle();
//                args.putString("group_id", group_id);
//                nextFrag.setArguments(args);
//                temp.getFragmentManager().beginTransaction()
//                        .replace(R.id.content_frame, nextFrag,null)
//                        .addToBackStack(null)
//                        .commit();
//            }
//        });
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.findItem(R.id.archived).setVisible(false);
            menu.findItem(R.id.language).setVisible(false);
            menu.findItem(R.id.backup_setting).setVisible(false);
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

            jsonA = UserFunctions.sortJSONArrayIgnoreCase(jsonA, "display_name");

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
            jsonA = UserFunctions.sortJSONArrayIgnoreCase(jsonA, "display_name");
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
