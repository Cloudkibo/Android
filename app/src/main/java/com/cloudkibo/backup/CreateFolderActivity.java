package com.cloudkibo.backup;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * An activity to illustrate how to create a new folder.
 */
public class CreateFolderActivity extends BaseActivity {

    public  final String TAG = "CreateFolderActivity";
    /**
     * Temporarily using a hard code string to get a single
     * group image later a list of group id's will be passed
     */
    public  final String group_image = "3ddadsadasd0";

    private GoogleApiClient mGoogleApiClient;
    ResultCallback<DriveFolder.DriveFolderResult> folderCreatedCallback = new
            ResultCallback<DriveFolder.DriveFolderResult>() {
                @Override
                public void onResult(DriveFolder.DriveFolderResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Error while trying to create the folder");
                        return;
                    }
                    showMessage("Created KiboChat folder: " + result.getDriveFolder().getDriveId());
                    createGroupIconsFolder(result.getDriveFolder().getDriveId());
                }
            };
    ResultCallback<DriveFolder.DriveFolderResult> groupIconsFolderCreatedCallback = new
            ResultCallback<DriveFolder.DriveFolderResult>() {
                @Override
                public void onResult(DriveFolder.DriveFolderResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Error while trying to create the folder");
                        return;
                    }
                    showMessage("Created GroupIcons folder: " + result.getDriveFolder().getDriveId());

                    Drive.DriveApi.newDriveContents(getGoogleApiClient())
                            .setResultCallback(driveContentsCallback);
                }
            };

    final private ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback = new
            ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Error while trying to create new file contents");
                        return;
                    }
                    final DriveContents driveContents = result.getDriveContents();

                    // Perform I/O off the UI thread.
                    new Thread() {
                        @Override
                        public void run() {
                            // write content to DriveContents

                            //Reads the file from the SD card ()
                            File mypath=new File(getApplicationContext().getFilesDir(),group_image);
                            Bitmap src= BitmapFactory.decodeFile(mypath.getPath());
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            src.compress(Bitmap.CompressFormat.PNG, 100, baos);
                            OutputStream outputStream = driveContents.getOutputStream();
                            Writer writer = new OutputStreamWriter(outputStream);
                            try {
                                outputStream.write(baos.toByteArray());
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.e(TAG, e.getMessage());
                            }
                            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                    .setTitle(group_image)
                                    .setMimeType("text/plain")
                                    .setStarred(true).build();

                            // create a file on root folder
                            Drive.DriveApi.getRootFolder(getGoogleApiClient())
                                    .createFile(getGoogleApiClient(), changeSet, driveContents)
                                    .setResultCallback(fileCallback);
                        }
                    }.start();
                }
            };


    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Error while trying to create the file");
                        return;
                    }
                    showMessage("Created a file with content: " + result.getDriveFile().getDriveId());
                }
            };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);
        Log.i("BaseDriveActivity", "On COnneceted: ");
        createKiboChatFolder();
    }

    public void createKiboChatFolder(){
        Toast.makeText(getApplicationContext(), "Creating KiboChat Folder", Toast.LENGTH_LONG).show();
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle("KiboChat").build();
        Drive.DriveApi.getAppFolder(getGoogleApiClient()).createFolder(
                getGoogleApiClient(), changeSet).setResultCallback(folderCreatedCallback);
    }

    public void createGroupIconsFolder(DriveId kiboChatFolderId){
        Toast.makeText(getApplicationContext(), "Creating Group Icons Folder", Toast.LENGTH_LONG).show();
        DriveFolder folder = Drive.DriveApi.getFolder(getGoogleApiClient(), kiboChatFolderId);
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle("GroupIcons").build();
        folder.createFolder(getGoogleApiClient(), changeSet).setResultCallback(groupIconsFolderCreatedCallback);
    }


}

