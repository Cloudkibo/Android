package com.cloudkibo.library;

import io.cordova.hellocordova.CordovaApp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.ui.ContactList;
import com.cloudkibo.ui.GroupChat;
import com.cloudkibo.utils.IFragmentName;
import com.cloudkibo.webrtc.filesharing.FileConnection;
import com.koushikdutta.async.http.socketio.Acknowledge;
import com.koushikdutta.async.http.socketio.ConnectCallback;
import com.koushikdutta.async.http.socketio.EventCallback;
import com.koushikdutta.async.http.socketio.SocketIOClient;

import android.app.Dialog;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SocketService extends Service {
	
	private final IBinder socketBinder = new SocketBinder();
	
	private SocketIOClient client;
	
	private MessageHandler messageHandler = new MessageHandler();

	@Override
	public IBinder onBind(Intent intent) {
		return socketBinder;
	}
	
	public SocketService(){
		
	}
	
	
	public class SocketBinder extends Binder{
		public SocketService getService(){
			return SocketService.this;
		}
	}
	
	public void setSocketIOConfig(){

		SocketIOClient.connect("https://www.cloudkibo.com", new ConnectCallback() {

			@Override
			public void onConnectCompleted(Exception ex, SocketIOClient socket) {

				if (ex != null) {
					Log.e("SOCKET.IO","WebRtcClient connect failed: "+ex.getMessage());
					return;
				}


				Log.d("SOCKET.IO","WebRtcClient connected.");

				client = socket;

				JSONObject message = new JSONObject();

				try {
					
					JSONObject userInfo = new JSONObject();
					//userInfo.put("username", user.get("username"));
					//userInfo.put("_id", user.get("_id"));


					message.put("user", userInfo);
					//message.put("room", room);

					socket.emit("join global chatroom", new JSONArray().put(message));
					

				} catch (JSONException e) {
					e.printStackTrace();
				}
			// specify which events you are interested in receiving

				client.addListener("id", messageHandler);
				client.addListener("message", messageHandler);
				//client.addListener("youareonline", messageHandler);
				client.addListener("im", messageHandler);
				client.addListener("theseareonline", messageHandler);
				client.addListener("offline", messageHandler);
				client.addListener("online", messageHandler);
				client.addListener("Reject Call", messageHandler);
				client.addListener("Accept Call", messageHandler);
				client.addListener("areyoufreeforcall", messageHandler);
				client.addListener("othersideringing", messageHandler);
				client.addListener("calleeisbusy", messageHandler);
				client.addListener("calleeisoffline", messageHandler);
				client.addListener("messagefordatachannel", messageHandler);

			}
		}, new Handler());

	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	

	private class MessageHandler implements EventCallback {


		@Override
		public void onEvent(String s, JSONArray jsonArray,
				Acknowledge acknowledge) {
/*			Log.d("SOCKET.IO", "ID = "+ s);
			Log.d("SOCKET.IO", "MSG = "+ jsonArray.toString());
			
			try{
				
				if(s.equals("im")){
					
					try{
					
						IFragmentName myFragment = (IFragmentName) getSupportFragmentManager().findFragmentById(R.id.content_frame);
						
						if(myFragment.getFragmentName().equals("GroupChat"))
						{
						   GroupChat myGroupChatFragment = (GroupChat) myFragment;
						   myGroupChatFragment.receiveMessage(jsonArray.getJSONObject(0).getString("msg")); //here you call the method of your current Fragment.
						   
						   DatabaseHandler db = new DatabaseHandler(getApplicationContext());
						   db.addChat(jsonArray.getJSONObject(0).getString("to"), 
								   jsonArray.getJSONObject(0).getString("from"),
								   jsonArray.getJSONObject(0).getString("fromFullName"),
								   jsonArray.getJSONObject(0).getString("msg"),
								   jsonArray.getJSONObject(0).getString("date")); // DATE THROWS EXCEPTION
						}
						else{
							//NEED TO DO NOTIFICATION LIKE WORK HERE.. IT MAY RING OR DO SOMETHING, CHECK FOR EACH FRAGMENT
						}
					}catch(NullPointerException e){}
										
				}
				else if(s.equals("messagefordatachannel")){
					
					IFragmentName myFragment = (IFragmentName) getSupportFragmentManager().findFragmentById(R.id.content_frame);
					
					if(myFragment.getFragmentName().equals("FileConnection"))
					{
						
						FileConnection myFileConnectionFragment = (FileConnection) myFragment;
					    myFileConnectionFragment.receivedSignallingData(jsonArray); //here you call the method of your current Fragment.
					   
					}
					else{
					}
					
				}
				else if(jsonArray.get(0).toString().startsWith("Missed")){
					
					Toast.makeText(getApplicationContext(),
							jsonArray.get(0).toString(), Toast.LENGTH_SHORT).show();
					
					amInCall = false;
					
					ringing = false;
					
					dialog.dismiss();
					
					amInCallWith = "";
					
				}
				else if(jsonArray.get(0).toString().equals("Reject Call")){
					
					Toast.makeText(getApplicationContext(),
							amInCallWith +
							" is busy", Toast.LENGTH_SHORT).show();
					
					amInCall = false;
					
					dialog.dismiss();
					
					otherSideRinging = false;
					
					amInCallWith = "";
					
				}
				else if(jsonArray.get(0).toString().equals("got user media")){
					
					client.disconnect();
  					
  					Intent i = new Intent(getApplicationContext(), CordovaApp.class);
  					i.putExtra("username", user.get("username"));
  					i.putExtra("_id", user.get("_id"));
  					i.putExtra("peer", amInCallWith);
  					i.putExtra("lastmessage", "GotUserMedia");
  					i.putExtra("room", room);
  		            startActivity(i);
					
				}
				else if(jsonArray.get(0).toString().equals("Accept Call")){
					
					
					dialog.dismiss();
					
					otherSideRinging = false;
					
					areYouCallingSomeone = false;
					
					client.disconnect();
					
					Intent i = new Intent(getApplicationContext(), CordovaApp.class);
  					i.putExtra("username", user.get("username"));
  					i.putExtra("_id", user.get("_id"));
  					i.putExtra("peer", amInCallWith);
  					i.putExtra("lastmessage", "AcceptCallFromOther");
  					i.putExtra("room", room);
  		            startActivity(i);
					
				}
				else if(s.equals("theseareonline")){
					
					try{
					
						IFragmentName myFragment = (IFragmentName) getSupportFragmentManager().findFragmentById(R.id.content_frame);
						Log.d("SOCKET.IO", "ONLINEs = "+ jsonArray.toString());
						if(myFragment.getFragmentName().equals("ContactList"))
						{
						   ContactList myContactListFragment = (ContactList) myFragment;
	
						   myContactListFragment.setOnlineStatus(jsonArray); //here you call the method of your current Fragment.
						   
						}
					}catch(NullPointerException e){}
					
				}
				else if(s.equals("calleeisoffline")){
					
					try{
					
						amInCall = false;
						
						amInCallWith = "";
						
						dialog.dismiss();
						
						Toast.makeText(getApplicationContext(),
								jsonArray.getString(0) +
								" is offline", Toast.LENGTH_SHORT).show();
						
					}catch(NullPointerException e){}
					
				}
				else if(s.equals("calleeisbusy")){
					
					try{
					
						amInCall = false;
						
						amInCallWith = "";
						
						dialog.dismiss();
						
						Toast.makeText(getApplicationContext(),
								jsonArray.getJSONObject(0).getString("callee") +
								" is busy", Toast.LENGTH_SHORT).show();
						
					}catch(NullPointerException e){}
					
				}
				else if(s.equals("othersideringing")){
					
					try{
					
						amInCall = true;
						
						otherSideRinging = true;
						
						amInCallWith = jsonArray.getJSONObject(0).getString("callee");
						
						dialog = new Dialog(MainActivity.this);
		      			dialog.setContentView(R.layout.call_dialog);
		      			dialog.setTitle(amInCallWith);
		       
		      			// set the custom dialog components - text, image and button
		      			TextView text = (TextView) dialog.findViewById(R.id.textDialog);
		      			text.setText(amInCallWith);
		      			ImageView image = (ImageView) dialog.findViewById(R.id.imageDialog);
		      			image.setImageResource(R.drawable.ic_launcher);
		       
		      			Button dialogButton = (Button) dialog.findViewById(R.id.declineButton);
		      			// if button is clicked, close the custom dialog
		      			dialogButton.setOnClickListener(new OnClickListener() {
		      				@Override
		      				public void onClick(View v) {
		      				
		      					sendSocketMessage("Missed Incoming Call: "+ user.get("username"), amInCallWith);
			      				
		      					amInCall = false;
								
								amInCallWith = "";
								
								otherSideRinging = false;
								
								areYouCallingSomeone = false;
			      				
		      					dialog.dismiss();
		      				}
		      			});
		       
		      			dialog.show();
						
					}catch(NullPointerException e){}
					
				}
				else if(s.equals("areyoufreeforcall")){
					
					try{
					
						JSONObject message2 = new JSONObject();
						
						message2.put("me", user.get("username"));
						message2.put("mycaller", jsonArray.getJSONObject(0).getString("caller"));
						
						if(!amInCall){
							
							isSomeOneCalling = true;
							ringing = true;
							amInCall = true;
							
							amInCallWith = jsonArray.getJSONObject(0).getString("caller");
							
							client.emit("yesiamfreeforcall", new JSONArray().put(message2));

							dialog = new Dialog(MainActivity.this);
			      			dialog.setContentView(R.layout.call_dialog2);
			      			dialog.setTitle(amInCallWith);
			       
			      			// set the custom dialog components - text, image and button
			      			TextView text = (TextView) dialog.findViewById(R.id.textDialog);
			      			text.setText(amInCallWith);
			      			ImageView image = (ImageView) dialog.findViewById(R.id.imageDialog);
			      			image.setImageResource(R.drawable.ic_launcher);
			       
			      			Button dialogButton = (Button) dialog.findViewById(R.id.declineButton);
			      			// if button is clicked, close the custom dialog
			      			dialogButton.setOnClickListener(new OnClickListener() {
			      				@Override
			      				public void onClick(View v) {
			      					
			      					sendSocketMessage("Reject Call", amInCallWith);
			      					
			      					isSomeOneCalling = false;
			      					ringing = false;
			      					amInCall = false;
			      					amInCallWith = "";
			      					
			      					dialog.dismiss();
			      				}
			      			});
			      			
			      			Button acceptButton = (Button) dialog.findViewById(R.id.acceptButton);
			      			// if button is clicked, close the custom dialog
			      			acceptButton.setOnClickListener(new OnClickListener() {
			      				@Override
			      				public void onClick(View v) {
			      					
			      					sendSocketMessage("Accept Call", amInCallWith);
			      					
			      					isSomeOneCalling = false;
			      					ringing = false;
			      					
			      					dialog.dismiss();   
			      				}
			      			});
			       
			      			dialog.show();
							
						}
						else{
							
							client.emit("noiambusy", new JSONArray().put(message2));
						}
						
					}catch(NullPointerException e){}
					
				}
				else if(s.equals("offline")){
					try{
					
						IFragmentName myFragment = (IFragmentName) getSupportFragmentManager().findFragmentById(R.id.content_frame);
						Log.d("SOCKET.IO", "ONLINEs = "+ jsonArray.toString());
						if(myFragment.getFragmentName().equals("ContactList"))
						{
						   ContactList myContactListFragment = (ContactList) myFragment;
	
						   myContactListFragment.setOfflineStatusIndividual(jsonArray); //here you call the method of your current Fragment.
						   
						}
					}catch(NullPointerException e){}
				}
				else if(s.equals("online")){
					
					try{
					
						IFragmentName myFragment = (IFragmentName) getSupportFragmentManager().findFragmentById(R.id.content_frame);
						Log.d("SOCKET.IO", "ONLINEs = "+ jsonArray.toString());
						if(myFragment.getFragmentName().equals("ContactList"))
						{
						   ContactList myContactListFragment = (ContactList) myFragment;
	
						   myContactListFragment.setOnlineStatusIndividual(jsonArray); //here you call the method of your current Fragment.
						   
						}
					}catch(NullPointerException e){}
					
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		*/}
	}
	

}
