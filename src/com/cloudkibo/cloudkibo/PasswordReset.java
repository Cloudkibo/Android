package com.cloudkibo.cloudkibo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudkibo.library.UserFunctions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class PasswordReset extends Activity {

	private static String KEY_SUCCESS = "status";
	private static String KEY_MSG = "msg";

	EditText username;
	Button resetpass;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.passwordreset);

		Button login = (Button) findViewById(R.id.bktolog);
		login.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent myIntent = new Intent(view.getContext(), Login.class);
				startActivityForResult(myIntent, 0);
				finish();
			}

		});

		resetpass = (Button) findViewById(R.id.resetBtn);
		
		resetpass.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				NetAsync(view);

			}

		});
	}

	private class NetCheck extends AsyncTask<String, String, Boolean>

	{
		private ProgressDialog nDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			nDialog = new ProgressDialog(PasswordReset.this);
			nDialog.setMessage("Loading..");
			nDialog.setTitle("Checking Network");
			nDialog.setIndeterminate(false);
			nDialog.setCancelable(true);
			//nDialog.show();
		}

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
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return false;

		}

		@Override
		protected void onPostExecute(Boolean th) {

			if (th == true) {
				nDialog.dismiss();
				new ProcessRegister().execute();
			} else {
				nDialog.dismiss();
				Toast.makeText(getApplicationContext(),
						"Could not connect to Internet", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	private class ProcessRegister extends AsyncTask<String, String, JSONObject> {

		private ProgressDialog pDialog;

		String usernameStr;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			username = (EditText) findViewById(R.id.txtUsername);
			usernameStr = username.getText().toString();

			pDialog = new ProgressDialog(PasswordReset.this);
			pDialog.setTitle("Contacting CloudKibo");
			pDialog.setMessage("Sending request ...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected JSONObject doInBackground(String... args) {

			UserFunctions userFunction = new UserFunctions();
			JSONObject json = userFunction.forPass(usernameStr);
			return json;

		}

		@Override
		protected void onPostExecute(JSONObject json) {
			/**
			 * Checks if the Password Change Process is sucesss
			 **/
			try {
				if (json.getString(KEY_SUCCESS) != null) {
					
					String res = json.getString(KEY_SUCCESS);
					String red = json.getString(KEY_MSG);

					if (res.equals("success") || res.equals("error")) {
						pDialog.dismiss();
						Toast.makeText(getApplicationContext(),
								red,
								Toast.LENGTH_SHORT).show();

					} else {
						pDialog.dismiss();
						Toast.makeText(getApplicationContext(),
								"Error occured in changing Password",
								Toast.LENGTH_SHORT).show();
					}

				}
			} catch (JSONException e) {
				e.printStackTrace();

			}
		}
	}

	public void NetAsync(View view) {
		new NetCheck().execute();
	}
}
