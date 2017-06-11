package com.cloudkibo.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cloudkibo.R;
import com.facebook.accountkit.AccountKit;

import org.json.JSONArray;

public class CustomDayStatusAdapter extends BaseAdapter {
    JSONArray members;
    Context context;
    String statusID;
    private static LayoutInflater inflater=null;
    public CustomDayStatusAdapter(LayoutInflater inflater, JSONArray members, Context context, String statusID) {
        this.members = members;
        this.inflater = inflater;
        this.context = context;
        this.statusID = statusID;
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
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class Holder
    {
        TextView name;
        TextView time;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Holder holder=new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.participant, null);
        holder.name=(TextView) rowView.findViewById(R.id.name);
        holder.time=(TextView) rowView.findViewById(R.id.isAdmin);

        // TODO: 6/11/17 fetch the members from db and set TextViews 
        return null;
    }
}
