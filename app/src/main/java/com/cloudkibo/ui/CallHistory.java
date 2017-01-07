package com.cloudkibo.ui;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//import com.cloudkibo.R;
import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.model.CallHistoryItem;
import com.cloudkibo.utils.IFragmentName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The Class ContactList is the Fragment class that is launched when the user
 * clicks on Projects option in Left navigation drawer. It simply display a
 * dummy list of projects. You need to write actual implementation for loading
 * and displaying projects.
 */
public class CallHistory extends CustomFragment implements IFragmentName
{

    /** The Activity list. */
    private ArrayList<CallHistoryItem> callList;

    private String authtoken;

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.call_history_list, null);
        setHasOptionsMenu(true);

        authtoken = getActivity().getIntent().getExtras().getString("authtoken");

        loadCallHistoryList();
        ListView list = (ListView) v.findViewById(R.id.list);
        list.setAdapter(new NoteAdapter());
        list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
                                    long arg3) {

            }
        });

        registerForContextMenu(list);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.findItem(R.id.archived).setVisible(false);
            menu.findItem(R.id.language).setVisible(false);
            menu.findItem(R.id.backup_setting).setVisible(false);
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

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle(getString(R.string.common_select_action));
        menu.add(0, v.getId(), 0, getString(R.string.common_call));
        menu.add(0, v.getId(), 0, getString(R.string.common_send_message));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {

        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        if(item.getTitle()==getString(R.string.common_call))
        {

            Log.d("CALL", "Call button pressed");

            MainActivity act1 = (MainActivity)getActivity();

            act1.callThisPerson(callList.get(info.position).getContact_phone(),
                    callList.get(info.position).getContact_name());

        }
        else if(item.getTitle()==getString(R.string.common_send_message))
        {
            Bundle bundle = new Bundle();
            bundle.putString("contactusername", callList.get(info.position).getContact_name());
            bundle.putString("contactphone", callList.get(info.position).getContact_phone());
            bundle.putString("contactid", callList.get(info.position).getContact_id());
            bundle.putString("authtoken", authtoken);

            GroupChat groupChatFragment = new GroupChat();
            groupChatFragment.setArguments(bundle);

            getFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, groupChatFragment, "groupChatFragmentTag")
                    .addToBackStack(callList.get(info.position).getContact_name()).commit();
        }
        else
        {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see com.socialshare.custom.CustomFragment#onClick(android.view.View)
     */
    @Override
    public void onClick(View v)
    {
        super.onClick(v);
    }

    /**
     * This method currently loads a dummy list of Projects. You can write the
     * actual implementation of loading projects.
     */
    private void loadCallHistoryList()
    {

        DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
        try{

            ArrayList<CallHistoryItem> pList = new ArrayList<CallHistoryItem>();

            JSONArray chats = db.getCallHistory();

            for (int i=0; i < chats.length(); i++) {
                JSONObject row = chats.getJSONObject(i);

                pList.add(new CallHistoryItem(
                        row.getString("call_date"),
                        row.getString("type"),
                        row.getString("display_name"),
                        row.getString("contact_phone"),
                        row.getString("contact_id")));

            }

            callList = new ArrayList<CallHistoryItem>(pList);
            //callList.addAll(pList);
            //callList.addAll(pList);

        } catch(JSONException e){
            e.printStackTrace();
        }

    }

    /**
     * The Class CutsomAdapter is the adapter class for Projects ListView. The
     * currently implementation of this adapter simply display static dummy
     * contents. You need to write the code for displaying actual contents.
     */
    private class NoteAdapter extends BaseAdapter
    {

        /* (non-Javadoc)
         * @see android.widget.Adapter#getCount()
         */
        @Override
        public int getCount()
        {
            return callList.size();
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getItem(int)
         */
        @Override
        public CallHistoryItem getItem(int arg0)
        {
            return callList.get(arg0);
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getItemId(int)
         */
        @Override
        public long getItemId(int arg0)
        {
            return arg0;
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
         */
        @Override
        public View getView(int pos, View v, ViewGroup arg2)
        {
            if (v == null)
                v = LayoutInflater.from(getActivity()).inflate(
                        R.layout.call_history_item, null);

            CallHistoryItem c = getItem(pos);
            TextView lbl = (TextView) v.findViewById(R.id.lblContactDisplayName);
            lbl.setText(c.getContact_name());

            lbl = (TextView) v.findViewById(R.id.lbl2);
            lbl.setText(c.getDate());

            ImageView img = (ImageView) v.findViewById(R.id.callStatusIcon);
            //img.setImageResource(c.getIcon());
            if (c.getType().equals("missed"))
                img.setImageResource(android.R.drawable.sym_call_missed);
            else if(c.getType().equals("placed"))
                img.setImageResource(android.R.drawable.sym_call_outgoing);
            else
                img.setImageResource(android.R.drawable.sym_call_incoming);

            return v;
        }

    }

    public String getFragmentName()
    {
        return "CallHistory";
    }

    public String getFragmentContactPhone()
    {
        return "About Chat";
    }
}
