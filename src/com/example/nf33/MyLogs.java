package com.example.nf33;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import android.os.Environment;
import android.util.Log;

public class MyLogs {

	public class LogItem {
		private final float[] 	m_coords;
		private final long 		m_lTime;
		private boolean 		m_bIsStepDetected;

		public LogItem(float[] _coords, boolean _isStepDetected, long _time) {
			m_coords 			= _coords;
			m_bIsStepDetected	= _isStepDetected;
			m_lTime 			= _time;
		}

		public float[] getCoords() {
			return m_coords;
		}

		public float getX() {
			return m_coords[0];
		}

		public float getY() {
			return m_coords[1];
		}

		public float getZ() {
			return m_coords[2];
		}

		public boolean getIsStepDetected() {
			return m_bIsStepDetected;
		}
		
		public long getTime() {
			return m_lTime;
		}
	
		public void setIsStepDetected(boolean _value) {
			m_bIsStepDetected = _value;
		}
	}

	private final int					m_iLength;
	private final ArrayList<LogItem>	m_list;

	public ArrayList<LogItem> getList() {
		return m_list;
	}

	public MyLogs(int _length) {
		m_iLength 	= (_length > 0) ? _length : 0;
		m_list 		= new ArrayList<LogItem>(m_iLength);
	}

	public void add(long _time, float _x, float _y, float _z) {
		add(_time, _x, _y, _z, false);
	}
	
	public void add(long _time, float _x, float _y, float _z, boolean _isStepDetected) {
		if (m_iLength > 0 && m_list.size() >= m_iLength) {
			// Rotation de l'historique
			m_list.remove(m_list.size()-1);
		}

		m_list.add(0, new LogItem(new float[] {_x, _y, _z}, _isStepDetected, _time));
	}

	public boolean writeLogFile(String _tag, String _filename) {
		try {
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

			// Création des dossiers
            File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File dir = new File (root, "NF33_data");
            if (!dir.exists()) {
            	if (!dir.mkdirs()) {
            		Log.i(_tag, "Unable to create " + dir.getAbsolutePath());
            		return false;
            	}
            }

            // Création du fichier
            File file = new File(dir, _filename);

            if (!file.exists()) {
               try {
                  file.createNewFile();
               } catch (IOException e) {
            	   Log.i(_tag, "Unable to create " + file.getAbsolutePath());
            	   return false;
               }
            }

            // Génération des données texte
			String txt = "timestamp;X;Y;Z;StepDetected\n";
			for (LogItem li : m_list) {
				txt += String.format(
					"%d;%f;%f;%f;%d\n",
					li.m_lTime,
					li.m_coords[0],
					li.m_coords[1],
					li.m_coords[2],
					(li.m_bIsStepDetected) ? 1 : 0
				);
			}
			// Pour Excel, LibreOffice...
			//txt = txt.replace('.', ',');
            try {
            	BufferedWriter buf = new BufferedWriter(new FileWriter(file, false));
                buf.append(txt);
                buf.newLine();
                buf.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.i(_tag, "File not found");
                return false;
            }
        }
        catch (IOException e) {
        	e.printStackTrace();
            Log.e(_tag, "File write failed: " + e.toString());
            return false;
        }
		return true;
	}
	
	public void clear() {
		m_list.clear();
	}
	
	/**
	 * Add a StepDectected to the last LogItem
	 */
	public void addStepDetected() {
		m_list.get(0).setIsStepDetected(true);
	}
}
