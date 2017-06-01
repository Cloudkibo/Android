package com.cloudkibo.ui;


import android.app.ActionBar;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.library.Utility;
import com.cloudkibo.utils.IFragmentName;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.Date;
import java.util.HashMap;

public class DayStatusView extends CustomFragment implements IFragmentName{
    String authtoken;
    String contactPhone; //whose status we are viewing
    String statusID; //of status we are viewing
    Context ctx;
    private HashMap<String, String> user;
    LayoutInflater inflater;
    EditText replyMessage;
    TextView totalViewer;
    Button buttonDelete;
    Button send;
    LinearLayout deleteLayout;

    int down = 0;
    int up = 500000;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.daystatus_view, null);
        this.inflater = inflater;
        setHasOptionsMenu(true);
        ctx = getActivity().getApplicationContext();
        DatabaseHandler db = new DatabaseHandler(ctx);
        user = db.getUserDetails();
        authtoken = getActivity().getIntent().getExtras().getString("authtoken");
        Bundle args = getArguments();
        contactPhone = user.get("phone"); // only for testing purposes
        if (args  != null){
            // TODO: 5/27/17 get arguments from fragment it is called in
            contactPhone = args.getString("contactPhone");
            statusID = args.getString("statusID");
        }

        replyMessage = (EditText) v.findViewById(R.id.replyMsg);
        send = (Button) v.findViewById(R.id.btnSend);
        deleteLayout = (LinearLayout) v.findViewById(R.id.deleteLayout);
        totalViewer = (TextView) v.findViewById(R.id.totalViewer);
        buttonDelete = (Button) v.findViewById(R.id.btnDel);

        replyMessage.setVisibility(View.GONE);
        send.setVisibility(View.GONE);
        deleteLayout.setVisibility(View.GONE);


        totalViewer.setText(totalViewer.getText()+" 0");

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

                    //condition to check if own status is opened or of a contact's
                    if(contactPhone.equals(user.get("phone"))) {
                        deleteLayout.setVisibility(View.VISIBLE);


                    } else {
                        replyMessage.setVisibility(View.VISIBLE);
                        send.setVisibility(View.VISIBLE);
                        replyMessage.requestFocus();

                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getContext().INPUT_METHOD_SERVICE);
                        imm.showSoftInput(replyMessage, InputMethodManager.SHOW_IMPLICIT);
                    }

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
                //Call sendMessage() below here.
                InputMethodManager imm = (InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(replyMessage.getWindowToken(), 0);
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 6/1/17 Call db method to delete the status.
                Toast.makeText(getContext(), "Delete button clicked", Toast.LENGTH_SHORT).show();
//                DatabaseHandler db = new DatabaseHandler(ctx);
//                db.deleteDaystatus(statusID);
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
        if(contactPhone.equals(user.get("phone")))
            inflater.inflate(R.menu.newchat, menu);  // Use filter.xml from step 1
        else
            inflater.inflate(R.menu.daystatus, menu);
        getActivity().getActionBar().setSubtitle(null);    // TODO: 5/30/17 add the person name, whose status is opened
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
    }

    private void sendMessage()
    {
        if (replyMessage.length() == 0)
            return;

        String uniqueid = Long.toHexString(Double.doubleToLongBits(Math.random()));
        uniqueid += (new Date().getYear()) + "" + (new Date().getMonth()) + "" + (new Date().getDay());
        uniqueid += (new Date().getHours()) + "" + (new Date().getMinutes()) + "" + (new Date().getSeconds());

        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(replyMessage.getWindowToken(), 0);

        String messageString = replyMessage.getText().toString();

        DatabaseHandler db = new DatabaseHandler(ctx);
        db.addChat(contactPhone, user.get("phone"), user.get("display_name"),
                messageString, Utility.getCurrentTimeInISO(), "pending", uniqueid, "day_status_chat", statusID);


        sendMessageUsingAPI(messageString, uniqueid, "day_status_chat", statusID);

        replyMessage.setText(null);
    }

    public void sendMessageUsingAPI(final String msg, final String uniqueid, final String type, final String file_type){
        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
                UserFunctions userFunction = new UserFunctions(ctx);
                JSONObject message = new JSONObject();

                try {
                    message.put("from", user.get("phone"));
                    message.put("to", contactPhone);
                    message.put("fromFullName", user.get("display_name"));
                    message.put("msg", msg);
                    message.put("date", Utility.getCurrentTimeInISO());
                    message.put("uniqueid", uniqueid);
                    message.put("type", type);
                    message.put("file_type", file_type);
                } catch (JSONException e){
                    e.printStackTrace();
                }

                return userFunction.sendChatMessageToServer(message, authtoken);

            }

            @Override
            protected void onPostExecute(JSONObject row) {
                try {

                    if (row != null) {
                        if(row.has("status")){
                            updateChatStatus(row.getString("status"), row.getString("uniqueid"));
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }.execute();
    }

    public void updateChatStatus(String status, String uniqueid){
        try {
            DatabaseHandler db = new DatabaseHandler(ctx);
            db.updateChat(status, uniqueid);
        } catch (NullPointerException e){
            e.printStackTrace();
        }
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
