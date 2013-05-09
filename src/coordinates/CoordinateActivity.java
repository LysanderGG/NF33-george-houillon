package coordinates;

import com.example.nf33.R;
import com.example.nf33.R.layout;
import com.example.nf33.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class CoordinateActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_coordinate);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.coordinate, menu);
		return true;
	}

}
