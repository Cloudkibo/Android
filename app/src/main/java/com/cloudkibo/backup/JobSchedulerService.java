package com.cloudkibo.backup;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.IntentSender;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.file.filechooser.utils.FileUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.MetadataChangeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import static android.app.Activity.RESULT_OK;

/**
 * Created by sojharo on 08/01/2017.
 */

public class JobSchedulerService extends JobService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "JobScheduler";
    MainActivity mainActivity = new MainActivity();
    protected static final int REQUEST_CODE_RESOLUTION = 1;
    GoogleApiClient mGoogleApiClient;
    private FileUtils fileUtils;

    DriveId groupIconsFolder;
    DriveId imagesAttachmentFolder;
    DriveId documentAttachmentFolder;
    DriveId audioAttachmentFolder;
    DriveId videoAttachmentFolder;
    DriveId sqliteTablesFolder;


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


    final private ResultCallback<DriveApi.DriveContentsResult> driveContentCallBack =
            new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(@NonNull DriveApi.DriveContentsResult result) {
                    if(!result.getStatus().isSuccess()){
                        showMessage("Error while creating a file");
                        return;
                    }

                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle("appconfig.txt")
                            .setMimeType("text/plain")
                            .build();
                    Drive.DriveApi.getAppFolder(mGoogleApiClient)
                            .createFile(mGoogleApiClient, changeSet, result.getDriveContents())
                            .setResultCallback(fileCallBack);
                }
            };
    final private ResultCallback<DriveFolder.DriveFileResult> fileCallBack =
            new ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(@NonNull DriveFolder.DriveFileResult result) {
                    if(!result.getStatus().isSuccess()){
                        showMessage("Error while trying to create a file");
                        return;
                    }

                    showMessage("Created a file in App Folder: " + result.getDriveFile().getDriveId());

                }
            };

    public void CreateKiboAppFolder(){
        Toast.makeText(getApplicationContext(), "Drive Connected", Toast.LENGTH_SHORT).show();
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle("KiboChat").build();

        Drive.DriveApi.getAppFolder(mGoogleApiClient).createFolder(
                mGoogleApiClient, changeSet).setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
            @Override
            public void onResult(@NonNull DriveFolder.DriveFolderResult result) {
                if (!result.getStatus().isSuccess()) {
                    showMessage("Error while trying to create the folder");
                    return;
                }
                showMessage("Created app folder: " + result.getDriveFolder().getDriveId());

                createGroupIconsFolder(result.getDriveFolder().getDriveId());
                createImagesAttachmentFolder(result.getDriveFolder().getDriveId());
                createDocumentAttachmentFolder(result.getDriveFolder().getDriveId());
                createAudioAttachmentFolder(result.getDriveFolder().getDriveId());
                createVideoAttachmentFolder(result.getDriveFolder().getDriveId());
                createSqliteFolder(result.getDriveFolder().getDriveId());
            }
        });

    }

    public void CreateKiboFolder(){
        Toast.makeText(getApplicationContext(), "Drive Connected", Toast.LENGTH_SHORT).show();


        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle("KiboChat").build();
        Drive.DriveApi.getRootFolder(mGoogleApiClient).createFolder(
                mGoogleApiClient, changeSet).setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
            @Override
            public void onResult(@NonNull DriveFolder.DriveFolderResult result) {
                if (!result.getStatus().isSuccess()) {
                    showMessage("Error while trying to create the folder");
                    return;
                }
                showMessage("Created a folder: " + result.getDriveFolder().getDriveId());

                createGroupIconsFolder(result.getDriveFolder().getDriveId());
                createImagesAttachmentFolder(result.getDriveFolder().getDriveId());
                createDocumentAttachmentFolder(result.getDriveFolder().getDriveId());
                createAudioAttachmentFolder(result.getDriveFolder().getDriveId());
                createVideoAttachmentFolder(result.getDriveFolder().getDriveId());
                createSqliteFolder(result.getDriveFolder().getDriveId());
            }
        });

    }

    public void createGroupIconsFolder(DriveId kiboChatFolderId){
        Toast.makeText(getApplicationContext(), "Creating Group Icons Folder", Toast.LENGTH_LONG).show();
        DriveFolder folder = Drive.DriveApi.getFolder(mGoogleApiClient, kiboChatFolderId);
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle("GroupIcons").build();
        folder.createFolder(mGoogleApiClient, changeSet).setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
            @Override
            public void onResult(@NonNull DriveFolder.DriveFolderResult result) {
                if (!result.getStatus().isSuccess()) {
                    showMessage("Error while trying to create the folder");
                    return;
                }
                showMessage("Created GroupIcons folder: " + result.getDriveFolder().getDriveId());
                groupIconsFolder = result.getDriveFolder().getDriveId();

                //Drive.DriveApi.newDriveContents(mGoogleApiClient)
                //        .setResultCallback(driveContentsCallback);
            }
        });
    }
    public void createImagesAttachmentFolder(DriveId kiboChatFolderId){
        Toast.makeText(getApplicationContext(), "Creating Images Attachment Folder", Toast.LENGTH_LONG).show();
        DriveFolder folder = Drive.DriveApi.getFolder(mGoogleApiClient, kiboChatFolderId);
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle("ImageAttachment").build();
        folder.createFolder(mGoogleApiClient, changeSet).setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
            @Override
            public void onResult(@NonNull DriveFolder.DriveFolderResult result) {
                if (!result.getStatus().isSuccess()) {
                    showMessage("Error while trying to create the folder");
                    return;
                }
                showMessage("Created ImageAttachment folder: " + result.getDriveFolder().getDriveId());
                imagesAttachmentFolder = result.getDriveFolder().getDriveId();
                uploadFiles("image");

                //Drive.DriveApi.newDriveContents(mGoogleApiClient)
                //        .setResultCallback(driveContentsCallback);
            }
        });
    }
    public void createDocumentAttachmentFolder(DriveId kiboChatFolderId){
        Toast.makeText(getApplicationContext(), "Creating Document Attachment Folder", Toast.LENGTH_LONG).show();
        DriveFolder folder = Drive.DriveApi.getFolder(mGoogleApiClient, kiboChatFolderId);
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle("DocumentAttachment").build();
        folder.createFolder(mGoogleApiClient, changeSet).setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
            @Override
            public void onResult(@NonNull DriveFolder.DriveFolderResult result) {
                if (!result.getStatus().isSuccess()) {
                    showMessage("Error while trying to create the folder");
                    return;
                }
                showMessage("Created DocumentAttachment folder: " + result.getDriveFolder().getDriveId());
                documentAttachmentFolder = result.getDriveFolder().getDriveId();
                uploadFiles("document");

                //Drive.DriveApi.newDriveContents(mGoogleApiClient)
                //        .setResultCallback(driveContentsCallback);
            }
        });
    }
    public void createAudioAttachmentFolder(DriveId kiboChatFolderId){
        Toast.makeText(getApplicationContext(), "Creating Audio Attachment Folder", Toast.LENGTH_LONG).show();
        DriveFolder folder = Drive.DriveApi.getFolder(mGoogleApiClient, kiboChatFolderId);
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle("AudioAttachment").build();
        folder.createFolder(mGoogleApiClient, changeSet).setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
            @Override
            public void onResult(@NonNull DriveFolder.DriveFolderResult result) {
                if (!result.getStatus().isSuccess()) {
                    showMessage("Error while trying to create the folder");
                    return;
                }
                showMessage("Created AudioAttachment folder: " + result.getDriveFolder().getDriveId());
                audioAttachmentFolder = result.getDriveFolder().getDriveId();
                uploadFiles("audio");

                //Drive.DriveApi.newDriveContents(mGoogleApiClient)
                //        .setResultCallback(driveContentsCallback);
            }
        });
    }
    public void createVideoAttachmentFolder(DriveId kiboChatFolderId){
        Toast.makeText(getApplicationContext(), "Creating Video Attachment Folder", Toast.LENGTH_LONG).show();
        DriveFolder folder = Drive.DriveApi.getFolder(mGoogleApiClient, kiboChatFolderId);
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle("VideoAttachment").build();
        folder.createFolder(mGoogleApiClient, changeSet).setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
            @Override
            public void onResult(@NonNull DriveFolder.DriveFolderResult result) {
                if (!result.getStatus().isSuccess()) {
                    showMessage("Error while trying to create the folder");
                    return;
                }
                showMessage("Created VideoAttachment folder: " + result.getDriveFolder().getDriveId());
                videoAttachmentFolder = result.getDriveFolder().getDriveId();
                uploadFiles("video");

                //Drive.DriveApi.newDriveContents(mGoogleApiClient)
                //        .setResultCallback(driveContentsCallback);
            }
        });
    }
    public void createSqliteFolder(DriveId kiboChatFolderId){
        Toast.makeText(getApplicationContext(), "Creating Sqlite Tables Folder", Toast.LENGTH_LONG).show();
        DriveFolder folder = Drive.DriveApi.getFolder(mGoogleApiClient, kiboChatFolderId);
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle("Sqlite").build();
        folder.createFolder(mGoogleApiClient, changeSet).setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
            @Override
            public void onResult(@NonNull DriveFolder.DriveFolderResult result) {
                if (!result.getStatus().isSuccess()) {
                    showMessage("Error while trying to create the folder");
                    return;
                }
                showMessage("Created Sqlite folder: " + result.getDriveFolder().getDriveId());
                sqliteTablesFolder = result.getDriveFolder().getDriveId();

                //Drive.DriveApi.newDriveContents(mGoogleApiClient)
                //        .setResultCallback(driveContentsCallback);
            }
        });
    }


    private void uploadFiles(String type){
        DatabaseHandler db = new DatabaseHandler(this);

        try{
            JSONArray files = db.getAllFiles(type);
            Toast.makeText(this,"in Uploading file", Toast.LENGTH_SHORT);
            for(int i=0; i < files.length(); i++) {
                JSONObject row = files.getJSONObject(i);

                Uri url = Uri.parse(row.getString("path"));
                String mime = row.getString("file_type")+"/"+row.getString("file_ext").substring(1);

                Toast.makeText(this, "mime " + mime, Toast.LENGTH_SHORT);

                //File file = fileUtils.getFile(this, url);
                File file = new File(url.getPath());

                Toast.makeText(this, "file path " + url, Toast.LENGTH_SHORT);

                saveFiletoDrive(file, mime, type);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveFiletoDrive(final File file, final String mime, final String type) {

        Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(
                new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(DriveApi.DriveContentsResult result) {
                        // If the operation was not successful, we cannot do
                        // anything
                        // and must
                        // fail.
                        if (!result.getStatus().isSuccess()) {
                            Log.i(TAG, "Failed to create new contents.");
                            return;
                        }
                        Log.i(TAG, "Connection successful, creating new contents...");
                        // Otherwise, we can write our data to the new contents.
                        // Get an output stream for the contents.
                        OutputStream outputStream = result.getDriveContents()
                                .getOutputStream();
                        FileInputStream fis;
                        try {
                            fis = new FileInputStream(file.getPath());
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            byte[] buf = new byte[1024];
                            int n;
                            while (-1 != (n = fis.read(buf)))
                                baos.write(buf, 0, n);
                            byte[] photoBytes = baos.toByteArray();
                            outputStream.write(photoBytes);

                            outputStream.close();
                            outputStream = null;
                            fis.close();
                            fis = null;

                        } catch (FileNotFoundException e) {
                            Log.w(TAG, "FileNotFoundException: " + e.getMessage());
                        } catch (IOException e1) {
                            Log.w(TAG, "Unable to write file contents." + e1.getMessage());
                        }

                        DriveFolder folder = Drive.DriveApi.getFolder(mGoogleApiClient, imagesAttachmentFolder);
                        if(type.equals("document")) {
                            folder = Drive.DriveApi.getFolder(mGoogleApiClient, documentAttachmentFolder);
                        } else if (type.equals("audio")) {
                            folder = Drive.DriveApi.getFolder(mGoogleApiClient, audioAttachmentFolder);
                        } else if (type.equals("video")) {
                            folder = Drive.DriveApi.getFolder(mGoogleApiClient, videoAttachmentFolder);
                        }

                            String title = file.getName();
                        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                                .setMimeType(mime).setTitle(title).build();


                            folder.createFile(mGoogleApiClient,
                                    metadataChangeSet,
                                    result.getDriveContents());

                        showMessage("file "+ title + "");

                    }
                });
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        //super.onConnected(connectionHint);
        Log.i("BaseDriveActivity", "On COnneceted: ");
        CreateKiboFolder();
        //CreateKiboAppFolder();
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
