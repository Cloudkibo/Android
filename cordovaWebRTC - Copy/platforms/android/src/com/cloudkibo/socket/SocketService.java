package com.cloudkibo.socket;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.cordova.hellocordova.CordovaApp;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
//import com.koushikdutta.async.http.socketio.Acknowledge;
//import com.koushikdutta.async.http.socketio.ConnectCallback;
//import com.koushikdutta.async.http.socketio.EventCallback;
//import com.koushikdutta.async.http.socketio.SocketIOClient;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Dialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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

/**
 * Service for connecting to socket server. This should be alive forever to receive
 * chat and other updates from server in real-time.
 * 
 * Learning Sources:
 * http://developer.android.com/guide/components/services.html
 * http://www.vogella.com/tutorials/AndroidServices/article.html
 * http://stackoverflow.com/questions/10547577/how-can-a-remote-service-send-messages-to-a-bound-activity
 * 
 * @author sojharo
 *
 */

public class SocketService extends Service {
	
	private final IBinder socketBinder = new SocketBinder();
	private BoundServiceListener mListener;
	
	private HashMap<String, String> user;
	private String room;
	
	//private SocketIOClient client;
	//private MessageHandler messageHandler = new MessageHandler();
	
	private Boolean areYouCallingSomeone = false;
	private Boolean ringing = false;
	private String amInCallWith;
	private Boolean amInCall = false;
	private Boolean otherSideRinging = false;
	private Boolean isSomeOneCalling = false;
	
	//nkzawa
	Socket socket;

	@Override
	public IBinder onBind(Intent intent) {
		return socketBinder;
	}

	
	public class SocketBinder extends Binder{
		
		public SocketService getService(){
			return SocketService.this;
		}
		
		public void setListener(BoundServiceListener listener) {
	        mListener = listener;
	    }
		
	}
	
	public void setSocketIOConfig(){
			
		try {
			
			socket = IO.socket("https://www.cloudkibo.com");
			socket.connect();
			
			socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

				  @Override
				  public void call(Object... args) {
					  JSONObject message = new JSONObject();

						try {
							
							JSONObject userInfo = new JSONObject();
							userInfo.put("username", user.get("username"));
							userInfo.put("_id", user.get("_id"));

							message.put("user", userInfo);
							message.put("room", room);

							socket.emit("join global chatroom", new JSONArray().put(message));
							

						} catch (JSONException e) {
							e.printStackTrace();
						}
				  }

				}).on("youareonline", new Emitter.Listener() {

				  @Override
				  public void call(Object... args) {
					  Log.e("SOCKETTEST", args.toString());
				  }
				  
				  //todo Sample
				  
				  /*
				  public void call(Object... args) {

	                    mListener.receiveSocketMessage("id", args[0].toString());

	                    JSONObject obj = new JSONObject();
	                    try {
	                        obj.put("msg", args[0]);
	                    } catch (JSONException e) {
	                        e.printStackTrace();
	                    }

	                    socket.emit("message", obj);
	                }*/

				}).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

