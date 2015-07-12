package com.cloudkibo.ui;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudkibo.MainActivity;
//import com.cloudkibo.R;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.model.ContactItem;
import com.cloudkibo.utils.IFragmentName;

/**
 * The Class ContactList is the Fragment class that is launched when the user
 * clicks on Notes option in Left navigation drawer. It simply display a dummy list of notes.
 * You need to write actual implementation for loading and displaying notes
 */
public class AddRequest extends CustomFragment implements IFragmentName
{

	/** The Note list. */
	private ArrayList<ContactItem> contactList;
	//private AccountManager mAccountManager;
	private String authtoken;
	private ContactAdapter contactAdapter;
	
	UserFunctions userFunction;

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		//mAccountManager = AccountManager.get(getActivity());
		
		View v = inflater.inflate(R.layout.pendinglist, null);
		
		userFunction = new UserFunctions();
		
		authtoken = getActivity().getIntent().getExtras().getString("authtoken");
		
		loadPendingContactList();
		
		ListView list = (ListView) v.findViewById(R.id.list);
		contactAdapter = new ContactAdapter();
		list.setAdapter(contactAdapter);
		
		registerForContextMenu(list);
/*		
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3)
			{
				Log.d("SOJHARO", contactList.get(pos).getUserName());
				
				final String tempContactId = contactList.get(pos).getUserId();
				
				//Intent chatIntent = new Intent(getActivity().getApplicationContext(), ChatList.class);
				//chatIntent.putExtra("contactUserNameToChat", contactList.get(pos).getUserName());
				//startActivity(chatIntent);
				
				//contactList.get(pos).setUnReadMessage(false);
				//contactAdapter.notifyDataSetChanged();
				
				
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
							
							new AsyncTask<String, String, JSONObject>() {

								@Override
								protected JSONObject doInBackground(String... args) {
									JSONObject result = null;
									
									MainActivity act1 = (MainActivity)getActivity();
									
									result = userFunction.markChatAsRead(act1.getUserId(), tempContactId, authtoken);
									
									
									return result;
									
								}

								@Override
								protected void onPostExecute(JSONObject json) {
										
								}
					            
					        }.execute();
							
						} else {
							//Toast.makeText(getActivity().getApplicationContext(),
							//		"Could not connect to Internet", Toast.LENGTH_SHORT)
							//		.show();
						}
					}
		            
		        }.execute();
				
				
				
				Bundle bundle = new Bundle();
		        bundle.putString("contactusername", contactList.get(pos).getUserName());
		        bundle.putString("contactid", contactList.get(pos).getUserId());
		        bundle.putString("authtoken", authtoken);
		        
		        GroupChat groupChatFragment = new GroupChat();
		        groupChatFragment.setArguments(bundle);
				
				getFragmentManager().beginTransaction()
				.replace(R.id.content_frame, groupChatFragment, "groupChatFragmentTag")
				.addToBackStack(contactList.get(pos).firstName() +" "+ contactList.get(pos).lastName()).commit();

			}
		});
*/
		setTouchNClick(v.findViewById(R.id.tab1));
		setTouchNClick(v.findViewById(R.id.tab2));
		
