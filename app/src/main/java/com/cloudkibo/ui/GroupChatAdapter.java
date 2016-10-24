package com.cloudkibo.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudkibo.R;

import java.util.ArrayList;

public class GroupChatAdapter extends BaseAdapter{
    ArrayList<String> result = new ArrayList<String>();
    ArrayList<String> contactName = new ArrayList<String>();
    private static LayoutInflater inflater=null;
    public GroupChatAdapter(LayoutInflater context, ArrayList<String> chatList, ArrayList<String> contactName) {
        // TODO Auto-generated constructor stub
        result=chatList;
        this.contactName =contactName;
        inflater = ( LayoutInflater )context;
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return result.size();
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
        TextView message;
        TextView contact_name;
        LinearLayout message_box;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder=new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.group_chat_list, null);

        holder.message=(TextView) rowView.findViewById(R.id.message);
        holder.contact_name=(TextView) rowView.findViewById(R.id.contact_name);
        holder.message.setText(result.get(position));
        if(contactName.get(position).equals("")){
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(100,10, 5,0);
            holder.contact_name.setText("You");
            holder.message_box = (LinearLayout) rowView.findViewById(R.id.message_box);
            holder.message_box.setBackgroundColor(Color.parseColor("#EBEBEB"));
//            message_box.setLayoutParams(layoutParams);
        }else{
            holder.contact_name.setText(contactName.get(position));
        }
        rowView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //Toast.makeText(context, "You Clicked "+result[position], Toast.LENGTH_LONG).show();
            }
        });
        return rowView;
    }

} 