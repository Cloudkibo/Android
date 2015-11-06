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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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

    private MediaProjectionManager projectionManager;
    private ImageReader imageReader;
    private MediaProjection mediaProjection;
    private Handler handler;
    private ImageView imgViewer;
    private boolean projectionStarted;
    private int projectionDisplayWidth;
    private int projectionDisplayHeight;
    private int imagesProduced;

    SocketService socketService;
    boolean isBound = false;

    FileTransferService fileTransferService;
    boolean fileServiceIsBound = false;

    boolean localVideoShared = false;
    boolean localAudioShared = true;

    private HashMap<String, String> user;
    private String room;

    private ImageButton tglVideo;
    private ImageButton tglAudio;
    private ImageButton sendFile;
    private ImageButton tglChat;
    private ImageButton tglScreen;

    private static final int REQUEST_CHOOSER = 11050;
    private static final int SCREEN_REQUEST_CODE = 10151;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        projectionManager = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        user = (HashMap) getIntent().getExtras().get("user");
        room = getIntent().getExtras().getString("room");

        Intent i = new Intent(this, SocketService.class);
        i.putExtra("user", user);
        i.putExtra("room", room);
        startService(i);
        bindService(i, socketConnection, Context.BIND_AUTO_CREATE);

        Intent intentFile = new Intent(getApplicationContext(), FileTransferService.class);
        bindService(intentFile, fileTransferConnection, Context.BIND_AUTO_CREATE);

        getWindow().addFlags(
                LayoutParams.FLAG_FULLSCREEN
                        | LayoutParams.FLAG_KEEP_SCREEN_ON
                        | LayoutParams.FLAG_DISMISS_KEYGUARD
                        | LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.main);

        handler = new Handler();

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

        tglChat = (ImageButton) findViewById(R.id.startChat);

        tglChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isStarted) {

                    Intent intent = new Intent(getApplicationContext(), GroupChat.class);
                    intent.putExtra("user", user);
                    intent.putExtra("room", room);
                    startActivity(intent);
                }
            }
        });

        tglScreen = (ImageButton) findViewById(R.id.startScreenSharing);

        tglScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleProjection();
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

    public void toggleProjection(){
        if (projectionStarted) {
            stopProjection();
        } else {
            startProjection();
        }
    }

    /**
     * Requests to start projection
     */
    public void startProjection() {
        startActivityForResult(projectionManager.createScreenCaptureIntent(), SCREEN_REQUEST_CODE);
    }

    /**
     * Request to stop projection
     */
    public void stopProjection() {
        projectionStarted = false;

        try {
            JSONObject payload = new JSONObject();
            payload.put("username", user.get("username"));
            payload.put("type", "screenAndroid");
            payload.put("action", projectionStarted);
            payload.put("id", Integer.toString(currentId));
            socketService.sendConferenceMsgGeneric("conference.stream", payload);
        }catch(JSONException e){
            e.printStackTrace();
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mediaProjection != null) {
                    mediaProjection.stop();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHOOSER:
                if (resultCode == RESULT_OK) {
                    final Uri uri = data.getData();
                    fileTransferService.sendFile(FileUtils.getPath(this, uri));
                }
                break;
            case SCREEN_REQUEST_CODE:
                mediaProjection = projectionManager.getMediaProjection(resultCode, data);
                if (mediaProjection != null) {

                    projectionStarted = true;

                    try {
                        JSONObject payload = new JSONObject();
                        payload.put("username", user.get("username"));
                        payload.put("type", "screenAndroid");
                        payload.put("action", projectionStarted);
                        payload.put("id", Integer.toString(currentId));
                        socketService.sendConferenceMsgGeneric("conference.stream", payload);
                    }catch(JSONException e){
                        e.printStackTrace();
                    }

                    // Initialize the media projection
                    DisplayMetrics metrics = getResources().getDisplayMetrics();
                    int density = metrics.densityDpi;
                    int flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY
                            | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;

                    Display display = getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);

                    projectionDisplayWidth = size.x;
                    projectionDisplayHeight = size.y;

                    imageReader = ImageReader.newInstance(projectionDisplayWidth, projectionDisplayHeight
                            , PixelFormat.RGBA_8888, 2);
                    mediaProjection.createVirtualDisplay("screencap",
                            projectionDisplayWidth, projectionDisplayHeight, density,
                            flags, imageReader.getSurface(), null, handler);
                    imageReader.setOnImageAvailableListener(new ImageAvailableListener(), handler);
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
        unbindService(fileTransferConnection);
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

    public void sendChatMessage(String msg){
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
        fileTransferService.onDataChannelMessage(buffer);
    }

    /**
     * DataTransferListener implementation
     */
    public void onDataReceive(String remotePeerId, final byte[] data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("CONFERENCE_SCREEN", "onDataReceive: " + data.length);
                if (data != null && data.length != 0) {
                    Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
                    Log.d("CONFERENCE_SCREEN", "Set Image : " + bm.toString());
                    imgViewer.setImageBitmap(bm);
                }
            }
        });
    }

    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = null;
            FileOutputStream fos = null;
            Bitmap bitmap = null;

            ByteArrayOutputStream stream = null;

            try {
                image = imageReader.acquireLatestImage();
                if (image != null) {
                    Image.Plane[] planes = image.getPlanes();
                    ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * projectionDisplayWidth;

                    // create bitmap
                    bitmap = Bitmap.createBitmap(projectionDisplayWidth + rowPadding / pixelStride,
                            projectionDisplayHeight, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);

                    stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 5, stream);

                    if(stream.toByteArray().length < Utility.getChunkSize()){
                        ByteBuffer byteBuffer = ByteBuffer.wrap(stream.toByteArray());
                        DataChannel.Buffer buf = new DataChannel.Buffer(byteBuffer, true);

                        Log.w("CONFERENCE_SCREEN", "Image size less than chunk size condition");

                        client.sendDataChannelMessage(buf);

                        client.sendDataChannelMessage(new DataChannel.Buffer(Utility.toByteBuffer("\n"), false));
                    } else {
                        // todo break files in pieces here

                        ByteBuffer byteBuffer = ByteBuffer.wrap(stream.toByteArray());
                        DataChannel.Buffer buf = new DataChannel.Buffer(byteBuffer, true);
                        client.sendDataChannelMessage(buf);
                        client.sendDataChannelMessage(new DataChannel.Buffer(Utility.toByteBuffer("\n"), false));
                        //   skylinkConnection.sendData(currentRemotePeerId, stream.toByteArray());
                        Log.w("CONFERENCE_SCREEN", "sending screen data to peer :");
                    }

                    imagesProduced++;
                    Log.w("CONFERENCE_SCREEN", "captured image: " + imagesProduced);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }

                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }

                if (bitmap != null) {
                    bitmap.recycle();
                }

                if (image != null) {
                    image.close();
                }
            }
        }
    }

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

    private ServiceConnection fileTransferConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            FileTransferService.FileTransferBinder binder = (FileTransferService.FileTransferBinder) service;
            fileTransferService = binder.getService();
            fileServiceIsBound = true;

            binder.setListener(new BoundFileServiceListener() {
                @Override
                public void sendChat(String msg) {
                    sendChatMessage(msg);
                }

                @Override
                public void sendDataChannelMessage(DataChannel.Buffer buf) {
                    client.sendDataChannelMessage(buf);
                }

                @Override
                public void receivedFileOffer(final String message, final int sizeOfFileToSave) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            new AlertDialog.Builder(VideoCallView.this)
                                    .setTitle("File offered")
                                    .setMessage(message)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setPositiveButton("Accept", new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            onStatusChanged("Receiving file...");
                                            if (Utility.isFreeSpaceAvailableForFileSize(sizeOfFileToSave)) {
                                                Log.w("FILE_TRANSFER", "Free space is available for file save, requesting chunk now");
                                                fileTransferService.requestChunk();
                                            } else {
                                                Log.w("FILE_TRANSFER", "Need more space to save this file");
                                                onStatusChanged("Need more free space to save this file");
                                            }
                                        }
                                    })
                                    .setNegativeButton("Reject", new DialogInterface.OnClickListener(){

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    }).show();
                        }
                    });
                }

                @Override
                public void fileTransferCompleteNotification(final String name, final ArrayList<Byte> fileBytesArray, final String fileNameToSave) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(VideoCallView.this)
                                    .setTitle("File Transfer Completed")
                                    .setMessage(name)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            if (fileBytesArray.size() > 0) {
                                                byte[] fileBytes = new byte[fileBytesArray.size()];
                                                for (int i = 0; i < fileBytesArray.size(); i++) {
                                                    fileBytes[i] = fileBytesArray.get(i);
                                                }
                                                if (Utility.convertByteArrayToFile(fileBytes, fileNameToSave)) {
                                                    onStatusChanged("File stored in Downloads");
                                                } else {
                                                    onStatusChanged("Some error caused storage failure. You must have SD card.");
                                                }
                                            }
                                        }
                                    })
                                    .setNegativeButton("Remove", new DialogInterface.OnClickListener(){

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    }).show();
                        }
                    });
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            fileServiceIsBound = false;
        }
    };
}