		return v;
	}
	
	@Override 
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
    {
	      super.onCreateContextMenu(menu, v, menuInfo);
	      menu.setHeaderTitle("Select The Action");  
	      menu.add(0, v.getId(), 0, "Accept");  
	      menu.add(0, v.getId(), 0, "Reject"); 

    } 
	
	 @Override  
     public boolean onContextItemSelected(MenuItem item)
     {  

             final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
    
             if(item.getTitle()=="Accept")
             {
            	 
            	 final List<NameValuePair> params = new ArrayList<NameValuePair>();
            	 params.add(new BasicNameValuePair("username", contactList.get(info.position).getUserName()));
            	 params.add(new BasicNameValuePair("_id", contactList.get(info.position).getUserId()));
            	 
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
 							
 							new AsyncTask<String, String, JSONObject>() {

 								@Override
 								protected JSONObject doInBackground(String... args) {
 									JSONObject result = null;
 									
 									
 									result = userFunction.acceptFriendRequest(params, authtoken);
 									
 									
 									return result;
 									
 								}

 								@Override
 								protected void onPostExecute(JSONObject json) {

 									try{
	 									if(json.getString("status").equals("success")){
	 										
	 										try{
	 											contactList.remove(info.position);
	 											contactAdapter.notifyDataSetChanged();
	 											
	 										}catch(NullPointerException e){}
	 										
	 									}
	 									else{
	 										Toast.makeText(getActivity().getApplicationContext(),
	 			 									"Some error occured. Try again.", Toast.LENGTH_SHORT)
	 			 									.show();
	 									}
 									}
 									catch(JSONException e){
 										
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
            	 
            	 
               // Code to execute when clicked on This Item
             }  
             else if(item.getTitle()=="Reject")
             {  
            	 final List<NameValuePair> params = new ArrayList<NameValuePair>();
            	 params.add(new BasicNameValuePair("username", contactList.get(info.position).getUserName()));
            	 params.add(new BasicNameValuePair("_id", contactList.get(info.position).getUserId()));
            	 
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
 							
 							new AsyncTask<String, String, JSONObject>() {

 								@Override
 								protected JSONObject doInBackground(String... args) {
 									JSONObject result = null;
 									
 									
 									result = userFunction.rejectFriendRequest(params, authtoken);
 									
 									
 									return result;
 									
 								}

 								@Override
 								protected void onPostExecute(JSONObject json) {

 									try{
	 									if(json.getString("status").equals("success")){
	 										
	 										try{
	 											contactList.remove(info.position);
	 											contactAdapter.notifyDataSetChanged();
	 											
	 										}catch(NullPointerException e){}
	 										
	 									}
	 									else{
	 										Toast.makeText(getActivity().getApplicationContext(),
	 			 									"Some error occured. Try again.", Toast.LENGTH_SHORT)
	 			 									.show();
	 									}
 									}
 									catch(JSONException e){
 										
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
             else 
             {
                 return false;
             }  
             return true;  
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
	}
	
	
	/**
	 * This method currently loads a dummy list of Notes. You can write the
	 * actual implementation of loading Notes.
	 */
	private void loadPendingContactList()
	{
		
		ArrayList<ContactItem> noteList = new ArrayList<ContactItem>();
		contactList = new ArrayList<ContactItem>(noteList);

        new AsyncTask<String, String, JSONArray>() {

            @Override
            protected JSONArray doInBackground(String... args) {
                UserFunctions userFunction = new UserFunctions();
                JSONArray json = userFunction.getPendingContactsList(authtoken);
                return json;
            }

            @Override
            protected void onPostExecute(JSONArray jsonA) {
                try {

                    if (jsonA != null) {

                        //String res = jsonA.get(0).toString();

                        ArrayList<ContactItem> contactList1 = new ArrayList<ContactItem>();

                        for (int i=0; i < jsonA.length(); i++) {
                            JSONObject row = jsonA.getJSONObject(i);
                            try{
                            	contactList1.add(new ContactItem(row.getJSONObject("userid").getString("_id"),
                                        row.getJSONObject("userid").getString("username"),
                                        row.getJSONObject("userid").getString("firstname"),
                                        row.getJSONObject("userid").getString("lastname"),
                                        row.getJSONObject("userid").getString("phone"), 01,
                                        false, "",
                                        row.getJSONObject("userid").getString("status"),
                                        row.getString("detailsshared"),
                                        row.getBoolean("unreadMessage")
                                ));
                            }catch(JSONException e){
                            	contactList1.add(new ContactItem(row.getJSONObject("userid").getString("_id"),
                                        row.getJSONObject("userid").getString("username"),
                                        row.getJSONObject("userid").getString("firstname"),
                                        row.getJSONObject("userid").getString("lastname"),
                                        "nill", 01,
                                        false, "",
                                        row.getJSONObject("userid").getString("status"),
                                        row.getString("detailsshared"),
                                        row.getBoolean("unreadMessage")
                                ));
                            }
                            
                        }

                        loadNewContacts(contactList1);
                        

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }.execute();


    }
	
	public void loadNewContacts(ArrayList<ContactItem> contactList1){
		try{
			contactList.clear();
			contactList.addAll(contactList1);
			contactAdapter.notifyDataSetChanged();
			
		}catch(NullPointerException e){}
	}
	
	/**
	 * The Class ContactAdapter is the adapter class for Note ListView. The
	 * currently implementation of this adapter simply display static dummy
	 * contents. You need to write the code for displaying actual contents.
	 */
	private class ContactAdapter extends BaseAdapter
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

			ImageView img2 = (ImageView) v.findViewById(R.id.online);
			img2.setVisibility(c.isOnline() ? View.VISIBLE : View.INVISIBLE);
			
			ImageView img3 = (ImageView) v.findViewById(R.id.messageicon);
			img3.setVisibility(c.hasUnreadMessage() ? View.VISIBLE : View.INVISIBLE);

			return v;
		}

	}
	
	 public String getFragmentName()
     {
       return "PendingList";
     }
	

}
