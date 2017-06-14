package com.cloudkibo.ui;


import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.library.CircleTransform;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.library.Utility;
import com.cloudkibo.utils.IFragmentName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class DayStatusView extends CustomFragment implements IFragmentName{
    String authtoken;
    String contactPhone; //whose status we are viewing
    String statusID; //of status we are viewing
    Context ctx;
    DatabaseHandler db;
    JSONArray viewers;
    JSONArray statuses;
    private HashMap<String, String> user;
    LayoutInflater inflater;
    EditText replyMessage;
    TextView totalViewer;
    Button buttonDelete;
    ImageView statusImage;
    Button send;
    ListView lv;
    LinearLayout deleteLayout;
    ProgressBar pb;
    DayStatusView current;
    File imgFile;

    int down = 0;
    int up = 500000;
    int progressStatus;
    int statusNo;
    JSONObject currentStatus;
    boolean signalPB = true;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.daystatus_view, null);
        this.inflater = inflater;
        current = this;
        setHasOptionsMenu(true);
        ctx = getActivity().getApplicationContext();
        db = new DatabaseHandler(ctx);
        DatabaseHandler db = new DatabaseHandler(ctx);
        user = db.getUserDetails();
        authtoken = getActivity().getIntent().getExtras().getString("authtoken");
        Bundle args = getArguments();

        final Handler handler = new Handler();
        //contactPhone = user.get("phone"); // only for testing purposes
        if (args  != null){
            contactPhone = args.getString("contactPhone");
        }

        try {
            statuses = db.getSpecificContactDayStatus(contactPhone);
            int leng = statuses.length();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        replyMessage = (EditText) v.findViewById(R.id.replyMsg);
        statusImage = (ImageView) v.findViewById(R.id.statusImg);
        send = (Button) v.findViewById(R.id.btnSend);
        pb = (ProgressBar) v.findViewById(R.id.pb);
        lv = (ListView) v.findViewById(R.id.listView);
        deleteLayout = (LinearLayout) v.findViewById(R.id.deleteLayout);
        totalViewer = (TextView) v.findViewById(R.id.totalViewer);
        buttonDelete = (Button) v.findViewById(R.id.btnDel);

        replyMessage.setVisibility(View.GONE);
        send.setVisibility(View.GONE);
        deleteLayout.setVisibility(View.GONE);


        totalViewer.setText(totalViewer.getText()+" 0");
        progressStatus = 0;
        statusNo = 0;
        //lv.setAdapter(new CustomDayStatusAdapter(inflater, viewers, getContext(), statusID));

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(progressStatus < 100){
                    if(Thread.currentThread().isInterrupted()){
                        return;
                    }
                    // Update the progress status
                    if(signalPB) {
                        if(statuses!=null){
                            try {
                                currentStatus = statuses.getJSONObject(statusNo);

                                if(getActivity()!=null){
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                loadStatusImage(currentStatus);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        progressStatus += 1;
                    }

                    // Try to sleep the thread for 20 milliseconds
                    try{
                        Thread.sleep(30);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }

                    // Update the progress bar
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            pb.setProgress(progressStatus);
                        }
                    });

                    if(progressStatus == 100 ){
//
                        progressStatus = 0;
                        statusNo++;
                        if(getActivity() != null)
                            getActivity().invalidateOptionsMenu();
                        if(statusNo == statuses.length()){
                            DayStatus goback = new DayStatus();

                            if(getActivity() != null) {
                                getFragmentManager().beginTransaction()
                                        .replace(R.id.content_frame, goback, "dayStatusTag")
                                        .addToBackStack("Day Status")
                                        .commit();
                            }
                            Thread.currentThread().interrupt();
                        }
                    }
                }

            }
        }).start(); // Start the operation

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
                        signalPB = false;


                    } else {
                        replyMessage.setVisibility(View.VISIBLE);
                        send.setVisibility(View.VISIBLE);
                        replyMessage.requestFocus();

                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getContext().INPUT_METHOD_SERVICE);
                        imm.showSoftInput(replyMessage, InputMethodManager.SHOW_IMPLICIT);
                        signalPB = false;
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

                Toast.makeText(getContext(), "reply status clicked", Toast.LENGTH_SHORT).show();
                InputMethodManager imm = (InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(replyMessage.getWindowToken(), 0);
                signalPB = true;
                //sendMessage();
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(getContext(), "Delete button clicked", Toast.LENGTH_SHORT).show();
//                DatabaseHandler db = new DatabaseHandler(ctx);
//                db.deleteDaystatus(statusID);
            }
        });

        // TODO: 6/11/17 set viewers JSONArray by fetching from database 

        return v;

    }

    public void loadStatusImage(JSONObject status) throws JSONException {
//        imgFile = new File(status.getString("file_path"));
//
//        if(imgFile.exists()){
//            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
//            statusImage.setImageBitmap(myBitmap);
//        }

        Glide
                .with(MainActivity.mainActivity)
                .load(status.getString("file_path"))
                .thumbnail(0.1f)
                .centerCrop()
                .into(statusImage);
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
        getActivity().getActionBar().setTitle(contactPhone+"\t \t \t"+ (statusNo+1) + " / " + statuses.length());
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
