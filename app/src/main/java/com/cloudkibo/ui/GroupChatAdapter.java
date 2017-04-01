package com.cloudkibo.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomActivity;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.library.CircleTransform;
import com.cloudkibo.library.Utility;
import com.cloudkibo.model.Conversation;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class GroupChatAdapter extends BaseAdapter{
    ArrayList<String> result = new ArrayList<String>();
    ArrayList<String> contactName = new ArrayList<String>();
    private ArrayList<Conversation> convList;
    Context context;
    private static LayoutInflater inflater=null;
    public GroupChatAdapter(LayoutInflater context, ArrayList<String> chatList, ArrayList<String> contactName, ArrayList<Conversation> convList, Context ctx) {
        result=chatList;
        this.contactName =contactName;
        inflater = ( LayoutInflater )context;
        this.convList = convList;
        this.context = ctx;
    }
    @Override
    public int getCount() {
        return convList.size();
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
        TextView message;
        TextView contact_phone;
        TextView date;
        TextView status;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder=new Holder();

        View rowView;
       // rowView = inflater.inflate(R.layout.chat_item_contact, null);

        if(convList.get(position).getType().equals("contact")){
            rowView = inflater.inflate(R.layout.chat_item_contact, null);
            if (convList.get(position).isSent()) {
                TextView contact_name = (TextView) rowView.findViewById(R.id.contact_name);
                contact_name.setText(convList.get(position).getMsg().split(":")[0]);
                ImageView contact_image = (ImageView) rowView.findViewById(R.id.contact_image);
                DatabaseHandler db = new DatabaseHandler(MainActivity.mainActivity);
                String image_uri = db.getContactImage(convList.get(position).getMsg().split(":")[1]);
                Glide
                        .with(MainActivity.mainActivity)
                        .load(image_uri)
                        .thumbnail(0.1f)
                        .centerCrop()
                        .transform(new CircleTransform(MainActivity.mainActivity))
                        .placeholder(R.drawable.avatar)
                        .into(contact_image);
            } else {
                rowView = inflater.inflate(R.layout.chat_item_contact_received, null);
                TextView contact_name = (TextView) rowView.findViewById(R.id.contact_name);
                contact_name.setText(convList.get(position).getMsg().split(":")[0]);
                (rowView.findViewById(R.id.addButton)).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MainActivity act1 = MainActivity.mainActivity;
                        act1.createContact(convList.get(position).getMsg().split(":")[1], convList.get(position).getMsg().split(":")[0]);
                    }
                });
            }


            return  rowView;
        }

        String type = convList.get(position).getType();

        if(convList.get(position).getType().equals("location")){
            rowView = inflater.inflate(R.layout.chat_item_image, null);

            TextView lbl = (TextView) rowView.findViewById(R.id.lblContactPhone);

                lbl.setText(convList.get(position).getStatus());

            if (!convList.get(position).isSent()) {
                rowView = inflater.inflate(
                        R.layout.chat_item_image_received, null);
            }
            lbl = (TextView) rowView.findViewById(R.id.lblContactDisplayName);
            DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            DateFormat outputFormat = new SimpleDateFormat("MM/dd KK:mm a");
            try {
                String readable_date = Utility.convertDateToLocalTimeZoneAndReadable(convList.get(position).getDate());
                lbl.setText(outputFormat.format(inputFormat.parse(readable_date)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            final String latitude = convList.get(position).getMsg().split(":")[0];
            final String longitude = convList.get(position).getMsg().split(":")[1];
            ImageView container_image = (ImageView) rowView.findViewById(R.id.row_stamp);
            String image_uri = "http://maps.google.com/maps/api/staticmap?center="+ latitude +","+ longitude +"&zoom=18&size=500x300&sensor=TRUE_OR_FALSE";
            Glide
                    .with(MainActivity.mainActivity)
                    .load(image_uri)
                    .thumbnail(0.1f)
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_dialog_map)
                    .into(container_image);

            container_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Double lat = Double.parseDouble(latitude);
                    Double  lon = Double.parseDouble(longitude);
                    String uri = "geo:" + lat + ","
                            +lon + "?q=" + lat
                            + "," + lon;
                    context.startActivity(new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse(uri)));
                }
            });

            return  rowView;
        }

        if(convList.get(position).getType().equals("log")){
            rowView = inflater.inflate(R.layout.chat_item_log, null);
        }
        else if (convList.get(position).isSent())
            rowView = inflater.inflate(R.layout.chat_item_sent, null);
        else
            rowView = inflater.inflate(R.layout.chat_item_rcv, null);

        if(!convList.get(position).getType().equals("log")){
        holder.message=(TextView) rowView.findViewById(R.id.lbl2);
        holder.contact_phone=(TextView) rowView.findViewById(R.id.phone);
        holder.date = (TextView) rowView.findViewById(R.id.lblContactDisplayName);
        holder.status = (TextView) rowView.findViewById(R.id.lblContactPhone);
        holder.message.setText(convList.get(position).getMsg());
        holder.status.setText(convList.get(position).getStatus());
        if(!convList.get(position).getDate().equals("")){
//            holder.date.setText(convList.get(position).getDate().split(" ")[1]);
            try {
                DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                DateFormat outputFormat = new SimpleDateFormat("MM/dd KK:mm a");
                String readable_date = Utility.convertDateToLocalTimeZoneAndReadable(convList.get(position).getDate());
                holder.date.setText(outputFormat.format(inputFormat.parse(readable_date)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if(convList.get(position).getSender_phone().equals("")){
            holder.contact_phone.setText(inflater.getContext().getString(R.string.common_you));
        }else{
            holder.contact_phone.setText(convList.get(position).getSender_phone());
        }
        }else {
            TextView log = (TextView) rowView.findViewById(R.id.log);
            log.setText(convList.get(position).getMsg());
        }


        rowView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
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