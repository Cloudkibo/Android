package com.cloudkibo.webrtc.filesharing;

import java.nio.ByteBuffer;

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


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomActivity;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.utils.IFragmentName;

public class FileConnection extends CustomActivity {

	PeerConnectionFactory factory;
	FilePeer peer;
	
	String peerName;
	String fileData;
	
	
	
	protected void onCreate(Bundle savedInstanceState)
	{
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fileshare);
		
		Toast.makeText(getApplicationContext(),
                "came here", Toast.LENGTH_SHORT)
                .show();
		
		factory = new PeerConnectionFactory();
		
		peer = new FilePeer();
		
		/*
		MainActivity mainActivity = (MainActivity)getActivity();
		
		if(mainActivity.isInitiatorFileTransfer()){
			
			peerName = mainActivity.getFilePeerName();
			fileData = mainActivity.getFileData();
			
			createOffer();
		}
		*/
		
		createOffer();
		
		//peer.dc.send("data will be sent from here");
		
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
	
	public class FilePeer implements SdpObserver, PeerConnection.Observer, DataChannel.Observer {
		
		private PeerConnection pc;
		private DataChannel dc;
		
		public FilePeer() {
			
	      this.pc = factory.createPeerConnection(RTCConfig.getIceServer(), 
	    		  RTCConfig.getMediaConstraints(), this);
	      
	      
	      dc = this.pc.createDataChannel("sendDataChannel", new DataChannel.Init());
			
	    }

		@Override
		public void onAddStream(MediaStream arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onDataChannel(DataChannel dataChannel) {
			this.dc = dataChannel;

		}

		@Override
		public void onIceCandidate(final IceCandidate candidate) {
			try {
		        JSONObject payload = new JSONObject();
		        payload.put("type", "candidate");
		        payload.put("label", candidate.sdpMLineIndex);
		        payload.put("id", candidate.sdpMid);
		        payload.put("candidate", candidate.sdp);
		        
		        //MainActivity mainActivity = (MainActivity)getActivity();
           	 	//mainActivity.sendSocketMessageDataChannel(payload.toString());
		        
		        
		      } catch (JSONException e) {
		        e.printStackTrace();
		      }

		}

		@Override
		public void onIceConnectionChange(IceConnectionState iceConnectionState) {
			
		}

		@Override
		public void onIceGatheringChange(IceGatheringState arg0) {
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
		public void onSignalingChange(SignalingState arg0) {
			// TODO Auto-generated method stub

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
		        
		        //MainActivity act1 = (MainActivity)getActivity();
           	 	//act1.sendSocketMessageDataChannel(payload.toString());
		        
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

		@Override
		public void onMessage(Buffer data) {
			Log.w("FILE", data.toString());
			
		}

		@Override
		public void onStateChange() {
			
			 byte[] bytes = new byte[10];
			 
			 bytes[0] = 0;
			 bytes[1] = 1;
			 bytes[2] = 2;
			 bytes[3] = 3;
			 bytes[4] = 4;
			 bytes[5] = 5;
			 bytes[6] = 6;
			 bytes[7] = 7;
			 bytes[8] = 8;
			 bytes[9] = 9;
			 
		     ByteBuffer buf = ByteBuffer.wrap(bytes);
		     
		     
		     
		     Buffer b = new Buffer(buf, true);
		     
			dc.send(b);
			
		}

	}
	/*
	public void receivedSignallingData(JSONArray data){
		
	}
*/

}
