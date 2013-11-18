package com.example.app_2.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ShareActionProvider;

import com.example.app_2.App_2;
import com.example.app_2.R;
import com.example.app_2.provider.Images;
import com.example.app_2.storage.Database;
import com.example.app_2.storage.Storage;

public class MainActivity extends Activity {
	private final static String LOG_TAG = "MainActivity";
	private ShareActionProvider mShareActionProvider;
	private SharedPreferences prefs = null;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		
		
		LinearLayout ll = (LinearLayout) findViewById(R.id.main_activity);
		ll.setBackgroundDrawable(App_2.wallpaperDrawable);


		ActionBar actionBar = getActionBar();
		actionBar.setSubtitle("G�owne menu");
		//actionBar.setTitle("vogella.com"); 
		prefs = getSharedPreferences("com.example.app_2", MODE_PRIVATE);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	       if (prefs.getBoolean("firstrun", true)) {
	            // Do first run stuff here then set 'firstrun' as false
	            // using the following line to edit/commit prefs
	            prefs.edit().putBoolean("firstrun", false).commit();
	        }
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			 new AlertDialog.Builder(this)
		        .setTitle("Czy usun�c informacje oraz relacje o obrazkach?")
		        .setMessage("Obrazki zostan� dodane do g��wnej kategori")
		        .setNegativeButton(android.R.string.no, null)
		        .setPositiveButton(android.R.string.yes, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Database.open();
						Database.recreateDB();
						
						Images.addImagesToDatabase(Storage.getThumbsMaxDir().getAbsolutePath(), "-1");
					}
		        }).create().show();
			 break;
		}
		return true;
	}

	public void onClick(View view) {
		Intent intent;
		switch (view.getId()) {
		case R.id.grid_activity:
			intent = new Intent(this, ImageGridActivity.class);
			// intent = new Intent(this, ProgressActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.right_slide_in,
					R.anim.right_slide_out);
			break;

		case R.id.client_activity:
			intent = new Intent(this, SimpleClientActivity.class);
			startActivity(intent);
			break;

		case R.id.swipe_activity:
			intent = new Intent(this, SwipeActivity.class);
			startActivity(intent);
			break;
			
		case R.id.edit_activity:
			intent = new Intent(this, ImageEditActivity.class);
			startActivity(intent);
			break;
		
		case R.id.users_activity:
			intent = new Intent(this, UsersActivity.class);
			startActivity(intent);
			break;
			
		case R.id.settings_activity:
			intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			break;
			
		default:
			intent = null;
			break;
		}

	}

}
