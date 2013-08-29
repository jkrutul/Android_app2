package com.examples.app_2.activities;

import com.example.app_2.R;
import com.example.app_2.R.layout;
import com.example.app_2.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class FGILActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fgil);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.fgil, menu);
		return true;
	}

}
