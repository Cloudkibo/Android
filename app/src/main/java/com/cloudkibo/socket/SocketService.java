package com.cloudkibo.socket;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.SplashScreen;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.library.Utility;
import com.cloudkibo.ui.ContactList;
import com.cloudkibo.ui.GroupChat;
import com.cloudkibo.utils.IFragmentName;
import com.cloudkibo.webrtc.call.IncomingCall;
import com.cloudkibo.webrtc.filesharing.FileConnection;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Service for connecting to socket server. This should be alive forever to receive
 * chat and other updates from server in real-time.
 * <p/>
 * Learning Sources:
 * http://developer.android.com/guide/components/services.html
 * http://www.vogella.com/tutorials/AndroidServices/article.html
 * http://stackoverflow.com/questions/10547577/how-can-a-remote-service-send-messages-to-a-bound-activity
 *
 * @author sojharo
 */

public class SocketService extends Service {

    private final IBinder socketBinder = new SocketBinder();
    private BoundServiceListener mListener;

    private HashMap<String, String> user;
    private String room;


    private Boolean areYouCallingSomeone = false;
    private Boolean ringing = false;
    private String amInCallWith;
    private Boolean amInCall = false;
    private Boolean otherSideRinging = false;
    private Boolean isSomeOneCalling = false;
    private Boolean isCallAckReceived = false;

    private Boolean isConnected = false;

    //nkzawa
    Socket socket;

    @Override
    public IBinder onBind(Intent intent) {
        return socketBinder;
    }


    public class SocketBinder extends Binder {

        public SocketService getService() {
            return SocketService.this;
        }

        public void setListener(BoundServiceListener listener) {
            mListener = listener;
        }

    }

