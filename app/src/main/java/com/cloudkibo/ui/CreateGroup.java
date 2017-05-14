package com.cloudkibo.ui;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cloudkibo.MainActivity;
import com.cloudkibo.NewChat;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.library.CircleTransform;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.library.Utility;
import com.cloudkibo.model.ChatItem;
import com.cloudkibo.model.ContactItem;
import com.cloudkibo.utils.IFragmentName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.zip.Inflater;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */


public class CreateGroup extends CustomFragment implements IFragmentName
{


    private String authtoken;
    private DatabaseHandler db;
    public static ArrayList<ContactItem> contactList;
    private ContactAdapter contactAdapter;
    CreateGroup  reference = this;
    HorizontalViewAdapter adapter;
    String group_id;
    String group_name;
    String unique_id;
    Context context = getContext();

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.create_group_whatsapp, null);
        LinearLayout innerLay = (LinearLayout) v.findViewById(R.id.innerLay);
        setHasOptionsMenu(true);
        db = new DatabaseHandler(getContext());
//
        authtoken = getActivity().getIntent().getExtras().getString("authtoken");
        if(contactList == null){
            contactList = new ArrayList<ContactItem>();

        }
        Bundle args = getArguments();

        if (args  != null){
            group_id = args.getString("bList_id");
            group_name = args.getString("group_name");
            unique_id = args.getString("unique_id");
            Toast.makeText(getContext(), group_id, Toast.LENGTH_LONG).show();

        }

        adapter = new HorizontalViewAdapter(innerLay);



        ListView list = (ListView) v.findViewById(R.id.kibo_contact);
        contactAdapter = new ContactAdapter();
        loadContactList();
        list.setAdapter(contactAdapter);

        FloatingActionButton next = (FloatingActionButton) v.findViewById(R.id.fab);
        final CreateGroup temp = this;
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //String bList_id = randomString(10);
                //Toast.makeText(getContext(), "Group Name: " + group_name.getText().toString(), Toast.LENGTH_LONG).show();
                //db.createGroup(bList_id, group_name.getText().toString(), 0);

                if(adapter.getPhones().size() <= 0){
                    Toast.makeText(getContext(), "Please select atleast one group member", Toast.LENGTH_LONG).show();
                    return;
                }
                group_id = randomString(10);
                Toast.makeText(getContext(), "Group Name: " + group_name, Toast.LENGTH_LONG).show();
                db.createGroup(group_id, group_name, 0);

                String message = "You created group "+ group_name;
                String member_name = db.getUserDetails().get("display_name");
                String member_phone = db.getUserDetails().get("phone");
                String uniqueid = Long.toHexString(Double.doubleToLongBits(Math.random()));
                uniqueid += (new Date().getYear()) + "" + (new Date().getMonth()) + "" + (new Date().getDay());
                uniqueid += (new Date().getHours()) + "" + (new Date().getMinutes()) + "" + (new Date().getSeconds());
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                db.addGroupMessage(group_id,message,member_phone,member_name,uniqueid, "log");
                addMembers(adapter.getPhones());
                createGroupOnServer(group_name, group_id, adapter.getPhones(), authtoken);
                MainActivity act1 = (MainActivity)getActivity();
                act1.uploadIcon(group_id);
                GroupChatUI nextFrag= new GroupChatUI();
                Bundle args = new Bundle();
                args.putString("bList_id", group_id);
                args.putString("group_name", group_name);
                nextFrag.setArguments(args);
                temp.getFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, nextFrag,null)
//                        .addToBackStack(group_name)
                        .commit();
            }
        });


