package com.example.app_2.activities;

import java.io.File;
import java.io.IOException;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ShareActionProvider;

import com.example.app_2.R;
import com.example.app_2.storage.Database;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.ImageLoader;

public class MainActivity extends Activity {
	private final static String LOG_TAG = "MainActivity";
	private ShareActionProvider mShareActionProvider;
	private SharedPreferences prefs = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		
		LinearLayout ll = (LinearLayout) findViewById(R.id.main_activity);
		//Utils.setWallpaper(ll, App_2.maxHeight, App_2.getMaxWidth(), null, ScalingLogic.CROP);

		ActionBar actionBar = getActionBar();
		actionBar.setSubtitle("G³owne menu");
		prefs = getSharedPreferences("com.example.app_2", MODE_PRIVATE);
		Log.i("PREFS", prefs.getString("pref_img_size", "def val"));
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// Inflate menu resource file.
		getMenuInflater().inflate(R.menu.main, menu);
		// Locate MenuItem with ShareActionProvider
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			 new AlertDialog.Builder(this)
		        .setTitle("Czy usun¹c informacje oraz relacje o obrazkach?")
		        .setMessage("Obrazki zostan¹ dodane do g³ównej kategori")
		        .setNegativeButton(android.R.string.no, null)
		        .setPositiveButton(android.R.string.yes, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Database.open();
						Database.recreateDB();
						File directory = Storage.getAppRootDir();
						try {
							Storage.delete(directory);
						} catch (IOException e) {
						
						}
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
			
		case R.id.user_login_activity:
			intent = new Intent(this, UserLoginActivity.class);
			startActivity(intent);
			break;
		default:
			intent = null;
			break;
		}

	}

	
}
