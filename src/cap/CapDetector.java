package cap;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class CapDetector implements SensorEventListener {

	/**
	 * Current value of the accelerometer
	 */
	float x, y, z;

	float[] acceleromterVector=new float[3];
	float[] magneticVector=new float[3];
	float[] resultMatrix=new float[9];
	float[] values=new float[3];
	
	private CapListener capListener;
	
	public CapDetector() {
		capListener = null;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			acceleromterVector=event.values;
		else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
			magneticVector=event.values;
		
		SensorManager.getRotationMatrix(resultMatrix, null, acceleromterVector, magneticVector);
		SensorManager.getOrientation(resultMatrix, values);
		
		// the azimuts
		x =(float) Math.toDegrees(values[0]);
		// the pitch
		y = (float) Math.toDegrees(values[1]);
		// the roll
		z = (float) Math.toDegrees(values[2]);
		
		//emit a changing
		capListener.hasChanged(x,y,z);
	}
	
	@Override
	public void onAccuracyChanged(Sensor event, int arg1) {

	}
	
	//handle listener
	public void setHasChangedListener(CapListener listener){
		capListener = listener;
	}
		
	public float getCap(){
		return x;
	}
}
