package com.example.nf33;

import android.hardware.SensorListener;

@SuppressWarnings("deprecation")
public class Sensor implements SensorListener {
	float x, y, z;

	private final MainActivity parent_activity;

	public Sensor(MainActivity a) {
		parent_activity = a;
	}

	@Override
	public void onAccuracyChanged(int arg0, int arg1) {}

	@Override
	public void onSensorChanged(int arg0, float[] arg1) {
		x = arg1[0];
		y = arg1[1];
		z = arg1[2];
		parent_activity.handleMeasure(x, y, z);
	}

}
