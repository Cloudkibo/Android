package com.cloudkibo.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.backup.JobSchedulerService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asad on 1/21/17.
 */

public class BackSettingActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    String backup_drive_options[];
    String backup_drive_selected_option = "";
    String backup_over_options[];
    String backup_over_selected_option = "";

    final static int MINUTELY = 60 * 1000;
    final static int HOURLY = 60 * MINUTELY;
    final static int DAILY = 24 * HOURLY;
    final static int WEEKLY = 7 * DAILY;
    final static int MONTHLY = 4 * WEEKLY;

    private static final String TAG = "BackSettingActivity";

    protected static final int REQUEST_CODE_RESOLUTION = 1;

    /**
     * Google API client.
     */
    private GoogleApiClient mGoogleApiClient;

    BackSettingActivity activity;

    Boolean runningOnce = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_backup_setting);

        activity = this;

        backup_drive_selected_option = getString(R.string.never);
        backup_over_selected_option = getString(R.string.wifi_only);
        backup_drive_options = new String[]{getString(R.string.never), getString(R.string.only_when_i_tap_back), getString(R.string.daily), getString(R.string.weekly), getString(R.string.monthly)};
        backup_over_options = new String[]{getString(R.string.wifi_only), getString(R.string.wifi_cellular)};
        updateDefaultValues();

        LinearLayout drive_backup = (LinearLayout) findViewById(R.id.drive_backup);
        drive_backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showGoogleDriveDialog();
            }
        });

        LinearLayout backup_over = (LinearLayout) findViewById(R.id.backup_over);
        backup_over.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBackupOverDialog();
            }
        });
        Button backup_button = (Button) findViewById(R.id.backup_button);
        backup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(getActivity(), CreateFolderActivity.class);
//                startActivity(intent);

                runningOnce = true;
                connectToDrive();

            }
        });

    }

    private void connectToDrive(){
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(activity.getApplicationContext())
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER) // required for App Folder sample
                    .addConnectionCallbacks(activity)
                    .addOnConnectionFailedListener(activity)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    private void updateDefaultValues(){
        TextView drive_backup_text = (TextView) findViewById(R.id.drive_backup_text);
        drive_backup_text.setText(backup_drive_selected_option);

        TextView backup_over_text = (TextView) findViewById(R.id.backup_over_text);
        backup_over_text.setText(backup_over_selected_option);
        SharedPreferences prefs = getSharedPreferences(
                "com.cloudkibo", Context.MODE_PRIVATE);

        prefs.edit().putString("com.cloudkibo.drive_backup_text", backup_drive_selected_option).apply();
        prefs.edit().putString("com.cloudkibo.drive_over_text", backup_over_selected_option).apply();

    }

    private static int kJobId = 0;
    public void startJobService(String period){
        ComponentName mServiceComponent = new ComponentName(getApplicationContext(), JobSchedulerService.class);
        JobInfo.Builder builder = new JobInfo.Builder(kJobId++, mServiceComponent);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // todo change according to selected value - require unmetered network
        builder.setPeriodic(MINUTELY); // todo need to change this.. for testing purpose it is minutely, we would uncomment the following code and comment this line
//        if (period.equals(backup_drive_options[2])) {
//            builder.setPeriodic(DAILY);
//        } else if (period.equals(backup_drive_options[3])) {
//            builder.setPeriodic(WEEKLY);
//        } else if (period.equals(backup_drive_options[4])) {
//            builder.setPeriodic(MONTHLY);
//        }
        builder.setRequiresDeviceIdle(true); // device should be idle
        builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = (JobScheduler) getApplicationContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }

    public void startJobServiceForOneTimeOnly(){
        ComponentName mServiceComponent = new ComponentName(getApplicationContext(), JobSchedulerService.class);
        JobInfo.Builder builder = new JobInfo.Builder(kJobId++, mServiceComponent);
        builder.setMinimumLatency(5 * 1000); // wait at least
        builder.setOverrideDeadline(50 * 1000); // maximum delay
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // todo change according to selected value - require unmetered network
        builder.setRequiresDeviceIdle(true); // device should be idle
        builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = (JobScheduler) getApplicationContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void cancelJobs() {
        JobScheduler tm = (JobScheduler) getApplicationContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        tm.cancel(kJobId);
    }

    private void showBackupOverDialog() {

        // custom dialog
        final Dialog dialog = new Dialog(MainActivity.mainActivity);
        dialog.setTitle("Back up Over");
        dialog.setContentView(R.layout.drive_backup_dialog);

        List<String> stringList=new ArrayList<>();  // here is list
        for (int i=0; i<backup_over_options.length; i++){
            stringList.add(backup_over_options[i]);
        }

        final RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.radio_group);
        Button cancel = (Button) dialog.findViewById(R.id.cancel_drive_backup_dialog);
        for(int i=0;i<stringList.size();i++){
            RadioButton rb=new RadioButton(MainActivity.mainActivity); // dynamically creating RadioButton and adding to RadioGroup.
            rb.setText(stringList.get(i));
            rb.setPadding(20,20,20,20);
            rg.addView(rb);
            if(stringList.get(i).equals(backup_over_selected_option)){
                rb.setChecked(true);
            }
        }
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                RadioButton radioButton = (RadioButton) rg.findViewById(checkedId);
                backup_over_selected_option = radioButton.getText().toString();
                updateDefaultValues();
                dialog.cancel();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        dialog.show();

    }

    String newOption;
    private void showGoogleDriveDialog() {

        // custom dialog
        final Dialog dialog = new Dialog(MainActivity.mainActivity);
        dialog.setTitle(R.string.backup_to_google_drive);
        dialog.setContentView(R.layout.drive_backup_dialog);

        List<String> stringList=new ArrayList<>();  // here is list
        for (int i=0; i<backup_drive_options.length; i++){
            stringList.add(backup_drive_options[i]);
        }

        final RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.radio_group);
        Button cancel = (Button) dialog.findViewById(R.id.cancel_drive_backup_dialog);
        for(int i=0;i<stringList.size();i++){
            RadioButton rb=new RadioButton(MainActivity.mainActivity); // dynamically creating RadioButton and adding to RadioGroup.
            rb.setText(stringList.get(i));
            rb.setPadding(20,20,20,20);
            rg.addView(rb);
            if(stringList.get(i).equals(backup_drive_selected_option)){
                rb.setChecked(true);
            }
        }
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                RadioButton radioButton = (RadioButton) rg.findViewById(checkedId);
                if(!backup_drive_selected_option.equals(radioButton.getText().toString())) {
                    newOption = radioButton.getText().toString();
                    if (newOption.equals(backup_drive_options[0])){
                        if(!backup_drive_selected_option.equals(backup_drive_options[1]))
                            cancelJobs();
                    } else if (newOption.equals(backup_drive_options[1])){
                        if(!backup_drive_selected_option.equals(backup_drive_options[0]))
                            cancelJobs();
                    } else {
                        runningOnce = false;
                        connectToDrive();
                    }
                    backup_drive_selected_option = newOption;
                    updateDefaultValues();
                }
                dialog.cancel();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        dialog.show();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "GoogleApiClient connected");
        Toast.makeText(getApplicationContext(), "Connected to google drive", Toast.LENGTH_LONG).show();


        if(runningOnce) startJobServiceForOneTimeOnly();
        else startJobService(newOption);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient suspended");
        Toast.makeText(getApplicationContext(), "Suspended connection to google drive", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == RESULT_OK) {
            mGoogleApiClient.connect();
        }
    }
}
