package com.cloudkibo.database;

/**
 * Created by sojharo on 6/16/16.
 */
public interface BoundKiboSyncListener {

    public void contactsLoaded();

    public void chatLoaded();

    public void sendPendingMessageUsingSocket(String contactPhone, String msg, String uniqueid);

    public void sendMessageStatusUsingSocket(String contactPhone, String status, String uniqueid);

}
