package com.cloudkibo.ui;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Telephony;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cloudkibo.MainActivity;
//import com.cloudkibo.R;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.library.CircleTransform;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.library.Utility;
import com.cloudkibo.model.ChatItem;
import com.cloudkibo.model.ContactItem;
import com.cloudkibo.utils.IFragmentName;

/**
 * The Class ContactList is the Fragment class that is launched when the user
 * clicks on Notes option in Left navigation drawer. It simply display a dummy list of notes.
 * You need to write actual implementation for loading and displaying notes
 */
public class ContactList extends CustomFragment implements IFragmentName
{

	/** The Note list. */
	public static ArrayList<ContactItem> contactList;
	public ArrayList<ContactItem> backupList = new ArrayList<ContactItem>();
	//private AccountManager mAccountManager;
	private String authtoken;
	private ContactAdapter contactAdapter;
	//private ArrayList<String> contact_phone = new ArrayList<String>();
	UserFunctions userFunction;
	ContactList  reference = this;
    EditText editsearch;
    LinearLayout search_view;
	public static int totalCount = 0;

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		//mAccountManager = AccountManager.get(getActivity());

		final View v = inflater.inflate(R.layout.note, null);
		setHasOptionsMenu(true);

		userFunction = new UserFunctions();

		authtoken = getActivity().getIntent().getExtras().getString("authtoken");
		if(contactList == null){
			contactList = new ArrayList<ContactItem>();
		}


		ListView list = (ListView) v.findViewById(R.id.list);
		contactAdapter = new ContactAdapter();
		loadContactList();
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

				Bundle bundle = new Bundle();
				bundle.putString("contactusername", contactList.get(pos).getUserName());
				bundle.putString("contactphone", contactList.get(pos).getPhone());
				bundle.putString("contactid", contactList.get(pos).getUserId());
				bundle.putString("authtoken", authtoken);

				GroupChat groupChatFragment = new GroupChat();
				groupChatFragment.setArguments(bundle);

				getFragmentManager().beginTransaction()
						.replace(R.id.content_frame, groupChatFragment, "groupChatFragmentTag")
						.addToBackStack(contactList.get(pos).getUserName()).commit();

			}
		});


