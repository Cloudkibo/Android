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
import com.cloudkibo.custom.CustomActivity;
import com.cloudkibo.model.Conversation;

import java.util.ArrayList;


public class GroupChatAdapter extends BaseAdapter{
    ArrayList<String> result = new ArrayList<String>();
    ArrayList<String> contactName = new ArrayList<String>();
    private ArrayList<Conversation> convList;
    private static LayoutInflater inflater=null;
    public GroupChatAdapter(LayoutInflater context, ArrayList<String> chatList, ArrayList<String> contactName, ArrayList<Conversation> convList) {
        // TODO Auto-generated constructor stub
        result=chatList;
        this.contactName =contactName;
        inflater = ( LayoutInflater )context;
        this.convList = convList;
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
        TextView contact_phone;
        TextView date;
        TextView status;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder=new Holder();
        View rowView;
        if (convList.get(position).isSent())
            rowView = inflater.inflate(R.layout.chat_item_sent, null);
        else
            rowView = inflater.inflate(R.layout.chat_item_rcv, null);
        holder.message=(TextView) rowView.findViewById(R.id.lbl2);
        holder.contact_phone=(TextView) rowView.findViewById(R.id.phone);
        holder.date = (TextView) rowView.findViewById(R.id.lblContactDisplayName);
        holder.status = (TextView) rowView.findViewById(R.id.lblContactPhone);
        holder.message.setText(convList.get(position).getMsg());
        holder.status.setText(convList.get(position).getStatus());
        if(!convList.get(position).getDate().equals("")){
            holder.date.setText(convList.get(position).getDate().replaceAll("-", "/").split("/",2)[1]);
        }
        if(convList.get(position).getSender_phone().equals("")){
            holder.contact_phone.setText("You");
        }else{
            holder.contact_phone.setText(convList.get(position).getSender_phone());
        }

        rowView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //Toast.makeText(inflater.getContext(), "item clicked", Toast.LENGTH_SHORT).show();
            }
        });

        rowView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return false;
            }
        });
        return rowView;
    }
} 