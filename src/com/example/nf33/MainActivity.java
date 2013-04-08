package com.example.nf33;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {

	private Sensor sensor;
	private TextView tv_axis_x,
	                 tv_axis_y,
	                 tv_axis_z;

	private ArrayList<float[]> history;
	private final int history_max_length = 4096;

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

		history = new ArrayList<float[]>(history_max_length);
	}

	/*
	 * TODO:
	 * . ajouter un timestamp à chaque mesure (créer un objet de log)
	 * . ajouter un boutton "Log" qui enregistre l'historique courante
	 *   et la réinitialise
	 */
	void handleMeasure(float x, float y, float z)
	{
		tv_axis_x.setText(getString(R.string.axis_x) + x);
		tv_axis_y.setText(getString(R.string.axis_y) + y);
		tv_axis_z.setText(getString(R.string.axis_z) + z);
		if (history.size() >= history_max_length) {
			// Rotation de l'historique
			history.remove(history.size()-1);
		}
		history.add(0, new float[] {
			x,
			y,
			z
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
