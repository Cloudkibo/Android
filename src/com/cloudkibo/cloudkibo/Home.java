package com.cloudkibo.cloudkibo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.library.DatabaseHandler;

import com.koushikdutta.async.http.socketio.Acknowledge;
import com.koushikdutta.async.http.socketio.ConnectCallback;
import com.koushikdutta.async.http.socketio.EventCallback;
import com.koushikdutta.async.http.socketio.SocketIOClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Home extends Activity {
	
	
	
	
	/////////////////////////////////////////////////////////////////////
	// All Variables                                                   //
    /////////////////////////////////////////////////////////////////////
	
	private SocketIOClient client;
	private MessageHandler messageHandler = new MessageHandler();
	Button btnLogout;
	String room = "globalchatroom";
	HashMap<String, String> user;
	
	private static String KEY_STATUS = "status";
	
	UserFunctions userFunctions;

	
	
	
	
	
	
	
	
	
	/////////////////////////////////////////////////////////////////////
	// ACTIVITIY IS CREATED HERE                                       //
    /////////////////////////////////////////////////////////////////////
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		
		
		
		
		/////////////////////////////////////////////////////////////////////
		// CHECKING USER'S LOGIN STATUS                                    //
		/////////////////////////////////////////////////////////////////////
		
		userFunctions = new UserFunctions();
        if(userFunctions.isUserLoggedIn(getApplicationContext())){
        	
    		setContentView(R.layout.home);

    		
    		
    		
    		
    		
    		/////////////////////////////////////////////////////////////////////
    		// INITIALISATION AND DECLARATIONS OF VARIABLES                    //
    	    /////////////////////////////////////////////////////////////////////
    		
    		btnLogout = (Button) findViewById(R.id.logout);

    		DatabaseHandler db = new DatabaseHandler(getApplicationContext());
    		
    		
    		// Hashmap to load data from the Sqlite database
    		user = new HashMap<String, String>();
    		user = db.getUserDetails();
    		
    		
    		
    		
    		
    		
    		
    		
    		/////////////////////////////////////////////////////////////////////
    		// CONNECTING TO SOCKET.IO                                         //
    		/////////////////////////////////////////////////////////////////////
    		
    		SocketIOClient.connect("https://www.cloudkibo.com", new ConnectCallback() {

    			  
    			
    		      @Override
    		      public void onConnectCompleted(Exception ex, SocketIOClient socket) {
    		    	  
    		        if (ex != null) {
    		            Log.e("SOCKET.IO","WebRtcClient connect failed: "+ex.getMessage());
    		          return;
    		        }
    		        
    		        
    		        Log.d("SOCKET.IO","WebRtcClient connected.");
    		        
    		        client = socket;
    		        
    		        
    		        
    		        
    		        
    		        // JOINING THE CHAT ROOM
    		        
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
    		
    		
    		
    		
    		
    		NetAsync();

    		
    		
    		
    		
    		
    		
    		
    		
    		/////////////////////////////////////////////////////////////////////
    		// USER INTERFACE CONTROLS                                         //
    		/////////////////////////////////////////////////////////////////////
    		
    		
    		/**
    		 * LOGOUT from the User Panel which clears the data in Sqlite database
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
        else{
        	
        	 Intent login = new Intent(getApplicationContext(), Login.class);
             login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             startActivity(login);
            
             // Closing dashboard screen
             finish();
        }
        
		
		
	}


	
	
	
	
	
	/////////////////////////////////////////////////////////////////////
	// SOCKET.IO MESSAGE HANDLER CLASS                                 //
    /////////////////////////////////////////////////////////////////////
	
	
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
	
	
	
	
	
	
	/////////////////////////////////////////////////////////////////////
	// CHECKING BEFORE CONNECTING TO INTERNET                          //
    /////////////////////////////////////////////////////////////////////

	public void NetAsync() {
		new NetCheck().execute();
	}
	
	
	
	
	
	
	
	/////////////////////////////////////////////////////////////////////
	// ASYNC TASK TO CHECK INTERNET                                    //
    /////////////////////////////////////////////////////////////////////

	private class NetCheck extends AsyncTask<String, String, Boolean> {
		private ProgressDialog nDialog;
		
		
		
		
		// PRE-EXECUTE

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			nDialog = new ProgressDialog(Home.this);
			nDialog.setTitle("Checking Network");
			nDialog.setMessage("Loading..");
			nDialog.setIndeterminate(false);
			nDialog.setCancelable(true);
			// nDialog.show();
		}
		
		
		
		
		
		// DO THIS WORK IN BACKGROUND

		/**
		 * Gets current device state and checks for working internet connection
		 * by trying Google.
		 **/
		@Override
		protected Boolean doInBackground(String... args) {

			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getActiveNetworkInfo();
			if (netInfo != null && netInfo.isConnected()) {
				try {
					URL url = new URL("http://www.google.com");
					HttpURLConnection urlc = (HttpURLConnection) url
							.openConnection();
					urlc.setConnectTimeout(3000);
					urlc.connect();
					if (urlc.getResponseCode() == 200) {
						return true;
					}
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return false;

		}
		
		
		
		
		
		
		// POST-EXECUTE

		@Override
		protected void onPostExecute(Boolean th) {

			if (th == true) {
				nDialog.dismiss();
				new ProcessFetchContacts().execute();
			} else {
				nDialog.dismiss();
				Toast.makeText(getApplicationContext(),
						"Could not connect to Internet", Toast.LENGTH_SHORT)
						.show();
			}
		}
		
		
	}
	
	
	
	
	
	
	
	
	/////////////////////////////////////////////////////////////////////
	// GETTING DATA FROM INTERNET NOW                                  //
    /////////////////////////////////////////////////////////////////////

	/**
	 * Async Task to get and send data to My Sql database through JSON respone.
	 **/
	private class ProcessFetchContacts extends AsyncTask<String, String, JSONObject> {

		private ProgressDialog pDialog;
		
		
		
		
		
		// PRE-EXECUTE

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			pDialog = new ProgressDialog(Home.this);
			pDialog.setTitle("Contacting CloudKibo");
			pDialog.setMessage("Preparing Contacts List ...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		
		
		
		
		
		
		// DO THIS WORK IN BACKGROUND

		@Override
		protected JSONObject doInBackground(String... args) {

			UserFunctions userFunction = new UserFunctions();
			JSONObject json = userFunction.getContacts();
			return json;
		}
		
		
		
		
		
		
		// POST-EXECUTE

		@Override
		protected void onPostExecute(JSONObject json) {
			try {

				if (json.getString(KEY_STATUS) != null) {

					String res = json.getString(KEY_STATUS);

					if (res.equals("success")) {
						pDialog.setMessage("Loading User Space");
						pDialog.setTitle("Getting Data");
						DatabaseHandler db = new DatabaseHandler(
								getApplicationContext());
						JSONObject json_user = json.getJSONObject("user");
						/**
						 * Clear all previous data in SQlite database.
						 **/
						UserFunctions logout = new UserFunctions();
						logout.logoutUser(getApplicationContext());
			/*			db.addUser(json_user.getString(KEY_FIRSTNAME),
								json_user.getString(KEY_LASTNAME),
								json_user.getString(KEY_EMAIL),
								json_user.getString(KEY_USERNAME),
								json_user.getString(KEY_UID),
								json_user.getString(KEY_CREATED_AT));
						*/
						/**
						 * If JSON array details are stored in SQlite it
						 * launches the User Panel.
						 **/

						Intent upanel = new Intent(getApplicationContext(),
								Home.class);
						upanel.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						pDialog.dismiss();
						startActivity(upanel);

						/**
						 * Close Login Screen
						 **/
						finish();
					} else {

						pDialog.dismiss();
						Toast.makeText(getApplicationContext(),
								"Incorrect Username or Password",
								Toast.LENGTH_SHORT).show();
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}


}