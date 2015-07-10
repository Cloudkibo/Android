/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package io.cordova.hellocordova;

import android.os.Bundle;
import android.webkit.JavascriptInterface;

import org.apache.cordova.*;

public class CordovaApp extends CordovaActivity
{
	
	private String username;
	private String peer;
	private String lastmessage;
	private String room;
	private String id;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        super.init();
        
        username = getIntent().getExtras().getString("username");
        peer = getIntent().getExtras().getString("peer");
        lastmessage = getIntent().getExtras().getString("lastmessage");
        room = getIntent().getExtras().getString("room");
        id = getIntent().getExtras().getString("_id");
        
        super.appView.addJavascriptInterface(this, "KiboJava");
        
        // Set by <content src="index.html" /> in config.xml
        loadUrl(launchUrl);
    }
    
    @JavascriptInterface
    public String getUsername(){
    	return username;
    }
    
    @JavascriptInterface
    public String getPeer(){
    	return peer;
    }
    
    @JavascriptInterface
    public String getLastMessage(){
    	return lastmessage;
    }
    
    @JavascriptInterface
    public String getRoom(){
    	return room;
    }
    
    @JavascriptInterface
    public String getId(){
    	return id;
    }
}
