package coordinates;

import java.util.ArrayList;
import java.lang.Math;

import cap.CapDetector;
import cap.CapListener;
import com.example.nf33.IStepListener;
import com.example.nf33.MainActivity;

public class LocalisationManager{
	public class Position{
		public float x;
		public float y;
		public float z;
		
		public Position(float x, float y, float z){
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
	
	private ArrayList<Position> positions = new ArrayList<Position>();
	private float currentPosition[] = {0,0,0};
	private float oldPosition[] = {0,0,0};
	private float cap;
	LocalisationListener localisationListener;
	
	CapDetector capDetector;
	
	//TODO Changer d'objet pour que ce soit plus propre
	MainActivity stepDetector;
	
	public LocalisationManager(){
		Position first = new Position(0,0,0);
		positions.add(first);
		
		localisationListener = null;
		
		capDetector = new CapDetector();
		stepDetector = new MainActivity();
		
		//On récupère les nouveaux cap en non stop
		capDetector.setHasChangedListener(new CapListener(){
			@Override
			public void hasChanged(float capn, float pitch, float roll){
				cap = capn;
			}
		});
		
		//dès qu'on détecte un pas, on prend le dernier cap relevé et on l'envoie
		//TODO Voir si on peut pas faire mieux
		stepDetector.setStepListener(new IStepListener(){
			@Override
			public void stepDetected(float _stepLength){
				computeNewPosition(_stepLength, cap);
			}
		});
	}
	
	public float[] getCurrentPosition(){		
		return currentPosition;
	}
	
	private void computeNewPosition(float _stepLength, float cap){
		//calculer la nouvelle position ac le cap
		float newPosition[] = {0,0,0};
		
		//ahah vive les maths, je sais plus comment on fait ça
		newPosition[0] = currentPosition[0] + _stepLength*(float)Math.sin(cap);
		newPosition[1] = currentPosition[1] + _stepLength*(float)Math.cos(cap);
		
		//M-a-J des variables
		oldPosition = currentPosition;
		currentPosition = newPosition;
		
		//déclencher le listener pour dire qu'il y a une nouvelle position
		localisationListener.onNewPosition(oldPosition, currentPosition);

		positions.add(new Position(newPosition[0],newPosition[1],0));
	}
	
	public void setLocalisationListener(LocalisationListener listener){
		localisationListener = listener;
	}
}
