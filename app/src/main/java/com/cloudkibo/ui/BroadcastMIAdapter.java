package com.cloudkibo.ui;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cloudkibo.R;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.library.GroupUtility;
import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.AccountKit;

import org.json.JSONArray;
import org.json.JSONException;

public class BroadcastMIAdapter extends BaseAdapter {
    JSONArray members;
    Context context;
    String bList_id;
    String message_id;
    String message;
    private static LayoutInflater inflater=null;

    public BroadcastMIAdapter(LayoutInflater inflater, JSONArray members, Context context, String bList_id, String message_id, String message) {
        this.members = members;
        this.inflater = inflater;
        this.context = context;
        this.bList_id = bList_id;
        this.message_id = message_id;
        this.message = message;
        AccountKit.initialize(context.getApplicationContext());
    }

    @Override
    public int getCount() {
        return members.length();
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
        TextView name;
        TextView isAdmin;
        TextView status;
        TextView Message;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder=new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.group_message_status, null);
        holder.name=(TextView) rowView.findViewById(R.id.name);
        holder.status=(TextView) rowView.findViewById(R.id.status);
        rowView.findViewById(R.id.lblTime).setVisibility(View.GONE);

        try {
            DatabaseHandler db = new DatabaseHandler(context);
            if(!members.getJSONObject(position).has("display_name")){
                if(members.getJSONObject(position).getString("phone").toString().equals(db.getUserDetails().get("phone"))){
                    holder.name.setText(db.getUserDetails().get("display_name"));
                }else{
                    holder.name.setText("Anonymous");
                }
            } else{
                holder.name.setText(members.getJSONObject(position).getString("display_name"));
            }
            JSONArray demo = db.getBroadcastMessageStatus(message_id, members.getJSONObject(position).getString("phone").toString());
            int l = demo.length();

            holder.status.setText(demo.getJSONObject(0).getString("status"));

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return rowView;
    }


    public JSONArray getMembers(DatabaseHandler db){
        try {
            JSONArray members = db.getBroadCastListMembers(bList_id);
//            members.put(db.getMyDetailsInGroup(bList_id));
            return  members;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }

}
