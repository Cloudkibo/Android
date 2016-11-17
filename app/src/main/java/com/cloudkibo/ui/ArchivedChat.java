package com.cloudkibo.ui;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.model.ChatItem;
import com.cloudkibo.utils.IFragmentName;

import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;


public class ArchivedChat extends CustomFragment implements IFragmentName {

    private String text;
    private ArrayList<ChatItem> archivedChats;
    private ChatAdapter adp;
    private String authToken;




    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.archived_chat, null);


        Bundle args = getArguments();
        authToken = args.getString("authToken");
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
                    bundle.putString("group_id", archivedChats.get(pos).getTitle());
                    bundle.putString("authtoken", authToken);
                    groupChatFragment.setArguments(bundle);
                    getFragmentManager().beginTransaction()
                            .replace(R.id.content_frame, groupChatFragment, "groupChatFragmentTag")
                            .addToBackStack(archivedChats.get(pos).getName()).commit();
                }
                else{
                    GroupChat groupChatFragment = new GroupChat();
                    bundle.putString("contactusername", archivedChats.get(pos).getName());
                    bundle.putString("contactphone", archivedChats.get(pos).getTitle());
                    bundle.putString("authtoken", authToken);
                    groupChatFragment.setArguments(bundle);
                    getFragmentManager().beginTransaction()
                            .replace(R.id.content_frame, groupChatFragment, "groupChatFragmentTag")
                            .addToBackStack(archivedChats.get(pos).getName()).commit();


                }

            }
        });
        adp.notifyDataSetChanged();


            text = "this is archive screen";
            Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
            getActivity().getActionBar().setTitle("Archived");

        return v;
    }




    public void getData(ArrayList<ChatItem> archivedChats){
        this.archivedChats = archivedChats;
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
            // TODO Auto-generated catch block
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
