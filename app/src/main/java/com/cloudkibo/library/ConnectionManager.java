package com.cloudkibo.library;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cloudkibo.MainActivity;
import com.cloudkibo.SplashScreen;
import com.google.gson.Gson;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ConnectionManager {
	
	
    /////////////////////////////////////////////////////////////////////
    // VARIABLES                                                       //
    /////////////////////////////////////////////////////////////////////

    static InputStream is = null;
    static JSONObject jObj = null;
    static JSONArray jArr = null;
    static String json = "";
    
    
    
	/////////////////////////////////////////////////////////////////////
	// Constructor                                                     //
	/////////////////////////////////////////////////////////////////////

    public ConnectionManager() {

    }
    
    
    
    
    
    
	/////////////////////////////////////////////////////////////////////
	// Sending HTTP POST Request to URL                                //
	/////////////////////////////////////////////////////////////////////

    public String getTokenFromServer(String url, List<NameValuePair> params) throws Exception {
    	
    	String authtoken = null;
        // Making HTTP request
        try {
            // defaultHttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new UrlEncodedFormEntity(params));

            HttpResponse response = httpClient.execute(httpPost);

            String responseString = EntityUtils.toString(response.getEntity());
            if (response.getStatusLine().getStatusCode() != 200) {
                Log.w("Register", responseString);
                // try parse the string to a JSON object
                try {
                    jObj = new JSONObject(responseString);

                    if(jObj.has("errors")) {
                        jObj = jObj.getJSONObject("errors");

                        if(jObj.has("username")){
                            throw new Exception(jObj.getJSONObject("username").getString("message"));
                        }
                        if(jObj.has("phone")){
                            throw new Exception(jObj.getJSONObject("phone").getString("message"));
                        }
                        if(jObj.has("email")){
                            throw new Exception(jObj.getJSONObject("email").getString("message"));
                        }
                    }

                    if(jObj.has("message")){
                        throw new Exception(jObj.getString("message"));
                    }

                } catch (JSONException e) {
                    throw new Exception("JSON Error getTokenFromServer");
                }

                throw new Exception("Error signing-in");
            }

            User loggedUser = new Gson().fromJson(responseString, User.class);
            authtoken = loggedUser.token;

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
		}

        // return JSON String
        return authtoken;

    }
    
    public JSONObject getDataFromServer(String userDataURL, String authtoken) {
		
    	// Making HTTP request
        try {
            // defaultHttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(userDataURL);
            httpGet.addHeader("kibo-token", authtoken);

            HttpResponse httpResponse = httpClient.execute(httpGet);

            if(httpResponse.getStatusLine().getStatusCode() == 401){
                return null;
            }

            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
            Log.d("JSON", json);
        } catch (Exception e) {
            Log.d("Buffer Error", "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.d("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
        return jObj;
	}
    
    
    
    
    
    
    public JSONArray getArrayFromServer(String userDataURL, String authtoken) {
		
    	// Making HTTP request
        try {
            // defaultHttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(userDataURL);
            httpGet.addHeader("kibo-token", authtoken);

            HttpResponse httpResponse = httpClient.execute(httpGet);

            if(httpResponse.getStatusLine().getStatusCode() == 401){
                return null;
            }

            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
            Log.d("JSON", json);
        } catch (Exception e) {
            Log.d("Buffer Error", "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jArr = new JSONArray(json);
        } catch (JSONException e) {
            Log.d("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
        return jArr;
	}
    
    
    
    
    
    
    
    public JSONArray sendArrayToServer(String userDataURL, String authtoken, List<NameValuePair> params) {
		
    	// Making HTTP request
        try {
            // defaultHttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(userDataURL);
            httpPost.addHeader("kibo-token", authtoken);
            httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

            HttpResponse httpResponse = httpClient.execute(httpPost);

            if(httpResponse.getStatusLine().getStatusCode() == 401){
                return null;
            }

            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
            Log.d("JSON", json);
        } catch (Exception e) {
            Log.d("Buffer Error", "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jArr = new JSONArray(json);
        } catch (JSONException e) {
            Log.d("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
        return jArr;
	}
    
    
    
    
    
    
    public JSONObject sendObjectToServer(String userDataURL, String authtoken, List<NameValuePair> params) {
		
    	// Making HTTP request
        try {
            // defaultHttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(userDataURL);
            httpPost.addHeader("kibo-token", authtoken);
            httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

            HttpResponse httpResponse = httpClient.execute(httpPost);

            if(httpResponse.getStatusLine().getStatusCode() == 401){
                return null;
            }

            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            try {
				return new JSONObject().put("Error", "No Internet");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, HTTP.UTF_8), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
            Log.d("JSON", json);
        } catch (Exception e) {
            Log.d("Buffer Error", "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
            Log.d("Dayem", "Error parsing data " + jObj.toString());
        } catch (JSONException e) {
            Log.d("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
        return jObj;
	}



    public JSONArray sendObjectReturnArray(String userDataURL, String authtoken, List<NameValuePair> params) {

        // Making HTTP request
        try {
            // defaultHttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(userDataURL);
            httpPost.addHeader("kibo-token", authtoken);
            httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

            HttpResponse httpResponse = httpClient.execute(httpPost);

            if(httpResponse.getStatusLine().getStatusCode() == 401){
                return null;
            }

            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                return new JSONArray().put(0, new JSONObject().put("Error", "No Internet"));
            } catch (JSONException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
            Log.d("JSON", json);
        } catch (Exception e) {
            Log.d("Buffer Error", "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jArr = new JSONArray(json);
            Log.d("Dayem", "Error parsing data " + jObj.toString());
        } catch (JSONException e) {
            Log.d("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
        return jArr;
    }





    public JSONObject sendObjectToServerNoAuth(String userDataURL, List<NameValuePair> params) {

        // Making HTTP request
        try {
            // defaultHttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(userDataURL);
            httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

            HttpResponse httpResponse = httpClient.execute(httpPost);

            if(httpResponse.getStatusLine().getStatusCode() == 401){
                return null;
            }

            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                return new JSONObject().put("Error", "No Internet");
            } catch (JSONException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
            Log.d("JSON", json);
        } catch (Exception e) {
            Log.d("Buffer Error", "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.d("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
        return jObj;
    }


    public JSONObject sendJSONObjectToServer(String userDataURL, String authtoken, JSONObject jsonD) {

        // Making HTTP request
        try {
            // defaultHttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(userDataURL);
//            httpPost.addHeader("kibo-app-id", appId);
//            httpPost.addHeader("kibo-client-id", clientId);
//            httpPost.addHeader("kibo-app-secret", appSecret);
            httpPost.addHeader("kibo-token", authtoken);
            httpPost.setHeader("Content-type", "application/json");

            StringEntity params = new StringEntity(jsonD.toString(), HTTP.UTF_8);
            httpPost.setEntity(params);
            Log.d("Getme", jsonD.toString());

            HttpResponse httpResponse = httpClient.execute(httpPost);

            if(httpResponse.getStatusLine().getStatusCode() == 401){
                return null;
            }

            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                return new JSONObject().put("Error", "No Internet");
            } catch (JSONException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
            //Log.d("JSON", json);
        } catch (Exception e) {
            Log.d("Buffer Error", "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.d("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
        return jObj;
    }








    /////////////////////////////////////////////////////////////////////
	// Serializable User Class                                         //
	/////////////////////////////////////////////////////////////////////

    private class User implements Serializable {

        private String firstName;
        private String lastName;
        private String username;
        private String phone;
        public String token;


        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getSessionToken() {
            return token;
        }

        public void setSessionToken(String token) {
            this.token = token;
        }
    }
    
    
    private class ParseComError implements Serializable {
        int code;
        String error;
    }
    
    
}
