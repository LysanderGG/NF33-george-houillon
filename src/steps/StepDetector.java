package steps;

import java.util.ArrayList;
import java.util.Calendar;

import steps.MyLogs.LogItem;
import android.content.Context;
import android.hardware.SensorManager;

public class StepDetector {
    private float         m_fLastMax    = 0.0f;
    private float         m_fLastMin    = 0.0f;
    private boolean       m_bMultiAxis  = false;
    
    private Sensor        m_sensor;
    private SensorManager m_sensorManager;
    private MyLogs        m_history;
    private ArrayList<Integer>          m_stateHistory;
    private ArrayList<IStepListener>    m_stepListenerList;
    private final StepActivity          m_parentActivity;    
    
    /*
     * Constantes de l'application
     */

    private static final int     HISTORY_MAX_LENGTH     = 1024;
    private static final int    N_HISTORY_LOOK_BACK     = 10;
    private static final float  CONSTANT_STEP_LENGTH    = 0.70f;

    /*
     * Constantes propres a l'algorithme de detection de pas
     */

    private static final int STATE_ASCENDENT  = 0;
    private static final int STATE_DESCENDENT = 1;
    private static final int STATE_CAPTURING  = 2;

    private int state = STATE_CAPTURING;
    private long m_iCaptureStartTime = -1;

    private static final float NEGATIVE_DEFAULT_LIMIT_MULTI_AXIS     = -1.00f;
    private static final float POSITIVE_DEFAULT_LIMIT_MULTI_AXIS     = +1.00f;
    private static final float AMPLITUDE_DEFAULT_MINIMUM_MULTI_AXIS  = +2.00f;

    private static final float NEGATIVE_DEFAULT_LIMIT_1_AXIS         = -1.00f;
    private static final float POSITIVE_DEFAULT_LIMIT_1_AXIS         = +1.00f;
    private static final float AMPLITUDE_DEFAULT_MINIMUM_1_AXIS      = +2.00f;
    
    private static final float LIMIT_SENSIBILITY_MIN        = 1.0f;
    private static final float LIMIT_SENSIBILITY_MAX        = 3.0f;
    private static final float LIMIT_SENSIBILITY_DEFAULT    = 2.0f;
    private static final float AMPLITUDE_SENSIBILITY_MIN    = 1.0f;
    private static final float AMPLITUDE_SENSIBILITY_MAX    = 3.0f;
    private static final float AMPLITUDE_SENSIBILITY_DEFAULT= 2.0f;
    private float m_fLimitSensibility     = LIMIT_SENSIBILITY_DEFAULT;
    private float m_fAmplitudeSensibility = AMPLITUDE_SENSIBILITY_DEFAULT;
    
    /**
     * 
     * @param _activity : activite parente
     * @param _amplitudeSensibility : sensibilite de l'amplitude (entre 1 et 3)
     * @param _limitSensibility     : sensibilite de la limite de detection de phases (entre 1 et 3)
     */
    public StepDetector(StepActivity _activity, float _limitSensibility, float _amplitudeSensibility) {
        m_parentActivity        = _activity;
        
        if(_limitSensibility < LIMIT_SENSIBILITY_MIN) {
            m_fLimitSensibility = LIMIT_SENSIBILITY_MIN;
        } else if(_limitSensibility > LIMIT_SENSIBILITY_MAX) {
            m_fLimitSensibility = LIMIT_SENSIBILITY_MAX;
        } else {
            m_fLimitSensibility = _limitSensibility;
        }
        
        if(_amplitudeSensibility < AMPLITUDE_SENSIBILITY_MIN) {
            m_fAmplitudeSensibility = AMPLITUDE_SENSIBILITY_MIN;
        } else if(_amplitudeSensibility > AMPLITUDE_SENSIBILITY_MAX) {
            m_fAmplitudeSensibility = AMPLITUDE_SENSIBILITY_MAX;
        } else {
            m_fAmplitudeSensibility = _amplitudeSensibility;
        }

        m_sensor        = new Sensor(this);
        m_sensorManager = (SensorManager)m_parentActivity.getSystemService(Context.SENSOR_SERVICE);

        m_history       = new MyLogs(HISTORY_MAX_LENGTH);
        m_stateHistory  = new ArrayList<Integer>(3);
        
        m_stepListenerList = new ArrayList<IStepListener>();
    }
    
    public StepDetector(StepActivity _activity) {
        this(_activity, LIMIT_SENSIBILITY_DEFAULT, AMPLITUDE_SENSIBILITY_DEFAULT);
    }

    /*
     * Getters - Setters
     */
    public SensorManager getSensorManager() {
        return m_sensorManager;
    }

    public MyLogs getHistory() {
        return m_history;
    }

    public boolean getIsMultiAxis() {
        return m_bMultiAxis;
    }
    public void setIsMultiAxis(boolean _b) {
        m_bMultiAxis = _b;
    }

    public float getLimitSensibility() {
        return m_fLimitSensibility;
    }
    public void setLimitSensibility(float _f) {
        m_fLimitSensibility = _f;
    }

    public float getAmplitudeSensibility() {
        return m_fAmplitudeSensibility;
    }
    public void setAmplitudeSensibility(float _f) {
        m_fAmplitudeSensibility = _f;
    }


    /*
     * Mets en pause ou reprend l'activite (capture des senseurs, log, mise à jour de l'ecran)
     */
    public void toggleActivity(boolean on) {
        m_sensor.toggleActivity(on);
    }
    public void registerSensors() {
        toggleActivity(true);
    }
    public void unregisterSensors() {
        toggleActivity(false);
    }

