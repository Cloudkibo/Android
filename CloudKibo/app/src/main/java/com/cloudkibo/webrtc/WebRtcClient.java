package com.cloudkibo.webrtc;

import java.util.HashMap;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;

import android.os.Handler;
import android.util.Log;

import com.cloudkibo.webrtc.interfaces.Command;
import com.cloudkibo.webrtc.interfaces.RTCListener;
import com.koushikdutta.async.http.socketio.Acknowledge;
import com.koushikdutta.async.http.socketio.ConnectCallback;
import com.koushikdutta.async.http.socketio.EventCallback;
import com.koushikdutta.async.http.socketio.SocketIOClient;

/**
 * Created by sojharo on 1/22/2015.
 */

class WebRtcClient {
    private final static int MAX_PEER = 2;
    private boolean[] endPoints = new boolean[MAX_PEER];
    private PeerConnectionFactory factory;
    private HashMap<String, Peer> peers = new HashMap<String, Peer>();
    private LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<PeerConnection.IceServer>();
    private MediaConstraints pcConstraints = new MediaConstraints();
    private MediaStream lMS;
    private RTCListener mListener;
    private SocketIOClient client;
    private final MessageHandler messageHandler = new MessageHandler();
    private final static String TAG = WebRtcClient.class.getCanonicalName();
    private final String room;
    private final String username;
    private String sendersocket;

    private class CreateOfferCommand implements Command {
        public void execute(String peerId, JSONObject payload) throws JSONException {
            Log.d(TAG,"CreateOfferCommand");
            Peer peer = peers.get(peerId);
            peer.pc.createOffer(peer, pcConstraints);
        }
    }

    private class CreateAnswerCommand implements Command{
        public void execute(String peerId, JSONObject payload) throws JSONException {
            Log.d(TAG,"CreateAnswerCommand");
            Peer peer = peers.get(peerId);
            String typeToSend = payload.getString("type");
            String sdpToSend = payload.getString("sdp");
            SessionDescription sdp = new SessionDescription(
                    SessionDescription.Type.fromCanonicalForm(typeToSend),
                    sdpToSend
            );
            peer.pc.setRemoteDescription(peer, sdp);
            peer.pc.createAnswer(peer, pcConstraints);
        }
    }

    private class SetRemoteSDPCommand implements Command{
        public void execute(String peerId, JSONObject payload) throws JSONException {
            Log.d(TAG,"SetRemoteSDPCommand");
            Peer peer = peers.get(peerId);
            SessionDescription sdp = new SessionDescription(
                    SessionDescription.Type.fromCanonicalForm(payload.getString("type")),
                    payload.getString("sdp")
            );
            peer.pc.setRemoteDescription(peer, sdp);
        }
    }

    private class AddIceCandidateCommand implements Command{
        public void execute(String peerId, JSONObject payload) throws JSONException {
            Log.d(TAG,"AddIceCandidateCommand");
            PeerConnection pc = peers.get(peerId).pc;
            if (pc.getRemoteDescription() != null) {
                IceCandidate candidate = new IceCandidate(
                        payload.getString("id"),
                        payload.getInt("label"),
                        payload.getString("candidate")
                );
                pc.addIceCandidate(candidate);
            }
        }
    }

    private class Peer implements SdpObserver, PeerConnection.Observer{
        private PeerConnection pc;
        private String id;
        private int endPoint;

