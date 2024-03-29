package com.cloudkibo.ui;


import android.app.ActionBar;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.library.Utility;
import com.cloudkibo.model.ChatItem;
import com.cloudkibo.utils.IFragmentName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;


public class ArchivedChat extends CustomFragment implements IFragmentName {

    private String text;
    private ArrayList<ChatItem> archivedChats;
    private ChatAdapter adp;
    private String authToken;

    JSONArray lists;




    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.archived_chat, null);
        setHasOptionsMenu(true);

        Bundle args = getArguments();
        authToken = args.getString("authToken");
        loadChatList();
        ListView list = (ListView) v.findViewById(R.id.list);

        adp = new ChatAdapter();
        list.setAdapter(adp);


        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int pos,long arg3)
            {

                Bundle bundle = new Bundle();
                bundle.putString("contactusername", archivedChats.get(pos).getName());
                bundle.putString("contactphone", archivedChats.get(pos).getTitle());
                bundle.putString("authtoken", authToken);

                ChatItem item = (ChatItem) archivedChats.get(pos);
                if(item.isGroup()){
                    GroupChatUI groupChatFragment = new GroupChatUI();
                    bundle.putString("bList_id", archivedChats.get(pos).getTitle());
                    bundle.putString("authtoken", authToken);
                    groupChatFragment.setArguments(bundle);
                    getFragmentManager().beginTransaction()
                            .replace(R.id.content_frame, groupChatFragment, "groupChatFragmentTag")
                            .addToBackStack(archivedChats.get(pos).getName()).commit();
                }
                else if (!item.isGroup() && !item.isBroadCast()) {
                    GroupChat groupChatFragment = new GroupChat();
                    bundle.putString("contactusername", archivedChats.get(pos).getName());
                    bundle.putString("contactphone", archivedChats.get(pos).getTitle());
                    bundle.putString("authtoken", authToken);
                    groupChatFragment.setArguments(bundle);
                    getFragmentManager().beginTransaction()
                            .replace(R.id.content_frame, groupChatFragment, "groupChatFragmentTag")
                            .addToBackStack(archivedChats.get(pos).getName()).commit();


                } else if(item.isBroadCast()) {
                    BroadCastChat broadCastChatFragment = new BroadCastChat();
                    bundle.putString("list_name", archivedChats.get(pos).getName());
                    bundle.putString("bList_id", archivedChats.get(pos).getTitle());
                    bundle.putString("authtoken", authToken);
                    broadCastChatFragment.setArguments(bundle);
                    getFragmentManager().beginTransaction()
                            .replace(R.id.content_frame, broadCastChatFragment, "groupChatFragmentTag")
                            .addToBackStack(archivedChats.get(pos).getName()).commit();
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


            text = "this is archive screen";
            Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
            getActivity().getActionBar().setTitle("Archived");

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.findItem(R.id.archived).setVisible(false);
            menu.findItem(R.id.settings).setVisible(false);
            menu.findItem(R.id.connect_to_desktop).setVisible(false);
        }
        inflater.inflate(R.menu.newchat, menu);  // Use filter.xml from step 1
        getActivity().getActionBar().setSubtitle(null);
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.archived){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuinfo){
        super.onCreateContextMenu(menu, v, menuinfo);

        menu.setHeaderTitle(getString(R.string.common_select_action));
        menu.add(0, v.getId(), 0, "Unarchive");
    }


    public boolean onContextItemSelected(MenuItem item){

        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        Bundle bundle = new Bundle();
        try {
            if (item.getTitle() == "Unarchive") {

                DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
                ChatItem cItem = (ChatItem) archivedChats.get(info.position);
                if(cItem.isGroup()){
                    db.unArchiveGroup(cItem.getTitle());
                    Toast.makeText(getContext(),cItem.getTitle(),Toast.LENGTH_SHORT).show();
                    archivedChats.remove(info.position);
                    if(MainActivity.isVisible)
                        MainActivity.mainActivity.sendArchiveInfoToDesktop(cItem.getTitle(), "No");

                }else{
                    if(cItem.isBroadCast()){
                        db.unArchiveBroadcast(cItem.getTitle());
                        Toast.makeText(getContext(),cItem.getTitle(),Toast.LENGTH_SHORT).show();
                        archivedChats.remove(info.position);
                        if(MainActivity.isVisible)
                            MainActivity.mainActivity.sendArchiveInfoToDesktop(cItem.getTitle(), "No");

                    } else {
                        db.unArchive(cItem.getTitle());
                        Toast.makeText(getContext(), cItem.getTitle(), Toast.LENGTH_SHORT).show();
                        archivedChats.remove(info.position);
                        if (MainActivity.isVisible)
                            MainActivity.mainActivity.sendArchiveInfoToDesktop(cItem.getTitle(), "No");
                    }
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


    public void loadChatList()
    {
        DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
        try{

            ArrayList<ChatItem> chatList1 = new ArrayList<ChatItem>();



            JSONArray chats = db.getArchivedChatList();
//			JSONArray groups = db.getAllGroups();
            JSONArray groups = db.getMyArchivedGroups(db.getUserDetails().get("phone"));
            lists = db.getArchivedBroadCastChatList();
            for (int i=0; i < chats.length(); i++) {
                JSONObject row = chats.getJSONObject(i);


                    chatList1.add(new ChatItem(
                            row.getString("display_name"),
                            row.getString("contact_phone"),
                            row.getString("msg"),
                            Utility.convertDateToLocalTimeZoneAndReadable(row.getString("date")),
                            R.drawable.user1, false,
                            false, Integer.parseInt(row.getString("pendingMsgs")), "", false));


            }


            for (int i=0; i < groups.length(); i++) {
                JSONObject row = groups.getJSONObject(i);
                chatList1.add(new ChatItem(
                        row.getString("group_name"),
                        row.getString("unique_id"),
                        "Last Message",
                        row.getString("date_creation"),
                        R.drawable.user1, false,
                        true, 0, "", false));

            }

            for (int i=0; i < lists.length(); i++) {
                JSONObject row = lists.getJSONObject(i);

                //if (row.getInt("isArchived") == 0) {
                chatList1.add(new ChatItem(
                        row.getString("name"),
                        row.getString("unique_id"),
                        row.getString("msg"),
                        Utility.convertDateToLocalTimeZoneAndReadable(row.getString("date")),
                        R.drawable.user1, false,
                        false, 0, "", true).setProfileImage(null));
            }




            this.archivedChats = new ArrayList<ChatItem>(chatList1);
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


    public void setOnlineStatus(JSONArray contacts){

        for(int i=0; i<archivedChats.size(); i++)
            archivedChats.get(i).setOnline(false);

        try{
            for(int j=0; j<contacts.length(); j++)
                for(int i=0; i<archivedChats.size(); i++){
                    if(archivedChats.get(i).getName().equals(contacts.getJSONObject(j).getString("phone"))){
                        break;
                    }
                }


        } catch (JSONException e) {
            e.printStackTrace();
        } finally {

        }
    }





    /**
     * This is the ChatAdapter for Archived chat list
     */
    private class ChatAdapter extends BaseAdapter
    {

        /* (non-Javadoc)
         * @see android.widget.Adapter#getCount()
         */
        @Override
        public int getCount()
        {
            return archivedChats.size();
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getItem(int)
         */
        @Override
        public ChatItem getItem(int arg0)
        {
            return archivedChats.get(arg0);
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
        return "Archived Chat";
    }
    public String getFragmentContactPhone()
    {
        return "Archived Chat";
    }


}
