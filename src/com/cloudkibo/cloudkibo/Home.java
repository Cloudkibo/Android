package com.cloudkibo.cloudkibo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

import fr.pchab.AndroidRTC.WebRtcClient.AddIceCandidateCommand;
import fr.pchab.AndroidRTC.WebRtcClient.Command;
import fr.pchab.AndroidRTC.WebRtcClient.CreateAnswerCommand;
import fr.pchab.AndroidRTC.WebRtcClient.CreateOfferCommand;
import fr.pchab.AndroidRTC.WebRtcClient.SetRemoteSDPCommand;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Home extends Activity {
	
	private SocketIOClient client;
	
	Button btnLogout;
	Button changepas;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);

		
		
		
		// changepas = (Button) findViewById(R.id.btchangepass);
		btnLogout = (Button) findViewById(R.id.logout);

		DatabaseHandler db = new DatabaseHandler(getApplicationContext());

		/**
		 * Hashmap to load data from the Sqlite database
		 **/
		HashMap<String, String> user = new HashMap<String, String>();
		user = db.getUserDetails();

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
	    private HashMap<String, Command> commandMap;

	    public MessageHandler() {
	      this.commandMap = new HashMap<String, Command>();
	      commandMap.put("init", new CreateOfferCommand());
	      commandMap.put("offer", new CreateAnswerCommand());
	      commandMap.put("answer", new SetRemoteSDPCommand());
	      commandMap.put("candidate", new AddIceCandidateCommand());
	    }

	    @Override
	    public void onEvent(String s, JSONArray jsonArray,
				Acknowledge acknowledge) {
			try {
				Log.d(TAG, "MessageHandler.onEvent() "
						+ (s == null ? "nil" : s));

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	  }


}