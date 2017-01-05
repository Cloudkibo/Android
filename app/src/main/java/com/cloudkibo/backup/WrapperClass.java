package com.cloudkibo.backup;


import com.cloudkibo.library.ConnectionManager;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//BaseActivity is the google drive API client class

public class WrapperClass extends BaseActivity {

    //connection to establish with google drive / storage
    private ConnectionManager connection;


    //Following will be the google drive / storage API urls.


    //constructor
    public WrapperClass(){
        connection = new ConnectionManager();
    }

    //Function to log in to storage point
    public String loginUser(String username, String password) throws Exception{
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", password));
        String loginURL =  "";
        String authtoken = connection.getTokenFromServer(loginURL, params);
        return authtoken;
    }

    //Function to upload chat
    public JSONObject uploadChat(JSONArray chat, String authtoken){
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        try{
            //code to add chat to List params
        }
        catch (Exception e){
            e.printStackTrace();
        }
        String googleDriveUrl = "";
//        JSONObject response = connection.sendObjectToServer(googleDriveUrl, params, authtoken);
        return null;
    }

    //function to upload File
    public JSONObject uploadFile (File f, String authtoken){
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        try{
            //code to add file to params
        }
        catch (Exception e) {
            e.printStackTrace();
        }

//        JSONObject response = connection.sendFileToServer(googleDriveUrl, params, authtoken);
//        return response;
        return null;
    }

    public JSONObject uploadGroupInfo(JSONArray groupinfo, String authtoken){
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        try{
            //code to add groupinfo to params
        }
        catch (Exception e){
            e.printStackTrace();
        }

//        JSONObject response = connection.sendObjectToServer(googleDriveUrl, params, authtoken);
//        return response;
        return null;
    }


}
