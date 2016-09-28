package com.cloudkibo.library;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.cloudkibo.MainActivity;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by sojharo on 25/08/2016.
 */
public class NetworkStateReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        Log.d("app","Network connectivity change");
        if(intent.getExtras()!=null) {
            NetworkInfo ni=(NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
            if(ni!=null && ni.getState()==NetworkInfo.State.CONNECTED) {
                Log.i("NetworkStateReceiver","Network "+ni.getTypeName()+" connected");
                //if(hasActiveInternetConnection(context)){
                    if (MainActivity.isVisible) {
                        //MainActivity.mainActivity.startSocketService();
                        MainActivity.mainActivity.reconnectSocket();
                        MainActivity.mainActivity.startSyncService();
                        //MainActivity.mainActivity.syncContacts();
                    }
               // }
            }
        }
        if(intent.getExtras().getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY,Boolean.FALSE)) {
            Log.i("NetworkStateReceiver","There's no network connectivity");
        }
    }

    // todo has bug in it. crashes the app.
    public static boolean hasActiveInternetConnection(Context context) {
        try {
            HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
            urlc.setRequestProperty("User-Agent", "Test");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(1500);
            urlc.connect();
            return (urlc.getResponseCode() == 200);
        } catch (IOException e) {
            Log.e("NetworkStateReceiver", "Error checking internet connection", e);
        }
        return false;
    }

}