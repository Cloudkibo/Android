package com.cloudkibo.webrtc.conference;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudkibo.R;
import com.cloudkibo.model.Conversation;
import com.cloudkibo.socket.BoundServiceListener;
import com.cloudkibo.socket.SocketService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by sojharo on 10/29/2015.
 */
public class GroupChat extends Activity {

    /** The Conversation list. */
    private ArrayList<Conversation> convList;

    /** The chat adapter. */
    private ChatAdapter adp;

    /** The Editext to compose the message. */
    private EditText txt;

    private Button sendButton;

    SocketService socketService;
    boolean isBound = false;

    private HashMap<String, String> user;
    private String room;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.group_chat);

        user = (HashMap) getIntent().getExtras().get("user");
        room = getIntent().getExtras().getString("room");

        Intent i = new Intent(this, SocketService.class);
        i.putExtra("user", user);
        i.putExtra("room", room);
        startService(i);
        bindService(i, socketConnection, Context.BIND_AUTO_CREATE);

        convList = new ArrayList<Conversation>();

        ListView list = (ListView) findViewById(R.id.list);
        adp = new ChatAdapter(GroupChat.this);
        list.setAdapter(adp);
        list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        list.setStackFromBottom(true);

        txt = (EditText) findViewById(R.id.txt);
        txt.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        sendButton = (Button) findViewById(R.id.btnSend);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void sendMessage()
    {
        if (txt.length() == 0)
            return;

        String messageString = txt.getText().toString();

        convList.add(new Conversation(messageString, new Date().toString(), true, true, "", "", "chat"));
        adp.notifyDataSetChanged();

        try {
            JSONObject payload = new JSONObject();
            payload.put("message", messageString);
            payload.put("username", user.get("username"));
            socketService.sendConferenceMsgGeneric("conference.chat", payload);
        }catch(JSONException e){
            e.printStackTrace();
        }

        txt.setText(null);
    }

    public void receiveMessage(String msg){

        convList.add(new Conversation(msg, new Date().toString(), false, true, "", "", "chat"));

        adp.notifyDataSetChanged();

    }

    private class ChatAdapter extends BaseAdapter
    {

        private Activity context;

        public ChatAdapter(Activity context) {
            this.context = context;
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getCount()
         */
        @Override
        public int getCount()
        {
            return convList.size();
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getItem(int)
         */
        @Override
        public Conversation getItem(int arg0)
        {
            return convList.get(arg0);
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getItemId(int)
         */
        @Override
        public long getItemId(int arg0)
        {
            return arg0;
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
         */
        @Override
        public View getView(int pos, View v, ViewGroup arg2)
        {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            Conversation c = getItem(pos);
            if (c.isSent())
                v = vi.inflate(
                        R.layout.chat_item_sent, null);
            else
                v = vi.inflate(
                        R.layout.chat_item_rcv, null);

            TextView lbl = (TextView) v.findViewById(R.id.lblContactDisplayName);
            lbl.setText(c.getDate());

            lbl = (TextView) v.findViewById(R.id.lbl2);
            lbl.setText(c.getMsg());

            lbl = (TextView) v.findViewById(R.id.lblContactPhone);
            if (c.isSuccess())
                lbl.setText("Delivered");
            else
                lbl.setText("");

            return v;
        }

    }

    private ServiceConnection socketConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SocketService.SocketBinder binder = (SocketService.SocketBinder) service;
            socketService = binder.getService();
            isBound = true;

            binder.setListener(new BoundServiceListener() {

                @Override
                public void receiveSocketMessage(String type, String body) {

                }

                @Override
                public void receiveSocketArray(String type, JSONArray body) {

                }

                @Override
                public void receiveSocketJson(String type, final JSONObject body) {
                    try {
                        if (type.equals("conference.chat")) {
                            if (!body.getString("username").equals(user.get("username")))
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            receiveMessage(body.getString("username") + ": " + body.getString("message"));
                                        }catch(JSONException e){
                                            e.printStackTrace();
                                        }
                                    }
                                });

                        }
                    }catch(JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    };

}
