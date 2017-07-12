package com.cloudkibo.ui;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.file.filechooser.utils.FileUtils;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.library.Utility;
import com.cloudkibo.model.Conversation;
import com.cloudkibo.utils.IFragmentName;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;

import static com.cloudkibo.file.filechooser.utils.FileUtils.getExternalStoragePublicDirForImages;

public class DayStatus extends CustomFragment implements IFragmentName {
    String authtoken;
    ListView lv;
    LayoutInflater inflater;
    JSONArray members;
    private String tempCameraCaptureHolderString;
    private HashMap<String, String> user;
    Context ctx;
    CustomDayStatusAdapter adp;
    private static int kJobId = 0;
    final static int MINUTELY = 60 * 1000;
    final static int HOURLY = 60 * MINUTELY;
    final static int DAILY = 24 * HOURLY;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.day_status_screen, null);
        this.inflater = inflater;
        setHasOptionsMenu(true);
        authtoken = getActivity().getIntent().getExtras().getString("authtoken");
        ctx = getActivity().getApplicationContext();
        DatabaseHandler db = new DatabaseHandler(getContext());
        user = db.getUserDetails();
        Bundle args = getArguments();
        if (args  != null){
            // TODO: 5/27/17 get arguments from fragment it is called in
        }

        FloatingActionButton createStatus = (FloatingActionButton) v.findViewById(R.id.fab_addStatus);
        createStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Action Button clicked", Toast.LENGTH_SHORT).show();
