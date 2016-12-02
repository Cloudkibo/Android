package com.cloudkibo.ui;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cloudkibo.MainActivity;
import com.cloudkibo.NewChat;
//import com.cloudkibo.R;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.database.CloudKiboDatabaseContract;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.library.CircleTransform;
import com.cloudkibo.library.Utility;
import com.cloudkibo.model.ChatItem;
import com.cloudkibo.model.ContactItem;
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
	public static ArrayList<ChatItem> chatList;

	private ChatAdapter adp;

	private String authtoken;
	private ChatList reference = this;
	private ArrayList<String> contact_phone = new ArrayList<String>();

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.chat_list, null);
		setHasOptionsMenu(true);

		authtoken = getActivity().getIntent().getExtras().getString("authtoken");
		if(chatList == null){
			chatList =  new ArrayList<ChatItem>();
		}
		loadChatList();

		ListView list = (ListView) v.findViewById(R.id.list);
		adp = new ChatAdapter();
		list.setAdapter(adp);



        list.setOnItemClickListener(new OnItemClickListener() {

            @Override
			public void onItemClick(AdapterView<?> adapter, View v, int pos,long arg3)
			{
				Bundle bundle = new Bundle();
				bundle.putString("contactusername", chatList.get(pos).getName());
				bundle.putString("contactphone", chatList.get(pos).getTitle());
				bundle.putString("authtoken", authtoken);

				ChatItem item = (ChatItem) chatList.get(pos);
				if(item.isGroup()){
					GroupChatUI groupChatFragment = new GroupChatUI();
					bundle.putString("group_id", chatList.get(pos).getTitle());
					bundle.putString("authtoken", authtoken);
					groupChatFragment.setArguments(bundle);
					getFragmentManager().beginTransaction()
							.replace(R.id.content_frame, groupChatFragment, "groupChatFragmentTag")
							.addToBackStack(chatList.get(pos).getName()).commit();
				}else{
					GroupChat groupChatFragment = new GroupChat();
					bundle.putString("contactusername", chatList.get(pos).getName());
					bundle.putString("contactphone", chatList.get(pos).getTitle());
					bundle.putString("authtoken", authtoken);
					groupChatFragment.setArguments(bundle);
					getFragmentManager().beginTransaction()
						.replace(R.id.content_frame, groupChatFragment, "groupChatFragmentTag")
						.addToBackStack(chatList.get(pos).getName()).commit();
			    }
			}
		});

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                return false;
            }
        });
        registerForContextMenu(list);

		adp.notifyDataSetChanged();

		setTouchNClick(v.findViewById(R.id.btnNewChat));
