package com.example.app_2.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Toast;

import com.example.app_2.R;
import com.example.app_2.fragments.ImageDetailsFragment;
import com.example.app_2.fragments.ImageGridFragment;

public class ImageDetailsActivity extends FragmentActivity{
	private final static String TAG = "ImageDetailsActivity";
	ImageDetailsFragment details;
	
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
        	details = new ImageDetailsFragment();
            details.setArguments(getIntent().getExtras());
            
            if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
                final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(android.R.id.content, details, TAG);
                ft.commit();
            }
        }
    }
    
    public void onButtonClick(View view) {
		switch (view.getId()) {
		case R.id.select_parents:
			Intent i = new Intent(this, ParentMultiselectActivity.class);
			startActivityForResult(i, 1);
			break;
			
		case R.id.submit_button:
			details.onButtonClick(view);
			break;	
		}
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    	  if (requestCode == 1) {

    	     if(resultCode == RESULT_OK){      
    	         String result=data.getStringExtra("result");  
    	         Toast.makeText(this, result, Toast.LENGTH_LONG).show();
    	     }
    	     if (resultCode == RESULT_CANCELED) {    
    	         //Write your code if there's no result
    	     }
    	  }
    	}//onActivityResult

}
