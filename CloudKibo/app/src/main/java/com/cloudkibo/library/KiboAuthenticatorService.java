package com.cloudkibo.library;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class KiboAuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {

        KiboAuthenticator authenticator = new KiboAuthenticator(this);
        return authenticator.getIBinder();
    }
}
