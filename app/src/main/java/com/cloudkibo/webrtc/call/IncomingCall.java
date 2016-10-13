package com.cloudkibo.webrtc.call;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudkibo.R;
import com.cloudkibo.SplashScreen;
import com.cloudkibo.custom.CustomActivity;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.socket.BoundServiceListener;
import com.cloudkibo.socket.SocketService;
import com.cloudkibo.socket.SocketService.SocketBinder;
import com.cloudkibo.ui.CallHistory;

public class IncomingCall extends CustomActivity {

    String peerName;
    Boolean initiator;
    String contactName;

    SocketService socketService;
    boolean isBound = false;

    private HashMap<String, String> user;

    ImageButton btnAcceptCall;
    ImageButton btnRejectCall;

    TextView contactNameView;

    Ringtone r;

    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.incoming_call);

        user = (HashMap) getIntent().getExtras().get("user");
        String room = getIntent().getExtras().getString("room");
        peerName = getIntent().getExtras().getString("contact");
        contactName = getIntent().getExtras().getString("contact_name");

        Intent i = new Intent(this, SocketService.class);
        i.putExtra("user", user);
        i.putExtra("room", room);
        startService(i);
        bindService(i, socketConnection, Context.BIND_AUTO_CREATE);

        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }

        btnAcceptCall = (ImageButton) findViewById(R.id.pickCall);
        btnRejectCall = (ImageButton) findViewById(R.id.rejectCall);

        contactNameView = (TextView) findViewById(R.id.nameOfCallerTextView);
        contactNameView.setText(contactName);

        btnRejectCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                socketService.rejectCallMessageToCallee();
                r.stop();
                DatabaseHandler db = new DatabaseHandler(getApplicationContext());

                db.addCallHistory("received", peerName);
                finish();
            }
        });

        btnAcceptCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                socketService.acceptCallMessageToCallee();
                r.stop();
                DatabaseHandler db = new DatabaseHandler(getApplicationContext());

                db.addCallHistory("received", peerName);

                /*
                Intent i = new Intent(getApplicationContext(), VideoCallView.class);
                i.putExtra("username", user.get("phone"));
                i.putExtra("_id", user.get("_id"));
                i.putExtra("peer", peerName);
                i.putExtra("lastmessage", "AcceptCallFromMe");
                i.putExtra("room", room);
                startActivity(i);

                finish();
                */
            }
        });

    }

    protected void onDestroy() {

        if(isBound){
            unbindService(socketConnection);
        }

        super.onDestroy();
    }

    private ServiceConnection socketConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SocketBinder binder = (SocketBinder) service;
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
                public void receiveSocketJson(String type, JSONObject body) {

                    try{

                        if (type.equals("call")) {
                            String status = body.getString("status");
                            if(status.equals("missing")){
                                Intent intent = new Intent(getApplicationContext(), CallHistory.class);
                                PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                                r.stop();

                                Notification n = new Notification.Builder(getApplicationContext())
                                        .setContentTitle(body.getString("callerphone"))
                                        .setContentText("Missed Call")
                                        .setSmallIcon(R.drawable.icon)
                                        .setContentIntent(pIntent)
                                        .setAutoCancel(true)
                                        .build();

                                NotificationManager notificationManager =
                                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                                notificationManager.notify(0, n);

                                DatabaseHandler db = new DatabaseHandler(getApplicationContext());

                                db.addCallHistory("missed", peerName);

                                finish();
                            }
                        } else if(type.equals("room_name")){
                            Intent i = new Intent(getApplicationContext(), com.cloudkibo.webrtc.conference.VideoCallView.class);
                            //i.putExtra("username", user.get("phone"));
                            //i.putExtra("_id", user.get("_id"));
                            //i.putExtra("peer", peerName);
                            //i.putExtra("lastmessage", "AcceptCallFromOther");
                            user.put("username", user.get("display_name"));
                            i.putExtra("user", user);
                            i.putExtra("room", body.getString("room_name"));
                            startActivity(i);

                            finish();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    };
}
