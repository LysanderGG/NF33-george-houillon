package com.example.nf33;

import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	private Sensor 		m_sensor;
	private TextView 	m_tvAxisX,
	                 	m_tvAxisY,
	                 	m_tvAxisZ,
	                 	m_tvLogButton;
	private Button		m_LogButton;
	private MyLogs 		m_History;
	
	private static final float	STEP_DETECTION_LIMIT	= 7.0f;
	private static final int 	HISTORY_MAX_LENGTH 		= 4096;
	private static final String LOG_FILENAME			= "NF33.log";
	private static final String TAG 					= MainActivity.class.getName();
	

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		m_tvAxisX 	= (TextView)findViewById(R.id.tv_axis_x);
		m_tvAxisY 	= (TextView)findViewById(R.id.tv_axis_y);
		m_tvAxisZ 	= (TextView)findViewById(R.id.tv_axis_z);
		m_LogButton = (Button)findViewById(R.id.buttonLog);
		m_tvLogButton = (TextView)findViewById(R.id.textViewButtonLog);

		m_sensor = new Sensor(this);
		SensorManager m = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		m.registerListener(m_sensor, SensorManager.SENSOR_ACCELEROMETER);

		m_History = new MyLogs(HISTORY_MAX_LENGTH);
		
		m_LogButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_History.writeLogFile(TAG, LOG_FILENAME);
				m_tvLogButton.setText("Logs saved.");
			}
		});
	}

	void handleMeasure(float _x, float _y, float _z)
	{
		m_tvAxisX.setText(getString(R.string.axis_x) + _x);
		m_tvAxisY.setText(getString(R.string.axis_y) + _y);
		m_tvAxisZ.setText(getString(R.string.axis_z) + _z);
		
		long _time = Calendar.getInstance().getTimeInMillis();
		m_History.add(_time, _x, _y, _z);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
