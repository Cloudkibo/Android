package com.cloudkibo.webrtc.filesharing;

import java.nio.ByteBuffer;
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

public class FileConnection extends CustomActivity {

	PeerConnectionFactory factory;
	FilePeer peer;
	
	String peerName;
	String fileData;
	Boolean initiator;
	
	SocketService socketService;
	boolean isBound = false;
	
	private HashMap<String, String> user;
	private String room;
	
	Button sendButton;
	
	@SuppressWarnings("unchecked")
	protected void onCreate(Bundle savedInstanceState)
	{
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fileshare);
		
		user = (HashMap) getIntent().getExtras().get("user");
		room = getIntent().getExtras().getString("room");
		peerName = getIntent().getExtras().getString("contact");
        fileData = getIntent().getExtras().getString("filepath");
        initiator = getIntent().getExtras().getBoolean("initiator");
        
        Intent i = new Intent(this, SocketService.class);
        i.putExtra("user", user);
        i.putExtra("room", room);
        startService(i);
        bindService(i, socketConnection, Context.BIND_AUTO_CREATE);
        
        sendButton = (Button) findViewById(R.id.sendFile);
        
        sendButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View view) {
				
				PeerConnectionFactory.initializeAndroidGlobals(getApplicationContext(), true, true,
		        		VideoRendererGui.getEGLContext());
		        
				factory = new PeerConnectionFactory();
				
				peer = new FilePeer();
				
				if(initiator){
					createOffer();
				}
				
			}
		});
		
		//peer.dc.send("data will be sent from here");
		
	}
	
	protected void onDestroy() {
		unbindService(socketConnection);
		super.onDestroy();
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
		        
		        sendSocketMessageDataChannel(payload.toString());
		        
		        
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

		@Override
		public void onMessage(Buffer data) {
			Log.w("FILE", data.toString());
			
		}

		@Override
		public void onStateChange() {
			
			Toast.makeText(getApplicationContext(),
                    "State Got Changed", Toast.LENGTH_SHORT)
                    .show();
			
			/*
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
			*/
		}

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
					
					if(type.equals("messagefordatachannel")){
						
						try {
							
							JSONObject payload = body.getJSONObject(0);
							String type2 = body.getJSONObject(0).getString("type");
							
							Toast.makeText(getApplicationContext(),
				                    payload.toString(), Toast.LENGTH_SHORT)
				                    .show();
							
							if(type2.equals("offer")){
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
						}
						
						
						
					}
					
				}
			});
		}
	};
}
