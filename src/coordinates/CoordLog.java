package coordinates;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import steps.MyLogs.LogItem;
import android.os.Environment;
import android.util.Log;

public class CoordLog {
	public class LogItem{
		private float currentCap;
		private boolean stepDetected;
		private float chosenCap;
		private long time;
		
		public LogItem(float _currentCap, boolean _stepDetected, float _chosenCap, long _time){
			currentCap = _currentCap;
			stepDetected = _stepDetected;
			chosenCap = _chosenCap;
			time = _time;
		}
		
		public float getCap(){
			return currentCap;
		}
		
		public boolean isStep(){
			return stepDetected;
		}
		
		public float getChosenCap(){
			return chosenCap;
		}
		
		public long getTime(){
			return time;
		}
	}
	
	private int maxSizeList;
	private LinkedList<LogItem> itemsList;
	private static String CSV_HEAD = "timestamp;current cap;step detected;chosen cap\n";
	
	public CoordLog(int _sizeList){
		maxSizeList = _sizeList;
		itemsList = new LinkedList<LogItem>();
	}
	
	public void add(float cap, boolean stepDetected, float chosenCap, long time){
		if(itemsList.size() > maxSizeList)
			itemsList.removeFirst();
		
		itemsList.addLast(new LogItem(cap, stepDetected, chosenCap, time));
	}
	
    public boolean writeLogFile(String _tag, String _dirname, String _filename, boolean _useDateInName) {

        try {
            // Vérifie que le périphérique de stockage est utilisable
            boolean externalStorageAvailable = false;
            boolean externalStorageWriteable = false;
            String state = Environment.getExternalStorageState();

            if (Environment.MEDIA_MOUNTED.equals(state)) {
                // Lecture/écriture
                externalStorageAvailable = externalStorageWriteable = true;
            } else if(Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                // Lecture seule
                externalStorageAvailable = true;
                externalStorageWriteable = false;
            } else {
                // Ni lecture ni écriture
                externalStorageAvailable = externalStorageWriteable = false;
            }
            
            Log.i(_tag, "\n\nExternal Media: readable=" + externalStorageAvailable + " writable=" + externalStorageWriteable);

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
            long lastTimestamp = itemsList.get(itemsList.size() - 1).getTime();
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
            for (LogItem li : itemsList) {
                txt = String.format(
                    "%d;%f;%d,%f\n",
                    li.time - lastTimestamp,
                    li.currentCap,
                    li.isStep()?1:0,
                    li.chosenCap
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

    public void clear() {
        itemsList.clear();
    }

}
