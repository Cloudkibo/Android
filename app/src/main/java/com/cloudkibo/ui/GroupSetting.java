package com.cloudkibo.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.database.CloudKiboDatabaseContract;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.library.CircleTransform;
import com.cloudkibo.library.GroupUtility;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.utils.IFragmentName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.UUID;

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
    View view;

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        ViewGroup v = (ViewGroup)inflater.inflate(R.layout.group_setting_header, lv, false);
        ViewGroup footer = (ViewGroup)inflater.inflate(R.layout.group_setting_footer, lv, false);
        View vg = inflater.inflate(R.layout.group_setting, null);
        setHasOptionsMenu(true);
        this.view = v;
        this.inflater = inflater;
        authtoken = getActivity().getIntent().getExtras().getString("authtoken");
        setGroupInfo(v);
        loadDisplayImage();
        Bundle args = getArguments();
        if (args  != null){
            group_id = args.getString("group_id");
        }
        Button leave_group = (Button) footer.findViewById(R.id.leave_group);
  //      btnSelectIcon = (ImageButton) v.findViewById(R.id.selectIconBtn);
        Switch muteSwitch = (Switch) v.findViewById(R.id.switch1);
        try{
        muteSwitch.setChecked(new DatabaseHandler(getActivity().getApplicationContext()).isMute(group_id));
        } catch (JSONException e ){ e.printStackTrace();}
        lv=(ListView) vg.findViewById(R.id.listView);

        lv.addHeaderView(v, null, false);
        lv.addFooterView(footer, null, false);
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
                builder.setTitle(getString(R.string.group_utility_member_add));
                builder.setItems(items, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        DatabaseHandler db = new DatabaseHandler(getContext());
                        try {
                            JSONObject group_member = db.getGroupMemberDetail(group_id, phoneList[which]);
                            if(group_member.getString("phone").equals(phoneList[which])){
                                db.updateGroupMembershipStatus(group_id,phoneList[which], "joined");
                            }else {
                                db.addGroupMember(group_id,phoneList[which],"0","joined");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        db.addGroupMemberServerPending(group_id, phoneList[which]);
                        lv.setAdapter(new CustomParticipantAdapter(inflater, getMembers(), getContext(),group_id));
                        GroupUtility groupUtility = new GroupUtility(getContext());
                        String member_phone[] = new String[]{phoneList[which]};
                        try {
                            JSONObject info = db.getGroupInfo(group_id);
                            String group_name = info.getString("group_name");
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

//        btnSelectIcon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                MainActivity act1 = (MainActivity)getActivity();
//                act1.uploadIcon(group_id);
//            }
//        });

        leave_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GroupUtility groupUtility = new GroupUtility(getContext());
                if(isAdmin(group_id) && groupUtility.adminCount(group_id) <= 1){
                    Toast.makeText(getContext(), getString(R.string.group_utility_member_leave_admin_prompt) +": " + groupUtility.adminCount(group_id), Toast.LENGTH_LONG ).show();
                }else {
                    leaveGroup(group_id);
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


        return vg;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.findItem(R.id.archived).setVisible(false);
            menu.findItem(R.id.language).setVisible(false);
        }
        inflater.inflate(R.menu.groupsetting, menu);  // Use filter.xml from step 1
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.settingMenu){
            MainActivity act1 = (MainActivity)getActivity();
            act1.uploadIcon(group_id);
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

        DatabaseHandler db = new DatabaseHandler(getContext());
        try {
            JSONObject group_info = db.getGroupInfo(group_id);
//            Toast.makeText(getContext(), "Group Name is: " + group_info.toString() + " and group id is: " + group_id, Toast.LENGTH_LONG).show();
//            ((TextView)v.findViewById(R.id.group_name)).setText(group_info.getString("group_name"));
//            ((TextView)v.findViewById(R.id.creation_date)).setText(group_info.getString("date_creation"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void loadDisplayImage(){
        ImageView dp = (ImageView) view.findViewById(R.id.display_ic);

        Glide
                .with(this)
                .load(getActivity().getApplicationContext().getFilesDir()+"/"+group_id)
                .signature(new StringSignature(UUID.randomUUID().toString()))
                .thumbnail(0.1f)
                .centerCrop()
                .transform(new CircleTransform(context))
                .placeholder(R.drawable.avatar)
                .into(dp);

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
