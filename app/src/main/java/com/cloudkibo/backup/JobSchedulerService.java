package com.cloudkibo.backup;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.IntentSender;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import static android.app.Activity.RESULT_OK;

/**
 * Created by sojharo on 08/01/2017.
 */

public class JobSchedulerService extends JobService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "SyncService";
    private int number;
    private static final String MIME_PHOTO = "";
    private static final String MIME_VIDEO = "";
    MainActivity mainActivity = new MainActivity();
    protected static final int REQUEST_CODE_RESOLUTION = 1;
    GoogleApiClient mGoogleApiClient;


    // todo this for testing only and will make the beep sound on defined intervals
    // We would use the wrapper class to talk to Google Drive to do backups when user
    // selects to do backup on interval basis - wrapper class is under construction
    // This will call the methods of the wrapper class to talk to Google Drive
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.i(TAG, "on start job: " + jobParameters.getJobId());

        final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.bell);
        mp.start();



        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER) // required for App Folder sample
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();





        return false;
    }



//
//    //Following is the code moved from BackUp files
//
    ResultCallback<DriveFolder.DriveFolderResult> folderCreatedCallback = new
            ResultCallback<DriveFolder.DriveFolderResult>() {
                @Override
                public void onResult(DriveFolder.DriveFolderResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Error while trying to create the folder");
                        return;
                    }
                    showMessage("Created a folder: " + result.getDriveFolder().getDriveId());
                }
            };

    public void CreateKiboFolder(){
        Toast.makeText(getApplicationContext(), "Drive Connected", Toast.LENGTH_SHORT).show();


        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle("KiboChat "+number).build();
        Drive.DriveApi.getAppFolder(mGoogleApiClient).createFolder(
                mGoogleApiClient, changeSet).setResultCallback(folderCreatedCallback);
        number++;
    }
//
//    private void saveFiletoDrive(final File file, final String mime) {
//
//        Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(
//                new ResultCallback<DriveApi.DriveContentsResult>() {
//                    @Override
//                    public void onResult(DriveApi.DriveContentsResult result) {
//                        // If the operation was not successful, we cannot do
//                        // anything
//                        // and must
//                        // fail.
//                        if (!result.getStatus().isSuccess()) {
//                            Log.i(TAG, "Failed to create new contents.");
//                            return;
//                        }
//                        Log.i(TAG, "Connection successful, creating new contents...");
//                        // Otherwise, we can write our data to the new contents.
//                        // Get an output stream for the contents.
//                        OutputStream outputStream = result.getDriveContents()
//                                .getOutputStream();
//                        FileInputStream fis;
//                        try {
//                            fis = new FileInputStream(file.getPath());
//                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                            byte[] buf = new byte[1024];
//                            int n;
//                            while (-1 != (n = fis.read(buf)))
//                                baos.write(buf, 0, n);
//                            byte[] photoBytes = baos.toByteArray();
//                            outputStream.write(photoBytes);
//
//                            outputStream.close();
//                            outputStream = null;
//                            fis.close();
//                            fis = null;
//
//                        } catch (FileNotFoundException e) {
//                            Log.w(TAG, "FileNotFoundException: " + e.getMessage());
//                        } catch (IOException e1) {
//                            Log.w(TAG, "Unable to write file contents." + e1.getMessage());
//                        }
//
//                        String title = file.getName();
//                        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
//                                .setMimeType(mime).setTitle(title).build();
//
//                        if (mime.equals(MIME_PHOTO)) { //Please assign value to MIME_photo at top
//
//                            Log.i(TAG, "Creating new photo on Drive (" + title
//                                    + ")");
//                            Drive.DriveApi.getFolder(mainActivity.getGoogleApiClient(),
//                                    result.getDriveContents().getDriveId()).createFile(mainActivity.getGoogleApiClient(),
//                                    metadataChangeSet,
//                                    result.getDriveContents());
//                        } else if (mime.equals(MIME_VIDEO)) { //Please assign value to MIME_video at top
//                            Log.i(TAG, "Creating new video on Drive (" + title
//                                    + ")");
//                            Drive.DriveApi.getFolder(mainActivity.getGoogleApiClient(),
//                                    result.getDriveContents().getDriveId()).createFile(mainActivity.getGoogleApiClient(),
//                                    metadataChangeSet,
//                                    result.getDriveContents());
//                        }
//
//                    }
//                });
//    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        //super.onConnected(connectionHint);
        Log.i("BaseDriveActivity", "On COnneceted: ");
        CreateKiboFolder();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.mainActivity, result.getErrorCode(), 0).show();
            return;
        }
        try {
            result.startResolutionForResult(MainActivity.mainActivity, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
