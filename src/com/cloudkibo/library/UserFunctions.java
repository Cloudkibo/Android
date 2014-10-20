package com.cloudkibo.library;


import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import android.content.Context;


public class UserFunctions {
	
	
    /////////////////////////////////////////////////////////////////////
    // VARIABLES                                                       //
    /////////////////////////////////////////////////////////////////////

    private JSONParser jsonParser;

    //URL of the NODEJS API
    private static String loginURL = "https://www.cloudkibo.com/loginApp";
    private static String registerURL = "https://www.cloudkibo.com/registerApp";
    private static String forpassURL = "https://www.cloudkibo.com/forgotPasswordRequest";
    private static String chgpassURL = "https://www.cloudkibo.com/learn2crack_login_api/";
    private static String getContactsURL = "https://www.cloudkibo.com/getContactsList";
    
    
    
    
    
    
    
	/////////////////////////////////////////////////////////////////////
	// Constructor                                                     //
	/////////////////////////////////////////////////////////////////////

    public UserFunctions(){
        jsonParser = new JSONParser();
    }
    
    
    
    
    
    

    /**
     * Function to Login
     **/

    public JSONObject loginUser(String username, String password){
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", password));
        JSONObject json = jsonParser.getJSONFromUrl(loginURL, params);
        return json;
    }

    
    
    
    
    
    

    /**
     * Function to get contacts list
     **/

    public JSONObject getContacts(){
        
        JSONObject json = jsonParser.getJSONFromUrl(getContactsURL);
        return json;
        
    }
    
    
    
    
    
    
    

    /**
     * Function to change password
     **/

    public JSONObject chgPass(String newpas, String email){
        List<NameValuePair> params = new ArrayList<NameValuePair>();


        params.add(new BasicNameValuePair("newpas", newpas));
        params.add(new BasicNameValuePair("email", email));
        JSONObject json = jsonParser.getJSONFromUrl(chgpassURL, params);
        return json;
    }



    
    
    


    /**
     * Function to reset the password
     **/

    public JSONObject forPass(String username){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", username));
        JSONObject json = jsonParser.getJSONFromUrl(forpassURL, params);
        return json;
    }

    
    
    
    


     /**
      * Function to  Register
      **/
    public JSONObject registerUser(String fname, String lname, String email, String uname, String password, String phone){
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("fname", fname));
        params.add(new BasicNameValuePair("lname", lname));
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("username", uname));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("phone", phone));
        JSONObject json = jsonParser.getJSONFromUrl(registerURL,params);
        return json;
    }
    
    
    
    
    
    
    
    /**
     * Function get Login status
     * */
    public boolean isUserLoggedIn(Context context){
        DatabaseHandler db = new DatabaseHandler(context);
        int count = db.getRowCount();
        if(count > 0){
            // user logged in
            return true;
        }
        return false;
    }
    

    
    
    
    

    /**
     * Function to logout user
     * Resets the temporary data stored in SQLite Database
     * */
    public boolean logoutUser(Context context){
        DatabaseHandler db = new DatabaseHandler(context);
        db.resetTables();
        return true;
    }

}