        @Override
        public void onCreateSuccess(final SessionDescription sdp) {
            try {
                JSONObject payload = new JSONObject();
                payload.put("type", sdp.type.canonicalForm());
                payload.put("sdp", sdp.description);
                sendMessage(id, sdp.type.canonicalForm(), payload);
                pc.setLocalDescription(Peer.this, sdp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSetSuccess() {}

        @Override
        public void onCreateFailure(String s) {}

        @Override
        public void onSetFailure(String s) {}

        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {}

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            if(iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED) {
                removePeer(id);
                mListener.onStatusChanged("DISCONNECTED");
            }
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {}

        @Override
        public void onIceCandidate(final IceCandidate candidate) {
            try {
                JSONObject payload = new JSONObject();
                payload.put("label", candidate.sdpMLineIndex);
                payload.put("id", candidate.sdpMid);
                payload.put("candidate", candidate.sdp);
                sendMessage(id, "candidate", payload);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError() {}

        @Override
        public void onAddStream(MediaStream mediaStream) {
            Log.d(TAG,"onAddStream "+mediaStream.label());

            // remote streams are displayed from 1 to MAX_PEER (0 is localStream)
            mListener.onAddRemoteStream(mediaStream, endPoint+1);
        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {
            mListener.onRemoveRemoteStream(mediaStream, endPoint);

            removePeer(id);
        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {}

        public Peer(String id, int endPoint) {
            Log.d(TAG,"new Peer: "+id + " " + endPoint);
            this.pc = factory.createPeerConnection(iceServers, pcConstraints, this);
            this.id = id;
            this.endPoint = endPoint;

            pc.addStream(lMS, new MediaConstraints());

            mListener.onStatusChanged("CONNECTING");
        }
    }

    public WebRtcClient(RTCListener listener, String host, final String username, final String userid, final String room) {
        mListener = listener;
        factory = new PeerConnectionFactory();

        this.room = room;
        this.username = username;

        SocketIOClient.connect(host, new ConnectCallback() {

            @Override
            public void onConnectCompleted(Exception ex, SocketIOClient socket) {
                if (ex != null) {
                    Log.e(TAG,"WebRtcClient connect failed: "+ex.getMessage());
                    return;
                }
                Log.d(TAG,"WebRtcClient connected.");

                client = socket;

                JSONObject message = new JSONObject();

                try {

                    JSONObject userInfo = new JSONObject();
                    userInfo.put("username", username);
                    userInfo.put("_id", userid);


                    message.put("user", userInfo);
                    message.put("room", room);

                    socket.emit("join global chatroom", new JSONArray().put(message));


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // specify which events you are interested in receiving
                client.addListener("youareonline", messageHandler);
                client.addListener("areyoufreeforcall", messageHandler);
                client.addListener("message", messageHandler);
                client.addListener("created", messageHandler);
                client.addListener("joined", messageHandler);
            }
        }, new Handler());


        iceServers.add(new PeerConnection.IceServer("stun:23.21.150.121"));
        iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));

        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
    }

    private class MessageHandler implements EventCallback {
        private HashMap<String, Command> commandMap;

        public MessageHandler() {
            this.commandMap = new HashMap<String, Command>();
            commandMap.put("init", new CreateOfferCommand());
            commandMap.put("offer", new CreateAnswerCommand());
            commandMap.put("answer", new SetRemoteSDPCommand());
            commandMap.put("candidate", new AddIceCandidateCommand());
        }

        @Override
        public void onEvent(String s, JSONArray jsonArray, Acknowledge acknowledge) {
            try {
                Log.d(TAG,"MessageHandler.onEvent() "+ (s == null ? "nil" : s));
                if(s.equals("areyoufreeforcall")){

                    JSONObject message1 = new JSONObject();

                    message1.put("me", username);
                    message1.put("mycaller", jsonArray.getJSONObject(0).getString("caller"));

                    client.emit("yesiamfreeforcall", new JSONArray().put(message1));

                    sendersocket = jsonArray.getJSONObject(0).getString("sendersocket");

                    JSONObject message = new JSONObject();

                    message.put("msg", "Accept Call");
                    message.put("to", jsonArray.getJSONObject(0).getString("caller"));
                    message.put("room", room);
                    message.put("username", username);
                    client.emit("message", new JSONArray().put(message));


                } else if (s.equals("youareonline")) {


                    JSONObject message1 = new JSONObject();

                    message1.put("room", room);
                    message1.put("callee", "sabachanna");
                    message1.put("caller", username);

                    //client.emit("callthisperson", new JSONArray().put(message1));

                } else {

                    if(jsonArray.getString(0).equals("Accept Call"))
                        mListener.onCallReady("Initiator");
                    else if(jsonArray.getString(0).equals("got user media")) {

                        mListener.onCallReady("Not Initiator");

                        int endPoint = findEndPoint();
                        addPeer(sendersocket, endPoint);

                        commandMap.get("init").execute(sendersocket, new JSONObject());

                    }



                    JSONObject json = jsonArray.getJSONObject(0);

                    String from = json.getString("from");
                    String type = null;
                    try{
                        type = json.getString("type");
                    }catch(JSONException e){}


                    // if peer is unknown, try to add him
                    if(!peers.containsKey(from)) {
                        // if MAX_PEER is reach, ignore the call
                        int endPoint = findEndPoint();
                        if(endPoint != MAX_PEER) {
                            addPeer(from, endPoint);

                            commandMap.get(type).execute(from, json);
                        }
                    } else {
                        commandMap.get(type).execute(from, json);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String to, String type, JSONObject payload) throws JSONException {
        JSONObject message = new JSONObject();
        message.put("type", type);
        message.put("msg", payload);
        message.put("to", "sabachanna");
        message.put("room", room);
        message.put("username", username);
        client.emit("message", new JSONArray().put(message));
    }

    public void start(String name, boolean privacy){
        try {
            JSONObject message = new JSONObject();
            message.put("msg", "got user media");
            message.put("to", "sabachanna");
            message.put("room", room);
            message.put("username", username);
            client.emit("message", new JSONArray().put(message));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addPeer(String id, int endPoint) {
        Peer peer = new Peer(id, endPoint);
        peers.put(id, peer);

        endPoints[endPoint] = true;
    }

    private void removePeer(String id) {
        Peer peer = peers.get(id);
        peer.pc.close();
        peer.pc.dispose();
        peers.remove(peer.id);

        endPoints[peer.endPoint] = false;
    }

    private int findEndPoint() {
        for(int i = 0; i < MAX_PEER; i++) {
            if(!endPoints[i]) return i;
        }
        return MAX_PEER;
    }

    public void setCamera(String cameraFacing, String height, String width){
        MediaConstraints videoConstraints = new MediaConstraints();
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxHeight", height));
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxWidth", width));

        VideoSource videoSource = factory.createVideoSource(getVideoCapturer(cameraFacing), videoConstraints);
        lMS = factory.createLocalMediaStream("ARDAMS");
        lMS.addTrack(factory.createVideoTrack("ARDAMSv0", videoSource));
        lMS.addTrack(factory.createAudioTrack("ARDAMSa0"));

        mListener.onLocalStream(lMS);
    }

    /*
   Cycle through likely device names for the camera and return the first
   capturer that works, or crash if none do.
   */
    private VideoCapturer getVideoCapturer(String cameraFacing) {
        int[] cameraIndex = { 0, 1 };
        int[] cameraOrientation = { 0, 90, 180, 270 };
        for (int index : cameraIndex) {
            for (int orientation : cameraOrientation) {
                String name = "Camera " + index + ", Facing " + cameraFacing +
                        ", Orientation " + orientation;
                VideoCapturer capturer = VideoCapturer.create(name);
                if (capturer != null) {
                    return capturer;
                }
            }
        }
        throw new RuntimeException("Failed to open capturer");
    }

}
