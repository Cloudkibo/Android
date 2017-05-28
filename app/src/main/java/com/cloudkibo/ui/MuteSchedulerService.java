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
        final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.bell);
        mp.start();

        String contactPhone = jobParameters.getExtras().getString("contactPhone");

        Toast.makeText(getApplicationContext(), contactPhone, Toast.LENGTH_SHORT).show();



        db.unMuteContact(contactPhone);




        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }
}
