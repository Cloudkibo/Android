package com.cloudkibo.ui;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.utils.IFragmentName;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import org.json.JSONException;

import java.io.File;
import java.util.Date;

import static com.cloudkibo.file.filechooser.utils.FileUtils.getExternalStoragePublicDirForImages;

public class DayStatus extends CustomFragment implements IFragmentName {
    String authtoken;
    ListView lv;
    LayoutInflater inflater;
    private String tempCameraCaptureHolderString;
    Context ctx;
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
        Bundle args = getArguments();
        if (args  != null){
            // TODO: 5/27/17 get arguments from fragment it is called in
        }

        FloatingActionButton createStatus = (FloatingActionButton) v.findViewById(R.id.fab_addStatus);
        createStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Action Button clicked", Toast.LENGTH_SHORT).show();
                DayStatusView demo = new DayStatusView();

                getFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, demo, "dayStatusViewTag")
                        .addToBackStack("View Status")
                        .commit();

                //sendImageSelected();

            }
        });

        lv=(ListView) v.findViewById(R.id.listView);


        // TODO: 5/27/17 set Adapter for ListView here. 


        return v;

    }

    private void sendImageSelected() {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

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
                } else if (options[item].equals(R.string.cancel)) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
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
                    // TODO: 6/4/17 Add the image info into dayastatus table

                        MediaScannerConnection.scanFile(ctx, new String[] { tempCameraCaptureHolderString }, new String[] { "image/jpeg" }, null);
                        tempCameraCaptureHolderString = "";
                        //sendFileAttachment(uniqueid, "image"); // TODO: 6/4/17 method to upload the status to server


                    startJobServiceForOneTimeOnly(uniqueid);

                    break;
            }
        } else {
            Log.e("MainActivity", "Failed to pick contact");
        }

//        super.onActivityResult(requestCode, resultCode, data);
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

    // TODO: 5/27/17 Retrieve statuses from db and render on screen via  custom adapter 

    @Override
    public String getFragmentName() {
        return "Day Status";
    }

    @Override
    public String getFragmentContactPhone() {
        return "About Chat";
    }
}
