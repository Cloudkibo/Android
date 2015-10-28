package com.cloudkibo.webrtc.conference;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.cloudkibo.file.filechooser.utils.FileUtils;
import com.cloudkibo.socket.BoundServiceListener;
import com.cloudkibo.webrtc.filesharing.Utility;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class FileTransferService extends Service {

    private String filePath;
    private String fileNameToSave;
    private int numberOfChunksInFileToSave;
    private int numberOfChunksReceived;
    private int sizeOfFileToSave;
    private int chunkNumberToRequest;
    private ArrayList<Byte> fileBytesArray = new ArrayList<Byte>();

    private final IBinder fileTransferBinder = new FileTransferBinder();
    private BoundFileServiceListener mListener;

    private File file;

    @Override
    public IBinder onBind(Intent intent) {
        return fileTransferBinder;
    }

    public void sendFile(String fp){
        filePath = fp;
        // Alternatively, use FileUtils.getFile(Context, Uri)
        if (filePath != null && FileUtils.isLocal(filePath)) {

            file = new File(filePath);

            try {
                JSONObject metadata = new JSONObject();

                metadata.put("eventName", "data_msg");
                metadata.put("data", (new JSONObject()).put("file_meta", Utility.getFileMetaData(filePath)));

                mListener.sendDataChannelMessage(new DataChannel.Buffer(Utility.toByteBuffer(metadata.toString()), false));

                mListener.sendChat("You have received a file. Download and Save it.");

                Log.w("FILE_TRANSFER", "Sending file meta to peer");
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
    }

    public void onDataChannelMessage(DataChannel.Buffer buffer){
        Log.w("FILE_TRANSFER", "Data Channel message received");

        ByteBuffer data = buffer.data;
        final byte[] bytes = new byte[ data.capacity() ];
        data.get(bytes);

        if(buffer.binary){

            String strData = new String( bytes );
            Log.w("FILE_TRANSFER", strData);

            for(int i=0; i<bytes.length; i++)
                fileBytesArray.add(bytes[i]);

            if (numberOfChunksReceived % Utility.getChunksPerACK() == (Utility.getChunksPerACK() - 1)
                    || numberOfChunksInFileToSave == (numberOfChunksReceived + 1)) {
                if (numberOfChunksInFileToSave > numberOfChunksReceived) {
                    chunkNumberToRequest += Utility.getChunksPerACK();
                    Log.w("FILETRANSFER", "Asking other chunk");
                    requestChunk();
                }
            } else {
                if (numberOfChunksInFileToSave == numberOfChunksReceived) {
                    Log.w("FILETRANSFER", "File transfer completed");
                    mListener.fileTransferCompleteNotification(fileNameToSave + " : " +
                            FileUtils.getReadableFileSize(sizeOfFileToSave), fileBytesArray, fileNameToSave);
                }
            }

            numberOfChunksReceived++;

        } else {

            String strData = new String(bytes);

            if(strData.equals("Speaking") || strData.equals("Silent"))
                return ;

            Log.w("FILE_TRANSFER", strData);

            try {

                JSONObject jsonData = new JSONObject(strData);

                if (jsonData.getJSONObject("data").has("file_meta")) {

                    fileNameToSave = jsonData.getJSONObject("data").getJSONObject("file_meta").getString("name");
                    sizeOfFileToSave = jsonData.getJSONObject("data").getJSONObject("file_meta").getInt("size");
                    numberOfChunksInFileToSave = (int) Math.ceil(sizeOfFileToSave / Utility.getChunkSize());
                    numberOfChunksReceived = 0;
                    chunkNumberToRequest = 0;
                    fileBytesArray = new ArrayList<Byte>();

                    mListener.receivedFileOffer(fileNameToSave + " : " + FileUtils.getReadableFileSize(sizeOfFileToSave), sizeOfFileToSave);

                } else if (jsonData.getJSONObject("data").has("kill")) {

                } else if (jsonData.getJSONObject("data").has("ok_to_download")) {

                } else {

                    boolean isBinaryFile = true;

                    int chunkNumber = jsonData.getJSONObject("data").getInt("chunk");

                    Log.w("FILE_TRANSFER", "Chunk Number " + chunkNumber);
                    if (chunkNumber % Utility.getChunksPerACK() == 0) {
                        for (int i = 0; i < Utility.getChunksPerACK(); i++) {

                            if (file.length() < Utility.getChunkSize()) {
                                ByteBuffer byteBuffer = ByteBuffer.wrap(Utility.convertFileToByteArray(file));
                                DataChannel.Buffer buf = new DataChannel.Buffer(byteBuffer, isBinaryFile);

                                Log.w("FILE_TRANSFER", "File Smaller than chunk size condition");

                                mListener.sendDataChannelMessage(buf);
                                break;
                            }

                            Log.w("FILE_TRANSFER", "File Length " + file.length());
                            Log.w("FILE_TRANSFER", "Ceiling " + Math.ceil(file.length() / Utility.getChunkSize()));
                            if ((chunkNumber + i) >= Math.ceil(file.length() / Utility.getChunkSize())) {
                                Log.w("FILE_TRANSFER", "Came into math ceiling condition");
                                //break;
                            }

                            int upperLimit = (chunkNumber + i + 1) * Utility.getChunkSize();

                            if (upperLimit > (int) file.length()) {
                                upperLimit = (int) file.length() - 1;
                            }

                            int lowerLimit = (chunkNumber + i) * Utility.getChunkSize();
                            Log.w("FILE_TRANSFER", "Limits: " + lowerLimit + " " + upperLimit);

                            if (lowerLimit > upperLimit)
                                break;

                            ByteBuffer byteBuffer = ByteBuffer.wrap(Utility.convertFileToByteArray(file), lowerLimit, upperLimit - lowerLimit);
                            DataChannel.Buffer buf = new DataChannel.Buffer(byteBuffer, isBinaryFile);

                            mListener.sendDataChannelMessage(buf);
                            Log.w("FILE_TRANSFER", "Chunk has been sent");
                        }
                    }
                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    public void requestChunk(){
        Log.w("FILERECEIVE", "Requesting CHUNK: "+ chunkNumberToRequest);
        JSONObject request_chunk = new JSONObject();
        try {
            request_chunk.put("eventName", "request_chunk");
            JSONObject request_data = new JSONObject();
            request_data.put("chunk", chunkNumberToRequest);
            request_data.put("browser", "chrome"); // This chrome is hardcoded for testing purpose
            request_chunk.put("data", request_data);
            mListener.sendDataChannelMessage(new DataChannel.Buffer(Utility.toByteBuffer(request_chunk.toString()), false));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class FileTransferBinder extends Binder {

        public FileTransferService getService() {
            return FileTransferService.this;
        }

        public void setListener(BoundFileServiceListener listener) {
            mListener = listener;
        }

    }
}
