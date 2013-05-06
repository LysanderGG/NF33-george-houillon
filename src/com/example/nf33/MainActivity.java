package com.example.nf33;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.nf33.MyLogs.LogItem;

public class MainActivity extends Activity {

	private Sensor 			m_sensor;
	private TextView 		m_tvLogs,
	                 		m_tvLogButton,
	                 		m_tvStepsCounter,
							m_tvAxis;
	private MyLogs 			m_history;
	private ProgressBar 	m_progressBar;

	private int			m_iStepsCounter		= 0;
	
	private float		m_fLastMax			= 0.0f;
	private float		m_fLastMin			= 0.0f;
	
	private boolean		m_bMultiAxis		= false;
	
	private ArrayList<Integer> stateHistory;

	/*
	 * Constantes de l'application 
	 */
	
	private static final int 	HISTORY_MAX_LENGTH 		= 1024;

	private static final String LOG_FILENAME			= "NF33.csv";
	private static final String TAG 					= "NF33-data";

	/*
	 * Constantes propres a l'algorithme de detection de pas
	 */

	private static final int STATE_ASCENDENT  = 0;
	private static final int STATE_DESCENDENT = 1;
	private static final int STATE_CAPTURING  = 2;

	private int state = STATE_CAPTURING;

	private static final float NEGATIVE_LIMIT = -1.25f;
	private static final float POSITIVE_LIMIT = +1.25f;

	private static final float AMPLITUDE_MINIMUM = 3.0f;


	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		m_tvLogs			= (TextView)findViewById(R.id.tvLogs);
		m_tvLogButton 		= (TextView)findViewById(R.id.tv_log_button);
		m_tvStepsCounter	= (TextView)findViewById(R.id.tv_steps_counter);
		m_tvAxis	= (TextView)findViewById(R.id.tv_axis);

		m_progressBar = (ProgressBar)findViewById(R.id.progressBar);

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
					//m_tvLogButton.setText("Logs ok.");
				} else {
					//m_tvLogButton.setText("Logs failed.");
				}
			}
		});
		findViewById(R.id.button_reset).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				resetAll();
			}
		});
		findViewById(R.id.tgbtnAxis).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				m_bMultiAxis = !m_bMultiAxis;
				resetAll();
			}
		});
	}

	void handleMeasure(float _x, float _y, float _z)
	{
		// Acceleration display on the screen
		String txt = getString(R.string.axis_x) + _x + "\n" + getString(R.string.axis_y) + _y + "\n" + getString(R.string.axis_z) + _z + "\n";

		// Data logging
		long time = Calendar.getInstance().getTimeInMillis();
		m_history.add(time, _x, _y, _z);

		float norm = (float) Math.sqrt(_x * _x + _y * _y + _z * _z);
		txt += "Norm : " + norm + "\n";
		
		if(!m_bMultiAxis){
			// 1 axe seulement
			// Determination de l'axe majeur sur les 10 dernieres mesures
			ArrayList<LogItem> history = m_history.getList();
			float normX = 0;
			float normY = 0;
			float normZ = 0;
			float nbElements = Math.min(history.size(), 10);
			
			for(int i = 0; i < nbElements; ++i) {
				float x = history.get(i).getX();
				float y = history.get(i).getY();
				float z = history.get(i).getZ();
				float normInst = (float)Math.sqrt(x * x + y * y + z * z);
				normX += Math.abs(Math.abs(x) - normInst) / nbElements;
				normY += Math.abs(Math.abs(y) - normInst) / nbElements;
				normZ += Math.abs(Math.abs(z) - normInst) / nbElements;
			}
			
			txt += "normX - norm = " + normX + "\n";
			txt += "normY - norm = " + normY + "\n";
			txt += "normZ - norm = " + normZ + "\n";
			
			float minNorm = Math.min(normX, normY);
			minNorm = Math.min(minNorm, normZ);
			
			if(minNorm == normX) {
				norm = _x;
				m_tvAxis.setText("X");
			} else if(minNorm == normY) {
				norm = _y;
				m_tvAxis.setText("Y");
			} else {
				norm = _z;
				m_tvAxis.setText("Z");
			}
		} else {
			m_tvAxis.setText("XYZ");
		}
	
		// Dessine la norme sur la barre de progres
		int progress = (int) ((norm / (2*Sensor.G)) * 100);
		if (progress > 100) progress = 100;
		m_progressBar.setProgress(progress);

		// DÃ©duit la gravitÃ© de la norme
		norm -= Sensor.G;

		switch (state) {
		// Cherche simultanÃ©ment un minimum et un maximum local
		case STATE_CAPTURING:
			if (norm < NEGATIVE_LIMIT) {
				m_fLastMin = norm;
				setState(STATE_DESCENDENT);
			} else if (norm > POSITIVE_LIMIT) {
				m_fLastMax = norm;
				setState(STATE_ASCENDENT);
			}
			break;
		// Enregistre un passage à l'état ascendant avant de recherche de nouveau une phase descendante
		case STATE_ASCENDENT:
			if (norm > m_fLastMax) {
				m_fLastMax = norm;
			}
			if (norm < POSITIVE_LIMIT) {
				setState(STATE_CAPTURING);
			}
			break;
		// Une détection de pas ne peut avoir lieu qu'en phase descendente (choix arbitraire)
		case STATE_DESCENDENT:
			if (norm < m_fLastMin) {
				m_fLastMin = norm;
			}
			// Cherche une intersection avec l'origine
			if (norm > 0) {
				if (amplitudeCheck() && sequenceCheck()) {
					stepDetected();
				}
				// Réinitialise la machine à états
				m_fLastMax = 0f;
				m_fLastMin = 0f;
				setState(STATE_CAPTURING);
			}
			break;
		}
		
		m_tvLogs.setText(txt);
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
		m_tvStepsCounter.setText(String.valueOf(m_iStepsCounter));
	}
	
	/*
	 * Reset methods
	 */
	
	private void resetStepsCounter() {
		m_iStepsCounter = 0;
		m_tvStepsCounter.setText("0");
	}
	
	private void resetHistory() {
		m_history.clear();
	}
	
	private void resetAll() {
		resetStepsCounter();
		resetHistory();
	}
	
}
