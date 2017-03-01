package com.cloudkibo.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.library.GroupUtility;
import com.cloudkibo.library.UserFunctions;
import com.cloudkibo.utils.IFragmentName;

import org.json.JSONObject;

import java.util.Date;

public class ChangeGroupName extends CustomFragment implements IFragmentName {
    UserFunctions userFunctions;
    private String authtoken;
    String group_id;
    String userInput = "";
    DatabaseHandler db;
    EditText newName;
    Button changeName;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.group_name_change, null);
        setHasOptionsMenu(true);
        userFunctions = new UserFunctions(getContext());

        authtoken = getActivity().getIntent().getExtras().getString("authtoken");
        Bundle args = this.getArguments();
        if(args != null){
            group_id = args.getString("groupid");
        }

        db = new DatabaseHandler(getContext());

        newName = (EditText) v.findViewById(R.id.inputname);
        changeName = (Button) v.findViewById(R.id.changeBtn);

        changeName.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                userInput =  newName.getText().toString();
                if(userInput.equals("")){
                    return;
                }
                updateGroupName(userInput);
                newName.setText("");

            }
        });

        return v;
    }

    public void updateGroupName(final String name){
        new AsyncTask<String, String, JSONObject>(){
            @Override
            protected JSONObject doInBackground(String... strings) {
                return userFunctions.sendChangedGroupName(group_id, name, authtoken);
            }

            @Override
            protected void onPostExecute(JSONObject row) {
                if(row!=null) {
                    if (row.optString("Error").equals("No Internet")) {
                        sendNotification("Group Name can only be updated while internet is available");
                    }
                    else {
                        sendNotification("Group Name is changed");
                        try{
                            db.updateGroupName(group_id, name);
                            final String unique_id = randomString();
                            db.addGroupMessage(group_id, "Group Name was updated to " +name+ " by " + "You" , db.getUserDetails().get("phone"),"", unique_id, "log");

                        } catch (Exception e){
                            e.printStackTrace();
                        }

                        ChatList cList = new ChatList();
                        Bundle bundle = new Bundle();
                        bundle.putString("authtoken", authtoken);
                        cList.setArguments(bundle);
                        getFragmentManager().beginTransaction()
                                .replace(R.id.content_frame, cList, "ChatListTag")
                                .commit();
                    }
                }
            }
        }.execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();


    }

    private void sendNotification(String log){
        Toast.makeText(getContext(),log,Toast.LENGTH_SHORT).show();
    }
    public String randomString() {
        String uniqueid = Long.toHexString(Double.doubleToLongBits(Math.random()));
        uniqueid += (new Date().getYear()) + "" + (new Date().getMonth()) + "" + (new Date().getDay());
        uniqueid += (new Date().getHours()) + "" + (new Date().getMinutes()) + "" + (new Date().getSeconds());
        return uniqueid;
    }

    @Override
    public String getFragmentName() {
        return "ChangeGroupName";
    }

    @Override
    public String getFragmentContactPhone() {
        return "About Chat";
    }
}
