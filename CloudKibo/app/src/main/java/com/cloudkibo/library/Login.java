package com.cloudkibo.library;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.cloudkibo.library.AccountGeneral.KEY_MSG;
import static com.cloudkibo.library.AccountGeneral.KEY_STATUS;

import android.accounts.AccountAuthenticatorActivity;
import android.content.IntentSender;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;

//import com.cloudkibo.R;
//import com.cloudkibo.R.id;

import com.cloudkibo.R;
import com.cloudkibo.R.id;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;

/**
 * The Class Login is an Activity class that shows the login screen to users.
 * The current implementation simply start the MainActivity. You can write your
 * own logic for actual login and for login using Facebook and Twitter.
 */
public class Login extends AccountAuthenticatorActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener
{
	
	private AccountManager mAccountManager;
    private String mAuthTokenType;
	
	Button btnLogin;
	Button btnRegister;
    Button btnForgot;
    LoginButton btnFacebook;
	EditText userNameText;
	EditText passwordText;

	/**
	 * Called when the activity is first created.
	 */
	private static String KEY_STATUS = "status";
	private static String KEY_UID = "_id";
	private static String KEY_USERNAME = "username";
	private static String KEY_FIRSTNAME = "firstname";
	private static String KEY_LASTNAME = "lastname";
	private static String KEY_EMAIL = "email";
	private static String KEY_CREATED_AT = "date";
	
	public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    public static final String KEY_ERROR_MESSAGE = "ERR_MSG";

    public final static String PARAM_USER_PASS = "USER_PASS";

    private final int REQ_SIGNUP = 1;
    private final int REQ_FORGOT = 2;

    private CallbackManager callbackManager; // for facebook login

    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient; // for google authentication

    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false; // for google authentication

    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false; // for google authentication

    // todo added for google authentication, need to remove or work on (Google Login required methods)
    @Override
    public void onClick(View v) {
        if (v.getId() == id.btnGP) {
            // User clicked the sign-in button, so begin the sign-in process and automatically
            // attempt to resolve any errors that occur.
            mShouldResolve = true;
            mGoogleApiClient.connect();

            // Show a message to the user that we are signing in.
            Toast.makeText(getApplicationContext(),
                    "Google Login in progress", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        // onConnected indicates that an account was selected on the device, that the selected
        // account has granted any requested permissions to our app and that we were able to
        // establish a service connection to Google Play services.
        Log.d("GOOGLE_SIGN", "onConnected:" + bundle);
        mShouldResolve = false;

        // Show the signed-in UI
        Toast.makeText(getApplicationContext(),
                "Google Login successful: "+ Plus.PeopleApi.getCurrentPerson(mGoogleApiClient).getDisplayName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.
        Log.d("GOOGLE_SIGN", "onConnectionFailed:" + connectionResult);

        if (!mIsResolving && mShouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, RC_SIGN_IN);
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e) {
                    Log.e("GOOGLE_SIGN", "Could not resolve ConnectionResult.", e);
                    mIsResolving = false;
                    mGoogleApiClient.connect();
                }
            } else {
                // Could not resolve the connection result, show the user an
                // error dialog.
                Toast.makeText(getApplicationContext(),
                        connectionResult.toString(), Toast.LENGTH_SHORT).show();
            }
        } else {
            // Show the signed-out UI

            //showSignedOutUI();
        }
    }

    /**
     * todo use this for google sign out later
     */
    private void onSignOutClicked() {
        // Clear the default account so that GoogleApiClient will not automatically
        // connect in the future.
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
        }