//		Utility utility = new Utility();
//		utility.updateDatabaseWithContactImages(getContext(),contact_phone);
        loadChatList();
		return v;
	}

	/* (non-Javadoc)
	 * @see com.socialshare.custom.CustomFragment#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v)
	{
		super.onClick(v);
		if (v.getId() == R.id.btnNewChat) {
			startActivity(new Intent(getActivity(), NewChat.class));
		}
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);  // Use filter.xml from step 1
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.archived){
            ArchivedChat archivedChatFragment = new ArchivedChat();
            Bundle bundle = new Bundle();
            bundle.putString("authToken", authtoken);
            archivedChatFragment.setArguments(bundle);
            getFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, archivedChatFragment, "archivedChatFragmentTag").addToBackStack("Archived")
                    .commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuinfo){
        super.onCreateContextMenu(menu, v, menuinfo);

        menu.setHeaderTitle("Select the Action");
        menu.add(0, v.getId(), 0, "Archive");
    }

    public boolean onContextItemSelected(MenuItem item){

        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        Bundle bundle = new Bundle();
		try {
			if (item.getTitle() == "Archive") {

				DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
				ChatItem cItem = (ChatItem) chatList.get(info.position);
				if(cItem.isGroup()){
					db.setArchiveGroup(cItem.getTitle());
					Toast.makeText(getContext(),cItem.getTitle(),Toast.LENGTH_SHORT).show();
					chatList.remove(info.position);

				}else{


					db.setArchive(cItem.getTitle());
					Toast.makeText(getContext(), cItem.getTitle() , Toast.LENGTH_SHORT).show();
					chatList.remove(info.position);

				}

				if (adp != null) {
					adp.notifyDataSetChanged();
				}

			}
		} catch(Exception e){
			e.printStackTrace();
		}



        return true;
    }

//	@Override
//	protected View onResume(){
//		super.onResume();
//	}



	public void loadChatList()
	{
		final DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
        final ArrayList<ChatItem> chatList1 = new ArrayList<ChatItem>();

        new AsyncTask<String, String, ArrayList<ChatItem>>() {

            @Override
            protected ArrayList<ChatItem> doInBackground(String... args) {


                try{
                    contact_phone.clear();
					JSONArray chats = new JSONArray();
					if(chatList == null){
						 chats = db.getChatList();
					}else {
						chats = db.getChatListWithImages();
					}
//					JSONArray chats = db.getChatList();
//			JSONArray groups = db.getAllGroups();
                    JSONArray groups = db.getMyGroups(db.getUserDetails().get("phone"));
                    for (int i=0; i < chats.length(); i++) {
                        JSONObject row = chats.getJSONObject(i);
                        String image = row.optString("image_uri");
                        //if(row.getInt("isArchived") ==  0) {
                        chatList1.add(new ChatItem(
                                row.getString("display_name"),
                                row.getString("contact_phone"),
                                row.getString("msg"),
                                Utility.convertDateToLocalTimeZoneAndReadable(row.getString("date")),
                                R.drawable.user1, false,
                                false, Integer.parseInt(row.getString("pendingMsgs"))).setProfileImage(image));

                        //}
                        contact_phone.add(row.getString("contact_phone"));
                    }

                    for (int i=0; i < groups.length(); i++) {
                        JSONObject row = groups.getJSONObject(i);

                        //if (row.getInt("isArchived") == 0) {
                        chatList1.add(new ChatItem(
                                row.getString("group_name"),
                                row.getString("unique_id"),
                                "Last Message",
                                row.getString("date_creation"),
                                R.drawable.user1, false,
                                true, 0).setProfileImage(null));


                    }

                } catch(JSONException e){
                    e.printStackTrace();
                } catch (ParseException e){
                    e.printStackTrace();
                }

                return chatList1;
            }
            @Override
            protected void onPostExecute(ArrayList<ChatItem> chatList1) {
                chatList = new ArrayList<ChatItem>(chatList1);
                //this.chatList.addAll(chatList);
                //this.chatList.addAll(chatList);
                if(adp != null)
                    adp.notifyDataSetChanged();
            }
        }.execute();

	}

	public void setOnlineStatus(JSONArray contacts){

		for(int i=0; i<chatList.size(); i++)
			chatList.get(i).setOnline(false);

		try{
			for(int j=0; j<contacts.length(); j++)
				for(int i=0; i<chatList.size(); i++){
					if(chatList.get(i).getName().equals(contacts.getJSONObject(j).getString("phone"))){
						//contactList.get(i).setOnline(true);
						break;
					}
				}

			//contactAdapter.notifyDataSetChanged();

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

		}
	}

	public String getContactsDetails(String address, Context context) {
		DatabaseHandler db  = new DatabaseHandler(context);
		return  db.getContactImage(address);
	}

	static class ViewHolderItem{
		TextView lbl;
		TextView lbl2;
		TextView lbl3;
		TextView lbl4;
		TextView lbl5;
		ImageView profile;
		ImageView img2;
		ImageView img3;
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
			ViewHolderItem viewHolder;
			if (v == null){
				v = LayoutInflater.from(getActivity()).inflate(
						R.layout.chat_item, null);

				viewHolder = new ViewHolderItem();
				viewHolder.lbl = (TextView) v.findViewById(R.id.lblContactDisplayName);
				viewHolder.lbl2 = (TextView) v.findViewById(R.id.lbl2);
				viewHolder.lbl3 = (TextView) v.findViewById(R.id.lblContactPhone);
				viewHolder.lbl4 = (TextView) v.findViewById(R.id.lblPendingMsgs);
				viewHolder.lbl5 = (TextView) v.findViewById(R.id.lblContactStatus);
				viewHolder.profile  = (ImageView)v.findViewById(R.id.img1);
				viewHolder.img2 = (ImageView) v.findViewById(R.id.img2);
				viewHolder.img3 = (ImageView) v.findViewById(R.id.online);

				v.setTag(viewHolder);
			}else {
				viewHolder = (ViewHolderItem) v.getTag();
			}


			ChatItem c = getItem(pos);
//			TextView lbl = (TextView) v.findViewById(R.id.lblContactDisplayName);
			viewHolder.lbl.setText(c.getName());
			if(c.getPendingMsgs() > 0) viewHolder.lbl.setTextColor(getResources().getColor(R.color.black));
			else viewHolder.lbl.setTextColor(getResources().getColor(R.color.main_color_green));

//			TextView lbl2 = (TextView) v.findViewById(R.id.lbl2);
			viewHolder.lbl2.setText(c.getDate());

//			TextView lbl3 = (TextView) v.findViewById(R.id.lblContactPhone);
			viewHolder.lbl3.setText(c.getTitle());

//			TextView lbl4 = (TextView) v.findViewById(R.id.lblPendingMsgs);
			if(c.getPendingMsgs() > 0) viewHolder.lbl4.setText(Integer.toString(c.getPendingMsgs()));
			else viewHolder.lbl4.setText("");

//			TextView lbl5 = (TextView) v.findViewById(R.id.lblContactStatus);
			viewHolder.lbl5.setText(c.getMsg());

			if(c.getPendingMsgs() > 0) viewHolder.lbl5.setTextColor(getResources().getColor(R.color.black));
			else viewHolder.lbl5.setTextColor(getResources().getColor(R.color.main_color_gray_lt));

//			ImageView profile  = (ImageView)v.findViewById(R.id.img1);


			if(!c.isGroup()){
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

			}else{
				//viewHolder.profile.setImageResource(R.drawable.avatar);
				try {
					File f = new File(getActivity().getApplicationContext().getFilesDir(), c.getTitle());
					Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
					viewHolder.profile.setImageBitmap(b);
				} catch (FileNotFoundException e){
					e.printStackTrace();
				}
			}


//			ImageView img2 = (ImageView) v.findViewById(R.id.img2);
			viewHolder.img2.setImageResource(c.isGroup() ? R.drawable.ic_group
					: R.drawable.ic_lock);

//			ImageView img3 = (ImageView) v.findViewById(R.id.online);
			viewHolder.img3.setVisibility(c.isOnline() ? View.VISIBLE : View.INVISIBLE);
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