//		Utility utility = new Utility();
//		utility.updateDatabaseWithContactImages(getContext(),contact_phone);

        editsearch = (EditText) v.findViewById(R.id.contact_search);

        editsearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                String text = editsearch.getText().toString().toLowerCase(Locale.getDefault());
                contactAdapter.filter(text);

            }
        });

        search_view = (LinearLayout) v.findViewById(R.id.search_view);
        search_view.setVisibility(View.GONE);
        ImageView close_search = (ImageView) v.findViewById(R.id.close_search);
        close_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_view = (LinearLayout) v.findViewById(R.id.search_view);
                search_view.setVisibility(View.GONE);
				contactAdapter.filter("");
            }
        });

		return v;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (menu != null) {
			menu.findItem(R.id.archived).setVisible(false);
			menu.findItem(R.id.language).setVisible(false);
			menu.findItem(R.id.backup_setting).setVisible(false);
			menu.findItem(R.id.search_chats).setVisible(false);
		}
		inflater.inflate(R.menu.contacts, menu);  // Use filter.xml from step 1
		getActivity().getActionBar().setSubtitle(null);
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setDisplayShowCustomEnabled(true);

		LayoutInflater inflator = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflator.inflate(R.layout.custom_imageview, null);
		ImageView search_button = (ImageView) v.findViewById(R.id.imageView4);
        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_view.setVisibility(View.VISIBLE);
            }
        });
		actionBar.setCustomView(v);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == R.id.refreshContacts){
			MainActivity act1 = (MainActivity) getActivity();

			act1.syncContacts();
			return true;
		}
		if(id == R.id.addContact){
			MainActivity act1 = (MainActivity) getActivity();

			act1.createContact();
			return true;
		}
        if(id == R.id.search_contacts){
            search_view.setVisibility(View.VISIBLE);
            return true;
        }

		return super.onOptionsItemSelected(item);
	}

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle(getString(R.string.common_select_action));
		menu.add(0, v.getId(), 0, getString(R.string.common_call));
		menu.add(0, v.getId(), 0, getString(R.string.common_transfer_file));
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{

		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

		if(item.getTitle()==getString(R.string.common_call))
		{

			Log.d("CALL", "Call button pressed");

			MainActivity act1 = (MainActivity)getActivity();

			act1.callThisPerson(contactList.get(info.position).getPhone(),
					contactList.get(info.position).getUserName());

		}
		else if(item.getTitle()==getString(R.string.common_transfer_file))
		{
			MainActivity act1 = (MainActivity)getActivity();
			act1.sendFileToThisPerson(contactList.get(info.position).getUserName());
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
	}

	public void loadContactList()
	{

		//ArrayList<ContactItem> noteList = new ArrayList<ContactItem>();
		//contactList = new ArrayList<ContactItem>(noteList);
		final DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
		//contact_phone.clear();


		new AsyncTask<String, String, ArrayList<ContactItem>>() {

			@Override
			protected ArrayList<ContactItem> doInBackground(String... args) {

				try {
					JSONArray jsonA = db.getContactsWithImages();
					ArrayList<ContactItem> contactList1 = new ArrayList<ContactItem>();

					Utility.sendLogToServer("In Contact List page: the size of contacts on cloudkibo "+ jsonA.length());

					jsonA = UserFunctions.sortJSONArrayIgnoreCase(jsonA, "display_name");
//
					String my_btmp;
					//This loop adds contacts to the display list which are on cloudkibo

					for (int i=0; i < jsonA.length(); i++) {
						JSONObject row = jsonA.getJSONObject(i);
						my_btmp = row.optString("image_uri");

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
						).setProfile(my_btmp));
					//	contact_phone.add(row.getString("phone"));
					}

					return contactList1;
				} catch (JSONException e) {
					e.printStackTrace();
				}

				return null;
			}

			@Override
			protected void onPostExecute(ArrayList<ContactItem> contactList1) {
				if(contactList1 != null) {
					//loadNewContacts(contactList1);
					contactList.clear();
					contactList.addAll(contactList1);
                    totalCount = 0;
                    totalCount = contactList.size();
					contactAdapter.notifyDataSetChanged();
				}
			}

		}.execute();

	}




	public String getContactsDetails(String address, Context context) {

		DatabaseHandler db  = new DatabaseHandler(context);
		return  db.getContactImage(address);

	}


	/**
	 * The Class ContactAdapter is the adapter class for Note ListView. The
	 * currently implementation of this adapter simply display static dummy
	 * contents. You need to write the code for displaying actual contents.
	 */

	static class ViewHolderItem {
		ImageView profile;
		TextView lbl;
		TextView lbl2;
		TextView lbl3;
		ImageView img2;
		ImageView img3;
		TextView invite;

	}

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
			ViewHolderItem viewHolder;
			if(v==null){
				v = LayoutInflater.from(getActivity()).inflate(
						R.layout.contact_item, null);

				viewHolder = new ViewHolderItem();
				viewHolder.lbl = (TextView) v.findViewById(R.id.lblContactDisplayName);
				viewHolder.lbl2 = (TextView) v.findViewById(R.id.lblContactPhone);
				viewHolder.lbl3 = (TextView) v.findViewById(R.id.lblContactStatus);
				viewHolder.profile = (ImageView) v.findViewById(R.id.imgContactListItem);
				viewHolder.img2 = (ImageView) v.findViewById(R.id.online);
				viewHolder.img3 = (ImageView) v.findViewById(R.id.messageicon);
				viewHolder.invite = (TextView) v.findViewById(R.id.invite_button);

				v.setTag(viewHolder);
			}else {
				viewHolder = (ViewHolderItem) v.getTag();
			}


//			Holder holder=new Holder();
			ContactItem c = getItem(pos);
//			TextView lbl = (TextView) v.findViewById(R.id.lblContactDisplayName);
			viewHolder.lbl.setText(c.getUserName());

//			TextView lbl2 = (TextView) v.findViewById(R.id.lblContactPhone);
			viewHolder.lbl2.setText(c.getPhone());

//			TextView lbl3 = (TextView) v.findViewById(R.id.lblContactStatus);
			viewHolder.lbl3.setText(c.status());

//			holder.profile = (ImageView) v.findViewById(R.id.imgContactListItem);

			if (c.getProfileimg() != null) {
				Glide
						.with(reference)
						.load(c.getProfileimg())
						.thumbnail(0.1f)
						.centerCrop()
						.transform(new CircleTransform(getContext()))
						.placeholder(R.drawable.avatar)
						.into(viewHolder.profile);

			}else{
				viewHolder.profile.setImageResource(R.drawable.avatar);
			}
