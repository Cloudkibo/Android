package com.cloudkibo.ui;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudkibo.MainActivity;
//import com.cloudkibo.R;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.library.Utility;
import com.cloudkibo.model.Conversation;
import com.cloudkibo.utils.IFragmentName;

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

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.group_chat, null);
		
		contactName = this.getArguments().getString("contactusername");

		contactPhone = this.getArguments().getString("contactphone");

		authtoken = this.getArguments().getString("authtoken");

		DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());

		user = db.getUserDetails();

		loadConversationList();
		
		ListView list = (ListView) v.findViewById(R.id.list);
		adp = new ChatAdapter();
		list.setAdapter(adp);
		list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		list.setStackFromBottom(true);
		adp.notifyDataSetChanged();

		txt = (EditText) v.findViewById(R.id.txt);
		txt.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_MULTI_LINE);

		setTouchNClick(v.findViewById(R.id.btnCamera));
		setTouchNClick(v.findViewById(R.id.btnSend));
		return v;
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
		} else if (v.getId() == R.id.btnCamera) {
			MainActivity act1 = (MainActivity)getActivity();

			act1.callThisPerson(contactPhone,
					 contactName);
		}

	}

	/**
	 * Call this method to Send message to opponent. The current implementation
	 * simply add an auto reply message with each sent message.
	 * You need to write actual logic for sending and receiving the messages.
	 */
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

			MainActivity act1 = (MainActivity) getActivity();

			// todo deprecated remove this way
			//act1.sendMessage(contactPhone, messageString, uniqueid);

			DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
			db.addChat(contactPhone, user.get("phone"), user.get("display_name"),
					messageString, Utility.getCurrentTimeInISO(), "pending", uniqueid);

			convList.add(new Conversation(messageString, Utility.convertDateToLocalTimeZoneAndReadable(Utility.getCurrentTimeInISO()), true, true, "pending", uniqueid));
			adp.notifyDataSetChanged();

			sendMessageUsingAPI(messageString, uniqueid);

			txt.setText(null);
		} catch (ParseException e){
			e.printStackTrace();
		}
	}
	
	public void receiveMessage(String msg, String uniqueid, String from, String date) {

		try {

			final MediaPlayer mp = MediaPlayer.create(getActivity().getApplicationContext(), R.raw.bell);
			mp.start();

			// todo see if this really needs the uniqueid and status
			convList.add(new Conversation(msg, Utility.convertDateToLocalTimeZoneAndReadable(date), false, true, "seen", uniqueid));

			adp.notifyDataSetChanged();

			MainActivity act1 = (MainActivity) getActivity();

			act1.updateChatStatus("seen", uniqueid);
			//act1.sendMessageStatusUsingSocket("seen", uniqueid, from);
			sendMessageStatusUsingAPI("seen", uniqueid, from);
		} catch (ParseException e){
			e.printStackTrace();
		}
		
	}

	public void sendMessageUsingAPI(final String msg, final String uniqueid){
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
					message.put("type", "chat");
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
							MainActivity act1 = (MainActivity) getActivity();
							act1.updateChatStatus(row.getString("status"), row.getString("uniqueid"));
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

				return userFunction.sendChatMessageStatusToServer(message, authtoken);
			}

			@Override
			protected void onPostExecute(JSONObject row) {
				try {

					Boolean gotGoodServerResponse = false;
					if (row != null) {
						if(row.has("status")){
							MainActivity act1 = (MainActivity) getActivity();
							act1.resetSpecificChatHistorySync(row.getString("uniqueid"));
							act1.updateChatStatus(row.getString("status"), row.getString("uniqueid"));
							gotGoodServerResponse = true;
						}
					}
					if(!gotGoodServerResponse){
						MainActivity act1 = (MainActivity) getActivity();
						act1.updateChatStatus("seen", row.getString("uniqueid"));
						act1.addChatHistorySync(row.getString("uniqueid"), row.getString("fromperson"));
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

		}.execute();
	}

	public void updateStatusSentMessage(String status, String uniqueid){
		loadConversationList();
		adp.notifyDataSetChanged();
	}
	

	/**
	 * This method currently loads a dummy list of conversations. You can write the
	 * actual implementation of loading conversations.
	 */
	public void loadConversationList()
	{
		convList = new ArrayList<Conversation>();
		
		loadChatFromDatabase();
		
		//loadChatMessagesFromServer();

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
						true, true, row.getString("status"), row.getString("uniqueid")));
				else
					chatList1.add(new Conversation(
							row.getString("msg"),
							Utility.convertDateToLocalTimeZoneAndReadable(row.getString("date")),
							false, true, row.getString("status"), row.getString("uniqueid")));

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
			if (c.isSent())
				v = LayoutInflater.from(getActivity()).inflate(
						R.layout.chat_item_sent, null);
			else
				v = LayoutInflater.from(getActivity()).inflate(
						R.layout.chat_item_rcv, null);

			TextView lbl = (TextView) v.findViewById(R.id.lblContactDisplayName);
			lbl.setText(c.getDate());

			lbl = (TextView) v.findViewById(R.id.lbl2);
			lbl.setText(c.getMsg());

			lbl = (TextView) v.findViewById(R.id.lblContactPhone);
			if (c.isSuccess())
				lbl.setText(c.getStatus());
			else
				lbl.setText("");

			return v;
		}

	}
	
	public String getFragmentName()
    {
      return "GroupChat";
    }

	public String getFragmentContactPhone() { return contactPhone; }
}
