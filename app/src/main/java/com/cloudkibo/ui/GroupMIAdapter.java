package com.cloudkibo.ui;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudkibo.R;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.library.GroupUtility;
import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.AccountKit;

import org.json.JSONArray;
import org.json.JSONException;

public class GroupMIAdapter extends BaseAdapter {

    JSONArray members;
    JSONArray status;
    Context context;
    String group_id;
    String message_id;
    String message;
    private static LayoutInflater inflater=null;
    public GroupMIAdapter(LayoutInflater inflater, JSONArray members, JSONArray status, Context context, String group_id, String message_id, String message) {
        this.members = members;
        this.status = status;
        this.inflater = inflater;
        this.context = context;
        this.group_id = group_id;
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
        TextView status;
        TextView time;
        TextView message;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder=new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.group_message_status, null);
        holder.name=(TextView) rowView.findViewById(R.id.name);
        holder.status=(TextView) rowView.findViewById(R.id.status);
        holder.time = (TextView) rowView.findViewById(R.id.lblTime);
//        holder.tv.setText(result[position]);

        try {
//            Toast.makeText(context, members.toString(), Toast.LENGTH_LONG ).show();
            DatabaseHandler db = new DatabaseHandler(context);
            if(!members.getJSONObject(position).has("display_name")){
                if(members.getJSONObject(position).getString("phone").toString().equals(db.getUserDetails().get("phone"))){
                    holder.name.setText(db.getUserDetails().get("display_name"));


                }else{
                    holder.name.setText("Anonymous");
                }
            }
            else{
                holder.name.setText(members.getJSONObject(position).getString("display_name"));
            }
            status = db.getGroupMessageStatus(message_id, members.getJSONObject(position).getString("phone"));

            holder.status.setText(status.getJSONObject(0).getString("status"));
            holder.time.setText("--");

            Toast.makeText(context, status.getJSONObject(0).getString("delivered_date"), Toast.LENGTH_LONG).show();

                if(status.getJSONObject(0).getString("delivered_date")!=null) {
                    if (status.getJSONObject(0).getString("status").equals("seen")) {
                        holder.time.setText(status.getJSONObject(0).getString("read_date"));
                    }
//                else
//                    holder.time.setText(status.getJSONObject(position).getString("delivered_time"));
                }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return rowView;
    }

    public void updateData(DatabaseHandler db){
        this.members = getMembers(db);
        this.notifyDataSetChanged();
    }

    public JSONArray getMembers(DatabaseHandler db){
        try {
            JSONArray members = db.getGroupMembers(group_id);
//            members.put(db.getMyDetailsInGroup(group_id));
            return  members;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }
}
