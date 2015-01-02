package com.chatt.ui;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.chatt.MainActivity;
import com.chatt.NewChat;
import com.chatt.R;
import com.chatt.SplashScreen;
import com.chatt.custom.CustomFragment;
import com.chatt.model.ContactItem;
import com.cloudkibo.library.AccountGeneral;
import com.cloudkibo.library.DatabaseHandler;
import com.cloudkibo.library.UserFunctions;

/**
 * The Class NoteList is the Fragment class that is launched when the user
 * clicks on Notes option in Left navigation drawer. It simply display a dummy list of notes.
 * You need to write actual implementation for loading and displaying notes
 */
public class NoteList extends CustomFragment
{

	/** The Note list. */
	private ArrayList<ContactItem> contactList;
	//private AccountManager mAccountManager;
	private String authtoken;
	private NoteAdapter contactAdapter;

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		//mAccountManager = AccountManager.get(getActivity());
		
		View v = inflater.inflate(R.layout.note, null);
		
		authtoken = getActivity().getIntent().getExtras().getString("authtoken");
		
		loadContactList();
		
		ListView list = (ListView) v.findViewById(R.id.list);
		contactAdapter = new NoteAdapter();
		list.setAdapter(contactAdapter);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3)
			{
				Log.d("SOJHARO", contactList.get(pos).getUserName());
				
				//Intent home = new Intent(getActivity().getApplicationContext(), NewChat.class);
				//startActivity(home);

			}
		});

		setTouchNClick(v.findViewById(R.id.tab1));
		setTouchNClick(v.findViewById(R.id.tab2));
		setTouchNClick(v.findViewById(R.id.btnNewChat));
		return v;
	}

	/* (non-Javadoc)
	 * @see com.socialshare.custom.CustomFragment#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v)
	{
		super.onClick(v);
		if (v.getId() == R.id.tab1)
		{
			getView().findViewById(R.id.tab2).setEnabled(true);
			v.setEnabled(false);
		}
		else if (v.getId() == R.id.tab2)
		{
			getView().findViewById(R.id.tab1).setEnabled(true);
			v.setEnabled(false);
		}
		else if (v.getId() == R.id.btnNewChat)
			startActivity(new Intent(getActivity(), NewChat.class));
	}

	/**
	 * This method currently loads a dummy list of Notes. You can write the
	 * actual implementation of loading Notes.
	 */
	private void loadContactList()
	{
		
		ArrayList<ContactItem> noteList = new ArrayList<ContactItem>();
		this.contactList = new ArrayList<ContactItem>(noteList);
		loadContactsFromDatabase();
		
		 
		 new AsyncTask<String, String, Boolean>() {

			@Override
			protected Boolean doInBackground(String... args) {

				ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
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
										
					new AsyncTask<String, String, JSONArray>() {

						@Override
						protected JSONArray doInBackground(String... args) {
							UserFunctions userFunction = new UserFunctions();
							JSONArray json = userFunction.getContactsList(authtoken);
							return json;
						}

						@Override
						protected void onPostExecute(JSONArray jsonA) {
							try {

								if (jsonA != null) {

									//String res = jsonA.get(0).toString();

									DatabaseHandler db = new DatabaseHandler(
											getActivity().getApplicationContext());

									ArrayList<ContactItem> contactList1 = new ArrayList<ContactItem>();
									
									for (int i=0; i < jsonA.length(); i++) {
										JSONObject row = jsonA.getJSONObject(i);
										contactList1.add(new ContactItem(row.getJSONObject("contactid").getString("_id"),
												row.getJSONObject("contactid").getString("username"),
												row.getJSONObject("contactid").getString("firstname"),
												row.getJSONObject("contactid").getString("lastname"),
												row.getJSONObject("contactid").getString("phone"), 01, 
												false, "",
												row.getJSONObject("contactid").getString("status"),
												row.getString("detailsshared")
												));
									}
									
									loadNewContacts(contactList1);
									insertContactsIntoDB(contactList1);
									
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
			            
			        }.execute();
					
				} else {
					Toast.makeText(getActivity().getApplicationContext(),
							"Could not connect to Internet", Toast.LENGTH_SHORT)
							.show();
				}
			}
            
        }.execute();
		 	
		 
	}
	
	public void loadNewContacts(ArrayList<ContactItem> contactList1){
		contactList.clear();
		contactList.addAll(contactList1);
		contactAdapter.notifyDataSetChanged();
	}
	
	public void insertContactsIntoDB(ArrayList<ContactItem> contactList1){
		DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
		
		db.resetContactsTable();
		
		for(int i=0; i<contactList1.size(); i++){
			db.addContact(contactList1.get(i).firstName(),
					contactList1.get(i).lastName(), contactList1.get(i).getPhone(),
					contactList1.get(i).getUserName(), contactList1.get(i).getUserId(),
					contactList1.get(i).details_shared(), contactList1.get(i).status());
		}
	}
	
	public void loadContactsFromDatabase(){
		DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
		
		try {
			
			JSONArray jsonA = db.getContacts();
			
			ArrayList<ContactItem> contactList1 = new ArrayList<ContactItem>();
			
			for (int i=0; i < jsonA.length(); i++) {
				JSONObject row = jsonA.getJSONObject(i);
				contactList1.add(new ContactItem(row.getString("_id"),
						row.getString("username"),
						row.getString("firstname"),
						row.getString("lastname"),
						row.getString("phone"), 01, 
						false, "",
						row.getString("status"),
						row.getString("detailsshared")
						));
			}
			
			contactList.addAll(contactList1);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * The Class CutsomAdapter is the adapter class for Note ListView. The
	 * currently implementation of this adapter simply display static dummy
	 * contents. You need to write the code for displaying actual contents.
	 */
	private class NoteAdapter extends BaseAdapter
	{

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount()
		{
			return contactList.size();
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public ContactItem getItem(int arg0)
		{
			return contactList.get(arg0);
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int arg0)
		{
			return arg0;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int pos, View v, ViewGroup arg2)
		{
			if (v == null)
				v = LayoutInflater.from(getActivity()).inflate(
						R.layout.contact_item, null);

			ContactItem c = getItem(pos);
			TextView lbl = (TextView) v.findViewById(R.id.lblContactDisplayName);
			lbl.setText(c.firstName() +" "+ c.lastName());

			lbl = (TextView) v.findViewById(R.id.lblContactPhone);
			lbl.setText(c.getPhone());

			lbl = (TextView) v.findViewById(R.id.lblContactStatus);
			lbl.setText(c.status());

			ImageView img = (ImageView) v.findViewById(R.id.imgContactListItem);
			//img.setImageResource(c.getIcon());

			img = (ImageView) v.findViewById(R.id.online);
			img.setVisibility(c.isOnline() ? View.VISIBLE : View.INVISIBLE);

			return v;
		}

	}
	
	/**
	 * Get an auth token for the account.
	 * If not exist - add it and then return its auth token.
	 * If one exist - return its auth token.
	 * If more than one exists - show a picker and return the select account's auth token.
	 * @param accountType
	 * @param authTokenType
	 */
/*	private void getTokenForAccountCreateIfNeeded(String accountType, String authTokenType) {
		final AccountManagerFuture<Bundle> future = mAccountManager.getAuthTokenByFeatures(accountType, authTokenType, null, getActivity(), null, null,
				new AccountManagerCallback<Bundle>() {
			@Override
			public void run(AccountManagerFuture<Bundle> future) {
				Bundle bnd = null;
				try {
					bnd = future.getResult();
					String authtoken1 = bnd.getString(AccountManager.KEY_AUTHTOKEN);
					//Toast.makeText(getBaseContext(), ((authtoken != null) ? "SUCCESS!\ntoken: " + authtoken : "FAIL"), Toast.LENGTH_SHORT).show();                            
					Log.d("SOJHARO", "GetTokenForAccount Bundle is " + bnd);
					
					authtoken = authtoken1;


				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(getActivity().getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		}
		, null);
	}*/
}
