package com.example.nf33;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

public class MyLogs {
	
	private class LogItem {
		private float[] m_coords;
		private int 	m_time;
		
		public LogItem(float[] _coords, int _time) {
			m_coords 	= _coords;
			m_time 		= _time;
		}
	}
	
	private int					m_length;
	private ArrayList<LogItem>	m_list;
	
	public MyLogs(int _length) {
		m_length 	= (_length > 0) ? _length : 0;
		m_list 		= new ArrayList<LogItem>();
	}
	
	public void add(int _time, float _x, float _y, float _z) {
		if (m_length > 0 && m_list.size() >= m_length) {
			// Rotation de l'historique
			m_list.remove(m_list.size()-1);
		}
		
		m_list.add(0, new LogItem(new float[] {_x, _y, _z}, _time));
	}
	
	public void WriteLogFile(Context _context, String _tag, String _filename, String _data) {
		try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(_context.openFileOutput(_filename, Context.MODE_PRIVATE));
            outputStreamWriter.write(_data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e(_tag, "File write failed: " + e.toString());
        }
	}
	
}
