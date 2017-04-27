package com.cloudkibo.ui;


import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.utils.IFragmentName;

import java.util.ArrayList;
import java.util.Date;

public class CreateBroadCastList_A extends CustomFragment implements IFragmentName {

    private String bList_id = "";
    private String authtoken = "";
    Context context;
    ArrayList<String> selected_contacts;
    CreateBroadCastList_A temp = this;
    private DatabaseHandler db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.createbroadcast_a, null);
        setHasOptionsMenu(true);
        authtoken = getActivity().getIntent().getExtras().getString("authtoken");
        db = new DatabaseHandler(getContext());
        Button create_button = (Button) v.findViewById(R.id.create_group);
        final EditText list_name = (EditText) v.findViewById(R.id.group_name);

        create_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(list_name.getText().toString().equals("")){
                    String tmp = list_name.getText().toString();
                    Toast.makeText(getContext(), "Please insert List name before proceeding", Toast.LENGTH_SHORT).show();
                }
                else {
                    bList_id = randomString(10);

                    String uniqueid = Long.toHexString(Double.doubleToLongBits(Math.random()));
                    uniqueid += (new Date().getYear()) + "" + (new Date().getMonth()) + "" + (new Date().getDay());
                    uniqueid += (new Date().getHours()) + "" + (new Date().getMinutes()) + "" + (new Date().getSeconds());

                    CreateBroadCastList_B nextFrag = new CreateBroadCastList_B();
                    Bundle args = new Bundle();
                    args.putString("bList_id", bList_id);
                    args.putString("list_name", list_name.getText().toString());
                    args.putString("unique_id", uniqueid);
                    nextFrag.setArguments(args);
                    temp.getFragmentManager().beginTransaction()
                            .replace(R.id.content_frame, nextFrag, null)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

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
        }
        inflater.inflate(R.menu.newchat, menu);  // Use filter.xml from step 1
        getActivity().getActionBar().setSubtitle(null);
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.archived){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v)
    {
        super.onClick(v);
    }




    String randomString(final int length) {
        String uniqueid = Long.toHexString(Double.doubleToLongBits(Math.random()));
        uniqueid += (new Date().getYear()) + "" + (new Date().getMonth()) + "" + (new Date().getDay());
        uniqueid += (new Date().getHours()) + "" + (new Date().getMinutes()) + "" + (new Date().getSeconds());
        return uniqueid;
    }

    @Override
    public String getFragmentName() {
        return "Add BroadCast list members";
    }

    @Override
    public String getFragmentContactPhone() {
        return "About Chat";
    }
}
