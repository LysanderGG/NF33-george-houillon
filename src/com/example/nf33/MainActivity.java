package com.example.nf33;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {

	private Sensor sensor;
	private TextView tv_axis_x,
	                 tv_axis_y,
	                 tv_axis_z;

	private MyLogs history;
	
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

		sensor = new Sensor(this);
		SensorManager m = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		m.registerListener(sensor, SensorManager.SENSOR_ACCELEROMETER);

		history = new MyLogs(HISTORY_MAX_LENGTH);
	}

	/*
	 * TODO:
	 * . ajouter un timestamp a chaque mesure (creer un objet de log)
	 * . ajouter un boutton "Log" qui enregistre l'historique courante
	 *   et la réinitialise
	 */
	void handleMeasure(float _x, float _y, float _z)
	{
		tv_axis_x.setText(getString(R.string.axis_x) + _x);
		tv_axis_y.setText(getString(R.string.axis_y) + _y);
		tv_axis_z.setText(getString(R.string.axis_z) + _z);
		
		int _time = Calendar.getInstance().get(Calendar.MILLISECOND);
		history.add(_time, _x, _y, _z);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