    public void setSocketIOConfig() {

        try {

            UserFunctions userFunctions = new UserFunctions(getApplicationContext());
            socket = IO.socket(userFunctions.getBaseURL());
            socket.connect();

            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {

                    Log.w("SOCKET", "CONNECTED");

                    isConnected = true;

                    JSONObject message = new JSONObject();

                    try {

                        JSONObject userInfo = new JSONObject();
                        userInfo.put("phone", user.get("phone"));
                        userInfo.put("_id", user.get("_id"));

                        message.put("user", userInfo);
                        message.put("room", room);

                        socket.emit("join global chatroom", message);//new JSONArray().put(message));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }).on("messagefordatachannel", new Emitter.Listener() {

                @Override
                public void call(Object... args) {

                    try {

                        JSONObject payload = new JSONObject(args[0].toString());

                        if (isForeground("com.cloudkibo"))
                            mListener.receiveSocketJson("messagefordatachannel", payload);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            }).on("message", new Emitter.Listener() {

                @Override
                public void call(Object... args) {

                    Log.e("SOCKET", args[0].toString());

                    try {

                        JSONObject payload = new JSONObject(args[0].toString());

                        mListener.receiveSocketJson(payload.getString("type"), payload);

                        if (payload.getString("type").equals("call")) {
                            String status = payload.getString("status");
                            if(status.equals("calleeoffline")){
                                amInCall = false;
                                amInCallWith = "";
                                isCallAckReceived = true;
                            }
                            if(status.equals("calleeisbusy")){
                                amInCall = false;
                                amInCallWith = "";
                                isCallAckReceived = true;
                            }
                            else if(status.equals("calleeisavailable")){
                                amInCall = true;
                                otherSideRinging = true;
                                amInCallWith = payload.getString("calleephone");
                                isCallAckReceived = true;
                            }
                            else if(status.equals("missing")){
                                isSomeOneCalling = false;
                                ringing = false;
                                amInCall = false;
                                amInCallWith = "";
                            }
                            else if(status.equals("callrejected")){
                                amInCall = false;
                                otherSideRinging = false;
                                amInCallWith = "";
                            }
                            else if(status.equals("callaccepted")){
                                otherSideRinging = false;
                                areYouCallingSomeone = false;
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            }).on("areyoufreeforcall", new Emitter.Listener() {

                @Override
                public void call(Object... args) {

                    try {

                        JSONObject payload = new JSONObject(args[0].toString());

                        JSONObject message2 = new JSONObject();

                        message2.put("calleephone", user.get("phone"));
                        message2.put("callerphone", payload.getString("callerphone"));
                        message2.put("type", "call");

                        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                        JSONObject callerObj = db.getSpecificContact(payload.getString("callerphone")).getJSONObject(0);


                        if (!amInCall) {

                            isSomeOneCalling = true;
                            ringing = true;
                            amInCall = true;

                            amInCallWith = payload.getString("callerphone");

                            message2.put("status", "calleeisavailable");

                            JSONObject message3 = new JSONObject();

                            message3.put("msg", message2);
                            message3.put("to", payload.getString("callerphone"));

                            socket.emit("message", message3);

                            Intent i = new Intent(getApplicationContext(), IncomingCall.class);
                            i.putExtra("user", user);
                            i.putExtra("room", room);
                            i.putExtra("contact", amInCallWith);
                            i.putExtra("contact_name", callerObj.getString("display_name"));
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);

                        } else {

                            message2.put("status", "calleeisbusy");

                            JSONObject message3 = new JSONObject();

                            message3.put("msg", message2);
                            message3.put("to", payload.getString("callerphone"));

                            socket.emit("message", message3);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            }).on("peer.connected", new Emitter.Listener() {

                @Override
                public void call(Object... args) {

                    Log.w("CONFERENCE", args[0].toString());
                    try {
                        JSONObject payload = new JSONObject(args[0].toString());

                        mListener.receiveSocketJson("peer.connected", payload);

                    }catch(JSONException e){
                        e.printStackTrace();
                    }

                }

            }).on("peer.disconnected", new Emitter.Listener() {

                @Override
                public void call(Object... args) {

                    Log.w("CONFERENCE", args[0].toString());
                    try {
                        JSONObject payload = new JSONObject(args[0].toString());

                        mListener.receiveSocketJson("peer.disconnected", payload);

                    }catch(JSONException e){
                        e.printStackTrace();
                    }

                }

            }).on("conference.stream", new Emitter.Listener() {

                @Override
                public void call(Object... args) {

                    Log.w("CONFERENCE", args[0].toString());
                    try {
                        JSONObject payload = new JSONObject(args[0].toString());

                        mListener.receiveSocketJson("conference.stream", payload);

                    }catch(JSONException e){
                        e.printStackTrace();
                    }

                }

            }).on("conference.chat", new Emitter.Listener() {

                @Override
                public void call(Object... args) {

                    Log.w("CONFERENCE", args[0].toString());
                    try {
                        JSONObject payload = new JSONObject(args[0].toString());

                        mListener.receiveSocketJson("conference.chat", payload);

                    }catch(JSONException e){
                        e.printStackTrace();
                    }

                }

            }).on("msg", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    
                    try {
                        JSONObject payload = new JSONObject(args[0].toString());

                        mListener.receiveSocketJson("msg", payload);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {

                    isConnected = false;

                }

            });

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * This checks if the activity or app is running or not.
     *
     * @param myPackage
     * @return
     */

    public boolean isForeground(String myPackage) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        Log.e("SOCKET", "Packange name of foreground " + componentInfo.getPackageName());
        return componentInfo.getPackageName().equals(myPackage);
    }

    public Boolean isSocketConnected(){
        return isConnected;
    }

    public Boolean isSocketConnected2(){
        return socket.connected();
    }

    public static boolean isAppSentToBackground(final Context context) {

        try {
            ActivityManager am = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            // The first in the list of RunningTasks is always the foreground
            // task.
            RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);
            String foregroundTaskPackageName = foregroundTaskInfo.topActivity
                    .getPackageName();// get the top fore ground activity
            PackageManager pm = context.getPackageManager();
            PackageInfo foregroundAppPackageInfo = pm.getPackageInfo(
                    foregroundTaskPackageName, 0);

            String foregroundTaskAppName = foregroundAppPackageInfo.applicationInfo
                    .loadLabel(pm).toString();

            Log.e("SOCKET", foregroundTaskAppName + "----------" +
                    foregroundTaskPackageName);
            if (!foregroundTaskAppName.equals("CloudKibo")) {
                return true;
            }
        } catch (Exception e) {
            Log.e("isAppSentToBackground", "" + e);
        }
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        user = (HashMap) intent.getExtras().get("user");
        room = intent.getExtras().getString("room");

        return Service.START_REDELIVER_INTENT;
    }

    /**
     * This following part needs some rigorous testing of more than 2 days usage.
     * I need to check what happens when this method is not called. Or what happens
     * when service is destroyed and started again by the system (not activity)
     * <p/>
     * Learning Source: http://stackoverflow.com/questions/14182014/android-oncreate-or-onstartcommand-for-starting-service
     *
     * @author sojharo
     */

    @Override
    public void onCreate() {

        setSocketIOConfig();

        super.onCreate();
    }


    public void sendSocketMessage(String msg, String peer) {

        JSONObject message = new JSONObject();

        try {

            message.put("msg", msg);
            message.put("room", room);
            message.put("to", peer);
            message.put("username", user.get("phone"));

            socket.emit("message", message);//new JSONArray().put(message));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendConferenceMsg(JSONObject message) { // todo: do test this

        socket.emit("msg", message);//new JSONArray().put(message));

    }

    public void sendConferenceMsgGeneric(String msgType, JSONObject message) { // todo: do test this

        socket.emit(msgType, message);//new JSONArray().put(message));

    }

    public void stopCallMessageToCallee() {

        try {
            JSONObject message2 = new JSONObject();

            message2.put("calleephone", amInCallWith);
            message2.put("callerphone", user.get("phone"));
            message2.put("type", "call");
            message2.put("status", "missing");

            JSONObject message3 = new JSONObject();

            message3.put("msg", message2);
            message3.put("to", amInCallWith);

            socket.emit("message", message3);
        } catch (JSONException e){
            e.printStackTrace();
        }

        //sendSocketMessage("Missed Incoming Call: " + user.get("display_name"), amInCallWith);

        amInCall = false;
        amInCallWith = "";
        otherSideRinging = false;
        areYouCallingSomeone = false;

    }

    public void acceptCallMessageToCallee() {
        try {
            JSONObject message2 = new JSONObject();

            message2.put("calleephone", user.get("phone"));
            message2.put("callerphone", amInCallWith);
            message2.put("type", "call");
            message2.put("status", "callaccepted");

            JSONObject message3 = new JSONObject();

            message3.put("msg", message2);
            message3.put("to", amInCallWith);

            socket.emit("message", message3);
        } catch (JSONException e){
            e.printStackTrace();
        }
        //sendSocketMessage("Accept Call", amInCallWith);

        isSomeOneCalling = false;
        ringing = false;
        amInCall = false;
        amInCallWith = "";
    }

    public void rejectCallMessageToCallee() {
        try {
            JSONObject message2 = new JSONObject();

            message2.put("calleephone", user.get("phone"));
            message2.put("callerphone", amInCallWith);
            message2.put("type", "call");
            message2.put("status", "callrejected");

            JSONObject message3 = new JSONObject();

            message3.put("msg", message2);
            message3.put("to", amInCallWith);

            socket.emit("message", message3);
        } catch (JSONException e){
            e.printStackTrace();
        }
        //sendSocketMessage("Reject Call", amInCallWith);

        isSomeOneCalling = false;
        ringing = false;
        amInCall = false;
    }

    public void sendRoomNameToCallee(String roomId) {
        try {
            JSONObject message2 = new JSONObject();

            message2.put("calleephone", amInCallWith);
            message2.put("callerphone", user.get("phone"));
            message2.put("type", "room_name");
            message2.put("room_name", roomId);

            JSONObject message3 = new JSONObject();

            message3.put("msg", message2);
            message3.put("to", amInCallWith);

            socket.emit("message", message3);
        } catch (JSONException e){
            e.printStackTrace();
        }
        //sendSocketMessage("Accept Call", amInCallWith);

        isSomeOneCalling = false;
        ringing = false;
        amInCall = false;
        amInCallWith = "";
    }

    public void sendSocketMessageDataChannel(String msg, String filePeer) {

        JSONObject message = new JSONObject();

        try {

            message.put("msg", msg);
            message.put("room", room);
            message.put("to", filePeer);
            message.put("from", user.get("username"));

            socket.emit("messagefordatachannel", message);// new JSONArray().put(message));


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void callThisPerson(String contact) {

        try {

            Log.d("CALL", "Making Call to " + contact);

            isCallAckReceived = false;

            JSONObject message1 = new JSONObject();

            message1.put("room", room);
            message1.put("callerphone", user.get("phone"));
            message1.put("calleephone", contact);

            socket.emit("callthisperson", message1, new Ack() {
                @Override
                public void call(Object... args) {
                    isCallAckReceived = true;
                }
            });//new JSONArray().put(message1));

            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            Log.i("tag", "This'll run 3000 milliseconds later");
                            if(!isCallAckReceived){
                                Toast.makeText(getApplicationContext(),
                                        "Can't connect to server.", Toast.LENGTH_SHORT)
                                        .show();
                                areYouCallingSomeone = false;
                                mListener.receiveSocketMessage("NoAck", "NoAck");
                            } else {
                                isCallAckReceived = false;
                            }
                        }
                    },
                    3000);

            areYouCallingSomeone = true;

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            Toast.makeText(getApplicationContext(),
                    "Could not make this call. No Internet or behind proxy", Toast.LENGTH_SHORT)
                    .show();
        }
    }


    public void joinConference(JSONObject data){
        socket.emit("init", data, new Ack() {
            @Override
            public void call(Object... args) {

                Log.w("CURRENT ID", args[1].toString());

                mListener.receiveSocketMessage("conference_id", args[1].toString());

            }
        });
    }

    public void askFriendsOnlineStatus() {

        try {

            JSONObject message = new JSONObject();

            message.put("_id", user.get("_id"));
            message.put("phone", user.get("phone"));

            JSONObject completeMessage = new JSONObject();

            completeMessage.put("room", room);
            completeMessage.put("user", message);

            socket.emit("whozonline", completeMessage);//new JSONArray().put(completeMessage));

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void endCall(){
        amInCall = false;
        otherSideRinging = false;
        amInCallWith = "";

        socket.disconnect();
        socket.connect();
    }

    public void logToServer(String message) {
        socket.emit("logClient", "ANDROID : "+ message);
    }

}
