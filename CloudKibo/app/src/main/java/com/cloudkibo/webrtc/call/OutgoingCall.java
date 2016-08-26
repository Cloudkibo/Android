package com.cloudkibo.webrtc.call;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudkibo.R;
import com.cloudkibo.custom.CustomActivity;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.socket.BoundServiceListener;
import com.cloudkibo.socket.SocketService;
import com.cloudkibo.socket.SocketService.SocketBinder;


public class OutgoingCall extends CustomActivity {

    String peerName;
    Boolean initiator;
    String peerPhone;

    SocketService socketService;
    boolean isBound = false;

    private HashMap<String, String> user;
    private String room;

    ImageButton btnEndCall;
    TextView nameTextView;
    TextView statusTextView;

    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.outgoing_call);

        user = (HashMap) getIntent().getExtras().get("user");
        room = getIntent().getExtras().getString("room");
        peerName = getIntent().getExtras().getString("contact");
        peerPhone = getIntent().getExtras().getString("contact_name");

        DatabaseHandler db = new DatabaseHandler(getApplicationContext());

        db.addCallHistory("placed", peerName);

        Intent i = new Intent(this, SocketService.class);
        i.putExtra("user", user);
        i.putExtra("room", room);
        startService(i);
        bindService(i, socketConnection, Context.BIND_AUTO_CREATE);

        btnEndCall = (ImageButton) findViewById(R.id.endCall);

        btnEndCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                socketService.stopCallMessageToCallee();
                finish();
            }
        });

        nameTextView = (TextView) findViewById(R.id.nameOfCalleeTextView);
        nameTextView.setText(peerPhone);

        statusTextView = (TextView) findViewById(R.id.outGoingCallStatusTextView);

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

                    Log.e(type, body);
                    if (type.equals("NoAck")){
                        finish();
                    }

                }

                @Override
                public void receiveSocketArray(String type, JSONArray body) {



                }

                @Override
                public void receiveSocketJson(String type, JSONObject body) {

                    try{

                        if (type.equals("call")) {
                            String status = body.getString("status");
                            if(status.equals("calleeoffline")){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),
                                                "Other person is Offline.", Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                });
                                finish();
                            } else if(status.equals("calleeisbusy")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),
                                                "Other person is Busy.", Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                });
                            } else if(status.equals("calleeisavailable")){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),
                                                "Other side is ringing.", Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                });

                            } else if(status.equals("callrejected")){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),
                                                "Other person is Busy.", Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                });
                                Log.e("OutGoingCall", "Reject call is sent");
                                finish();
                            } else if(status.equals("callaccepted")){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),
                                                "Your call is answered. Connecting Now...", Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                });
                                String roomId = Long.toHexString(Double.doubleToLongBits(Math.random()));

                                socketService.sendRoomNameToCallee(roomId);

                                Intent i = new Intent(getApplicationContext(), com.cloudkibo.webrtc.conference.VideoCallView.class);
                                //i.putExtra("username", user.get("phone"));
                                //i.putExtra("_id", user.get("_id"));
                                //i.putExtra("peer", peerName);
                                //i.putExtra("lastmessage", "AcceptCallFromOther");
                                user.put("username", user.get("display_name"));
                                i.putExtra("user", user);
                                i.putExtra("room", roomId);
                                startActivity(i);

                                finish();
                            }

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
}
