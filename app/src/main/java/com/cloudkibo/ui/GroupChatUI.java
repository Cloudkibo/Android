package com.cloudkibo.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
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
    private String group_name="";
    private GroupChatAdapter groupAdapter;
    private ArrayList<Conversation> convList = new ArrayList<Conversation>();

    static final int PICK_CONTACT=1;
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
            group_name = args.getString("group_name");
        }

        final EditText my_message = (EditText) v.findViewById(R.id.txt);
        GroupUtility groupUtility = new GroupUtility(getContext());
        if(!groupUtility.isGroupMember(group_id)){
            my_message.setEnabled(false);
            my_message.setHint("Chat Disabled");
        }
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

        getActivity().getActionBar().setSubtitle(Html.fromHtml("<font color='#ffffff'>"+getMembersName(group_id)+"</font>"));

        return v;
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.findItem(R.id.archived).setVisible(false);
            menu.findItem(R.id.language).setVisible(false);
            menu.findItem(R.id.backup_setting).setVisible(false);
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
                    .addToBackStack(group_name)
                    .commit();
            return true;
        }
        if(id == R.id.sendContactMenu){
            Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            getActivity().startActivityForResult(contactPickerIntent, 0123);
        }

        return super.onOptionsItemSelected(item);
    }

    /* (non-Javadoc)
     * @see com.socialshare.custom.CustomFragment#onClick(android.view.View)
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check whether the result is ok

        if (resultCode == Activity.RESULT_OK) {
            // Check for the request code, we might be usign multiple startActivityForReslut
            switch (requestCode) {
                case 0123:
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
                default:
                    Toast.makeText(getContext(), "Could not get the contact you selected", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.e("MainActivity", "Failed to pick contact");
        }

//        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onClick(View v)
    {
        super.onClick(v);
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuinfo){
        super.onCreateContextMenu(menu, v, menuinfo);

        menu.setHeaderTitle(getString(R.string.common_select_action));
        menu.add(0, v.getId(), 0, getString(R.string.common_message_info));
        menu.add(0, v.getId(), 0, getString(R.string.common_remove_message));
    }


    public boolean onContextItemSelected(MenuItem item){

        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        if(item.getTitle() == getString(R.string.common_message_info)){

            GroupMessageInfo mInfoFrag = new GroupMessageInfo();
            Bundle bundle = new Bundle();

            bundle.putString("authtoken",authtoken);
            bundle.putString("message",convList.get(info.position).getMsg());
            bundle.putString("group_id", group_id);
            bundle.putString("message_id",convList.get(info.position).getUniqueid());

            mInfoFrag.setArguments(bundle);
            getFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, mInfoFrag, "messageInfoFragmentTag")
                    .addToBackStack("GroupMessageInfo")
                    .commit();
        }
        if(item.getTitle() == getString(R.string.common_remove_message)){
            DatabaseHandler db = new DatabaseHandler(getContext());
            db.deleteGroupChatMessage(convList.get(info.position).getUniqueid());
            populateMessages();
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
        String msg_unique_id = groupUtility.sendGroupMessage(group_id, message, authtoken, "chat");
        messages.add(message);
        names.add("");

        try {
            convList.add(new Conversation(message, db.getUserDetails().get("phone"), true,"", msg_unique_id, db.getGroupMessageStatus(msg_unique_id, db.getUserDetails().get("phone")).getJSONObject(0).getString("status"), "chat"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        groupAdapter.notifyDataSetChanged();
        my_message.setText("");
    }

    public void sendContact(String display_name, String phone, String contact_image){
        String message = display_name + ":" + phone;
        DatabaseHandler db = new DatabaseHandler(getContext());
        GroupUtility groupUtility = new GroupUtility(getContext());
        String msg_unique_id = groupUtility.sendGroupMessage(group_id, message, authtoken, "contact");
        messages.add(message);
        names.add("");

        try {
            convList.add(new Conversation(message, db.getUserDetails().get("phone"), true,"", msg_unique_id, db.getGroupMessageStatus(msg_unique_id, db.getUserDetails().get("phone")).getJSONObject(0).getString("status"), "contact").setContact_image(contact_image));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        groupAdapter.notifyDataSetChanged();
    }

    public void populateMessages(){
        DatabaseHandler db = new DatabaseHandler(getContext());
        messages.clear();
        names.clear();
        convList.clear();
        try {
            JSONArray msgs = db.getGroupMessages(group_id);
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

                JSONArray msgStatus = db.getGroupMessageStatusSeen(unique_id);
                String status = "";
//                if(msgStatus.length() != 0){
//                    status = msgStatus.getJSONObject(0).getString("status");
//                }

                if(msgStatus.length() >= db.getGroupMembers(group_id).length() - 1){
                    status = "seen";
                }
                else if(db.getGroupMessageStatusDelivered(unique_id).length() >= db.getGroupMembers(group_id).length() - 1){
                    status = "delivered";
                }
                else{
                    status = "sent";
//                    String temp = db.getGroupMessageStatus(unique_id, db.getUserDetails().get("phone")).getJSONObject(0).getString("status");
//                    Toast.makeText(getContext(), temp, Toast.LENGTH_LONG).show();
                }

                convList.add(new Conversation(message, from, isSent, date, unique_id, status, type));

            }

            if(groupAdapter != null && lv != null){
                groupAdapter.notifyDataSetChanged();
//                lv.smoothScrollToPosition(lv.getCount()+1);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }




    public String getMembersName(String group_id){

        String members = "";

        DatabaseHandler db = new DatabaseHandler(getContext());

        try {
            JSONArray participants = new JSONArray();
            participants = db.getGroupMembers(group_id);
            for(int i = 0; i < participants.length(); i++)
            {
                if(!participants.getJSONObject(i).has("display_name")){
                    if(participants.getJSONObject(i).getString("phone").toString().equals(db.getUserDetails().get("phone"))){
                        members = members + db.getUserDetails().get("display_name") + ",";
                    }else{
                        members = members + participants.getJSONObject(i).getString("phone") + ",";
                    }
                }else {
                    members = members + participants.getJSONObject(i).getString("display_name") + ",";
                }
            }

            return  members;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return members;
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
