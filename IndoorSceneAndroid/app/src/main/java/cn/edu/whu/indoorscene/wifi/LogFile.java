package cn.edu.whu.indoorscene.wifi;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.channels.FileChannel;


public class LogFile {
	
	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;

	public static String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
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
}

