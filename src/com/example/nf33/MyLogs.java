package com.example.nf33;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import android.os.Environment;
import android.util.Log;

public class MyLogs {
	
	private class LogItem {
		private float[] m_coords;
		private long 	m_uTime;
		
		public LogItem(float[] _coords, long _time) {
			m_coords 	= _coords;
			m_uTime 	= _time;
		}
	}
	
	private int					m_iLength;
	private ArrayList<LogItem>	m_list;
	
	public MyLogs(int _length) {
		m_iLength 	= (_length > 0) ? _length : 0;
		m_list 		= new ArrayList<LogItem>();
	}
	
	public void add(long _time, float _x, float _y, float _z) {
		if (m_iLength > 0 && m_list.size() >= m_iLength) {
			// Rotation de l'historique
			m_list.remove(m_list.size()-1);
		}
		
		m_list.add(0, new LogItem(new float[] {_x, _y, _z}, _time));
	}
	
	public void writeLogFile(String _tag, String _filename) {
		try {
			String txt = "Accelerometer datas logs - " + Calendar.getInstance().toString() + "\n";
			for(LogItem li : m_list) {
				txt += "" + li.m_uTime + ";"; 
				for(float f : li.m_coords) {
					txt += "" + f + ";";
				}
				txt += "\n";
			}
            
			// Check External Media
			boolean mExternalStorageAvailable = false;
		    boolean mExternalStorageWriteable = false;
		    String state = Environment.getExternalStorageState();

		    if(Environment.MEDIA_MOUNTED.equals(state)) {
		        // Can read and write the media
		        mExternalStorageAvailable = mExternalStorageWriteable = true;
		    } else if(Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		        // Can only read the media
		        mExternalStorageAvailable = true;
		        mExternalStorageWriteable = false;
		    } else {
		        // Can't read or write
		        mExternalStorageAvailable = mExternalStorageWriteable = false;
		    }
			Log.i(_tag, "\n\nExternal Media: readable=" + mExternalStorageAvailable + " writable=" + mExternalStorageWriteable);
			
			// Write file
            File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File dir = new File (root, "NF33_data");
            if(!dir.mkdirs()) {
            	Log.i(_tag, "Unable to create " + dir.getAbsolutePath());
            }
            File file = new File(dir, _filename);
            
            if(!file.exists()) {
               try {
                  file.createNewFile();
               } catch (IOException e) {
            	   Log.i(_tag, "Unable to create " + file.getAbsolutePath());
               }
            }
            
            try {
            	BufferedWriter buf = new BufferedWriter(new FileWriter(file, false)); 
                buf.append(txt);
                buf.newLine();
                buf.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.i(_tag, "******* File not found. Did you add a WRITE_EXTERNAL_STORAGE permission to the manifest?");
            }
        }
        catch (IOException e) {
        	e.printStackTrace();
            Log.e(_tag, "File write failed: " + e.toString());
        }
	}
	
}
