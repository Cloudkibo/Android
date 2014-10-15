package com.cloudkibo.cloudkibo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.library.DatabaseHandler;

import com.koushikdutta.async.http.socketio.Acknowledge;
import com.koushikdutta.async.http.socketio.ConnectCallback;
import com.koushikdutta.async.http.socketio.EventCallback;
import com.koushikdutta.async.http.socketio.SocketIOClient;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Home extends Activity {
	
	private SocketIOClient client;
	private MessageHandler messageHandler = new MessageHandler();
	Button btnLogout;
	String room = "globalchatroom";
	HashMap<String, String> user;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);

		
		
		
		btnLogout = (Button) findViewById(R.id.logout);

		DatabaseHandler db = new DatabaseHandler(getApplicationContext());
		
		

		/**
		 * Hashmap to load data from the Sqlite database
		 **/
		user = new HashMap<String, String>();
		user = db.getUserDetails();
		
		
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
		        	
					message.put("username", user.get("firstname"));
					message.put("room", room);
					
					socket.emit("join global chatroom", new JSONArray().put(message));
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
		        

		        // specify which events you are interested in receiving
		        client.addListener("id", messageHandler);
		        client.addListener("message", messageHandler);
		      }
		}, new Handler());

		
		
		/**
		 * Logout from the User Panel which clears the data in Sqlite database
		 **/
		btnLogout.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {

				UserFunctions logout = new UserFunctions();
				logout.logoutUser(getApplicationContext());
				Intent login = new Intent(getApplicationContext(), Login.class);
				login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(login);
				finish();
			}
		});
		
		
		

		/**
		 * Sets user first name and last name in text view.
		 **/

		final TextView login = (TextView) findViewById(R.id.textwelcome);
		login.setText("Welcome " + user.get("firstname") + " "
				+ user.get("lastname"));

	}


	private class MessageHandler implements EventCallback {
	    

	    @Override
	    public void onEvent(String s, JSONArray jsonArray,
				Acknowledge acknowledge) {
			//try {
				

			//} catch (JSONException e) {
				//e.printStackTrace();
			//}
		}
	  }


}