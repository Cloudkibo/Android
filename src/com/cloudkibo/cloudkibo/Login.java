package com.cloudkibo.cloudkibo;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import org.json.JSONException;
import org.json.JSONObject;

import com.cloudkibo.library.DatabaseHandler;
import com.cloudkibo.library.UserFunctions;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Login extends Activity {

	Button loginBtn;
	TextView Btnregister;
	TextView passreset;
	EditText userNameText;
	EditText passwordText;

	/**
	 * Called when the activity is first created.
	 */
	private static String KEY_SUCCESS = "status";
	private static String KEY_UID = "_id";
	private static String KEY_USERNAME = "username";
	private static String KEY_FIRSTNAME = "firstname";
	private static String KEY_LASTNAME = "lastname";
	private static String KEY_EMAIL = "email";
	private static String KEY_CREATED_AT = "date";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login);

		userNameText = (EditText) findViewById(R.id.firstNameText);
		passwordText = (EditText) findViewById(R.id.lastNameText);
		loginBtn = (Button) findViewById(R.id.submitBtn);

		Btnregister = (TextView) findViewById(R.id.btnRegister);
		passreset = (TextView) findViewById(R.id.forgotPass);

		/**
		 * Login button click event A Toast is set to alert when the Email and
		 * Password field is empty
		 **/
		loginBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {

				if ((!userNameText.getText().toString().equals(""))
						&& (!passwordText.getText().toString().equals(""))) {
					NetAsync(view);
				} else if ((!userNameText.getText().toString().equals(""))) {
					Toast.makeText(getApplicationContext(),
							"Username is required", Toast.LENGTH_SHORT).show();
				} else if ((!passwordText.getText().toString().equals(""))) {
					Toast.makeText(getApplicationContext(),
							"Password is required", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(),
							"Username and Password are required",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		Btnregister.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent myIntent = new Intent(view.getContext(), Register.class);
				startActivityForResult(myIntent, 0);
				finish();
			}
		});

		passreset.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent myIntent = new Intent(view.getContext(),
						PasswordReset.class);
				startActivityForResult(myIntent, 0);
				finish();
			}
		});
	}

	public void NetAsync(View view) {
		new NetCheck().execute();
	}

	/**
	 * Async Task to check whether internet connection is working.
	 **/

	private class NetCheck extends AsyncTask<String, String, Boolean> {
		private ProgressDialog nDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			nDialog = new ProgressDialog(Login.this);
			nDialog.setTitle("Checking Network");
			nDialog.setMessage("Loading..");
			nDialog.setIndeterminate(false);
			nDialog.setCancelable(true);
			// nDialog.show();
		}

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
				new ProcessLogin().execute();
			} else {
				nDialog.dismiss();
				Toast.makeText(getApplicationContext(),
						"Could not connect to Internet", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	/**
	 * Async Task to get and send data to My Sql database through JSON respone.
	 **/
	private class ProcessLogin extends AsyncTask<String, String, JSONObject> {

		private ProgressDialog pDialog;

		String username, password;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			userNameText = (EditText) findViewById(R.id.firstNameText);
			passwordText = (EditText) findViewById(R.id.lastNameText);
			username = userNameText.getText().toString();
			password = passwordText.getText().toString();
			pDialog = new ProgressDialog(Login.this);
			pDialog.setTitle("Contacting CloudKibo");
			pDialog.setMessage("Logging in ...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected JSONObject doInBackground(String... args) {

			UserFunctions userFunction = new UserFunctions();
			JSONObject json = userFunction.loginUser(username, password);
			return json;
		}

		@Override
		protected void onPostExecute(JSONObject json) {
			try {

				if (json.getString(KEY_SUCCESS) != null) {

					String res = json.getString(KEY_SUCCESS);

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
						db.addUser(json_user.getString(KEY_FIRSTNAME),
								json_user.getString(KEY_LASTNAME),
								json_user.getString(KEY_EMAIL),
								json_user.getString(KEY_USERNAME),
								json_user.getString(KEY_UID),
								json_user.getString(KEY_CREATED_AT));
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