package com.example.nf33;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MainActivity extends Activity {

	private Sensor 		m_sensor;
	private TextView 	m_tvAxisX,
	                 	m_tvAxisY,
	                 	m_tvAxisZ,
	                 	m_tvLogButton,
	                 	m_tvStepsCounter;
	private MyLogs 		m_history;

	private float		m_fLastMax			= 0.0f;
	private float		m_fLastMin			= 0.0f;
	private int			m_iStepsCounter		= 0;

	private static final int 	HISTORY_MAX_LENGTH 		= 128;

	private static final String LOG_FILENAME			= "NF33.csv";
	private static final String TAG 					= "NF33-data";

	/*
	 * Constantes propres à l'algorithme de détection de pas
	 */

	private static final int STATE_ASCENDENT  = 0;
	private static final int STATE_DESCENDENT = 1;
	private static final int STATE_CAPTURING  = 2;

	private int state = STATE_CAPTURING;

	private static final float NEGATIVE_LIMIT = -1.25f;
	private static final float POSITIVE_LIMIT = +1.25f;

	private static final float AMPLITUDE_MINIMUM = 3.0f;

	private ArrayList<Integer> stateHistory;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		m_tvAxisX 	= (TextView)findViewById(R.id.tv_axis_x);
		m_tvAxisY 	= (TextView)findViewById(R.id.tv_axis_y);
		m_tvAxisZ 	= (TextView)findViewById(R.id.tv_axis_z);
		m_tvLogButton 		= (TextView)findViewById(R.id.tv_log_button);
		m_tvStepsCounter	= (TextView)findViewById(R.id.tv_steps_counter);

		//m_logButton = (Button)findViewById(R.id.button_log);

		m_sensor = new Sensor(this);
		SensorManager m = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		m.registerListener(m_sensor, SensorManager.SENSOR_ACCELEROMETER, SensorManager.SENSOR_DELAY_FASTEST);

		m_history = new MyLogs(HISTORY_MAX_LENGTH);

		stateHistory = new ArrayList<Integer>(3);

		// Buttons delegates implementation
		findViewById(R.id.button_log).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (m_history.writeLogFile(TAG, LOG_FILENAME)) {
					m_tvLogButton.setText("Logs ok.");
				} else {
					m_tvLogButton.setText("Logs failed.");
				}
			}
		});
		findViewById(R.id.button_reset).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				m_iStepsCounter = 0;
				m_tvStepsCounter.setText("0");
				m_history = new MyLogs(HISTORY_MAX_LENGTH); // TODO méthode "clear"
			}
		});
	}

	void handleMeasure(float _x, float _y, float _z)
	{
		// Acceleration display on the screen
		m_tvAxisX.setText(getString(R.string.axis_x) + _x);
		m_tvAxisY.setText(getString(R.string.axis_y) + _y);
		m_tvAxisZ.setText(getString(R.string.axis_z) + _z);

		// Data logging
		long time = Calendar.getInstance().getTimeInMillis();
		m_history.add(time, _x, _y, _z);

		_z += Sensor.G;

		switch (state) {
		case STATE_CAPTURING:
			if (_z < NEGATIVE_LIMIT) {
				m_fLastMin = _z;
				setState(STATE_DESCENDENT);
			} else if (_z > POSITIVE_LIMIT) {
				m_fLastMax = _z;
				setState(STATE_ASCENDENT);
			}
			break;
		case STATE_ASCENDENT:
			if (_z > m_fLastMax) {
				m_fLastMax = _z;
			}
			if (_z < POSITIVE_LIMIT) {
				setState(STATE_CAPTURING);
			}
			break;
		case STATE_DESCENDENT:
			if (_z < m_fLastMin) {
				m_fLastMin = _z;
			}
			if (_z > NEGATIVE_LIMIT) {
				if (amplitudeCheck() && sequenceCheck()) {
					stepDetected();
				}
				setState(STATE_CAPTURING);
			}
			break;
		}
	}

	/*
	 * Change d'état en maintenant une liste des 3 derniers états
	 */

	private void setState(int newState) {
		state = newState;
		if (stateHistory.size() >= 3) {
			stateHistory.remove(2);
		}
		stateHistory.add(0, newState);
	}

	private boolean amplitudeCheck() {
		return m_fLastMax - m_fLastMin > AMPLITUDE_MINIMUM;
	}

	/*
	 * Vérifie qu'une séquence A-C-D a bien été réalisée.
	 */

	private boolean sequenceCheck() {
		return stateHistory.size() >= 3
		    && stateHistory.get(1) == STATE_CAPTURING
		    && stateHistory.get(2) == STATE_ASCENDENT;
	}

	private void stepDetected() {
		++m_iStepsCounter;
		m_fLastMax = 0f;
		m_fLastMin = 0f;
		m_tvStepsCounter.setText(String.valueOf(m_iStepsCounter));
	}

	private boolean detectStep() {
		if(m_history != null && m_history.getList().size() > 1) {
			// Z acceleration down phase
			if(m_history.getList().get(0).getZ() < m_history.getList().get(1).getZ()) {
				// y = ax + b
				float a = (m_fLastMax - m_history.getList().get(0).getZ()) - ((m_lLastMaxTime - m_history.getList().get(0).getTime()) / 1000);
				a = Math.abs(a);
				if(a > STEP_DETECTION_LIMIT) {
					m_fLastMax		= -9.81f;
					m_lLastMaxTime 	= 0;
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
