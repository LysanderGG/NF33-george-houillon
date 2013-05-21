package coordinates;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class mapView extends View{
	
	//The paint to draw the view
	private Paint paint = new Paint();
	
	private Canvas canvas;
	
	CoordinateActivity activity;
	LocalisationManager localisationManager;
	
	public mapView(Context context){
		super(context);
		activity = (CoordinateActivity) context;
		
		// TODO - proprement
		localisationManager = new LocalisationManager(activity);
		computeFirstPoint();
	}
	
	private void computeFirstPoint(){
		
	}
	
	private void redraw(){
		invalidate();
	}
	
	@Override
	public void onDraw(Canvas canvas){
		//draw map
		
	}
}
