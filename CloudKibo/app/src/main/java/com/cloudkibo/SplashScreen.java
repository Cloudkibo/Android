package com.cloudkibo;

//import com.cloudkibo.R;
import com.cloudkibo.R;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.library.AccountGeneral;
import com.cloudkibo.library.DisplayNameReg;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.ui.Invite_Friends;
import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import static com.cloudkibo.library.AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

/**
 * The Class SplashScreen will launched at the start of the application. It will
 * be displayed for 3 seconds and than finished automatically and it will also
 * start the next activity of app.
 */
public class SplashScreen extends Activity
{
	final private Boolean isDevelopment = false;

	/** Check if the app is running. */
	private boolean isRunning;
	private AccountManager mAccountManager;
	
	private AlertDialog mAlertDialog;
    private boolean mInvalidate;

	public static int APP_REQUEST_CODE = 99;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);
		mAccountManager = AccountManager.get(this);
		AccountKit.initialize(getApplicationContext());

		setContentView(R.layout.splash);

		isRunning = true;

		startSplash();

	}

	/**
	 * Starts the count down timer for 3-seconds. It simply sleeps the thread
	 * for 3-seconds.
	 */
	private void startSplash()
	{

		new Thread(new Runnable() {
			@Override
			public void run()
			{

				try
				{

					Thread.sleep(3000);

				} catch (Exception e)
				{
					e.printStackTrace();
				} finally
				{
					runOnUiThread(new Runnable() {
						@Override
						public void run()
						{
							doFinish();
						}
					});
				}
			}
		}).start();
	}

	/**
	 * If the app is still running than this method will start the Login activity
	 * and finish the Splash.
	 */
	private synchronized void doFinish()
	{

		if (isRunning)
		{
			isRunning = false;

			AccessToken accessToken = null;
			try {
				accessToken = AccountKit.getCurrentAccessToken();
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (accessToken != null) {
				//Handle Returning User
				if(isDevelopment) {
					Intent i = new Intent(this, DisplayNameReg.class);
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					i.putExtra("authtoken", accessToken.getToken());
					startActivity(i);
					finish();
				} else {
					UserFunctions fn = new UserFunctions();
					if(fn.isUserLoggedIn(getApplicationContext())){
						Intent i = new Intent(this, MainActivity.class);
						i.putExtra("authtoken", accessToken.getToken());
						i.putExtra("sync", true);
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(i);
						finish();
					} else {
						Intent i = new Intent(this, DisplayNameReg.class);
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						i.putExtra("authtoken", accessToken.getToken());
						startActivity(i);
						finish();
					}

				}


			} else {
				onLoginPhone();
			}
/*
            if(mAccountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE).length < 1){
                getTokenForAccountCreateIfNeeded(AccountGeneral.ACCOUNT_TYPE, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS);
            }
            else {
                getExistingAccountAuthToken(mAccountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE)[0],
                        AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS);
            }
*/
		}
	}


	public void onLoginPhone() {
		onLogin();
	}

	private void onLogin() {
		final Intent intent = new Intent(this, AccountKitActivity.class);
		AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
				new AccountKitConfiguration.AccountKitConfigurationBuilder(
						LoginType.PHONE,
						AccountKitActivity.ResponseType.TOKEN); // or .ResponseType.TOKEN
		// ... perform additional configuration ...
		intent.putExtra(
				AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
				configurationBuilder.build());
		startActivityForResult(intent, APP_REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(
			final int requestCode,
			final int resultCode,
			final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == APP_REQUEST_CODE) { // confirm that this response matches your request
			AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
			String toastMessage;
			if (loginResult.getError() != null) {
				toastMessage = loginResult.getError().getErrorType().getMessage();
				finish();
			} else if (loginResult.wasCancelled()) {
				toastMessage = "Login Cancelled";
			} else {
				if (loginResult.getAccessToken() != null) {
					toastMessage = "Login Successful"; //+ loginResult.getAccessToken().getAccountId();

					Intent i = new Intent(this, DisplayNameReg.class);
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					i.putExtra("authtoken", loginResult.getAccessToken().getToken());
					startActivity(i);
					finish();
				} else {
					toastMessage = String.format(
							"Success:%s...",
							loginResult.getAuthorizationCode().substring(0,10));
				}

				// If you have an authorization code, retrieve it from
				// loginResult.getAuthorizationCode()
				// and pass it to your server and exchange it for an access token.

				// Success! Start your next activity...
				//goToMyLoggedInActivity();
			}

			// Surface the result to your user in an appropriate way.
			Toast.makeText(
					this,
					toastMessage,
					Toast.LENGTH_LONG)
					.show();
		}
	}
	
	/**
	 * Get an auth token for the account.
	 * If not exist - add it and then return its auth token.
	 * If one exist - return its auth token.
	 * If more than one exists - show a picker and return the select account's auth token.
	 * @param accountType
	 * @param authTokenType
	 */
	private void getTokenForAccountCreateIfNeeded(String accountType, String authTokenType) {
		final AccountManagerFuture<Bundle> future = mAccountManager.getAuthTokenByFeatures(accountType, authTokenType, null, this, null, null,
				new AccountManagerCallback<Bundle>() {
			@Override
			public void run(AccountManagerFuture<Bundle> future) {
				Bundle bnd = null;
				try {
					bnd = future.getResult();
					String authtoken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
					//Toast.makeText(getBaseContext(), ((authtoken != null) ? "SUCCESS!\ntoken: " + authtoken : "FAIL"), Toast.LENGTH_SHORT).show();                            
					Log.d("SOJHARO", "GetTokenForAccount Bundle is " + bnd);

					Intent i = new Intent(SplashScreen.this, Invite_Friends.class);
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					i.putExtra("authtoken", authtoken);
					startActivity(i);
					finish();


				} catch (Exception e) {
					e.printStackTrace();
					//Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
				}
			}
		}
		, null);
	}

    /**
     * Get the auth token for an existing account on the AccountManager
     * @param account
     * @param authTokenType
     */
    private void getExistingAccountAuthToken(Account account, String authTokenType) {
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(account, authTokenType, null, this, null, null);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bundle bnd = future.getResult();

                    final String authtoken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                    //showMessage((authtoken != null) ? "SUCCESS!\ntoken: " + authtoken : "FAIL");

                    Intent i = new Intent(SplashScreen.this, MainActivity.class);
                    i.putExtra("authtoken", authtoken);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();

                } catch (Exception e) {
                    e.printStackTrace();
                    //showMessage(e.getMessage());
                }
            }
        }).start();
    }

	/* (non-Javadoc)
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{

		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			isRunning = false;
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}