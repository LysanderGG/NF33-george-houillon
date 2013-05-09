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
	private float newPosition[] = {0,0,0};
	private float oldPosition[] = {0,0,0};
	
	
	
	public LocalisationManager(){
		Position first = new Position(0,0,0);
		positions.add(first);
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
