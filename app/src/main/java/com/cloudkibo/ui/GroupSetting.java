package com.cloudkibo.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.PersistableBundle;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.cloudkibo.file.filechooser.utils.FileUtils;
import com.cloudkibo.library.CircleTransform;
import com.cloudkibo.library.GroupUtility;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.utils.IFragmentName;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
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
    Button leave_group;
    View view;
    Switch muteSwitch;

    private static int kJobId = 0;
    String mute_selected_option = "Never";
    String [] mute_options = new String[]{"Never", "Hourly", "Daily", "Weekly", "Monthly"};
    final static int MINUTELY = 60 * 1000;
    final static int HOURLY = 60 * MINUTELY;
    final static int DAILY = 24 * HOURLY;
    final static int WEEKLY = 7 * DAILY;
    final static int MONTHLY = 4 * WEEKLY;

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
            group_id = args.getString("bList_id");
        }
        leave_group = (Button) footer.findViewById(R.id.leave_group);
  //      btnSelectIcon = (ImageButton) v.findViewById(R.id.selectIconBtn);
        GroupUtility groupUtility = new GroupUtility(getContext());

        muteSwitch = (Switch) v.findViewById(R.id.switch1);
        if(!groupUtility.isMember(group_id)){
            leave_group.setVisibility(View.INVISIBLE);
            muteSwitch.setVisibility(View.INVISIBLE);
        }
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
//                act1.uploadIcon(bList_id);
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
                    showMuteDialog();
                } else {
                    DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
                    db.unmuteGroup(group_id);
                }
            }
        });


        return vg;
    }


    private void showMuteDialog() {

        // custom dialog
        final Dialog dialog = new Dialog(getContext());
        dialog.setTitle("Mute Contact");
        dialog.setContentView(R.layout.drive_backup_dialog);

        List<String> stringList=new ArrayList<>();  // here is list
        for (int i=0; i<mute_options.length; i++){
            stringList.add(mute_options[i]);
        }

        final RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.radio_group);
        Button cancel = (Button) dialog.findViewById(R.id.cancel_drive_backup_dialog);
        for(int i=0;i<stringList.size();i++){
            RadioButton rb=new RadioButton(MainActivity.mainActivity); // dynamically creating RadioButton and adding to RadioGroup.
            rb.setText(stringList.get(i));
            rb.setPadding(20,20,20,20);
            rg.addView(rb);
            if(stringList.get(i).equals(mute_selected_option)){
                rb.setChecked(true);
            }

        }
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                RadioButton radioButton = (RadioButton) rg.findViewById(checkedId);
                if(!mute_selected_option.equals(radioButton.getText().toString())) {
                    String newOption = radioButton.getText().toString();
                    Toast.makeText(getContext(), "Muted", Toast.LENGTH_SHORT).show();
                    DatabaseHandler db = new DatabaseHandler(getContext());
                    db.muteGroup(group_id);
                    startJobServiceForOneTimeOnly(newOption);
                }
                dialog.cancel();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                muteSwitch.setChecked(false);
            }
        });

        dialog.show();

    }


    public void startJobServiceForOneTimeOnly(String period){
        ComponentName mServiceComponent = new ComponentName(getContext(), MuteSchedulerService.class);
        JobInfo.Builder builder = new JobInfo.Builder(kJobId++, mServiceComponent);
        //builder.setMinimumLatency(60 * 1000); // wait at least
        if(period.equals("Daily")){
            builder.setMinimumLatency(10*1000); // maximum delay
        } else if(period.equals("Hourly")){
            builder.setMinimumLatency(HOURLY); // maximum delay
        } else if(period.equals("Weekly")){
            builder.setMinimumLatency(WEEKLY); // maximum delay
        } else if(period.equals("Monthly")){
            builder.setMinimumLatency(MONTHLY); // maximum delay
        }


        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // todo change according to selected value - require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        builder.setRequiresCharging(false);// we don't care if the device is charging or not
        PersistableBundle bundle = new PersistableBundle();
        bundle.putString("groupid", group_id);
        bundle.putString("chatType", "groupchat");
        builder.setExtras(bundle);
        JobScheduler jobScheduler = (JobScheduler) getContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }





    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.findItem(R.id.archived).setVisible(false);
            menu.findItem(R.id.search_chats).setVisible(false);
            menu.findItem(R.id.settings).setVisible(false);
            menu.findItem(R.id.connect_to_desktop).setVisible(false);
        }
        inflater.inflate(R.menu.groupsetting, menu);  // Use filter.xml from step 1
        GroupUtility groupUtility = new GroupUtility(getContext());
        if(!groupUtility.isMember(group_id)){
            menu.findItem(R.id.settingMenu).setVisible(false);
            menu.findItem(R.id.grpNameChange).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        GroupUtility groupUtility = new GroupUtility(getContext());
        if(id == R.id.settingMenu){
            if(!groupUtility.isMember(group_id)){
                return false;
            }
            MainActivity act1 = (MainActivity)getActivity();
            act1.uploadIcon(group_id);

            return true;
        }
        if(id == R.id.grpNameChange){
            ChangeGroupName cGNameFrag = new ChangeGroupName();
            Bundle bundle = new Bundle();
            bundle.putString("authtoken", authtoken);
            bundle.putString("groupid", group_id);

            cGNameFrag.setArguments(bundle);
            getFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, cGNameFrag, "changeGroupNameTag").addToBackStack("GroupNameChange")
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


   public JSONArray getMembers(){

       Bundle args = getArguments();
       if (args  != null){
           group_id = args.getString("bList_id");
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
           GroupUtility groupUtility = new GroupUtility(getContext());
           if(!groupUtility.isMember(group_id)){
               leave_group.setVisibility(View.INVISIBLE);
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
            group_id = args.getString("bList_id");
        }

        DatabaseHandler db = new DatabaseHandler(getContext());
        try {
            JSONObject group_info = db.getGroupInfo(group_id);
//            Toast.makeText(getContext(), "Group Name is: " + group_info.toString() + " and group id is: " + bList_id, Toast.LENGTH_LONG).show();
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
//        args.putString("bList_id", bList_id);
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
