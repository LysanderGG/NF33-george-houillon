package steps;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.util.Log;

/*
 * Enregistre les mesures successives du senseur.
 */

@SuppressLint({ "DefaultLocale", "SimpleDateFormat" })
public class MyLogs {

	/*
	 * Un enregistrement des 3 axes à un instant donné.
	 */

	public class LogItem {
		private final float[] 	m_coords;
		private final long 		m_lTime;
		private boolean 		m_bIsStepDetected;

		public LogItem(float[] _coords, boolean _isStepDetected, long _time) {
			m_coords 			= _coords;
			m_bIsStepDetected	= _isStepDetected;
			m_lTime 			= _time;
		}

		public float[] getCoords() {
			return m_coords;
		}

		public float getX() {
			return m_coords[0];
		}

		public float getY() {
			return m_coords[1];
		}

		public float getZ() {
			return m_coords[2];
		}

		public boolean getIsStepDetected() {
			return m_bIsStepDetected;
		}

		public long getTime() {
			return m_lTime;
		}

		public void setIsStepDetected(boolean _value) {
			m_bIsStepDetected = _value;
		}
	}

	private final int					m_iLength;
	private final ArrayList<LogItem>	m_list;

	private static final String CSV_HEAD = "timestamp;X;Y;Z;StepDetected\n";

	public ArrayList<LogItem> getList() {
		return m_list;
	}

	public MyLogs(int _length) {
		m_iLength 	= (_length > 0) ? _length : 0;
		m_list 		= new ArrayList<LogItem>(m_iLength);
	}

	/*
	 * Ajoute un enregistrement à l'historique.
	 */

	public void add(long _time, float _x, float _y, float _z) {
		add(_time, _x, _y, _z, false);
	}

	/*
	 * Ajoute un enregistrement spécifiant ou non un pas à l'historique.
	 * Instancie un élément de type /LogItem/.
	 */

	public void add(long _time, float _x, float _y, float _z, boolean _isStepDetected) {
		if (m_iLength > 0 && m_list.size() >= m_iLength) {
			// Rotation de l'historique
			m_list.remove(m_list.size()-1);
		}
		m_list.add(0, new LogItem(new float[] {_x, _y, _z}, _isStepDetected, _time));
	}

	/*
	 * Enregistre l'historique courante sous la forme d'un fichier texte CSV.
	 * Chaque ligne indique un enregistrement, donnant:
	 *  . l'instant de capture de l'échantillon (en millisecondes, relatives à
	 *    l'instant début de l'enregistrement)
	 *  . la valeur de l'accéléromètre selon X
	 *  . la valeur de l'accéléromètre selon Y
	 *  . la valeur de l'accéléromètre selon Z
	 */

	public boolean writeLogFile(String _tag, String _dirname, String _filename, boolean _useDateInName) {

		try {
			// Vérifie que le périphérique de stockage est utilisable
			boolean mExternalStorageAvailable = false;
		    boolean mExternalStorageWriteable = false;
		    String state = Environment.getExternalStorageState();

		    if (Environment.MEDIA_MOUNTED.equals(state)) {
		        // Lecture/écriture
		        mExternalStorageAvailable = mExternalStorageWriteable = true;
		    } else if(Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		        // Lecture seule
		        mExternalStorageAvailable = true;
		        mExternalStorageWriteable = false;
		    } else {
		        // Ni lecture ni écriture
		        mExternalStorageAvailable = mExternalStorageWriteable = false;
		    }

			// Création des dossiers
            File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File dir = new File (root, _dirname);
            if (!dir.exists()) {
            	if (!dir.mkdirs()) {
            		Log.i(_tag, "Unable to create " + dir.getAbsolutePath());
            		return false;
            	}
            }

            // Création du fichier
            if (_useDateInName) {
            	SimpleDateFormat dateFormat = new SimpleDateFormat(_filename);
        		Date date = new Date();
        		_filename = dateFormat.format(date);
            }
            File file = new File(dir, _filename);
            if (!file.exists()) {
               try {
                  file.createNewFile();
               } catch (IOException e) {
            	   Log.i(_tag, "Unable to create " + file.getAbsolutePath());
            	   return false;
               }
            }

            // Déduit de chaque timestamp la valeur du plus ancien afin
            // de rendre les valeurs relatives au début de l'enregistrement
            long lastTimestamp = m_list.get(m_list.size() - 1).getTime();
            BufferedWriter buf;

            try {
            	buf = new BufferedWriter(new FileWriter(file, false));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.i(_tag, "File not found");
                return false;
            }

            // Génération des données texte
			String txt;
			buf.append(CSV_HEAD);
			for (LogItem li : m_list) {
				txt = String.format(
					"%d;%f;%f;%f;%d\n",
					li.m_lTime - lastTimestamp,
					li.m_coords[0],
					li.m_coords[1],
					li.m_coords[2],
					(li.m_bIsStepDetected) ? 1 : 0
				);
				buf.append(txt);
			}
            buf.newLine();
            buf.close();
        } catch (IOException e) {
        	e.printStackTrace();
            Log.e(_tag, "File write failed: " + e.toString());
            return false;
        }
		return true;
	}

	/*
	 * Vide l'historique.
	 */

	public void clear() {
		m_list.clear();
	}

	/*
	 * Marque le dernier échantillon comme celui qui achève un pas.
	 */

	public void addStepDetected() {
		m_list.get(0).setIsStepDetected(true);
	}
}