    void handleMeasure(float _x, float _y, float _z) {
        long now = Calendar.getInstance().getTimeInMillis();

        if (m_iCaptureStartTime == -1) {
            m_iCaptureStartTime = now;
        }

        float norm;

        // Mode mono-axe: détection de pas sur l'axe majeur
        if (!m_bMultiAxis) {
            ArrayList<LogItem> history = m_history.getList();

            // Calcule la norme moyenne de chaque axe sur les derniers échantillons
            float normX = 0;
            float normY = 0;
            float normZ = 0;

            float n = Math.min(history.size(), N_HISTORY_LOOK_BACK);

            boolean useXAxis = false,
                    useYAxis = false,
                    useZAxis = false;

            if (n > 0) {
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
                    useXAxis = true;
                } else if(minNorm == normY) {
                    useYAxis = true;
                } else {
                    useZAxis = true;
                }
            } else {
                // utilise l'axe Z par défaut
                useZAxis = true;
            }
            if (useXAxis) {
                norm = Math.abs(_x);
                m_parentActivity.setMajorAxis(StepActivity.AXIS_X);
            } else if (useYAxis) {
                norm = Math.abs(_y);
                m_parentActivity.setMajorAxis(StepActivity.AXIS_Y);
            } else {
                norm = Math.abs(_z);
                m_parentActivity.setMajorAxis(StepActivity.AXIS_Z);
            }
        }
        // Mode multi-axe: détecte le pas sur la norme tridimentionnelle
        else {
            norm = (float)Math.sqrt(
                    _x * _x +
                    _y * _y +
                    _z * _z
                );
            m_parentActivity.setMajorAxis(StepActivity.AXIS_3D);
        }

        // Dessine la norme sur la barre de progres
        int progress = (int) ((norm / (2*Sensor.G)) * 100);
        if (progress < 0) {
            progress = 0;
        } else if (progress > 100) {
            progress = 100;
        }
        m_parentActivity.setAxisBalance(progress);

        // Déduit la gravité de la norme
        norm -= Sensor.G;

        boolean stepDetected = false;
        float amplitude = 0f;

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
                amplitude = getStepAmplitude();
                if (amplitudeCheck(amplitude) && sequenceCheck()) {
                    stepDetected = true;
                    stepDetected();
                }
                // Réinitialise la machine à états
                m_fLastMax = 0f;
                m_fLastMin = 0f;
                m_iCaptureStartTime = -1;
                setState(STATE_CAPTURING);
            }
            break;
        }

        m_history.add(
            now,
            _x,
            _y,
            _z,
            stepDetected,
            amplitude
        );
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

    private float getStepAmplitude() {
        return m_fLastMax - m_fLastMin;
    }

    private boolean amplitudeCheck(float amplitude) {
        return amplitude > getAmplitudeMinimum();
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
        float stepLength = computeStepLength();
        // Appel de stepDetected sur chaque listener
        for(IStepListener _listener : m_stepListenerList) {
            _listener.stepDetected(stepLength);
        }
    }

    /*
     * Calcule la taille du pas courant.
     * Cette méthode n'est sensée être appellée qu'avant une transition
     * de l'état final (pas détecté) vers l'état initial (phase de capture).
     */

    private float computeStepLength() {
        //long stepDuration = Calendar.getInstance().getTimeInMillis() - m_iCaptureStartTime;
        // L = 45% de la taille de la personne + 0.3 * vitesse de marche
        return CONSTANT_STEP_LENGTH;
    }

    /*
     * Remet à zéro l'historique de l'application.
     */
    public void resetHistory() {
        m_history.clear();
    }

    /*
     * Remet à zéro toute la mémoire de l'application.
     */
    public void resetAll() {
        resetHistory();
    }

    /*
     * Récupère l'amplitude minimale à dépasser pour valider un pas.
     * Prend en compte le mode de l'application (mono-axe ou multi-axe).
     */

    private float getAmplitudeMinimum() {
        return ((m_bMultiAxis) ? AMPLITUDE_DEFAULT_MINIMUM_MULTI_AXIS : AMPLITUDE_DEFAULT_MINIMUM_1_AXIS) * m_fAmplitudeSensibility;
    }

    /*
     * Récupère la borne minimale à dépasser pour changer d'état.
     * Prend en compte le mode de l'application (mono-axe ou multi-axe).
     */

    private float getNegativeLimit() {
        return ((m_bMultiAxis) ? NEGATIVE_DEFAULT_LIMIT_MULTI_AXIS : NEGATIVE_DEFAULT_LIMIT_1_AXIS) * m_fLimitSensibility;
    }

    /*
     * Récupère la borne maximale à dépasser pour changer d'état.
     * Prend en compte le mode de l'application (mono-axe ou multi-axe).
     */

    private float getPositiveLimit() {
        return ((m_bMultiAxis) ? POSITIVE_DEFAULT_LIMIT_MULTI_AXIS : POSITIVE_DEFAULT_LIMIT_1_AXIS) * m_fLimitSensibility;
    }

    /*
     * Spécifie l'écouteur de pas.
     */

    public void addStepListener(IStepListener _listener) {
        m_stepListenerList.add(_listener);
    }

    public void removeStepListener(IStepListener _listener) {
        m_stepListenerList.remove(_listener);
    }
    
    public void removeStepListener(int _indice) {
        m_stepListenerList.remove(_indice);
    }
    
}

