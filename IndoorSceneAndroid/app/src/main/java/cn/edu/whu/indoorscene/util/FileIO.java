/*
 * Copyright (c) 2016 Wuhan University
 * Author: Jingbin Liu
 */

package cn.edu.whu.indoorscene.util;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.channels.FileChannel;


public class FileIO {

	public static final String EXTERNAL_APP_FOLDER = "@indoor_data";
	public static final String DATA_COLLECTION_FOLDER = "data";
	public static final String DATA_ZIPPED_FOLDER = "zipped";
	public static final String DATA_MAP_FOLDER = "map";
	public static final String DATA_DELETED_FOLDER = "deleted_data";
	
	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;

	// store the directory of the SD card.
	public static String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
	//public static String baseDir = Environment.getDataDirectory().getAbsolutePath();
	//public static String baseDirInternal = Environment.getDataDirectory().getAbsolutePath();
		
	// specify output file name. TODO: Instead of hardcoding this, the value should be taken from the UI and stamped with a timetag. -DONE! - see below.
	//public static File dir = new File(baseDirInternal); 
	//private static SimpleDateFormat dateTimeForFilename = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	//public static String dateTimeForFilenameAsString = dateTimeForFilename.format(System.currentTimeMillis());
	
	//public static File dir = new File(baseDir + "/" + dateTimeForFilenameAsString);
	
	File file;
	
	public static File newDir(String dirname){
		File file = new File(baseDir + "/" + dirname); 
		file.mkdirs();
		return file;
	}


	public boolean checkIfExternalStorageIsAvailable() {
		
		String state = Environment.getExternalStorageState();
	
		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		return mExternalStorageAvailable;
	}
	
	static void writeToFile(StringBuilder sb, File file) {

		String logEntry = sb + "";
		// Execute writing of results to external storage.
	    writeFileToStorage(logEntry, file);
		
	}
	
	static void writeToFileAsString(String s, File file) {

		
		// Execute writing of results to external storage.
	    writeFileToStorage(s, file);
		
	}


	public static void writeFileToStorage(String contents, File file) {
		writeFileToStorage(contents, file, true);
	}
	
	
	public static void writeFileToStorage(String contents, File file, boolean append) {
		  	
		    // Get end-of-line separator for system (On Linux systems this is always \n, but it is better to check...
		  	String eol = System.getProperty("line.separator");
		  
		  	Writer writer = null;
		  	
		  	// Append result to file, or print exception if fail.
	      	try {
	      		writer = new PrintWriter(new BufferedWriter(new FileWriter(file, append)));
	      		//
	      		writer.write(contents + eol);
	      		writer.close();
	      	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
		  	
	  }
	
	public static void copyFile(File src, File dst) throws IOException
	{
	    FileChannel inChannel = new FileInputStream(src).getChannel();
	    FileChannel outChannel = new FileOutputStream(dst).getChannel();
	    try
	    {
	        inChannel.transferTo(0, inChannel.size(), outChannel);
	    }
	    finally
	    {
	        if (inChannel != null)
	            inChannel.close();
	        if (outChannel != null)
	            outChannel.close();
	    }
	}
	
	public static void scanNewFiles(File file, Context context){
		String paths[] = {file.getPath()};
		MediaScannerConnection.scanFile(context, paths, null, new MediaScannerConnection.OnScanCompletedListener() {
		      public void onScanCompleted(String path, Uri uri) {
		          Log.i("ExternalStorage", "Scanned " + path + ":");
		          Log.i("ExternalStorage", "-> uri=" + uri);
		      } 
		});
	}

	//Utility function to find and recovery files that may be "lost" on the internal storage.
	public static void findFiles(){
		final File dir = new File("/data/data/com.contextawareness.contextawaresensors/");
		
		File recoveryDir = newDir("recovery");
		recoveryDir.mkdirs();
		for(final File file : dir.listFiles()){ 
			String filePath = file.getPath();
			String fileName = file.getName();
			Log.d("findFiles", filePath);
			try {
				copyFile(file, new File(recoveryDir,fileName));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}

