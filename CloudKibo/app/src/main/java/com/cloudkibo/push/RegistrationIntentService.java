package com.cloudkibo.push;

/**
 * Created by sojharo on 20/08/2016.
 */
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.cloudkibo.MainActivity;
import com.cloudkibo.database.DatabaseHandler;
import com.cloudkibo.library.Utility;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.microsoft.windowsazure.messaging.NotificationHub;

import java.util.HashMap;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";

    private NotificationHub hub;

    public RegistrationIntentService() {
        super(TAG);
    }

    private HashMap<String, String> user;

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String resultString = null;
        String regID = null;

        try {
            DatabaseHandler db = new DatabaseHandler(getApplicationContext());
            user = db.getUserDetails();

            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(NotificationSettings.SenderId,
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE);
            Log.i(TAG, "Got GCM Registration Token: " + token);

            // Storing the registration id that indicates whether the generated token has been
            // sent to your server. If it is not stored, send the token to your server,
            // otherwise your server should have already received the token.
            if ((regID=sharedPreferences.getString("registrationID", null)) == null) {
                NotificationHub hub = new NotificationHub(NotificationSettings.HubName,
                        NotificationSettings.HubListenConnectionString, this);
                Log.i(TAG, "Attempting to register with NH using token : " + token);

                String phone = user.get("phone").substring(1);

                regID = hub.register(token, user.get("phone").substring(1)).getRegistrationId();

                // If you want to use tags...
                // Refer to : https://azure.microsoft.com/en-us/documentation/articles/notification-hubs-routing-tag-expressions/
                // regID = hub.register(token, "tag1", "tag2").getRegistrationId();

                resultString = "Registered Successfully - RegId : " + regID;
                Log.i(TAG, resultString);
                sharedPreferences.edit().putString("registrationID", regID ).apply();
            } else {
                resultString = "Previously Registered Successfully - RegId : " + regID;
            }
        } catch (Exception e) {
            Log.e(TAG, resultString="Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
        }

        // Notify UI that registration has completed.
        if (MainActivity.isVisible) {
            MainActivity.mainActivity.ToastNotify2(resultString);
            Utility.sendLogToServer(resultString+ " FOR NUMBER: "+ user.get("phone").substring(1));
        }
    }
}
