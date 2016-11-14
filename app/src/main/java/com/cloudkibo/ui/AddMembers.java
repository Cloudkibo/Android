package com.cloudkibo.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
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
    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.add_members, null);
        authtoken = getActivity().getIntent().getExtras().getString("authtoken");
        db = new DatabaseHandler(getContext());
        gv=(GridView) v.findViewById(R.id.gridView1);
        final CustomContactAdapter contactAdapter = new CustomContactAdapter(inflater, getContacts(), getPhone());
        gv.setAdapter(contactAdapter);
        final AddMembers temp = this;
        Button add_contacts = (Button) v.findViewById(R.id.add_contacts);
        Bundle args = getArguments();

        if (args  != null){
            group_id = args.getString("group_id");
            group_name = args.getString("group_name");
            Toast.makeText(getContext(), group_id, Toast.LENGTH_LONG).show();
        }

        add_contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMembers(contactAdapter.getSelected_contacts());
                createGroupOnServer(group_name, group_id,authtoken);
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

    /* (non-Javadoc)
     * @see com.socialshare.custom.CustomFragment#onClick(android.view.View)
     */

    public void createGroupOnServer(final String group_name, final String group_id, final String authtoken){



        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
                UserFunctions userFunctions = new UserFunctions();
                return userFunctions.sendCreateGroupToServer(getGroupCreationData(group_name, group_id), authtoken);
            }

            @Override
            protected void onPostExecute(JSONObject row) {
                if(row != null){
                    Toast.makeText(getContext(), row.toString(), Toast.LENGTH_LONG).show();
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
            body.put("members",  new JSONArray(Arrays.asList(getPhone())));
            groupPost.put("body", body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return body;
    }

    public void addMembers(ArrayList<String> phones){
        for (int i = 0; i< phones.size(); i++){
            db.addGroupMember(group_id,phones.get(i),0,"joined");
        }
        db.addGroupMember(group_id,db.getUserDetails().get("phone"),1,"joined");
    }

    public String getFragmentName()
    {
        return "Add Group Members";
    }

    public String getFragmentContactPhone()
    {
        return "About Chat";
    }
}
