package com.cloudkibo.webrtc.call;


import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.DataChannel.Buffer;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.PeerConnection.IceConnectionState;
import org.webrtc.PeerConnection.IceGatheringState;
import org.webrtc.PeerConnection.SignalingState;
import org.webrtc.VideoRendererGui;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomActivity;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.socket.BoundServiceListener;
import com.cloudkibo.socket.SocketService;
import com.cloudkibo.socket.SocketService.SocketBinder;
import com.cloudkibo.utils.IFragmentName;

public class OutgoingCall extends CustomActivity {

    String peerName;
    Boolean initiator;

    SocketService socketService;
    boolean isBound = false;

    private HashMap<String, String> user;
    private String room;

    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.outgoing_call);

        user = (HashMap) getIntent().getExtras().get("user");
        room = getIntent().getExtras().getString("room");
        peerName = getIntent().getExtras().getString("contact");

        Intent i = new Intent(this, SocketService.class);
        i.putExtra("user", user);
        i.putExtra("room", room);
        startService(i);
        bindService(i, socketConnection, Context.BIND_AUTO_CREATE);

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
            });
        }
    };
}
