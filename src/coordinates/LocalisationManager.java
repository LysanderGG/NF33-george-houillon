package coordinates;

import java.util.ArrayList;
import com.example.nf33.IStepListener;

public class LocalisationManager implements IStepListener {
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
	
	private float cap;
	private ArrayList<Position> positions = new ArrayList<Position>();
	private float newPosition[] = new float[3];
	private float oldPosition[] = new float[3];
	
	
	
	public LocalisationManager(){
		
	}
	
	
	public void updateCap(float cap){
		this.cap = cap;
	}
	
	public float[] getCurrentPosition(){		
		return newPosition;
	}
	
	@Override
	public void stepDetected(float _stepLength){
		//calculer la nouvelle position ac le cap
		
		//déclencher le listener pour dire qu'il y a une nouvelle position
	}
}
