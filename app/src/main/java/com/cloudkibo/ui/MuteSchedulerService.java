package com.cloudkibo.ui;


import android.app.job.JobParameters;
import android.app.job.JobService;
import android.media.MediaPlayer;
import android.widget.Toast;

import com.cloudkibo.R;
import com.cloudkibo.database.DatabaseHandler;

import org.json.JSONException;

public class MuteSchedulerService extends JobService {
    DatabaseHandler db;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        db = new DatabaseHandler(this);
        String chatType = jobParameters.getExtras().getString("chatType");

        if (chatType.equals("onetoone")) {

            final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.bell);
            mp.start();

            String contactPhone = jobParameters.getExtras().getString("contactPhone");

            Toast.makeText(getApplicationContext(), contactPhone, Toast.LENGTH_SHORT).show();

            db.unMuteContact(contactPhone);
        } else if(chatType.equals("groupchat")){
            final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.bell);
            mp.start();

            String groupid = jobParameters.getExtras().getString("groupid");
            Toast.makeText(getApplicationContext(), groupid, Toast.LENGTH_SHORT).show();

            db.unmuteGroup(groupid);
        }
        
        if(chatType.equals("remove")){
            final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.bell);
            mp.start();
            String statusID = jobParameters.getExtras().getString("uniqueid");
            Toast.makeText(getApplicationContext(), statusID, Toast.LENGTH_SHORT).show();
            db.deleteDaystatus(statusID);
        }








        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }
}
