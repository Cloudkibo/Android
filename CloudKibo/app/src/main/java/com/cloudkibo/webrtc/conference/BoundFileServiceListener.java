package com.cloudkibo.webrtc.conference;

import org.json.JSONArray;
import org.json.JSONObject;
import org.webrtc.DataChannel;

import java.util.ArrayList;

public interface BoundFileServiceListener {
	
	public void sendChat(String msg);

	public void sendDataChannelMessage(DataChannel.Buffer buf);

	public void receivedFileOffer(String message, int sizeOfFileToSave);

	public void fileTransferCompleteNotification(String name, ArrayList<Byte> fileBytesArray, String fileNameToSave);

}
