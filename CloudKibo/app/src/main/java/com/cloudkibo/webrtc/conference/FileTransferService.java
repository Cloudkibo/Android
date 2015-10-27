package com.cloudkibo.webrtc.conference;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.cloudkibo.socket.BoundServiceListener;

public class FileTransferService extends Service {

    private final IBinder fileTransferBinder = new FileTransferBinder();
    private BoundServiceListener mListener;

    @Override
    public IBinder onBind(Intent intent) {
        return fileTransferBinder;
    }

    public class FileTransferBinder extends Binder {

        public FileTransferService getService() {
            return FileTransferService.this;
        }

        public void setListener(BoundServiceListener listener) {
            mListener = listener;
        }

    }
}
