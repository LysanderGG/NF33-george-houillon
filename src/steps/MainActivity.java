package steps;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.example.nf33.R;

public class MainActivity extends StepActivity implements IStepListener {

	private int				m_iStepsCounter	= 0;
	
	private TextView 		m_tvLogs,
	                 		m_tvLogButton,
	                 		m_tvStepsCounter,
							m_tvAxis,
							m_tvLimit,
							m_tvAmpli;

	private ProgressBar 	m_progressBar;

	private StepDetector	m_stepDetector;

	private static final String LOG_FILENAME	= "NF33 dd-MM-yyyy HH:mm:ss.csv";
	private static final String LOG_DIRNAME		= "NF33-data";
	private static final String TAG 			= "NF33-data";

	// En secondes
	private static final int 	COUNTDOWN_DURATION 	= 5;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		m_tvLogs			= (TextView)findViewById(R.id.tvLogs);
		m_tvLogButton 		= (TextView)findViewById(R.id.tv_log_button);
		m_tvStepsCounter	= (TextView)findViewById(R.id.tv_steps_counter);
		m_tvAxis			= (TextView)findViewById(R.id.tv_axis);
		m_tvLimit 			= (TextView)findViewById(R.id.tv_limit);
		m_tvAmpli			= (TextView)findViewById(R.id.tv_ampli);

		m_progressBar = (ProgressBar)findViewById(R.id.progressBar);

		m_stepDetector = new StepDetector(this);
		m_stepDetector.addStepListener(this);

		// Implementation des delegates des bouttons
		findViewById(R.id.button_log).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				m_stepDetector.toggleActivity(false);
				if (m_stepDetector.getHistory().writeLogFile(TAG, LOG_DIRNAME, LOG_FILENAME, true)) {
					m_tvLogButton.setText("Logs ok.");
				} else {
					m_tvLogButton.setText("Logs failed.");
				}
				m_stepDetector.toggleActivity(true);
			}
		});
		
		findViewById(R.id.button_reset).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Mise en pause
				resetAll();
				m_stepDetector.toggleActivity(false);
				m_tvStepsCounter.setText(String.valueOf(COUNTDOWN_DURATION));
				m_tvStepsCounter.setTextColor(Color.rgb(0, 175, 45));
				// Attente de quelques secondes avant le redemarrage de l'activite
				new CountDownTimer(COUNTDOWN_DURATION*1000, 500) {
				     @Override
					public void onTick(long millisUntilFinished) {
				         m_tvStepsCounter.setText(String.valueOf((int)(millisUntilFinished / 1000) + 1));
				     }
				     @Override
					public void onFinish() {
				    	 m_tvStepsCounter.setText("0");
				    	 m_tvStepsCounter.setTextColor(Color.BLACK);
				    	 m_stepDetector.toggleActivity(true);
				    	 // Declenchement du vibreur pour avertir l'utilisateur
				    	 Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				    	 v.vibrate(300);
				     }
				  }.start();
			}
		});
		
		findViewById(R.id.tgbtnAxis).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				m_stepDetector.setIsMultiAxis(!m_stepDetector.getIsMultiAxis());
				resetAll();
			}
		});
		
		// Implementation des delegates de SeekBars 
		((SeekBar)(findViewById(R.id.seekBarLimit))).setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				m_stepDetector.setLimitSensibility(1.0f + (float)(progress + 1) / (float)((seekBar.getMax() + 1) / 2.0f));
				m_tvLimit.setText(getString(R.string.limit) + " : " + m_stepDetector.getLimitSensibility());
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		
		((SeekBar)(findViewById(R.id.seekBarAmplitude))).setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				m_stepDetector.setAmplitudeSensibility(1.0f + (float)(progress + 1) / (float)((seekBar.getMax() + 1) / 2.0f));
				m_tvAmpli.setText(getString(R.string.amplitude) + " : " + m_stepDetector.getAmplitudeSensibility());
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		
		
		m_stepDetector.toggleActivity(true);
	}

	
	/*
	 * Methodes de reset
	 */

	private void resetStepsCounter() {
		m_iStepsCounter = 0;
		m_tvStepsCounter.setText("0");
	}

	private void resetHistory() {
		m_stepDetector.getHistory().clear();
	}

	private void resetAll() {
		resetStepsCounter();
		resetHistory();
	}

	
	/*
	 * Surcharge des methodes de StepActivity
	 */
	
	@Override
	public void setMajorAxis(int _axis) {
		switch(_axis) {
		case StepActivity.AXIS_X:
			m_tvAxis.setText("X");
			break;
		case StepActivity.AXIS_Y:
			m_tvAxis.setText("Y");
			break;
		case StepActivity.AXIS_Z:
			m_tvAxis.setText("Z");
			break;
		case StepActivity.AXIS_3D:
			m_tvAxis.setText("3D");
			break;
		}
	}


	@Override
	public void setAxisBalance(int _balance) {
		m_progressBar.setProgress(_balance);
	}


	@Override
	public void setStepsCounter(int _value) {
		m_tvStepsCounter.setText(String.valueOf(_value));
	}


	@Override
	public void stepDetected(float _stepLength) {
		setStepsCounter(++m_iStepsCounter);
	}
	
	
	
}
