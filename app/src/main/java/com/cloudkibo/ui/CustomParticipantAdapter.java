package com.cloudkibo.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudkibo.R;

public class CustomParticipantAdapter extends BaseAdapter{
    String [] names;
    Context context;
    private static LayoutInflater inflater=null;
    public CustomParticipantAdapter(LayoutInflater inflater, String[] names) {
        // TODO Auto-generated constructor stub
        this.names = names;
        this.inflater = inflater;
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return names.length;
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
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder=new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.participant, null);
        holder.name=(TextView) rowView.findViewById(R.id.name);
//        holder.tv.setText(result[position]);
        holder.name.setText(names[position]);
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