package com.cloudkibo.webrtc.conference;

import android.opengl.EGLContext;
import android.util.Log;

import com.cloudkibo.webrtc.filesharing.RTCConfig;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoSource;

import java.util.HashMap;

public class WebRtcClient {
    private final static String TAG = WebRtcClient.class.getCanonicalName();
    private final static int MAX_PEER = 50;
    private boolean[] endPoints = new boolean[MAX_PEER];
    private PeerConnectionFactory factory;
    private HashMap<String, Peer> peers = new HashMap<String, Peer>();
    private PeerConnectionParameters pcParams;
    private MediaConstraints pcConstraints = new MediaConstraints();
    private MediaStream localMS;
    private VideoSource videoSource;
    private RtcListener mListener;

    /**
     * Implement this interface to be notified of events.
     */
    public interface RtcListener{
        void onStatusChanged(String newStatus);

        void onLocalStream(MediaStream localStream);

        void onAddRemoteStream(MediaStream remoteStream, int endPoint);

        void onRemoveRemoteStream(int endPoint);

        void sendMessage(String to, JSONObject payload);
    }

    private void createOffer(String peerId){
        Log.w(TAG, "CreateOfferCommand");
        Peer peer = peers.get(peerId);
        peer.pc.createOffer(peer, pcConstraints);
    }

