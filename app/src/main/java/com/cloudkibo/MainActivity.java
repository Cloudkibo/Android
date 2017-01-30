package com.cloudkibo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Build;
import android.provider.ContactsContract;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.cloudkibo.custom.CustomActivity;
import com.cloudkibo.database.BoundKiboSyncListener;
import com.cloudkibo.database.ContactService;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.database.KiboSyncService;
import com.cloudkibo.file.filechooser.utils.FileUtils;
import com.cloudkibo.library.GroupUtility;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.library.Utility;
import com.cloudkibo.model.Data;
//import io.cordova.hellocordova.CordovaApp;

import com.cloudkibo.push.MyHandler;
import com.cloudkibo.push.NotificationSettings;
import com.cloudkibo.push.RegistrationIntentService;
import com.cloudkibo.socket.BoundServiceListener;
import com.cloudkibo.socket.SocketService;
import com.cloudkibo.socket.SocketService.SocketBinder;
import com.cloudkibo.ui.AboutChat;
import com.cloudkibo.ui.AddMembers;
import com.cloudkibo.ui.CallHistory;
import com.cloudkibo.ui.ChatList;
import com.cloudkibo.ui.ContactList;
import com.cloudkibo.ui.ContactListPending;
import com.cloudkibo.ui.GroupChat;
import com.cloudkibo.ui.GroupChatUI;
import com.cloudkibo.ui.GroupSetting;
import com.cloudkibo.ui.LeftNavAdapter;
import com.cloudkibo.utils.IFragmentName;
import com.cloudkibo.webrtc.call.OutgoingCall;
import com.cloudkibo.webrtc.filesharing.FileConnection;
import com.cloudkibo.file.filechooser.utils.Base64;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.microsoft.windowsazure.notifications.NotificationsManager;




/**
 * The Class MainActivity is the base activity class of the application. This
 * activity is launched after the Splash and it holds all the Fragments used in
 * the app. It also creates the Navigation Drawer on left side.
 */
public class MainActivity extends CustomActivity
{

    SocketService socketService;
    KiboSyncService kiboSyncService;
    boolean isBound = false;
    boolean kiboServiceIsBound = false;

    /** The drawer layout. */
    private DrawerLayout drawerLayout;

    /** ListView for left side drawer. */
    private ListView drawerLeft;

    /** The drawer toggle. */
    private ActionBarDrawerToggle drawerToggle;

    /** Store Authentication Token **/
    String authtoken;

    private String room = "globalchatroom";

    UserFunctions userFunction;

    Dialog dialog;

    public String filePeer;
    public String fileData;
    public Boolean initiatorFileTransfer;

    HashMap<String, String> user;

    //AccountManager am;
    //Account account;

    public static final long SECONDS_PER_MINUTE = 60L;
    public static final long SYNC_INTERVAL_IN_MINUTES = 1440L;
    public static final long SYNC_INTERVAL =
            SYNC_INTERVAL_IN_MINUTES *
                    SECONDS_PER_MINUTE;

    private static final int REQUEST_CHOOSER = 1105;

    // Push Notification
    public static MainActivity mainActivity;
    public static Boolean isVisible = false;
    private static final String TAG = "MainActivity";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    // Push Notification

    Boolean shouldSync;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    final Map<String, String> photo_uri = new HashMap<>();

    //private static final int PLACE_PICKER_REQUEST = 1200;
    //private GoogleApiClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mainActivity = this;
        NotificationsManager.handleNotifications(this, NotificationSettings.SenderId, MyHandler.class);
        registerWithNotificationHubs();

        authtoken = getIntent().getExtras().getString("authtoken");
        shouldSync = getIntent().getExtras().getBoolean("sync");
       // this.updateChatList();
        //this.updatePartialContactList();


        setupContainer();
        setupDrawer();


        userFunction = new UserFunctions();

        if(userFunction.isUserLoggedIn(getApplicationContext()));
            getUserFromSQLiteDatabase();

        //am = AccountManager.get(MainActivity.this);
        //account = am.getAccountsByType(AccountGeneral.ACCOUNT_TYPE)[0];

        //if(!ContentResolver.isSyncActive(account, CloudKiboDatabaseContract.AUTHORITY)) {

        	/* this starts syncing on very short intervals */

        //ContentResolver.setSyncAutomatically(account, CloudKiboDatabaseContract.AUTHORITY, true);
        //ContentResolver.requestSync(account, CloudKiboDatabaseContract.AUTHORITY, new Bundle());

        //ContentResolver.addPeriodicSync(account, CloudKiboDatabaseContract.AUTHORITY, Bundle.EMPTY, SYNC_INTERVAL);

        //fetchUserFromServerForFirstTime();
        //}
        //else {
        startSocketService();
        //}
        if(shouldSync) {
            startSyncService();
        }

