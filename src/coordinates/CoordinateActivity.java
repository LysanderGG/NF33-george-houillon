package coordinates;

import steps.IStepListener;
import steps.StepActivity;
import steps.StepDetector;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import cap.CapDetector;
import cap.CapListener;

import com.example.nf33.R;

public class CoordinateActivity extends StepActivity {

	TextView textViewStep;
	ProgressBar progressBarCap;
	int nbStep = 0;
	
	/** * The sensor manager */
	private SensorManager m_sensorManager = null;
	private Sensor m_magnetic;
	private Sensor m_accelerometer;
	
	CapDetector capDetector;	
	StepDetector stepDetector;
	
	LinearLayout.LayoutParams lParamsName;
	LinearLayout mapLayout;
	MapView mapView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_coordinate);
		
		textViewStep = (TextView) findViewById(R.id.TextViewStepValue);
		progressBarCap = (ProgressBar) findViewById(R.id.ProgressBarCap);
		
		// Instantiate the SensorManager
		m_sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		// Instantiate the magnetic sensor and its max range
		m_magnetic = m_sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		// Instantiate the accelerometer
		m_accelerometer = m_sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		capDetector = new CapDetector();
		capDetector.addHasChangedListener(new CapListener() {
			@Override
			public void hasChanged(float cap, float pitch, float roll) {
				if(cap < 0)
					progressBarCap.setProgress((int) cap+360);
				else
					progressBarCap.setProgress((int) cap);
			}
		});
		
		//StepDetector
		stepDetector = new StepDetector(this);
		stepDetector.addStepListener(new IStepListener() {
			@Override
			public void stepDetected(float _stepLength) {
				nbStep++;
				textViewStep.setText(String.valueOf(nbStep));
			}
		});
		
		mapLayout = (LinearLayout) findViewById(R.id.mapLayout);
		// then build the view
		mapView = new MapView(this);
		// define the layout parameters and add the view to the layout
		LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		// add the view in the layout
		mapLayout.addView(mapView, layoutParam);
	}

	@Override  
    protected void onPause() {  
        //unregister every sensor
        m_sensorManager.unregisterListener(capDetector, m_accelerometer); 
        m_sensorManager.unregisterListener(capDetector, m_magnetic);
        stepDetector.toggleActivity(false);
        super.onPause();  
    }

	@Override  
    protected void onResume() {  
        //register listener
        m_sensorManager.registerListener(capDetector,m_accelerometer, m_sensorManager.SENSOR_DELAY_UI); 
        m_sensorManager.registerListener(capDetector,m_magnetic,m_sensorManager.SENSOR_DELAY_UI);
        stepDetector.toggleActivity(true);
        super.onResume(); 
    }
  
	@Override  
    protected void onStop() {
        //cancel register
        m_sensorManager.unregisterListener(capDetector, m_accelerometer); 
        m_sensorManager.unregisterListener(capDetector, m_magnetic);
        stepDetector.toggleActivity(false);
        super.onStop();  
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.coordinate, menu);
		return true;
	}

}
