package com.cloudkibo.socket;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.text.ParseException;
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
import android.os.AsyncTask;
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

import static com.cloudkibo.file.filechooser.utils.FileUtils.getExternalStoragePublicDirForDocuments;
import static com.cloudkibo.file.filechooser.utils.FileUtils.getExternalStoragePublicDirForDownloads;
import static com.cloudkibo.file.filechooser.utils.FileUtils.getExternalStoragePublicDirForImages;

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

    private String desktop_id;
    private String my_id;
    private Boolean platform_connected = false;
    private String authtoken;
    private ArrayList<Byte> fileBytesArray;

    public void connectToDesktop (String id, String authtoken) {
        this.authtoken = authtoken;
        desktop_id = id;
        try {
            JSONObject payload = new JSONObject();
            payload.put("phone", user.get("phone"));
            socket.emit("join_platform_room", payload);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Boolean isPlatformConnected(){
        return platform_connected;
    }

    public void refreshGroupInformation () {
        sendGroupsToDesktop();
    }

    private void sendChatListToDesktop () {
        try {

            DatabaseHandler db = new DatabaseHandler(getApplicationContext());

            JSONArray chatListArray = db.getChatList();

            JSONObject payload = new JSONObject();
            payload.put("phone", user.get("phone"));
            payload.put("to_connection_id", desktop_id);
            payload.put("from_connection_id", my_id);
            payload.put("type", "loading_chatlist");
            payload.put("data", chatListArray);

            socket.emit("platform_room_message", payload);//new JSONArray().put(message));
            // todo remove this, only for test
            //sendByteData();
            //sendGroupChatListToDesktop();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // todo remove this it was only for test
    public void sendByteData(){
        try {

            File file = new File("/storage/emulated/0/WhatsApp/Media/WhatsApp Images/IMG-20170307-WA0000.jpg");

            int CHUNK_SIZE = 16000;//32000;//16000;
            int sizeOfFileToSend = (int)file.length();
            int numberOfChunksInFileToSend = (int) Math.ceil(sizeOfFileToSend / CHUNK_SIZE);
            int numberOfChunksSent = 0;

            byte chunks[] = com.cloudkibo.webrtc.filesharing
                    .Utility.convertFileToByteArray(file);

            if (file.length() < CHUNK_SIZE) {

                JSONObject filePayload = new JSONObject();
                filePayload.put("chunk", chunks);
                filePayload.put("unique_id", "ABABABABABABABA");

                socket.emit("platform_room_message", filePayload);
            } else {
                while (numberOfChunksSent <= numberOfChunksInFileToSend) {

                    int upperLimit = (numberOfChunksSent + 1) * CHUNK_SIZE;

                    if (upperLimit > (int) file.length()) {
                        upperLimit = (int) file.length() - 1;
                    }

                    int lowerLimit = (numberOfChunksSent) * CHUNK_SIZE;
                    Log.w("FILE_ATTACHMENT", "Limits: " + lowerLimit + " " + upperLimit);
                    Utility.sendLogToServer(getApplicationContext(), "Limits: " + lowerLimit + " - "
                            + upperLimit + " AND byte length is " + chunks.length + " AND chunk is "
                    + (upperLimit - lowerLimit));


                    ByteBuffer byteBuffer = ByteBuffer.wrap(chunks, lowerLimit, upperLimit - lowerLimit);


                    JSONObject filePayload = new JSONObject();
                    filePayload.put("chunk", byteBuffer.array());
                    filePayload.put("unique_id", "ABABABABABABABA");

                    socket.emit("platform_room_message", filePayload);

                    numberOfChunksSent++;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendGroupChatListToDesktop () {
        try {

            DatabaseHandler db = new DatabaseHandler(getApplicationContext());

            JSONArray chatListArray = db.getGroupChatList();

            JSONObject payload = new JSONObject();
            payload.put("phone", user.get("phone"));
            payload.put("to_connection_id", desktop_id);
            payload.put("from_connection_id", my_id);
            payload.put("type", "loading_group_chatlist");
            payload.put("data", chatListArray);

            socket.emit("platform_room_message", payload);//new JSONArray().put(message));
            sendContactListToDesktop();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendContactListToDesktop () {
        try {

            DatabaseHandler db = new DatabaseHandler(getApplicationContext());

            JSONArray contactListArray = db.getContacts();

            JSONObject payload = new JSONObject();
            payload.put("phone", user.get("phone"));
            payload.put("to_connection_id", desktop_id);
            payload.put("from_connection_id", my_id);
            payload.put("type", "loading_contacts");
            payload.put("data", contactListArray);

            socket.emit("platform_room_message", payload);//new JSONArray().put(message));
            sendArchivedConversationToDesktop();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendConversationToDesktop (String user1, String user2) {
        try {

            DatabaseHandler db = new DatabaseHandler(getApplicationContext());

            JSONArray contactListArray = db.getChat(user1, user2);

            JSONObject payload = new JSONObject();
            payload.put("phone", user.get("phone"));
            payload.put("to_connection_id", desktop_id);
            payload.put("from_connection_id", my_id);
            payload.put("type", "loading_conversation");
            payload.put("data", contactListArray);

            socket.emit("platform_room_message", payload);//new JSONArray().put(message));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendGroupConversationToDesktop (String groupid) {
        try {

            DatabaseHandler db = new DatabaseHandler(getApplicationContext());

            JSONArray contactListArray = db.getGroupMessages(groupid);

            JSONObject payload = new JSONObject();
            payload.put("phone", user.get("phone"));
            payload.put("to_connection_id", desktop_id);
            payload.put("from_connection_id", my_id);
            payload.put("type", "loading_group_conversation");
            payload.put("data", contactListArray);

            socket.emit("platform_room_message", payload);//new JSONArray().put(message));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendArchivedConversationToDesktop () {
        try {

            DatabaseHandler db = new DatabaseHandler(getApplicationContext());

            JSONArray chatListArray = db.getArchivedChatList();

            JSONObject payload = new JSONObject();
            payload.put("phone", user.get("phone"));
            payload.put("to_connection_id", desktop_id);
            payload.put("from_connection_id", my_id);
            payload.put("type", "loading_archive");
            payload.put("data", chatListArray);

            socket.emit("platform_room_message", payload);//new JSONArray().put(message));
            sendGroupsToDesktop();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendGroupsToDesktop () {
        try {

            DatabaseHandler db = new DatabaseHandler(getApplicationContext());

            JSONArray groupListArray = db.getAllGroups();

            JSONObject payload = new JSONObject();
            payload.put("phone", user.get("phone"));
            payload.put("to_connection_id", desktop_id);
            payload.put("from_connection_id", my_id);
            payload.put("type", "loading_groups");
            payload.put("data", groupListArray);

            socket.emit("platform_room_message", payload);//new JSONArray().put(message));

            JSONArray payloadArray = payload.getJSONArray("data");

            for (int i=0; i<payloadArray.length(); i++) {
                sendGroupMembersToDesktop(payloadArray.getJSONObject(i).getString("unique_id"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendGroupMembersToDesktop (String group_id) {
        try {

            DatabaseHandler db = new DatabaseHandler(getApplicationContext());

            JSONArray chatListArray = db.getGroupMembers(group_id);

            JSONObject payload = new JSONObject();
            payload.put("phone", user.get("phone"));
            payload.put("to_connection_id", desktop_id);
            payload.put("from_connection_id", my_id);
            payload.put("type", "loading_group_members");
            payload.put("data", chatListArray);

            socket.emit("platform_room_message", payload);//new JSONArray().put(message));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendNewArrivedChatToDesktop (JSONObject chatPayload) {
        try {

            // // TODO: 03/03/2017 sends this payload even if not connected to desktop platform 
            JSONObject payload = new JSONObject();
            payload.put("phone", user.get("phone"));
            payload.put("to_connection_id", desktop_id);
            payload.put("from_connection_id", my_id);
            payload.put("type", "new_message_received");
            payload.put("data", chatPayload);

            socket.emit("platform_room_message", payload);//new JSONArray().put(message));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void sendArrivedChatStatusToDesktop(JSONObject chatPayload) {
        try {

            // // TODO: 03/03/2017 sends this payload even if not connected to desktop platform
            JSONObject payload = new JSONObject();
            payload.put("phone", user.get("phone"));
            payload.put("to_connection_id", desktop_id);
            payload.put("from_connection_id", my_id);
            payload.put("type", "message_status");
            payload.put("data", chatPayload);

            socket.emit("platform_room_message", payload);//new JSONArray().put(message));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendLastSeenInfoToDesktop(String chatPayload) {
        try {

            JSONObject payload = new JSONObject();
            payload.put("phone", user.get("phone"));
            payload.put("to_connection_id", desktop_id);
            payload.put("from_connection_id", my_id);
            payload.put("type", "sending_last_seen");
            payload.put("data", chatPayload);

            socket.emit("platform_room_message", payload);//new JSONArray().put(message));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendNewArchiveUpdateToDesktop(JSONObject chatPayload) {
        try {

            String type = "chat_archived";
            if (!chatPayload.getString("isArchived").equals("Yes")) type = "chat_unarchived";

            JSONObject payload = new JSONObject();
            payload.put("phone", user.get("phone"));
            payload.put("to_connection_id", desktop_id);
            payload.put("from_connection_id", my_id);
            payload.put("type", type);
            payload.put("data", chatPayload);

            socket.emit("platform_room_message", payload);//new JSONArray().put(message));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendFileChunkToDesktop(JSONObject payloadFile) {
        try {

            JSONObject payload = new JSONObject();
            payload.put("phone", user.get("phone"));
            payload.put("to_connection_id", desktop_id);
            payload.put("from_connection_id", my_id);
            payload.put("type", "mobile_sending_chunk");
            payload.put("data", payloadFile);

            socket.emit("platform_room_message", payload);//new JSONArray().put(message));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

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

            }).on("joined_platform_room", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    my_id = args[0].toString();
                    platform_connected = true;
                    sendChatListToDesktop();
                }

            }).on("offline", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    try {
                        JSONObject contact = new JSONObject(args[0].toString());
                        Log.e("SocketServiceTag", contact.toString());
                        mListener.receiveSocketJson("offline", contact);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            }).on("online", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    try {
                        JSONObject contact = new JSONObject(args[0].toString());
                        Log.e("SocketServiceTag", contact.toString());
                        mListener.receiveSocketJson("online", contact);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            }).on("theseareonline", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    try {
                        JSONArray contacts = new JSONArray(args[0].toString());

                        Log.e("SocketServiceTag", args[0].toString());
                        mListener.receiveSocketArray("theseareonline", contacts);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }).on("platform_room_message", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    try {

                        // todo test it with desktop app
                        JSONObject payload = new JSONObject(args[0].toString());

                        DatabaseHandler db = new DatabaseHandler(getApplicationContext());

                        if(payload.getString("type").equals("new_message_sent")){
                            // desktop app is trying to send data

                            JSONObject row = payload.getJSONObject("data");


                            db.addChat(row.getString("to"), row.getString("from"), row.getString("fromFullName"),
                                    row.getString("msg"), row.getString("date"),
                                    "pending",
                                    row.getString("uniqueid"),
                                    row.getString("type"),
                                   row.getString("file_type"));

                            sendMessageUsingAPI(row.getString("msg"), row.getString("uniqueid"),
                                    row.getString("type"), row.getString("file_type"),
                                    row.getString("to"));

                            if (isForeground("com.cloudkibo"))
                                mListener.receiveSocketJson("desktop_sent_chat", payload);

                        } else if (payload.getString("type").equals("message_status")) {

                            JSONObject row = payload.getJSONObject("data");

                            sendMessageStatusUsingAPI(row.getString("status"),
                                    row.getString("uniqueid"), row.getString("sender"));
                            if (isForeground("com.cloudkibo"))
                                mListener.receiveSocketJson("message_status", payload);

                        } else if (payload.getString("type").equals("asking_last_seen")) {

                            lastSeenStatus(payload.getString("data"));

                        } else if (payload.getString("type").equals("new_group_created")) {

                            payload = payload.getJSONObject("data");
                            String group_name = payload.getString("group_name");
                            String group_id = payload.getString("unique_id");
                            db.createGroup(group_id, group_name, 0);

                            String message = "You created group "+ group_name;
                            String member_name = db.getUserDetails().get("display_name");
                            String member_phone = db.getUserDetails().get("phone");
                            String uniqueid = Long.toHexString(Double.doubleToLongBits(Math.random()));
                            uniqueid += (new Date().getYear()) + "" + (new Date().getMonth()) + "" + (new Date().getDay());
                            uniqueid += (new Date().getHours()) + "" + (new Date().getMinutes()) + "" + (new Date().getSeconds());
                            db.addGroupMessage(group_id,message,member_phone,member_name,uniqueid, "log");

                        } else if (payload.getString("type").equals("member_added_to_group")) {

                            JSONArray data = payload.getJSONArray("data");

                            for (int i = 0; i< data.length(); i++){
                                JSONObject row = data.getJSONObject(i);
                                db.addGroupMember(row.getString("unique_id"),
                                        row.getString("phone"), row.getString("isAdmin"), "joined");

                            }

                        } else if (payload.getString("type").equals("desktop_requesting_attachment")) {

                            JSONObject row = payload.getJSONObject("data");
                            JSONObject fileinfo = db.getFilesInfo(row.getString("unique_id"));

                            File file = new File(fileinfo.getString("path"));

                            int CHUNK_SIZE = 36000;//32000;//16000;
                            int sizeOfFileToSend = Integer.parseInt(fileinfo.getString("file_size"));
                            int numberOfChunksInFileToSend = (int) Math.ceil(sizeOfFileToSend / CHUNK_SIZE);
                            int numberOfChunksSent = 0;

                            if (file.length() < CHUNK_SIZE) {

                                byte chunk [] = com.cloudkibo.webrtc.filesharing
                                        .Utility.convertFileToByteArray(file);

                                JSONObject filePayload = new JSONObject();
                                filePayload.put("chunk", chunk);
                                filePayload.put("unique_id", row.getString("unique_id"));
                                filePayload.put("chunk_id", numberOfChunksSent);
                                filePayload.put("total_chunks", numberOfChunksInFileToSend);

                                sendFileChunkToDesktop(filePayload);

                                Log.w("FILE_ATTACHMENT", "File Smaller than chunk size condition");

                            } else {
                                byte chunks [] = com.cloudkibo.webrtc.filesharing
                                        .Utility.convertFileToByteArray(file);

                                while (numberOfChunksSent <= numberOfChunksInFileToSend) {

                                    int upperLimit = (numberOfChunksSent + 1) * CHUNK_SIZE;

                                    if (upperLimit > (int) file.length()) {
                                        upperLimit = (int) file.length() - 1;
                                    }

                                    int lowerLimit = (numberOfChunksSent) * CHUNK_SIZE;
                                    Log.w("FILE_ATTACHMENT", "Limits: " + lowerLimit + " " + upperLimit);
                                    /*Utility.sendLogToServer(getApplicationContext(), "Limits: " + lowerLimit + " - "
                                            + upperLimit + " AND byte length is " + chunks.length + " AND chunk is "
                                            + (upperLimit - lowerLimit));*/

                                    ByteBuffer byteBuffer = ByteBuffer.wrap(chunks, lowerLimit, upperLimit - lowerLimit);


                                    JSONObject filePayload = new JSONObject();
                                    filePayload.put("chunk", byteBuffer.array());
                                    filePayload.put("unique_id", row.getString("unique_id"));
                                    filePayload.put("chunk_id", numberOfChunksSent+1);
                                    filePayload.put("total_chunks", numberOfChunksInFileToSend);

                                    sendFileChunkToDesktop(filePayload);

                                    numberOfChunksSent++;
                                }
                            }
                        } else if (payload.getString("type").equals("desktop_wants_send_file")) {

                            JSONObject row = payload.getJSONObject("data");

                            fileBytesArray = new ArrayList<Byte>();

                            db.createFilesInfo(row.getString("unique_id"), row.getString("file_name"),
                                    row.getString("file_size"), row.getString("file_type"),
                                    row.getString("file_ext"), "under construction");

                        } else if (payload.getString("type").equals("desktop_sending_chunk")) {

                            JSONObject row = payload.getJSONObject("data");
                            JSONObject fileinfo = db.getFilesInfo(row.getString("unique_id"));

                            byte[] chunk = (byte[]) row.get("chunk");

                            for(int i=0; i<chunk.length; i++)
                                fileBytesArray.add(chunk[i]);

                            if (row.getInt("chunk_id") == row.getInt("total_chunks")) {
                                try {
                                    Log.w("FILETRANSFER", "File transfer completed");
                                    byte[] fileBytes = new byte[fileBytesArray.size()];
                                    for (int i = 0; i < fileBytesArray.size(); i++) {
                                        fileBytes[i] = fileBytesArray.get(i);
                                    }

                                    File folder = getExternalStoragePublicDirForImages(getString(R.string.app_name));
                                    if (fileinfo.getString("file_type").equals("document")) {
                                        folder = getExternalStoragePublicDirForDocuments(getString(R.string.app_name));
                                    }
                                    if (fileinfo.getString("file_type").equals("audio")) {
                                        folder = getExternalStoragePublicDirForDownloads(getString(R.string.app_name));
                                    }
                                    if (fileinfo.getString("file_type").equals("video")) {
                                        folder = getExternalStoragePublicDirForDownloads(getString(R.string.app_name));
                                    }
                                    FileOutputStream outputStream;
                                    outputStream = new FileOutputStream(folder.getPath() + "/" +
                                            fileinfo.getString("file_name"));
                                    outputStream.write(fileBytes);
                                    outputStream.close();
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    } catch (JSONException e) {
                        Utility.sendLogToServer(getApplicationContext(), "URGENT ERROR ON ANDROID: " +
                                "WRONG DATA SENT BY DESKTOP APP "+ args[0].toString());
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

    // TODO move this to Utility Class, it is also duplicated in GroupChat.java
    public void sendMessageUsingAPI(final String msg, final String uniqueid, final String type,
                                    final String file_type, final String contactPhone){
        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
                UserFunctions userFunction = new UserFunctions(getApplicationContext());
                JSONObject message = new JSONObject();

                try {
                    message.put("from", user.get("phone"));
                    message.put("to", contactPhone);
                    message.put("fromFullName", user.get("display_name"));
                    message.put("msg", msg);
                    message.put("date", Utility.getCurrentTimeInISO());
                    message.put("uniqueid", uniqueid);
                    message.put("type", type);
                    message.put("file_type", file_type);
                } catch (JSONException e){
                    e.printStackTrace();
                }

                return userFunction.sendChatMessageToServer(message, authtoken);
            }

            @Override
            protected void onPostExecute(JSONObject row) {
                try {

                    if (row != null) {
                        if(row.has("status")){
                            try {
                                DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                                db.updateChat(row.getString("status"), uniqueid);
                                JSONObject payload = new JSONObject();
                                payload.put("status", row.getString("status"));
                                payload.put("uniqueid", uniqueid);
                                sendArrivedChatStatusToDesktop(payload);
                            } catch (NullPointerException e){
                                e.printStackTrace();
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }.execute();
    }

    // TODO move this to Utility Class, it is also duplicated in GroupChat.java
    public void sendMessageStatusUsingAPI(final String status, final String uniqueid, final String sender){
        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
                UserFunctions userFunction = new UserFunctions(getApplicationContext());
                JSONObject message = new JSONObject();

                try {
                    message.put("sender", sender);
                    message.put("status", status);
                    message.put("uniqueid", uniqueid);
                } catch (JSONException e){
                    e.printStackTrace();
                }

                DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                db.updateChat("seen", uniqueid);
                db.addChatSyncHistory("seen", uniqueid, sender);

                return userFunction.sendChatMessageStatusToServer(message, authtoken);
            }

            @Override
            protected void onPostExecute(JSONObject row) {
                if (row != null) {
                    if(row.has("status")){
                        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                        db.resetSpecificChatHistorySync(uniqueid);
                        db.updateChat("seen", uniqueid);
                    }
                }
            }

        }.execute();
    }

    public void lastSeenStatus(final String contactPhone){
        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
                UserFunctions userFunctions = new UserFunctions(getApplicationContext());
                return userFunctions.getUserStatus(contactPhone, authtoken);
            }

            @Override
            protected void onPostExecute(JSONObject row) {
                Log.e("Last Seen on", "post executed");
                if(row != null){
                    Log.e("Last Seen on", "Fetched data");

                    try {
                        String text = "Last Seen on " + Utility.convertDateToLocalTimeZoneAndReadable(row.getString("last_seen"));
                        sendLastSeenInfoToDesktop(text);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Log.e("Last Seen on", "Inside TRY Statement");


                }
                Log.e("Last Seen on", "leaving post executed");
            }

        }.execute();
    }

    public void logToServer(String message) {
        socket.emit("logClient", "ANDROID : "+ message);
    }

}