//				try {
////					photo_stream.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}

			//img.setImageResource(c.getIcon());

//			ImageView img2 = (ImageView) v.findViewById(R.id.online);
			viewHolder.img2.setVisibility(c.isOnline() ? View.VISIBLE : View.INVISIBLE);

//			ImageView img3 = (ImageView) v.findViewById(R.id.messageicon);
			viewHolder.img3.setVisibility(c.hasUnreadMessage() ? View.VISIBLE : View.INVISIBLE);

//			TextView invite = (TextView) v.findViewById(R.id.invite_button);
			viewHolder.invite.setVisibility(c.lastName().equals("true") ? View.INVISIBLE : View.VISIBLE );


			final String tempContactId = c.getUserId();
			final String tempPhone = c.getPhone();
			final ContactItem c_reference = c;

			v.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					sendChat(view,c_reference);

				}
			});
			viewHolder.invite.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					sendInvite(view, c_reference);

				}
			});


			return v;
		}

		// Filter Class
		public void filter(String charText) {
			charText = charText.toLowerCase(Locale.getDefault());
            if(backupList.size() < totalCount) {
                backupList.addAll(contactList);
            }
			contactList.clear();
			if (charText.length() == 0) {
				contactList.addAll(backupList);
			} else {
				for (ContactItem contactItem : backupList) {
					if (contactItem.getUserName().toLowerCase(Locale.getDefault())
							.startsWith(charText)) {
						contactList.add(contactItem);
					}
				}
			}
			Set<ContactItem> duplicate = new HashSet<ContactItem>(contactList);
			contactList.clear();
			contactList.addAll(new ArrayList<ContactItem>(duplicate));

			// Sorting NOTE: this is commented by sojharo as it is causing irregular list - we don't sort
			// on basis of contact's username first. We sort on basis of who is on cloudkibo and who is not
			// first and then two lists are partially sorted according to alphabet
			Collections.sort(contactList, new Comparator<ContactItem>() {
				@Override
				public int compare(ContactItem c2, ContactItem c1)
				{

					return  c2.getUserName().compareTo(c1.getUserName());
				}
			});

			notifyDataSetChanged();
		}

		public void sendChat(View v, ContactItem c){

			if(c.lastName().equals("false")){
				sendInvite(v, c);
				return;
			}

			final String tempContactId = c.getUserId();

			//Intent chatIntent = new Intent(getActivity().getApplicationContext(), ChatList.class);
			//chatIntent.putExtra("contactUserNameToChat", contactList.get(pos).getUserName());
			//startActivity(chatIntent);
			c.setUnReadMessage(false);
			contactAdapter.notifyDataSetChanged();

			Bundle bundle = new Bundle();
			bundle.putString("contactusername", c.getUserName());
			bundle.putString("contactphone", c.getPhone());
			bundle.putString("contactid", c.getUserId());
			bundle.putString("authtoken", authtoken);

			GroupChat groupChatFragment = new GroupChat();
			groupChatFragment.setArguments(bundle);

			getFragmentManager().beginTransaction()
					.replace(R.id.content_frame, groupChatFragment, "groupChatFragmentTag")
					.addToBackStack(c.getUserName()).commit();
		}

		public void sendInvite(View v, ContactItem c){
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) // At least KitKat
			{
				String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(getActivity().getApplicationContext()); // Need to change the build to API 19

				Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
				sendIntent.setType("text/plain");
				sendIntent.setData(Uri.parse("smsto:" +  c.getPhone()));
				//sendIntent.putExtra(Intent.EXTRA_TEXT, "Join me on CloudKibo for video chat. Download from https://www.cloudkibo.com");
				sendIntent.putExtra("sms_body", "Join me on CloudKibo for video chat. Download from https://play.google.com/store/apps/details?id=com.cloudkibo&hl=en");

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
				smsIntent.putExtra("sms_body","Join me on CloudKibo for video chat. Download from https://play.google.com/store/apps/details?id=com.cloudkibo&hl=en");
				startActivity(smsIntent);
			}
		}

	}

	public String getFragmentName()
	{
		return "ContactList";
	}

	public String getFragmentContactPhone () { return ""; }

}
