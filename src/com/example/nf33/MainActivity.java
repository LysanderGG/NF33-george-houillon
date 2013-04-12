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
	                 	m_tvLogButton,
	                 	m_tvStepsCounter;
	private Button		m_logButton;
	private MyLogs 		m_history;
	private float		m_fLastMax			= 0.0f;
	private	long		m_lLastMaxTime		= 0;
	private int			m_iStepsCounter		= 0;
	
	private static final float	STEP_DETECTION_LIMIT	= 3.0f;
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
		m_tvLogButton 		= (TextView)findViewById(R.id.tv_log_button);
		m_tvStepsCounter	= (TextView)findViewById(R.id.tv_steps_counter);
				
		m_logButton = (Button)findViewById(R.id.button_log);

		m_sensor = new Sensor(this);
		SensorManager m = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		m.registerListener(m_sensor, SensorManager.SENSOR_ACCELEROMETER);

		m_history = new MyLogs(HISTORY_MAX_LENGTH);
		
		m_logButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_history.writeLogFile(TAG, LOG_FILENAME);
				m_tvLogButton.setText("Logs saved.");
			}
		});
	}

	void handleMeasure(float _x, float _y, float _z)
	{
		// Acceleration display on the screen
		m_tvAxisX.setText(getString(R.string.axis_x) + _x);
		m_tvAxisY.setText(getString(R.string.axis_y) + _y);
		m_tvAxisZ.setText(getString(R.string.axis_z) + _z);
		
		// Data logging
		long _time = Calendar.getInstance().getTimeInMillis();
		m_history.add(_time, _x, _y, _z);
		
		// Last Maximum Z value update
		if(_z > m_fLastMax) {
			m_fLastMax 		= _z;
			m_lLastMaxTime 	= _time;
		}
		
		if(detectStep()) {
			m_tvStepsCounter.setText(getString(R.string.steps_number) + ++m_iStepsCounter);
		}
		
	}

	private boolean detectStep() {
		if(m_history != null && m_history.getList().size() > 1) {
			// Z acceleration down phase
			if(m_history.getList().get(0).getZ() < m_history.getList().get(1).getZ()) {
				// y = ax + b
				float a = (m_fLastMax - m_history.getList().get(0).getZ()) - ((m_lLastMaxTime - m_history.getList().get(0).getTime()) / 1000);
				if(a > STEP_DETECTION_LIMIT) {
					m_fLastMax		= -9.81f;
					m_lLastMaxTime 	= 0;
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
