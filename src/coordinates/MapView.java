package coordinates;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.os.Message;
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
	Path northPath = new Path();
	CoordinateActivity activity;
	//LocalisationManager localisationManager;
	Position lastPosition;
	ArrayList<Position> positionsList = new ArrayList<Position>(); 
	
	boolean firstPoint = false;
	
	
	boolean init = false;
	/** * An atomic boolean to manage the external thread's destruction */
	AtomicBoolean isRunning = new AtomicBoolean(false);
	/** * An atomic boolean to manage the external thread's destruction */
	AtomicBoolean isPausing = new AtomicBoolean(false);
	/**
	 * The handler used to slow down the re-drawing of the view, else the device's battery is
	 * consumed
	 */
	private final Handler slowDownDrawingHandler;
	/**
	 * The thread that call the redraw
	 */
	private Thread background;
	
	
	
	public MapView(Context context){
		super(context);
		activity = (CoordinateActivity) context;
		
		
		northPath.moveTo(10, 5);
		northPath.lineTo(15, 25);
		northPath.lineTo(5, 25);
		northPath.close();
		
		slowDownDrawingHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				redraw();
			}
		};
		// Launching the Thread to update draw
		background = new Thread(new Runnable() {
			/**
			 * The message exchanged between this thread and the handler
			 */
			Message myMessage;

			// Overriden Run method
			public void run() {
				try {
					while (isRunning.get()) {
						if (isPausing.get()) {
							Thread.sleep(2000);
						} else {
							// Redraw to have 30 images by second
							//Thread.sleep(1000 / 30);
							Thread.sleep(3000);
							// Send the message to the handler (the handler.obtainMessage is more
							// efficient that creating a message from scratch)
							// create a message, the best way is to use that method:
							myMessage = slowDownDrawingHandler.obtainMessage();
							
							// then send the message
							slowDownDrawingHandler.sendMessage(myMessage);
						}
					}
				} catch (Throwable t) {
					// just end the background thread
				}
			}
		});
		// Initialize the threadSafe booleans
		isRunning.set(true);
		isPausing.set(false);
		// and start it
		background.start();
	}
	
	private void computeFirstPoint(){
		float x, y;
		x = getWidth()/2;
		y = getHeight()/2;
		lastPosition = new Position(x,y);
		positionsList.add(new Position(x,y));
	}
	
	private void redraw() {
		invalidate();
	}
	
	public void redraw(float x, float y){
		if(!firstPoint){
			computeFirstPoint();
			firstPoint = true;
		}
		lastPosition.x = (positionsList.get(0).x + x*20);
		lastPosition.y = (positionsList.get(0).y - y*20);
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
		//canvas.scale(getWidth(),getHeight());

		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.RED);
		canvas.drawPath(northPath, paint);
		paint.setColor(Color.BLACK);
		canvas.drawText("N", 5, 35, paint);
		
		int size = positionsList.size();
		for(int i=0; i<size; i++){
			paint.setColor(Color.RED);
			canvas.drawCircle(positionsList.get(i).x,positionsList.get(i).y, 3, paint);
			
			if(i<size-1){
				paint.setColor(Color.BLACK);
				canvas.drawLine(positionsList.get(i).x,positionsList.get(i).y,
								positionsList.get(i+1).x,positionsList.get(i+1).y, paint);
			}		
		}
		canvas.restore();
	}
}
