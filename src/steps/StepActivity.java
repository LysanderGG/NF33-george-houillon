package steps;

import android.app.Activity;

public abstract class StepActivity extends Activity {
	public static final int AXIS_X  = 0;
	public static final int AXIS_Y  = 1;
	public static final int AXIS_Z  = 2;
	public static final int AXIS_3D = 3;
	
	public void setMajorAxis	(int _axis) 	{}
	public void setAxisBalance	(int _balance) 	{}
	public void setStepsCounter	(int _value)	{}
}
