package com.cloudkibo.ui;

import java.util.ArrayList;

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
import com.cloudkibo.model.ChatItem;
import com.cloudkibo.utils.IFragmentName;

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

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.chat_list, null);

		loadChatList();
		ListView list = (ListView) v.findViewById(R.id.list);
		list.setAdapter(new ChatAdapter());
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3)
			{
				getFragmentManager().beginTransaction()
						.replace(R.id.content_frame, new GroupChat())
						.addToBackStack("Group Chat").commit();
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
	 * This method currently loads a dummy list of chats. You can write the
	 * actual implementation of loading chats.
	 */
	private void loadChatList()
	{
		ArrayList<ChatItem> chatList = new ArrayList<ChatItem>();
		chatList.add(new ChatItem("Saba Channa", "Test Subject",
				"I am testing", "12:10PM", R.drawable.user1, true,
				false));
		this.chatList = new ArrayList<ChatItem>(chatList);
		//this.chatList.addAll(chatList);
		//this.chatList.addAll(chatList);

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

			lbl = (TextView) v.findViewById(R.id.lbl2);
			lbl.setText(c.getDate());

			lbl = (TextView) v.findViewById(R.id.lblContactPhone);
			lbl.setText(c.getTitle());

			lbl = (TextView) v.findViewById(R.id.lblContactStatus);
			lbl.setText(c.getMsg());

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
}
