package com.cloudkibo.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.cloudkibo.MainActivity;
import com.cloudkibo.R;
import com.cloudkibo.custom.CustomFragment;
import com.cloudkibo.utils.IFragmentName;

import java.util.ArrayList;
import java.util.List;


public class BackupSetting extends CustomFragment implements IFragmentName{

    String backup_drive_options[];
    String backup_drive_selected_option = "";
    String backup_over_options[];
    String backup_over_selected_option = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_backup_setting, container, false);
        backup_drive_selected_option = getString(R.string.never);
        backup_over_selected_option = getString(R.string.wifi_only);
        backup_drive_options = new String[]{getString(R.string.never), getString(R.string.only_when_i_tap_back), getString(R.string.daily), getString(R.string.weekly), getString(R.string.monthly)};
        backup_over_options = new String[]{getString(R.string.wifi_only), getString(R.string.wifi_cellular)};
        updateDefaultValues(v);

        LinearLayout drive_backup = (LinearLayout) v.findViewById(R.id.drive_backup);
        drive_backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showGoogleDriveDialog(v);
            }
        });

        LinearLayout backup_over = (LinearLayout) v.findViewById(R.id.backup_over);
        backup_over.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBackupOverDialog(v);
            }
        });
        return v;
    }

    private void updateDefaultValues(View v){
        TextView drive_backup_text = (TextView) v.findViewById(R.id.drive_backup_text);
        drive_backup_text.setText(backup_drive_selected_option);

        TextView backup_over_text = (TextView) v.findViewById(R.id.backup_over_text);
        backup_over_text.setText(backup_over_selected_option);
        SharedPreferences prefs = getActivity().getSharedPreferences(
                "com.cloudkibo", Context.MODE_PRIVATE);

        prefs.edit().putString("com.cloudkibo.drive_backup_text", backup_drive_selected_option).apply();
        prefs.edit().putString("com.cloudkibo.drive_over_text", backup_over_selected_option).apply();

    }

    private void showBackupOverDialog(final View v) {

        // custom dialog
        final Dialog dialog = new Dialog(MainActivity.mainActivity);
        dialog.setTitle("Back up Over");
        dialog.setContentView(R.layout.drive_backup_dialog);

        List<String> stringList=new ArrayList<>();  // here is list
        for (int i=0; i<backup_over_options.length; i++){
            stringList.add(backup_over_options[i]);
        }

        final RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.radio_group);
        Button cancel = (Button) dialog.findViewById(R.id.cancel_drive_backup_dialog);
        for(int i=0;i<stringList.size();i++){
            RadioButton rb=new RadioButton(MainActivity.mainActivity); // dynamically creating RadioButton and adding to RadioGroup.
            rb.setText(stringList.get(i));
            rb.setPadding(20,20,20,20);
            rg.addView(rb);
            if(stringList.get(i).equals(backup_over_selected_option)){
                rb.setChecked(true);
            }
        }
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                RadioButton radioButton = (RadioButton) rg.findViewById(checkedId);
                backup_over_selected_option = radioButton.getText().toString();
                updateDefaultValues(v);
                dialog.cancel();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        dialog.show();

    }

    private void showGoogleDriveDialog(final View v) {

        // custom dialog
        final Dialog dialog = new Dialog(MainActivity.mainActivity);
        dialog.setTitle(R.string.backup_to_google_drive);
        dialog.setContentView(R.layout.drive_backup_dialog);

        List<String> stringList=new ArrayList<>();  // here is list
        for (int i=0; i<backup_drive_options.length; i++){
            stringList.add(backup_drive_options[i]);
        }

        final RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.radio_group);
        Button cancel = (Button) dialog.findViewById(R.id.cancel_drive_backup_dialog);
        for(int i=0;i<stringList.size();i++){
            RadioButton rb=new RadioButton(MainActivity.mainActivity); // dynamically creating RadioButton and adding to RadioGroup.
            rb.setText(stringList.get(i));
            rb.setPadding(20,20,20,20);
            rg.addView(rb);
            if(stringList.get(i).equals(backup_drive_selected_option)){
                rb.setChecked(true);
            }
        }
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                RadioButton radioButton = (RadioButton) rg.findViewById(checkedId);
                backup_drive_selected_option = radioButton.getText().toString();
                updateDefaultValues(v);
                dialog.cancel();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        dialog.show();

    }


    public String getFragmentName()
    {
        return "Backup Settings";
    }

    public String getFragmentContactPhone()
    {
        return "About Chat";
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
