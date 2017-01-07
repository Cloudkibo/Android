package com.cloudkibo.backup;

import android.app.job.JobParameters;
import android.app.job.JobService;

/**
 * Created by sojharo on 08/01/2017.
 */

public class JobScheduler extends JobService {


    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }
}
