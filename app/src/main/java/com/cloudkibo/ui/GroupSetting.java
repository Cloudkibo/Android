package com.cloudkibo.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
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

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */


public class GroupSetting extends CustomFragment implements IFragmentName
{


    private String authtoken;
    Context context;
    String group_id;
    ListView lv;
    CharSequence [] contactList;
    String [] phoneList;
    JSONArray participants;
    LayoutInflater inflater;
    ImageButton btnSelectIcon;

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.group_info, null);
        setHasOptionsMenu(true);
        this.inflater = inflater;
        authtoken = getActivity().getIntent().getExtras().getString("authtoken");
//        String names[] = getMembers();
//        Toast.makeText(getContext(), getMembers().length, Toast.LENGTH_LONG).show();
  //      Toast.makeText(getContext(), getMembers().toString(), Toast.LENGTH_LONG).show();
        setGroupInfo(v);
        Bundle args = getArguments();
        if (args  != null){
            group_id = args.getString("group_id");
        }
        Button leave_group = (Button) v.findViewById(R.id.leave_group);
        btnSelectIcon = (ImageButton) v.findViewById(R.id.selectIconBtn);
        Switch muteSwitch = (Switch) v.findViewById(R.id.switch1);
        try{
        muteSwitch.setChecked(new DatabaseHandler(getActivity().getApplicationContext()).isMute(group_id));
        } catch (JSONException e ){ e.printStackTrace();}
        lv=(ListView) v.findViewById(R.id.listView);
        participants = getMembers();
        lv.setAdapter(new CustomParticipantAdapter(inflater, participants, getContext(),group_id));
        LinearLayout add_members = (LinearLayout) v.findViewById(R.id.add_members);
        if(isAdmin(group_id)){
            add_members.setVisibility(View.VISIBLE);
        }
        add_members.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final CharSequence[] items = getAddtionalMembers();
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Add Members");
                builder.setItems(items, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Toast.makeText(getContext(), phoneList[which], Toast.LENGTH_LONG).show();
                        DatabaseHandler db = new DatabaseHandler(getContext());
                        db.addGroupMember(group_id,phoneList[which],0,"joined");
                        db = new DatabaseHandler(getContext());
                        db.addGroupMemberServerPending(group_id, phoneList[which]);
                        lv.setAdapter(new CustomParticipantAdapter(inflater, getMembers(), getContext(),group_id));
                        GroupUtility groupUtility = new GroupUtility(getContext());
                        String member_phone[] = new String[]{phoneList[which]};
                        try {
                            JSONObject info = db.getGroupInfo(group_id);
                            String group_name = info.getString("group_name");
//                            Toast.makeText(getContext(), "Add member: "+ groupUtility.getMemberData(group_name, group_id, member_phone).toString(), Toast.LENGTH_LONG).show();

                            groupUtility.addMemberOnServer(group_name,group_id,member_phone,authtoken);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        dialog.cancel();

                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        btnSelectIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity act1 = (MainActivity)getActivity();
                act1.uploadIcon(group_id);
            }
        });

        leave_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GroupUtility groupUtility = new GroupUtility(getContext());
                if(isAdmin(group_id) && groupUtility.adminCount(group_id) <= 1 ){
                    Toast.makeText(getContext(), "Please make someone else admin before you leave the group: " + groupUtility.adminCount(group_id), Toast.LENGTH_LONG ).show();
                }else {
//                    Toast.makeText(getContext(), "Left the group: " + groupUtility.adminCount(group_id), Toast.LENGTH_LONG ).show();
                    leaveGroup(group_id);
                    Toast.makeText(getContext(), "You left the group", Toast.LENGTH_LONG ).show();
                }
            }
        });

        muteSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
                    db.muteGroup(group_id);
                } else {
                    DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
                    db.unmuteGroup(group_id);
                }
            }
        });


        return v;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.findItem(R.id.archived).setVisible(false);
        }
        inflater.inflate(R.menu.newchat, menu);  // Use filter.xml from step 1
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.archived){
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


   public JSONArray getMembers(){

       Bundle args = getArguments();
       if (args  != null){
           group_id = args.getString("group_id");
       }

       String names[];
       DatabaseHandler db = new DatabaseHandler(getContext());

       try {
           participants = new JSONArray();
           participants = db.getGroupMembers(group_id);
//           participants.put(db.getMyDetailsInGroup(group_id));

//           Toast.makeText(getContext(), "Custom Members "+participants.toString(), Toast.LENGTH_LONG).show();
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
               CustomParticipantAdapter customParticipantAdapter = new CustomParticipantAdapter(inflater, participants,getContext(), group_id);
               lv.setAdapter(customParticipantAdapter);
               customParticipantAdapter.notifyDataSetChanged();
//               Toast.makeText(getContext(), "List Updated", Toast.LENGTH_LONG).show();
           }
           return  participants;
       } catch (JSONException e) {
           e.printStackTrace();
       }
        return new JSONArray();
   }


    public void setGroupInfo(View v){
        Bundle args = getArguments();
        if (args  != null){
            group_id = args.getString("group_id");
        }
       // Toast.makeText(getContext(), "Group In function", Toast.LENGTH_LONG).show();

        DatabaseHandler db = new DatabaseHandler(getContext());
        try {
            JSONObject group_info = db.getGroupInfo(group_id);
//            Toast.makeText(getContext(), "Group Name is: " + group_info.toString() + " and group id is: " + group_id, Toast.LENGTH_LONG).show();
            ((TextView)v.findViewById(R.id.group_name)).setText(group_info.getString("group_name"));
            ((TextView)v.findViewById(R.id.creation_date)).setText(group_info.getString("date_creation"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public CharSequence[] getAddtionalMembers(){


        DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());

        try {

            JSONArray jsonA = db.getMembersNotInGroup(group_id);

            jsonA = UserFunctions.sortJSONArray(jsonA, "display_name");

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

    public boolean isAdmin(String group_id){
        DatabaseHandler db = new DatabaseHandler(getContext());
        try {
            JSONObject details = db.getMyDetailsInGroup(group_id);
            if(details.getString("isAdmin").equals("1")){
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void leaveGroup(String group_id){
        GroupUtility groupUtility = new GroupUtility(getContext());
        DatabaseHandler db = new DatabaseHandler(getContext());
        groupUtility.leaveGroup(group_id, db.getUserDetails().get("phone"), authtoken);
        ChatList nextFrag= new ChatList();
//        Bundle args = new Bundle();
//        args.putString("group_id", group_id);
//        nextFrag.setArguments(args);
        this.getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, nextFrag,null)
                .addToBackStack(null)
                .commit();
    }

    public String getFragmentName()
    {
        return "GroupSetting";
    }

    public String getFragmentContactPhone()
    {
        return "About Chat";
    }
}
