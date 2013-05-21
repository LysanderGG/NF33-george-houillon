package coordinates;

import cap.CapDetector;
import cap.CapListener;

import com.example.nf33.IStepListener;
import com.example.nf33.MainActivity;
import com.example.nf33.R;
import com.example.nf33.R.layout;
import com.example.nf33.R.menu;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;

public class CoordinateActivity extends Activity {

	TextView textViewCap, step, coordinate;
	int nbStep = 0;
	
	/** * The sensor manager */
	private SensorManager m_sensorManager = null;
	private Sensor m_magnetic;
	private Sensor m_accelerometer;
	
	LocalisationManager localisationManager;
	
	CapDetector capDetector;	
	//StepDetector
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_coordinate);
		
		textViewCap = (TextView) findViewById(R.id.TextViewCapValue);
		step = (TextView) findViewById(R.id.TextViewPasValue);
		coordinate = (TextView) findViewById(R.id.TextViewCoordValue);
		
		// Instantiate the SensorManager
		m_sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		// Instantiate the magnetic sensor and its max range
		m_magnetic = m_sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		// Instantiate the accelerometer
		m_accelerometer = m_sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		localisationManager = new LocalisationManager();
		localisationManager.setLocalisationListener(new LocalisationListener() {
			@Override
			public void onNewPosition(float[] oldPosition, float[] newPosition) {
				coordinate.setText("x: " + Float.toString(newPosition[0])+
									",y:"+ Float.toString(newPosition[1]));
			}
		});
		
		capDetector = new CapDetector();
		capDetector.setHasChangedListener(new CapListener() {
			@Override
			public void hasChanged(float cap, float pitch, float roll) {
				textViewCap.setText(Float.toString(cap));	
			}
		});
		
		//StepDetector
		/*stepDetector.setStepListener(new IStepListener() {
			@Override
			public void stepDetected(float _stepLength) {
				nbStep++;
				step.setText(String.valueOf(nbStep));
			}
		});*/
	}

	@Override  
    protected void onPause() {  
        //unregister every sensor
        m_sensorManager.unregisterListener(capDetector, m_accelerometer); 
        m_sensorManager.unregisterListener(capDetector, m_magnetic);
        //m_sensorManager.unregisterListener(stepDetector, m_accelerometer); 
        super.onPause();  
    }

	@Override  
    protected void onResume() {  
        //register listener
        m_sensorManager.registerListener(capDetector,m_accelerometer, m_sensorManager.SENSOR_DELAY_UI); 
        m_sensorManager.registerListener(capDetector,m_magnetic,m_sensorManager.SENSOR_DELAY_UI);
        //m_sensorManager.registerListener(stepDetector,m_magnetic,m_sensorManager.SENSOR_DELAY_FASTEST);
        super.onResume(); 
    }
  
	@Override  
    protected void onStop() {
        //cancel register
        m_sensorManager.unregisterListener(capDetector, m_accelerometer); 
        m_sensorManager.unregisterListener(capDetector, m_magnetic);
      //m_sensorManager.unregisterListener(stepDetector, m_accelerometer); 
        super.onStop();  
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.coordinate, menu);
		return true;
	}

}
