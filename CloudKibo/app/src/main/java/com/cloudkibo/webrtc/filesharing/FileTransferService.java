package com.cloudkibo.webrtc.filesharing;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoRendererGui;




import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class FileTransferService extends Service {
	
	PeerConnectionFactory factory;
	FilePeer peer;

	  @Override
	  public int onStartCommand(Intent intent, int flags, int startId) {
	    

	    return Service.START_NOT_STICKY;
	  }

	  @Override
	  public IBinder onBind(Intent intent) {
		  
		  
	    return null;
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
		

		private class FilePeer implements SdpObserver {
			
			private PeerConnection pc;
			private DataChannel dc;
			
			public FilePeer() {
				// Commented for testing
	    		/*PcObserver pcObserver = new PcObserver();
	    		
	    		pc = factory.createPeerConnection(RTCConfig.getIceServer(), 
		  	    		  RTCConfig.getMediaConstraints(), pcObserver);
	    		
				dc = pc.createDataChannel("sendDataChannel", new DataChannel.Init());
				*/
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
			        
			        //sendSocketMessageDataChannel(payload.toString());
			        
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
		
	  
	} 
