package com.example.nf33;

import android.hardware.SensorListener;
import android.hardware.SensorManager;

@SuppressWarnings("deprecation")
public class Sensor implements SensorListener {

	float m_fX, m_fY, m_fZ;

	boolean active = false;

	// Moyenne expérimentale du bruit sur l'axe Z (téléphone à plat)
	public static final float G = 10.131f;

	private final MainActivity parent_activity;

	public Sensor(MainActivity a) {
		parent_activity = a;
	}

	@Override
	public void onAccuracyChanged(int arg0, int arg1) {}

	@Override
	public void onSensorChanged(int arg0, float[] arg1) {
		m_fX = arg1[0];
		m_fY = arg1[1];
		m_fZ = arg1[2];
		parent_activity.handleMeasure(m_fX, m_fY, m_fZ);
	}

	public void toggleActivity(SensorManager m, boolean on) {
		if (on && !active) {
			m.registerListener(this, SensorManager.SENSOR_ACCELEROMETER, SensorManager.SENSOR_DELAY_FASTEST);
			active = true;
		} else if (!on && active) {
			m.unregisterListener(this);
			active = false;
		}
	}
}
