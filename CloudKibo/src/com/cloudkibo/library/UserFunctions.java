package com.cloudkibo.library;


import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cloudkibo.database.DatabaseHandler;


import android.content.Context;


public class UserFunctions {
	
	
    /////////////////////////////////////////////////////////////////////
    // VARIABLES                                                       //
    /////////////////////////////////////////////////////////////////////

    private ConnectionManager connection;

    //URL of the NODEJS API
    private static String loginURL = "https://www.cloudkibo.com/auth/local";
    private static String registerURL = "https://www.cloudkibo.com/api/users/";
    private static String userDataURL = "https://www.cloudkibo.com/api/users/me";
    private static String saveChatURL = "https://www.cloudkibo.com/api/userchat/save";
    private static String getChatURL = "https://www.cloudkibo.com/api/userchat";
    private static String markChatReadURL = "https://www.cloudkibo.com/api/userchat/markasread";
    private static String saveContactURL = "https://www.cloudkibo.com/api/contactslist/addbyusername";
    private static String forpassURL = "https://www.cloudkibo.com/forgotPasswordRequest";
    private static String chgpassURL = "https://www.cloudkibo.com/learn2crack_login_api/";
    private static String getContactsURL = "https://www.cloudkibo.com/api/contactslist/";
    
    
    
    
    
    
    
	/////////////////////////////////////////////////////////////////////
	// Constructor                                                     //
	/////////////////////////////////////////////////////////////////////

    public UserFunctions(){
        connection = new ConnectionManager();
    }
    
    
    
    
    
    

    /**
     * Function to Login
     * @throws Exception 
     **/

    public String loginUser(String username, String password) throws Exception{
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", password));
        String authtoken = connection.getTokenFromServer(loginURL, params);
        return authtoken;
    }

    
    
    
    
    
    
    

    /**
     * Function to change password
     * @throws Exception 
     **/

    public String chgPass(String newpas, String email) throws Exception{
        List<NameValuePair> params = new ArrayList<NameValuePair>();


        params.add(new BasicNameValuePair("newpas", newpas));
        params.add(new BasicNameValuePair("email", email));
        String result = connection.getTokenFromServer(chgpassURL, params);
        return result;
        // FIX IT LATER IN THE FUTURE
    }

    
    
    
    


     /**
      * Function to Register
      * @throws Exception 
      **/
    public String registerUser(String fname, String lname, String email, String uname, String password, String phone) throws Exception{
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("firstname", fname));
        params.add(new BasicNameValuePair("lastname", lname));
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("username", uname));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("phone", phone));
        String authtoken = connection.getTokenFromServer(registerURL,params);
        return authtoken;
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







	public JSONObject getUserData(String authtoken) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        JSONObject userdata = connection.getDataFromServer(userDataURL, authtoken);
        return userdata;
	}
	
	
	
	
	
	public JSONArray getContactsList(String authtoken) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		JSONArray contactslist = connection.getArrayFromServer(getContactsURL, authtoken);
        return contactslist;
	}
	
	public JSONObject saveChat(List<NameValuePair> params, String authtoken) {
		JSONObject userchatresponse = connection.sendObjectToServer(saveChatURL, authtoken, params);
        return userchatresponse;
	}
	
	public JSONObject markChatAsRead(String user1, String user2, String authtoken) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("user1", user1));
		params.add(new BasicNameValuePair("user2", user2));
		JSONObject userchatresponse = connection.sendObjectToServer(markChatReadURL, authtoken, params);
        return userchatresponse;
	}
	
	public JSONObject getUserChat(String user1, String user2, String authtoken) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("user1", user1));
		params.add(new BasicNameValuePair("user2", user2));
		JSONObject response = connection.sendObjectToServer(getChatURL, authtoken, params);
		return response;
	}
	
	public JSONObject saveContact(String username, String authtoken) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("searchusername", username));
		JSONObject response = connection.sendObjectToServer(saveContactURL, authtoken, params);
        return response;
	}

}

