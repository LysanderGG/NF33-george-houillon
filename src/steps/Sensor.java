package steps;

import android.hardware.SensorListener;
import android.hardware.SensorManager;

@SuppressWarnings("deprecation")
public class Sensor implements SensorListener {

	float m_fX, m_fY, m_fZ;

	boolean active = false;

	// Moyenne expérimentale du bruit sur l'axe Z (téléphone à plat)
	public static final float G = 10.131f;

	private final StepDetector m_stepDetector;

	public Sensor(StepDetector s) {
		m_stepDetector = s;
	}

	@Override
	public void onAccuracyChanged(int arg0, int arg1) {}

	@Override
	public void onSensorChanged(int arg0, float[] arg1) {
		m_fX = arg1[0];
		m_fY = arg1[1];
		m_fZ = arg1[2];
		m_stepDetector.handleMeasure(m_fX, m_fY, m_fZ);
	}

	public void toggleActivity(boolean on) {
		if (on && !active) {
			m_stepDetector.getSensorManager().registerListener(this, SensorManager.SENSOR_ACCELEROMETER, SensorManager.SENSOR_DELAY_FASTEST);
			active = true;
		} else if (!on && active) {
			m_stepDetector.getSensorManager().unregisterListener(this);
			active = false;
		}
	}
}