package com.cloudkibo;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Dialog;
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
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.cloudkibo.R;

import com.cloudkibo.custom.CustomActivity;
import com.cloudkibo.database.CloudKiboDatabaseContract;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.file.filechooser.utils.FileUtils;
import com.cloudkibo.library.AccountGeneral;
import com.cloudkibo.library.Login;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.model.Data;
//import io.cordova.hellocordova.CordovaApp;

import com.cloudkibo.socket.BoundServiceListener;
import com.cloudkibo.socket.SocketService;
import com.cloudkibo.socket.SocketService.SocketBinder;
import com.cloudkibo.ui.AboutChat;
import com.cloudkibo.ui.AddRequest;
import com.cloudkibo.ui.ChatList;
import com.cloudkibo.ui.ContactList;
import com.cloudkibo.ui.GroupChat;
import com.cloudkibo.ui.LeftNavAdapter;
import com.cloudkibo.ui.ProjectList;
import com.cloudkibo.utils.IFragmentName;
import com.cloudkibo.webrtc.filesharing.FileConnection;
import com.cloudkibo.file.filechooser.utils.Base64;



/**
 * The Class MainActivity is the base activity class of the application. This
 * activity is launched after the Splash and it holds all the Fragments used in
 * the app. It also creates the Navigation Drawer on left side.
 */
public class MainActivity extends CustomActivity
{

    SocketService socketService;
    boolean isBound = false;

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

    Dialog dialog;;

    public String filePeer;
    public String fileData;
    public Boolean initiatorFileTransfer;

    HashMap<String, String> user;

    List<NameValuePair> msg1;

    AccountManager am;
    Account account;

    public static final long SECONDS_PER_MINUTE = 60L;
    public static final long SYNC_INTERVAL_IN_MINUTES = 1440L;
    public static final long SYNC_INTERVAL =
            SYNC_INTERVAL_IN_MINUTES *
                    SECONDS_PER_MINUTE;

    private static final int REQUEST_CHOOSER = 1105;

    /* (non-Javadoc)
     * @see com.newsfeeder.custom.CustomActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authtoken = getIntent().getExtras().getString("authtoken");

        userFunction = new UserFunctions();

        if(userFunction.isUserLoggedIn(getApplicationContext()));
        getUserFromSQLiteDatabase();

        am = AccountManager.get(MainActivity.this);
        account = am.getAccountsByType(AccountGeneral.ACCOUNT_TYPE)[0];

        if(!ContentResolver.isSyncActive(account, CloudKiboDatabaseContract.AUTHORITY)) {
        	
        	/* todo this starts syncing on very short intervals */

            ContentResolver.setSyncAutomatically(account, CloudKiboDatabaseContract.AUTHORITY, true);
            ContentResolver.requestSync(account, CloudKiboDatabaseContract.AUTHORITY, new Bundle());

            ContentResolver.addPeriodicSync(account, CloudKiboDatabaseContract.AUTHORITY, Bundle.EMPTY, SYNC_INTERVAL);

