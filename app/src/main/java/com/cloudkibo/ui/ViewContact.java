package com.cloudkibo.ui;


import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.model.ContactItem;
import com.cloudkibo.utils.IFragmentName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ViewContact extends CustomFragment implements IFragmentName {
    private String contactName;
    private String contactPhone;
    private String authtoken;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.view_contact, null);
        setHasOptionsMenu(true);

        TextView lblContactName = (TextView) v.findViewById(R.id.lblContactName);
        TextView lblContactPhone = (TextView) v.findViewById(R.id.lblContactPhone);
        Button sendButton = (Button) v.findViewById(R.id.sendButton);
        Button inviteButton = (Button) v.findViewById(R.id.inviteButton);

        contactName = this.getArguments().getString("name");
        contactPhone = this.getArguments().getString("phone");
        authtoken = this.getArguments().getString("authtoken");

        DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());

        try {
            JSONArray jsonA = db.getContactsWithImages();

            jsonA = UserFunctions.sortJSONArrayIgnoreCase(jsonA, "display_name");



            for (int i=0; i < jsonA.length(); i++) {
                JSONObject row = jsonA.getJSONObject(i);
                String phone = row.getString("phone");
                String newphone = phone.substring(phone.length()-10);
                String newCont = contactPhone.substring(contactPhone.length()-10);
                if(newphone.equals(newCont)){
                    contactPhone = phone;
                    sendButton.setVisibility(View.VISIBLE);
                    inviteButton.setVisibility(View.GONE);
                    break;

                } else {
                    sendButton.setVisibility(View.GONE);
                    inviteButton.setVisibility(View.VISIBLE);

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Send Clicked", Toast.LENGTH_SHORT).show();
                GroupChat groupChatFragment = new GroupChat();
                Bundle bundle = new Bundle();

                bundle.putString("contactusername", contactName);
                bundle.putString("contactphone", contactPhone);
                bundle.putString("authtoken", authtoken);

                groupChatFragment.setArguments(bundle);

                getFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, groupChatFragment, "groupChatFragmentTag")
                        .addToBackStack(contactName).commit();
            }
        });

        inviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Invite Clicked", Toast.LENGTH_SHORT).show();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) // At least KitKat
                {
                    String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(getActivity().getApplicationContext()); // Need to change the build to API 19

                    Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
                    sendIntent.setType("text/plain");
                    sendIntent.setData(Uri.parse("smsto:" +  contactPhone));
                    //sendIntent.putExtra(Intent.EXTRA_TEXT, "Join me on CloudKibo for video chat. Download from https://www.cloudkibo.com");
                    sendIntent.putExtra("sms_body", "Join me on CloudKibo for video chat. Download from https://play.google.com/store/apps/details?id=com.cloudkibo&hl=en");

                    if (defaultSmsPackageName != null)// Can be null in case that there is no default, then the user would be able to choose
                    // any app that support this intent.
                    {
                        sendIntent.setPackage(defaultSmsPackageName);
                    }
                    startActivity(sendIntent);

                }
                else // For early versions.
                {
                    Intent smsIntent = new Intent(android.content.Intent.ACTION_VIEW);
                    smsIntent.setType("vnd.android-dir/mms-sms");
                    smsIntent.putExtra("address", contactPhone);
                    smsIntent.putExtra("sms_body","Join me on CloudKibo for video chat. Download from https://play.google.com/store/apps/details?id=com.cloudkibo&hl=en");
                    startActivity(smsIntent);
                }

            }
        });

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
