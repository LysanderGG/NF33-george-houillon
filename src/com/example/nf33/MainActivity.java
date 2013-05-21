package com.example.nf33;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.nf33.MyLogs.LogItem;

public class MainActivity extends Activity {

	private Sensor 			m_sensor;
	private SensorManager   m_sensorManager;
	private TextView 		m_tvLogButton,
	                 		m_tvStepsCounter,
							m_tvAxis;
	private MyLogs 			m_history;
	private ProgressBar 	m_progressBar;

	private int				m_iStepsCounter	= 0;

	private float			m_fLastMax		= 0.0f;
	private float			m_fLastMin		= 0.0f;

	private boolean			m_bMultiAxis	= false;

	private ArrayList<Integer> m_stateHistory;

	private IStepListener	m_stepListener;

	/*
	 * Constantes de l'application
	 */

	// seconds
	private static final int 	COUNTDOWN_DURATION 		= 5;

	private static final int 	HISTORY_MAX_LENGTH 		= 1024;

	private static final String LOG_FILENAME			= "NF33.csv";
	private static final String TAG 					= "NF33-data";

	private static final float CONSTANT_STEP_LENGTH		= 0.70f;


	/*
	 * Constantes propres a l'algorithme de detection de pas
	 */

	private static final int STATE_ASCENDENT  = 0;
	private static final int STATE_DESCENDENT = 1;
	private static final int STATE_CAPTURING  = 2;

	private int state = STATE_CAPTURING;


	private static final float NEGATIVE_LIMIT_MULTI_AXIS 	= -1.25f;
	private static final float POSITIVE_LIMIT_MULTI_AXIS 	= +1.25f;
	private static final float AMPLITUDE_MINIMUM_MULTI_AXIS = 3.0f;

	private static final float NEGATIVE_LIMIT_1_AXIS 		= -1.25f;
	private static final float POSITIVE_LIMIT_1_AXIS 		= +1.25f;
	private static final float AMPLITUDE_MINIMUM_1_AXIS 	= 3.0f;

	// Le nombre d'échantillons à observer pour déterminer l'axe principal
	// sur lequel le téléphone est utilisé.
	private final int N_HISTORY_LOOK_BACK = 10;

	/*
	 * Prépare l'activité au démarrage de l'enregistrement en branchant
	 * les différents widgets.
	 */

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		m_tvLogButton 		= (TextView)findViewById(R.id.tv_log_button);
		m_tvStepsCounter	= (TextView)findViewById(R.id.tv_steps_counter);
		m_tvAxis			= (TextView)findViewById(R.id.tv_axis);

		m_progressBar = (ProgressBar)findViewById(R.id.progressBar);

		m_sensor = new Sensor(this);
		m_sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

		m_history = new MyLogs(HISTORY_MAX_LENGTH);

		m_stateHistory = new ArrayList<Integer>(3);

