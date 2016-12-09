package com.cloudkibo.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.database.CloudKiboDatabaseContract;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.library.GroupUtility;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.utils.IFragmentName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class GroupMessageInfo extends CustomFragment implements IFragmentName {
    private String authtoken;
    Context context;
    String group_id;
    String message;
    String message_id;
    ListView lv;
    CharSequence [] contactList;
    String [] phoneList;
    JSONArray participants;
    JSONArray status;
    LayoutInflater inflater;
    ImageButton btnSelectIcon;

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.group_message_info, null);
        this.inflater = inflater;
        authtoken = getActivity().getIntent().getExtras().getString("authtoken");
        Bundle args = getArguments();
        if (args  != null){
            authtoken = args.getString("authtoken");
            group_id = args.getString("group_id");
            message_id = args.getString("message_id");
            message = args.getString("message");
        }
//        String names[] = getMembers();
//        Toast.makeText(getContext(), getMembers().length, Toast.LENGTH_LONG).show();
        //      Toast.makeText(getContext(), getMembers().toString(), Toast.LENGTH_LONG).show();
        setGroupInfo(v);
        lv=(ListView) v.findViewById(R.id.listView);
        participants = getMembers();
        status = getStatus();
        lv.setAdapter(new GroupMIAdapter(inflater, participants, status, getContext(),group_id, message_id, message));


        return v;

    }


    /* (non-Javadoc)
     * @see com.socialshare.custom.CustomFragment#onClick(android.view.View)
     */


    @Override
    public void onClick(View v)
    {
        super.onClick(v);
    }


    public JSONArray getMembers(){

        Bundle args = getArguments();
        if (args  != null){
            group_id = args.getString("group_id");
        }

        String names[];
        DatabaseHandler db = new DatabaseHandler(getContext());

        try {
            participants = new JSONArray();
            participants = db.getGroupMembers(group_id);
//           participants.put(db.getMyDetailsInGroup(group_id));

//           Toast.makeText(getContext(), "Custom Members "+participants.toString(), Toast.LENGTH_LONG).show();
            names = new String[participants.length()];
            for(int i = 0; i < participants.length(); i++)
            {
                if(!participants.getJSONObject(i).has("display_name")){
                    names[i] = "Anonymous";
                }else {
                    names[i] = participants.getJSONObject(i).getString("display_name");
                }
                if(participants.getJSONObject(i).getString("phone").toString().equals(db.getUserDetails().get("phone"))){
                    names[i] = db.getUserDetails().get("display_name");
                }
            }
            if(lv != null){
                GroupMIAdapter groupMIAdapter = new GroupMIAdapter(inflater, participants, status,getContext(), group_id, message_id, message);
                lv.setAdapter(groupMIAdapter);
                groupMIAdapter.notifyDataSetChanged();
//               Toast.makeText(getContext(), "List Updated", Toast.LENGTH_LONG).show();
            }
            return  participants;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }

    public JSONArray getStatus(){

        Bundle args = getArguments();
        if (args  != null){
            message_id = args.getString("message_id");
        }

        DatabaseHandler db = new DatabaseHandler(getContext());

        try {
            status = new JSONArray();
            status = db.getGroupMessageStatus(group_id);
//           participants.put(db.getMyDetailsInGroup(group_id));

//           Toast.makeText(getContext(), "Custom Members "+participants.toString(), Toast.LENGTH_LONG).show();

            if(lv != null){
                GroupMIAdapter groupMIAdapter = new GroupMIAdapter(inflater, participants, status, getContext(), group_id, message_id, message);
                lv.setAdapter(groupMIAdapter);
                groupMIAdapter.notifyDataSetChanged();
//               Toast.makeText(getContext(), "List Updated", Toast.LENGTH_LONG).show();
            }
            return  participants;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }


    public void setGroupInfo(View v){
        Bundle args = getArguments();
        if (args  != null){
            group_id = args.getString("group_id");
        }
        // Toast.makeText(getContext(), "Group In function", Toast.LENGTH_LONG).show();

        DatabaseHandler db = new DatabaseHandler(getContext());
        try {
            JSONObject group_info = db.getGroupInfo(group_id);
//
            ((TextView)v.findViewById(R.id.lbl2)).setText(message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public CharSequence[] getAddtionalMembers(){


        DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());

        try {

            JSONArray jsonA = db.getMembersNotInGroup(group_id);

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




    public String getFragmentName()
    {
        return "GroupMessageInfo";
    }

    public String getFragmentContactPhone()
    {
        return "About Chat";
    }
}
