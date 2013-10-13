package com.examples.app_2.activities;

import com.example.app_2.R;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ShareActionProvider;

public class MainActivity extends Activity {
	private final static String LOG_TAG = "MainActivity";
	private ShareActionProvider mShareActionProvider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);		
	}


	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// Inflate menu resource file.
		getMenuInflater().inflate(R.menu.main, menu);
		// Locate MenuItem with ShareActionProvider
		MenuItem item = menu.findItem(R.id.menu_item_share);
		// Fetch and store ShareActionProvider
		mShareActionProvider = (ShareActionProvider) item.getActionProvider();
		return true;
	}	
	
	public void onClick(View view){
		Intent intent;
		switch(view.getId()){
			case R.id.grid_activity:
				intent = new Intent(this, ImageGridActivity.class);
				//intent = new Intent(this, ProgressActivity.class);
				startActivity(intent);
				break;
		
			case R.id.db_test_activity:
				intent = new Intent(this, DatabaseTestActivity.class);
				startActivity(intent);
				break;
				
			case R.id.client_activity:
				intent = new Intent(this, SimpleClientActivity.class);
				startActivity(intent);
				break;
			
			case R.id.image_overview_activity:
				intent = new Intent(this, ImagesOverviewActivity.class);
				startActivity(intent);
				break;
				
			case R.id.swipe_activity:
				intent = new Intent(this, SwipeActivity.class);
				startActivity(intent);
				break;
				
			default:
				intent = null;
				break;
		}

	}

}