				  @Override
				  public void call(Object... args) {}

				});
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		/*
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
					userInfo.put("username", user.get("username"));
					userInfo.put("_id", user.get("_id"));


					message.put("user", userInfo);
					message.put("room", room);

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

	*/
	}
	
	/**
	 * This checks if the activity or app is running or not.
	 * @param myPackage
	 * @return
	 */
	
	public boolean isForeground(String myPackage) {
	    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	    List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1); 
	    ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
	    return componentInfo.getPackageName().equals(myPackage);
	}
	
	public static boolean isAppSentToBackground(final Context context) {

	    try {
	        ActivityManager am = (ActivityManager) context
	                .getSystemService(Context.ACTIVITY_SERVICE);
	        // The first in the list of RunningTasks is always the foreground
	        // task.
	        RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);
	        String foregroundTaskPackageName = foregroundTaskInfo.topActivity
	                .getPackageName();// get the top fore ground activity
	        PackageManager pm = context.getPackageManager();
	        PackageInfo foregroundAppPackageInfo = pm.getPackageInfo(
	                foregroundTaskPackageName, 0);

	        String foregroundTaskAppName = foregroundAppPackageInfo.applicationInfo
	                .loadLabel(pm).toString();

	        // Log.e("", foregroundTaskAppName +"----------"+
	        // foregroundTaskPackageName);
	        if (!foregroundTaskAppName.equals("CloudKibo")) {
	            return true;
	        }
	    } catch (Exception e) {
	        Log.e("isAppSentToBackground", "" + e);
	    }
	    return false;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		user = (HashMap) intent.getExtras().get("user");
		room = intent.getExtras().getString("room");
		
		return Service.START_REDELIVER_INTENT;
	}
	
	/**
	 * This following part needs some rigorous testing of more than 2 days usage.
	 * I need to check what happens when this method is not called. Or what happens
	 * when service is destroyed and started again by the system (not activity)
	 * 
	 * Learning Source: http://stackoverflow.com/questions/14182014/android-oncreate-or-onstartcommand-for-starting-service
	 * 
	 * @author sojharo
	 */

	@Override
	public void onCreate() {
		
		setSocketIOConfig();
		
		super.onCreate();
	}

	
	/*
	private class MessageHandler implements EventCallback {


		@Override
		public void onEvent(String s, JSONArray jsonArray,
				Acknowledge acknowledge) {
			Log.d("SOCKET.IO", "ID = "+ s);
			Log.d("SOCKET.IO", "MSG = "+ jsonArray.toString());
			
			try{
				
				if(s.equals("im")){
					
					try{
						
						mListener.receiveSocketMessage("im", jsonArray.getJSONObject(0).getString("msg"));
					
						// todo Date throws exception
						
						DatabaseHandler db = new DatabaseHandler(getApplicationContext());
					    
						db.addChat(jsonArray.getJSONObject(0).getString("to"), 
							   jsonArray.getJSONObject(0).getString("from"),
							   jsonArray.getJSONObject(0).getString("fromFullName"),
							   jsonArray.getJSONObject(0).getString("msg"),
							   jsonArray.getJSONObject(0).getString("date")); // DATE THROWS EXCEPTION
					   
					}catch(NullPointerException e){}
										
				}
				else if(s.equals("messagefordatachannel")){
					
					mListener.receiveSocketArray("messagefordatachannel", jsonArray);
					
				}
				else if(jsonArray.get(0).toString().startsWith("Missed")){
					
					
					// todo Create Notification here for the missed call
					 
					
					Toast.makeText(getApplicationContext(),
							jsonArray.get(0).toString(), Toast.LENGTH_SHORT).show();
					
					amInCall = false;
					
					ringing = false;
					
					mListener.receiveSocketMessage("Missed", "");
					
					amInCallWith = "";
					
				}
				else if(jsonArray.get(0).toString().equals("Reject Call")){
					
					Toast.makeText(getApplicationContext(),
							amInCallWith +
							" is busy", Toast.LENGTH_SHORT).show();
					
					amInCall = false;
					
					mListener.receiveSocketMessage("Reject Call", "");
					
					otherSideRinging = false;
					
					amInCallWith = "";
					
				}
				else if(jsonArray.get(0).toString().equals("got user media")){
					
					// todo not sure about this. check if cordova app can access service run by
					// our main app
					 
					
					//client.disconnect();
  					
					mListener.receiveSocketMessage("got user media", amInCallWith);
					
					
				}
				else if(jsonArray.get(0).toString().equals("Accept Call")){
					
					otherSideRinging = false;
					
					areYouCallingSomeone = false;
					
					mListener.receiveSocketMessage("Accept Call", amInCallWith);
					
					// todo not sure about this. check if cordova app can access service run by
					// our main app
					 
					
					//client.disconnect();
					
					
				}
				else if(s.equals("theseareonline")){
					mListener.receiveSocketArray("theseareonline", jsonArray);
				}
				else if(s.equals("calleeisoffline")){
					
					amInCall = false;
					
					amInCallWith = "";
					
					mListener.receiveSocketMessage("calleeisoffline", "");
					
					Toast.makeText(getApplicationContext(),
							jsonArray.getString(0) +
							" is offline", Toast.LENGTH_SHORT).show();
					
				}
				else if(s.equals("calleeisbusy")){
					
					amInCall = false;
					
					amInCallWith = "";
					
					mListener.receiveSocketMessage("calleeisbusy", "");
					
					Toast.makeText(getApplicationContext(),
							jsonArray.getString(0) +
							" is offline", Toast.LENGTH_SHORT).show();
					
				}
				else if(s.equals("othersideringing")){
					
					try{
					
						amInCall = true;
						
						otherSideRinging = true;
						
						amInCallWith = jsonArray.getJSONObject(0).getString("callee");
						
						mListener.receiveSocketMessage("othersideringing", amInCallWith);
						
					}catch(NullPointerException e){}
					
				}
				else if(s.equals("areyoufreeforcall")){
				
					JSONObject message2 = new JSONObject();
					
					message2.put("me", user.get("username"));
					message2.put("mycaller", jsonArray.getJSONObject(0).getString("caller"));
					
					if(!amInCall){
						
						isSomeOneCalling = true;
						ringing = true;
						amInCall = true;
						
						amInCallWith = jsonArray.getJSONObject(0).getString("caller");
						
						socket.emit("yesiamfreeforcall", new JSONArray().put(message2));
						
						mListener.receiveSocketMessage("areyoufreeforcall", amInCallWith);
						
					}
					else{
						
						socket.emit("noiambusy", new JSONArray().put(message2));
					}
				
				}
				else if(s.equals("offline")){
					mListener.receiveSocketArray("offline", jsonArray);
				}
				else if(s.equals("online")){
					mListener.receiveSocketArray("online", jsonArray);
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	*/
	
	public void sendSocketMessage(String msg, String peer){
		
		JSONObject message = new JSONObject();
		
		try {
			
			message.put("msg", msg);
			message.put("room", room);
			message.put("to", peer);
			message.put("username", user.get("username"));
			
			socket.emit("message", new JSONArray().put(message));
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void stopCallMessageToCallee(){
		
		sendSocketMessage("Missed Incoming Call: "+ user.get("username"), amInCallWith);
			
		amInCall = false;
		amInCallWith = "";
		otherSideRinging = false;
		areYouCallingSomeone = false;
		
	}
	
	public void acceptCallMessageToCallee(){
		sendSocketMessage("Reject Call", amInCallWith);
		
		isSomeOneCalling = false;
		ringing = false;
		amInCall = false;
		amInCallWith = "";
	}
	
	public void rejectCallMessageToCallee(){
		sendSocketMessage("Accept Call", amInCallWith);
		
		isSomeOneCalling = false;
		ringing = false;
	}
	
	public void sendSocketMessageDataChannel(String msg, String filePeer){
		
		JSONObject message = new JSONObject();
		
		try {
			
			message.put("msg", msg);
			message.put("room", room);
			message.put("to", filePeer);
			message.put("from", user.get("username"));
			
			socket.emit("messagefordatachannel", new JSONArray().put(message));
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void callThisPerson(String contact){
		
		try {

			JSONObject message1 = new JSONObject();

			message1.put("room", room);
			message1.put("caller", user.get("username"));
			message1.put("callee", contact);

			socket.emit("callthisperson", new JSONArray().put(message1));
			
			areYouCallingSomeone = true;

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			Toast.makeText(getApplicationContext(),
                    "Could not make this call. No Internet or behind proxy", Toast.LENGTH_SHORT)
                    .show();
		}
	}
	
	public void sendMessage(String contactUserName, String contactId, String msg){
		
		try {
			
			JSONObject message = new JSONObject();
			
			message.put("from", user.get("username"));
			message.put("to", contactUserName);
			message.put("from_id", user.get("_id"));
			message.put("to_id", contactId);
			message.put("fromFullName", user.get("firstname")+" "+ user.get("lastname"));
			message.put("msg", msg);
			message.put("date", (new Date().toString()));
			
			JSONObject completeMessage = new JSONObject();
			
			completeMessage.put("room", room);
			completeMessage.put("stanza", message);

			socket.emit("im", new JSONArray().put(completeMessage));
			
			DatabaseHandler db = new DatabaseHandler(getApplicationContext());
			db.addChat(contactUserName, user.get("username"), user.get("firstname")+" "+ user.get("lastname"),
					msg, (new Date().toString()));
			

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			Toast.makeText(getApplicationContext(),
                    "Message not sent. No Internet", Toast.LENGTH_SHORT)
                    .show();
		}
	}
	
	public void askFriendsOnlineStatus(){
		
		try {
			
			JSONObject message = new JSONObject();
			
			message.put("_id", user.get("_id"));
			
			JSONObject completeMessage = new JSONObject();
			
			completeMessage.put("room", room);
			completeMessage.put("user", message);

			socket.emit("whozonline", new JSONArray().put(completeMessage));
			
		} catch (JSONException e) {
			e.printStackTrace();
		}


	}

}
