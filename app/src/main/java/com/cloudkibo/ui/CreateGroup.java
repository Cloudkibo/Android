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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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

import java.text.ParseException;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */


public class CreateGroup extends CustomFragment implements IFragmentName
{


    private String authtoken;

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.create_group, null);

        authtoken = getActivity().getIntent().getExtras().getString("authtoken");

        Button create_group = (Button) v.findViewById(R.id.create_group);
        final CreateGroup temp = this;
        create_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddMembers nextFrag= new AddMembers();
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
}
