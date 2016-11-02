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

import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.ui.AddMembers;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by root on 10/19/16.
 */
public class CustomContactAdapter extends BaseAdapter {

    String [] result;
    String [] phones;

    ArrayList<String> selected_contacts = new ArrayList<String>();

    private static LayoutInflater inflater=null;
    public CustomContactAdapter(LayoutInflater context, String[] prgmNameList, String[] phones) {
        // TODO Auto-generated constructor stub
        result=prgmNameList;
        this.phones = phones;
        inflater = context;

    }
    public ArrayList<String> getSelected_contacts(){
        return selected_contacts;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return result.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }


    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder
    {
        TextView tv;
        boolean clicked = false;
        LinearLayout single_contact;
        String phone;

    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        final Holder holder=new Holder();
        View rowView;

        rowView = inflater.inflate(R.layout.group_contact_entity, null);
        holder.tv=(TextView) rowView.findViewById(R.id.textView1);
        holder.single_contact = (LinearLayout) rowView.findViewById(R.id.single_contact);
        holder.tv.setText(result[position]);
        holder.phone = phones[position];
        rowView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // Toast.makeText(context, "You Clicked "+result[position], Toast.LENGTH_LONG).show();
                if(!holder.clicked){
                    holder.single_contact.setBackgroundColor(Color.parseColor("#2ecc71"));
                    holder.tv.setTextColor(Color.WHITE);
                    holder.clicked = true;
                    selected_contacts.add(holder.phone);
                }else {
                    holder.single_contact.setBackgroundColor(Color.parseColor("#F1F3EE"));
                    holder.tv.setTextColor(Color.BLACK);
                    holder.clicked = false;
                    selected_contacts.remove(holder.phone);
                }
            }
        });

        return rowView;
    }
}
