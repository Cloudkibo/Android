package com.cloudkibo.webrtc.filesharing;

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

public class FileConnection extends CustomActivity {

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
	
	Button makeConnectionButton;
	Button sendFileButton;
	Button downloadFileButton;
	Button saveFileButton;
	
	@SuppressWarnings("unchecked")
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
        
        makeConnectionButton = (Button) findViewById(R.id.makeConnection);
        
        makeConnectionButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View view) {
				
				createPeerConnectionFactory();
				
				peer = new FilePeer();
				
				if(initiator){
					createOffer();
				}
				
			}
		});
        
        sendFileButton = (Button) findViewById(R.id.sendFile);
        
        sendFileButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View view) {
					
				runOnUiThread(new Runnable() {
				      public void run() {
			    	
						try {

				    		JSONObject metadata = new JSONObject();
				    		
							metadata.put("eventName", "data_msg");
							metadata.put("data", (new JSONObject()).put("file_meta", Utility.getFileMetaData(filePath)));

							peer.dc.send(new DataChannel.Buffer(Utility.toByteBuffer(metadata.toString()), false));
							
							peer.dc.send(new DataChannel.Buffer(Utility.toByteBuffer("You have " +
									"received a file. Download and Save it."), false));
							
						} catch (JSONException e) {
							e.printStackTrace();
						}
						
				      }
				    });
				
			}
		});
        
        downloadFileButton = (Button) findViewById(R.id.downloadFile);
        
        downloadFileButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View view) {
				
				requestChunk();				
			}
		});
		
        saveFileButton = (Button) findViewById(R.id.saveFile);
        
        saveFileButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View view) {
				
				runOnUiThread(new Runnable(){
					public void run() {
						
						byte[] fileBytes = new byte[fileBytesArray.size()];
						
						for(int i=0; i<fileBytesArray.size(); i++){
							fileBytes[i] = fileBytesArray.get(i);
						}
						
						if(Utility.convertByteArrayToFile(fileBytes, fileNameToSave)){
							Toast.makeText(getApplicationContext(),
				                    "File stored in Downloads", Toast.LENGTH_SHORT)
				                    .show();
						}
						else{
							Toast.makeText(getApplicationContext(),
				                    "Some error caused storage failure. You must have SD card.", Toast.LENGTH_SHORT)
				                    .show();
						}
					}
			    });
				
			}
		});
		
	}
	
	public void requestChunk(){
		
		Log.d("FILERECEIVE", "Requesting CHUNK: "+ chunkNumberToRequest);
		
		JSONObject request_chunk = new JSONObject();
		
		try {
			
			request_chunk.put("eventName", "request_chunk");
			
			JSONObject request_data = new JSONObject();
			request_data.put("chunk", chunkNumberToRequest);
			request_data.put("browser", "chrome"); // This chrome is hardcoded for testing purpose
			
			request_chunk.put("data", request_data);

			peer.dc.send(new DataChannel.Buffer(Utility.toByteBuffer(request_chunk.toString()), false));
			
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	protected void onDestroy() {
		unbindService(socketConnection);
		super.onDestroy();
	}
	
	public void createPeerConnectionFactory(){
		PeerConnectionFactory.initializeAndroidGlobals(getApplicationContext(), true, true,
        		VideoRendererGui.getEGLContext());
        
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
			
			ByteBuffer data = buffer.data;
		    final byte[] bytes = new byte[ data.capacity() ];
		    data.get(bytes);
		    
		    final File file = new File(filePath);;
			
			if(buffer.binary){
				
				String strData = new String( bytes );
			    Log.d("FILETRANSFER", strData);
			    
			    runOnUiThread(new Runnable(){
					public void run() {
						for(int i=0; i<bytes.length; i++)
							fileBytesArray.add(bytes[i]);
						
						if (numberOfChunksReceived % Utility.getChunksPerACK() == (Utility.getChunksPerACK() - 1) 
								|| numberOfChunksInFileToSave == (numberOfChunksReceived + 1)) {
							
							if (numberOfChunksInFileToSave > numberOfChunksReceived) {
								chunkNumberToRequest += Utility.getChunksPerACK();
								
                                requestChunk();
                            }
							
			            }
						
						numberOfChunksReceived++;
					}
			    });
			    
			}
			else {
				
			    runOnUiThread(new Runnable() {
				      public void run() {
				    	    String strData = new String( bytes );
						    
						    Log.d("FILETRANSFER", strData);
						    
						    try {
						    	
								JSONObject jsonData = new JSONObject(strData);
								
								if(jsonData.getJSONObject("data").has("file_meta")){
									
									fileNameToSave = jsonData.getJSONObject("data").getJSONObject("file_meta").getString("name");
									sizeOfFileToSave = jsonData.getJSONObject("data").getJSONObject("file_meta").getInt("size");
									numberOfChunksInFileToSave = (int) Math.ceil(sizeOfFileToSave / Utility.getChunkSize());
									numberOfChunksReceived = 0;
									chunkNumberToRequest = 0;
									
								}
								else if(jsonData.getJSONObject("data").has("kill")){
									
								}
								else if(jsonData.getJSONObject("data").has("ok_to_download")){
									
								}
								else {
									
									boolean isBinaryFile = true;
									
									int chunkNumber = jsonData.getJSONObject("data").getInt("chunk");
									
									Log.d("FILETRANSFER", "Chunk Number "+ chunkNumber);
									if(chunkNumber % Utility.getChunksPerACK() == 0){
										for(int i = 0; i< Utility.getChunksPerACK(); i++){
											
											if(file.length() < Utility.getChunkSize()){
												ByteBuffer byteBuffer = ByteBuffer.wrap(Utility.convertFileToByteArray(file));
												DataChannel.Buffer buf = new DataChannel.Buffer(byteBuffer, isBinaryFile);
												
												peer.dc.send(buf);
												break;
											}
											
											Log.d("FILETRANSFER", "File Length "+ file.length());
											Log.d("FILETRANSFER", "Ceiling "+ Math.ceil(file.length() / Utility.getChunkSize()));
											if((chunkNumber+i) >= Math.ceil(file.length() / Utility.getChunkSize())){
												break;
											}
											
											int upperLimit = (chunkNumber + i + 1) * Utility.getChunkSize();
											
											if(upperLimit > (int)file.length()){
												upperLimit = (int)file.length();
											}
											
											int lowerLimit = (chunkNumber + i) * Utility.getChunkSize();
											Log.d("LIMITS", ""+ lowerLimit +" "+ upperLimit);
											ByteBuffer byteBuffer = ByteBuffer.wrap(Utility.convertFileToByteArray(file), lowerLimit, upperLimit);
											DataChannel.Buffer buf = new DataChannel.Buffer(byteBuffer, isBinaryFile);
											
											peer.dc.send(buf);
											Log.d("CHUNK", "Chunk has been sent");
										}
									}
								}
								
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}  
				      }
				});
			    
			}
			
			
		    
			
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
