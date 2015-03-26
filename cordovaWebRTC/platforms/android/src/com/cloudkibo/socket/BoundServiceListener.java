package com.cloudkibo.socket;

import org.json.JSONArray;
import org.json.JSONObject;

public interface BoundServiceListener {
	
	public void receiveSocketMessage(String type, String body);
	
	public void receiveSocketArray(String type, JSONArray body);

}
