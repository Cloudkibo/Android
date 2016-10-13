package com.cloudkibo.library;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//import com.cloudkibo.R;
import com.cloudkibo.R;

import static com.cloudkibo.library.AccountGeneral.sServerAuthenticate;
import static com.cloudkibo.library.Login.ARG_ACCOUNT_TYPE;
import static com.cloudkibo.library.Login.KEY_ERROR_MESSAGE;
import static com.cloudkibo.library.Login.PARAM_USER_PASS;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Register extends Activity {
	
	private AccountManager mAccountManager;
    private String mAuthTokenType;
    private String mAccountType;

	/**
	 * JSON Response node names.
	 **/

	private static String KEY_SUCCESS = "status";
	private static String KEY_UID = "_id";
	private static String KEY_FIRSTNAME = "firstname";
	private static String KEY_LASTNAME = "lastname";
	private static String KEY_USERNAME = "username";
	private static String KEY_EMAIL = "email";
	private static String KEY_PHONE = "phone";
	private static String KEY_CREATED_AT = "date";
	private static String KEY_ERROR = "error";

	/**
	 * Defining layout items.
	 **/

	EditText inputFirstName;
	EditText inputLastName;
	EditText inputUsername;
	EditText inputEmail;
	EditText inputPassword;
	EditText inputPhone;
	Button btnRegister;
	TextView registerErrorMsg;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		
		mAccountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);

		/**
		 * Defining all layout items
		 **/

		inputFirstName = (EditText) findViewById(R.id.editFirstName);
		inputLastName = (EditText) findViewById(R.id.editLastName);
		inputUsername = (EditText) findViewById(R.id.editTextUserName);
		inputEmail = (EditText) findViewById(R.id.editEmail);
		inputPassword = (EditText) findViewById(R.id.editTextPassword);
		inputPhone = (EditText) findViewById(R.id.editPhone);
		btnRegister = (Button) findViewById(R.id.btnRegister);


		/**
		 * Register Button click event. A Toast is set to alert when the fields
		 * are empty. Another toast is set to alert Username must be 5
		 * characters.
		 **/

		btnRegister.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				if ((!inputUsername.getText().toString().equals(""))
						&& (!inputPassword.getText().toString().equals(""))
						&& (!inputFirstName.getText().toString().equals(""))
						&& (!inputLastName.getText().toString().equals(""))
						&& (!inputEmail.getText().toString().equals(""))
                        && (!inputPhone.getText().toString().equals(""))) {

                    if (inputUsername.getText().toString().length() < 3) {
                        Toast.makeText(getApplicationContext(),
                                "Username should be minimum 3 characters",
                                Toast.LENGTH_SHORT).show();
					} else if(inputPassword.getText().toString().length() < 7){
                        Toast.makeText(getApplicationContext(),
                                "Password should be minimum 7 characters",
                                Toast.LENGTH_SHORT).show();
                    } else if(inputFirstName.getText().toString().length() < 3){
                        Toast.makeText(getApplicationContext(),
                                "First Name is too short",
                                Toast.LENGTH_SHORT).show();
                    } else if(inputLastName.getText().toString().length() < 3){
                        Toast.makeText(getApplicationContext(),
                                "Last Name is too short",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        NetAsync(view);
					}

				} else {
					Toast.makeText(getApplicationContext(),
							"One or more fields are empty", Toast.LENGTH_SHORT)
							.show();
				}
			}
		});

	}// end onCreate()

	public void NetAsync(View view) {
		new NetCheck().execute();
	}

	/**
	 * Async Task to check whether internet connection is working
	 **/

	private class NetCheck extends AsyncTask<String, String, Boolean> {
		private ProgressDialog nDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			nDialog = new ProgressDialog(Register.this);
			nDialog.setMessage("Loading..");
			nDialog.setTitle("Checking Network");
			nDialog.setIndeterminate(false);
			nDialog.setCancelable(true);
			nDialog.show();
		}

		@Override
		protected Boolean doInBackground(String... args) {

			/**
			 * Gets current device state and checks for working internet
			 * connection by trying Google.
			 **/
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

	private class ProcessRegister extends AsyncTask<String, String, Intent> {

		/**
		 * Defining Process dialog
		 **/
		private ProgressDialog pDialog;

		String email, password, firstname, lastname, username, phone;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			firstname = inputFirstName.getText().toString();
			lastname = inputLastName.getText().toString();
			email = inputEmail.getText().toString();
			username = inputUsername.getText().toString();
			password = inputPassword.getText().toString();
			phone = inputPhone.getText().toString();
			pDialog = new ProgressDialog(Register.this);
			pDialog.setTitle("Contacting CloudKibo");
			pDialog.setMessage("Registering ...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected Intent doInBackground(String... args) {

			UserFunctions userFunction = new UserFunctions();
			
			final String accountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);
			
			Bundle data = new Bundle();
			
			try {
				String authtoken = userFunction.registerUser(firstname, lastname,
						email, username, password, phone);
				data.putString(AccountManager.KEY_ACCOUNT_NAME, username);
	            data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
	            data.putString(AccountManager.KEY_AUTHTOKEN, authtoken);
	            data.putString(PARAM_USER_PASS, password);

            } catch (Exception e) {
                data.putString(KEY_ERROR_MESSAGE, e.getMessage());
            }
			
            final Intent res = new Intent();
            res.putExtras(data);
            return res;

		}

		@Override
		protected void onPostExecute(Intent intent) {
            pDialog.dismiss();
			if (intent.hasExtra(KEY_ERROR_MESSAGE)) {
                Toast.makeText(getBaseContext(), intent.getStringExtra(KEY_ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
            } else {
                setResult(RESULT_OK, intent);
                finish();
            }
		}
	}
	
	@Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

}
