package com.examples.app_2.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.example.app_2.R;
import com.example.app_2.fragments.ImageDetailsFragment;
import com.example.app_2.fragments.ImageGridFragment;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.ImageLoader;

// za³adowawnie pustego ImageDetailsFragment
public class AddNewImageActivity extends FragmentActivity{
	 public static final String TAG = "AddnewImageActivity";
	 
	 @Override
	 protected void onCreate(Bundle savedInstanceState){
		 super.onCreate(savedInstanceState);
		  // During initial setup, plug in the details fragment.
         if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
             ImageDetailsFragment details = new ImageDetailsFragment();
             details.setArguments(getIntent().getExtras());
        	 final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	         ft.add(android.R.id.content, details, TAG);
	         ft.commit();	
         }	            
	 }
	 
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

}
