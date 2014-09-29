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

import com.cloudkibo.library.DatabaseHandler;
import com.cloudkibo.library.UserFunctions;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Register extends Activity {


    /**
     *  JSON Response node names.
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

        /**
         * Defining all layout items
         **/
            inputFirstName = (EditText) findViewById(R.id.firstNameText);
            inputLastName = (EditText) findViewById(R.id.lastNameText);
            inputUsername = (EditText) findViewById(R.id.userName);
            inputEmail = (EditText) findViewById(R.id.emailIdtxt);
            inputPassword = (EditText) findViewById(R.id.passwordText);
            inputPhone = (EditText) findViewById(R.id.phoneNumberText);
            btnRegister = (Button) findViewById(R.id.submitBtn);
            //registerErrorMsg = (TextView) findViewById(R.id.register_error);
    
 
            
        /**
         * Button which Switches back to the login screen on clicked
         **/

                Button login = (Button) findViewById(R.id.backToLogin);
                login.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        Intent myIntent = new Intent(view.getContext(), Login.class);
                        startActivityForResult(myIntent, 0);
                        finish();
                    }

                });
 
                /**
                 * Register Button click event.
                 * A Toast is set to alert when the fields are empty.
                 * Another toast is set to alert Username must be 5 characters.
                 **/

                btnRegister.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (  ( !inputUsername.getText().toString().equals("")) && ( !inputPassword.getText().toString().equals("")) && ( !inputFirstName.getText().toString().equals("")) && ( !inputLastName.getText().toString().equals("")) && ( !inputEmail.getText().toString().equals("")) )
                        {
                            if ( inputUsername.getText().toString().length() > 2 ){
                            NetAsync(view);

                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(),
                                        "Username should be minimum 3 characters", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),
                                    "One or more fields are empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
               
         
    
    }// end onCreate()
    
    public void NetAsync(View view){
        new NetCheck().execute();
    }
    
    /**
     * Async Task to check whether internet connection is working
     **/

    private class NetCheck extends AsyncTask<String,String,Boolean>
    {
        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            nDialog = new ProgressDialog(Register.this);
            nDialog.setMessage("Loading..");
            nDialog.setTitle("Checking Network");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(true);
            //nDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... args){

			/**
			 * Gets current device state and checks for working internet connection by trying Google.
			 **/
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
                try {
                    URL url = new URL("http://www.google.com");
                    HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
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
        protected void onPostExecute(Boolean th){

            if(th == true){
                nDialog.dismiss();
                new ProcessRegister().execute();
            }
            else{
                nDialog.dismiss();
                Toast.makeText(getApplicationContext(),
                        "Could not connect to Internet", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private class ProcessRegister extends AsyncTask<String, String, JSONObject> {

/**
 * Defining Process dialog
 **/
        private ProgressDialog pDialog;

        String email,password,firstname,lastname,username, phone;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            inputUsername = (EditText) findViewById(R.id.userName);
            inputPassword = (EditText) findViewById(R.id.passwordText);
               firstname = inputFirstName.getText().toString();
               lastname = inputLastName.getText().toString();
                email = inputEmail.getText().toString();
                username= inputUsername.getText().toString();
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
        protected JSONObject doInBackground(String... args) {


        UserFunctions userFunction = new UserFunctions();
        JSONObject json = userFunction.registerUser(firstname, lastname, email, username, password, phone);

            return json;


        }
       @Override
        protected void onPostExecute(JSONObject json) {
       /**
        * Checks for success message.
        **/
                try {
                    if (json.getString(KEY_SUCCESS) != null) {
                    	String res = json.getString(KEY_SUCCESS);

                        if(res.equals("success")){
                            pDialog.setTitle("Getting Data");
                            pDialog.setMessage("Loading Info");

                            registerErrorMsg.setText("Successfully Registered");


                            DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                            JSONObject json_user = json.getJSONObject("user");

                            /**
                             * Removes all the previous data in the SQlite database
                             **/

                            UserFunctions logout = new UserFunctions();
                            logout.logoutUser(getApplicationContext());
                            db.addUser(json_user.getString(KEY_FIRSTNAME),json_user.getString(KEY_LASTNAME),json_user.getString(KEY_EMAIL),json_user.getString(KEY_USERNAME),json_user.getString(KEY_UID),json_user.getString(KEY_CREATED_AT));
                            /**
                             * Stores registered data in SQlite Database
                             * Launch Registered screen
                             **/

                            Intent registered = new Intent(getApplicationContext(), Register.class);

                            /**
                             * Close all views before launching Registered screen
                            **/
                            registered.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            pDialog.dismiss();
                            startActivity(registered);


                              finish();
                        }

                        else if (false){ // Tackle it later
                            pDialog.dismiss();
                            registerErrorMsg.setText("User already exists");
                        }
                        else if (false){ // Tackle it later
                            pDialog.dismiss();
                            registerErrorMsg.setText("Invalid Email id");
                        }

                    }


                        else{
                        pDialog.dismiss();

                            registerErrorMsg.setText("Error occured in registration");
                        }

                } catch (JSONException e) {
                    e.printStackTrace();


                }
            }}
    
}




