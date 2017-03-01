package com.cloudkibo.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudkibo.R;
import com.cloudkibo.custom.CustomActivity;
import com.cloudkibo.model.Data;

import java.util.ArrayList;

/**
 * Created by sojharo on 27/02/2017.
 */

public class Settings extends AppCompatActivity {

    private ArrayList<Data> settingsItemsList;

    private SettingsItemsAdapter settingsAdapter;

    private String authtoken;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings);

        authtoken = getIntent().getExtras().getString("token");

        loadSettings();

    }

    private void loadSettings () {
        loadSettingsItems();
        ListView settings_items = (ListView) findViewById(R.id.list);
        settingsAdapter = new SettingsItemsAdapter(getApplicationContext());
        settings_items.setAdapter(settingsAdapter);
        settings_items.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
                                    long arg3) {
                launchSetting(pos);
            }
        });
        settingsAdapter.notifyDataSetChanged();
    }

    private void loadSettingsItems()
    {
        ArrayList<Data> al = new ArrayList<Data>();
        al.add(new Data(getString(R.string.unblock_contact), null, R.drawable.ic_notes));
        al.add(new Data(getString(R.string.backup_settings), null, R.drawable.ic_chat));
        al.add(new Data(getString(R.string.language), null, R.drawable.ic_about));
        this.settingsItemsList = new ArrayList<Data>(al);
        if(settingsAdapter != null)
            settingsAdapter.notifyDataSetChanged();
    }

    private void launchSetting(int pos)
    {
        if (pos == 0)
        {
            Intent i = new Intent(getApplicationContext(), BlockedContacts.class);
            i.putExtra("token", authtoken);
            startActivity(i);
        }
        else if(pos == 1){
            startActivity(new Intent(getApplicationContext(), BackSettingActivity.class));
        }
        else if(pos == 2){
            startActivity(new Intent(getApplicationContext(), LocaleChange.class));
        }
    }

    private class SettingsItemsAdapter extends BaseAdapter
    {

        Context context;

        public SettingsItemsAdapter(Context appContext){
            context = appContext;
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getCount()
         */
        @Override
        public int getCount()
        {
            return settingsItemsList.size();
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getItem(int)
         */
        @Override
        public Data getItem(int arg0)
        {
            return settingsItemsList.get(arg0);
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getItemId(int)
         */
        @Override
        public long getItemId(int position)
        {
            return position;
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
                convertView = LayoutInflater.from(this.context).inflate(
                        R.layout.settings_item, null);
            TextView lbl = (TextView) convertView.findViewById(R.id.lbl);
            lbl.setText(getItem(position).getTitle1());

            ImageView img = (ImageView) convertView.findViewById(R.id.img);
            img.setImageResource(getItem(position).getImage1());
            return convertView;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


}
