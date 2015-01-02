package com.chatt.ui;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.Toast;

import com.chatt.MainActivity;
import com.chatt.R;
import com.chatt.custom.CustomFragment;
import com.chatt.model.ContactItem;
import com.chatt.model.Conversation;
import com.chatt.utils.IFragmentName;
import com.cloudkibo.library.DatabaseHandler;
import com.cloudkibo.library.UserFunctions;

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
	
	String contactName;
	String contactId;

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.group_chat, null);
		
		contactName = this.getArguments().getString("contactusername");
		
		contactId = this.getArguments().getString("contactid");
		
		authtoken = this.getArguments().getString("authtoken");
		
		loadConversationList();
		
		ListView list = (ListView) v.findViewById(R.id.list);
		adp = new ChatAdapter();
		list.setAdapter(adp);
		list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		list.setStackFromBottom(true);

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
		}

	}

	/**
	 * Call this method to Send message to opponent. The current implementation
	 * simply add an auto reply message with each sent message.
	 * You need to write actual logic for sending and receiving the messages.
	 */
	private void sendMessage()
	{
		if (txt.length() == 0)
			return;

		InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(txt.getWindowToken(), 0);

		String messageString = txt.getText().toString();
		
		MainActivity act1 = (MainActivity)getActivity();
		
		act1.sendMessage(contactName, contactId, messageString);
		
		convList.add(new Conversation(messageString, new Date().toString(), true, true));
		adp.notifyDataSetChanged();
		
		txt.setText(null);
	}
	
	public void receiveMessage(String msg){
		
		convList.add(new Conversation(msg, new Date().toString(), false, true));
		adp.notifyDataSetChanged();
		
	}

	/**
	 * This method currently loads a dummy list of conversations. You can write the
	 * actual implementation of loading conversations.
	 */
	private void loadConversationList()
	{
		convList = new ArrayList<Conversation>();
		
		loadChatFromDatabase();
		
		loadChatMessagesFromServer();

	}
	
	/**
	 * This method loads all the chats from the server and store them on sqlite database server.
	 */
	
	public void loadChatMessagesFromServer()
	{
		new AsyncTask<String, String, JSONArray>() {

			@Override
			protected JSONArray doInBackground(String... args) {
				MainActivity act1 = (MainActivity)getActivity();
				
				UserFunctions userFunction = new UserFunctions();
				JSONArray json = null;
				try {
					json = userFunction.getUserChat(act1.getUserName(), contactName, authtoken).getJSONArray("msg");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return json;
			}

			@Override
			protected void onPostExecute(JSONArray jsonA) {
				try {

					if (jsonA != null) {

						MainActivity act1 = (MainActivity)getActivity();
						
						DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
						
						db.resetSpecificChat(act1.getUserName(), contactName);
						
						for (int i=0; i < jsonA.length(); i++) {
							JSONObject row = jsonA.getJSONObject(i);
							db.addChat(row.getString("to"), row.getString("from"), row.getString("fromFullName"),
									row.getString("msg"), row.getString("date"));
						}
						
						loadChatFromDatabase();
						adp.notifyDataSetChanged();
						
						
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
            
        }.execute();
	}

	
	public void loadChatFromDatabase(){
		DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
		
		try {
			
			MainActivity act1 = (MainActivity)getActivity();
			
			JSONArray jsonA = db.getChat(act1.getUserName(), contactName);
			
			ArrayList<Conversation> chatList1 = new ArrayList<Conversation>();
			
			for (int i=0; i < jsonA.length(); i++) {
				JSONObject row = jsonA.getJSONObject(i);
				
				if(row.getString("toperson").equals(contactName))
					chatList1.add(new Conversation(
						row.getString("msg"),
						row.getString("date"),
						false, true));
				else
					chatList1.add(new Conversation(
							row.getString("msg"),
							row.getString("date"),
							true, true));
				
			}
			
			convList.clear();

			convList.addAll(chatList1);
			
		} catch (JSONException e) {
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
				lbl.setText("Delivered");
			else
				lbl.setText("");

			return v;
		}

	}
	
	public String getFragmentName()
    {
      return "GroupChat";
    }
}