//                DayStatusView demo = new DayStatusView();
//
//                getFragmentManager().beginTransaction()
//                        .replace(R.id.content_frame, demo, "dayStatusViewTag")
//                        .addToBackStack("View Status")
//                        .commit();

                sendImageSelected();

            }
        });

        lv=(ListView) v.findViewById(R.id.listView);

        try {
            members = db.getAllDayStatus();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        adp = new CustomDayStatusAdapter(inflater, members, getContext());
        lv.setAdapter(adp);
        registerForContextMenu(lv);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                JSONObject obj = null;
                try {
                    obj = members.getJSONObject(i);
                    String uploadedBy = obj.getString("uploaded_by");

                    DayStatusView demo = new DayStatusView();
                    Bundle bundle = new Bundle();
                    bundle.putString("contactPhone", uploadedBy);
                    demo.setArguments(bundle);

                    getFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, demo, "dayStatusViewTag")
                        .addToBackStack("View Status")
                        .commit();


                    Toast.makeText(ctx, uploadedBy, Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        return v;

    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuinfo){
        super.onCreateContextMenu(menu, v, menuinfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuinfo;

        if(true){
            menu.setHeaderTitle("Mute Status from this");
            menu.add(0, v.getId(), 0, "Mute");
        } else {
            menu.setHeaderTitle(getString(R.string.common_select_action));
            menu.add(0, v.getId(), 0, getString(R.string.common_remove_message));
        }
    }

    public boolean onContextItemSelected(MenuItem item){

        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        if(item.getTitle() == "Mute"){

            Toast.makeText(ctx, "Mute clicked", Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    private void sendImageSelected() {
        final CharSequence[] options = { "Take Photo","Record Video", "Choose from Gallery","Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.mainActivity);

        builder.setTitle("Set Status");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        getActivity().requestPermissions(new String[]{Manifest.permission.CAMERA}, 103);
                    } else {
                        uploadImageFromCamera();
                    }
                } else if (options[item].equals("Choose from Gallery")) {
//                    MainActivity act3 = (MainActivity)getActivity();
//                    act3.uploadChatAttachment("image", "not_group");
                    MainActivity.mainActivity.ToastNotify2("Call Image Chooser");
                } else if (options[item].equals(R.string.cancel)) {
                    dialog.dismiss();
                } else if (options[item].equals("Record Video")) {
                    //uploadVideoFromCamera();
                }
            }
        });
        builder.show();
    }

    public void uploadVideoFromCamera(){
        String uniqueid = Long.toHexString(Double.doubleToLongBits(Math.random()));
        uniqueid += (new Date().getYear()) + "" + (new Date().getMonth()) + "" + (new Date().getDay());
        uniqueid += (new Date().getHours()) + "" + (new Date().getMinutes()) + "" + (new Date().getSeconds());
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        File folder= getExternalStoragePublicDirForImages(getString(R.string.app_name));
        File f = new File(folder, uniqueid +".mp4");
        tempCameraCaptureHolderString = f.getPath();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        getActivity().startActivityForResult(intent, 7153);
    }


    public void uploadImageFromCamera(){
        String uniqueid = Long.toHexString(Double.doubleToLongBits(Math.random()));
        uniqueid += (new Date().getYear()) + "" + (new Date().getMonth()) + "" + (new Date().getDay());
        uniqueid += (new Date().getHours()) + "" + (new Date().getMinutes()) + "" + (new Date().getSeconds());
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File folder= getExternalStoragePublicDirForImages(getString(R.string.app_name));
        File f = new File(folder, uniqueid +".jpg");
        tempCameraCaptureHolderString = f.getPath();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        getActivity().startActivityForResult(intent, 7152);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check whether the result is ok

        if (resultCode == Activity.RESULT_OK) {
            // Check for the request code, we might be usign multiple startActivityForReslut
            switch (requestCode) {
                case 7152:
                    String uniqueid = Long.toHexString(Double.doubleToLongBits(Math.random()));
                    uniqueid += (new Date().getYear()) + "" + (new Date().getMonth()) + "" + (new Date().getDay());
                    uniqueid += (new Date().getHours()) + "" + (new Date().getMinutes()) + "" + (new Date().getSeconds());
                    String name = "blank";
                    try {
                        name = com.cloudkibo.webrtc.filesharing.Utility.getFileMetaData(tempCameraCaptureHolderString)
                                .getString("name");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    DatabaseHandler db = new DatabaseHandler(ctx);
                        Toast.makeText(ctx, name , Toast.LENGTH_SHORT).show();
                    //                        db.createDaystatusInfo(uniqueid,
//                                com.cloudkibo.webrtc.filesharing.Utility.getFileMetaData(tempCameraCaptureHolderString)
//                                        .getString("filetype"),
//                                name, //temp label
//                                name,
//                                tempCameraCaptureHolderString,
//                                com.cloudkibo.webrtc.filesharing.Utility.getFileMetaData(tempCameraCaptureHolderString)
//                                        .getString("size"),
//                                user.get("phone"));

                    MediaScannerConnection.scanFile(ctx, new String[] { tempCameraCaptureHolderString }, new String[] { "image/jpeg" }, null);
                        String imageP = tempCameraCaptureHolderString;
                        tempCameraCaptureHolderString = "";
                        //sendFileAttachment(uniqueid, imageP);
                        gotoHolderFrag(imageP);



                    startJobServiceForOneTimeOnly(uniqueid);

                    break;

                case 7153:
                    String myNa = "blank";
                    try {
                        myNa = com.cloudkibo.webrtc.filesharing.Utility.getFileMetaData(tempCameraCaptureHolderString)
                                .getString("name");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(ctx, tempCameraCaptureHolderString , Toast.LENGTH_SHORT).show();
                    gotoHolderFrag(tempCameraCaptureHolderString);
                    tempCameraCaptureHolderString="";

                    break;
            }
        } else {
            Log.e("MainActivity", "Failed to pick contact");
        }

//        super.onActivityResult(requestCode, resultCode, data);
    }

    public void gotoHolderFrag(String path){
        FileAttachmentHolder holder = new FileAttachmentHolder();
        Bundle bundle = new Bundle();

        bundle.putString("authtoken", authtoken);
        bundle.putString("clFragType", "DayStatus");
        bundle.putString("path", path);

        holder.setArguments(bundle);

        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, holder, "fileAttachmentHolderTag")
                .addToBackStack("File Holder")
                .commitAllowingStateLoss();
    }

    public void startJobServiceForOneTimeOnly(String uniqueid){
        ComponentName mServiceComponent = new ComponentName(getContext(), MuteSchedulerService.class);
        JobInfo.Builder builder = new JobInfo.Builder(kJobId++, mServiceComponent);
        //builder.setMinimumLatency(60 * 1000); // wait at least

            builder.setMinimumLatency(DAILY); // maximum delay



        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        //builder.setRequiresDeviceIdle(true); // device should be idle
        builder.setRequiresCharging(false);// we don't care if the device is charging or not
        PersistableBundle bundle = new PersistableBundle();
        bundle.putString("uniqueid", uniqueid);
        bundle.putString("chatType", "remove");
        builder.setExtras(bundle);
        JobScheduler jobScheduler = (JobScheduler) getContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.findItem(R.id.archived).setVisible(false);
            menu.findItem(R.id.settings).setVisible(false);
            menu.findItem(R.id.connect_to_desktop).setVisible(false);
            menu.findItem(R.id.broadcast).setVisible(false);
        }
        menu.clear();
        inflater.inflate(R.menu.newchat, menu);  // Use filter.xml from step 1
        getActivity().getActionBar().setSubtitle(null);
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
    }

    public void sendFileAttachment(final String uniqueid, final String filePath)
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

            UserFunctions userFunctions = new UserFunctions(ctx);
            final String name = com.cloudkibo.webrtc.filesharing.Utility.getFileMetaData(filePath)
                    .getString("name");
            String size = com.cloudkibo.webrtc.filesharing.Utility.getFileMetaData(filePath)
                    .getString("size");
            String type = com.cloudkibo.webrtc.filesharing.Utility.getFileMetaData(filePath)
                    .getString("filetype");
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
                    .setMultipartParameter("uniqueid", uniqueid)
                    .setMultipartParameter("file_name", name)
                    .setMultipartParameter("file_size", size)
                    .setMultipartParameter("label", "label")
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
                                    db.createDaystatusInfo(uniqueid,
                                            com.cloudkibo.webrtc.filesharing.Utility.getFileMetaData(filePath)
                                                    .getString("filetype"),
                                            name, //temp label
                                            name,
                                            filePath,
                                            com.cloudkibo.webrtc.filesharing.Utility.getFileMetaData(filePath)
                                                    .getString("size"),
                                            user.get("phone"));
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

    @Override
    public String getFragmentName() {
        return "Day Status";
    }

    @Override
    public String getFragmentContactPhone() {
        return "About Chat";
    }
}