            fetchUserFromServerForFirstTime();
        }
        else {
            startSocketService();
        }
        setupContainer();
        setupDrawer();
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

    @Override
    protected void onDestroy() {

        if(isBound){
            unbindService(socketConnection);
        }

        super.onDestroy();
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
                getActionBar().setTitle("Chat");
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
                                    long arg3)
            {
                drawerLayout.closeDrawers();
                launchFragment(pos);
            }
        });

        drawerLayout.openDrawer(drawerLeft);

        if(userFunction.isUserLoggedIn(getApplicationContext())){

            final TextView userFirstName = (TextView)findViewById(R.id.textViewUserNameOnNavigationBar);
            userFirstName.setText(user.get("firstname")+" "+user.get("lastname"));

            final TextView userEmail = (TextView)findViewById(R.id.textViewUserEmailOnNavigationBar);
            userEmail.setText(user.get("email"));

        }

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
        //al.add(new Data("Chat", null, R.drawable.ic_chat));
        al.add(new Data("Contacts", null, R.drawable.ic_notes));
        al.add(new Data("Add Requests", null, R.drawable.ic_projects));
        //al.add(new Data("Settings", null, R.drawable.ic_setting));
        al.add(new Data("About CloudKibo", null, R.drawable.ic_about));
        al.add(new Data("Logout", null, R.drawable.ic_logout));
        //al.add(new Data("WebRTC", null, R.drawable.group1)); // this is for testing purpose
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
            title = "Contacts";
            f = new ContactList();
        }
        else if(pos == 2){
        	title = "Add Requests";
        	f = new AddRequest();
        }
        else if (pos == 3)
        {
            title = "About CloudKibo";
            f = new AboutChat();
        }
        else if (pos == 4)
        {
            startActivity(new Intent(this, Login.class));

            DatabaseHandler db = new DatabaseHandler(getApplicationContext());

            db.resetChatsTable();
            db.resetContactsTable();
            db.resetTables();

            am.removeAccount(account, null, null);

            finish();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
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
	
	/*
	 * Remove these functions and fragments should be able to directly call the service
	 * Need to think on it, as fragments are short-lived, this might not be good idea to
	 * bind fragments to service
	 */

    public void sendSocketMessage(String msg, String peer){
        socketService.sendSocketMessage(msg, peer);
    }

    public void callThisPerson(String contact){
        socketService.callThisPerson(contact);
    }

    public void sendFileToThisPerson(String contact){

        filePeer = contact;

        Intent getContentIntent = FileUtils.createGetContentIntent();

        Intent intent = Intent.createChooser(getContentIntent, "Select a file");
        startActivityForResult(intent, REQUEST_CHOOSER);

    }

    public void sendMessage(String contactUserName, String contactId, String msg){
        socketService.sendMessage(contactUserName, contactId, msg);
    }


    public void askFriendsOnlineStatus(){
        socketService.askFriendsOnlineStatus();
    }

    public void getUserFromSQLiteDatabase(){
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());


        // Hashmap to load data from the Sqlite database
        user = new HashMap<String, String>();
        user = db.getUserDetails();

    }

    public String getUserName(){
        return user.get("username");
    }

    public String getUserId(){
        return user.get("_id");
    }

    /* (non-Javadoc)
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

                	final String message = msg;
                    if(type.equals("im")){

                        IFragmentName myFragment = (IFragmentName) getSupportFragmentManager().findFragmentById(R.id.content_frame);

                        if(myFragment.getFragmentName().equals("GroupChat"))
                        {
                            final GroupChat myGroupChatFragment = (GroupChat) myFragment;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                	myGroupChatFragment.receiveMessage(message);

                               }
                           });
                            
                        }

                    }
                    else if(type.equals("Missed")){
                        dialog.dismiss();
                    }
                    else if(type.equals("Reject Call")){
                        dialog.dismiss();
                    }
                    else if(type.equals("got user media")){

                        Toast.makeText(getApplicationContext(),
                                "Disabled due to phonertc vs socket service bug #5", Toast.LENGTH_SHORT)
                                .show();
/*
	  					Intent i = new Intent(getApplicationContext(), CordovaApp.class);
	  					i.putExtra("username", user.get("username"));
	  					i.putExtra("_id", user.get("_id"));
	  					i.putExtra("peer", msg);
	  					i.putExtra("lastmessage", "GotUserMedia");
	  					i.putExtra("room", room);
	  		            startActivity(i);
	  		            */
                    }
                    else if(type.equals("Accept Call")){
                        dialog.dismiss();

                        Toast.makeText(getApplicationContext(),
                                "Disabled due to phonertc vs socket service bug #5", Toast.LENGTH_SHORT)
                                .show();
                        /*
						Intent i = new Intent(getApplicationContext(), CordovaApp.class);
	  					i.putExtra("username", user.get("username"));
	  					i.putExtra("_id", user.get("_id"));
	  					i.putExtra("peer", msg);
	  					i.putExtra("lastmessage", "AcceptCallFromOther");
	  					i.putExtra("room", room);
	  		            startActivity(i);*/
                    }
                    else if(type.equals("calleeisoffline") || type.equals("calleeisbusy")){
                        dialog.dismiss();
                    }
                    else if(type.equals("othersideringing")){
                        dialog = new Dialog(MainActivity.this);
                        dialog.setContentView(R.layout.call_dialog);
                        dialog.setTitle(msg);

                        // set the custom dialog components - text, image and button
                        TextView text = (TextView) dialog.findViewById(R.id.textDialog);
                        text.setText(msg);
                        ImageView image = (ImageView) dialog.findViewById(R.id.imageDialog);
                        image.setImageResource(R.drawable.ic_launcher);

                        Button dialogButton = (Button) dialog.findViewById(R.id.declineButton);
                        // if button is clicked, close the custom dialog
                        dialogButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                socketService.stopCallMessageToCallee();

                                dialog.dismiss();
                            }
                        });

                        dialog.show();
                    }
                    else if(type.equals("areyoufreeforcall")){
                        dialog = new Dialog(MainActivity.this);
                        dialog.setContentView(R.layout.call_dialog2);
                        dialog.setTitle(msg);

                        // set the custom dialog components - text, image and button
                        TextView text = (TextView) dialog.findViewById(R.id.textDialog);
                        text.setText(msg);
                        ImageView image = (ImageView) dialog.findViewById(R.id.imageDialog);
                        image.setImageResource(R.drawable.ic_launcher);

                        Button dialogButton = (Button) dialog.findViewById(R.id.declineButton);
                        // if button is clicked, close the custom dialog
                        dialogButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                socketService.acceptCallMessageToCallee();

                                dialog.dismiss();
                            }
                        });

                        Button acceptButton = (Button) dialog.findViewById(R.id.acceptButton);
                        // if button is clicked, close the custom dialog
                        acceptButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                socketService.rejectCallMessageToCallee();

                                dialog.dismiss();
                            }
                        });

                        dialog.show();
                    }

                }

                @Override
                public void receiveSocketArray(String type, JSONArray body) {

                    if(type.equals("theseareonline")){

                        IFragmentName myFragment = (IFragmentName) getSupportFragmentManager().findFragmentById(R.id.content_frame);

                        if(myFragment.getFragmentName().equals("ContactList"))
                        {
                            ContactList myContactListFragment = (ContactList) myFragment;
                            myContactListFragment.setOnlineStatus(body); //here you call the method of your current Fragment.
                        }

                    }
                    else if(type.equals("offline")){
                        IFragmentName myFragment = (IFragmentName) getSupportFragmentManager().findFragmentById(R.id.content_frame);

                        if(myFragment.getFragmentName().equals("ContactList"))
                        {
                            ContactList myContactListFragment = (ContactList) myFragment;
                            myContactListFragment.setOfflineStatusIndividual(body); //here you call the method of your current Fragment.
                        }
                    }
                    else if(type.equals("online")){
                        IFragmentName myFragment = (IFragmentName) getSupportFragmentManager().findFragmentById(R.id.content_frame);

                        if(myFragment.getFragmentName().equals("ContactList"))
                        {
                            ContactList myContactListFragment = (ContactList) myFragment;
                            myContactListFragment.setOfflineStatusIndividual(body); //here you call the method of your current Fragment.
                        }
                    }

                }

            });
        }
    };


}