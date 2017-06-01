package com.cloudkibo.ui;


import android.app.ActionBar;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.utils.IFragmentName;

public class DayStatusView extends CustomFragment implements IFragmentName{
    String authtoken;
    LayoutInflater inflater;
    EditText replyMessage;
    Button send;

    int down = 0;
    int up = 500000;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.daystatus_view, null);
        this.inflater = inflater;
        setHasOptionsMenu(true);
        authtoken = getActivity().getIntent().getExtras().getString("authtoken");
        Bundle args = getArguments();
        if (args  != null){
            // TODO: 5/27/17 get arguments from fragment it is called in
        }

        replyMessage = (EditText) v.findViewById(R.id.replyMsg);
        send = (Button) v.findViewById(R.id.btnSend);
        replyMessage.setVisibility(View.GONE);
        send.setVisibility(View.GONE);

        //To enable swipe motion
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    down = (int) motionEvent.getY();
                }
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    up = (int) motionEvent.getY();
                }

                if(down > up) {
                    Toast.makeText(getContext(), "swipe up" + down + " " + up, Toast.LENGTH_SHORT).show();

                    replyMessage.setVisibility(View.VISIBLE);
                    send.setVisibility(View.VISIBLE);
                    replyMessage.requestFocus();

                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getContext().INPUT_METHOD_SERVICE);
                    imm.showSoftInput(replyMessage, InputMethodManager.SHOW_IMPLICIT);

                    down = 0;
                    up = 500000;
                }


                return true;
            }
        });
        
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 5/30/17 Add logic to send reply
                Toast.makeText(getContext(), "reply status clicked", Toast.LENGTH_SHORT).show();
            }
        });



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
        inflater.inflate(R.menu.daystatus, menu);  // Use filter.xml from step 1
        getActivity().getActionBar().setSubtitle(null);    // TODO: 5/30/17 add the person name, whose status is opened
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
    }

    @Override
    public String getFragmentName() {
        return "Day Status View";
    }

    @Override
    public String getFragmentContactPhone() {
        return "About Chat";
    }
}
