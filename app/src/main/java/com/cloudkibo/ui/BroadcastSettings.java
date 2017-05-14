package com.cloudkibo.ui;


import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.database.CloudKiboDatabaseContract;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.library.GroupUtility;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.utils.IFragmentName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class BroadcastSettings extends CustomFragment implements IFragmentName {

    private String authtoken;
    Context context;
    String bList_id;
    ListView lv;
    CharSequence [] contactList;
    String [] phoneList;
    JSONArray participants;
    LayoutInflater inflater;
    ImageButton btnSelectIcon;
    Button delete_list;
    Button submit;
    Button add_members;
    EditText newName;
    View view;
    String list_name;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        //// TODO: 5/10/17 Please define layout items for adding and deleting participants

        View vg = inflater.inflate(R.layout.broadcast_settings, null);
        lv=(ListView) vg.findViewById(R.id.listView);
        setHasOptionsMenu(true);
        this.inflater = inflater;
        authtoken = getActivity().getIntent().getExtras().getString("authtoken");
        Bundle args = getArguments();

        if (args  != null){
            bList_id = args.getString("bList_id");
            list_name = args.getString("list_name");
        }

        delete_list = (Button) vg.findViewById(R.id.delete_list);
        submit = (Button) vg.findViewById(R.id.newName);
        add_members = (Button) vg.findViewById(R.id.add_members);
        newName = (EditText) vg.findViewById(R.id.new_name);
        newName.setVisibility(View.GONE);
        submit.setVisibility(View.GONE);

        participants = getMembers();
        lv.setAdapter(new BroadCastParticipantAdapter(inflater, participants, getContext(),bList_id));

        delete_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 5/10/17 Add code to delete list
                Toast.makeText(getContext(), "Delete clicked", Toast.LENGTH_SHORT).show();
                deleteList(bList_id);
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(newName.getText().toString().equals("")) {
                    Toast.makeText(getContext(), "Please give some name", Toast.LENGTH_SHORT).show();
                }
                else{
                    DatabaseHandler db = new DatabaseHandler(getContext());
                    db.updateBroadCastListName(bList_id, newName.getText().toString());
                    gotoChatList();
                }
            }
        });

        add_members.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final CharSequence[] items = getAddtionalMembers();
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getString(R.string.group_utility_member_add));
                builder.setItems(items, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseHandler db = new DatabaseHandler(getContext());
                        db.addBroadCastListMember(bList_id, phoneList[which]);
                        lv.setAdapter(new CustomParticipantAdapter(inflater, getMembers(), getContext(),bList_id));
                        dialog.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        return vg;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.findItem(R.id.archived).setVisible(false);
            menu.findItem(R.id.search_chats).setVisible(false);
            menu.findItem(R.id.settings).setVisible(false);
            menu.findItem(R.id.connect_to_desktop).setVisible(false);
            menu.findItem(R.id.broadcast).setVisible(false);
        }
        inflater.inflate(R.menu.broadcastsetting, menu);
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setTitle(list_name.toUpperCase());
        actionBar.setDisplayShowCustomEnabled(false); // Use filter.xml from step 1
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.listNameChange){
            // TODO: 5/10/17 Add code to change list name
            Toast.makeText(getContext(), "Change name clicked", Toast.LENGTH_SHORT).show();
            newName.setVisibility(View.VISIBLE);
            submit.setVisibility(View.VISIBLE);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v)
    {
        super.onClick(v);
    }

    public void deleteList(String group_id){
        DatabaseHandler db = new DatabaseHandler(getContext());
        db.DeleteBroadCastList(bList_id);
        ChatList nextFrag= new ChatList();
//        Bundle args = new Bundle();
//        args.putString("bList_id", bList_id);
//        nextFrag.setArguments(args);
        this.getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, nextFrag,null)
                .addToBackStack(null)
                .commit();
    }

    public void gotoChatList(){
        ChatList nextFrag= new ChatList();
        this.getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, nextFrag,null)
                .addToBackStack(null)
                .commit();
    }

    public JSONArray getMembers(){

        String names[];
        DatabaseHandler db = new DatabaseHandler(getContext());

        try {
            participants = new JSONArray();
            participants = db.getBroadCastListMembers(bList_id);
            names = new String[participants.length()];
            for(int i = 0; i < participants.length(); i++)
            {
                if(!participants.getJSONObject(i).has("display_name")){
                    names[i] = "Anonymous";
                }else {
                    names[i] = participants.getJSONObject(i).getString("display_name");
                }
                if(participants.getJSONObject(i).getString("phone").toString().equals(db.getUserDetails().get("phone"))){
                    names[i] = db.getUserDetails().get("display_name");
                }
            }
            if(lv != null){
                BroadCastParticipantAdapter customParticipantAdapter = new BroadCastParticipantAdapter(inflater, participants,getContext(), bList_id);
                lv.setAdapter(customParticipantAdapter);
                customParticipantAdapter.notifyDataSetChanged();
            }
            return  participants;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }

    public CharSequence[] getAddtionalMembers(){


        DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());

        try {

            JSONArray jsonA = db.getMembersNotInBroadcast(bList_id);

            jsonA = UserFunctions.sortJSONArrayIgnoreCase(jsonA, "display_name");

            contactList = new String[jsonA.length()];
            phoneList  = new String[jsonA.length()];

            //This loop adds contacts to the display list which are on cloudkibo
            for (int i=0; i < jsonA.length(); i++) {
                JSONObject row = jsonA.getJSONObject(i);
                contactList[i] = row.getString("display_name");
                phoneList[i] = row.getString(CloudKiboDatabaseContract.Contacts.CONTACT_PHONE);
            }
            return contactList;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getFragmentName() {
        return "BroadcastSettings";
    }

    @Override
    public String getFragmentContactPhone() {
        return "About Chat";
    }

}