		// Branche le bouton d'enregistrement de l'historique
		findViewById(R.id.button_log).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				m_sensor.toggleActivity(false);
				if (m_history.writeLogFile(TAG, LOG_FILENAME)) {
					m_tvLogButton.setText("Logs ok.");
				} else {
					m_tvLogButton.setText("Logs failed.");
				}
				m_sensor.toggleActivity(true);
			}
		});
		// Branche le bouton de remise à zéro
		findViewById(R.id.button_reset).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Met l'activité de capture en pause
				resetAll();
				toggleActivity(false);
				m_tvStepsCounter.setText(String.valueOf(COUNTDOWN_DURATION));
				m_tvStepsCounter.setTextColor(Color.rgb(0, 175, 45));
				// Attend quelques secondes avant de redémarrer l'activité de capture
				new CountDownTimer(COUNTDOWN_DURATION*1000, 500) {
				     @Override
					public void onTick(long millisUntilFinished) {
				         m_tvStepsCounter.setText(String.valueOf((int)(millisUntilFinished / 1000)+1));
				     }
				     @Override
					public void onFinish() {
				    	 m_tvStepsCounter.setText("0");
				    	 m_tvStepsCounter.setTextColor(Color.BLACK);
				    	 toggleActivity(true);
				    	 // Vibre pendant 300 millisecondes
				    	 Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				    	 v.vibrate(300);
				     }
				  }.start();
			}
		});
		// Branche le bouton de changement de mode
		findViewById(R.id.tgbtnAxis).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				m_bMultiAxis = !m_bMultiAxis;
				resetAll();
			}
		});
		// Démarre l'activité de capture
		toggleActivity(true);
	}

	/*
	 * Suspend or resume the activity (sensor capturing, logging, screen update)
	 */
	void toggleActivity(boolean on) {
		m_sensor.toggleActivity(on);
	}

	void handleMeasure(float _x, float _y, float _z)
	{
		m_history.add(
			Calendar.getInstance().getTimeInMillis(),
			_x,
			_y,
			_z
		);

		float norm;

		// Mode mono-axe: détection de pas sur l'axe majeur
		if (!m_bMultiAxis) {
			ArrayList<LogItem> history = m_history.getList();

			// Calcule la norme moyenne de chaque axe sur les derniers échantillons
			float normX = 0;
			float normY = 0;
			float normZ = 0;

			float n = Math.min(history.size(), N_HISTORY_LOOK_BACK);

			for (int i = 0; i < n; ++i) {
				float x = history.get(i).getX();
				float y = history.get(i).getY();
				float z = history.get(i).getZ();
				float normInst = (float)Math.sqrt(
					x * x +
					y * y +
					z * z
				);
				normX += Math.abs(Math.abs(x) - normInst);
				normY += Math.abs(Math.abs(y) - normInst);
				normZ += Math.abs(Math.abs(z) - normInst);
			}

			normX /= n;
			normY /= n;
			normZ /= n;

			float minNorm = Math.min(
				Math.min(normX, normY),
				normZ
			);

			if (minNorm == normX) {
				norm = Math.abs(_x);
				m_tvAxis.setText("X");
			} else if(minNorm == normY) {
				norm = Math.abs(_y);
				m_tvAxis.setText("Y");
			} else {
				norm = Math.abs(_z);
				m_tvAxis.setText("Z");
			}
		}
		// Mode multi-axe: détecte le pas sur la norme tridimentionnelle
		else {
			norm = (float)Math.sqrt(
				_x * _x +
				_y * _y +
				_z * _z
			);
			m_tvAxis.setText("3D");
		}

		// Dessine la norme sur la barre de progres
		int progress = (int) ((norm / (2*Sensor.G)) * 100);
		if (progress < 0) {
			progress = 0;
		} else if (progress > 100) {
			progress = 100;
		}
		m_progressBar.setProgress(progress);

		// Déduit la gravité de la norme
		norm -= Sensor.G;

		switch (state) {
		// Cherche simultanement un minimum et un maximum local.
		case STATE_CAPTURING:
			if (norm < getNegativeLimit()) {
				m_fLastMin = norm;
				setState(STATE_DESCENDENT);
			} else if (norm > getPositiveLimit()) {
				m_fLastMax = norm;
				setState(STATE_ASCENDENT);
			}
			break;
		// Enregistre un passage à l'état ascendant avant de recherche de nouveau
		// une phase descendante.
		case STATE_ASCENDENT:
			if (norm > m_fLastMax) {
				m_fLastMax = norm;
			}
			if (norm < getPositiveLimit()) {
				setState(STATE_CAPTURING);
			}
			break;
		// Une détection de pas ne peut avoir lieu qu'en phase descendente.
		// Choix arbitraire.
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
	}

	/*
	 * Change d'état en maintenant une liste des 3 derniers états
	 */

	private void setState(int newState) {
		state = newState;
		if (m_stateHistory.size() >= 3) {
			m_stateHistory.remove(2);
		}
		m_stateHistory.add(0, newState);
	}

	/*
	 * Vérifie qu'une certaine amplitude a bien été enregistrée
	 * lors de la recherche des minimums et maximums locaux.
	 */

	private boolean amplitudeCheck() {
		return m_fLastMax - m_fLastMin > getAmplitudeMinimum();
	}

	/*
	 * Vérifie qu'une séquence A-C-D a bien été réalisée.
	 */

	private boolean sequenceCheck() {
		return m_stateHistory.size() >= 3
		    && m_stateHistory.get(1) == STATE_CAPTURING
		    && m_stateHistory.get(2) == STATE_ASCENDENT;
	}

	/*
	 * Enregistre un pas.
	 */

	private void stepDetected() {
		++m_iStepsCounter;
		m_tvStepsCounter.setText(String.valueOf(m_iStepsCounter));
		m_history.addStepDetected();
		if (m_stepListener != null) {
			m_stepListener.stepDetected(CONSTANT_STEP_LENGTH);
		}
	}

	/*
	 * Remet à zéro le compteur de pas.
	 */

	private void resetStepsCounter() {
		m_iStepsCounter = 0;
		m_tvStepsCounter.setText("0");
	}

	/*
	 * Remet à zéro l'historique de l'application.
	 */

	private void resetHistory() {
		m_history.clear();
	}

	/*
	 * Remet à zéro toute la mémoire de l'application.
	 */

	private void resetAll() {
		resetStepsCounter();
		resetHistory();
	}

	/*
	 * Récupère l'amplitude minimale à dépasser pour valider un pas.
	 * Prend en compte le mode de l'application (mono-axe ou multi-axe).
	 */

	private float getAmplitudeMinimum() {
		return (m_bMultiAxis) ? AMPLITUDE_MINIMUM_MULTI_AXIS : AMPLITUDE_MINIMUM_1_AXIS;
	}

	/*
	 * Récupère la borne minimale à dépasser pour changer d'état.
	 * Prend en compte le mode de l'application (mono-axe ou multi-axe).
	 */

	private float getNegativeLimit() {
		return (m_bMultiAxis) ? NEGATIVE_LIMIT_MULTI_AXIS : NEGATIVE_LIMIT_1_AXIS;
	}

	/*
	 * Récupère la borne maximale à dépasser pour changer d'état.
	 * Prend en compte le mode de l'application (mono-axe ou multi-axe).
	 */

	private float getPositiveLimit() {
		return (m_bMultiAxis) ? POSITIVE_LIMIT_MULTI_AXIS : POSITIVE_LIMIT_1_AXIS;
	}

	/*
	 * Spécifie l'écouteur de pas.
	 */

	public boolean setStepListener(IStepListener _listener) {
		if (m_stepListener != null) {
			return false;
		}
		m_stepListener = _listener;
		return true;
	}

	/*
	 * Renvoie le gestionnaire de senseur courant.
	 */

	public SensorManager getSensorManager() {
		return m_sensorManager;
	}
}
