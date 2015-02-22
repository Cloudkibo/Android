package com.cloudkibo.webrtc.interfaces;

import org.webrtc.MediaStream;

/**
 * Created by sojharo on 1/22/2015.
 */

public interface RTCListener {

    void onCallReady(String callId);

    void onStatusChanged(String newStatus);

    void onLocalStream(MediaStream localStream);

    void onAddRemoteStream(MediaStream remoteStream, int endPoint);

    void onRemoveRemoteStream(MediaStream remoteStream, int endPoint);

}
