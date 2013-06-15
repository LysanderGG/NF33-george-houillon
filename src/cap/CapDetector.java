package cap;

import java.util.ArrayList;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class CapDetector implements SensorEventListener {
	/**
	 * Current value of the accelerometer
	 */
	private float x, y, z;

	private float[] acceleromterVector=new float[3];
	private float[] magneticVector=new float[3];
	private float[] resultMatrix=new float[9];
	private float[] values=new float[3];
	
	private ArrayList<Float> capList = new ArrayList<Float>(); 
	private boolean v2 = false;
	private ArrayList<CapListener>  capListenerList;
	
	private int PAST_ITERATION = 14;
	
	public CapDetector(boolean _v2) {
		capListenerList = new ArrayList<CapListener>();
		v2 = _v2;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			acceleromterVector = event.values;
		else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
			magneticVector = event.values;
		
		SensorManager.getRotationMatrix(resultMatrix, null, acceleromterVector, magneticVector);
		SensorManager.getOrientation(resultMatrix, values);
		
		// the azimuts
		x =(float) Math.toDegrees(values[0]);
		// the pitch
		y = (float) Math.toDegrees(values[1]);
		// the roll
		z = (float) Math.toDegrees(values[2]);
		
		/*if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
		    //l' azimuth
		    x = event.values[0];
		    //le pitch
		    y = event.values[1];
		    //le roll
		    z = event.values[2];
		    }*/
		
		//emit a changing
		for(CapListener listener : capListenerList)
			listener.hasChanged(x,getOldCap(),y,z);
		
		if(v2)
			capList.add(x);
	}
	
	@Override
	public void onAccuracyChanged(Sensor event, int arg1) {

	}
	
	//handle listener
	public void addHasChangedListener(CapListener listener){
		capListenerList.add(listener);
	}
	
	public float getCap(){
		return x;
	}
	
	public float getOldCap(){
		if(capList.size()==0)
			return 0;
		else if(capList.size()<=PAST_ITERATION)
			return capList.get(0);
		else
			return capList.get(capList.size()-PAST_ITERATION);
	}
	
	public void clearList(){
		capList.clear();
	}
	
	public boolean isV2(){
		return v2;
	}
}
