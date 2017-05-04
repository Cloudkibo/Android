package com.cloudkibo.ui;


import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.library.CircleTransform;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.library.Utility;
import com.cloudkibo.model.ContactItem;
import com.cloudkibo.utils.IFragmentName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class CreateBroadCastList_B extends CustomFragment implements IFragmentName{

    private String authtoken;
    private DatabaseHandler db;
    public static ArrayList<ContactItem> contactList;
    private ContactAdapter contactAdapter;
    CreateBroadCastList_B reference = this;
    HorizontalViewAdapter adapter;
    String bList_id;
    String list_name;
    String unique_id;
    Context context = getContext();


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
            bList_id = args.getString("bList_id");
            list_name = args.getString("list_name");
            unique_id = args.getString("unique_id");
            Toast.makeText(getContext(), bList_id, Toast.LENGTH_LONG).show();

        }

        adapter = new HorizontalViewAdapter(innerLay);



        ListView list = (ListView) v.findViewById(R.id.kibo_contact);
        contactAdapter = new ContactAdapter();
        loadContactList();
        list.setAdapter(contactAdapter);

        FloatingActionButton next = (FloatingActionButton) v.findViewById(R.id.fab);
        final CreateBroadCastList_B temp = this;
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(adapter.getPhones().size() <= 0){
                    Toast.makeText(getContext(), "Please select atleast one List member", Toast.LENGTH_LONG).show();
                    return;
                }
                bList_id = randomString(10);
                Toast.makeText(getContext(), "List Name: " + list_name, Toast.LENGTH_LONG).show();
                db.createBroadcastList(bList_id, list_name);

                String message = "You created List "+ list_name;
                String member_name = db.getUserDetails().get("display_name");
                String member_phone = db.getUserDetails().get("phone");
                String uniqueid = Long.toHexString(Double.doubleToLongBits(Math.random()));
                uniqueid += (new Date().getYear()) + "" + (new Date().getMonth()) + "" + (new Date().getDay());
                uniqueid += (new Date().getHours()) + "" + (new Date().getMinutes()) + "" + (new Date().getSeconds());
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();

                db.addBroadCastChat("", member_phone, member_name, message,
                        Utility.getCurrentTimeInISO(), "sent", uniqueid, "log", "", bList_id);

                addMembers(adapter.getPhones());
                // // TODO: 4/26/17 add link to broadcast list fragment
//                BroadCastChat nextFrag= new BroadCastChat();
//                Bundle args = new Bundle();
//                args.putString("bList_id", bList_id);
//                args.putString("list_name", list_name);
//                args.putString("authtoken", authtoken);
//                nextFrag.setArguments(args);
//                getFragmentManager().beginTransaction()
//                        .replace(R.id.content_frame, nextFrag, "groupChatFragmentTag")
//                        .addToBackStack(null)
//                        .commit();
            }
        });

        return v;
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

    @Override
    public void onClick(View v)
    {
        super.onClick(v);
    }


    public void addMembers(ArrayList<String> phones){

        for (int i = 0; i< phones.size(); i++){
            db.addBroadCastListMember(bList_id ,phones.get(i));
            Toast.makeText(getContext(), phones.get(i) + "Added", Toast.LENGTH_SHORT).show();
        }
        if(MainActivity.isVisible) {
            // todo do the broadcast work on desktop app as well
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

        final DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());

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
            final CreateGroup.ViewHolderItem viewHolder;
//            if(v==null){
            v = LayoutInflater.from(getActivity()).inflate(
                    R.layout.contact_item, null);

            viewHolder = new CreateGroup.ViewHolderItem();
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


    @Override
    public String getFragmentName() {
        return "CreateBroadcast List";
    }

    @Override
    public String getFragmentContactPhone() {
        return "About chat";
    }
}
