package com.cloudkibo.webrtc.call;

import android.app.Activity;
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
import com.cloudkibo.socket.BoundServiceListener;
import com.cloudkibo.socket.SocketService;
import com.cloudkibo.webrtc.filesharing.FileTransferService;
import com.cloudkibo.webrtc.filesharing.RTCConfig;
import com.cloudkibo.webrtc.filesharing.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoRendererGui;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sojharo on 8/27/2015.
 */
public class VideoCall extends Activity {

    PeerConnectionFactory factory;
    FilePeer peer;

    String peerName;
    String filePath;
    Boolean initiator;

    String fileNameToSave;
    int numberOfChunksInFileToSave;
    int numberOfChunksReceived;
    int sizeOfFileToSave;
    int chunkNumberToRequest;

    SocketService socketService;
    boolean isBound = false;

    private HashMap<String, String> user;
    private String room;
    ArrayList<Byte> fileBytesArray = new ArrayList<Byte>();

    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.fileshare);

        user = (HashMap) getIntent().getExtras().get("user");
        room = getIntent().getExtras().getString("room");
        peerName = getIntent().getExtras().getString("contact");
        filePath = getIntent().getExtras().getString("filepath");
        initiator = getIntent().getExtras().getBoolean("initiator");

        Intent i = new Intent(this, SocketService.class);
        i.putExtra("user", user);
        i.putExtra("room", room);
        startService(i);
        bindService(i, socketConnection, Context.BIND_AUTO_CREATE);

        // For the File Transfer Service
        Intent intentFile = new Intent(this, FileTransferService.class);
        intentFile.putExtra("contact", peerName);
        intentFile.putExtra("filepath", filePath);
        intentFile.putExtra("initiator", initiator);
        startService(intentFile);

    }

    protected void onDestroy() {

        if(isBound){
            unbindService(socketConnection);
        }

        super.onDestroy();
    }

    public void createPeerConnectionFactory(){
        PeerConnectionFactory.initializeAndroidGlobals(getApplicationContext(), true, true,
                false, VideoRendererGui.getEGLContext());


        factory = new PeerConnectionFactory();
    }

    public void createOffer(){
        peer.pc.createOffer(peer, RTCConfig.getMediaConstraints());
    }

    public void createAnswer(JSONObject payload){
        try{

            SessionDescription sdp = new SessionDescription(
                    SessionDescription.Type.fromCanonicalForm(payload.getString("type")),
                    payload.getString("sdp")
            );
            peer.pc.setRemoteDescription(peer, sdp);
            peer.pc.createAnswer(peer, RTCConfig.getMediaConstraints());

        }catch(JSONException e){
            Toast.makeText(getApplicationContext(),
                    e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void setRemoteSDP(JSONObject payload){

        try{

            SessionDescription sdp = new SessionDescription(
                    SessionDescription.Type.fromCanonicalForm(payload.getString("type")),
                    payload.getString("sdp")
            );
            peer.pc.setRemoteDescription(peer, sdp);

        }catch(JSONException e){
            Toast.makeText(getApplicationContext(),
                    e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void addIceCandidate(JSONObject payload){

        try{

            if (peer.pc.getRemoteDescription() != null) {
                IceCandidate candidate = new IceCandidate(
                        payload.getString("id"),
                        payload.getInt("label"),
                        payload.getString("candidate")
                );
                peer.pc.addIceCandidate(candidate);
            }

        }catch(JSONException e){
            Toast.makeText(getApplicationContext(),
                    e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
        }

    }

    public void sendSocketMessageDataChannel(String msg){
        socketService.sendSocketMessageDataChannel(msg, peerName);
    }

    private class DcObserver implements DataChannel.Observer {

        public DcObserver(){

        }

        @Override
        public void onMessage(DataChannel.Buffer buffer) {

        }

        @Override
        public void onStateChange() {

            Log.e("FILE_ERROR", "DataChannel State Changed");

        }
    }


    private class PcObserver implements PeerConnection.Observer{

        public PcObserver(){

        }

        @Override
        public void onAddStream(MediaStream arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onDataChannel(final DataChannel dataChannel) {
            final DataChannel dc = dataChannel;
            runOnUiThread(new Runnable() {
                public void run() {
                    //peer.dc = dc;

                    DcObserver dcObserver = new DcObserver();

                    peer.dc.registerObserver(dcObserver);

                    //dc.registerObserver(dcObserver);
                }
            });


        }

        @Override
        public void onIceCandidate(final IceCandidate candidate) {
            try {

                JSONObject payload = new JSONObject();
                payload.put("type", "candidate");
                payload.put("label", candidate.sdpMLineIndex);
                payload.put("id", candidate.sdpMid);
                payload.put("candidate", candidate.sdp);

                sendSocketMessageDataChannel(payload.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {

        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onRemoveStream(MediaStream arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onRenegotiationNeeded() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSignalingChange(PeerConnection.SignalingState arg0) {
            // TODO Auto-generated method stub

        }

    }

    private class FilePeer implements SdpObserver {

        private PeerConnection pc;
        private DataChannel dc;

        public FilePeer() {

            PcObserver pcObserver = new PcObserver();

            pc = factory.createPeerConnection(RTCConfig.getIceServer(),
                    RTCConfig.getMediaConstraints(), pcObserver);

            dc = pc.createDataChannel("sendDataChannel", new DataChannel.Init());

            //DcObserver dcObserver = new DcObserver();

            //dc.registerObserver(dcObserver);
        }

        @Override
        public void onCreateFailure(String msg) {
            Toast.makeText(getApplicationContext(),
                    msg, Toast.LENGTH_SHORT)
                    .show();

        }

        @Override
        public void onCreateSuccess(SessionDescription sdp) {
            try {

                JSONObject payload = new JSONObject();
                payload.put("type", sdp.type.canonicalForm());
                payload.put("sdp", sdp.description);

                sendSocketMessageDataChannel(payload.toString());

                pc.setLocalDescription(FilePeer.this, sdp);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onSetFailure(String arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSetSuccess() {
            // TODO Auto-generated method stub

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

                    if(type.equals("msg")){

                        try {

                            JSONObject payload = body.getJSONObject(0);
                            String type2 = body.getJSONObject(0).getString("type");

                            Toast.makeText(getApplicationContext(),
                                    payload.toString(), Toast.LENGTH_SHORT)
                                    .show();

                            if(type2.equals("offer")){

                                createPeerConnectionFactory();

                                peer = new FilePeer();

                                SessionDescription sdp = new SessionDescription(
                                        SessionDescription.Type.fromCanonicalForm(payload.getString("type")),
                                        payload.getString("sdp")
                                );
                                peer.pc.setRemoteDescription(peer, sdp);
                                peer.pc.createAnswer(peer, RTCConfig.getMediaConstraints());
                            }
                            else if(type2.equals("answer")){
                                SessionDescription sdp = new SessionDescription(
                                        SessionDescription.Type.fromCanonicalForm(payload.getString("type")),
                                        payload.getString("sdp")
                                );
                                peer.pc.setRemoteDescription(peer, sdp);
                            }
                            else if(type2.equals("candidate")){
                                PeerConnection pc = peer.pc;
                                if (pc.getRemoteDescription() != null) {
                                    IceCandidate candidate = new IceCandidate(
                                            payload.getString("id"),
                                            payload.getInt("label"),
                                            payload.getString("candidate")
                                    );
                                    pc.addIceCandidate(candidate);
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e){
                            e.printStackTrace();

							/*
							 * todo This needs to be fixed. It does not receive offer and receives
							 * the candidate. This is a network error
							 */

                            Toast.makeText(getApplicationContext(),
                                    "Network error occurred. Try again after connecting to Internet", Toast.LENGTH_SHORT)
                                    .show();

                        }

                    }

                }
            });
        }
    };

}
