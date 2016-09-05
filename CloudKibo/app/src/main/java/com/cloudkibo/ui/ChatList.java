package com.cloudkibo.ui;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudkibo.NewChat;
//import com.cloudkibo.R;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.model.ChatItem;
import com.cloudkibo.model.Conversation;
import com.cloudkibo.utils.IFragmentName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

/**
 * The Class ChatList is the Fragment class that is launched when the user
 * clicks on Chats option in Left navigation drawer. It shows a dummy list of
 * user's chats. You need to write your own code to load and display actual
 * chat.
 */

public class ChatList extends CustomFragment implements IFragmentName
{

	/** The Chat list. */
	private ArrayList<ChatItem> chatList;

	private ChatAdapter adp;

	private String authtoken;

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.chat_list, null);

		authtoken = getActivity().getIntent().getExtras().getString("authtoken");

		loadChatList();
		ListView list = (ListView) v.findViewById(R.id.list);
		adp = new ChatAdapter();
		list.setAdapter(adp);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3)
			{

				Bundle bundle = new Bundle();
				bundle.putString("contactusername", chatList.get(pos).getName());
				bundle.putString("contactphone", chatList.get(pos).getTitle());
				bundle.putString("authtoken", authtoken);

				GroupChat groupChatFragment = new GroupChat();
				groupChatFragment.setArguments(bundle);

				getFragmentManager().beginTransaction()
						.replace(R.id.content_frame, groupChatFragment, "groupChatFragmentTag")
						.addToBackStack(chatList.get(pos).getName()).commit();

			}
		});
		adp.notifyDataSetChanged();

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
		if (v.getId() == R.id.btnNewChat)
			startActivity(new Intent(getActivity(), NewChat.class));
	}

	public void loadChatList()
	{
		DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
		try{

			ArrayList<ChatItem> chatList1 = new ArrayList<ChatItem>();

			JSONArray chats = db.getChatList();

			for (int i=0; i < chats.length(); i++) {
				JSONObject row = chats.getJSONObject(i);

				DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);
				Date date = format.parse("2001-07-04T12:08:56.235-0700");

				chatList1.add(new ChatItem(
						row.getString("display_name"),
						row.getString("contact_phone"),
						row.getString("msg"), row.getString("date"), R.drawable.user1, false,
						false, Integer.parseInt(row.getString("pendingMsgs"))));

			}

			this.chatList = new ArrayList<ChatItem>(chatList1);
			//this.chatList.addAll(chatList);
			//this.chatList.addAll(chatList);
			if(adp != null)
				adp.notifyDataSetChanged();

		} catch(JSONException e){
			e.printStackTrace();
		} catch (ParseException e){
			e.printStackTrace();
		}


	}

	/**
	 * The Class CutsomAdapter is the adapter class for Chat ListView.
	 */
	private class ChatAdapter extends BaseAdapter
	{

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount()
		{
			return chatList.size();
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public ChatItem getItem(int arg0)
		{
			return chatList.get(arg0);
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
						R.layout.chat_item, null);

			ChatItem c = getItem(pos);
			TextView lbl = (TextView) v.findViewById(R.id.lblContactDisplayName);
			lbl.setText(c.getName());
			if(c.getPendingMsgs() > 0) lbl.setTextColor(getResources().getColor(R.color.black));
			else lbl.setTextColor(getResources().getColor(R.color.main_color_green));

			lbl = (TextView) v.findViewById(R.id.lbl2);
			lbl.setText(c.getDate());

			lbl = (TextView) v.findViewById(R.id.lblContactPhone);
			lbl.setText(c.getTitle());

			lbl = (TextView) v.findViewById(R.id.lblPendingMsgs);
			if(c.getPendingMsgs() > 0) lbl.setText(Integer.toString(c.getPendingMsgs()));
			else lbl.setText("");

			lbl = (TextView) v.findViewById(R.id.lblContactStatus);
			lbl.setText(c.getMsg());
			if(c.getPendingMsgs() > 0) lbl.setTextColor(getResources().getColor(R.color.black));
			else lbl.setTextColor(getResources().getColor(R.color.main_color_gray_lt));

			ImageView img = (ImageView) v.findViewById(R.id.img1);
			img.setImageResource(c.getIcon());

			img = (ImageView) v.findViewById(R.id.img2);
			img.setImageResource(c.isGroup() ? R.drawable.ic_group
					: R.drawable.ic_lock);

			img = (ImageView) v.findViewById(R.id.online);
			img.setVisibility(c.isOnline() ? View.VISIBLE : View.INVISIBLE);
			return v;
		}

	}
	
	 public String getFragmentName()
     {
       return "ChatList";
     }

	public String getFragmentContactPhone()
	{
		return "About Chat";
	}
}
