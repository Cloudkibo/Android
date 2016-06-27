package com.cloudkibo.webrtc.call;

import java.util.HashMap;

import org.json.JSONArray;
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
import android.widget.Toast;

import com.cloudkibo.R;
import com.cloudkibo.SplashScreen;
import com.cloudkibo.custom.CustomActivity;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.socket.BoundServiceListener;
import com.cloudkibo.socket.SocketService;
import com.cloudkibo.socket.SocketService.SocketBinder;

public class IncomingCall extends CustomActivity {

    String peerName;
    Boolean initiator;

    SocketService socketService;
    boolean isBound = false;

    private HashMap<String, String> user;
    private String room;

    Button btnAcceptCall;
    Button btnRejectCall;

    Ringtone r;

    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.incoming_call);

        user = (HashMap) getIntent().getExtras().get("user");
        room = getIntent().getExtras().getString("room");
        peerName = getIntent().getExtras().getString("contact");

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

        btnAcceptCall = (Button) findViewById(R.id.pickCall);
        btnRejectCall = (Button) findViewById(R.id.rejectCall);

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
                    if(type.equals("Missed")){

                        Intent intent = new Intent(getApplicationContext(), SplashScreen.class);
                        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                        r.stop();

                        Notification n = new Notification.Builder(getApplicationContext())
                                .setContentTitle(body)
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
                        //dialog.dismiss();
                    }
                    else if(type.equals("got user media")){

                        finish();
/*
	  					Intent i = new Intent(getApplicationContext(), CordovaApp.class);
	  					i.putExtra("username", user.get("username"));
	  					i.putExtra("_id", user.get("_id"));
	  					i.putExtra("peer", msg);
	  					i.putExtra("lastmessage", "GotUserMedia");
	  					i.putExtra("room", room);
	  		            startActivity(i);
	  		            */
                    }
                    else if(type.equals("call_room")){
                        Intent i = new Intent(getApplicationContext(), com.cloudkibo.webrtc.conference.VideoCallView.class);
                        //i.putExtra("username", user.get("phone"));
                        //i.putExtra("_id", user.get("_id"));
                        //i.putExtra("peer", peerName);
                        //i.putExtra("lastmessage", "AcceptCallFromOther");
                        user.put("username", user.get("display_name"));
                        i.putExtra("user", user);
                        i.putExtra("room", body);
                        startActivity(i);

                        finish();
                    }

                }

                @Override
                public void receiveSocketArray(String type, JSONArray body) {



                }

                @Override
                public void receiveSocketJson(String type, JSONObject body) {

                }
            });
        }
    };
}
