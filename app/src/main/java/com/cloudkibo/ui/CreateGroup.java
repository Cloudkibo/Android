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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudkibo.NewChat;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.library.Utility;
import com.cloudkibo.model.ChatItem;
import com.cloudkibo.utils.IFragmentName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */


public class CreateGroup extends CustomFragment implements IFragmentName
{


    private String authtoken;
    private DatabaseHandler db;

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.create_group, null);
        db = new DatabaseHandler(getContext());

        authtoken = getActivity().getIntent().getExtras().getString("authtoken");

        Button create_group = (Button) v.findViewById(R.id.create_group);
        final EditText group_name = (EditText) v.findViewById(R.id.group_name);
        final CreateGroup temp = this;
        create_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String group_id = randomString(10)+ DateFormat.getDateTimeInstance().format(new Date());
                Toast.makeText(getContext(), "Group Name: " + group_name.getText().toString(), Toast.LENGTH_LONG).show();
                db.createGroup(group_id, group_name.getText().toString(), 0);
                AddMembers nextFrag= new AddMembers();
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
        return "Create Group";
    }

    public String getFragmentContactPhone()
    {
        return "About Chat";
    }


    String randomString(final int length) {
        Random r = new Random(); // perhaps make it a class variable so you don't make a new one every time
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < length; i++) {
            char c = (char)(r.nextInt((int)(Character.MAX_VALUE)));
            sb.append(c);
        }
        return sb.toString();
    }
}
