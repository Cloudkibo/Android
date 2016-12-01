package com.cloudkibo.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudkibo.MainActivity;
import com.cloudkibo.NewChat;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomContactAdapter;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.database.CloudKiboDatabaseContract;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.library.GroupUtility;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.library.Utility;
import com.cloudkibo.model.ChatItem;
import com.cloudkibo.model.ContactItem;
import com.cloudkibo.model.Conversation;
import com.cloudkibo.utils.IFragmentName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */


public class GroupChatUI extends CustomFragment implements IFragmentName
{


    private String authtoken;
    private ListView lv;
    private ArrayList<String> messages = new ArrayList<String>();
    private ArrayList<String> names = new ArrayList<String>();
    private String group_id="";
    private GroupChatAdapter groupAdapter;
    private ArrayList<Conversation> convList = new ArrayList<Conversation>();
    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.group_chat, null);
        setHasOptionsMenu(true);
        authtoken = getActivity().getIntent().getExtras().getString("authtoken");
        final GroupChatUI temp = this;
//        LinearLayout group_header = (LinearLayout) v.findViewById(R.id.group_header);
//        group_header.setVisibility(View.VISIBLE);
//        Button settings = (Button) v.findViewById(R.id.setting);
        Bundle args = getArguments();
        if (args  != null){
            group_id = args.getString("group_id");
            Toast.makeText(getContext(), group_id, Toast.LENGTH_LONG).show();
        }
        final EditText my_message = (EditText) v.findViewById(R.id.txt);
        lv=(ListView) v.findViewById(R.id.list);
        populateMessages();
        groupAdapter = new GroupChatAdapter(inflater, messages,names, convList);
        lv.setAdapter(groupAdapter);
        lv.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        lv.setStackFromBottom(true);

        Button send_button = (Button) v.findViewById(R.id.btnSend);
        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(my_message);
            }
        });

//        settings.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                GroupSetting nextFrag= new GroupSetting();
//                Bundle args = new Bundle();
//                args.putString("group_id", group_id);
//                nextFrag.setArguments(args);
//                temp.getFragmentManager().beginTransaction()
//                        .replace(R.id.content_frame, nextFrag,null)
//                        .addToBackStack("ChatList")
//                        .commit();
//            }
//        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                return false;
            }
        });
        registerForContextMenu(lv);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.findItem(R.id.archived).setVisible(false);
        }
        inflater.inflate(R.menu.groupchat, menu);  // Use filter.xml from step 1
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.settingMenu){
            final GroupChatUI temp = this;
            GroupSetting nextFrag= new GroupSetting();
            Bundle args = new Bundle();
            args.putString("group_id", group_id);
            nextFrag.setArguments(args);
            temp.getFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, nextFrag,null)
                    .addToBackStack("ChatList")
                    .commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* (non-Javadoc)
     * @see com.socialshare.custom.CustomFragment#onClick(android.view.View)
     */


    @Override
    public void onClick(View v)
    {
        super.onClick(v);
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuinfo){
        super.onCreateContextMenu(menu, v, menuinfo);

        menu.setHeaderTitle("Select the Action");
        menu.add(0, v.getId(), 0, "Message Info");
    }


    public boolean onContextItemSelected(MenuItem item){

        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        if(item.getTitle() == "Message Info"){

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


    public void sendMessage(EditText my_message){
        String message = my_message.getText().toString();
        if(message.trim().equals("")){
           return;
        }
        DatabaseHandler db = new DatabaseHandler(getContext());
        GroupUtility groupUtility = new GroupUtility(getContext());
        String msg_unique_id = groupUtility.sendGroupMessage(group_id, message, authtoken);
        messages.add(message);
        names.add("");
        try {
            convList.add(new Conversation(message, db.getUserDetails().get("phone"), true,"", msg_unique_id, db.getGroupMessageStatus(msg_unique_id), "chat"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        groupAdapter.notifyDataSetChanged();
        my_message.setText("");
    }

    public void populateMessages(){
        DatabaseHandler db = new DatabaseHandler(getContext());
        messages.clear();
        names.clear();
        convList.clear();
        try {
            JSONArray msgs = db.getGroupMessages(group_id);
            Toast.makeText(getContext(), "In Messages: " + msgs.length(), Toast.LENGTH_LONG).show();
            for(int i = 0; i < msgs.length(); i++){
                messages.add(msgs.getJSONObject(i).get("msg").toString());
                names.add(msgs.getJSONObject(i).get("from_fullname").toString());
                String message = msgs.getJSONObject(i).get("msg").toString();
                String from = msgs.getJSONObject(i).get("from").toString();
                String date = msgs.getJSONObject(i).get("date").toString();
                String unique_id = msgs.getJSONObject(i).get("unique_id").toString();
                String type = msgs.getJSONObject(i).get("type").toString();
                boolean isSent = false;
                if(db.getUserDetails().get("phone").equals(from)){
                    isSent = true;
                    from = "You";
                }
                String display_name = db.getDisplayName(from);
                if(!display_name.equals("")){
                    from = display_name;
                }
                convList.add(new Conversation(message, from, isSent, date, unique_id, db.getGroupMessageStatus(unique_id), type));
            }

            if(groupAdapter != null && lv != null){
                groupAdapter.notifyDataSetChanged();
//                lv.smoothScrollToPosition(lv.getCount()+1);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }





    public String getFragmentName()
    {
        return "GroupChatUI";
    }

    public String getGroupId()
    {
        return this.group_id;
    }

    public String getFragmentContactPhone()
    {
        return "Group Chat";
    }
}
