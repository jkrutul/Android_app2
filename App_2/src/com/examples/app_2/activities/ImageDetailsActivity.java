package com.examples.app_2.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.example.app_2.R;
import com.example.app_2.fragments.ImageDetailsFragment;

public class ImageDetailsActivity extends FragmentActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		
        if (getResources().getConfiguration().orientation   == Configuration.ORIENTATION_LANDSCAPE) {
            // If the screen is now in landscape mode, we can show the
            // dialog in-line with the list so we don't need this activity.
            finish();
            return;
        }

        if (savedInstanceState == null) {
            // During initial setup, plug in the details fragment.
            ImageDetailsFragment details = new ImageDetailsFragment();
            details.setArguments(getIntent().getExtras());
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	            ft.add(android.R.id.content, details);
	            ft.commit();		

        }
    }

}
