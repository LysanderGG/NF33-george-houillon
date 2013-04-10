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

	private Sensor sensor;
	private TextView tv_axis_x,
	                 tv_axis_y,
	                 tv_axis_z,
	                 tv_logButton;
	private Button	logButton;
	private MyLogs 	history;
	
	private static final int 	HISTORY_MAX_LENGTH 	= 4096;
	private static final String LOG_FILENAME		= "NF33.log";
	private static final String TAG 				= MainActivity.class.getName();
	

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		tv_axis_x = (TextView)findViewById(R.id.tv_axis_x);
		tv_axis_y = (TextView)findViewById(R.id.tv_axis_y);
		tv_axis_z = (TextView)findViewById(R.id.tv_axis_z);
		logButton = (Button)  findViewById(R.id.buttonLog);
		tv_logButton = (TextView)findViewById(R.id.textViewButtonLog);

		sensor = new Sensor(this);
		SensorManager m = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		m.registerListener(sensor, SensorManager.SENSOR_ACCELEROMETER);

		history = new MyLogs(HISTORY_MAX_LENGTH);
		
		logButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				history.writeLogFile(TAG, LOG_FILENAME);
				tv_logButton.setText("Logs saved.");
			}
		});
	}

	void handleMeasure(float _x, float _y, float _z)
	{
		tv_axis_x.setText(getString(R.string.axis_x) + _x);
		tv_axis_y.setText(getString(R.string.axis_y) + _y);
		tv_axis_z.setText(getString(R.string.axis_z) + _z);
		
		long _time = Calendar.getInstance().getTimeInMillis();
		history.add(_time, _x, _y, _z);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
