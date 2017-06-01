package com.cloudkibo.ui;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.utils.IFragmentName;

public class DayStatus extends CustomFragment implements IFragmentName {
    String authtoken;
    ListView lv;
    LayoutInflater inflater;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.day_status_screen, null);
        this.inflater = inflater;
        setHasOptionsMenu(true);
        authtoken = getActivity().getIntent().getExtras().getString("authtoken");
        Bundle args = getArguments();
        if (args  != null){
            // TODO: 5/27/17 get arguments from fragment it is called in
        }

        FloatingActionButton createStatus = (FloatingActionButton) v.findViewById(R.id.fab_addStatus);
        createStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 5/27/17 add logic to create day status
                Toast.makeText(getContext(), "Action Button clicked", Toast.LENGTH_SHORT).show();
                DayStatusView demo = new DayStatusView();

                getFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, demo, "dayStatusViewTag")
                        .addToBackStack("View Status")
                        .commit();
            }
        });

        lv=(ListView) v.findViewById(R.id.listView);


        // TODO: 5/27/17 set Adapter for ListView here. 


        return v;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.findItem(R.id.archived).setVisible(false);
            menu.findItem(R.id.settings).setVisible(false);
            menu.findItem(R.id.connect_to_desktop).setVisible(false);
            menu.findItem(R.id.broadcast).setVisible(false);
        }
        menu.clear();
        inflater.inflate(R.menu.newchat, menu);  // Use filter.xml from step 1
        getActivity().getActionBar().setSubtitle(null);
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
    }

    // TODO: 5/27/17 Retrieve statuses from db and render on screen via  custom adapter 

    @Override
    public String getFragmentName() {
        return "Day Status";
    }

    @Override
    public String getFragmentContactPhone() {
        return "About Chat";
    }
}
