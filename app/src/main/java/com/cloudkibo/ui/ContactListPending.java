package com.cloudkibo.ui;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
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
import android.content.ContentResolver;

/**
 * The Class ContactList is the Fragment class that is launched when the user
 * clicks on Notes option in Left navigation drawer. It simply display a dummy list of notes.
 * You need to write actual implementation for loading and displaying notes
 */
public class ContactListPending extends CustomFragment implements IFragmentName
{

    /** The Note list. */
    private ArrayList<ContactItem> contactList;
    //private AccountManager mAccountManager;
    private String authtoken;
    private ContactAdapter contactAdapter;

    UserFunctions userFunction;

    private final int MSG_SENT = 124;

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        //mAccountManager = AccountManager.get(getActivity());

        View v = inflater.inflate(R.layout.note, null);

        userFunction = new UserFunctions();

        authtoken = getActivity().getIntent().getExtras().getString("authtoken");

        loadContactList();

        ListView list = (ListView) v.findViewById(R.id.list);
        contactAdapter = new ContactAdapter();
        list.setAdapter(contactAdapter);

        registerForContextMenu(list);

        list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
                                    long arg3) {
                Log.d("SOJHARO", contactList.get(pos).getUserName());

                final String tempContactId = contactList.get(pos).getUserId();

                contactList.get(pos).setUnReadMessage(false);
                contactAdapter.notifyDataSetChanged();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) // At least KitKat
                {
                    String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(getActivity().getApplicationContext()); // Need to change the build to API 19

                    Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
                    sendIntent.setType("text/plain");
                    sendIntent.setData(Uri.parse("smsto:" + contactList.get(pos).getPhone()));
                    //sendIntent.putExtra(Intent.EXTRA_TEXT, "Join me on CloudKibo for video chat. Download from https://www.cloudkibo.com");
                    sendIntent.putExtra("sms_body", "Join me on CloudKibo for video chat. Download from https://www.cloudkibo.com");

                    if (defaultSmsPackageName != null)// Can be null in case that there is no default, then the user would be able to choose
                    // any app that support this intent.
                    {
                        sendIntent.setPackage(defaultSmsPackageName);
                    }
                    startActivity(sendIntent);

                }
                else // For early versions.
                {
                    Intent smsIntent = new Intent(android.content.Intent.ACTION_VIEW);
                    smsIntent.setType("vnd.android-dir/mms-sms");
                    smsIntent.putExtra("address",contactList.get(pos).getPhone());
                    smsIntent.putExtra("sms_body","Join me on CloudKibo for video chat. Download from https://www.cloudkibo.com");
                    startActivity(smsIntent);
                }


                // gives lots of options: use it to let user decide who to send invitation
                /*Intent msg = new Intent(Intent.ACTION_SEND);
                msg.putExtra(Intent.EXTRA_TEXT, "Join me on CloudKibo for video chat. Download from https://www.cloudkibo.com");
                msg.setType("text/plain");
                startActivityForResult(Intent.createChooser(msg, "Call someone :"), MSG_SENT);*/

            }
        });

        return v;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Select The Action");
        menu.add(0, v.getId(), 0, "Call");
        menu.add(0, v.getId(), 0, "Transfer File");
        //menu.add(0, v.getId(), 0, "Remove Conversation");
        //menu.add(0, v.getId(), 0, "Remove Contact");

    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {

        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        if(item.getTitle()=="Call")
        {

            Log.d("CALL", "Call button pressed");

            MainActivity act1 = (MainActivity)getActivity();

            act1.callThisPerson(contactList.get(info.position).getPhone(),
                    contactList.get(info.position).getUserName());
         		/* 
         		// custom dialog
      			final Dialog dialog = new Dialog(getActivity().getApplicationContext());
      			dialog.setContentView(R.layout.call_dialog);
      			dialog.setTitle("Calling");
       
      			// set the custom dialog components - text, image and button
      			TextView text = (TextView) dialog.findViewById(R.id.textDialog);
      			text.setText("Making a call to "+ contactList.get(info.position).getUserName());
      			ImageView image = (ImageView) dialog.findViewById(R.id.imageDialog);
      			image.setImageResource(R.drawable.ic_launcher);
       
      			Button dialogButton = (Button) dialog.findViewById(R.id.declineButton);
      			// if button is clicked, close the custom dialog
      			dialogButton.setOnClickListener(new OnClickListener() {
      				@Override
      				public void onClick(View v) {
      					dialog.dismiss();
      				}
      			});
       
      			dialog.show();*/


            // Code to execute when clicked on This Item
        }
        else if(item.getTitle()=="Transfer File")
        {
            MainActivity act1 = (MainActivity)getActivity();
            act1.sendFileToThisPerson(contactList.get(info.position).getUserName());
        }
        else if(item.getTitle() == "Remove Conversation"){
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

                                result = userFunction.removeChat(act1.getUserId(), contactList.get(info.position).getUserName(), authtoken);

                                return result;

                            }

                            @Override
                            protected void onPostExecute(JSONObject json) {

                                try {

                                    if(json != null){

                                        if(json.getString("status").equals("success")){

                                            DatabaseHandler db = new DatabaseHandler(
                                                    getActivity().getApplicationContext());

                                            MainActivity act1 = (MainActivity)getActivity();

                                            db.resetSpecificChat(act1.getUserName(), contactList.get(info.position).getUserName());

                                        }


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
        else if (item.getTitle() == "Remove Contact"){
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

                                result = userFunction.removeContact(act1.getUserId(), contactList.get(info.position).getUserName(), authtoken);

                                return result;

                            }

                            @Override
                            protected void onPostExecute(JSONObject json) {

                                try {

                                    if(json != null){

                                        if(json.getString("status").equals("success")){

                                            DatabaseHandler db = new DatabaseHandler(
                                                    getActivity().getApplicationContext());

                                            MainActivity act1 = (MainActivity)getActivity();

                                            db.resetSpecificContact(act1.getUserName(), contactList.get(info.position).getUserName());

                                            db.resetSpecificChat(act1.getUserName(), contactList.get(info.position).getUserName());

                                            loadContactList();

                                        }


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
		/*if (v.getId() == R.id.btnNewChat){

			LayoutInflater layoutInflater = LayoutInflater.from(getActivity().getApplicationContext());

			View promptView = layoutInflater.inflate(R.layout.prompt, null);

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
			
			final EditText input = (EditText) promptView.findViewById(R.id.userInput);
			
			// set prompts.xml to be the layout file of the alertdialog builder
			alertDialogBuilder.setView(promptView);

			// setup a dialog window
			alertDialogBuilder
					.setCancelable(false)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									// get user input and send it to server
									Log.d("SOJHARO", "VALUE = "+ input.getText());

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
														
														result = userFunction.saveContact(input.getText().toString(), authtoken);
																												
														return result;
														
													}

													@Override
													protected void onPostExecute(JSONObject json) {
														
														try{	
															
															if(json.getString("status").equals("success")){
																
																ArrayList<ContactItem> contactList1 = new ArrayList<ContactItem>();
																
																if(json.isNull("msg")){
																	Toast.makeText(getActivity().getApplicationContext(),
																			"User not found on CloudKibo", Toast.LENGTH_SHORT)
																			.show();
																}
																else{
																	JSONArray jsonA = json.getJSONArray("msg");
																	
																	for (int i=0; i < jsonA.length(); i++) {
																		JSONObject row = jsonA.getJSONObject(i);
																		
																		try{
																			contactList1.add(new ContactItem(row.getJSONObject("contactid").getString("_id"),
																					row.getJSONObject("contactid").getString("username"),
																					row.getJSONObject("contactid").getString("firstname"),
																					row.getJSONObject("contactid").getString("lastname"),
																					row.getJSONObject("contactid").getString("phone"), 01, 
																					false, "",
																					row.getJSONObject("contactid").getString("status"),
																					row.getString("detailsshared"),
																					row.getBoolean("unreadMessage")
																					));
																		}catch(JSONException e){
																			contactList1.add(new ContactItem(row.getJSONObject("contactid").getString("_id"),
																					row.getJSONObject("contactid").getString("username"),
																					row.getJSONObject("contactid").getString("firstname"),
																					row.getJSONObject("contactid").getString("lastname"),
																					"nill", 01, 
																					false, "",
																					row.getJSONObject("contactid").getString("status"),
																					row.getString("detailsshared"),
																					row.getBoolean("unreadMessage")
																					));
																		}
																		
																		
																	}
																	
																	loadNewContacts(contactList1);
																	insertContactsIntoDB(contactList1);
																}
																
																
															
															}
															else{
																Toast.makeText(getActivity().getApplicationContext(),
																		json.getString("msg"), Toast.LENGTH_SHORT)
																		.show();
															}
														
															
															
														} catch (JSONException e) {
															// TODO Auto-generated catch block
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




		}*/
    }


    private void loadContactList()
    {

        ArrayList<ContactItem> noteList = new ArrayList<ContactItem>();
        contactList = new ArrayList<ContactItem>(noteList);

        DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());

        try {

            JSONArray jsonA = db.getContactsOnAddressBook();

            jsonA = UserFunctions.sortJSONArray(jsonA, "display_name");

            ArrayList<ContactItem> contactList1 = new ArrayList<ContactItem>();

            for (int i=0; i < jsonA.length(); i++) {
                JSONObject row = jsonA.getJSONObject(i);

                contactList1.add(new ContactItem(row.getString("_id"),
                        row.getString("display_name"),
                        "", // first name
                        row.getString("on_cloudkibo"),
                        row.getString("phone"),
                        01,
                        false, "",
                        row.getString("status"),
                        row.getString("detailsshared"),
                        false
                ));
            }

            loadNewContacts(contactList1);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void loadNewContacts(ArrayList<ContactItem> contactList1){
        try{
            contactList.clear();
            contactList.addAll(contactList1);
            contactAdapter.notifyDataSetChanged();

            MainActivity act1 = (MainActivity)getActivity();
            //act1.askFriendsOnlineStatus();
        }catch(NullPointerException e){}
    }

    public void setOnlineStatus(JSONArray contacts){

        for(int i=0; i<contactList.size(); i++)
            contactList.get(i).setOnline(false);

        try{
            for(int j=0; j<contacts.length(); j++)
                for(int i=0; i<contactList.size(); i++){
                    if(contactList.get(i).getUserName().equals(contacts.getJSONArray(0).getJSONObject(j).getString("username"))){
                        contactList.get(i).setOnline(true);
                        break;
                    }
                }

            contactAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {

        }
    }

    public void setOfflineStatusIndividual(JSONArray individual){
        for(int i=0; i<contactList.size(); i++){
            try {
                if(contactList.get(i).getUserName().equals(individual.getJSONObject(0).getString("username"))){
                    contactList.get(i).setOnline(false);
                    break;
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        contactAdapter.notifyDataSetChanged();
    }

    public void setOnlineStatusIndividual(JSONArray individual){
        for(int i=0; i<contactList.size(); i++){
            try {
                if(contactList.get(i).getUserName().equals(individual.getJSONObject(0).getString("username"))){
                    contactList.get(i).setOnline(true);
                    break;
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        contactAdapter.notifyDataSetChanged();
    }

    public void insertContactsIntoDB(ArrayList<ContactItem> contactList1){
        try{
            DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());

            db.resetContactsTable();

            for(int i=0; i<contactList1.size(); i++){
                db.addContact(contactList1.get(i).firstName(),
                        contactList1.get(i).lastName(), contactList1.get(i).getPhone(),
                        contactList1.get(i).getUserName(), contactList1.get(i).getUserId(),
                        contactList1.get(i).details_shared(), contactList1.get(i).status());
            }
        }catch(NullPointerException e){
            e.printStackTrace();
        }
    }

    public void sendInvite(View v, ContactItem c){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) // At least KitKat
        {
            String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(getActivity().getApplicationContext()); // Need to change the build to API 19

            Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
            sendIntent.setType("text/plain");
            sendIntent.setData(Uri.parse("smsto:" +  c.getPhone()));
            //sendIntent.putExtra(Intent.EXTRA_TEXT, "Join me on CloudKibo for video chat. Download from https://www.cloudkibo.com");
            sendIntent.putExtra("sms_body", "Join me on KiboChat for video chat. Download from https://goo.gl/CBlIzM");

            if (defaultSmsPackageName != null)// Can be null in case that there is no default, then the user would be able to choose
            // any app that support this intent.
            {
                sendIntent.setPackage(defaultSmsPackageName);
            }
            startActivity(sendIntent);

        }
        else // For early versions.
        {
            Intent smsIntent = new Intent(android.content.Intent.ACTION_VIEW);
            smsIntent.setType("vnd.android-dir/mms-sms");
            smsIntent.putExtra("address", c.getPhone());
            smsIntent.putExtra("sms_body","Join me on KiboChat for video chat. Download from https://goo.gl/CBlIzM");
            startActivity(smsIntent);
        }
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
                v = LayoutInflater.from(getActivity()).inflate(
                        R.layout.contact_item, null);

            final ContactItem c = getItem(pos);
            TextView lbl = (TextView) v.findViewById(R.id.lblContactDisplayName);
            lbl.setText(c.getUserName());

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

            TextView invite_button = (TextView) v.findViewById(R.id.invite_button);

            invite_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendInvite(view, c);
                }
            });

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendInvite(view, c);
                }
            });

            return v;
        }

    }

    public String getFragmentName()
    {
        return "ContactListPending";
    }

    public String getFragmentContactPhone () { return ""; }

}
