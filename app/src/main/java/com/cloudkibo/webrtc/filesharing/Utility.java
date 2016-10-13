package com.cloudkibo.webrtc.filesharing;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Environment;
import android.util.Log;

import com.cloudkibo.file.filechooser.utils.FileUtils;

public class Utility {
	
	private static final int CHUNK_SIZE = 16000;
	private static final int CHUNKS_PER_ACK = 16;
	private static final String FOLDER_NAME = "CloudKibo";

	public static byte[] convertFileToByteArray(File f) {
		
		java.io.FileInputStream fis = null;
		byte[] stream = new byte[(int) f.length()];
		try {
			fis = new java.io.FileInputStream(f);
		} catch (java.io.FileNotFoundException ex) {
			return null;
		}
		try {
			fis.read(stream);
			fis.close();
		} catch (java.io.IOException ex) {
			return null;
		}
		return stream;
	}
	
	public static Boolean convertByteArrayToFile(byte[] bytes, String fileName){
		
		try {
			
			if(isExternalStorageWritable()){
			
				String folderName = FOLDER_NAME;
				//convert array of bytes into file
			    FileOutputStream fileOuputStream = 
		                  new FileOutputStream(getDownloadStorageDir(folderName)+"/"+fileName); 
			    fileOuputStream.write(bytes);
			    fileOuputStream.close();
			    
			    return true;
			}
			else{
				return false;
			}
	 
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
		
	}
	
	public static JSONObject getFileMetaData(String filePath){
		
		File file = new File(filePath);
		
		JSONObject meta = new JSONObject();
		
		try {
			
			meta.put("name", file.getName());
			meta.put("size", file.length());
			meta.put("filetype", FileUtils.getExtension(filePath));
			meta.put("browser", "chrome"); // This is a hack. Will remove it later.
			
		} catch (JSONException e) {
			e.printStackTrace();
			
			return meta; // This will contain some missing information. Client should know about exception from this
			
		}
		
		return meta;
		
	}
	
	public static ByteBuffer toByteBuffer(String text){
		return ByteBuffer.wrap(text.getBytes());
	}
	
	public static int getChunkSize(){
		return CHUNK_SIZE;
	}
	
	public static int getChunksPerACK(){
		return CHUNKS_PER_ACK;
	}
	
	/* Checks if external storage is available for read and write */
	public static boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}

	/* Checks if external storage is available to at least read */
	public static boolean isExternalStorageReadable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) ||
	        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    }
	    return false;
	}
	
	public static boolean isFreeSpaceAvailableForFileSize(int fileSize){
		 File file = new File(Environment.getExternalStoragePublicDirectory(
		            Environment.DIRECTORY_DOWNLOADS), FOLDER_NAME);
		    if (!file.mkdirs()) {
		        Log.e("FILESTORAGE", "Directory not created");
		    }
		    
		    if(fileSize < (file.getFreeSpace() - 10000000)){
		    	return true;
		    }
		    
		    return false;
	}
	
	public static File getDownloadStorageDir(String foldername) {
	    // Get the directory for the user's public pictures directory. 
	    File file = new File(Environment.getExternalStoragePublicDirectory(
	            Environment.DIRECTORY_DOWNLOADS), foldername);
	    if (!file.mkdirs()) {
	        Log.e("FILESTORAGE", "Directory not created");
	    }
	    return file;
	}
	
}
