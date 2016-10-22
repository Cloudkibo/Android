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
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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


public class GroupChatUI extends CustomFragment implements IFragmentName
{


    private String authtoken;
    private ListView lv;
    private ArrayList<String> messages = new ArrayList<String>();
    private ArrayList<String> names = new ArrayList<String>();

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.group_chat_dayem, null);

        authtoken = getActivity().getIntent().getExtras().getString("authtoken");

        final GroupChatUI temp = this;
        Button settings = (Button) v.findViewById(R.id.setting);
        final EditText my_message = (EditText) v.findViewById(R.id.my_message);


        lv=(ListView) v.findViewById(R.id.listView);

        lv.setAdapter(new GroupChatAdapter(inflater, messages,names));

        ImageView send_button = (ImageView) v.findViewById(R.id.send_button);
        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = my_message.getText().toString();
                if(message.trim().equals("")){
                    return;
                }else{
                    messages.add(message);
                    names.add("");
                    lv.setAdapter(new GroupChatAdapter(inflater, messages,names));
                    my_message.setText("");
                }

            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GroupSetting nextFrag= new GroupSetting();
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


    @Override
    public void onClick(View v)
    {
        super.onClick(v);
    }




    public String getFragmentName()
    {
        return "Group Chat";
    }

    public String getFragmentContactPhone()
    {
        return "Group Chat";
    }
}
