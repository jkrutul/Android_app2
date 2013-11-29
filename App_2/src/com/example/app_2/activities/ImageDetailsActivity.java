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
	Long row_id;
	private static final int SELECT_PARENTS_REQUEST_CODE = 1;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		
        if (getResources().getConfiguration().orientation   == Configuration.ORIENTATION_LANDSCAPE) {
            finish();
            return;
        }

        if (savedInstanceState == null) {
        	row_id = getIntent().getExtras().getLong("row_id");
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
			i.putExtra("row_id", row_id);
			startActivityForResult(i, SELECT_PARENTS_REQUEST_CODE);
			break;
			
		case R.id.submit_button:
			details.onButtonClick(view);
			break;	
		}
		
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    	  if (requestCode == SELECT_PARENTS_REQUEST_CODE) {

    	     if(resultCode == RESULT_OK){  
    	    	 long checkedIds[] =  data.getLongArrayExtra("result_checked_ids");
    	    	 ImageDetailsFragment.addParents(checkedIds);
    	    	 details.setParentsView(checkedIds);
    	    	 String result = new String();
    	    	 for(Long i: checkedIds)
    	    		 result += " "+ i;
    	         long uncheckedIds[] = data.getLongArrayExtra("result_unchecked_ids");
    	         ImageDetailsFragment.deleteParents(uncheckedIds);
    	     }
    	     
    	     if (resultCode == RESULT_CANCELED) {    

    	     }
    	  }
    	}
}
