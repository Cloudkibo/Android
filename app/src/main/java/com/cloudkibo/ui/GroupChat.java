package com.cloudkibo.ui;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cloudkibo.MainActivity;
//import com.cloudkibo.R;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomActivity;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.library.CircleTransform;
import com.cloudkibo.library.GroupUtility;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.library.Utility;
import com.cloudkibo.model.Conversation;
import com.cloudkibo.utils.IFragmentName;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.internal.DowngradeableSafeParcel;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

/**
 * The Class GroupChat is the Fragment class that is launched when the user
 * clicks on a Chat item in ChatList fragment. The current implementation simply
 * shows dummy conversations and when you send a chat message it will show a
 * dummy auto reply message. You can write your own code for actual Chat.
 */
public class GroupChat extends CustomFragment implements IFragmentName
{

	/** The Conversation list. */
	private ArrayList<Conversation> convList;

	/** The chat adapter. */
	private ChatAdapter adp;

	/** The Editext to compose the message. */
	private EditText txt;

	private String authtoken;

	private HashMap<String, String> user;

	String contactName;
	String contactPhone;

	View view;

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.group_chat, null);
		view = v;
		setHasOptionsMenu(true);

		contactName = this.getArguments().getString("contactusername");

		contactPhone = this.getArguments().getString("contactphone");

		authtoken = this.getArguments().getString("authtoken");

		if(contactName.equals(contactPhone)){
			LinearLayout tabs = (LinearLayout) v.findViewById(R.id.newContactOptionsBtns);
			tabs.setVisibility(View.VISIBLE);
			Button tab1 = (Button) v.findViewById(R.id.tab1);
			tab1.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					MainActivity act1 = (MainActivity) getActivity();
					act1.ToastNotify2("Under Construction");
				}
			});
			Button tab2 = (Button) v.findViewById(R.id.tab2);
			tab2.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					MainActivity act1 = (MainActivity) getActivity();
					act1.createContact(contactPhone);
				}
			});
		}

		DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());

		user = db.getUserDetails();

		loadConversationList();

		ListView list = (ListView) v.findViewById(R.id.list);
		adp = new ChatAdapter();
		list.setAdapter(adp);
		list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		list.setStackFromBottom(true);


		list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
				return false;
			}
		});
		registerForContextMenu(list);


		adp.notifyDataSetChanged();

		txt = (EditText) v.findViewById(R.id.txt);
		txt.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_MULTI_LINE);

		setTouchNClick(v.findViewById(R.id.btnSend));
		Utility.getLastSeenStatus(contactPhone, authtoken, getContext());

		return v;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (menu != null) {
			menu.findItem(R.id.archived).setVisible(false);
			menu.findItem(R.id.language).setVisible(false);
			menu.findItem(R.id.backup_setting).setVisible(false);
		}
		inflater.inflate(R.menu.chat, menu);  // Use filter.xml from step 1
		getActivity().getActionBar().setSubtitle("Last seen on: ");

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == R.id.callMenu){
			MainActivity act1 = (MainActivity)getActivity();

			act1.callThisPerson(contactPhone,
					contactName);
			return true;
		}
		/*if(id == R.id.attachMenu){
			PopupMenu popup = new PopupMenu(getActivity().getApplicationContext(), view);
			MenuInflater inflater = popup.getMenuInflater();
			inflater.inflate(R.menu.attachment, popup.getMenu());
			popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					switch (item.getItemId()) {
						case R.id.sendImage:
							MainActivity act3 = (MainActivity)getActivity();
							act3.uploadChatAttachment("image");
							return true;
						case R.id.sendDoc:
							MainActivity act2 = (MainActivity)getActivity();
							act2.uploadChatAttachment("document");
							return true;
						default:
							return false;

					}
				}
			});
			popup.show();
			return true;
		}*/
		if(id == R.id.sendImage){
			// todo add logic here
			MainActivity act3 = (MainActivity)getActivity();
			act3.uploadChatAttachment("image");

			return true;
		}
		if(id == R.id.sendDoc){
			// todo add logic here
			MainActivity act2 = (MainActivity)getActivity();
			act2.uploadChatAttachment("document");

			return true;
		}
		if(id == R.id.sendContact){
			Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
			getActivity().startActivityForResult(contactPickerIntent, 129);

			return true;
		}
		if(id == R.id.sendLocation){

			PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

			try {
				getActivity().startActivityForResult(builder.build(MainActivity.mainActivity), 141);
			} catch (GooglePlayServicesRepairableException e) {
				e.printStackTrace();
			} catch (GooglePlayServicesNotAvailableException e) {
				e.printStackTrace();
			}

			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// check whether the result is ok

		if (resultCode == Activity.RESULT_OK) {
			// Check for the request code, we might be usign multiple startActivityForReslut
			switch (requestCode) {
				case 129:
					Cursor cursor = null;
					try {
						String phoneNo = null ;
						String name = null;
						String photo_uri = null;
						Uri uri = data.getData();
						cursor = getContext().getContentResolver().query(uri, null, null, null, null);
						cursor.moveToFirst();
						int  phoneIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
						int  nameIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
						int  photoIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI);
						phoneNo = cursor.getString(phoneIndex);
						name = cursor.getString(nameIndex);
						photo_uri = cursor.getString(photoIndex);
						sendContact(name, phoneNo, photo_uri);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				case 141:
					Place place = PlacePicker.getPlace(data, MainActivity.mainActivity);
					String placename = String.format("%s", place.getName());
					String latitude = String.valueOf(place.getLatLng().latitude);
					String longitude = String.valueOf(place.getLatLng().longitude);
					String address = String.format("%s", place.getAddress());
					sendLocation(latitude, longitude);
					break;
			}
		} else {
			Log.e("MainActivity", "Failed to pick contact");
		}

//        super.onActivityResult(requestCode, resultCode, data);
	}

	/* (non-Javadoc)
	 * @see com.socialshare.custom.CustomFragment#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v)
	{
		super.onClick(v);
		if (v.getId() == R.id.btnSend)
		{
			sendMessage();

		}

	}

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuinfo){
		super.onCreateContextMenu(menu, v, menuinfo);

		menu.setHeaderTitle(getString(R.string.common_select_action));
		menu.add(0, v.getId(), 0, getString(R.string.common_message_info));
	}

	public boolean onContextItemSelected(MenuItem item){

		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

		if(item.getTitle() == getString(R.string.common_message_info)){

			MessageInfo mInfoFrag = new MessageInfo();
			Bundle bundle = new Bundle();

			bundle.putString("authtoken",authtoken);
			bundle.putString("message",convList.get(info.position).getMsg());
			bundle.putString("status",convList.get(info.position).getStatus());
			bundle.putString("date",convList.get(info.position).getDate());

			mInfoFrag.setArguments(bundle);
			getFragmentManager().beginTransaction()
					.replace(R.id.content_frame, mInfoFrag, "messageInfoFragmentTag")
					.addToBackStack("Message Info")
					.commit();
		}



		return true;
	}

	private void sendMessage()
	{
		try {
			if (txt.length() == 0)
				return;

			String uniqueid = Long.toHexString(Double.doubleToLongBits(Math.random()));
			uniqueid += (new Date().getYear()) + "" + (new Date().getMonth()) + "" + (new Date().getDay());
			uniqueid += (new Date().getHours()) + "" + (new Date().getMinutes()) + "" + (new Date().getSeconds());

			InputMethodManager imm = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(txt.getWindowToken(), 0);

			String messageString = txt.getText().toString();

			DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
			db.addChat(contactPhone, user.get("phone"), user.get("display_name"),
					messageString, Utility.getCurrentTimeInISO(), "pending", uniqueid, "chat", "");

			convList.add(new Conversation(messageString,
					Utility.convertDateToLocalTimeZoneAndReadable(Utility.getCurrentTimeInISO()),
					true, true, "pending", uniqueid, "chat"));
			adp.notifyDataSetChanged();

			sendMessageUsingAPI(messageString, uniqueid, "chat");

			txt.setText(null);
		} catch (ParseException e){
			e.printStackTrace();
		}
	}

	private void sendContact(String display_name, String phone, String contact_image)
	{
		try {

			String uniqueid = Long.toHexString(Double.doubleToLongBits(Math.random()));
			uniqueid += (new Date().getYear()) + "" + (new Date().getMonth()) + "" + (new Date().getDay());
			uniqueid += (new Date().getHours()) + "" + (new Date().getMinutes()) + "" + (new Date().getSeconds());

			String messageString = display_name + ":" + phone;

			DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
			db.addChat(contactPhone, user.get("phone"), user.get("display_name"),
					messageString, Utility.getCurrentTimeInISO(), "pending", uniqueid, "contact", "");

			convList.add(new Conversation(messageString,
					Utility.convertDateToLocalTimeZoneAndReadable(Utility.getCurrentTimeInISO()),
					true, true, "pending", uniqueid, "contact").setContact_image(contact_image));
			adp.notifyDataSetChanged();

			sendMessageUsingAPI(messageString, uniqueid, "contact");

			txt.setText(null);
		} catch (ParseException e){
			e.printStackTrace();
		}
	}

	private void sendLocation(String latitude, String longitude)
	{
		try {

			String uniqueid = Long.toHexString(Double.doubleToLongBits(Math.random()));
			uniqueid += (new Date().getYear()) + "" + (new Date().getMonth()) + "" + (new Date().getDay());
			uniqueid += (new Date().getHours()) + "" + (new Date().getMinutes()) + "" + (new Date().getSeconds());

			String messageString = latitude + ":" + longitude;

			DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
			db.addChat(contactPhone, user.get("phone"), user.get("display_name"),
					messageString, Utility.getCurrentTimeInISO(), "pending", uniqueid, "location", "");

			convList.add(new Conversation(messageString,
					Utility.convertDateToLocalTimeZoneAndReadable(Utility.getCurrentTimeInISO()),
					true, true, "pending", uniqueid, "location"));
			adp.notifyDataSetChanged();

			sendMessageUsingAPI(messageString, uniqueid, "location");

			txt.setText(null);
		} catch (ParseException e){
			e.printStackTrace();
		}
	}

	public void receiveMessage(String msg, String uniqueid, String from, String date, String type) {

		try {

			final MediaPlayer mp = MediaPlayer.create(getActivity().getApplicationContext(), R.raw.bell);
			mp.start();
			GroupUtility groupUtility = new GroupUtility(getContext());
			groupUtility.sendNotification("Single message", msg);
			// todo see if this really needs the uniqueid and status
			convList.add(new Conversation(msg, Utility.convertDateToLocalTimeZoneAndReadable(date), false, true, "seen", uniqueid, type));

			adp.notifyDataSetChanged();

			updateChatStatus("seen", uniqueid);
			sendMessageStatusUsingAPI("seen", uniqueid, from);
		} catch (ParseException e){
			e.printStackTrace();
		}

	}

	public void sendMessageUsingAPI(final String msg, final String uniqueid, final String type){
		new AsyncTask<String, String, JSONObject>() {

			@Override
			protected JSONObject doInBackground(String... args) {
				UserFunctions userFunction = new UserFunctions();
				JSONObject message = new JSONObject();

				try {
					message.put("from", user.get("phone"));
					message.put("to", contactPhone);
					message.put("fromFullName", user.get("display_name"));
					message.put("msg", msg);
					message.put("date", Utility.getCurrentTimeInISO());
					message.put("uniqueid", uniqueid);
					message.put("type", type);
					message.put("file_type", "");
				} catch (JSONException e){
					e.printStackTrace();
				}

				return userFunction.sendChatMessageToServer(message, authtoken);
			}

			@Override
			protected void onPostExecute(JSONObject row) {
				try {

					if (row != null) {
						if(row.has("status")){
							updateChatStatus(row.getString("status"), row.getString("uniqueid"));
							updateStatusSentMessage(row.getString("status"), row.getString("uniqueid"));
						}
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

		}.execute();
	}

	public void sendMessageStatusUsingAPI(final String status, final String uniqueid, final String sender){
		new AsyncTask<String, String, JSONObject>() {

			@Override
			protected JSONObject doInBackground(String... args) {
				UserFunctions userFunction = new UserFunctions();
				JSONObject message = new JSONObject();

				try {
					message.put("sender", sender);
					message.put("status", status);
					message.put("uniqueid", uniqueid);
				} catch (JSONException e){
					e.printStackTrace();
				}

				updateChatStatus("seen", uniqueid);
				addChatHistorySync(uniqueid, sender);

				return userFunction.sendChatMessageStatusToServer(message, authtoken);
			}

			@Override
			protected void onPostExecute(JSONObject row) {
				try {
					if (row != null) {
						if(row.has("status")){
							resetSpecificChatHistorySync(row.getString("uniqueid"));
							updateChatStatus("seen", row.getString("uniqueid"));
						}
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

		}.execute();
	}

	public void updateStatusSentMessage(String status, String uniqueid){
		for(int i=convList.size()-1; i>-1; i--){
			if(convList.get(i).getUniqueid().equals(uniqueid)){
				convList.get(i).setStatus(status);
				break;
			}
		}
		adp.notifyDataSetChanged();
	}

	public void updateChatStatus(String status, String uniqueid){
		try {
			DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
			db.updateChat(status, uniqueid);
		} catch (NullPointerException e){
			e.printStackTrace();
		}
	}

	public void resetSpecificChatHistorySync(String uniqueid){
		try {
			DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
			db.resetSpecificChatHistorySync(uniqueid);
		} catch (NullPointerException e){
			e.printStackTrace();
		}
	}

	public void addChatHistorySync(String uniqueid, String from){
		try {
			DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
			db.addChatSyncHistory("seen", uniqueid, from);
		} catch (NullPointerException e){
			e.printStackTrace();
		}
	}


	/**
	 * This method currently loads a dummy list of conversations. You can write the
	 * actual implementation of loading conversations.
	 */
	public void loadConversationList()
	{
		convList = new ArrayList<Conversation>();

		loadChatFromDatabase();

	}

	public void setNewContactName(String contact_name){
		contactName = contact_name;
		LinearLayout tabs = (LinearLayout) view.findViewById(R.id.newContactOptionsBtns);
		tabs.setVisibility(View.GONE);
	}

	public void loadChatFromDatabase(){
		DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());

		try {

			MainActivity act1 = (MainActivity)getActivity();

			JSONArray jsonA = db.getChat(act1.getUserPhone(), contactPhone);

			ArrayList<Conversation> chatList1 = new ArrayList<Conversation>();

			for (int i=0; i < jsonA.length(); i++) {
				JSONObject row = jsonA.getJSONObject(i);

				if(row.getString("toperson").equals(contactPhone))
					chatList1.add(new Conversation(
						row.getString("msg"),
						Utility.convertDateToLocalTimeZoneAndReadable(row.getString("date")),
						true, true, row.getString("status"), row.getString("uniqueid"),
							row.has("type") ? row.getString("type") : ""));
				else
					chatList1.add(new Conversation(
							row.getString("msg"),
							Utility.convertDateToLocalTimeZoneAndReadable(row.getString("date")),
							false, true, row.getString("status"), row.getString("uniqueid"),
							row.has("type") ? row.getString("type") : ""));

				if(row.getString("fromperson").equals(contactPhone)){
					if(row.getString("status").equals("delivered")){
						sendMessageStatusUsingAPI("seen", row.getString("uniqueid"), row.getString("fromperson"));
					}
				}/* else {
					if(row.getString("status").equals("pending")){
						if(act1.isSocketConnected()){
							act1.sendPendingMessage(contactPhone, row.getString("msg"), row.getString("uniqueid"));
						}
					}
				}*/
			}

			convList.clear();

			convList.addAll(chatList1);

			if(adp != null)
				adp.notifyDataSetChanged();

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * The Class CutsomAdapter is the adapter class for Chat ListView. The
	 * currently implementation of this adapter simply display static dummy
	 * contents. You need to write the code for displaying actual contents.
	 */
	private class ChatAdapter extends BaseAdapter
	{

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount()
		{
			return convList.size();
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Conversation getItem(int arg0)
		{
			return convList.get(arg0);
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
			Conversation c = getItem(pos);
			if(c.getType().equals("contact")){
				v = LayoutInflater.from(getActivity()).inflate(
						R.layout.chat_item_contact, null);
				if (c.isSent()) {
					TextView contact_name = (TextView) v.findViewById(R.id.contact_name);
					contact_name.setText(c.getMsg().split(":")[0]);
					ImageView contact_image = (ImageView) v.findViewById(R.id.contact_image);
					DatabaseHandler db = new DatabaseHandler(MainActivity.mainActivity);
					String image_uri = db.getContactImage(c.getMsg().split(":")[1]);
					Glide
							.with(MainActivity.mainActivity)
							.load(image_uri)
							.thumbnail(0.1f)
							.centerCrop()
							.transform(new CircleTransform(MainActivity.mainActivity))
							.placeholder(R.drawable.avatar)
							.into(contact_image);
				}
				else {
					v = LayoutInflater.from(getActivity()).inflate(
							R.layout.chat_item_contact_received, null);
					TextView contact_name = (TextView) v.findViewById(R.id.contact_name);
					contact_name.setText(c.getMsg().split(":")[0]);
					final Conversation cTemp = c;
					(v.findViewById(R.id.addButton)).setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							MainActivity act1 = MainActivity.mainActivity;
							act1.createContact(cTemp.getMsg().split(":")[1], cTemp.getMsg().split(":")[0]);
						}
					});
				}
				return  v;
			}

			if(c.getType().equals("location")){
				v = LayoutInflater.from(getActivity()).inflate(
						R.layout.chat_image_me, null);
				String name = user.get("display_name");
				if (!c.isSent()) {
					v = LayoutInflater.from(getActivity()).inflate(
							R.layout.chat_image_sender, null);
					name = contactName;
				}
				final String latitude = c.getMsg().split(":")[0];
				final String longitude = c.getMsg().split(":")[1];
				ImageView container_image = (ImageView) v.findViewById(R.id.row_stamp);
				String image_uri = "http://maps.google.com/maps/api/staticmap?center="+ latitude +","+ longitude +"&zoom=18&size=500x300&sensor=TRUE_OR_FALSE";
				Glide
						.with(MainActivity.mainActivity)
						.load(image_uri)
						.thumbnail(0.1f)
						.centerCrop()
						.transform(new CircleTransform(MainActivity.mainActivity))
						.placeholder(android.R.drawable.ic_dialog_map)
						.into(container_image);

				final String nameOfMapPerson = name;
				container_image.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Double lat = Double.parseDouble(latitude);
						Double  lon = Double.parseDouble(longitude);
						String uri = "geo:" + lat + ","
								+lon + "?q=" + lat
								+ "," + lon;
						startActivity(new Intent(android.content.Intent.ACTION_VIEW,
								Uri.parse(uri)));
					}
				});
				return  v;
			}

			if (c.isSent()) {
				if(true)//(c.getType().equals("chat"))
					v = LayoutInflater.from(getActivity()).inflate(
						R.layout.chat_item_sent, null);
				else
					v = LayoutInflater.from(getActivity()).inflate(
							R.layout.chat_image_me, null);
			} else {
				if(true)//(c.getType().equals("chat"))
					v = LayoutInflater.from(getActivity()).inflate(
						R.layout.chat_item_rcv, null);
				else
					v = LayoutInflater.from(getActivity()).inflate(
							R.layout.chat_image_sender, null);
			}


			if(true){//(c.getType().equals("chat")) {
				TextView lbl = (TextView) v.findViewById(R.id.lblContactDisplayName);
//				String date_temp = c.getDate().replaceAll("-", "/").split(" ")[0].split("/")[1] + "/" +c.getDate().replaceAll("-", "/").split(" ")[0].split("/")[2];
//				c.getDate() +" - "+ date_temp+" "+Utility.dateConversion(c.getDate().replaceAll("-", "/").split("/",2)[1].split(" ")[1])
				DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				DateFormat outputFormat = new SimpleDateFormat("MM/dd KK:mm a");
				try {
					lbl.setText(outputFormat.format(inputFormat.parse(c.getDate())));
				} catch (ParseException e) {
					e.printStackTrace();
				}

				TextView phone = (TextView) v.findViewById(R.id.phone);
				phone.setVisibility(View.GONE);

				lbl = (TextView) v.findViewById(R.id.lbl2);
				lbl.setText(c.getMsg());

				lbl = (TextView) v.findViewById(R.id.lblContactPhone);
				if (c.isSuccess())
					lbl.setText(c.getStatus());
				else
					lbl.setText("");
			} else {
				/*if(c.getFile_type().equals("image")) {
					ImageView stamp = (ImageView) v.findViewById(R.id.row_stamp);
					stamp.setImageBitmap(BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.spinner));
					if(c.getOnLocal()){

					}
				}*/
			}

			return v;
		}

	}

	public String getFragmentName()
    {
      return "GroupChat";
    }

	public String getFragmentContactPhone() { return contactPhone; }
}
