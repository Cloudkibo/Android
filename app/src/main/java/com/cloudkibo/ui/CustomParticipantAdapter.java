package com.cloudkibo.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudkibo.R;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.library.GroupUtility;
import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.AccountKit;

import org.json.JSONArray;
import org.json.JSONException;

public class CustomParticipantAdapter extends BaseAdapter{
    JSONArray members;
    Context context;
    String group_id;
    private static LayoutInflater inflater=null;
    public CustomParticipantAdapter(LayoutInflater inflater, JSONArray members, Context context, String group_id) {
        // TODO Auto-generated constructor stub
        this.members = members;
        this.inflater = inflater;
        this.context = context;
        this.group_id = group_id;
        AccountKit.initialize(context.getApplicationContext());
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return members.length();
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
        TextView name;
        TextView isAdmin;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder=new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.participant, null);
        holder.name=(TextView) rowView.findViewById(R.id.name);
        holder.isAdmin=(TextView) rowView.findViewById(R.id.isAdmin);
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

            if(members.getJSONObject(position).getString("isAdmin").equals("1")){
                holder.isAdmin.setVisibility(View.VISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        rowView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //Toast.makeText(context, "You Clicked "+result[position], Toast.LENGTH_LONG).show();

                final CharSequence[] items = {"Make Admin", "Demote Admin", "Remove from group"};

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Member Status");
                builder.setItems(items, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        DatabaseHandler db = new DatabaseHandler(context);
                        GroupUtility groupUtility = new GroupUtility(context);
                        final AccessToken accessToken = AccountKit.getCurrentAccessToken();
                        try {
                            if(which == 0) {
                                // Toast.makeText(context,"I was clicked",Toast.LENGTH_LONG).show();
                                db.makeGroupAdmin(group_id, members.getJSONObject(position).getString("phone"));
                                groupUtility.updateMemberRole(group_id, members.getJSONObject(position).getString("phone"),"Yes",accessToken.getToken());
                                // Toast.makeText(context,db.getGroupAdmins(group_id).toString(),Toast.LENGTH_LONG).show();
                                updateData(db);
                            } else if (which == 1){
                                  db.demoteGroupAdmin(group_id,members.getJSONObject(position).getString("phone"));
                                  groupUtility.updateMemberRole(group_id, members.getJSONObject(position).getString("phone"),"No",accessToken.getToken());
                                  updateData(db);
                            } else if (which == 2) {
                                db.leaveGroup(group_id, members.getJSONObject(position).getString("phone"));

                                groupUtility.removeMember(group_id, members.getJSONObject(position).getString("phone"), accessToken.getToken());
                                updateData(db);
                            }

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
