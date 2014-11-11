package com.chatt;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.chatt.R;
import com.chatt.custom.CustomActivity;
import com.chatt.model.Data;
import com.chatt.ui.AboutChat;
import com.chatt.ui.ChatList;
import com.chatt.ui.LeftNavAdapter;
import com.chatt.ui.NoteList;
import com.chatt.ui.ProjectList;
import com.cloudkibo.library.DatabaseHandler;
import com.cloudkibo.library.Login;
import com.cloudkibo.library.UserFunctions;
import com.koushikdutta.async.http.socketio.Acknowledge;
import com.koushikdutta.async.http.socketio.ConnectCallback;
import com.koushikdutta.async.http.socketio.EventCallback;
import com.koushikdutta.async.http.socketio.SocketIOClient;

/**
 * The Class MainActivity is the base activity class of the application. This
 * activity is launched after the Splash and it holds all the Fragments used in
 * the app. It also creates the Navigation Drawer on left side.
 */
public class MainActivity extends CustomActivity
{

	/** The drawer layout. */
	private DrawerLayout drawerLayout;

	/** ListView for left side drawer. */
	private ListView drawerLeft;

	/** The drawer toggle. */
	private ActionBarDrawerToggle drawerToggle;
	
	/** Store Authentication Token **/
	String authtoken;
	
	/** Socket.IO Connection Object **/
	private SocketIOClient client;
	
	private MessageHandler messageHandler = new MessageHandler();
	
	private String room = "globalchatroom";

	/* (non-Javadoc)
	 * @see com.newsfeeder.custom.CustomActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		authtoken = getIntent().getExtras().getString("authtoken");

		setupContainer();
		setupDrawer();
		setSocketIOConfig();
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
		
		DatabaseHandler db = new DatabaseHandler(
				getApplicationContext());
		
		final TextView userFirstName = (TextView)findViewById(R.id.textViewUserNameOnNavigationBar);
		userFirstName.setText(db.getUserDetails().get("firstname")+" "+db.getUserDetails().get("lastname"));
		
		final TextView userEmail = (TextView)findViewById(R.id.textViewUserEmailOnNavigationBar);
		userEmail.setText(db.getUserDetails().get("email"));
		
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
					
					/**
					 * Getting the data from the Internet Now.
					 */
					
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
									/**
									 * Clear all previous data in SQlite database.
									 **/
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
		al.add(new Data("Projects", null, R.drawable.ic_projects));
		al.add(new Data("Settings", null, R.drawable.ic_setting));
		al.add(new Data("About CloudKibo", null, R.drawable.ic_about));
		al.add(new Data("Logout", null, R.drawable.ic_logout));
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
		else if (pos == 2)
		{
			title = "Contacts";
			f = new NoteList();
		}
		else if (pos == 3)
		{
			title = "Projects";
			f = new ProjectList();
		}
		else if (pos == 5)
		{
			title = "About CloudKibo";
			f = new AboutChat();
		}
		else if (pos == 6)
		{
			startActivity(new Intent(this, Login.class));
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
	 * Set the Configuration of Socket.io Connection
	 */
	
	public void setSocketIOConfig(){

		SocketIOClient.connect("https://www.cloudkibo.com", new ConnectCallback() {

			@Override
			public void onConnectCompleted(Exception ex, SocketIOClient socket) {

				if (ex != null) {
					Log.e("SOCKET.IO","WebRtcClient connect failed: "+ex.getMessage());
					return;
				}


				Log.d("SOCKET.IO","WebRtcClient connected.");

				client = socket;

				JSONObject message = new JSONObject();

				try {
					
					DatabaseHandler db = new DatabaseHandler(
							getApplicationContext());


					message.put("user", db.getUserDetails().get("username"));
					message.put("room", room);

					socket.emit("join global chatroom", new JSONArray().put(message));

				} catch (JSONException e) {
					e.printStackTrace();
				}
			// specify which events you are interested in receiving

				client.addListener("id", messageHandler);
				client.addListener("message", messageHandler);
				client.addListener("youareonline", messageHandler);
				client.addListener("im", messageHandler);

			}
		}, new Handler());

	}
	
	/**
	 * Socket.IO Message Handler
	 */
	
	private class MessageHandler implements EventCallback {


		@Override
		public void onEvent(String s, JSONArray jsonArray,
				Acknowledge acknowledge) {
			Log.d("SOCKET.IO", "ID = "+ s);
			Log.d("SOCKET.IO", "ID = "+ jsonArray.toString());
			
			/*try{
			
				if(s.equals("youareonline")){
					if(user.get("username").equals("sojharo")){
						
						JSONObject message = new JSONObject();
	
						message.put("from", user.get("username"));
						message.put("to", "sabachanna");
						message.put("from_id", user.get("_id"));
						message.put("to_id", "545af4e27345b48d20845ff0");
						message.put("fromFullName", user.get("firstname")+" "+ user.get("lastname"));
						message.put("msg", "Hello");
	
						client.emit("im", new JSONArray().put(message));
	
	
						
					}
				}
				if(s.equals("im")){
					
					Toast.makeText(getBaseContext(), jsonArray.getJSONObject(0).getString("msg"), Toast.LENGTH_SHORT).show();
					
					if(user.get("username").equals("sojharo")){
						
						JSONObject message = new JSONObject();
	
						message.put("from", user.get("username"));
						message.put("to", "sabachanna");
						message.put("from_id", user.get("_id"));
						message.put("to_id", "545af4e27345b48d20845ff0");
						message.put("fromFullName", user.get("firstname")+" "+ user.get("lastname"));
						message.put("msg", "Hello! How are you?");
						
						JSONObject stanza = new JSONObject();
						
						stanza.put("stanza", message);
	
						client.emit("im", new JSONArray().put(stanza));
	
						
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}*/
		}
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
}