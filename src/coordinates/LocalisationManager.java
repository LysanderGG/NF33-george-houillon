package coordinates;

import java.lang.Math;
import java.util.Calendar;

import cap.CapDetector;
import cap.CapListener;
import steps.IStepListener;
import steps.StepDetector;

public class LocalisationManager{
	
	private CoordLog log;
	private boolean startLog = false;
	
	private float currentPosition[] = {0,0,0};
	private float oldPosition[] = {0,0,0};
	private float currentCap;
	private float oldCap;
	
	LocalisationListener localisationListener;
	CapDetector capDetector;
	StepDetector stepDetector;
	
	public LocalisationManager(CoordinateActivity activity){
		log = new CoordLog(1000);
		localisationListener = null;
		
		capDetector = new CapDetector(true);
		stepDetector = new StepDetector(activity);
		
		//On recupere les nouveaux cap en non-stop
		capDetector.addHasChangedListener(new CapListener(){
			@Override
			public void hasChanged(float capn, float oldCapn,float pitch, float roll){
				if(capDetector.isV2())
					oldCap = oldCapn;
				else
					oldCap = -1;
				currentCap = capn;
				log.add(currentCap<0?currentCap+360:currentCap, false, -1, Calendar.getInstance().getTimeInMillis());
			}
		});
		
		//des qu'on detecte un pas, on prend le dernier cap releve et on l'envoie
		//TODO Voir si on peut pas faire mieux
		stepDetector.addStepListener(new IStepListener() {
			@Override
			public void stepDetected(float _stepLength) {
				if(capDetector.isV2()){
					computeNewPosition(_stepLength, oldCap);
					capDetector.clearList();
					if(startLog)
						log.add(currentCap<0?currentCap+360:currentCap, true, oldCap<0?oldCap+360:oldCap, Calendar.getInstance().getTimeInMillis());
				}
				else{
					computeNewPosition(_stepLength, currentCap);
				}
			}
		});
		stepDetector.registerSensors();
	}
	
	private void computeNewPosition(float _stepLength, float _cap){
		//calculer la nouvelle position ac le cap
		float newPosition[] = {0,0,0};
		
		if(_cap < 0)
			_cap += 360;

		newPosition[0] = currentPosition[0] + _stepLength*(float)Math.sin(Math.toRadians(_cap));
		newPosition[1] = currentPosition[1] + _stepLength*(float)Math.cos(Math.toRadians(_cap));
		
		//M-a-J des variables
		oldPosition = currentPosition;
		currentPosition = newPosition;
		
		//declencher le listener pour dire qu'il y a une nouvelle position
		localisationListener.onNewPosition(oldPosition, currentPosition);
	}
	
	public float getCurrentCap(){
		return currentCap;
	}
	
	public CoordLog getCoordLog(){
		return log;
	}
	
	public CapDetector getCapDetector(){
		return capDetector;
	}
	
	public StepDetector getStepDetector(){
		return stepDetector;
	}
	
	public void setStartLog(boolean start){
		startLog = start;
	}
	
	public void setLocalisationListener(LocalisationListener listener){
		localisationListener = listener;
	}
}