//
//        Button create_group = (Button) v.findViewById(R.id.create_group);
//        final EditText group_name = (EditText) v.findViewById(R.id.group_name);
//        final CreateGroup temp = this;
//        create_group.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String bList_id = randomString(10);
//                Toast.makeText(getContext(), "Group Name: " + group_name.getText().toString(), Toast.LENGTH_LONG).show();
//                db.createGroup(bList_id, group_name.getText().toString(), 0);
//                AddMembers nextFrag= new AddMembers();
//                Bundle args = new Bundle();
//                args.putString("bList_id", bList_id);
//                args.putString("group_name", group_name.getText().toString());
//                nextFrag.setArguments(args);
//                temp.getFragmentManager().beginTransaction()
//                        .replace(R.id.content_frame, nextFrag,null)
//                        .addToBackStack(null)
//                        .commit();
//            }
//        });

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.findItem(R.id.archived).setVisible(false);
            menu.findItem(R.id.search_chats).setVisible(false);
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

    /* (non-Javadoc)
     * @see com.socialshare.custom.CustomFragment#onClick(android.view.View)
     */


    @Override
    public void onClick(View v)
    {
        super.onClick(v);
    }




    public String getFragmentName()
    {
        return "Create Group";
    }

    public String getFragmentContactPhone()
    {
        return "About Chat";
    }

    public void addMembers(ArrayList<String> phones){

        for (int i = 0; i< phones.size(); i++){
            db.addGroupMember(group_id,phones.get(i),"0","joined");
            Toast.makeText(getContext(), phones.get(i) + "Added", Toast.LENGTH_SHORT).show();
        }
        db.addGroupMember(group_id,db.getUserDetails().get("phone"),"1","joined");
        Toast.makeText(getContext(),db.getUserDetails().get("phone") + " added as admin", Toast.LENGTH_SHORT).show();
        if(MainActivity.isVisible) {
            MainActivity.mainActivity.refreshGroupsOnDesktop();
        }
    }

    String randomString(final int length) {
        String uniqueid = Long.toHexString(Double.doubleToLongBits(Math.random()));
        uniqueid += (new Date().getYear()) + "" + (new Date().getMonth()) + "" + (new Date().getDay());
        uniqueid += (new Date().getHours()) + "" + (new Date().getMinutes()) + "" + (new Date().getSeconds());
        return uniqueid;
    }

    public void loadContactList()
    {

        //ArrayList<ContactItem> noteList = new ArrayList<ContactItem>();
        //contactList = new ArrayList<ContactItem>(noteList);
        final DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
        //contact_phone.clear();

        try {
            JSONArray jsonA = db.getContactsWithImages();
            ArrayList<ContactItem> contactList1 = new ArrayList<ContactItem>();


            jsonA = UserFunctions.sortJSONArrayIgnoreCase(jsonA, "display_name");
//
            String my_btmp;
            //This loop adds contacts to the display list which are on cloudkibo

            for (int i=0; i < jsonA.length(); i++) {
                JSONObject row = jsonA.getJSONObject(i);
                my_btmp = row.optString("image_uri");

                contactList1.add(new ContactItem(row.getString("_id"),
                        row.getString("display_name"),
                        "", // first name
                        row.getString("on_cloudkibo"),
                        row.getString("phone"),
                        01,
                        false, "",
                        row.getString("status"),
                        row.getString("detailsshared"),
                        false
                ).setProfile(my_btmp));
                //	contact_phone.add(row.getString("phone"));
            }

            contactList.clear();
            contactList.addAll(contactList1);
            contactAdapter.notifyDataSetChanged();
        } catch (JSONException e) {

            e.printStackTrace();

        }

    }


    private class HorizontalViewAdapter{
        HashMap<String, ViewGroup> selected_contacts  = new HashMap<String, ViewGroup>();

        LayoutInflater inflater = LayoutInflater.from(getContext());
        LinearLayout innerLay;

        public HorizontalViewAdapter(LinearLayout innerLay){

            this.innerLay = innerLay;
        }

        public ArrayList<String> getPhones(){
            ArrayList<String> phones  = new ArrayList<String>();
            for (HashMap.Entry<String, ViewGroup> entry : selected_contacts.entrySet())
            {
                phones.add(entry.getKey());
            }
            return phones;
        }

        public void addContacts(String phone){
            ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.added_contact, null);
            selected_contacts.put(phone, vg);
        }

        public boolean hasContact(String phone){
           return selected_contacts.containsKey(phone);
        }

        public void addAndApply(String phone, String name, String photo_uri){
            ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.added_contact, null);
            ((TextView) vg.findViewById(R.id.name)).setText(name);
            selected_contacts.put(phone, vg);
            if(selected_contacts.get(phone).getParent()!=null)
                ((ViewGroup)selected_contacts.get(phone).getParent()).removeView(selected_contacts.get(phone));
            if (photo_uri != null) {
                Glide
                        .with(reference)
                        .load(photo_uri)
                        .thumbnail(0.1f)
                        .centerCrop()
                        .transform(new CircleTransform(getContext()))
                        .placeholder(R.drawable.avatar)
                        .into((ImageView)vg.findViewById(R.id.profile));

            }else{
                ((ImageView)vg.findViewById(R.id.profile)).setImageResource(R.drawable.avatar);
            }
            innerLay.addView(selected_contacts.get(phone));
        }

        public ViewGroup getContact(String phone){
            return selected_contacts.get(phone);
        }

        public void removeContact(String phone){
                ((ViewGroup) selected_contacts.get(phone).getParent()).removeView(selected_contacts.get(phone));
                selected_contacts.remove(phone);

        }

        public void notifyDataSetChanged(){
            for (HashMap.Entry<String, ViewGroup> entry : selected_contacts.entrySet())
            {
                if(entry.getValue().getParent()!=null)
                    ((ViewGroup)entry.getValue().getParent()).removeView(entry.getValue());
                innerLay.addView(entry.getValue());
            }
        }


    }


    public JSONObject getGroupCreationData(String group_name, String group_id, ArrayList<String> selected_contacts){
        JSONObject groupPost = new JSONObject();
        JSONObject body = new JSONObject();
        try {
            body.put("group_name", group_name);
            body.put("unique_id",  group_id);
            body.put("members",  new JSONArray(selected_contacts));
            groupPost.put("body", body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return body;
    }

    public void createGroupOnServer(final String group_name, final String group_id, final ArrayList<String> selected_contacts, final String authtoken){



        new AsyncTask<String, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(String... args) {
                UserFunctions userFunctions = new UserFunctions(getActivity().getApplicationContext());
                try {
                    db.createGroupServerPending(group_id, group_name, getGroupCreationData(group_name, group_id, selected_contacts).getJSONArray("members").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return userFunctions.sendCreateGroupToServer(getGroupCreationData(group_name, group_id, selected_contacts), authtoken);
            }

            @Override
            protected void onPostExecute(JSONObject row) {

                if(row != null){
                    if(row.has("Error")){
                        Log.d("Add Members", "No Internet. Group information saved in pending groups table.");
                    } else {
                        try {
                            db.deleteGroupServerPending(row.getString("unique_id"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(MainActivity.mainActivity, row.toString(), Toast.LENGTH_LONG).show();
                        Toast.makeText(MainActivity.mainActivity,"New Group Created Successfully", Toast.LENGTH_LONG).show();
                    }
//                    Toast.makeText(getContext(), "Group Successfully Created On Server", Toast.LENGTH_LONG).show();
                }
            }

        }.execute();

    }

    static class ViewHolderItem {
        ImageView profile;
        TextView lbl;
        TextView lbl2;
        TextView lbl3;
        ImageView img2;
        ImageView img3;
        TextView invite;
        LinearLayout parent;
        boolean selected = false;
        String photo_uri;
    }

    private class ContactAdapter extends BaseAdapter
    {

        /* (non-Javadoc)
         * @see android.widget.Adapter#getCount()
         */
        @Override
        public int getCount()
        {
            return contactList.size();
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getItem(int)
         */
        @Override
        public ContactItem getItem(int arg0)
        {
            return contactList.get(arg0);
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getItemId(int)
         */
        @Override
        public long getItemId(int arg0)
        {
            return arg0;
        }


        public class Holder
        {
            ImageView profile;

        }



        /* (non-Javadoc)
         * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
         */
        @Override
        public View getView(int pos, View v, ViewGroup arg2)
        {
            final ViewHolderItem viewHolder;
//            if(v==null){
                v = LayoutInflater.from(getActivity()).inflate(
                        R.layout.contact_item, null);

                viewHolder = new ViewHolderItem();
                viewHolder.lbl = (TextView) v.findViewById(R.id.lblContactDisplayName);
                viewHolder.lbl2 = (TextView) v.findViewById(R.id.lblContactPhone);
                viewHolder.lbl3 = (TextView) v.findViewById(R.id.lblContactStatus);
                viewHolder.profile = (ImageView) v.findViewById(R.id.imgContactListItem);
                viewHolder.img2 = (ImageView) v.findViewById(R.id.online);
                viewHolder.img3 = (ImageView) v.findViewById(R.id.messageicon);
                viewHolder.invite = (TextView) v.findViewById(R.id.invite_button);
                viewHolder.parent = (LinearLayout) v.findViewById(R.id.parent_contact);

//                v.setTag(viewHolder);
//            }else {
//                viewHolder = (ViewHolderItem) v.getTag();
//            }



//			Holder holder=new Holder();
            ContactItem c = getItem(pos);
//			TextView lbl = (TextView) v.findViewById(R.id.lblContactDisplayName);
            viewHolder.lbl.setText(c.getUserName());
            viewHolder.selected = adapter.hasContact(c.getPhone());

//			TextView lbl2 = (TextView) v.findViewById(R.id.lblContactPhone);
            viewHolder.lbl2.setText(c.getPhone());

//			TextView lbl3 = (TextView) v.findViewById(R.id.lblContactStatus);
            viewHolder.lbl3.setText(c.status());

//			holder.profile = (ImageView) v.findViewById(R.id.imgContactListItem);
            viewHolder.photo_uri = c.getProfileimg();
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
//				try {
////					photo_stream.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}

            //img.setImageResource(c.getIcon());

//			ImageView img2 = (ImageView) v.findViewById(R.id.online);
            viewHolder.img2.setVisibility(View.INVISIBLE);

//			ImageView img3 = (ImageView) v.findViewById(R.id.messageicon);
            viewHolder.img3.setVisibility(View.INVISIBLE);

//			TextView invite = (TextView) v.findViewById(R.id.invite_button);
            viewHolder.invite.setVisibility(View.INVISIBLE);

            viewHolder.selected = adapter.hasContact(c.getPhone());
            if(viewHolder.selected) {
//                viewHolder.selected = true;
                viewHolder.parent.setBackgroundColor(Color.parseColor("#00b863"));
//                adapter.addAndApply(viewHolder.lbl2.getText().toString(), viewHolder.lbl.getText().toString(), viewHolder.photo_uri);
                viewHolder.lbl.setTextColor(Color.parseColor("#ffffff"));
            }else {
//                viewHolder.selected = false;
                viewHolder.parent.setBackgroundColor(Color.parseColor("#ffffff"));
                viewHolder.lbl.setTextColor(Color.parseColor("#00b863"));
//                adapter.removeContact(viewHolder.lbl2.getText().toString());
            }


            v.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if(!viewHolder.selected) {
                        viewHolder.selected = true;
                        viewHolder.parent.setBackgroundColor(Color.parseColor("#00b863"));
                        adapter.addAndApply(viewHolder.lbl2.getText().toString(), viewHolder.lbl.getText().toString(), viewHolder.photo_uri);
                        viewHolder.lbl.setTextColor(Color.parseColor("#ffffff"));
                    }else {
                        viewHolder.selected = false;
                        viewHolder.parent.setBackgroundColor(Color.parseColor("#ffffff"));
                        viewHolder.lbl.setTextColor(Color.parseColor("#00b863"));
                        adapter.removeContact(viewHolder.lbl2.getText().toString());
                    }
                }
            });

            final String tempContactId = c.getUserId();
            final String tempPhone = c.getPhone();
            final ContactItem c_reference = c;




            return v;
        }





    }
}
