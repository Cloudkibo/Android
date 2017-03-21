package com.cloudkibo.ui;


import android.app.ActionBar;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.utils.IFragmentName;

public class ViewContact extends CustomFragment implements IFragmentName {
    private String contactName;
    private String contactPhone;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.view_contact, null);
        setHasOptionsMenu(true);

        TextView lblContactName = (TextView) v.findViewById(R.id.lblContactName);
        TextView lblContactPhone = (TextView) v.findViewById(R.id.lblContactPhone);

        contactName = this.getArguments().getString("name");
        contactPhone = this.getArguments().getString("phone");

        lblContactName.setText(contactName);
        lblContactPhone.setText(contactPhone);

        getActivity().getActionBar().setTitle("View Contact");

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.findItem(R.id.archived).setVisible(false);
            menu.findItem(R.id.settings).setVisible(false);
            menu.findItem(R.id.connect_to_desktop).setVisible(false);
            menu.findItem(R.id.search_chats).setVisible(false);
        }
        inflater.inflate(R.menu.newchat, menu);  // Use filter.xml from step 1
        getActivity().getActionBar().setSubtitle(null);
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.archived){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public String getFragmentName() {
        return "View Contact";
    }

    @Override
    public String getFragmentContactPhone() {
        return "View Contact";
    }
}
