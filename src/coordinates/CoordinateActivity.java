package coordinates;

import steps.IStepListener;
import steps.StepActivity;
import steps.StepDetector;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import cap.CapDetector;
import cap.CapListener;

import com.example.nf33.R;

public class CoordinateActivity extends StepActivity {
	private static final String LOG_FILENAME = "'NF33'-dd-MM-yyyy-HH-mm-ss'.csv'";
    private static final String LOG_DIRNAME  = "NF33-data";
    private static final String TAG          = "NF33-data";

	TextView textViewStep, tvAmpli;
	ProgressBar progressBarCap;
	int nbStep = 0;
	
	/** * The sensor manager */
	private SensorManager m_sensorManager;
	private Sensor m_magnetic;
	private Sensor m_accelerometer;
	//private Sensor m_orientation;
	
	CapDetector capDetector;	
	StepDetector stepDetector;
	LocalisationManager localisationManager;
	
	LinearLayout.LayoutParams lParamsName;
	LinearLayout mapLayout;
	MapView mapView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_coordinate);
		
		textViewStep = (TextView) findViewById(R.id.TextViewStepValue);
		tvAmpli = (TextView) findViewById(R.id.tv_ampli);
		progressBarCap = (ProgressBar) findViewById(R.id.ProgressBarCap);
		progressBarCap.setMax(360);
		
		findViewById(R.id.startButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				localisationManager.setStartLog(true);
			}
		});
		
		findViewById(R.id.resetButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				localisationManager.getCoordLog().clear();
			}
		});
		
		findViewById(R.id.saveButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				localisationManager.getCoordLog().writeLogFile(TAG, LOG_DIRNAME, LOG_FILENAME, true);
			}
		});
		
		// Instantiate the SensorManager
		m_sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		// Instantiate the magnetic sensor
		m_magnetic = m_sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		// Instantiate the accelerometer
		m_accelerometer = m_sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		//Instantiate the orientation sensor
		//m_orientation = m_sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		

		localisationManager = new LocalisationManager(this);
		localisationManager.setLocalisationListener(new LocalisationListener() {
			@Override
			public void onNewPosition(float[] oldPosition, float[] newPosition) {
				mapView.redraw(newPosition[0], newPosition[1]);
			}
		});
		
		capDetector = localisationManager.getCapDetector();
		capDetector.addHasChangedListener(new CapListener() {
			@Override
			public void hasChanged(float cap, float oldCap, float pitch, float roll) {
				if(cap < 0)
					progressBarCap.setProgress((int) cap+360);
				else
					progressBarCap.setProgress((int) cap);
			}
		});
		
		//StepDetector
		stepDetector = localisationManager.getStepDetector();
		stepDetector.addStepListener(new IStepListener() {
			@Override
			public void stepDetected(float _stepLength) {
				nbStep++;
				textViewStep.setText(String.valueOf(nbStep));
			}
		});
		
		((SeekBar)(findViewById(R.id.seekBarAmplitude))).setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                stepDetector.setAmplitudeSensibility(3.0f - (float)(progress + 1) / (float)((seekBar.getMax() + 1) / 2.0f));
                stepDetector.setLimitSensibility(3.0f - (float)(progress + 1) / (float)((seekBar.getMax() + 1) / 2.0f));
                tvAmpli.setText(getString(R.string.sensibilite) + " : " + stepDetector.getAmplitudeSensibility());
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
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
		protected void onDestroy() {
			// kill the thread
		 mapView.isRunning.set(false);
			super.onDestroy();
		}

	@Override  
    protected void onPause() {  
        //unregister every sensor
        m_sensorManager.unregisterListener(capDetector, m_accelerometer); 
        m_sensorManager.unregisterListener(capDetector, m_magnetic);
        //m_sensorManager.unregisterListener(capDetector, m_orientation);
        stepDetector.unregisterSensors();
        super.onPause();  
    }

	@Override  
    protected void onResume() {  
        //register listener
        m_sensorManager.registerListener(capDetector,m_accelerometer, SensorManager.SENSOR_DELAY_UI); 
        m_sensorManager.registerListener(capDetector,m_magnetic, SensorManager.SENSOR_DELAY_UI);
        //m_sensorManager.registerListener(capDetector,m_orientation, SensorManager.SENSOR_DELAY_UI);
        stepDetector.registerSensors();
        super.onResume(); 
    }
  
	@Override  
    protected void onStop() {
        //cancel register
        m_sensorManager.unregisterListener(capDetector, m_accelerometer); 
        m_sensorManager.unregisterListener(capDetector, m_magnetic);
        //m_sensorManager.unregisterListener(capDetector, m_orientation);
        stepDetector.unregisterSensors();
        super.onStop();  
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.coordinate, menu);
		return true;
	}

}