        startContactsObserverService();

//        Utility utility = new Utility();
//        utility.updateDatabaseWithContactImages(getApplicationContext(),new ArrayList<String>());

    }

    public boolean isRTL(Context ctx) {
        Configuration config = ctx.getResources().getConfiguration();
        return config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    int countRetryConnectingSocket = 0;
    public void startSyncService(){
        Intent intentSync = new Intent(getApplicationContext(), KiboSyncService.class);
        bindService(intentSync, kiboSyncConnection, Context.BIND_AUTO_CREATE);
        if (kiboServiceIsBound) {
            kiboSyncService.startIncrementalSyncWithoutAddressBookAccess(authtoken);
        }
        /*if(socketService != null) {
            if (socketService.isSocketConnected()) {
                Intent intentSync = new Intent(getApplicationContext(), KiboSyncService.class);
                bindService(intentSync, kiboSyncConnection, Context.BIND_AUTO_CREATE);
                if (kiboServiceIsBound) {
                    kiboSyncService.startIncrementalSyncWithoutAddressBookAccess(authtoken);
                }
            } else {
                if(countRetryConnectingSocket > 2){
                    countRetryConnectingSocket = 0;
                    return ;
                }
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                startSyncService();
                            }
                        },
                        1000);
                countRetryConnectingSocket++;
            }
        } else {
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            startSyncService();
                        }
                    },
                    1000);
        }*/
    }

    public void syncContacts(){
        //startSocketService();
        reconnectSocket();
        if (socketService.isSocketConnected()) {
            Intent intentSync = new Intent(getApplicationContext(), KiboSyncService.class);
            bindService(intentSync, kiboSyncConnection, Context.BIND_AUTO_CREATE);
            if (kiboServiceIsBound) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
                    //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
                } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
                    ToastNotify2("Can't refresh contacts without permission.");
                } else {
                    loadContactsFromAddressBook();
                }
            }
        } else {
            if(countRetryConnectingSocket > 2){
                countRetryConnectingSocket = 0;
                ToastNotify2("Can't sync at the moment.");
                return ;
            }
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            syncContacts();
                        }
                    },
                    1000);
            countRetryConnectingSocket++;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                if(kiboServiceIsBound){
                    loadContactsFromAddressBook();
                }
            } else {
                ToastNotify2(getString(R.string.main_activity_contacts_permission_denied));
            }
        } else if (requestCode == 101) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                uploadChatAttachmentFileChooser();
            } else {
                ToastNotify2(getString(R.string.main_activity_file_permission_denied));
            }
        } else if (requestCode == 102) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                uploadIconChooser();
            } else {
                ToastNotify2(getString(R.string.main_activity_file_permission_denied));
            }
        } else if (requestCode == 103) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                IFragmentName myFragment3 = (IFragmentName) getSupportFragmentManager().findFragmentById(R.id.content_frame);
                if(myFragment3 == null) return;
                if(myFragment3.getFragmentName().equals("GroupChat")){
                    final GroupChat myGroupChatListFragment = (GroupChat) myFragment3;
                    myGroupChatListFragment.uploadImageFromCamera();
                }
            } else {
                //ToastNotify2(getString(R.string.main_activity_file_permission_denied));
            }
        }
    }

    public void startSocketService(){
		/*
         * Binding the service to only activity and not fragment
         * http://stackoverflow.com/questions/24309379/bind-service-to-activity-or-fragment
         */

        Intent i = new Intent(this, SocketService.class);
        i.putExtra("user", user);
        i.putExtra("room", room);
        startService(i);
        bindService(i, socketConnection, Context.BIND_AUTO_CREATE);
    }

    public void stopSocketService(){
        Intent i = new Intent(this, SocketService.class);
        stopService(i);
    }

    public void reconnectSocket(){
        if(socketService != null){
            if(isBound){
                socketService.setSocketIOConfig();
            } else {
                startSocketService();
            }
        } else {
            startSocketService();
        }
    }

    public void startContactsObserverService(){
        Intent i = new Intent(this, ContactService.class);
        i.putExtra("authtoken", authtoken);
        startService(i);
    }

    public void stopContactsObserverService(){
        Intent i = new Intent(this, ContactService.class);
        stopService(i);
    }

    @Override
    protected void onDestroy() {

        if(isBound){
            unbindService(socketConnection);
            //stopSocketService();
        }

        if(kiboServiceIsBound) unbindService(kiboSyncConnection);

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        startSocketService();

        //reconnectSocket();

        super.onResume();

        isVisible = true;

        IFragmentName myFragment = (IFragmentName) getSupportFragmentManager().findFragmentById(R.id.content_frame);

        if(myFragment == null) return;
        if(myFragment.getFragmentName().equals("GroupChat"))
        {
            final GroupChat myGroupChatFragment = (GroupChat) myFragment;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    myGroupChatFragment.loadConversationList();
                                }
                            },
                            1000);
                }
            });
        } else if(myFragment.getFragmentName().equals("ChatList")) {
            final ChatList myChatListFragment = (ChatList) myFragment;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    myChatListFragment.loadChatList();
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        isVisible = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isVisible = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isVisible = false;
    }

    /**
     * Setup the drawer layout. This method also includes the method calls for
     * setting up the Left side drawer.
     */
    private void setupDrawer()
    {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final boolean isRTL  =isRTL(getApplicationContext());
        if(isRTL){
            drawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                    Gravity.RIGHT);
        }else{
            drawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                    GravityCompat.START);
        }

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open,
                R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View view)
            {
                setActionBarTitle();
            }

            @Override
            public void onDrawerOpened(View drawerView)
            {
                getActionBar().setTitle("KiboChat");
            }
            @Override
            public boolean onOptionsItemSelected(MenuItem item) {

                if(isRTL){
                if (item != null && item.getItemId() == android.R.id.home) {
                    if (drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                        drawerLayout.closeDrawer(Gravity.RIGHT);
                    } else {
                        drawerLayout.openDrawer(Gravity.RIGHT);
                    }
                }
                }else {
                    super.onOptionsItemSelected(item);
                }
                return false;
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
        drawerLayout.closeDrawers();

        setupLeftNavDrawer();
    }

    /**
     * Setup the left navigation drawer/slider. You can add your logic to load
     * the contents to be displayed on the left side drawer. You can also setup
     * the Header and Footer contents of left drawer if you need them.
     */
    private void setupLeftNavDrawer()
    {
        drawerLeft = (ListView) findViewById(R.id.left_drawer);

        View header = getLayoutInflater().inflate(R.layout.left_nav_header,
                null);
        drawerLeft.addHeaderView(header);
        if(isRTL(getApplicationContext())) {
            DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) drawerLeft.getLayoutParams();
            params.gravity = Gravity.END;
            drawerLeft.setLayoutParams(params);
        }
        drawerLeft.setAdapter(new LeftNavAdapter(this, getDummyLeftNavItems()));

        drawerLeft.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
                                    long arg3) {
                drawerLayout.closeDrawers();
                launchFragment(pos);
            }
        });



        drawerLayout.openDrawer(drawerLeft);

    }

    /**
     * This method returns a list of dummy items for left navigation slider. You
     * can write or replace this method with the actual implementation for list
     * items.
     *
     * @return the dummy items
     */
    private ArrayList<Data> getDummyLeftNavItems()
    {
        ArrayList<Data> al = new ArrayList<Data>();
        al.add(new Data(getString(R.string.side_menu_chat), null, R.drawable.ic_chat));
        al.add(new Data(getString(R.string.side_menu_contacts), null, R.drawable.ic_notes));
        al.add(new Data(getString(R.string.side_menu_calls_history), null, android.R.drawable.sym_action_call));
        al.add(new Data("Invite", null, R.drawable.ic_notes));
        al.add(new Data(getString(R.string.side_menu_create_group), null, R.drawable.ic_about));
        //al.add(new Data("Add Requests", null, R.drawable.ic_projects));
        //al.add(new Data("Conference", null, R.drawable.group1));
        //al.add(new Data("Settings", null, R.drawable.ic_setting));
        al.add(new Data(getString(R.string.side_menu_about), null, R.drawable.ic_about));
        //al.add(new Data("Logout", null, R.drawable.ic_logout));
        return al;
    }

    /**
     * This method can be used to attach Fragment on activity view for a
     * particular tab position. You can customize this method as per your need.
     *
     * @param pos
     *            the position of tab selected.
     */
    private void launchFragment(int pos)
    {
        Fragment f = null;
        String title = null;
        if (pos == 1)
        {
            title = getString(R.string.side_menu_chat);
            f = new ChatList();
        }
        else if(pos == 2){
            title = getString(R.string.side_menu_contacts);
            f = new ContactList();
        }
        else if(pos == 3){
            title = getString(R.string.side_menu_calls_history);
            f = new CallHistory();
        }
        else if(pos == 4){
            title = getString(R.string.side_menu_contacts_invite);
            f = new ContactListPending();
        }
        else if(pos == 5){
            title = getString(R.string.side_menu_create_group);
            f = new AddMembers();
        }

        else if(pos == -5){ // this is removing of conference tab

            // get prompts.xml view
            LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());

            View promptView = layoutInflater.inflate(R.layout.prompt_conference_name, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            final EditText input = (EditText) promptView.findViewById(R.id.userInput);

            // set prompts.xml to be the layout file of the alertdialog builder
            alertDialogBuilder.setView(promptView);

            // setup a dialog window
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // get user input and send it to server
                            Log.w("SOJHARO", "VALUE = "+ input.getText());

                            Intent i = new Intent(getApplicationContext(), com.cloudkibo.webrtc.conference.VideoCallView.class);
                            i.putExtra("authtoken", authtoken);
                            i.putExtra("user", user);
                            i.putExtra("room", input.getText().toString());
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();


                        }
                    })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,	int id) {
                                    dialog.cancel();
                                }
                            });

            // create an alert dialog
            AlertDialog alertD = alertDialogBuilder.create();

            alertD.show();
        }
        else if (pos == 6)
        {
            title = getString(R.string.side_menu_about);
            f = new AboutChat();
        }
        if (f != null)
        {

            while (getSupportFragmentManager().getBackStackEntryCount() > 0)
            {
                getSupportFragmentManager().popBackStackImmediate();
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, f).addToBackStack(title)
                    .commit();
        }
    }

    public void createContact () {
        Intent i = new Intent(Intent.ACTION_INSERT);
        i.setType(ContactsContract.Contacts.CONTENT_TYPE);
        if (Integer.valueOf(Build.VERSION.SDK) > 14)
            i.putExtra("finishActivityOnSaveCompleted", true); // Fix for 4.0.3 +
        startActivityForResult(i, 5123);
    }

    String phoneOfContactToAdd = "";
    public void createContact (String phone) {
        phoneOfContactToAdd = phone;
        Intent i = new Intent(Intent.ACTION_INSERT);
        i.setType(ContactsContract.Contacts.CONTENT_TYPE);
        i.putExtra(ContactsContract.Intents.Insert.PHONE, phone);
        if (Integer.valueOf(Build.VERSION.SDK) > 14)
            i.putExtra("finishActivityOnSaveCompleted", true); // Fix for 4.0.3 +
        startActivityForResult(i, 5123);
    }

    public void createContact (String phone, String name) {
        phoneOfContactToAdd = phone;
        Intent i = new Intent(Intent.ACTION_INSERT);
        i.setType(ContactsContract.Contacts.CONTENT_TYPE);
        i.putExtra(ContactsContract.Intents.Insert.PHONE, phone);
        i.putExtra(ContactsContract.Intents.Insert.NAME, name);
        if (Integer.valueOf(Build.VERSION.SDK) > 14)
            i.putExtra("finishActivityOnSaveCompleted", true); // Fix for 4.0.3 +
        startActivityForResult(i, 5123);
    }

    String icon_upload_group_id = "";
    public void uploadIcon(String group_id){
        icon_upload_group_id = group_id;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 102);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            uploadIconChooser();
        }
    }
    private void uploadIconChooser(){
        Intent getContentIntent = FileUtils.createGetContentIntentForImage();

        Intent intent = Intent.createChooser(getContentIntent, "Select an image");
        startActivityForResult(intent, 111);
    }

    String attachmentType = "";
    public void uploadChatAttachment(String type){
        attachmentType = type;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            uploadChatAttachmentFileChooser();
        }
    }
    private void uploadChatAttachmentFileChooser(){
        Intent getContentIntent = FileUtils.createGetContentIntentForImage();
        if(attachmentType.equals("document")) getContentIntent = FileUtils.createGetContentIntentForDocument();
        if(attachmentType.equals("audio")) getContentIntent = FileUtils.createGetContentIntentForAudio();

        Intent intent = Intent.createChooser(getContentIntent, "Select file");
        startActivityForResult(intent, 112);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 111:
                if (resultCode == -1) {
                    final Uri uri = data.getData();
                    final String selectedFilePath = FileUtils.getPath(getApplicationContext(), uri);
                    String groupId = icon_upload_group_id;
                    Ion.with(getApplicationContext())
                            .load("https://api.cloudkibo.com/api/groupmessaging/uploadIcon")
                            //.uploadProgressBar(uploadProgressBar)
                            .setHeader("kibo-token", authtoken)
                            .setMultipartParameter("unique_id", icon_upload_group_id)
                            .setMultipartFile("file", FileUtils.getExtension(selectedFilePath), new File(selectedFilePath))
                            .asJsonObject()
                            .setCallback(new FutureCallback<JsonObject>() {
                                @Override
                                public void onCompleted(Exception e, JsonObject result) {
                                    // do stuff with the result or error
                                    if(e == null) {
                                        try {

                                            String filename = icon_upload_group_id;
                                            FileOutputStream outputStream;
                                            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                                            outputStream.write(com.cloudkibo.webrtc.filesharing.Utility.convertFileToByteArray(new File(selectedFilePath)));
                                            outputStream.close();

                                            IFragmentName myFragment = (IFragmentName) getSupportFragmentManager().findFragmentById(R.id.content_frame);

                                            if(myFragment == null) return;
                                            if(myFragment.getFragmentName().equals("GroupSetting"))
                                            {
                                                final GroupSetting myGroupSettingFragment = (GroupSetting) myFragment;
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        myGroupSettingFragment.loadDisplayImage(); //here you call the method of your current Fragment.
                                                    }
                                                });
                                            }
                                        } catch (Exception e2) {
                                            e2.printStackTrace();
                                        }

                                        Toast.makeText(getApplicationContext(), result.toString(), Toast.LENGTH_LONG).show();
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(), getString(R.string.main_activity_file_upload_error), Toast.LENGTH_LONG).show();
                                        e.printStackTrace();
                                    }
                                }
                            });
                }
                break;
            case 112:
                if (resultCode == -1) {
                    final Uri uri = data.getData();
                    final String selectedFilePath = FileUtils.getPath(getApplicationContext(), uri);
                    String fileType = attachmentType;
                    if(selectedFilePath != null) {
                        if (FileUtils.isExternalStorageWritable()) {
                            try {
                                if (com.cloudkibo.webrtc.filesharing.Utility.isFreeSpaceAvailableForFileSize(
                                        Integer.parseInt(com.cloudkibo.webrtc.filesharing.Utility.getFileMetaData(selectedFilePath)
                                                .getString("size"))
                                )) {
                                    String uniqueid = Long.toHexString(Double.doubleToLongBits(Math.random()));
                                    uniqueid += (new Date().getYear()) + "" + (new Date().getMonth()) + "" + (new Date().getDay());
                                    uniqueid += (new Date().getHours()) + "" + (new Date().getMinutes()) + "" + (new Date().getSeconds());

                                    DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                                    db.createFilesInfo(uniqueid,
                                            com.cloudkibo.webrtc.filesharing.Utility.getFileMetaData(selectedFilePath)
                                                    .getString("name"),
                                            com.cloudkibo.webrtc.filesharing.Utility.getFileMetaData(selectedFilePath)
                                                    .getString("size"),
                                            fileType,
                                            com.cloudkibo.webrtc.filesharing.Utility.getFileMetaData(selectedFilePath)
                                                    .getString("filetype"), selectedFilePath);

                                    IFragmentName myFragment1 = (IFragmentName) getSupportFragmentManager().findFragmentById(R.id.content_frame);
                                    if (myFragment1 == null) return;
                                    if (myFragment1.getFragmentName().equals("GroupChat")) {
                                        final GroupChat myGroupChatListFragment = (GroupChat) myFragment1;
                                        myGroupChatListFragment.sendFileAttachment(uniqueid, fileType);
                                    }

                                } else {
                                    Toast.makeText(getApplicationContext(), getString(R.string.common_not_enough_storage), Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                Toast.makeText(getApplicationContext(), "Unexpected Error occurred.", Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.common_no_storage), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.error_selecting_file), Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case 5123:
                if(resultCode == -1) {
                     final String name = Utility.getContact(getApplicationContext(), data);
                    getActionBar().setTitle(name);

                    IFragmentName myFragment = (IFragmentName) getSupportFragmentManager().findFragmentById(R.id.content_frame);

                    if(myFragment == null) return;
                    if(myFragment.getFragmentName().equals("GroupChat")){
                        final GroupChat myGroupChatListFragment = (GroupChat) myFragment;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                myGroupChatListFragment.setNewContactName(name);

                            }
                        });
                    }

                    ArrayList<String> contactList1Available = new ArrayList<String>();
                    ArrayList<String> contactList1PhoneAvailable = new ArrayList<String>();

                    contactList1Available.add(name);
                    contactList1PhoneAvailable.add(phoneOfContactToAdd);

                    loadFoundContacts(contactList1Available, contactList1PhoneAvailable);
                    phoneOfContactToAdd = "";

                } else if(resultCode == 0){
                    // contact was not added. it was discarded
                    phoneOfContactToAdd = "";
                }
                break;
            case REQUEST_CHOOSER:
                if (resultCode == RESULT_OK) {

                    final Uri uri = data.getData();

                    // Get the File path from the Uri
                    String path = FileUtils.getPath(this, uri);

                    // Alternatively, use FileUtils.getFile(Context, Uri)
                    if (path != null && FileUtils.isLocal(path)) {
                        File file = new File(path);

                        try {

                            fileData = Base64.encode(FileUtils.loadFile(file));

                            initiatorFileTransfer = true;

                            Intent i = new Intent(this, FileConnection.class);
                            i.putExtra("user", user);
                            i.putExtra("room", room);
                            i.putExtra("contact", filePeer);
                            i.putExtra("initiator", initiatorFileTransfer);
                            i.putExtra("filepath", path);

                            startActivity(i);


                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case 0123:
                IFragmentName myFragment = (IFragmentName) getSupportFragmentManager().findFragmentById(R.id.content_frame);
                if(myFragment == null) return;
                if(myFragment.getFragmentName().equals("GroupChatUI")){
                    final GroupChatUI myGroupChatListFragment = (GroupChatUI) myFragment;
                    myGroupChatListFragment.onActivityResult(requestCode,  resultCode, data);
                }
                break;
            case 129:
                IFragmentName myFragment1 = (IFragmentName) getSupportFragmentManager().findFragmentById(R.id.content_frame);
                if(myFragment1 == null) return;
                if(myFragment1.getFragmentName().equals("GroupChat")){
                    final GroupChat myGroupChatListFragment = (GroupChat) myFragment1;
                    myGroupChatListFragment.onActivityResult(requestCode,  resultCode, data);
                }
                break;
            case 141:
                IFragmentName myFragment2 = (IFragmentName) getSupportFragmentManager().findFragmentById(R.id.content_frame);
                if(myFragment2 == null) return;
                if(myFragment2.getFragmentName().equals("GroupChat")){
                    final GroupChat myGroupChatListFragment = (GroupChat) myFragment2;
                    myGroupChatListFragment.onActivityResult(requestCode,  resultCode, data);
                }
                break;
            case 152:
                IFragmentName myFragment3 = (IFragmentName) getSupportFragmentManager().findFragmentById(R.id.content_frame);
                if(myFragment3 == null) return;
                if(myFragment3.getFragmentName().equals("GroupChat")){
                    final GroupChat myGroupChatListFragment = (GroupChat) myFragment3;
                    myGroupChatListFragment.onActivityResult(requestCode,  resultCode, data);
                }
                break;

        }
    }


    /**
     * Setup the container fragment for drawer layout. The current
     * implementation of this method simply calls launchFragment method for tab
     * position 1 as the position 0 is for List header view. You can customize
     * this method as per your need to display specific content.
     */
    private void setupContainer()
    {
        getSupportFragmentManager().addOnBackStackChangedListener(
                new OnBackStackChangedListener() {

                    @Override
                    public void onBackStackChanged()
                    {
                        setActionBarTitle();
                    }
                });
        launchFragment(1);
    }

    /**
     * Set the action bar title text.
     */
    private void setActionBarTitle()
    {
        if(drawerLayout == null) setupDrawer();
        if (drawerLayout.isDrawerOpen(drawerLeft))
        {
            getActionBar().setTitle(R.string.app_name);
            return;
        }
        if (getSupportFragmentManager().getBackStackEntryCount() == 0)
            return;
        String title = getSupportFragmentManager().getBackStackEntryAt(
                getSupportFragmentManager().getBackStackEntryCount() - 1)
                .getName();
        getActionBar().setTitle(title);

    }
	
	/**
	 * Remove these functions and fragments should be able to directly call the service
	 * Need to think on it, as fragments are short-lived, this might not be good idea to
	 * bind fragments to service
	 */
    public void sendSocketMessage(String msg, String phoneOfPeer){
        //socketService.sendSocketMessage(msg, phoneOfPeer);
    }

    public void callThisPerson(String contactPhone, String contactName){
        Log.d("CALL", "Call this person function called");
        socketService.callThisPerson(contactPhone);
        Log.d("CALL", "After call this person function called");
        Intent i = new Intent(this, OutgoingCall.class);
        i.putExtra("user", user);
        i.putExtra("room", room);
        i.putExtra("contact", contactPhone);
        i.putExtra("contact_name", contactName);
        startActivity(i);
    }

    public void sendFileToThisPerson(String contact){

        filePeer = contact;

        Intent getContentIntent = FileUtils.createGetContentIntent();

        Intent intent = Intent.createChooser(getContentIntent, getString(R.string.common_select_file));
        startActivityForResult(intent, REQUEST_CHOOSER);

    }



    public void getUserFromSQLiteDatabase(){
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());


        // Hashmap to load data from the Sqlite database
        user = new HashMap<String, String>();
        user = db.getUserDetails();

        TextView userFirstName = (TextView)findViewById(R.id.textViewUserNameOnNavigationBar);
        userFirstName.setText(user.get("display_name"));

        TextView userEmail = (TextView)findViewById(R.id.textViewUserEmailOnNavigationBar);
        userEmail.setText(user.get("phone"));

    }

    public String getUserName(){
        return user.get("display_name");
    }

    public String getUserPhone(){
        return user.get("phone");
    }

    public String getUserId(){
        return user.get("_id");
    }

    /** (non-Javadoc)
     * @see android.app.Activity#onPostCreate(android.os.Bundle)
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onConfigurationChanged(android.content.res.Configuration)
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggle
        drawerToggle.onConfigurationChanged(newConfig);
    }

    /* (non-Javadoc)
    * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
    */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* (non-Javadoc)
     * @see com.newsfeeder.custom.CustomActivity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (drawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onKeyDown(int, android.view.KeyEvent)
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            if (getSupportFragmentManager().getBackStackEntryCount() > 1)
            {
                getSupportFragmentManager().popBackStackImmediate();
            }
            else
                finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private ServiceConnection socketConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SocketBinder binder = (SocketBinder) service;
            socketService = binder.getService();
            isBound = true;

            binder.setListener(new BoundServiceListener() {

                @Override
                public void receiveSocketMessage(String type, String msg) {

                }

                @Override
                public void receiveSocketArray(String type, final JSONArray body) {

                }

                @Override
                public void receiveSocketJson(String type, final JSONObject body) {

                }

            });
            //socketService.setSocketIOConfig();
        }
    };

    private ServiceConnection kiboSyncConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            KiboSyncService.KiboSyncBinder binder = (KiboSyncService.KiboSyncBinder) service;
            kiboSyncService = binder.getService();
            kiboServiceIsBound = true;

            binder.setListener(new BoundKiboSyncListener() {

                @Override
                public void contactsLoaded() {

                    //kiboServiceIsBound = false;

                    //unbindService(kiboSyncConnection);
                }

                @Override
                public void chatLoaded() {
                    IFragmentName myFragment = (IFragmentName) getSupportFragmentManager().findFragmentById(R.id.content_frame);

                    if(myFragment == null) return;
                    if(myFragment.getFragmentName().equals("ChatList")){
                        final ChatList myChatListFragment = (ChatList) myFragment;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                myChatListFragment.loadChatList();

                            }
                        });
                    } else if(myFragment.getFragmentName().equals("GroupChat")){
                        final GroupChat myGroupChatListFragment = (GroupChat) myFragment;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                myGroupChatListFragment.loadConversationList();

                            }
                        });
                    }

                }

                @Override
                public void sendPendingMessageUsingSocket(String contactPhone, String msg, String uniqueid) {

                }

                @Override
                public void sendMessageStatusUsingSocket(String contactPhone, String status, String uniqueid) {

                }

            });
            kiboSyncService.startIncrementalSyncWithoutAddressBookAccess(authtoken);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            kiboServiceIsBound = false;
        }
    };

    public void handleIncomingStatusForSentMessage(String type, final JSONObject body) {
        IFragmentName myFragment = (IFragmentName) getSupportFragmentManager().findFragmentById(R.id.content_frame);

        if(myFragment == null) return;
        if(myFragment.getFragmentName().equals("GroupChat"))
        {
            final GroupChat myGroupChatFragment = (GroupChat) myFragment;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        myGroupChatFragment.updateStatusSentMessage(body.getString("status"), body.getString("uniqueid"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }

    public void updateChatList() {
        IFragmentName myFragment = (IFragmentName) getSupportFragmentManager().findFragmentById(R.id.content_frame);

        if(myFragment == null) return;
        if(myFragment.getFragmentName().equals("ChatList"))
        {
            final ChatList myChatListFragment = (ChatList) myFragment;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    myChatListFragment.loadChatList();
                }
            });

        }
    }

    public void updateGroupUIChat(final String unique_id) {
        IFragmentName myFragment = (IFragmentName) getSupportFragmentManager().findFragmentById(R.id.content_frame);

        if(myFragment == null) return;
        if(myFragment.getFragmentName().equals("GroupChatUI"))
        {
            final GroupChatUI groupChatUIFragment = (GroupChatUI) myFragment;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    groupChatUIFragment.populateMessages();
                }
            });

            new AsyncTask<String, String, JSONObject>() {
                @Override
                protected JSONObject doInBackground(String... args) {
                    return (new UserFunctions()).updateGroupChatStatusToSeen(unique_id, mainActivity.authtoken);
                }
                @Override
                protected void onPostExecute(JSONObject row) {
                    if(row != null){
                        DatabaseHandler db= new DatabaseHandler(getApplicationContext());
                        db.updateGroupChatStatus(unique_id, "seen");
                    }
                }
            }.execute();
        } else {
            new AsyncTask<String, String, JSONObject>() {
                @Override
                protected JSONObject doInBackground(String... args) {
                    return (new UserFunctions()).updateGroupChatStatusToDelivered(unique_id, mainActivity.authtoken);
                }
                @Override
                protected void onPostExecute(JSONObject row) {
                    if(row != null){
                        DatabaseHandler db= new DatabaseHandler(getApplicationContext());
                        db.updateGroupChatStatus(unique_id, "delivered");
                    }
                }
            }.execute();
        }
    }

    public void updateGroupUIChat() {
        IFragmentName myFragment = (IFragmentName) getSupportFragmentManager().findFragmentById(R.id.content_frame);

        if(myFragment == null) return;
        if(myFragment.getFragmentName().equals("GroupChatUI"))
        {
            final GroupChatUI groupChatUIFragment = (GroupChatUI) myFragment;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    groupChatUIFragment.populateMessages();
                }
            });

        }
    }

    public void updateGroupMembers() {
        IFragmentName myFragment = (IFragmentName) getSupportFragmentManager().findFragmentById(R.id.content_frame);

        if(myFragment == null) return;
        if(myFragment.getFragmentName().equals("GroupSetting"))
        {
            final GroupSetting groupSetting = (GroupSetting) myFragment;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    groupSetting.getMembers();
                }
            });

        }
    }

    public void handleIncomingChatMessage(String type, final JSONObject body){
        try{
            IFragmentName myFragment = (IFragmentName) getSupportFragmentManager().findFragmentById(R.id.content_frame);

            if(myFragment == null) return;
            if(myFragment.getFragmentName().equals("GroupChat"))
            {
                if(myFragment.getFragmentContactPhone().equals(body.getString("from"))){
                    final GroupChat myGroupChatFragment = (GroupChat) myFragment;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                myGroupChatFragment.receiveMessage(body.getString("msg"), body.getString("uniqueid"), body.getString("from"), body.getString("date_server_received"), body.getString("type"),
                                        body.has("file_type") ? body.getString("file_type") : "");
                                Utility.sendLogToServer(""+ body.getString("to") +" is now going to show the message on the UI in chat window");
                            } catch(JSONException e){
                                e.printStackTrace();
                            }

                        }
                    });
                } else {
                    Intent intent = new Intent(getApplicationContext(), SplashScreen.class);
                    PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                    String message = body.getString("msg");
                    String subMsg = (message.length() > 15) ? message.substring(0, 15) : message;

                    DatabaseHandler db = new DatabaseHandler(getApplicationContext());

                    String senderName = "";

                    JSONArray contactInAddressBook = db.getSpecificContact(body.getString("from"));
                    if(contactInAddressBook.length() > 0) {
                        senderName = contactInAddressBook.getJSONObject(0).getString("display_name");
                    } else {
                        senderName = body.getString("from");
                    }

                    (new GroupUtility(getApplicationContext())).sendNotification(senderName, subMsg);

                    Utility.sendLogToServer(""+ body.getString("to") +" is going to show notification and chime for message because user is on other chat screen in app");

                    try {
                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                        r.play();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


            } else {
                Intent intent = new Intent(getApplicationContext(), SplashScreen.class);
                PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                String message = body.getString("msg");
                String subMsg = (message.length() > 15) ? message.substring(0, 15) : message;

                DatabaseHandler db = new DatabaseHandler(getApplicationContext());

                String senderName = "";

                JSONArray contactInAddressBook = db.getSpecificContact(body.getString("from"));
                if(contactInAddressBook.length() > 0) {
                    senderName = contactInAddressBook.getJSONObject(0).getString("display_name");
                } else {
                    senderName = body.getString("from");
                }

                (new GroupUtility(getApplicationContext())).sendNotification(senderName, subMsg);

                Utility.sendLogToServer(""+ body.getString("to") +" is going to show notification and chime for message because user is on conversations list screen in app");

                try {
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    r.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(myFragment.getFragmentName().equals("ChatList")){
                    final ChatList myChatListFragment = (ChatList) myFragment;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            myChatListFragment.loadChatList();

                        }
                    });
                }

            }
        }catch (JSONException e){
            e.printStackTrace();
        }

        //socketService.updateReceivedMessageStatusToServer();
    }

    private void loadContactsFromAddressBook(){
        final ArrayList<String> contactList1 = new ArrayList<String>();
        final ArrayList<String> contactList1Phone = new ArrayList<String>();

        new AsyncTask<String, String, JSONObject>() {
            private ProgressDialog nDialog;

            @Override
            protected void onPreExecute() {
                ToastNotify2("Syncing contacts now.");
            }

            @Override
            protected JSONObject doInBackground(String... args) {

                List<NameValuePair> phones = new ArrayList<NameValuePair>();
                List<NameValuePair> emails = new ArrayList<NameValuePair>();

                ContentResolver cr = getApplicationContext().getContentResolver();
                Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                        null, null, null, null);
                if (cur.getCount() > 0) {
                    while (cur.moveToNext()) {
                        String id = cur.getString(
                                cur.getColumnIndex(ContactsContract.Contacts._ID));
                        String name = cur.getString(
                                cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        String image_uri = cur.getString(
                                cur.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));
                        //Log.w("Contact Name : ", "Name " + name + "");
                        if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                            Cursor pCur = cr.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                                    new String[]{id}, null);
                            while (pCur.moveToNext()) {
                                DatabaseHandler db = new DatabaseHandler(
                                        getApplicationContext());
                                String phone = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                if(phone.length() < 6) continue;
                                if(phone.charAt(0) != '+') {
                                    if(phone.charAt(0) == '0') phone = phone.substring(1, phone.length());
                                    if(phone.charAt(0) == '1') phone = "+" + phone;
                                    else phone = "+" + db.getUserDetails().get("country_prefix") + phone;
                                }
                                phone = phone.replaceAll("\\s+","");
                                phone = phone.replaceAll("\\p{P}","");
                                db = new DatabaseHandler(getApplicationContext());
                                String userPhone = db.getUserDetails().get("phone");
                                if(userPhone.equals(phone)) continue;
                                if(phone.equals("+923323800399") || phone.equals("+14255035617")) {
                                    Utility.sendLogToServer("CONTACT LOADING.. GOT NUMBER "+ phone);
                                }
                                if(contactList1Phone.contains(phone)) continue;
                                //if(Character.isLetter(name.charAt(0)))
                                //    name = name.substring(0, 1).toUpperCase() + name.substring(1);
                                if(phone.equals("+923323800399") || phone.equals("+14255035617")) {
                                    Utility.sendLogToServer("CONTACT LOADING.. THIS NUMBER WENT INTO LIST "+ phone);
                                }
                                phones.add(new BasicNameValuePair("phonenumbers", phone));
                                Log.w("Phone Number: ", "Name : " + name + " Number : " + phone);
                                contactList1.add(name);
                                contactList1Phone.add(phone);
                                photo_uri.put(phone,image_uri);
                            }
                            pCur.close();
                        }
                    }
                }
                cur.close();

                UserFunctions userFunction = new UserFunctions();
                JSONObject json = userFunction.sendAddressBookPhoneContactsToServer(phones, authtoken);
                Log.w("SERVER SENT RESPONSE", json.toString());
                ToastNotify2("Synced contacts with KiboChat");
                return json;
            }

            @Override
            protected void onPostExecute(JSONObject json) {

                try {

                    ArrayList<String> contactList1Available = new ArrayList<String>();
                    ArrayList<String> contactList1PhoneAvailable = new ArrayList<String>();

                    if(json != null){

                        JSONArray jArray = json.getJSONArray("available");

                        for(int i = 0; i<jArray.length(); i++){
                            contactList1Available.add(contactList1.get(contactList1Phone.indexOf(jArray.get(i).toString())));
                            contactList1PhoneAvailable.add(contactList1Phone.get(contactList1Phone.indexOf(jArray.get(i).toString())));
                            contactList1.remove(contactList1Phone.indexOf(jArray.get(i).toString()));
                            contactList1Phone.remove(contactList1Phone.indexOf(jArray.get(i).toString()));
                            Log.w("REMOVING", jArray.get(i).toString());
                        }

                    }
                    loadNotFoundContacts(contactList1, contactList1Phone);
                    loadFoundContacts(contactList1Available, contactList1PhoneAvailable);

                    ToastNotify2("Contacts synced successfully.");

                    IFragmentName myFragment = (IFragmentName) getSupportFragmentManager().findFragmentById(R.id.content_frame);

                    if(myFragment == null) return;
                    if(myFragment.getFragmentName().equals("ContactList"))
                    {
                        final ContactList myContactListFragment = (ContactList) myFragment;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                myContactListFragment.loadContactList(); //here you call the method of your current Fragment.
                            }
                        });
                    }
                    if(myFragment.getFragmentName().equals("ContactListPending"))
                    {
                        final ContactListPending myContactListFragment = (ContactListPending) myFragment;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                myContactListFragment.loadContactList(); //here you call the method of your current Fragment.
                            }
                        });
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

        }.execute();

    }

    private void loadNotFoundContacts(ArrayList<String> contactList1, ArrayList<String> contactList1Phone) {

        DatabaseHandler db = new DatabaseHandler(
                getApplicationContext());

        db.resetContactsTable();

        db = new DatabaseHandler(
                getApplicationContext());

        for (int i = 0; i < contactList1.size(); i++) {
            db.addContact("false", "null",
                    contactList1Phone.get(i),
                    contactList1.get(i),
                    "null",
                    "No",
                    "N/A",
                    photo_uri.get(contactList1Phone.get(i)));
        }

        try {
            JSONArray contacts = db.getContacts();
            contacts.toString();
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    private void loadFoundContacts(ArrayList<String> contactList1, ArrayList<String> contactList1Phone) {

        DatabaseHandler db = new DatabaseHandler(
                getApplicationContext());

        for (int i = 0; i < contactList1.size(); i++) {
            db.addContact("true", "null",
                    contactList1Phone.get(i),
                    contactList1.get(i),
                    "null",
                    "Yes",
                    "I am on CloudKibo",
                    photo_uri.get(contactList1Phone.get(i)));
            // todo work for status here
        }

        //kiboSyncService.startSyncWithoutAddressBookAccess(authtoken);

        try {
            JSONArray contacts = db.getContacts();
            contacts.toString();
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i("MainActivity", "This device is not supported by Google Play Services.");
                ToastNotify2("This device is not supported by Google Play Services.");
                finish();
            }
            return false;
        }
        return true;
    }

    public void registerWithNotificationHubs()
    {
        Log.i("MainActivity", " Registering with Notification Hubs");

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }


    public void ToastNotify2(final String notificationMessage){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, notificationMessage, Toast.LENGTH_LONG).show();
            }
        });


    }


}
