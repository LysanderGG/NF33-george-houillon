package coordinates;

import java.lang.Math;

import cap.CapDetector;
import cap.CapListener;
import steps.IStepListener;
import steps.StepActivity;
import steps.StepDetector;

public class LocalisationManager{

	private float currentPosition[] = {0,0,0};
	private float oldPosition[] = {0,0,0};
	private float cap;
	
	LocalisationListener localisationListener;
	CapDetector capDetector;
	StepDetector stepDetector;
	
	public LocalisationManager(CoordinateActivity ativity){
		localisationListener = null;
		
		capDetector = new CapDetector();
		stepDetector = new StepDetector(ativity);
		
		//On recupere les nouveaux cap en non-stop
		capDetector.addHasChangedListener(new CapListener(){
			@Override
			public void hasChanged(float capn, float pitch, float roll){
				cap = capn;
			}
		});
		
		//des qu'on detecte un pas, on prend le dernier cap releve et on l'envoie
		//TODO Voir si on peut pas faire mieux
		stepDetector.addStepListener(new IStepListener() {
			@Override
			public void stepDetected(float _stepLength) {
				computeNewPosition(_stepLength, cap);
			}
		});
		stepDetector.registerSensors();
	}
	
	private void computeNewPosition(float _stepLength, float cap){
		//calculer la nouvelle position ac le cap
		float newPosition[] = {0,0,0};
		
		if(cap < 0)
			cap += 360;
		
		//ahah vive les maths, je sais plus comment on fait ca
		newPosition[0] = currentPosition[0] + _stepLength*(float)Math.sin(cap);
		newPosition[1] = currentPosition[1] + _stepLength*(float)Math.cos(cap);
		
		//M-a-J des variables
		oldPosition = currentPosition;
		currentPosition = newPosition;
		
		//declencher le listener pour dire qu'il y a une nouvelle position
		localisationListener.onNewPosition(oldPosition, currentPosition);
	}
	
	public CapDetector getCapDetector(){
		return capDetector;
	}
	
	public StepDetector getStepDetector(){
		return stepDetector;
	}
	
	public void setLocalisationListener(LocalisationListener listener){
		localisationListener = listener;
	}
}
