package com.cloudkibo.ui;


import android.app.ActionBar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.utils.IFragmentName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BroadcastMessageInfo extends CustomFragment implements IFragmentName {

    String bList_id;
    String list_name;
    String message_id;
    String message;
    JSONArray participants;
    ListView lv;
    private String authtoken;
    LayoutInflater inflater;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.broadcast_message_info, null);
        this.inflater = inflater;
        authtoken = getActivity().getIntent().getExtras().getString("authtoken");
        Bundle args = getArguments();
        if (args  != null){
            authtoken = args.getString("authtoken");
            bList_id= args.getString("bList_id");
            message = args.getString("message");
            message_id = args.getString("message_id");
            list_name = args.getString("list_name");
        }
//        String names[] = getMembers();
//        Toast.makeText(getContext(), getMembers().length, Toast.LENGTH_LONG).show();
        //      Toast.makeText(getContext(), getMembers().toString(), Toast.LENGTH_LONG).show();
        setGroupInfo(v);
        lv=(ListView) v.findViewById(R.id.listView);
        participants = getMembers();
        //lv.setAdapter(new GroupMIAdapter(inflater, participants, getContext(),bList_id, message));


        return v;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.findItem(R.id.archived).setVisible(false);
            menu.findItem(R.id.search_chats).setVisible(false);
            menu.findItem(R.id.settings).setVisible(false);
            menu.findItem(R.id.connect_to_desktop).setVisible(false);
            menu.findItem(R.id.broadcast).setVisible(false);
            menu.findItem(R.id.listNameChange).setVisible(false);
        }
        inflater.inflate(R.menu.broadcastsetting, menu);
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setTitle(list_name.toUpperCase());
        actionBar.setDisplayShowCustomEnabled(false); // Use filter.xml from step 1
    }

    public JSONArray getMembers(){

        String names[];
        DatabaseHandler db = new DatabaseHandler(getContext());

        try {
            participants = new JSONArray();
            participants = db.getBroadCastListMembers(bList_id);
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
                BroadcastMIAdapter customParticipantAdapter = new BroadcastMIAdapter(inflater, participants,getContext(), bList_id, message_id, message);
                lv.setAdapter(customParticipantAdapter);
                customParticipantAdapter.notifyDataSetChanged();
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
            bList_id = args.getString("bList_id");
        }
        // Toast.makeText(getContext(), "Group In function", Toast.LENGTH_LONG).show();

        DatabaseHandler db = new DatabaseHandler(getContext());

//
        ((TextView)v.findViewById(R.id.lbl2)).setText(message);
    }


    @Override
    public String getFragmentName() {
        return "BroadcastMessageInfo";
    }

    @Override
    public String getFragmentContactPhone() {
        return "About Chat";
    }
}
