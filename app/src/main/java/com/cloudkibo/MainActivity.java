package com.cloudkibo;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.cloudkibo.ui.CallHistory;
import com.cloudkibo.ui.ChatList;
import com.cloudkibo.ui.ContactList;
import com.cloudkibo.ui.ContactListPending;
import com.cloudkibo.ui.CreateGroup;
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


    /* (non-Javadoc)
     * @see com.newsfeeder.custom.CustomActivity#onCreate(android.os.Bundle)
     */
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

        setupContainer();
        setupDrawer();


        userFunction = new UserFunctions();

        if(userFunction.isUserLoggedIn(getApplicationContext()));
            getUserFromSQLiteDatabase();

        //am = AccountManager.get(MainActivity.this);
        //account = am.getAccountsByType(AccountGeneral.ACCOUNT_TYPE)[0];

        //if(!ContentResolver.isSyncActive(account, CloudKiboDatabaseContract.AUTHORITY)) {

        	/* todo this starts syncing on very short intervals */

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
                ToastNotify2("Can't refresh contacts without permission.");
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
            //unbindService(socketConnection);
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
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
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
        al.add(new Data("Chat", null, R.drawable.ic_chat));
        al.add(new Data("Contacts", null, R.drawable.ic_notes));
        al.add(new Data("Calls", null, android.R.drawable.sym_action_call));
        al.add(new Data("Invite", null, R.drawable.ic_notes));
        al.add(new Data("Create Group", null, R.drawable.ic_about));
        //al.add(new Data("Add Requests", null, R.drawable.ic_projects));
        //al.add(new Data("Conference", null, R.drawable.group1));
        //al.add(new Data("Settings", null, R.drawable.ic_setting));
        al.add(new Data("About CloudKibo", null, R.drawable.ic_about));
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
            title = "Chat";
            f = new ChatList();
        }
        else if(pos == 2){
            title = "Contacts";
            f = new ContactList();
        }
        else if(pos == 3){
            title = "Calls History";
            f = new CallHistory();
        }
        else if(pos == 4){
            title = "Address Book";
            f = new ContactListPending();
        }
        else if(pos == 5){
            title = "Create Group";
            f = new CreateGroup();
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
            title = "About CloudKibo";
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

    String icon_upload_group_id = "";
    public void uploadIcon(String group_id){
        icon_upload_group_id = group_id;
        Intent getContentIntent = FileUtils.createGetContentIntent();

        Intent intent = Intent.createChooser(getContentIntent, "Select an image");
        startActivityForResult(intent, 111);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 111:
                if (resultCode == -1) {
                    final Uri uri = data.getData();
                    String selectedFilePath = FileUtils.getPath(getApplicationContext(), uri);

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
                                    if(e!= null)
                                        Toast.makeText(getApplicationContext(), result.toString(), Toast.LENGTH_LONG).show();
                                    else e.printStackTrace();
                                }
                            });
                    /*new AsyncTask<String, String, JSONObject>() {

                        @Override
                        protected JSONObject doInBackground(String... args) {
                            UserFunctions userFunctions = new UserFunctions();
                            // todo sojharo file upload function
                            String member_phone = "";
                            return  userFunctions.removeMember(group_id, member_phone, authtoken);
                        }

                        @Override
                        protected void onPostExecute(JSONObject row) {
                            if(row != null){
                                Toast.makeText(getActivity().getApplicationContext(), row.toString(), Toast.LENGTH_LONG).show();
//                    Toast.makeText(getContext(), "Group Successfully Created On Server", Toast.LENGTH_LONG).show();
                            }
                        }

                    }.execute();*/

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

        Intent intent = Intent.createChooser(getContentIntent, "Select a file");
        startActivityForResult(intent, REQUEST_CHOOSER);

    }

    public void updateChatStatus(String status, String uniqueid){
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        db.updateChat(status, uniqueid);
    }

    public void resetSpecificChatHistorySync(String uniqueid){
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        db.resetSpecificChatHistorySync(uniqueid);
    }

    public void addChatHistorySync(String uniqueid, String from){
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        db.addChatSyncHistory("seen", uniqueid, from);
    }

    public void sendMessage(String contactPhone, String msg, String uniqueid){
        socketService.sendMessage(contactPhone, msg, uniqueid);
    }

    public void sendPendingMessage(String contactPhone, String msg, String uniqueid){
        //socketService.sendPendingMessage(contactPhone, msg, uniqueid);
    }

    public void sendMessageStatusUsingSocket(String status, String uniqueid, String sender){
        if(socketService.isSocketConnected()) {
            socketService.updateReceivedMessageStatusToServer(status, uniqueid, sender);
        }
    }

    public Boolean isSocketConnected() {
        return socketService.isSocketConnected();
    }


    public void askFriendsOnlineStatus(){
        socketService.askFriendsOnlineStatus();
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

    public void startMessageInfo(String message, String status, String date){
        Intent i = new Intent(this, MessageInfo.class);
        i.putExtra("message", message);
        i.putExtra("status", status);
        i.putExtra("date", date);
        startActivity(i);

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

    private void fetchUserFromServerForFirstTime() {
        new AsyncTask<String, String, Boolean>() {

            @Override
            protected Boolean doInBackground(String... args) {

                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isConnected()) {
                    try {
                        URL url = new URL("http://www.google.com");
                        HttpURLConnection urlc = (HttpURLConnection) url
                                .openConnection();
                        urlc.setConnectTimeout(3000);
                        urlc.connect();
                        if (urlc.getResponseCode() == 200) {
                            return true;
                        }
                    } catch (MalformedURLException e1) {
                        e1.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return false;

            }

            @Override
            protected void onPostExecute(Boolean th) {

                if (th == true) {

                    new AsyncTask<String, String, JSONObject>() {

                        @Override
                        protected JSONObject doInBackground(String... args) {
                            UserFunctions userFunction = new UserFunctions();
                            JSONObject json = userFunction.getUserData(authtoken);
                            return json;
                        }

                        @Override
                        protected void onPostExecute(JSONObject json) {
                            try {

                                if(json != null){

                                    DatabaseHandler db = new DatabaseHandler(
                                            getApplicationContext());

                                    // Clear all previous data in SQlite database.

                                    UserFunctions logout = new UserFunctions();
                                    logout.logoutUser(getApplicationContext());

                                    db.addUser(json.getString("firstname"),
                                            json.getString("lastname"),
                                            json.getString("email"),
                                            json.getString("username"),
                                            json.getString("_id"),
                                            json.getString("date"));

                                    final TextView userFirstName = (TextView)findViewById(R.id.textViewUserNameOnNavigationBar);
                                    userFirstName.setText(db.getUserDetails().get("firstname")+" "+db.getUserDetails().get("lastname"));

                                    final TextView userEmail = (TextView)findViewById(R.id.textViewUserEmailOnNavigationBar);
                                    userEmail.setText(db.getUserDetails().get("email"));



                                    // Hashmap to load data from the Sqlite database
                                    user = new HashMap<String, String>();
                                    user = db.getUserDetails();

                                    startSocketService();

                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }.execute();

                } else {
                    Toast.makeText(getApplicationContext(),
                            "Could not connect to Internet", Toast.LENGTH_SHORT)
                            .show();
                }
            }

        }.execute();
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




                    if(type.equals("theseareonline")){
                        IFragmentName myFragment = (IFragmentName) getSupportFragmentManager().findFragmentById(R.id.content_frame);

                        if(myFragment == null) return;
                        if(myFragment.getFragmentName().equals("ContactList"))
                        {
                            final ContactList myContactListFragment = (ContactList) myFragment;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    myContactListFragment.setOnlineStatus(body); //here you call the method of your current Fragment.
                                }
                            });
                        }

                    }

                }

                @Override
                public void receiveSocketJson(String type, final JSONObject body) {

                    if(type.equals("group:you_are_added")){
                        GroupUtility groupUtility = new GroupUtility(getApplicationContext());
                        try {
                            groupUtility.updateGroupToLocalDatabase(body.getString("groupId"), body.getString("group_name"), authtoken);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    if(type.equals("group:added_to_group")){
                        GroupUtility groupUtility = new GroupUtility(getApplicationContext());
                        groupUtility.updateGroupMembers(body.toString(), authtoken);
                    }

                    if(type.equals("group:member_left_group")){
                        GroupUtility groupUtility = new GroupUtility(getApplicationContext());
                        groupUtility.memberLeftGroup(body.toString());
                    }

                    if(type.equals("group:chat_received")){
                        GroupUtility groupUtility = new GroupUtility(getApplicationContext());
                        groupUtility.updateGroupChat(body.toString(), authtoken);
                    }

                    if(type.equals("group:removed_from_group")){
                        GroupUtility groupUtility = new GroupUtility(getApplicationContext());
                        groupUtility.removedFromGroup(body.toString(), authtoken);
                    }

                    if(type.equals("im")){

                        handleIncomingChatMessage(type, body);

                    }
                    else if(type.equals("updateSentMessageStatus")){

                        handleIncomingStatusForSentMessage(type, body);

                    }
                    else if(type.equals("offline")){
                        IFragmentName myFragment = (IFragmentName) getSupportFragmentManager().findFragmentById(R.id.content_frame);

                        if(myFragment == null) return;
                        if(myFragment.getFragmentName().equals("ContactList"))
                        {
                            final ContactList myContactListFragment = (ContactList) myFragment;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    myContactListFragment.setOfflineStatusIndividual(body); //here you call the method of your current Fragment.
                                }
                            });
                        }
                    }
                    else if(type.equals("online")){
                        IFragmentName myFragment = (IFragmentName) getSupportFragmentManager().findFragmentById(R.id.content_frame);

                        if(myFragment == null) return;
                        if(myFragment.getFragmentName().equals("ContactList"))
                        {
                            final ContactList myContactListFragment = (ContactList) myFragment;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    myContactListFragment.setOnlineStatusIndividual(body); //here you call the method of your current Fragment.
                                }
                            });
                        }
                    } else if(type.equals("joinedroom")){
                        if(shouldSync){
                            startSyncService();
                        }
                    }

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
                    if(socketService.isSocketConnected()) {
                        sendPendingMessage(contactPhone, msg, uniqueid);
                    }
                }

                @Override
                public void sendMessageStatusUsingSocket(String contactPhone, String status, String uniqueid) {
                    if(socketService.isSocketConnected()) {
                        socketService.updateReceivedMessageStatusToServer(status, uniqueid, contactPhone);
                    }
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
                                GroupUtility groupUtility = new GroupUtility(getApplicationContext());
                                groupUtility.sendNotification("Single message", body.getString("msg"));
                                myGroupChatFragment.receiveMessage(body.getString("msg"), body.getString("uniqueid"), body.getString("from"), body.getString("date"));
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

                    Notification n = new Notification.Builder(getApplicationContext())
                            .setContentTitle(senderName)
                            .setContentText(subMsg)
                            .setSmallIcon(R.drawable.icon)
                            .setContentIntent(pIntent)
                            .setAutoCancel(true)
                            .build();

                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                    notificationManager.notify(0, n);

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

                Notification n = new Notification.Builder(getApplicationContext())
                        .setContentTitle(senderName)
                        .setContentText(subMsg)
                        .setSmallIcon(R.drawable.icon)
                        .setContentIntent(pIntent)
                        .setAutoCancel(true)
                        .build();

                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                notificationManager.notify(0, n);

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
                                if(contactList1Phone.contains(phone)) continue;
                                //if(Character.isLetter(name.charAt(0)))
                                //    name = name.substring(0, 1).toUpperCase() + name.substring(1);
                                phone = phone.replaceAll("\\s+","");
                                phone = phone.replaceAll("\\p{P}","");
                                db = new DatabaseHandler(getApplicationContext());
                                String userPhone = db.getUserDetails().get("phone");
                                if(userPhone.equals(phone)) continue;
                                phones.add(new BasicNameValuePair("phonenumbers", phone));
                                Log.w("Phone Number: ", "Name : " + name + " Number : " + phone);
                                contactList1.add(name);
                                contactList1Phone.add(phone);
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
                    "N/A");
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
                    "I am on CloudKibo");
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

    public void ToastNotify(final String notificationMessage) {
        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(MainActivity.this, notificationMessage, Toast.LENGTH_LONG).show();
                //startSocketService();
            }
        });*/
        //startSocketService();
        reconnectSocket();
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
