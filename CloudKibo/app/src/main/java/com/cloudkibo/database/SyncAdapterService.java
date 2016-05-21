package com.cloudkibo.database;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SyncAdapterService extends Service {

	private static final Object sSyncAdapterLock = new Object();
    private static CloudKiboSyncAdapter sSyncAdapter = null;
	
	@Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null)
                sSyncAdapter = new CloudKiboSyncAdapter(getApplicationContext(), true);
        }
    }
	
	@Override
	public IBinder onBind(Intent arg0) {
		return sSyncAdapter.getSyncAdapterBinder();
	}

}
