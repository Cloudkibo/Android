package com.cloudkibo.custom;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.library.CircleTransform;
import com.cloudkibo.ui.AddMembers;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by root on 10/19/16.
 */
public class CustomContactAdapter extends BaseAdapter {

//    String [] result;
    ArrayList<String> phones;

    ArrayList<String> selected_contacts = new ArrayList<String>();
    DatabaseHandler db;
    Context ctx;

    CustomContactAdapter reference = this;

    private static LayoutInflater inflater=null;
    public CustomContactAdapter(LayoutInflater context, ArrayList<String> phones, Context ctx) {
//        result=prgmNameList;
        this.phones = phones;
        inflater = context;
        db = new DatabaseHandler(ctx);
        this.ctx = ctx;

    }
    public ArrayList<String> getSelected_contacts(){
        return selected_contacts;
    }

    @Override
    public int getCount() {
        return phones.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    public class Holder
    {
        TextView tv;
        ImageView profile;
        boolean clicked = false;
        LinearLayout single_contact;
        String phone;

    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Holder holder=new Holder();
        View rowView;

        rowView = inflater.inflate(R.layout.group_contact_entity, null);
        holder.tv=(TextView) rowView.findViewById(R.id.textView1);
        holder.profile=(ImageView) rowView.findViewById(R.id.imageView1);
        holder.single_contact = (LinearLayout) rowView.findViewById(R.id.single_contact);
        holder.tv.setText(db.getContactName(phones.get(position)));
        holder.phone = phones.get(position);
        Glide   .with(ctx)
                .load(db.getContactImage(phones.get(position)))
                .thumbnail(0.1f)
                .centerCrop()
                .transform(new CircleTransform(ctx))
                .placeholder(R.drawable.avatar)
                .into(holder.profile);

//        if(!selected_contacts.contains(holder.phone)){
//            holder.single_contact.setBackgroundColor(Color.parseColor("#F1F3EE"));
//            holder.tv.setTextColor(Color.BLACK);
//        }else {
//            holder.single_contact.setBackgroundColor(Color.parseColor("#2ecc71"));
//            holder.tv.setTextColor(Color.WHITE);
//        }
//        rowView.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//
//                // Toast.makeText(context, "You Clicked "+result[position], Toast.LENGTH_LONG).show();
//                if(!holder.clicked){
//                    holder.single_contact.setBackgroundColor(Color.parseColor("#2ecc71"));
//                    holder.tv.setTextColor(Color.WHITE);
//                    holder.clicked = true;
//                    selected_contacts.add(holder.phone);
//                }else {
//                    holder.single_contact.setBackgroundColor(Color.parseColor("#F1F3EE"));
//                    holder.tv.setTextColor(Color.BLACK);
//                    holder.clicked = false;
//                    selected_contacts.remove(holder.phone);
//                }
//            }
//        });

        return rowView;
    }
}
