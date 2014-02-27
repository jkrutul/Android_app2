package com.example.app_2.activities;

import com.example.app_2.fragments.SettingsFragment;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

public class SettingsActivity extends Activity {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.holo_purple))); 
		
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

	}
}
