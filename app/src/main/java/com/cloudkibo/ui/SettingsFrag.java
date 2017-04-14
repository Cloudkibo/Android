package com.cloudkibo.ui;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.model.Data;
import com.cloudkibo.utils.IFragmentName;

import java.util.ArrayList;


public class SettingsFrag extends CustomFragment implements IFragmentName {
    private ArrayList<Data> settingsItemsList;
    private SettingsItemsAdapter settingsAdapter;
    private String authtoken;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.settings, null);
        setHasOptionsMenu(true);

        authtoken = this.getArguments().getString("authtoken");
        loadSettingsItems();

        ListView settings_items = (ListView) v.findViewById(R.id.list);
        settingsAdapter = new SettingsItemsAdapter(getActivity().getApplicationContext());
        settings_items.setAdapter(settingsAdapter);
        settings_items.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
                                    long arg3) {
                launchSetting(pos);
            }
        });
        settingsAdapter.notifyDataSetChanged();

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.findItem(R.id.archived).setVisible(false);
            menu.findItem(R.id.settings).setVisible(false);
            menu.findItem(R.id.connect_to_desktop).setVisible(false);
        }
        inflater.inflate(R.menu.newchat, menu);  // Use filter.xml from step 1
        getActivity().getActionBar().setSubtitle(null);
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
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
            Intent i = new Intent(getActivity().getApplicationContext(), BlockedContacts.class);
            i.putExtra("token", authtoken);
            startActivity(i);
        }
        else if(pos == 1){
            startActivity(new Intent(getActivity().getApplicationContext(), BackSettingActivity.class));
        }
        else if(pos == 2){
            startActivity(new Intent(getActivity().getApplicationContext(), LocaleChange.class));
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
    public String getFragmentName() {
        return "SettingsFrag";
    }

    @Override
    public String getFragmentContactPhone() {
        return "SettingsFrag";
    }
}