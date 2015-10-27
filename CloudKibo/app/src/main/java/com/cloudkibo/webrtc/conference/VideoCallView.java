package com.cloudkibo.webrtc.conference;

/**
 * Created by sojharo on 8/27/2015.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;
import android.widget.Toast;

import com.cloudkibo.R;
import com.cloudkibo.file.filechooser.utils.Base64;
import com.cloudkibo.file.filechooser.utils.FileUtils;
import com.cloudkibo.socket.BoundServiceListener;
import com.cloudkibo.socket.SocketService;
import com.cloudkibo.webrtc.filesharing.FileConnection;
import com.cloudkibo.webrtc.filesharing.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.MediaStream;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VideoCallView extends Activity implements WebRtcClient.RtcListener {
    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String AUDIO_CODEC_OPUS = "opus";
    // Local preview screen position before call is connected.
    private static final int LOCAL_X_CONNECTING = 25;//0;
    private static final int LOCAL_Y_CONNECTING = 25;//0;
    private static final int LOCAL_WIDTH_CONNECTING = 50;//100;
    private static final int LOCAL_HEIGHT_CONNECTING = 50;//100;
    // Local preview screen position after call is connected.
    private static final int LOCAL_X_CONNECTED = 72;
    private static final int LOCAL_Y_CONNECTED = 72;
    private static final int LOCAL_WIDTH_CONNECTED = 25;
    private static final int LOCAL_HEIGHT_CONNECTED = 25;
    // Local preview screen position if video is hidden
    private static final int LOCAL_X_HIDDEN = 99;
    private static final int LOCAL_Y_HIDDEN = 99;
    private static final int LOCAL_WIDTH_HIDDEN = 1;
    private static final int LOCAL_HEIGHT_HIDDEN = 1;
    // Remote video screen position
    private static final int REMOTE_X = 0;
    private static final int REMOTE_Y = 0;
    private static final int REMOTE_WIDTH = 100;
    private static final int REMOTE_HEIGHT = 100;
    private VideoRendererGui.ScalingType scalingType = VideoRendererGui.ScalingType.SCALE_ASPECT_FILL;
    private GLSurfaceView vsv;
    private VideoRenderer.Callbacks localRender;
    private VideoRenderer.Callbacks remoteRender;
    private VideoRenderer.Callbacks screenRender;
    private MediaStream localStream;
    private WebRtcClient client;
    private String callerId;
    private int currentId;
    private boolean isStarted = false;

    SocketService socketService;
    boolean isBound = false;

    boolean localVideoShared = false;
    boolean localAudioShared = true;

    String filePath;
    /*String fileNameToSave;
    int numberOfChunksInFileToSave;
    int numberOfChunksReceived;
    int sizeOfFileToSave;
    int chunkNumberToRequest;*/
    //ArrayList<Byte> fileBytesArray = new ArrayList<Byte>();

    private HashMap<String, String> user;
    private String room;

    private ImageButton tglVideo;
    private ImageButton tglAudio;
    private ImageButton sendFile;

    private static final int REQUEST_CHOOSER = 11050;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        user = (HashMap) getIntent().getExtras().get("user");
        room = getIntent().getExtras().getString("room");

        Intent i = new Intent(this, SocketService.class);
        i.putExtra("user", user);
        i.putExtra("room", room);
        startService(i);
        bindService(i, socketConnection, Context.BIND_AUTO_CREATE);

        getWindow().addFlags(
                LayoutParams.FLAG_FULLSCREEN
                        | LayoutParams.FLAG_KEEP_SCREEN_ON
                        | LayoutParams.FLAG_DISMISS_KEYGUARD
                        | LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.main);

        vsv = (GLSurfaceView) findViewById(R.id.glview_call);
        vsv.setPreserveEGLContextOnPause(true);
        vsv.setKeepScreenOn(true);
        VideoRendererGui.setView(vsv, new Runnable() {
            @Override
            public void run() {
                GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
                init();
            }
        });

        // local and remote render
        remoteRender = VideoRendererGui.create(
                REMOTE_X, REMOTE_Y,
                REMOTE_WIDTH, REMOTE_HEIGHT, scalingType, false);
        screenRender = VideoRendererGui.create(
                LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
                LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING, scalingType, true);
        localRender = VideoRendererGui.create(
                LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
                LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING, scalingType, true);

        tglVideo = (ImageButton) findViewById(R.id.toggleVideo);

        tglVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(isStarted) {
                        localVideoShared = !localVideoShared;
                        JSONObject payload = new JSONObject();
                        payload.put("username", user.get("username"));
                        payload.put("type", "video");
                        payload.put("action", localVideoShared);
                        payload.put("id", Integer.toString(currentId));
                        socketService.sendConferenceMsgGeneric("conference.stream", payload);
                        if (localVideoShared) {
                            VideoRendererGui.update(localRender,
                                    LOCAL_X_CONNECTED, LOCAL_Y_CONNECTED,
                                    LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED,
                                    scalingType);
                        } else {
                            VideoRendererGui.update(localRender,
                                    LOCAL_X_HIDDEN, LOCAL_Y_HIDDEN,
                                    LOCAL_WIDTH_HIDDEN, LOCAL_HEIGHT_HIDDEN,
                                    scalingType);
                        }
                    }
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }
        });

        tglAudio = (ImageButton) findViewById(R.id.toggleAudio);

        tglAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isStarted) {
                    localAudioShared = !localAudioShared;
                    localStream.audioTracks.get(0).setEnabled(localAudioShared);
                }
            }
        });

        sendFile = (ImageButton) findViewById(R.id.sendFile);

        sendFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isStarted) {
                    Intent getContentIntent = FileUtils.createGetContentIntent();

                    Intent intent = Intent.createChooser(getContentIntent, "Select a file");
                    startActivityForResult(intent, REQUEST_CHOOSER);
                }
            }
        });

        final Intent intent = getIntent();
        final String action = intent.getAction();

        if (Intent.ACTION_VIEW.equals(action)) {
            Log.e("INTENT", action);
            final List<String> segments = intent.getData().getPathSegments();
            callerId = segments.get(0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHOOSER:
                if (resultCode == RESULT_OK) {

                    final Uri uri = data.getData();

                    // Get the File path from the Uri
                    filePath = FileUtils.getPath(this, uri);

                    // Alternatively, use FileUtils.getFile(Context, Uri)
                    if (filePath != null && FileUtils.isLocal(filePath)) {

                        try {
                            JSONObject metadata = new JSONObject();

                            metadata.put("eventName", "data_msg");
                            metadata.put("data", (new JSONObject()).put("file_meta", Utility.getFileMetaData(filePath)));

                            client.sendDataChannelMessage(new DataChannel.Buffer(Utility.toByteBuffer(metadata.toString()), false));

                            sendChat("You have received a file. Download and Save it.");

                            Log.w("FILE_TRANSFER", "Sending file meta to peer");
                        }catch(JSONException e){
                            e.printStackTrace();
                        }

                    }
                }
                break;
        }
    }

    private void init() {
        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);
        PeerConnectionParameters params = new PeerConnectionParameters(
                true, false, displaySize.x, displaySize.y, 30, 1, VIDEO_CODEC_VP9, true, 1, AUDIO_CODEC_OPUS, true);

        client = new WebRtcClient(this, params, VideoRendererGui.getEGLContext());

        startCam();

    }

    @Override
    public void onPause() {
        super.onPause();
        vsv.onPause();
        if(client != null) {
            client.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        vsv.onResume();
        if(client != null) {
            client.onResume();
        }
    }

    @Override
    public void onDestroy() {
        if(client != null) {
            client.onDestroy();
        }
        super.onDestroy();
    }

    public void startCam() {
        // Camera settings

        Log.w("VideoCallView", "inside startCam going to call start()");
        client.start(user.get("username"));

    }

    public void joinRoomForCall(){
        Log.w("VideoCallView", "inside joinRoomForCall");
        try {
            JSONObject data = new JSONObject();
            data.put("room", room);
            data.put("username", user.get("username"));

            socketService.joinConference(data);
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    public void sendMessage(String to, JSONObject payload){
        try {
            payload.put("to", to);
            payload.put("by", Integer.toString(currentId));
            payload.put("username", user.get("username"));
            socketService.sendConferenceMsg(payload);
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    public void sendChat(String msg){
        try {
            JSONObject payload = new JSONObject();
            payload.put("message", msg);
            payload.put("username", user.get("username"));
            socketService.sendConferenceMsgGeneric("conference.chat", payload);
        }catch(JSONException e){
            e.printStackTrace();
        }
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
        Log.w("VideoCallView", "inside onLocalStream");
        this.localStream = localStream;
        localStream.videoTracks.get(0).addRenderer(new VideoRenderer(localRender));
        VideoRendererGui.update(localRender,
                LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
                LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING,
                scalingType);

       joinRoomForCall();

    }

    @Override
    public void onAddRemoteStream(MediaStream remoteStream, int endPoint, Boolean screenShare) {
        // todo handle the case when multiple peers are there.. for now it shows them on interval basis
        if(!screenShare)
            remoteStream.videoTracks.get(0).addRenderer(new VideoRenderer(remoteRender));
        else
            remoteStream.videoTracks.get(0).addRenderer(new VideoRenderer(screenRender));

        VideoRendererGui.update(remoteRender,
                REMOTE_X, REMOTE_Y,
                REMOTE_WIDTH, REMOTE_HEIGHT, scalingType);
        VideoRendererGui.update(localRender,
                LOCAL_X_HIDDEN, LOCAL_Y_HIDDEN,
                LOCAL_WIDTH_HIDDEN, LOCAL_HEIGHT_HIDDEN,
                scalingType);
        if(screenShare){
            VideoRendererGui.update(screenRender,
                    REMOTE_X, REMOTE_Y,
                    REMOTE_WIDTH, REMOTE_HEIGHT, VideoRendererGui.ScalingType.SCALE_ASPECT_FIT);
        }
        isStarted = true;
    }

    @Override
    public void onRemoveRemoteStream(int endPoint) {
        // todo handle it for multiple peers and make UI cleaner
        isStarted = false;
        VideoRendererGui.update(localRender,
                LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
                LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING,
                scalingType);
    }

    public void toggleRemoteVideo(boolean action){
        if(action) {
            VideoRendererGui.update(remoteRender,
                    REMOTE_X, REMOTE_Y,
                    REMOTE_WIDTH, REMOTE_HEIGHT, scalingType);
        } else {
            VideoRendererGui.update(remoteRender,
                    0, 0,
                    1, 1, scalingType);
        }
    }

    public void gotDataChannelMessage(DataChannel.Buffer buffer){
       /* Log.w("FILE_TRANSFER", "Data Channel message received");

        ByteBuffer data = buffer.data;
        final byte[] bytes = new byte[ data.capacity() ];
        data.get(bytes);

        final File file = new File(filePath);

        if(buffer.binary){

            String strData = new String( bytes );
            Log.w("FILE_TRANSFER", strData);

            runOnUiThread(new Runnable(){
                public void run() {
                    for(int i=0; i<bytes.length; i++)
                        fileBytesArray.add(bytes[i]);

                    if (numberOfChunksReceived % Utility.getChunksPerACK() == (Utility.getChunksPerACK() - 1)
                            || numberOfChunksInFileToSave == (numberOfChunksReceived + 1)) {
                        if (numberOfChunksInFileToSave > numberOfChunksReceived) {
                            chunkNumberToRequest += Utility.getChunksPerACK();
                            requestChunk();
                        } else {
                            new AlertDialog.Builder(getApplicationContext())
                                    .setTitle("File Transfer Completed")
                                    .setMessage(fileNameToSave + " : " + FileUtils.getReadableFileSize(sizeOfFileToSave))
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            if (fileBytesArray.size() > 0) {
                                                byte[] fileBytes = new byte[fileBytesArray.size()];
                                                for (int i = 0; i < fileBytesArray.size(); i++) {
                                                    fileBytes[i] = fileBytesArray.get(i);
                                                }
                                                if (Utility.convertByteArrayToFile(fileBytes, fileNameToSave)) {
                                                    Toast.makeText(getApplicationContext(),
                                                            "File stored in Downloads", Toast.LENGTH_SHORT)
                                                            .show();
                                                } else {
                                                    Toast.makeText(getApplicationContext(),
                                                            "Some error caused storage failure. You must have SD card.", Toast.LENGTH_SHORT)
                                                            .show();
                                                }
                                            }
                                        }
                                    })
                                    .setNegativeButton("Remove", null).show();
                        }
                    }

                    numberOfChunksReceived++;
                }
            });

        }
        else {

            runOnUiThread(new Runnable() {
                public void run() {
                    String strData = new String(bytes);

                    Log.w("FILE_TRANSFER", strData);

                    try {

                        JSONObject jsonData = new JSONObject(strData);

                        if (jsonData.getJSONObject("data").has("file_meta")) {

                            fileNameToSave = jsonData.getJSONObject("data").getJSONObject("file_meta").getString("name");
                            sizeOfFileToSave = jsonData.getJSONObject("data").getJSONObject("file_meta").getInt("size");
                            numberOfChunksInFileToSave = (int) Math.ceil(sizeOfFileToSave / Utility.getChunkSize());
                            numberOfChunksReceived = 0;
                            chunkNumberToRequest = 0;
                            fileBytesArray = new ArrayList<Byte>();

                            new AlertDialog.Builder(getApplicationContext())
                                    .setTitle("File offered")
                                    .setMessage(fileNameToSave + " : " + FileUtils.getReadableFileSize(sizeOfFileToSave))
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setPositiveButton("Accept", new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            Toast.makeText(getApplicationContext(),
                                                    "Receiving file...", Toast.LENGTH_SHORT)
                                                    .show();
                                            if (Utility.isFreeSpaceAvailableForFileSize(sizeOfFileToSave)) {
                                                Log.w("FILE_TRANSFER", "Free space is available for file save, requesting chunk now");
                                                requestChunk();
                                            } else {
                                                Log.w("FILE_TRANSFER", "Need more space to save this file");
                                                Toast.makeText(getApplicationContext(),
                                                        "Need more free space to save this file", Toast.LENGTH_SHORT)
                                                        .show();
                                            }
                                        }
                                    })
                                    .setNegativeButton("Reject", null).show();


                        } else if (jsonData.getJSONObject("data").has("kill")) {

                        } else if (jsonData.getJSONObject("data").has("ok_to_download")) {

                        } else {

                            boolean isBinaryFile = true;

                            int chunkNumber = jsonData.getJSONObject("data").getInt("chunk");

                            Log.w("FILE_TRANSFER", "Chunk Number " + chunkNumber);
                            if (chunkNumber % Utility.getChunksPerACK() == 0) {
                                for (int i = 0; i < Utility.getChunksPerACK(); i++) {

                                    if (file.length() < Utility.getChunkSize()) {
                                        ByteBuffer byteBuffer = ByteBuffer.wrap(Utility.convertFileToByteArray(file));
                                        DataChannel.Buffer buf = new DataChannel.Buffer(byteBuffer, isBinaryFile);

                                        Log.w("FILE_TRANSFER", "File Smaller than chunk size condition");

                                        client.sendDataChannelMessage(buf);
                                        break;
                                    }

                                    Log.w("FILE_TRANSFER", "File Length " + file.length());
                                    Log.w("FILE_TRANSFER", "Ceiling " + Math.ceil(file.length() / Utility.getChunkSize()));
                                    if ((chunkNumber + i) >= Math.ceil(file.length() / Utility.getChunkSize())) {
                                        Log.w("FILE_TRANSFER", "Came into math ceiling condition");
                                        //break;
                                    }

                                    int upperLimit = (chunkNumber + i + 1) * Utility.getChunkSize();

                                    if (upperLimit > (int) file.length()) {
                                        upperLimit = (int) file.length() - 1;
                                    }

                                    int lowerLimit = (chunkNumber + i) * Utility.getChunkSize();
                                    Log.w("FILE_TRANSFER", "Limits: " + lowerLimit + " " + upperLimit);

                                    if (lowerLimit > upperLimit)
                                        break;

                                    ByteBuffer byteBuffer = ByteBuffer.wrap(Utility.convertFileToByteArray(file), lowerLimit, upperLimit - lowerLimit);
                                    DataChannel.Buffer buf = new DataChannel.Buffer(byteBuffer, isBinaryFile);

                                    client.sendDataChannelMessage(buf);
                                    Log.w("FILE_TRANSFER", "Chunk has been sent");
                                }
                            }
                        }

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });

        }*/
    }
/*
    public void requestChunk(){
        Log.w("FILERECEIVE", "Requesting CHUNK: "+ chunkNumberToRequest);
        JSONObject request_chunk = new JSONObject();
        try {
            request_chunk.put("eventName", "request_chunk");
            JSONObject request_data = new JSONObject();
            request_data.put("chunk", chunkNumberToRequest);
            request_data.put("browser", "chrome"); // This chrome is hardcoded for testing purpose
            request_chunk.put("data", request_data);
            client.sendDataChannelMessage(new DataChannel.Buffer(Utility.toByteBuffer(request_chunk.toString()), false));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
*/
    private ServiceConnection socketConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SocketService.SocketBinder binder = (SocketService.SocketBinder) service;
            socketService = binder.getService();
            isBound = true;

            binder.setListener(new BoundServiceListener() {

                @Override
                public void receiveSocketMessage(String type, String body) {
                    if(type.equals("conference_id")){
                        currentId = Integer.parseInt(body);
                        client.setCurrentId(currentId);
                    }
                }

                @Override
                public void receiveSocketArray(String type, JSONArray body) {

                }

                @Override
                public void receiveSocketJson(String type, JSONObject body) {
                    client.messageReceived(type, body);
                }
            });
        }

    };
}