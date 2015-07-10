package com.cloudkibo.library;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.cloudkibo.library.AccountGeneral.KEY_MSG;
import static com.cloudkibo.library.AccountGeneral.KEY_STATUS;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
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

import org.json.JSONObject;

/**
 *
 */
public class ForgotPassword extends Activity
{

    Button btnSubmit;
    EditText txtEmail;

    public static final String KEY_ERROR_MESSAGE = "ERR_MSG";

    public final static String PARAM_USER_PASS = "USER_PASS";

    /* (non-Javadoc)
     * @see com.chatt.custom.CustomActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgotpassword);


        txtEmail = (EditText) findViewById(id.editEmailAddress);

        btnSubmit = (Button) findViewById(id.btnSubmit);


        /**
         *
         **/
        btnSubmit.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                if ((!txtEmail.getText().toString().equals(""))) {
                    NetAsync(view);
                }else {
                    Toast.makeText(getApplicationContext(),
                            "Email address is required",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });


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
            nDialog = new ProgressDialog(ForgotPassword.this);
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

        String email;





        // PRE-EXECUTE

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            txtEmail = (EditText) findViewById(id.editEmailAddress);
            email = txtEmail.getText().toString();
            pDialog = new ProgressDialog(ForgotPassword.this);
            pDialog.setTitle("Contacting CloudKibo");
            pDialog.setMessage("Sending request ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }






        // DO THIS WORK IN BACKGROUND

        @Override
        protected Intent doInBackground(String... args) {

            UserFunctions userFunction = new UserFunctions();

            Bundle data = new Bundle();

            try {

                JSONObject response = userFunction.forgotPass(email);

                data.putString(KEY_STATUS, response.getString(KEY_STATUS));
                data.putString(KEY_MSG, response.getString(KEY_MSG));

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
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }



    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

}
