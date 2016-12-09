package com.cloudkibo.ui;

import android.os.Bundle;
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



public class MessageInfo extends CustomFragment implements IFragmentName {

    private String authtoken;
    private String iMessage;
    private String iStatus;
    private String iDate;

    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.message_info, null);
        setHasOptionsMenu(true);

        authtoken = this.getArguments().getString("authtoken");

        TextView message = (TextView) v.findViewById(R.id.lbl2);
        TextView status = (TextView) v.findViewById(R.id.lblMessageStatus);
        TextView date = (TextView) v.findViewById(R.id.lblTimeStatus);

        iMessage = this.getArguments().getString("message");
        iStatus = this.getArguments().getString("status");
        iDate = this.getArguments().getString("date");

        iDate.replaceAll("-", "/").split("/",2);

        message.setText(iMessage);
        status.setText(iStatus);
        date.setText(iDate);

        getActivity().getActionBar().setTitle("Message Info");
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.findItem(R.id.archived).setVisible(false);
        }
        inflater.inflate(R.menu.newchat, menu);  // Use filter.xml from step 1
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.archived){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }







    public String getFragmentName()
    {
        return "Message Info";
    }

    public String getFragmentContactPhone()
    {
        return "Message Info";
    }
}
