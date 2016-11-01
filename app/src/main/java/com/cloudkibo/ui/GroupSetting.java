package com.cloudkibo.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

import com.cloudkibo.NewChat;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomContactAdapter;
import com.cloudkibo.custom.CustomFragment;
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

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */


public class GroupSetting extends CustomFragment implements IFragmentName
{


    private String authtoken;
    Context context;
    String group_id;
    ListView lv;



    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.group_info, null);

        authtoken = getActivity().getIntent().getExtras().getString("authtoken");
//        String names[] = getMembers();
//        Toast.makeText(getContext(), getMembers().length, Toast.LENGTH_LONG).show();
  //      Toast.makeText(getContext(), getMembers().toString(), Toast.LENGTH_LONG).show();
        setGroupInfo(v);
        Bundle args = getArguments();
        if (args  != null){
            group_id = args.getString("group_id");
        }
        lv=(ListView) v.findViewById(R.id.listView);
        lv.setAdapter(new CustomParticipantAdapter(inflater, getMembers(), getContext(),group_id));


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
           JSONArray members = db.getGroupMembers(group_id);
           members.put(db.getMyDetailsInGroup(group_id));
          // Toast.makeText(getContext(), "Custom Members "+members.toString(), Toast.LENGTH_LONG).show();
           names = new String[members.length()];
           for(int i = 0; i < members.length(); i++)
           {
               names[i] = members.getJSONObject(i).getString("display_name");
           }
           return  members;
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
//            Toast.makeText(getContext(), "Group Name is: " + group_info.toString() + " and group id is: " + group_id, Toast.LENGTH_LONG).show();
            ((TextView)v.findViewById(R.id.group_name)).setText(group_info.getString("group_name"));
            ((TextView)v.findViewById(R.id.creation_date)).setText(group_info.getString("date_creation"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
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