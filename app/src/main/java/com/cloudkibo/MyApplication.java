package com.cloudkibo;

import android.app.Application;
import android.content.Context;

import com.cloudkibo.R;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

/**
 * Created by sojharo on 07/07/2016.
 */

@ReportsCrashes(
        formUri = "https://cloudkibo1.cloudant.com/acra-cloudkibo/_design/acra-storage/_update/report",
        reportType = org.acra.sender.HttpSender.Type.JSON,
        httpMethod = org.acra.sender.HttpSender.Method.PUT,
        formUriBasicAuthLogin="pesserseencoulinedgeterr",
        formUriBasicAuthPassword="3c68c2ff8794c1180d1cd84c0b9205f81cc1e77d",
        // Your usual ACRA configuration
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text
)
public class MyApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }
}

