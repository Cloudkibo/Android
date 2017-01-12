package com.cloudkibo.backup;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.media.MediaPlayer;
import android.util.Log;

import com.cloudkibo.R;

/**
 * Created by sojharo on 08/01/2017.
 */

public class JobSchedulerService extends JobService {

    private static final String TAG = "SyncService";


    // todo this for testing only and will make the beep sound on defined intervals
    // We would use the wrapper class to talk to Google Drive to do backups when user
    // selects to do backup on interval basis - wrapper class is under construction
    // This will call the methods of the wrapper class to talk to Google Drive
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.i(TAG, "on start job: " + jobParameters.getJobId());

        final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.bell);
        mp.start();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }
}
