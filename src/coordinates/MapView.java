package coordinates;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class MapView extends View{
	
	//The paint to draw the view
	private Paint paint = new Paint();
	private Canvas canvas;
	
	CoordinateActivity activity;
	LocalisationManager localisationManager;
	float[] newPosition, currentPoint;
	
	public MapView(Context context){
		super(context);
		activity = (CoordinateActivity) context;
		
		newPosition = new float[2];
		currentPoint = new float[2];
		
		computeFirstPoint();
		
		// TODO - proprement
		localisationManager = new LocalisationManager(activity);
		localisationManager.setLocalisationListener(new LocalisationListener() {
			@Override
			public void onNewPosition(float[] oldPosition, float[] newPosition) {
				MapView.this.newPosition[0] = currentPoint[0] + newPosition[0];
				MapView.this.newPosition[1] = currentPoint[1] + newPosition[1];
				redraw();
			}
		});
	}
	
	private void computeFirstPoint(){
		float x, y;
		x = getWidth()/2;
		y = getHeight();
		
		currentPoint[0] = x;
		currentPoint[0] = y;
	}
	
	private void redraw(){
		invalidate();
	}
	
	@Override
	public void onDraw(Canvas canvas){
		//draw map
		canvas.save();
		
		paint.setColor(Color.BLACK);
		canvas.drawLine(currentPoint[0], currentPoint[1], newPosition[0], newPosition[1], paint);
		paint.setColor(Color.RED);
		canvas.drawPoint(newPosition[0], newPosition[1], paint);
		
		canvas.restore();
		
		currentPoint[0] = newPosition[0];
		currentPoint[1] = newPosition[1];
	}
}
