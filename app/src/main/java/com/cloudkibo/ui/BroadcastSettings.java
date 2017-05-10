package com.cloudkibo.ui;


import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.library.GroupUtility;
import com.cloudkibo.utils.IFragmentName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class BroadcastSettings extends CustomFragment implements IFragmentName {

    private String authtoken;
    Context context;
    String bList_id;
    ListView lv;
    CharSequence [] contactList;
    String [] phoneList;
    JSONArray participants;
    LayoutInflater inflater;
    ImageButton btnSelectIcon;
    Button delete_list;
    TextView name;
    View view;
    String list_name;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        //// TODO: 5/10/17 Please define layout items for adding and deleting participants

        View vg = inflater.inflate(R.layout.broadcast_settings, null);
        lv=(ListView) vg.findViewById(R.id.listView);
        setHasOptionsMenu(true);
        this.inflater = inflater;
        authtoken = getActivity().getIntent().getExtras().getString("authtoken");
        Bundle args = getArguments();
        if (args  != null){
            bList_id = args.getString("bList_id");
            list_name = args.getString("list_name");
        }



        delete_list = (Button) vg.findViewById(R.id.delete_list);



        delete_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 5/10/17 Add code to delete list
                Toast.makeText(getContext(), "Delete clicked", Toast.LENGTH_SHORT).show();
                deleteList(bList_id);
            }
        });

        return vg;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.findItem(R.id.archived).setVisible(false);
            menu.findItem(R.id.search_chats).setVisible(false);
            menu.findItem(R.id.settings).setVisible(false);
            menu.findItem(R.id.connect_to_desktop).setVisible(false);
            menu.findItem(R.id.broadcast).setVisible(false);
        }
        inflater.inflate(R.menu.broadcastsetting, menu);
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setTitle(list_name.toUpperCase());
        actionBar.setDisplayShowCustomEnabled(false); // Use filter.xml from step 1

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.listNameChange){
            // TODO: 5/10/17 Add code to change list name
            Toast.makeText(getContext(), "Change name clicked", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v)
    {
        super.onClick(v);
    }

    public void deleteList(String group_id){
        DatabaseHandler db = new DatabaseHandler(getContext());
        db.DeleteBroadCastList(bList_id);
        ChatList nextFrag= new ChatList();
//        Bundle args = new Bundle();
//        args.putString("group_id", group_id);
//        nextFrag.setArguments(args);
        this.getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, nextFrag,null)
                .addToBackStack(null)
                .commit();
    }





    @Override
    public String getFragmentName() {
        return "BroadcastSettings";
    }

    @Override
    public String getFragmentContactPhone() {
        return "About Chat";
    }
}
