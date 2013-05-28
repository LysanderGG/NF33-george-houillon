package coordinates;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class MapView extends View{
	public class Position{
		public float x,y;
		public Position(float x, float y){
			this.x = x;
			this.y = y; 
		}
	}
	
	//The paint to draw the view
	private Paint paint = new Paint();
	private Canvas canvas;
	
	CoordinateActivity activity;
	//LocalisationManager localisationManager;
	Position lastPosition;
	ArrayList<Position> positionsList = new ArrayList<Position>(); 
	
	boolean firstPoint = false;
	
	public MapView(Context context){
		super(context);
		activity = (CoordinateActivity) context;
			
		/*localisationManager = new LocalisationManager(activity);
		localisationManager.setLocalisationListener(new LocalisationListener() {
			@Override
			public void onNewPosition(float[] oldPosition, float[] newPosition) {
				lastPosition.x += newPosition[0];
				lastPosition.y += newPosition[1];
				positionsList.add(new Position(lastPosition.x,lastPosition.y));
				redraw();
			}
		});*/
	}
	
	private void computeFirstPoint(){
		float x, y;
		x = getWidth()/2;
		y = getHeight();
		lastPosition = new Position(x,y);
		positionsList.add(new Position(x,y));
	}
	
	public void redraw(float x, float y){
		if(!firstPoint){
			computeFirstPoint();
			firstPoint = true;
		}
		lastPosition.x += x;
		lastPosition.y += y;
		positionsList.add(new Position(lastPosition.x,lastPosition.y));
		invalidate();
	}
	
	@Override
	public void onDraw(Canvas canvas){
		if(!firstPoint){
			computeFirstPoint();
			firstPoint = true;
		}
		//draw map
		canvas.save();
		
		int size = positionsList.size();
		for(int i=0; i<size; i++){
			paint.setColor(Color.RED);
			canvas.drawPoint(positionsList.get(i).x,positionsList.get(i).y, paint);
			
			if(i<size-1){
				paint.setColor(Color.BLACK);
				canvas.drawLine(positionsList.get(i).x,positionsList.get(i).y,
								positionsList.get(i+1).x,positionsList.get(i+1).y, paint);
			}		
		}
		canvas.restore();
	}
}
