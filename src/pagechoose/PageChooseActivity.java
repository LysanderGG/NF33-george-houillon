package pagechoose;

import com.example.nf33.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class PageChooseActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_page_choose);
		
		//change the interface to Cap
		//TODO the interface of Cap
		Button buttonCap = (Button) findViewById(R.id.buttonCap);
		buttonCap.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				//intent.setClass(PageChooseActivity.this, .class);
				//startActivity(intent);

			}
		});
		
		//change the interface to Step
		Button buttonStep = (Button) findViewById(R.id.buttonStep);
		buttonStep.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(PageChooseActivity.this, steps.MainActivity.class);
				startActivity(intent);

			}
		});
		
		
		//change the interface to Coordinate
		Button buttonCoordinate = (Button) findViewById(R.id.buttonCoordinate);
		buttonCoordinate.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(PageChooseActivity.this, coordinates.CoordinateActivity.class);
				startActivity(intent);

			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.page_choose, menu);
		return true;
	}

}