        //showSignedOutUI();
    }
    // End Google Login required methods


    /* (non-Javadoc)
	 * @see com.chatt.custom.CustomActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

       // FacebookSdk.sdkInitialize(getApplicationContext());
		setContentView(R.layout.login);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.PROFILE))
                .build();
		
		mAccountManager = AccountManager.get(getBaseContext());

        mAuthTokenType = getIntent().getStringExtra(ARG_AUTH_TYPE);
        if (mAuthTokenType == null)
            mAuthTokenType = AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS;

		userNameText = (EditText) findViewById(R.id.editTextUserName);
		passwordText = (EditText) findViewById(R.id.editTextPassword);
		btnLogin = (Button) findViewById(R.id.btnLogin);

        //btnFacebook = (LoginButton) findViewById(R.id.btnFb);
        findViewById(id.btnGP).setOnClickListener(this);

        callbackManager = CallbackManager.Factory.create();

        //btnFacebook.setReadPermissions("user_friends");
/*
        btnFacebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                Log.e("FACEBOOK", "User ID: "
                        + loginResult.getAccessToken().getUserId()
                        + "\n" +
                        "Auth Token: "
                        + loginResult.getAccessToken().getToken());

            }

            @Override
            public void onCancel() {
                Log.e("FACEBOOK", "User cancelled");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.e("FACEBOOK", "Error occurred");
            }
        });
*/

		btnRegister = (Button) findViewById(R.id.btnReg);
        btnForgot = (Button) findViewById(id.btnForgot);


		/**
		 * Login button click event A Toast is set to alert when the Email and
		 * Password field is empty
		 **/
		btnLogin.setOnClickListener(new View.OnClickListener() {

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

		btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(getBaseContext(), Register.class);
                //myIntent.putExtras(getIntent().getExtras());
                startActivityForResult(myIntent, REQ_SIGNUP);
                //finish(); //Don't Finish this activity for now
            }
        });

        btnForgot.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(getBaseContext(), ForgotPassword.class);
                //myIntent.putExtras(getIntent().getExtras());
                startActivityForResult(myIntent, REQ_FORGOT);
                //finish(); //Don't Finish this activity for now
            }
        });
		
	}
	
	
	
	
	/////////////////////////////////////////////////////////////////////
	// ACTIVITY RETURNS RESULT                                //
	/////////////////////////////////////////////////////////////////////
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // The sign up activity returned that the user has successfully created an account
        if (requestCode == REQ_SIGNUP && resultCode == RESULT_OK) {
            finishLogin(data);
        } else if (requestCode == REQ_FORGOT && resultCode == RESULT_OK) {
            if(data.getStringExtra(KEY_STATUS).equals("success"))
                Toast.makeText(getBaseContext(), "Check your email", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getBaseContext(), data.getStringExtra(KEY_MSG), Toast.LENGTH_SHORT).show();
        } else if (requestCode == RC_SIGN_IN) {
            // If the error resolution was not successful we should not resolve further.
            if (resultCode != RESULT_OK) {
                mShouldResolve = false;
            }

            mIsResolving = false;
            mGoogleApiClient.connect();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }
	
	
	
	
	/////////////////////////////////////////////////////////////////////
	// CHECKING BEFORE CONNECTING TO INTERNET                          //
    /////////////////////////////////////////////////////////////////////

	public void NetAsync(View view) {
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
			nDialog = new ProgressDialog(Login.this);
			nDialog.setTitle("Checking Network");
			nDialog.setMessage("Loading..");
			nDialog.setIndeterminate(false);
			nDialog.setCancelable(true);
			nDialog.show();
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
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
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
				new ProcessLogin().execute();
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
	private class ProcessLogin extends AsyncTask<String, Void, Intent> {

		private ProgressDialog pDialog;

		String username, password;
		
		
		
		
		
		// PRE-EXECUTE

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			userNameText = (EditText) findViewById(R.id.editTextUserName);
			passwordText = (EditText) findViewById(id.editTextPassword);
			username = userNameText.getText().toString();
			password = passwordText.getText().toString();
			pDialog = new ProgressDialog(Login.this);
			pDialog.setTitle("Contacting CloudKibo");
			pDialog.setMessage("Logging in ...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		
		
		
		
		
		
		// DO THIS WORK IN BACKGROUND

		@Override
		protected Intent doInBackground(String... args) {

			UserFunctions userFunction = new UserFunctions();
			
			final String accountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);
			
			Bundle data = new Bundle();
			
			try {
				String authtoken = userFunction.loginUser(username, password);
				
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
		
		
		
		
		
		
		// POST-EXECUTE

		@Override
		protected void onPostExecute(Intent intent) {
            pDialog.dismiss();
			if (intent.hasExtra(KEY_ERROR_MESSAGE)) {
                Toast.makeText(getBaseContext(), intent.getStringExtra(KEY_ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
            } else {
                finishLogin(intent);
            }
		}
	}
	
	private void finishLogin(Intent intent) {

        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountPassword = intent.getStringExtra(PARAM_USER_PASS);
        final Account account = new Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));

        if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
            String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
            String authtokenType = mAuthTokenType;

            // Creating the account on the device and setting the auth token we got
            // (Not setting the auth token will cause another call to the server to authenticate the user)
            mAccountManager.addAccountExplicitly(account, accountPassword, null);
            mAccountManager.setAuthToken(account, authtokenType, authtoken);
        } else {
            mAccountManager.setPassword(account, accountPassword);
        }

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
	
}
