package com.cloudkibo.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cloudkibo.R;
import com.cloudkibo.database.DatabaseHandler;
import com.facebook.accountkit.AccountKit;

import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Text;

public class CustomDayStatusAdapter extends BaseAdapter {
    JSONArray members;
    Context context;
    String statusID;
    DatabaseHandler db;
    private static LayoutInflater inflater=null;
    public CustomDayStatusAdapter(LayoutInflater inflater, JSONArray members, Context context, String statusID) {
        this.members = members;
        this.inflater = inflater;
        this.context = context;
        this.statusID = statusID;
        db = new DatabaseHandler(context);
        AccountKit.initialize(context.getApplicationContext());
    }
    public CustomDayStatusAdapter(LayoutInflater inflater, JSONArray members, Context context) {
        this.members = members;
        this.inflater = inflater;
        this.context = context;
        AccountKit.initialize(context.getApplicationContext());
    }

    @Override
    public int getCount() {
        return members.length();
    }

    @Override
    public Object getItem(int position) {
        try {
            return members.get(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class Holder{
        ImageView profile;
        TextView lbl;
        TextView lbl2;
        TextView lbl3;
        ImageView img2;
        ImageView img3;
        TextView invite;

    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Holder holder=new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.contact_item, null);
        holder.lbl=(TextView) rowView.findViewById(R.id.lblContactDisplayName);
        holder.lbl2=(TextView) rowView.findViewById(R.id.lblContactPhone);
        holder.lbl3=(TextView) rowView.findViewById(R.id.lblContactStatus);
        holder.img2=(ImageView) rowView.findViewById(R.id.online);
        holder.img3=(ImageView) rowView.findViewById(R.id.messageicon);
        holder.invite= (TextView) rowView.findViewById(R.id.invite_button);

        holder.img2.setVisibility(View.GONE);
        holder.img3.setVisibility(View.GONE);
        holder.invite.setVisibility(View.GONE);

        try {
//            String name = members.getJSONObject(i).getString("uploaded_by");
//            if(members!=null) {
//                name = db.getSpecificContact(members.getJSONObject(i).getString("uploaded_by")).getJSONObject(0).getString("display_name");
//            }
            holder.lbl2.setText(members.getJSONObject(i).getString("uploaded_by"));
            holder.lbl3.setText(members.getJSONObject(i).getString("upload_date"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rowView;
    }
}
