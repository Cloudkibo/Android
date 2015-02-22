package com.cloudkibo.webrtc.interfaces;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sojharo on 1/22/2015.
 */
public interface Command {
    void execute(String peerId, JSONObject payload) throws JSONException;
}
