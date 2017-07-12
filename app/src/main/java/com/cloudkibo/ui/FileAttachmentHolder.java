package com.cloudkibo.ui;


import android.app.ActionBar;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.file.filechooser.utils.FileUtils;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.library.Utility;
import com.cloudkibo.model.Conversation;
import com.cloudkibo.utils.IFragmentName;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;

public class FileAttachmentHolder extends CustomFragment implements IFragmentName {
    private String authtoken;
    private String clFragType;
    private String ID;
    private String contactPhone;
    private String fType;
    private String fPath;

    private EditText inputMessage;
    private Button sendButton;
    private ImageView imageHolder;
    Context ctx;
    private HashMap<String, String> user;

    JSONObject fileInfo;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.file_attachment_holder, null);
        setHasOptionsMenu(true);

        ctx = getActivity().getApplicationContext();

        inputMessage = (EditText) v.findViewById(R.id.replyMsg);
        sendButton = (Button) v.findViewById(R.id.btnSend);
        imageHolder = (ImageView) v.findViewById(R.id.imageHolder);

        DatabaseHandler db = new DatabaseHandler(ctx);
        user = db.getUserDetails();

        Bundle args = getArguments();
        if(args != null){
            authtoken = args.getString("authtoken");
            clFragType = args.getString("clFragType");

            if(clFragType.equals("GroupChat")){
                ID = args.getString("ID");
                contactPhone = args.getString("contactPhone");
                fType = args.getString("fType");

                fileInfo= db.getFilesInfo(ID);
                try {
                    setImage(fileInfo.getString("path"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                fPath = args.getString("path");

                setImage(fPath);
            }
        }

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!inputMessage.getText().toString().equals("")){
                    MainActivity.mainActivity.ToastNotify2("Send Button Clicked");
                    // TODO: 6/19/17 Ask sojharo about label end url, and then call the method 
                    //sendFileAttachment(ID, fType, inputMessage.getText().toString());
                    if(clFragType.equals("DayStatus")){
                        sendDayStatusAttachment(fPath, inputMessage.getText().toString());
                    }


                } else{
                    MainActivity.mainActivity.ToastNotify2("Please enter some text");
                }
            }
        });

        return v;
    }

    public void setImage(String path){

            Glide
                    .with(MainActivity.mainActivity)
                    .load(path)
                    .thumbnail(0.1f)
                    .centerCrop()
                    .into(imageHolder);

    }

    public void sendMessageAttachment(final String uniqueid, final String fileType, String label)
    {
        try {

            DatabaseHandler db = new DatabaseHandler(ctx);


            db.addChat(contactPhone, user.get("phone"), user.get("display_name"),
                    fileInfo.getString("file_name"), Utility.getCurrentTimeInISO(), "pending", uniqueid, "file",
                    fileType);

            final int id = 102;

            final NotificationManager mNotifyManager =
                    (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            final android.support.v4.app.NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(ctx);
            mBuilder.setContentTitle("Uploading attachment")
                    .setContentText("Upload in progress")
                    .setSmallIcon(R.drawable.icon);

            UserFunctions userFunctions = new UserFunctions(ctx);
            Ion.with(ctx)
                    .load(userFunctions.getBaseURL() + "/api/filetransfers/upload")
                    .progressHandler(new ProgressCallback() {
                        @Override
                        public void onProgress(long downloaded, long total) {
                            mBuilder.setProgress((int) total, (int) downloaded, false);
                            if(downloaded < total) {
                                mBuilder.setContentText("Upload in progress: "+
                                        ((downloaded / total) * 100) +"%");
                            } else {
                                mBuilder.setContentText("Uploaded file attachment");
                            }
                            mNotifyManager.notify(id, mBuilder.build());
                        }
                    })
                    .setHeader("kibo-token", authtoken)
                    .setMultipartParameter("filetype", fileType)
                    .setMultipartParameter("from", user.get("phone"))
                    .setMultipartParameter("to", contactPhone)
                    .setMultipartParameter("uniqueid", uniqueid)
                    .setMultipartParameter("filename", fileInfo.getString("file_name"))
                    .setMultipartParameter("filesize", fileInfo.getString("file_size"))
                    .setMultipartParameter("label", label)
                    .setMultipartFile("file", FileUtils.getExtension(fileInfo.getString("path")), new File(fileInfo.getString("path")))
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            // do stuff with the result or error
                            if(e == null) {
                                try {
                                    if (MainActivity.isVisible)
                                        MainActivity.mainActivity.ToastNotify2("Uploaded the file to server.");
                                    sendMessageUsingAPI(fileInfo.getString("file_name"), uniqueid, "file", fileType);
                                }catch (JSONException ee){ ee.printStackTrace(); }
                            }
                            else {
                                if(MainActivity.isVisible)
                                    MainActivity.mainActivity.ToastNotify2("Some error has occurred or Internet not available. Please try later.");
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendDayStatusAttachment(final String filePath, final String label)
    {
        try {

            final int id = 102;

            final NotificationManager mNotifyManager =
                    (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            final android.support.v4.app.NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(ctx);
            mBuilder.setContentTitle("Uploading status")
                    .setContentText("Upload in progress")
                    .setSmallIcon(R.drawable.icon);

            String uniqueid = Long.toHexString(Double.doubleToLongBits(Math.random()));
            uniqueid += (new Date().getYear()) + "" + (new Date().getMonth()) + "" + (new Date().getDay());
            uniqueid += (new Date().getHours()) + "" + (new Date().getMinutes()) + "" + (new Date().getSeconds());

            final String tempID = uniqueid;

            UserFunctions userFunctions = new UserFunctions(ctx);
            final String name = com.cloudkibo.webrtc.filesharing.Utility.getFileMetaData(filePath)
                    .getString("name");
            String size = com.cloudkibo.webrtc.filesharing.Utility.getFileMetaData(filePath)
                    .getString("size");
            String type = com.cloudkibo.webrtc.filesharing.Utility.getFileMetaData(filePath)
                    .getString("filetype");
            final String finalUniqueid = uniqueid;
            Ion.with(ctx)
                    .load(userFunctions.getBaseURL() + "/api/daystatus/create")
                    .progressHandler(new ProgressCallback() {
                        @Override
                        public void onProgress(long downloaded, long total) {
                            mBuilder.setProgress((int) total, (int) downloaded, false);
                            if(downloaded < total) {
                                mBuilder.setContentText("Upload in progress: "+
                                        ((downloaded / total) * 100) +"%");
                            } else {
                                mBuilder.setContentText("Uploaded daystatus");
                            }
                            mNotifyManager.notify(id, mBuilder.build());
                        }
                    })
                    .setHeader("kibo-token", authtoken)
                    .setMultipartParameter("date", Utility.convertDateToLocalTimeZoneAndReadable(Utility.getCurrentTimeInISO()))
                    .setMultipartParameter("uniqueid", tempID)
                    .setMultipartParameter("file_name", name)
                    .setMultipartParameter("file_size", size)
                    .setMultipartParameter("label", label)
                    .setMultipartParameter("file_type", type)
                    .setMultipartParameter("from", user.get("phone"))
                    .setMultipartFile("file", FileUtils.getExtension(filePath), new File(filePath))
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            // do stuff with the result or error
                            if(e == null) {
                                if (MainActivity.isVisible)
                                    MainActivity.mainActivity.ToastNotify2("Uploaded the file to server.");
                                //sendMessageUsingAPI(fileInfo.getString("file_name"), uniqueid, "file", fileType); What does this do?

                                DatabaseHandler db = new DatabaseHandler(ctx);
                                try {
                                    db.createDaystatusInfo(tempID,
                                            com.cloudkibo.webrtc.filesharing.Utility.getFileMetaData(filePath)
                                                    .getString("filetype"),
                                            label, //temp label
                                            name,
                                            filePath,
                                            com.cloudkibo.webrtc.filesharing.Utility.getFileMetaData(filePath)
                                                    .getString("size"),
                                            user.get("phone"));

                                    DayStatus goback = new DayStatus();

                                    if(getActivity() != null) {
                                        getFragmentManager().beginTransaction()
                                                .replace(R.id.content_frame, goback, "dayStatusTag")
                                                .addToBackStack("Day Status")
                                                .commit();
                                    }
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }

                            }
                            else {
                                if(MainActivity.isVisible)
                                    MainActivity.mainActivity.ToastNotify2("Some error has occurred or Internet not available. Please try later.");
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageUsingAPI(final String msg, final String uniqueid, final String type, final String file_type){
        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
                UserFunctions userFunction = new UserFunctions(ctx);
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
                            updateChatStatus(row.getString("status"), row.getString("uniqueid"));
                            //updateStatusSentMessage(row.getString("status"), row.getString("uniqueid"));
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }.execute();
    }

    public void updateChatStatus(String status, String uniqueid){
        try {
            DatabaseHandler db = new DatabaseHandler(ctx);
            db.updateChat(status, uniqueid);
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.findItem(R.id.archived).setVisible(false);
            menu.findItem(R.id.settings).setVisible(false);
            menu.findItem(R.id.connect_to_desktop).setVisible(false);
        }
        menu.clear();
        inflater.inflate(R.menu.newchat, menu);  // Use filter.xml from step 1
        getActivity().getActionBar().setSubtitle(null);
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
    }

    @Override
    public String getFragmentName() {
        return "File Attachment Holder";
    }

    @Override
    public String getFragmentContactPhone() {
        return "About chat";
    }
}
