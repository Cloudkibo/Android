package com.cloudkibo.webrtc.call;

import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

    Button btnEndCall;

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

        btnEndCall = (Button) findViewById(R.id.endCall);

        btnEndCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                socketService.stopCallMessageToCallee();
                finish();

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

                    Log.e(type, body);
                    if(type.equals("Reject Call")){
                        //Toast.makeText(getApplicationContext(),
                        //        body +
                        //                " is busy", Toast.LENGTH_SHORT).show();
                        Log.e("OutGoingCall", "Reject call is sent");
                        finish();
                    }
                    else if(type.equals("othersideringing")){
                        /*dialog = new Dialog(MainActivity.this);
                        dialog.setContentView(R.layout.call_dialog);
                        dialog.setTitle(msg);

                        // set the custom dialog components - text, image and button
                        TextView text = (TextView) dialog.findViewById(R.id.textDialog);
                        text.setText(msg);
                        ImageView image = (ImageView) dialog.findViewById(R.id.imageDialog);
                        image.setImageResource(R.drawable.ic_launcher);

                        Button dialogButton = (Button) dialog.findViewById(R.id.declineButton);
                        // if button is clicked, close the custom dialog
                        dialogButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                socketService.stopCallMessageToCallee();

                                dialog.dismiss();
                            }
                        });

                        dialog.show();*/
                    }
                    else if(type.equals("calleeisoffline") || type.equals("calleeisbusy")){
                        finish();
                    }
                    else if(type.equals("Accept Call")){

                        try {

                            String roomId = Long.toHexString(Double.doubleToLongBits(Math.random()));

                            JSONObject msg = new JSONObject();
                            msg.put("type", "room_name");
                            msg.put("room", roomId);

                            socketService.sendSocketMessage(msg.toString(), peerName);

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



                        } catch(JSONException e){
                            e.printStackTrace();
                        }

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
