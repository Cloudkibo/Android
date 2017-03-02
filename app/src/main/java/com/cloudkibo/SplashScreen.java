package com.cloudkibo;

//import com.cloudkibo.R;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.library.DisplayNameReg;
import com.cloudkibo.library.UserFunctions;
import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.net.URISyntaxException;
import java.util.TimeZone;

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
							checkFirstRunAndServerURL();
						}
					});
				}
			}
		}).start();
	}

	private void checkFirstRunAndServerURL() {

		final String PREFS_NAME = "MyPrefsFile";
		final String PREF_VERSION_CODE_KEY = "version_code";
		final String PREF_SERVER_URL_KEY = "server_url";
		final int DOESNT_EXIST = -1;


		// Get current version code
		int currentVersionCode = 0;
		try {
			currentVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		} catch (android.content.pm.PackageManager.NameNotFoundException e) {
			// handle exception
			e.printStackTrace();
			return;
		}

		// Get saved version code
		final SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);

		String savedServerURL = prefs.getString(PREF_SERVER_URL_KEY, "-1");

		if(savedServerURL.equals("-1")){
			final int tempCurrentVersionCode = currentVersionCode;
			final int tempSavedVersionCode = savedVersionCode;
			// get prompts.xml view
			LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
			View promptView = layoutInflater.inflate(R.layout.prompt_server_url, null);
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			final EditText input = (EditText) promptView.findViewById(R.id.userInput);
			// set prompts.xml to be the layout file of the alertdialog builder
			alertDialogBuilder.setView(promptView);
			// setup a dialog window
			alertDialogBuilder
					.setCancelable(false)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// get user input and send it to server
							Log.w("SOJHARO", "VALUE = "+ input.getText());

							prefs.edit().putString(PREF_SERVER_URL_KEY, input.getText().toString()).commit();

							checkFirstRun(tempCurrentVersionCode, tempSavedVersionCode, DOESNT_EXIST);
						}
					})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,	int id) {
									dialog.cancel();
									prefs.edit().putString(PREF_SERVER_URL_KEY, getString(R.string.server_url)).commit();
									checkFirstRun(tempCurrentVersionCode, tempSavedVersionCode, DOESNT_EXIST);
								}
							});
			// create an alert dialog
			AlertDialog alertD = alertDialogBuilder.create();
			alertD.show();
		} else {
			prefs.edit().putString(PREF_SERVER_URL_KEY, getString(R.string.server_url)).commit();
			checkFirstRun(currentVersionCode, savedVersionCode, DOESNT_EXIST);
		}


		// Update the shared preferences with the current version code
		prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).commit();

	}

	private void checkFirstRun (int currentVersionCode, int savedVersionCode, int DOESNT_EXIST) {
		// Check for first run or upgrade
		if (currentVersionCode == savedVersionCode) {

			// This is just a normal run
			Toast.makeText(
					this,
					"This is normal run : version "+ currentVersionCode,
					Toast.LENGTH_LONG)
					.show();

			doFinish();

			return;

		} else if (savedVersionCode == DOESNT_EXIST) {

			setupForNewInstall();

		} else if (currentVersionCode > savedVersionCode) {

			// This is an upgrade
			Toast.makeText(
					this,
					"This is an upgrade to next version : old version "+ savedVersionCode,
					Toast.LENGTH_LONG)
					.show();

			doFinish();

		}
	}

	private void setupForNewInstall(){
		// This is a new install (or the user cleared the shared preferences)
		Toast.makeText(
				this,
				"This is a new install",
				Toast.LENGTH_LONG)
				.show();

		UserFunctions fn = new UserFunctions(getApplicationContext());
		if(fn.isUserLoggedIn(getApplicationContext())){
			Toast.makeText(
					this,
					"Old data is found from previous install. Removing it.",
					Toast.LENGTH_LONG)
					.show();

			DatabaseHandler db = new DatabaseHandler(getApplicationContext());
			db.resetChatsTable();
			db = new DatabaseHandler(getApplicationContext());
			db.resetTables();
			db = new DatabaseHandler(getApplicationContext());
			db.resetContactsTable();
			db = new DatabaseHandler(getApplicationContext());
			db.resetChatHistorySync();
			db = new DatabaseHandler(getApplicationContext());
			db.resetCallHistoryTable();
			db = new DatabaseHandler(getApplicationContext());
			db.resetContactImageTable();

			AccessToken accessToken = null;
			try {
				accessToken = AccountKit.getCurrentAccessToken();
			} catch (Exception e) {
				e.printStackTrace();
			}

			Toast.makeText(
					this,
					"Going to facebook authentication now.",
					Toast.LENGTH_LONG)
					.show();



			doFinish();

		} else {
			Toast.makeText(
					this,
					"Old data is not found from previous install.",
					Toast.LENGTH_LONG)
					.show();
			doFinish();
		}
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
			Toast.makeText(
					this,
					"Getting Access Token.",
					Toast.LENGTH_LONG)
					.show();
			try {
				accessToken = AccountKit.getCurrentAccessToken();
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (accessToken != null) {
				//Handle Returning User
                Toast.makeText(
                        this,
                        "Got Access Token.",
                        Toast.LENGTH_LONG)
                        .show();
				if(isDevelopment) {
					Intent i = new Intent(this, DisplayNameReg.class);
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					i.putExtra("authtoken", accessToken.getToken());
					startActivity(i);
					finish();
				} else {
					UserFunctions fn = new UserFunctions(getApplicationContext());
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

					/*Intent i = new Intent(SplashScreen.this, Invite_Friends.class);
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					i.putExtra("authtoken", authtoken);
					startActivity(i);
					finish();*/


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
