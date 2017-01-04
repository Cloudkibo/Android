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



}