    private void createAnswer(String peerId, JSONObject payload){
        Log.w(TAG, "CreateAnswerCommand");
        try {
            Peer peer = peers.get(peerId);
            SessionDescription sdp = new SessionDescription(
                    SessionDescription.Type.fromCanonicalForm(payload.getString("type")),
                    payload.getString("sdp")
            );
            peer.pc.setRemoteDescription(peer, sdp);
            peer.pc.createAnswer(peer, pcConstraints);
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    private void SetRemoteSDPCommand(String peerId, JSONObject payload){
        Log.w(TAG, "SetRemoteSDPCommand");
        try {
            Peer peer = peers.get(peerId);
            SessionDescription sdp = new SessionDescription(
                    SessionDescription.Type.fromCanonicalForm(payload.getString("type")),
                    payload.getString("sdp")
            );
            peer.pc.setRemoteDescription(peer, sdp);
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    private void AddIceCandidateCommand(String peerId, JSONObject payload){
        try {
            Log.w(TAG, "AddIceCandidateCommand");
            PeerConnection pc = peers.get(peerId).pc;
            if (pc.getRemoteDescription() != null) {
                IceCandidate candidate = new IceCandidate(
                        payload.getString("id"),
                        payload.getInt("label"),
                        payload.getString("candidate")
                );
                pc.addIceCandidate(candidate);
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    public void messageReceived (String type, JSONObject body){
        try {
            if (type.equals("peer.connected")) {
                String id = body.getString("id");
                Peer peer = addPeer(id, findEndPoint(), body.getString("username"));
                peer.pc.addStream(localMS);
                createOffer(id);
            } else if (type.equals("msg")) {
                String msg_type = body.getString("type");
                if (msg_type.equals("offer")) {
                    if(!peers.containsKey(body.getString("by"))){
                        int endPoint = findEndPoint();
                        if (endPoint != MAX_PEER) {
                            Peer peer = addPeer(body.getString("by"), endPoint, body.getString("username"));
                            peer.pc.addStream(localMS);
                            createAnswer(body.getString("by"), body);
                        }
                    } else {
                        Peer peer = peers.get(body.getString("by"));
                        createAnswer(body.getString("by"), body);
                    }
                } else if(msg_type.equals("answer")){
                    SetRemoteSDPCommand(body.getString("by"), body);
                } else if(msg_type.equals("ice")){
                    AddIceCandidateCommand(body.getString("by"), body);
                }
            }
        }catch(JSONException e) {
            e.printStackTrace();
        }
    }

    private class Peer implements SdpObserver, PeerConnection.Observer{
        private PeerConnection pc;
        private String id;
        private int endPoint;
        private String name;

        @Override
        public void onCreateSuccess(final SessionDescription sdp) {
            // TODO: modify sdp to use pcParams prefered codecs
            try {
                Log.w("Conference", sdp.type.canonicalForm());
                JSONObject sdpString = new JSONObject();
                sdpString.put("type", sdp.type.canonicalForm());
                sdpString.put("sdp", sdp.description);

                JSONObject payload = new JSONObject();
                payload.put("sdp", sdpString);
                payload.put("type", sdp.type.canonicalForm());

                mListener.sendMessage(id, payload);
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
                //socket.emit('msg', { by: currentId, to: id, ice: evnt.candidate, type: 'ice' });
                JSONObject cnd = new JSONObject();
                cnd.put("type", "candidate");
                cnd.put("label", candidate.sdpMLineIndex);
                cnd.put("id", candidate.sdpMid);
                cnd.put("candidate", candidate.sdp);

                JSONObject payload = new JSONObject();
                payload.put("ice", cnd);
                payload.put("type", "ice");

                mListener.sendMessage(id, payload);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAddStream(MediaStream mediaStream) {
            Log.d(TAG,"onAddStream "+mediaStream.label());
            // remote streams are displayed from 1 to MAX_PEER (0 is localStream)
            mListener.onAddRemoteStream(mediaStream, endPoint + 1);
        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {
            Log.d(TAG,"onRemoveStream "+mediaStream.label());
            removePeer(id);
        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {}

        @Override
        public void onRenegotiationNeeded() {

        }

        public Peer(String id, int endPoint, String name) {
            Log.d(TAG,"new Peer: "+id + " " + endPoint);
            this.pc = factory.createPeerConnection(RTCConfig.getIceServer(), RTCConfig.getMediaConstraints(), this);
            this.id = id;
            this.endPoint = endPoint;
            this.name = name;

            pc.addStream(localMS); //, new MediaConstraints()

            mListener.onStatusChanged("CONNECTING");
        }
    }

    private Peer addPeer(String id, int endPoint, String username) {
        Peer peer = new Peer(id, endPoint, username);
        peers.put(id, peer);

        endPoints[endPoint] = true;
        return peer;
    }

    private void removePeer(String id) {
        Peer peer = peers.get(id);
        mListener.onRemoveRemoteStream(peer.endPoint);
        peer.pc.close();
        peers.remove(peer.id);
        endPoints[peer.endPoint] = false;
    }

    public WebRtcClient(RtcListener listener, PeerConnectionParameters params, EGLContext mEGLcontext) {
        mListener = listener;
        pcParams = params;

        PeerConnectionFactory.initializeAndroidGlobals(listener, true, true,
                params.videoCodecHwAcceleration, mEGLcontext);
        factory = new PeerConnectionFactory();
    }

    /**
     * Call this method in Activity.onPause()
     */
    public void onPause() {
        if(videoSource != null) videoSource.stop();
    }

    /**
     * Call this method in Activity.onResume()
     */
    public void onResume() {
        if(videoSource != null) videoSource.restart();
    }

    /**
     * Call this method in Activity.onDestroy()
     */
    public void onDestroy() {
        for (Peer peer : peers.values()) {
            peer.pc.dispose();
        }
        videoSource.dispose();
        factory.dispose();
    }

    private int findEndPoint() {
        for(int i = 0; i < MAX_PEER; i++) if (!endPoints[i]) return i;
        return MAX_PEER;
    }

    /**
     * Start the client.
     *
     * Set up the local stream and notify the signaling server.
     * Call this method after onCallReady.
     *
     */
    public void start(){
        setCamera();
    }

    private void setCamera(){
        localMS = factory.createLocalMediaStream("ARDAMS");
        if(pcParams.videoCallEnabled){
            MediaConstraints videoConstraints = new MediaConstraints();
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxHeight", Integer.toString(pcParams.videoHeight)));
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxWidth", Integer.toString(pcParams.videoWidth)));
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxFrameRate", Integer.toString(pcParams.videoFps)));
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minFrameRate", Integer.toString(pcParams.videoFps)));

            videoSource = factory.createVideoSource(getVideoCapturer(), videoConstraints);
            localMS.addTrack(factory.createVideoTrack("ARDAMSv0", videoSource));
        }

        AudioSource audioSource = factory.createAudioSource(new MediaConstraints());
        localMS.addTrack(factory.createAudioTrack("ARDAMSa0", audioSource));

        mListener.onLocalStream(localMS);
        Log.w("VideoCallView", "inside setCamera");
    }

    private VideoCapturer getVideoCapturer() {
        String frontCameraDeviceName = VideoCapturerAndroid.getNameOfFrontFacingDevice();
        return VideoCapturerAndroid.create(frontCameraDeviceName);
    }

}
