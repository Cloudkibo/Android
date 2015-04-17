package com.cloudkibo.webrtc.filesharing;

import java.io.File;
import java.nio.ByteBuffer;

import org.json.JSONException;
import org.json.JSONObject;

import com.cloudkibo.file.filechooser.utils.FileUtils;

public class Utility {

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
	
	
}
