package com.cloudkibo.webrtc;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.cloudkibo.R;
import com.cloudkibo.webrtc.interfaces.RTCListener;

import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoRenderer;

import java.util.List;

/**
 * Created by sojharo on 1/22/2015.
 */

public class RTCActivity extends Activity implements RTCListener {

    private VideoStreamsView vsv;
    private WebRtcClient client;
    private String mSocketAddress;
    private String callerId;
    public String username;
    public String userid;
    public String room;

    @Override
    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        username = getIntent().getExtras().getString("username");
        userid = getIntent().getExtras().getString("_id");
        room = getIntent().getExtras().getString("room");

        mSocketAddress = "https://" + getResources().getString(R.string.host);
        mSocketAddress += (":"+getResources().getString(R.string.port)+"/");

        PeerConnectionFactory.initializeAndroidGlobals(this);

        // Camera display view
        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);
        vsv = new VideoStreamsView(this, displaySize);

        client = new WebRtcClient(this, mSocketAddress, username, userid, room);

        final Intent intent = getIntent();
        final String action = intent.getAction();

        setContentView(R.layout.rtc_activity);

        FrameLayout flRenderer = (FrameLayout) findViewById(R.id.gl_container);
        flRenderer.addView(vsv);


        if (Intent.ACTION_VIEW.equals(action)) {
            final List<String> segments = intent.getData().getPathSegments();
            callerId = segments.get(0);
        }
    }


    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    public void onPause() {
        super.onPause();
        vsv.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        vsv.onResume();
    }

    @Override
    public void onCallReady(String callId) {
        startCam();
    }

    public void startCam() {
        // Camera settings
        client.setCamera("front", "640", "480");
        client.start("android_test", true);
    }

    @Override
    public void onStatusChanged(final String newStatus) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), newStatus, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onLocalStream(MediaStream localStream) {
        localStream.videoTracks.get(0).addRenderer(new VideoRenderer(new VideoCallbacks(vsv, 0)));
    }

    @Override
    public void onAddRemoteStream(MediaStream remoteStream, int endPoint) {
        remoteStream.videoTracks.get(0).addRenderer(new VideoRenderer(new VideoCallbacks(vsv, endPoint)));
        vsv.shouldDraw[endPoint] = true;
    }

    @Override
    public void onRemoveRemoteStream(MediaStream remoteStream, int endPoint) {
        remoteStream.videoTracks.get(0).dispose();
        vsv.shouldDraw[endPoint] = false;
    }

    // Implementation detail: bridge the VideoRenderer.Callbacks interface to the
    // VideoStreamsView implementation.
    private class VideoCallbacks implements VideoRenderer.Callbacks {
        private final VideoStreamsView view;
        private final int stream;

        public VideoCallbacks(VideoStreamsView view, int stream) {
            this.view = view;
            this.stream = stream;
        }

        @Override
        public void setSize(final int width, final int height) {
            view.queueEvent(new Runnable() {
                public void run() {
                    view.setSize(stream, width, height);
                }
            });
        }

        @Override
        public void renderFrame(VideoRenderer.I420Frame frame) {
            view.queueFrame(stream, frame);
        }
    }
}
